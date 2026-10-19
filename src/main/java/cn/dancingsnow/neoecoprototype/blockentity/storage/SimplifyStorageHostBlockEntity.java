package cn.dancingsnow.neoecoprototype.blockentity.storage;

import appeng.api.networking.IGridNodeListener;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEItemKey;
import gripe._90.megacells.misc.CompressionService;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCellItem;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.gui.storage.StorageHostActionUI;
import cn.dancingsnow.neoecoae.gui.storage.StorageHostUI;
import cn.dancingsnow.neoecoae.gui.theme.NEStyleSheets;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockBuildController;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementPlan;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementService;
import cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyStorageControllerBlock;
import cn.dancingsnow.neoecoprototype.integration.ae2.SimplifyGridFacade;
import cn.dancingsnow.neoecoprototype.items.SimplifyConcreteStorageCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkStorageCellItem;
import cn.dancingsnow.neoecoprototype.integration.omni.SimplifyComplexUniversalStorageCellItem;
import cn.dancingsnow.neoecoprototype.integration.omni.SimplifySpentNuclearWasteCellItem;
import cn.dancingsnow.neoecoprototype.integration.omni.SimplifyUniversalStorageCellItem;
import cn.dancingsnow.neoecoprototype.integration.omni.SimplifyQuantumStorageCellItem;
import net.neoforged.fml.ModList;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyStorageClusterCalculator;
import cn.dancingsnow.neoecoprototype.multiblock.cluster.SimplifyStorageCluster;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyStorageDefinition;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncPersistRPCBlockEntity;
import com.lowdragmc.lowdraglib2.syncdata.storage.FieldManagedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** L1 storage controller state and host for the reusable eco builder UI. */
public class SimplifyStorageHostBlockEntity
        extends NEBlockEntity<SimplifyStorageCluster, SimplifyStorageHostBlockEntity>
        implements ISyncPersistRPCBlockEntity, IStorageProvider, MultiBlockBuildController.Host {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifyStorageHostBlockEntity.class);
    private static final int DEFAULT_BUILD_LENGTH = 1;

    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @Override
    public FieldManagedStorage getSyncStorage() {
        return syncStorage;
    }

    @Persisted
    @DescSynced
    private int selectedBuildLength = DEFAULT_BUILD_LENGTH;

    @Persisted
    @DescSynced
    private boolean mirrorBuild;

    @Persisted
    @DescSynced
    private int storagePriority;

    @DescSynced
    private boolean buildInProgress;

    private final MultiBlockBuildController buildController = new MultiBlockBuildController(this);
    private boolean mirrored;
    private boolean communicationInterface;

    public SimplifyStorageHostBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, SimplifyStorageClusterCalculator::new);
    }

    /** Drives own the finite cells; the host still advertises the storage service like eco's controller. */
    @Override
    public void mountInventories(IStorageMounts storageMounts) {
    }

    @Override
    public void updateCluster(SimplifyStorageCluster next) {
        boolean wasAttached = getCluster() != null;
        super.updateCluster(next);
        boolean isAttached = getCluster() != null;
        if (level != null && !level.isClientSide && wasAttached != isAttached) {
            LOGGER.debug("host @{} cluster attach={} (tick {})", worldPosition, isAttached,
                    level.getServer() != null ? level.getServer().getTickCount() : -1);
            if (getCluster() != null) {
                SimplifyGridFacade.requestStorageUpdate(getMainNode());
                int onlineDrives = 0;
                int poweredDrives = 0;
                int connectedDrives = 0;
                for (SimplifyDriveBlockEntity drive : getCluster().getDrives()) {
                    var managedNode = drive.getMainNode();
                    SimplifyGridFacade.requestStorageUpdate(managedNode);
                    if (drive.isOnline()) {
                        onlineDrives++;
                    }
                    if (managedNode.isPowered()) {
                        poweredDrives++;
                    }
                    if (SimplifyGridFacade.isConnected(managedNode)) {
                        connectedDrives++;
                    }
                }
                LOGGER.debug("L1 drives status @{}: total={}, online={}, powered={}, connected={}",
                        worldPosition, getCluster().getDrives().size(), onlineDrives, poweredDrives, connectedDrives);
                if (getCluster().getTheInterface() != null) {
                    SimplifyGridFacade.requestStorageUpdate(getCluster().getTheInterface().getMainNode());
                }
            }
        }
    }

    /** Server ticker used by the incremental non-creative builder. */
    public static void tick(Level level, BlockPos pos, BlockState state, SimplifyStorageHostBlockEntity host) {
        boolean wasBuilding = host.isBuildInProgress();
        host.buildController.tick(level);
        if (wasBuilding && !host.isBuildInProgress() && level instanceof ServerLevel) {
            LOGGER.info("L1 auto-build finished @{} formed={} length={} mirrored={}",
                    pos, host.isFormed(), host.getSelectedBuildLength(), host.isMirrorBuild());
        }
    }

    public SimplifyTier getTier() {
        return SimplifyTier.L1;
    }

    public void setMirrored(boolean mirrored) {
        this.mirrored = mirrored;
    }

    /** Recorded by the calculator; published onto the block state by {@link #updateState(boolean)}. */
    public void setCommunicationInterface(boolean communicationInterface) {
        this.communicationInterface = communicationInterface;
    }

    public int getStoragePriority() {
        return storagePriority;
    }

    public void setStoragePriority(int storagePriority) {
        if (this.storagePriority == storagePriority) {
            return;
        }
        this.storagePriority = storagePriority;
        setChanged();
        refreshDriveStorageProviders();
    }

    private void refreshDriveStorageProviders() {
        SimplifyStorageCluster cluster = getCluster();
        if (cluster == null) {
            return;
        }
        SimplifyGridFacade.requestStorageUpdate(getMainNode());
        for (SimplifyDriveBlockEntity drive : cluster.getDrives()) {
            SimplifyGridFacade.requestStorageUpdate(drive.getMainNode());
        }
        if (cluster.getTheInterface() != null) {
            SimplifyGridFacade.requestStorageUpdate(cluster.getTheInterface().getMainNode());
        }
    }

    @Override
    public void onReady() {
        getMainNode().addService(IStorageProvider.class, this);
        super.onReady();
        getMainNode().setIdlePowerUsage(SimplifyPowerProfile.L1.storageControllerIdlePower());
    }

    @Override
    public void updateState(boolean updateExposed) {
        super.updateState(updateExposed);
        if (level == null || level.isClientSide || isRemoved()) {
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (state.hasProperty(SimplifyStorageControllerBlock.MIRRORED)) {
            boolean formed = isFormed();
            BlockState newState = state
                    .setValue(SimplifyStorageControllerBlock.MIRRORED, formed && mirrored)
                    .setValue(SimplifyStorageControllerBlock.COMMUNICATION_INTERFACE,
                            formed && communicationInterface);
            if (newState != state) {
                level.setBlock(worldPosition, newState, Block.UPDATE_CLIENTS);
            }
        }
    }

    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        UIElement root = StorageHostUI.create(createStoragePanelConfig(holder.player))
                .addClass("panel_bg");

        StorageHostActionUI.Elements actionUI = StorageHostActionUI.create(new StorageHostActionUI.Config(
                holder.player,
                () -> selectedBuildLength,
                () -> mirrorBuild,
                mirror -> buildController.setMirrorBuild(holder.player, mirror),
                () -> buildController.decreaseBuildLength(holder.player),
                () -> buildController.increaseBuildLength(holder.player),
                () -> requestBuild(holder.player),
                this::isFormed,
                () -> buildInProgress,
                buildController::createLocalPreviewPlan,
                () -> storagePriority,
                priority -> {
                    if (canPlayerInteract(holder.player)) {
                        setStoragePriority(priority);
                    }
                },
                delta -> changeStoragePriority(holder.player, delta)));
        // eco 21.2.3 dropped the bulk-marking button from its action bar (the Config lost
        // bulkMarkingAvailable / bulkMarkingThreshold / autoMarkBulkCells and now calls the service only
        // from its own mega controller), so autoMarkBulkCells has no caller from this UI any more.
        actionUI.addTo(root);
        return new ModularUI(UI.of(root,
                java.util.List.of(StylesheetManager.INSTANCE.getStylesheetSafe(NEStyleSheets.ECO))), holder.player);
    }

    private boolean bulkMarkingAvailable() {
        return !smallBulkDrives().isEmpty();
    }

    private long bulkMarkingThreshold() {
        return NeoECOPrototypeServerConfig.MEGA_BULK_AUTO_MARK_THRESHOLD.get();
    }

    private void autoMarkBulkCells(Player player) {
        if (!canPlayerInteract(player)) {
            return;
        }
        var counts = runAutoMark();
        // One line, because the action bar shows one line: a second call would erase the first.
        var line = Component.translatable("gui.neoecoprototype.storage.bulk_mark.result",
                counts.added(), counts.alreadyMarked());
        if (counts.noSpace() > 0) {
            line = line.append(Component.translatable("gui.neoecoprototype.storage.bulk_mark.no_space",
                    counts.noSpace()));
        }
        player.displayClientMessage(line, true);
    }

    /**
     * What one pass of the marking button did.
     *
     * @param noSpace candidates that were over the threshold and in a chain nothing marks yet, but found
     *                no free marker slot - without this the button just reports fewer marks and the
     *                player cannot tell "nothing was worth marking" from "the cell is full of marks"
     */
    public record BulkMarkCounts(int added, int alreadyMarked, int noSpace) {
    }

    /**
     * The keys worth marking in this pass, biggest pile first, with one drive left out of the pool.
     *
     * <p>Aggregated before anything else: the same key sitting in three source cells is one candidate for
     * one chain, not three, and ordering means nothing while duplicates are still in the way. The order is
     * what eco's own pass fills by, and it is not cosmetic - the marked form is what decides the fold
     * factor the cell then uses, so an unordered pool can mark the cheap end of a chain.</p>
     */
    private static List<AEItemKey> markingCandidates(
            java.util.Map<SimplifyDriveBlockEntity, appeng.api.stacks.KeyCounter> availableByDrive,
            SimplifyDriveBlockEntity except, long threshold) {
        java.util.Map<AEItemKey, Long> amounts = new java.util.HashMap<>();
        for (var source : availableByDrive.entrySet()) {
            if (source.getKey() == except) {
                continue;
            }
            for (var entry : source.getValue()) {
                if (!(entry.getKey() instanceof AEItemKey key)
                        || CompressionService.getChain(key).isEmpty()) {
                    continue;
                }
                amounts.merge(key, entry.getLongValue(), Long::sum);
            }
        }
        return amounts.entrySet().stream()
                .filter(entry -> entry.getValue() > threshold)
                .sorted(java.util.Map.Entry.<AEItemKey, Long>comparingByValue().reversed())
                .map(java.util.Map.Entry::getKey)
                .toList();
    }

    /**
     * One pass of the marking, without a player: the button only adds the interaction check and the chat line,
     * so this is the half a test can reach.
     */
    public BulkMarkCounts runAutoMark() {
        if (level == null || level.isClientSide) {
            return new BulkMarkCounts(0, 0, 0);
        }
        if (!ModList.get().isLoaded("megacells")) {
            // The chain lookup further down is MegaCells' own code. Small-bulk cells cannot be
            // registered without the mod, but a world made while it was installed still carries the
            // drives, so the button can still be pressed with the mod gone.
            return new BulkMarkCounts(0, 0, 0);
        }
        long threshold = bulkMarkingThreshold();
        // One inventory read per drive, not one per (target, source) pair: every small-bulk drive in the
        // cluster is offered the same sources, and getAvailableStacks walks the cell each time it is called.
        var availableByDrive = new java.util.IdentityHashMap<SimplifyDriveBlockEntity, appeng.api.stacks.KeyCounter>();
        for (SimplifyDriveBlockEntity source : getCluster().getDrives()) {
            IECOStorageCell inventory = source.getCellInventory();
            if (inventory == null) {
                continue;
            }
            appeng.api.stacks.KeyCounter available = new appeng.api.stacks.KeyCounter();
            inventory.getAvailableStacks(available);
            availableByDrive.put(source, available);
        }
        int added = 0;
        int alreadyMarked = 0;
        int noSpace = 0;
        List<AEItemKey> markedKeys = new ArrayList<>();
        for (SimplifyDriveBlockEntity target : smallBulkDrives()) {
            ItemStack targetStack = target.getCellStack();
            if (!(targetStack.getItem() instanceof SimplifySmallBulkStorageCellItem item)) {
                continue;
            }
            var config = item.getConfigInventory(targetStack);
            for (AEItemKey key : markingCandidates(availableByDrive, target, threshold)) {
                boolean exists = markedKeys.stream().anyMatch(existing -> sameMarkerChain(existing, key));
                if (!exists) {
                    for (int slot = 0; slot < config.size(); slot++) {
                        if (config.getKey(slot) instanceof AEItemKey existing
                                && sameMarkerChain(existing, key)) {
                            exists = true;
                            break;
                        }
                    }
                }
                if (exists) {
                    alreadyMarked++;
                    continue;
                }
                int free = -1;
                for (int slot = 0; slot < config.size(); slot++) {
                    if (config.getKey(slot) == null) {
                        free = slot;
                        break;
                    }
                }
                if (free < 0) {
                    noSpace++;
                    continue;
                }
                config.setStack(free, new appeng.api.stacks.GenericStack(key, 0L));
                markedKeys.add(key);
                added++;
            }
        }
        refreshDriveStorageProviders();
        return new BulkMarkCounts(added, alreadyMarked, noSpace);
    }

    /**
     * eco's own rule for "these two markers stand for the same compression chain", so the two hosts cannot
     * drift apart on what counts as already marked. It treats an empty chain as never matching, which is what
     * the gate above relies on.
     */
    public static boolean sameMarkerChain(AEItemKey left, AEItemKey right) {
        return cn.dancingsnow.neoecoae.integration.StorageBulkMarkingIntegration
                .isSameMarkerChain(left.toStack(), right.toStack());
    }

    public List<SimplifyDriveBlockEntity> smallBulkDrives() {
        SimplifyStorageCluster cluster = getCluster();
        if (cluster == null) {
            return List.of();
        }
        return cluster.getDrives().stream()
                .filter(drive -> drive.getCellStack() != null
                        && drive.getCellStack().getItem() instanceof SimplifySmallBulkStorageCellItem)
                .toList();
    }

    /** Public for the same reason {@link #createCellEntries()} is: the suite has to drive the panel through
     * the handles this config hands eco, not through the methods behind it. */
    public StorageHostUI.Config createStoragePanelConfig(
            net.minecraft.world.entity.player.Player player) {
        return new StorageHostUI.Config(
                () -> getBlockState().getBlock().getName(),
                // eco reads this int in three places and gates all of them on 1..3: the tier badge
                // (StorageHostCharts$1, skipped at <= 0), the host marker (StorageHostCharts$HostIcon,
                // skipped outside 1..3), and StorageCellGrid.tierName (tableswitch 1->"L4", 2->"L6",
                // 3->"L9", default->"L0"). So our 0 costs the two painted cells and prints "L0" -
                // confirmed in game, not inferred. Passing 1 would paint eco's L4 artwork on our own
                // panel, and getTier() also gates cell acceptance, so 0 stays.
                () -> getTier().getTier(),
                this::isFormed,
                this::getStoredEnergy,
                this::getMaxEnergy,
                this::panelIdlePowerPerTick,
                // eco renders this string as it is given it, and its own host ends the same slot with
                // HostText.ae2Amount(BigInteger): the panel shows "683K", not "683032".
                () -> cn.dancingsnow.neoecoae.gui.common.HostText.ae2Amount(getMaxLoadUsedBytes()),
                this::createCellEntries,
                createStorageTypeLines(),
                () -> false,
                () -> false,
                () -> false,
                () -> "",
                () -> 0,
                createReadOnlyEmptyComponentInventory(),
                player,
                (drivePos, interactionId, shiftDown) -> extractCellForPanel(drivePos, player, shiftDown),
                (drivePos, stack) -> insertCellForPanel(drivePos, stack, player),
                () -> new ItemStack(getBlockState().getBlock()));
    }

    /**
     * What the panel's consumption line asks for: the cost of the storage components themselves, in eco's
     * exact shape. Read off {@code ECOStorageHostStatistics.collectStorageUiSnapshot} at offsets 344..355 -
     * for every drive holding a cell, {@code Math.max(0, Math.round(cell.getIdleDrain()))}, added up.
     *
     * <p>It is deliberately not what the grid charges for the machine. Our cells report
     * {@code bytes / 1 MiB} AE per tick, so anything under 1M rounds to zero here and a host full of 16K
     * matrices reads 0 AE/t while the network still takes its ~32 AE/t per drive. That is what the line
     * means upstream and the label is eco's, so the number is eco's too; the tier-scaled figure each drive
     * publishes to its own node (see {@code SimplifyDriveBlockEntity#updateIdlePower}) is a different
     * quantity and stays out of here.</p>
     */
    public long panelIdlePowerPerTick() {
        SimplifyStorageCluster cluster = getCluster();
        if (cluster == null) {
            return 0L;
        }
        var total = 0L;
        for (var drive : cluster.getDrives()) {
            var cell = drive.getCellInventory();
            if (cell != null) {
                total += Math.max(0L, Math.round(cell.getIdleDrain()));
            }
        }
        return total;
    }

    /**
     * Takes the cell out of the drive at that position and hands it over the way eco does: a plain click
     * puts it on the cursor, a shift click looks for a free inventory slot.
     *
     * <p>Measured off {@code StorageCellUiExtraction.extract}: the shift branch scans
     * {@code Inventory.items} for the first empty index and gives up when there is none, the plain branch
     * gives up while the cursor still holds something, and both end in {@code broadcastChanges()}. Giving
     * up rather than dropping is the point - the cell stays in the drive, so a full inventory costs the
     * player nothing. The id coming back with the click is ours own (see
     * {@link SimplifyDriveBlockEntity#panelInteractionId()}) and exists only so eco sends the click at
     * all; the position is what selects the drive.</p>
     */
    public boolean extractCellForPanel(net.minecraft.core.BlockPos drivePos,
                                   net.minecraft.world.entity.player.Player player, boolean shiftDown) {
        if (level == null || level.isClientSide || !isFormed()) {
            return false;
        }
        // No distance check here on purpose: eco's own extract() gates on client-side, formed, a live
        // cluster and a non-null position and nothing else, and a gate it does not have would turn this
        // path into another "clicked and nothing happened" - which is the bug class this whole panel has
        // been carrying. The insert path below has never had one either.
        if (!(level.getBlockEntity(drivePos) instanceof SimplifyDriveBlockEntity drive) || !drive.hasCell()) {
            return false;
        }
        var inventory = player.getInventory();
        var menu = player.containerMenu;
        int freeSlot = -1;
        if (shiftDown) {
            for (int slot = 0; slot < inventory.items.size(); slot++) {
                if (inventory.getItem(slot).isEmpty()) {
                    freeSlot = slot;
                    break;
                }
            }
            if (freeSlot < 0) {
                return false;
            }
        } else if (!menu.getCarried().isEmpty()) {
            return false;
        }
        var cell = drive.removeCell();
        if (cell.isEmpty()) {
            return false;
        }
        if (shiftDown) {
            inventory.setItem(freeSlot, cell);
            inventory.setChanged();
        } else {
            menu.setCarried(cell);
        }
        menu.broadcastChanges();
        return true;
    }

    /**
     * The other half of the panel's drag path: put a cell the player is holding into that drive.
     *
     * <p>The stack arriving with the RPC is a client's claim, so it is used only to check what the server
     * says the player is holding, and the held stack is what gets spent. eco does the same in
     * {@code StorageCellUiInsertion} - read {@code menu.getCarried()}, {@code shrink(1)}, {@code
     * setCarried(EMPTY)} when it runs out. Skipping that spend turns the panel into a duplicator: the
     * drive takes a copy while the hand keeps the original.</p>
     */
    public boolean insertCellForPanel(net.minecraft.core.BlockPos drivePos, ItemStack claimed,
            net.minecraft.world.entity.player.Player player) {
        if (level == null || level.isClientSide || !isFormed()) {
            return false;
        }
        var carried = player.containerMenu.getCarried();
        if (carried.isEmpty() || !ItemStack.isSameItemSameComponents(carried, claimed)) {
            return false;
        }
        if (!(level.getBlockEntity(drivePos) instanceof SimplifyDriveBlockEntity drive)
                || !drive.insertCell(carried)) {
            return false;
        }
        carried.shrink(1);
        if (carried.isEmpty()) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }
        return true;
    }

    /**
     * The panel's rows. eco joins a cell entry to a row by number ({@code entry.typeId() ==
     * line.registryIndex()}), so the list below and {@link #panelRowOf} have to speak the same numbers -
     * which is why neither of them writes a bare digit.
     */
    public static final int ROW_ITEM = 0;
    public static final int ROW_MEGA_ITEM = 1;
    public static final int ROW_FLUID = 2;
    public static final int ROW_CHEMICAL = 3;
    public static final int ROW_UNIVERSAL = 4;
    public static final int ROW_QUANTUM = 5;
    public static final int ROW_CONCRETE = 6;
    public static final int ROW_FLUX = 7;
    public static final int ROW_COMPLEX_UNIVERSAL = 8;
    public static final int ROW_SPENT_WASTE = 9;
    /** Not a row: a cell the panel must not count anywhere. */
    public static final int ROW_NONE = -1;

    private java.util.List<StorageHostUI.StorageTypeLine> createStorageTypeLines() {
        java.util.List<StorageHostUI.StorageTypeLine> lines = new java.util.ArrayList<>();
        lines.add(createStorageTypeLine(SimplifyStorageCellItem.getItemCellType(), ROW_ITEM));
        lines.add(createStorageTypeLine(SimplifyStorageCellItem.getMegaItemCellType(), ROW_MEGA_ITEM));
        lines.add(createStorageTypeLine(SimplifyStorageCellItem.getFluidCellType(), ROW_FLUID));
        ECOCellType chemical = getChemicalCellType();
        if (chemical != null) {
            lines.add(createStorageTypeLine(chemical, ROW_CHEMICAL));
        }
        if (ModList.get().isLoaded("ae2omnicells")) {
            lines.add(createStorageTypeLine(SimplifyUniversalStorageCellItem.getUniversalCellType(), ROW_UNIVERSAL));
            lines.add(createStorageTypeLine(SimplifyQuantumStorageCellItem.getQuantumCellType(), ROW_QUANTUM));
            lines.add(createStorageTypeLine(
                    SimplifyComplexUniversalStorageCellItem.getComplexUniversalCellType(), ROW_COMPLEX_UNIVERSAL));
            if (ModList.get().isLoaded("mekanism") && ModList.get().isLoaded("appmek")) {
                lines.add(createStorageTypeLine(
                        SimplifySpentNuclearWasteCellItem.getSpentWasteCellType(), ROW_SPENT_WASTE));
            }
        }
        // Dedicated row for the infinite concrete matrix.
        lines.add(createStorageTypeLine(SimplifyConcreteStorageCellItem.CELL_TYPE, ROW_CONCRETE));
        // The flux matrices get their own row. Its name is not ours to write: eco builds the flux cell type's
        // description out of appflux's own medium word (appflux.key.flux), so this row reads exactly as it
        // does in eco's host - the same way the item row reads "item" rather than "matrix".
        ECOCellType flux = getFluxCellType();
        if (flux != null) {
            lines.add(createStorageTypeLine(flux, ROW_FLUX));
        }
        return lines;
    }

    /**
     * Which row of the panel a mounted cell belongs to, or {@link #ROW_NONE} when no row claims it.
     *
     * @param keyTypes the mounted item's own key types, or null when the stack is not an eco cell item at
     *                 all - a foreign cell then has nothing to read but its cell type
     */
    public static int panelRowOf(ECOCellType cellType, java.util.Set<AEKeyType> keyTypes) {
        if (SimplifySingularityCellItem.CELL_TYPE.equals(cellType)) {
            // No row for it, and not because nobody thought of one: it reports an unbounded byte total and
            // its stock is a world-clock figure, so whichever row it landed in would read as that medium's
            // own number. The mounted cell is named on the drive and in Jade instead.
            return ROW_NONE;
        }
        int row = SimplifyStorageCellItem.getMegaItemCellType().equals(cellType) ? ROW_MEGA_ITEM : ROW_ITEM;
        // The same gate as the two rows above, and for the same reason: our omni item classes extend
        // eco's, which extend OmniCells', so reaching for them at all without that mod loaded is a
        // NoClassDefFoundError - not a null.
        if (ModList.get().isLoaded("ae2omnicells")) {
            if (SimplifyUniversalStorageCellItem.getUniversalCellType().equals(cellType)) {
                return ROW_UNIVERSAL;
            }
            if (SimplifyQuantumStorageCellItem.getQuantumCellType().equals(cellType)) {
                return ROW_QUANTUM;
            }
            if (SimplifyComplexUniversalStorageCellItem.getComplexUniversalCellType().equals(cellType)) {
                return ROW_COMPLEX_UNIVERSAL;
            }
            if (ModList.get().isLoaded("mekanism") && ModList.get().isLoaded("appmek")
                    && SimplifySpentNuclearWasteCellItem.getSpentWasteCellType().equals(cellType)) {
                return ROW_SPENT_WASTE;
            }
        }
        if (SimplifyConcreteStorageCellItem.CELL_TYPE.equals(cellType)) {
            return ROW_CONCRETE;
        }
        ECOCellType flux = registeredFluxType();
        if (flux != null && flux.equals(cellType)) {
            return ROW_FLUX;
        }
        if (keyTypes != null) {
            if (keyTypes.contains(AEKeyType.fluids())) {
                return ROW_FLUID;
            }
            if (keyTypes.stream().anyMatch(type -> "chemical".equals(type.getId().getPath()))) {
                return ROW_CHEMICAL;
            }
        } else if (SimplifyStorageCellItem.getFluidCellType().equals(cellType)) {
            // A fluid matrix whose item does not implement eco's cell interface: still the fluid medium.
            return ROW_FLUID;
        }
        return row;
    }

    /** The icon eco draws for a row. Flux is neither items nor fluids to appflux, so it takes "other". */
    public static int panelKindOf(int row) {
        return switch (row) {
            case ROW_FLUID -> StorageHostUI.CellEntry.KIND_FLUID;
            case ROW_CHEMICAL -> StorageHostUI.CellEntry.KIND_GAS;
            case ROW_FLUX -> StorageHostUI.CellEntry.KIND_OTHER;
            default -> StorageHostUI.CellEntry.KIND_ITEM;
        };
    }

    private static java.util.Set<AEKeyType> keyTypesOf(SimplifyDriveBlockEntity drive) {
        ItemStack cellStack = drive.getCellStack();
        return cellStack != null && cellStack.getItem() instanceof IECOStorageCellItem cellItem
                ? cellItem.getKeyTypes() : null;
    }

    /**
     * The drives in the order the panel has to hand them over: rows top first, each row read across from the
     * host's own point of view, flipped when the machine is mirrored. The cluster list is in the order the
     * shape scanner found them, so without this a slot on screen and the drive it writes into are unrelated
     * - the cell lands somewhere else than where it was dropped. eco does the same job in
     * {@code StorageDriveLayout.ordered} (package-private, and typed to its own drive class) with the
     * extents of its own machine baked in; ours walks its bounds instead of copying those constants.
     */
    private java.util.List<SimplifyDriveBlockEntity> orderedDrivesForPanel(
            java.util.List<SimplifyDriveBlockEntity> drives) {
        var strategy = appeng.api.orientation.OrientationStrategies.horizontalFacing();
        var state = getBlockState();
        var across = strategy.getSide(state, mirrored
                ? appeng.api.orientation.RelativeSide.RIGHT
                : appeng.api.orientation.RelativeSide.LEFT);
        var downwards = strategy.getSide(state, appeng.api.orientation.RelativeSide.BOTTOM);
        var origin = getBlockPos();
        return drives.stream()
                .sorted(java.util.Comparator
                        .comparingInt((SimplifyDriveBlockEntity drive) ->
                                projectionAgainst(drive.getBlockPos().subtract(origin), downwards))
                        .thenComparingInt(drive ->
                                projectionAgainst(drive.getBlockPos().subtract(origin), across)))
                .toList();
    }

    private static int projectionAgainst(net.minecraft.core.BlockPos offset, net.minecraft.core.Direction axis) {
        // The step signs are the whole point: without them the key is unsigned and the rows come out
        // bottom-first, which is what this method did the first time it ran.
        return offset.getX() * axis.getStepX()
                + offset.getY() * axis.getStepY()
                + offset.getZ() * axis.getStepZ();
    }

    private ECOCellType getChemicalCellType() {
        return NERegistries.CELL_TYPE.get(ResourceLocation.fromNamespaceAndPath("neoecoae", "mekanism"));
    }

    /** eco registers the flux type only while appflux is present; without it this row is simply not drawn. */
    private ECOCellType getFluxCellType() {
        return registeredFluxType();
    }

    private static ECOCellType registeredFluxType() {
        return NERegistries.CELL_TYPE.get(ResourceLocation.fromNamespaceAndPath("neoecoae", "flux"));
    }

    private StorageHostUI.StorageTypeLine createStorageTypeLine(ECOCellType cellType, int registryIndex) {
        return new StorageHostUI.StorageTypeLine(
                cellType,
                registryIndex,
                () -> storageTotals(cellType).usedTypes(),
                () -> storageTotals(cellType).totalTypes(),
                () -> storageTotals(cellType).usedBytes(),
                () -> storageTotals(cellType).totalBytes(),
                () -> "0");
    }

    public java.util.List<StorageHostUI.CellEntry> createCellEntries() {
        SimplifyStorageCluster cluster = getCluster();
        if (cluster == null) {
            return java.util.List.of();
        }
        java.util.List<StorageHostUI.CellEntry> entries = new java.util.ArrayList<>();
        for (SimplifyDriveBlockEntity drive : orderedDrivesForPanel(cluster.getDrives())) {
            IECOStorageCell cell = drive.getCellInventory();
            if (cell == null) {
                // An empty drive still has to be handed over as a row: eco's grid sends the insert RPC only
                // from a row the player can see, so a host that lists only its occupied drives gives nobody
                // a slot to drop a matrix into - the panel could take cells out and never put one in.
                // CellEntry.EMPTY carries typeId -1, which is eco's own "not one of the media rows" mark.
                entries.add(StorageHostUI.CellEntry.EMPTY.atDrive(drive.getBlockPos(), ItemStack.EMPTY,
                        null, drive.panelInteractionId()));
                continue;
            }
            ECOCellType cellType = cell.getCellType();
            int typeId = panelRowOf(cellType, keyTypesOf(drive));
            if (typeId == ROW_NONE) {
                continue;
            }
            entries.add(new StorageHostUI.CellEntry(
                    typeId,
                    cell.getTier().getTier(),
                    panelKindOf(typeId),
                    Math.max(0L, cell.getStoredItemTypes()),
                    Math.max(0L, cn.dancingsnow.neoecoprototype.items.SmallBulkTypeCap
                            .of(drive.getCellStack(), cell)),
                    Math.max(0L, cell.getUsedBytes()),
                    Math.max(0L, cell.getTotalBytes()),
                    cell.hasInfiniteTypeCapacity(),
                    drive.getCellState(),
                    isFormed())
                    // eco 21.2.3's grid draws a slot from the entry's own position and stack - a cell that
                    // does not say where it stands shows up as an empty slot, which is what the panel did
                    // before this line existed. The id is the other half of that line: without one, the
                    // grid's mouse handler returns before it sends the extraction RPC, so a row that looks
                    // perfectly drawn refuses every click and says nothing about why.
                    .atDrive(drive.getBlockPos(), drive.getCellStack(), null, drive.panelInteractionId()));
        }
        return entries;
    }

    private L1StorageTotals storageTotals(ECOCellType cellType) {
        long usedTypes = 0L;
        long totalTypes = 0L;
        long usedBytes = 0L;
        long totalBytes = 0L;
        SimplifyStorageCluster cluster = getCluster();
        if (cluster == null) {
            return new L1StorageTotals(0L, 0L, 0L, 0L);
        }
        for (SimplifyDriveBlockEntity drive : cluster.getDrives()) {
            IECOStorageCell cell = drive.getCellInventory();
            if (cell == null || !cellType.equals(cell.getCellType())) {
                continue;
            }
            usedTypes = saturatingAdd(usedTypes, Math.max(0L, cell.getStoredItemTypes()));
            totalTypes = saturatingAdd(totalTypes, Math.max(0L,
                    cn.dancingsnow.neoecoprototype.items.SmallBulkTypeCap.of(drive.getCellStack(), cell)));
            usedBytes = saturatingAdd(usedBytes, Math.max(0L, cell.getUsedBytes()));
            totalBytes = saturatingAdd(totalBytes, Math.max(0L, cell.getTotalBytes()));
        }
        return new L1StorageTotals(usedTypes, totalTypes, usedBytes, totalBytes);
    }

    private L1StorageTotals chemicalStorageTotals() {
        ECOCellType chemical = getChemicalCellType();
        return chemical == null ? new L1StorageTotals(0L, 0L, 0L, 0L) : storageTotals(chemical);
    }

    private long getStoredEnergy() {
        double energy = 0.0D;
        SimplifyStorageCluster cluster = getCluster();
        if (cluster != null) {
            for (SimplifyEnergyCellBlockEntity cell : cluster.getEnergyCells()) {
                energy += cell.getAECurrentPower();
            }
        }
        return saturatingDoubleToLong(energy);
    }

    private long getMaxEnergy() {
        double energy = 0.0D;
        SimplifyStorageCluster cluster = getCluster();
        if (cluster != null) {
            for (SimplifyEnergyCellBlockEntity cell : cluster.getEnergyCells()) {
                energy += cell.getAEMaxPower();
            }
        }
        return saturatingDoubleToLong(energy);
    }

    private long getMaxLoadUsedBytes() {
        return saturatingAdd(
                saturatingAdd(
                        storageTotals(SimplifyStorageCellItem.getItemCellType()).usedBytes(),
                        storageTotals(SimplifyStorageCellItem.getFluidCellType()).usedBytes()),
                chemicalStorageTotals().usedBytes());
    }

    private static long saturatingAdd(long left, long right) {
        if (right <= 0L) {
            return left;
        }
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    private static long saturatingDoubleToLong(double value) {
        if (!(value > 0.0D)) {
            return 0L;
        }
        return value >= Long.MAX_VALUE ? Long.MAX_VALUE : Math.round(value);
    }

    private static IItemHandlerModifiable createReadOnlyEmptyComponentInventory() {
        return new ItemStackHandler(1) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return stack;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return false;
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
            }
        };
    }

    private record L1StorageTotals(long usedTypes, long totalTypes, long usedBytes, long totalBytes) {
    }


    private void requestBuild(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!canPlayerInteract(player)) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.too_far");
            return;
        }
        if (!(level instanceof ServerLevel)) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.server_only");
            return;
        }
        if (isFormed()) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.already_formed");
            return;
        }
        if (isBuildInProgress()) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.build_in_progress");
            return;
        }
        MultiBlockPlacementPlan plan = buildController.createLocalPreviewPlan();
        if (plan == null) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.no_plan");
            return;
        }
        if (!plan.getConflictPositions().isEmpty()) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.conflicts", plan.getConflictPositions().size());
            return;
        }
        if (!serverPlayer.isCreative()
                && !MultiBlockPlacementService.hasRequiredItems(serverPlayer, plan.getRequiredItems())) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.missing_items");
            return;
        }
        LOGGER.info("L1 auto-build accepted @{} length={} mirrored={} missing={} requiredItems={} creative={}",
                worldPosition, getSelectedBuildLength(), isMirrorBuild(), plan.getMissingBlocks().size(),
                plan.getRequiredItemCount(), serverPlayer.isCreative());
        buildController.autoBuild(serverPlayer);
    }

    private static void sendBuildMessage(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args), true);
    }

    private void changeStoragePriority(Player player, int delta) {
        if (canPlayerInteract(player)) {
            setStoragePriority(storagePriority + delta);
        }
    }

    @Override
    public MultiBlockDefinition getBuildDefinition() {
        return SimplifyStorageDefinition.L1;
    }

    @Override
    public int getMinBuildLength() {
        return getBuildDefinition().getExpandMin();
    }

    @Override
    public int getMaxBuildLength() {
        return getBuildDefinition().getExpandMax();
    }

    @Override
    public int getSelectedBuildLength() {
        return selectedBuildLength;
    }

    @Override
    public void setSelectedBuildLength(int length) {
        selectedBuildLength = Math.clamp(length, getMinBuildLength(), getMaxBuildLength());
    }

    @Override
    public boolean isMirrorBuild() {
        return mirrorBuild;
    }

    @Override
    public void setMirrorBuild(boolean mirrorBuild) {
        this.mirrorBuild = mirrorBuild;
    }

    @Override
    public boolean isBuildInProgress() {
        return buildInProgress;
    }

    @Override
    public void setBuildInProgress(boolean buildInProgress) {
        this.buildInProgress = buildInProgress;
    }

    @Override
    public boolean isFormed() {
        return formed;
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        return level != null && SimplifyStorageControllerBlock.isPlayerCloseEnough(level, worldPosition, player);
    }

    @Override
    public Level getBuildLevel() {
        return level;
    }

    @Override
    public BlockPos getBuildPosition() {
        return worldPosition;
    }

    @Override
    public BlockState getBuildState() {
        return getBlockState();
    }

    @Override
    public void rebuildAfterBuild() {
        rebuildMultiblock();
    }

    @Override
    public void buildStateChanged() {
        setChanged();
        markForUpdate();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (isServerStopping()) {
            return;
        }
        super.onMainNodeStateChanged(reason);
    }
}
