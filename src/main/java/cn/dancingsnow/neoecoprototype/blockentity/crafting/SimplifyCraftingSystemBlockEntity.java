package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingSystem;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoae.gui.crafting.CraftingControllerPanelState;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoprototype.api.MachineMood;
import cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingSystemBlock;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyCraftingDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * F1 crafting host. Publishes which of the two interface blocks the finished machine holds onto its
 * own block state so the blockstate file can pick a formed model per combination.
 *
 * <p>eco's own {@code updateState} already writes {@code mirrored} with a block change, and both that
 * and this run inside AE2's cluster recalculation, which holds a global "modification in progress"
 * latch. A second write on the same stack is what wedged the C1 host, so the value is recorded here
 * and applied on the next server tick instead.
 *
 * <p>Unlike the C1 host, this one stays a subclass on purpose, and the reason is measured rather than
 * assumed. AE2 files every node under {@code owner.getClass()} and eco looks machines up by that exact key,
 * so subclassing costs visibility: eco's {@code CraftingServiceMixin.onAddNode} will not find this machine
 * and its "ignore pattern substitutions" toggle is silently lost. That is the whole damage here -- the
 * crafting host is not one of the clusters eco feeds into AE2's CPU list, which is why C1 could not stay a
 * subclass -- while the face roll has to persist per host, and block entity data is the only place for it.
 * Do not "fix" this by moving it onto the block the way C1 was moved: there is nowhere to save the roll.
 */
public class SimplifyCraftingSystemBlockEntity extends ECOCraftingSystemBlockEntity {
    /**
     * The host rolls for the face once, the first time it forms, and keeps the answer: geometry checks
     * re-run on neighbour changes, chunk loads and rebuilds, so a per-check roll would flip the look
     * back and forth instead of being rare. One in sixteen.
     */
    private static final int MIND_CHANCE_ONE_IN = 16;
    private static final String HAS_MIND_TAG = "neoecoprototype:has_mind";
    private static final String MIND_ROLLED_TAG = "neoecoprototype:mind_rolled";

    /** How often a minded host re-reads its own situation. One second is plenty for a mood. */
    private static final int MOOD_SAMPLE_TICKS = 20;

    /** The core column the crafting artwork numbers 1 - see {@link #getCraftingControllerPanelState()}. */
    private static final int CORE_ART_COLUMN = 1;

    private boolean communicationInterface;
    private boolean appearancePending;
    private boolean hasMind;
    private boolean mindRolled;
    private int moodCountdown = MOOD_SAMPLE_TICKS;
    /** The idle draw the node currently carries, so a sample that changed nothing pushes nothing. */
    private double idlePowerPushed = -1.0D;
    /** A band the host has crossed but not yet written, or -1 when there is nothing to publish. */
    private int mindTierToPublish = -1;

    public SimplifyCraftingSystemBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                             IECOTier tier) {
        super(type, pos, state, tier);
    }

    /** F1 is a shell of addon blocks, so its geometry is our own definition, not eco's L4 layout. */
    @Override
    public MultiBlockDefinition getBuildDefinition() {
        return SimplifyCraftingDefinition.L1;
    }

    /**
     * The brackets above and below each working core on the crafting panel's queue strip.
     *
     * <p>eco writes one 2-bit number per slot out of the core's own tier index, and the same number - 0 -
     * means "no core stands here". An addon tier whose index is 0 is therefore indistinguishable from an
     * empty slot, and {@code CraftingControllerArtwork.drawPlus} returns for anything outside 1..3, so an
     * F1 host draws no brackets at all. Rebuilding the two lists from our own cluster is what separates
     * those two zeros: a slot gets {@link #CORE_ART_COLUMN} only where a core really sits above or below
     * that worker, and stays 0 where none does.
     *
     * <p>Everything else is handed back as super built it, and the slot order and the mirrored flip are
     * eco's own rule (workers sorted by distance to the controller, cut at {@code SLOT_COUNT}) - so a sync
     * that changes the coolant, switch, queue or energy halves needs no edit here. The column is 1 because
     * the artwork's leftmost column is already painted green: this costs no new art.
     */
    @Override
    public CraftingControllerPanelState getCraftingControllerPanelState() {
        var base = super.getCraftingControllerPanelState();
        var cluster = getCluster();
        if (!base.formed() || cluster == null) {
            return base;
        }
        var coresByPos = new java.util.HashMap<BlockPos, Integer>();
        for (var core : cluster.getParallelCores()) {
            int index = core.getTier().getTier();
            coresByPos.put(core.getBlockPos(), index >= 1 ? index : CORE_ART_COLUMN);
        }
        var up = appeng.api.orientation.OrientationStrategies.horizontalFacing()
                .getSide(getBlockState(), appeng.api.orientation.RelativeSide.TOP);
        var workers = cluster.getWorkers().stream()
                .sorted(java.util.Comparator.comparingDouble(
                        worker -> worker.getBlockPos().distSqr(getBlockPos())))
                .limit(CraftingControllerPanelState.SLOT_COUNT)
                .toList();
        var upper = new java.util.ArrayList<Integer>(workers.size());
        var lower = new java.util.ArrayList<Integer>(workers.size());
        for (var worker : workers) {
            upper.add(coresByPos.getOrDefault(worker.getBlockPos().relative(up), 0));
            lower.add(coresByPos.getOrDefault(worker.getBlockPos().relative(up.getOpposite()), 0));
        }
        return CraftingControllerPanelState.of(base.hostTier(), upper, lower, workers.size(),
                getBlockState().getValue(ECOCraftingSystem.MIRRORED), base.coolantOverlay(),
                base.switchOverlay(), base.queuePermille(), base.energyPermille());
    }

    @Override
    public void onReady() {
        super.onReady();
        // Deliberately after eco's own number, which is where the injection this replaces sat too.
        applyIdlePower(SimplifyPowerProfile.L1.craftingControllerIdlePower());
    }

    /** Records what the interface cell turned out to hold, for the next {@link #tick}. */
    public void setCommunicationInterface(boolean communicationInterface) {
        if (this.communicationInterface == communicationInterface) {
            return;
        }
        this.communicationInterface = communicationInterface;
        this.appearancePending = true;
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        super.tick(level, pos, state);
        if (appearancePending && !level.isClientSide) {
            appearancePending = false;
            publishAppearance(level, pos);
        }
        if (!level.isClientSide && --moodCountdown <= 0) {
            moodCountdown = MOOD_SAMPLE_TICKS;
            applyMindMood();
        }
    }

    /**
     * A host with a face draws slightly less power while idle, by how content its situation is: coolant in
     * the bath and work to do. Nothing here is stored - the next sample recomputes it - so the value cannot
     * be farmed, cannot drift across saves, and needs no migration when the numbers change.
     *
     * <p>The same mood also picks which face the art shows, but that one <em>is</em> written to the block
     * state, because models cannot read a live number. It is therefore the only part allowed to trigger a
     * block change, and it triggers on a band crossing rather than on every sample - see
     * {@link MachineMood#tierOf}. The write itself rides {@code appearancePending}, the same deferral the
     * face flag uses, so it never lands inside AE2's cluster recalculation.
     */
    private void applyMindMood() {
        Level reader = this.level;
        if (reader == null || reader.isClientSide) {
            return;
        }
        double baseline = SimplifyPowerProfile.L1.craftingControllerIdlePower();
        if (!hasMind) {
            applyIdlePower(baseline);
            return;
        }
        int capacity = getDisplayedCoolantCapacity();
        double coolant = capacity > 0 ? (double) getDisplayedCoolantAmount() / capacity : 0.0D;
        boolean busy = getRunningThreadCount() > 0;
        boolean formed = isFormed();
        applyIdlePower(baseline * MachineMood.idlePowerFactor(true, coolant, busy, formed,
                NeoECOPrototypeServerConfig.MIND_IDLE_POWER_SAVING.get()));
        if (!formed) {
            return;
        }
        int shown = reader.getBlockState(worldPosition)
                .getValue(SimplifyCraftingSystemBlock.MIND_TIER);
        int next = MachineMood.tierOf(MachineMood.mood(coolant, busy), shown);
        if (next != shown) {
            mindTierToPublish = next;
            appearancePending = true;
        }
    }

    /** Pushes a new idle draw only when it differs, because this runs on a timer and the node is shared. */
    private void applyIdlePower(double value) {
        if (Double.compare(value, idlePowerPushed) == 0) {
            return;
        }
        idlePowerPushed = value;
        getMainNode().setIdlePowerUsage(value);
    }

    @Override
    public void updateState(boolean updateExposed) {
        super.updateState(updateExposed);
        appearancePending = true;
    }

    private void publishAppearance(Level reader, BlockPos pos) {
        if (isRemoved() || level == null || level.isClientSide) {
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (!state.hasProperty(SimplifyCraftingSystemBlock.COMMUNICATION_INTERFACE)) {
            return;
        }
        if (isFormed() && !mindRolled) {
            mindRolled = true;
            hasMind = level.random.nextInt(MIND_CHANCE_ONE_IN) == 0;
            setChanged();
        }
        int publishedTier = mindTierToPublish >= 0 ? mindTierToPublish
                : state.getValue(SimplifyCraftingSystemBlock.MIND_TIER);
        mindTierToPublish = -1;
        BlockState next = state.setValue(SimplifyCraftingSystemBlock.COMMUNICATION_INTERFACE,
                        isFormed() && communicationInterface)
                .setValue(SimplifyCraftingSystemBlock.HAS_MIND, isFormed() && hasMind)
                .setValue(SimplifyCraftingSystemBlock.MIND_TIER,
                        isFormed() && hasMind ? publishedTier : 0);
        if (next != state) {
            level.setBlock(worldPosition, next, Block.UPDATE_CLIENTS);
        }
    }

    /** Whether this host grew a face. Read by the blockstate write above and by the game tests. */
    public boolean hasMind() {
        return hasMind;
    }

    /**
     * What the grid actually charges this host while idle. The live node answers it - {@code IManagedGridNode}
     * only has a setter - and before the node exists the answer is what we last meant to push.
     */
    public double idlePowerDraw() {
        var node = getMainNode().getNode();
        return node == null ? Math.max(0.0D, idlePowerPushed) : node.getIdlePowerUsage();
    }

    /** Whether the once-per-host roll has already happened. */
    public boolean mindRolled() {
        return mindRolled;
    }

    @Override
    public void saveAdditional(net.minecraft.nbt.CompoundTag tag,
                               net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean(MIND_ROLLED_TAG, mindRolled);
        tag.putBoolean(HAS_MIND_TAG, hasMind);
    }

    @Override
    public void loadTag(net.minecraft.nbt.CompoundTag tag,
                        net.minecraft.core.HolderLookup.Provider registries) {
        // AE2 declares loadAdditional final and routes it here, so loadTag is the hook for extra fields.
        super.loadTag(tag, registries);
        mindRolled = tag.getBoolean(MIND_ROLLED_TAG);
        hasMind = tag.getBoolean(HAS_MIND_TAG);
    }
}
