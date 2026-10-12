package cn.dancingsnow.neoecoprototype.items;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import cn.dancingsnow.neoecoae.all.NEMultiBlocks;
import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineCasingBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationCoolingControllerBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockBuildController;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementPlan;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementService;
import cn.dancingsnow.neoecoae.multiblock.placement.RequiredItem;
import cn.dancingsnow.neoecoae.multiblock.placement.WorldPlannedBlock;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyCraftingDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyStorageDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyTrinityDefinition;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Works on a machine that has already been built, in two directions.
 *
 * <p><b>Along its edge</b> - eco's own one-click builder refuses to run on a formed machine
 * ({@code MultiBlockBuildController.autoBuild} returns on {@code isFormed}), so enlarging today means
 * tearing the whole thing down and building it again.</p>
 *
 * <p><b>Along its tier</b> - at the ceiling there is nothing left to grow, and the only way to a
 * higher tier is the same teardown, because the host block itself is registered per tier. Shift-click
 * a machine that is already at its ceiling and it swaps in place: the blocks that change with the tier
 * come back to the player first, then the higher tier's blocks are placed from what they carry.
 * Nothing is computed as a difference - the cost is simply the higher tier's own parts.</p>
 *
 * <p>Two things about eco's planner had to be worked around, and both only bite once the machine is
 * already formed - which is why upstream never trips over them:</p>
 *
 * <ul>
 * <li>{@code preview} compares whole block states ({@code existingState.equals(targetState)}), while
 * {@code ECOMachineCasingBlockEntity} writes its {@code invisible} property into the state when a
 * cluster forms. Every casing the cluster hides therefore stops matching the plan, and
 * {@code buildInstant} refuses outright on a non-empty conflict list. So the plan handed to it is
 * reclassified here: a position holding the block the plan wants, differing only in properties the
 * formation sets by itself, counts as reused rather than conflicting.</li>
 * <li>The plan never mentions the host cell at all - {@code preview} skips
 * {@code MultiBlockRotation.CONTROLLER_ANCHOR}, which is where the system block stands. A tier swap
 * therefore has to move the anchor by itself, and the anchor is also the block entity that owns the
 * build state, so the machine is re-formed through whatever block entity ends up in that cell.</li>
 * </ul>
 *
 * <p>Nothing is consumed and no block is broken unless every position the target shape needs is
 * already free, already holds the right block, or is one of the blocks this machine's own definition
 * claims and which the target shape wants differently.</p>
 */
public class SimplifyStructureExpansionCardItem extends Item {

    /** How many lines of the shortfall list fit in an action bar before it stops being readable. */
    private static final int MAX_REPORTED_ITEMS = 4;

    /** The name eco's own {@code NEBlock.FORMED} is written under. Never copied across a swap. */
    private static final String FORMED_PROPERTY = "formed";

    /**
     * The upgrade ladder this card knows: only upward, and only to L9, which is a product call. Our
     * own L1 machines are not in here, so the card has nothing to say about them beyond "already at
     * the ceiling".
     *
     * <p>Written as comparisons rather than a static map on purpose. eco's definitions are singletons
     * whose ceiling comes off {@code NEConfig} when {@code NEMultiBlocks} is initialised, and a static
     * field here would trigger that during item registration - before the config has been read.</p>
     */
    private static @Nullable MultiBlockDefinition nextTier(MultiBlockDefinition current) {
        if (current == NEMultiBlocks.STORAGE_SYSTEM_L4 || current == NEMultiBlocks.STORAGE_SYSTEM_L6) {
            return NEMultiBlocks.STORAGE_SYSTEM_L9;
        }
        if (current == NEMultiBlocks.COMPUTATION_SYSTEM_L4
                || current == NEMultiBlocks.COMPUTATION_SYSTEM_L6) {
            return NEMultiBlocks.COMPUTATION_SYSTEM_L9;
        }
        if (current == NEMultiBlocks.CRAFTING_SYSTEM_L4 || current == NEMultiBlocks.CRAFTING_SYSTEM_L6) {
            return NEMultiBlocks.CRAFTING_SYSTEM_L9;
        }
        return null;
    }

    /**
     * The tier this click should land on. The expansion card climbs straight to the ceiling; a
     * {@link SimplifyTierUpgradeCardItem} overrides this to cover one rung and nothing else.
     */
    protected @Nullable MultiBlockDefinition targetOf(MultiBlockDefinition current) {
        return nextTier(current);
    }

    /** What to tell the player when {@link #targetOf} came back with nothing. */
    protected Component refusal(MultiBlockDefinition current, int length) {
        return Component.translatable("card.neoecoprototype.expansion.at_max", length);
    }

    /** Whether this card may still stretch a machine that is not at its ceiling yet. */
    protected boolean canGrow() {
        return true;
    }

    /** Whether the card itself is the price, so a successful click has to eat it. */
    protected boolean consumedOnSuccess() {
        return false;
    }

    /**
     * Whether a tier swap charges the player's inventory for the parts it changes. The expansion card
     * does - it is a builder, and its one-for-one ledger is what makes it safe to run on a loaded
     * machine. A rung card does not: the card itself is the price, so the click touches no inventory at
     * all and the swap has to be placed by hand instead of through eco's builder, which refuses to build
     * for a survival player it cannot bill.
     */
    protected boolean chargesParts() {
        return true;
    }

    /**
     * The line a finished swap prints. The billing card returns every block it takes out, so its line
     * says so; the free one returns nothing, and a message that promises a refund the player will never
     * see is worse than no message.
     */
    protected String upgradedKey() {
        return "card.neoecoprototype.expansion.upgraded";
    }

    public SimplifyStructureExpansionCardItem(Properties properties) {
        super(properties);
    }

    /**
     * A target plan with eco's formation-flag mismatches moved off the conflict list.
     *
     * @param plan    the plan to hand over, with only genuinely foreign positions left conflicting
     * @param moved   positions holding this machine's own block that the target shape wants differently
     * @param bill    what the plan and {@code moved} together cost, merged per item
     * @param foreign a position that belongs to neither, which stops the whole card
     */
    private record Classified(MultiBlockPlacementPlan plan, List<WorldPlannedBlock> moved,
                              List<RequiredItem> bill, @Nullable BlockPos foreign) {
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        // Decided on both sides, from the world and the inventory alone, so client and server never
        // disagree about whether this click was the card's or the host's own panel.
        if (!(level.getBlockEntity(pos) instanceof MultiBlockBuildController.Host host)) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }
        grow(host, (ServerLevel) level, pos, serverPlayer, context.getHand());
        return InteractionResult.CONSUME;
    }

    private void grow(MultiBlockBuildController.Host host, ServerLevel level, BlockPos pos,
                      ServerPlayer player, net.minecraft.world.InteractionHand hand) {
        if (!host.canPlayerInteract(player) || host.isBuildInProgress()) {
            say(player, "card.neoecoprototype.expansion.busy");
            return;
        }
        MultiBlockDefinition definition = host.getBuildDefinition();
        if (definition == null) {
            say(player, "card.neoecoprototype.expansion.no_definition");
            return;
        }
        int target = definition.getExpandMax();
        // selectedBuildLength is the panel's pending choice, not the size of what stands here - eco
        // initialises it to the ceiling, so reading it made every machine that never had its slider
        // moved look already maxed out. The size has to come off the world.
        int current = largestSatisfiedLength(host, level, pos, definition, target);
        if (current < definition.getExpandMin()) {
            say(player, "card.neoecoprototype.expansion.not_formed");
            return;
        }
        if (current >= target) {
            upgradeTier(host, level, pos, player, definition, current, hand);
            return;
        }
        // A card that only changes tiers must leave a short machine exactly as it found it - and must
        // not eat itself doing nothing, which is why this is checked before any block is touched.
        if (!canGrow()) {
            say(player, Component.translatable("card.neoecoprototype.upgrade.not_full", target));
            return;
        }

        Classified grown = classify(preview(host, level, pos, definition, target),
                plannedByPosition(preview(host, level, pos, definition, current)), level, false);
        if (grown.foreign() != null) {
            reportBlocked(player, grown.foreign());
            return;
        }
        MultiBlockPlacementPlan plan = grown.plan();
        List<RequiredItem> net = netted(grown.bill(), reclaimed(grown.moved(), level));
        if (!player.isCreative() && !MultiBlockPlacementService.hasRequiredItems(player, net)) {
            Component missing = shortfall(net);
            say(player, Component.translatable("card.neoecoprototype.expansion.short_of", missing));
            handOverTicket(level, player, growWorkOrder(definition), net, missing);
            return;
        }
        // Paid for first, so a player who cannot afford the larger shape keeps the machine they had.
        // The price is the difference, not the whole shape: the loop below hands every moved block back
        // into this same inventory, and eco's builder re-checks the full list only after that has landed.
        cn.dancingsnow.neoecoprototype.NeoECOPrototype.LOGGER.info("card build path (grow): def="
                + definition + " length=" + current + "->" + target + " creative=" + player.isCreative()
                + " billKinds=" + grown.bill().size() + " netKinds=" + net.size()
                + " moved=" + grown.moved().size() + " missing=" + plan.getMissingBlocks().size()
                + " conflicts=" + plan.getConflictPositions().size());
        for (WorldPlannedBlock moved : grown.moved()) {
            giveBack(level, moved.worldPos(), player);
        }

        host.setSelectedBuildLength(target);
        if (!MultiBlockPlacementService.buildInstant(level, plan, player)) {
            cn.dancingsnow.neoecoprototype.NeoECOPrototype.LOGGER.info("card build refused (grow): payable="
                    + MultiBlockPlacementService.hasRequiredItems(player, plan.getRequiredItems())
                    + " conflicts=" + plan.getConflictPositions().size()
                    + " missing=" + plan.getMissingBlocks().size()
                    + " billKinds=" + plan.getRequiredItems().size());
            // buildInstant only refuses here because a placement failed part-way through, so the
            // structure really did change. Re-form anyway: leaving the length bumped without telling
            // the cluster is the worse failure.
            host.rebuildAfterBuild();
            say(player, "card.neoecoprototype.expansion.partly_built");
            return;
        }
        host.rebuildAfterBuild();
        say(player, Component.translatable("card.neoecoprototype.expansion.grown", target,
                plan.getMissingBlocks().size()));
    }

    /**
     * Swaps an already-maxed machine up a tier without touching its size or its shape.
     *
     * <p>The three system families keep one geometry per family and vary only which block fills the
     * tier-bearing cells, so "what changes with the tier" is read off the two plans for the same
     * length rather than from a list of blocks: energy cells for storage, threading and parallel
     * cores plus the cooling controller for computation, parallel cores for crafting, and the host
     * block itself - which is the anchor, and so appears in neither plan.</p>
     */
    private void upgradeTier(MultiBlockBuildController.Host host, ServerLevel level, BlockPos pos,
                             ServerPlayer player, MultiBlockDefinition current, int length,
                             net.minecraft.world.InteractionHand hand) {
        MultiBlockDefinition target = targetOf(current);
        if (target == null) {
            say(player, refusal(current, length));
            return;
        }
        BlockState anchor = anchorTarget(host, level, pos, target);
        if (anchor == null) {
            say(player, "card.neoecoprototype.expansion.upgrade.shape");
            return;
        }
        Classified swap = classify(preview(host, level, pos, target, length),
                plannedByPosition(preview(host, level, pos, current, length)), level, true);
        if (swap.foreign() != null) {
            reportBlocked(player, swap.foreign());
            return;
        }
        if (!chargesParts()) {
            swapInPlace(level, pos, anchor, swap, player, hand, current, target);
            return;
        }
        List<RequiredItem> bill = new ArrayList<>(swap.bill());
        mergeItem(bill, new ItemStack(anchor.getBlock().asItem()));
        List<RequiredItem> net = netted(bill, upgradeReclaimed(swap.moved(), level, pos));
        if (!player.isCreative() && !MultiBlockPlacementService.hasRequiredItems(player, net)) {
            Component missing = shortfall(net);
            say(player, Component.translatable("card.neoecoprototype.expansion.short_of", missing));
            handOverTicket(level, player, upgradeWorkOrder(target), net, missing);
            return;
        }

        cn.dancingsnow.neoecoprototype.NeoECOPrototype.LOGGER.info("card build path (upgrade): def="
                + current + "->" + target + " length=" + length + " creative=" + player.isCreative()
                + " billKinds=" + bill.size() + " netKinds=" + net.size()
                + " moved=" + swap.moved().size());
        for (WorldPlannedBlock moved : swap.moved()) {
            giveBack(level, moved.worldPos(), player);
        }
        giveBack(level, pos, player);
        if (!MultiBlockPlacementService.buildInstant(level, swap.plan(), player)) {
            cn.dancingsnow.neoecoprototype.NeoECOPrototype.LOGGER.info("card build refused (upgrade): payable="
                    + MultiBlockPlacementService.hasRequiredItems(player, swap.plan().getRequiredItems())
                    + " conflicts=" + swap.plan().getConflictPositions().size()
                    + " missing=" + swap.plan().getMissingBlocks().size());
            finishAnchor(level, pos, anchor, player);
            say(player, "card.neoecoprototype.expansion.partly_built");
            return;
        }
        // The host block is not in the plan, so eco's builder never bills or consumes it. Taking it
        // after the rest is only safe because the bill above was checked with it included.
        if (!player.isCreative() && !consumeOne(player, new ItemStack(anchor.getBlock().asItem()))) {
            finishAnchor(level, pos, anchor, player);
            say(player, "card.neoecoprototype.expansion.upgrade.host_missing");
            return;
        }
        level.setBlock(pos, anchor, Block.UPDATE_ALL);
        rebuildHost(level, pos);
        if (consumedOnSuccess() && !player.hasInfiniteMaterials()) {
            // Every earlier return path leaves both the machine and the card untouched, so this is the
            // only place the card can be eaten - and it is eaten after the new tier is standing.
            var held = player.getItemInHand(hand);
            if (held.is(this)) {
                held.shrink(1);
            }
        }
        say(player, Component.translatable(upgradedKey(),
                tierLabel(target), swap.moved().size() + 1));
    }

    /**
     * Puts a tier swap in without touching anyone's inventory, because the rung card is the whole price:
     * there is nothing to bill and nothing to hand back, so writing the positions from the plan is both
     * simpler and the only option - eco's builder refuses to build for a survival player it cannot
     * charge, which is exactly the gate the expansion card needs and this one does not.
     *
     * <p>What this does spend is the block that stood in each moved position, and whatever it held.
     * Today that set is tier-bearing parts only - energy cells, the cores, the cooling controller and
     * the host - and none of those carry items, so the loss is stored charge, not inventory. Cells and
     * interfaces never appear in it, because neither changes with the tier.</p>
     */
    private void swapInPlace(ServerLevel level, BlockPos pos, BlockState anchor, Classified swap,
                             ServerPlayer player, net.minecraft.world.InteractionHand hand,
                             MultiBlockDefinition current, MultiBlockDefinition target) {
        for (WorldPlannedBlock moved : swap.moved()) {
            level.setBlock(moved.worldPos(), moved.targetState(), Block.UPDATE_ALL);
        }
        level.setBlock(pos, anchor, Block.UPDATE_ALL);
        rebuildHost(level, pos);
        cn.dancingsnow.neoecoprototype.NeoECOPrototype.LOGGER.info("card free swap (upgrade): def=" + current + "->" + target
                + " positions=" + (swap.moved().size() + 1));
        if (consumedOnSuccess() && !player.hasInfiniteMaterials()) {
            var held = player.getItemInHand(hand);
            if (held.is(this)) {
                held.shrink(1);
            }
        }
        say(player, Component.translatable(upgradedKey(),
                tierLabel(target), swap.moved().size() + 1));
    }

    /**
     * The tier a definition climbs to, read off its own host block id
     * ({@code neoecoae:storage_system_l6} to {@code L6}). The success line cannot name a tier because
     * two of the three cards stop short of L9, and a message that lies about what the player just got
     * is worse than no message.
     */
    private static String tierLabel(MultiBlockDefinition definition) {
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getKey(definition.getOwner().value()).getPath();
        java.util.regex.Matcher found = java.util.regex.Pattern.compile("l(\\d+)\\w*$")
                .matcher(path);
        return found.find() ? "L" + found.group(1) : path;
    }

    /**
     * The higher tier's host block, wearing this machine's own orientation.
     *
     * <p>Returns null when the target tier does not want the machine at this size - the ceiling is a
     * config value read per family, so the two tiers of one family only agree while the config holds
     * still between them - or when the block has no item to pay for it with.</p>
     */
    private @Nullable BlockState anchorTarget(MultiBlockBuildController.Host host, Level level,
                                              BlockPos pos, MultiBlockDefinition target) {
        int length = largestSatisfiedLength(host, level, pos, host.getBuildDefinition(),
                target.getExpandMax());
        if (length < 0 || length != target.getExpandMax()) {
            return null;
        }
        Block anchorBlock = target.getOwner().value();
        if (anchorBlock.asItem() == Items.AIR) {
            return null;
        }
        return carryOver(level.getBlockState(pos), anchorBlock.defaultBlockState());
    }

    /**
     * The blocks the card hands back before it builds the bigger shape: every position it moves, plus -
     * when the click swaps tiers - the host block itself. They are not a cost, so they come back out of
     * the work order's input list; the loop above only ever runs while those blocks still stand.
     */
    private List<ItemStack> reclaimed(List<WorldPlannedBlock> moved, Level level) {
        List<ItemStack> back = new ArrayList<>(moved.size());
        for (WorldPlannedBlock position : moved) {
            back.add(new ItemStack(level.getBlockState(position.worldPos()).getBlock().asItem()));
        }
        return back;
    }

    private List<ItemStack> upgradeReclaimed(List<WorldPlannedBlock> moved, Level level, BlockPos host) {
        List<ItemStack> back = reclaimed(moved, level);
        back.add(new ItemStack(level.getBlockState(host).getBlock().asItem()));
        return back;
    }

    /** {@code bill} with each reclaimed stack subtracted from the entry for the same item. */
    private static List<RequiredItem> netted(List<RequiredItem> bill, List<ItemStack> back) {
        List<RequiredItem> net = new ArrayList<>(bill);
        for (ItemStack given : back) {
            for (int i = 0; i < net.size(); i++) {
                RequiredItem entry = net.get(i);
                if (!ItemStack.isSameItemSameComponents(entry.stack(), given)) {
                    continue;
                }
                long left = entry.count() - given.getCount();
                if (left <= 0) {
                    net.remove(i);
                } else {
                    net.set(i, new RequiredItem(entry.stack(), (int) left));
                }
                break;
            }
        }
        return net;
    }

    /**
     * The work order for this machine: one registered item per definition the card can be pointed at.
     * They have to be registered items and not renamed paper because all an AE2 pattern can hold is
     * {@code GenericStack(AEKey, long)} - an id and an amount - so a name written on a stack would not
     * survive being produced by the very recipe it names.
     */
    private static @Nullable Item growWorkOrder(MultiBlockDefinition definition) {
        if (definition == SimplifyStorageDefinition.L1) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_L1_STORAGE.get();
        }
        if (definition == SimplifyCraftingDefinition.L1) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_F1_CRAFTING.get();
        }
        if (definition == SimplifyTrinityDefinition.L1) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_TRINITY.get();
        }
        if (definition == NEMultiBlocks.STORAGE_SYSTEM_L4) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_STORAGE_L4.get();
        }
        if (definition == NEMultiBlocks.STORAGE_SYSTEM_L6) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_STORAGE_L6.get();
        }
        if (definition == NEMultiBlocks.STORAGE_SYSTEM_L9) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_STORAGE_L9.get();
        }
        if (definition == NEMultiBlocks.COMPUTATION_SYSTEM_L4) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_COMPUTATION_L4.get();
        }
        if (definition == NEMultiBlocks.COMPUTATION_SYSTEM_L6) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_COMPUTATION_L6.get();
        }
        if (definition == NEMultiBlocks.COMPUTATION_SYSTEM_L9) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_COMPUTATION_L9.get();
        }
        if (definition == NEMultiBlocks.CRAFTING_SYSTEM_L4) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_CRAFTING_L4.get();
        }
        if (definition == NEMultiBlocks.CRAFTING_SYSTEM_L6) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_CRAFTING_L6.get();
        }
        if (definition == NEMultiBlocks.CRAFTING_SYSTEM_L9) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_CRAFTING_L9.get();
        }
        return null;
    }

    /** The work order for the tier this click was heading to; every tier the card climbs to has one. */
    private static @Nullable Item upgradeWorkOrder(MultiBlockDefinition target) {
        if (target == NEMultiBlocks.STORAGE_SYSTEM_L9) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_UPGRADE_STORAGE_L9.get();
        }
        if (target == NEMultiBlocks.COMPUTATION_SYSTEM_L9) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_UPGRADE_COMPUTATION_L9.get();
        }
        if (target == NEMultiBlocks.CRAFTING_SYSTEM_L9) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_UPGRADE_CRAFTING_L9.get();
        }
        return null;
    }

    /**
     * What a refused click leaves behind is a pair: the machine's work order, and a processing pattern
     * that turns the parts this step still needs into one more of those work orders. The direction is not a style choice - a pattern that ate
     * the ticket instead would turn one blank pattern into 48 drives and cells, which is what he caught
     * in game. The work order is therefore always the output, and the pattern is what tells the network
     * what "paid for" costs.
     *
     * <p>Both costs - the sheet the ticket is written on and the blank pattern the recipe is stored on -
     * are checked before either is taken, and each refusal says so out loud: a silent return here reads
     * to the player as a broken card.</p>
     */
    private void handOverTicket(ServerLevel level, ServerPlayer player, @Nullable Item workOrder,
                                List<RequiredItem> bill, Component missing) {
        cn.dancingsnow.neoecoprototype.NeoECOPrototype.LOGGER.info("card ticket: order="
                + (workOrder == null ? "none"
                        : net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(workOrder))
                + " billKinds=" + bill.size());
        if (workOrder == null) {
            return;
        }
        if (NeoECOPrototypeServerConfig.STRUCTURE_CARD_CONSUMES_ITEMS.get()) {
            ItemStack paper = new ItemStack(Items.PAPER);
            ItemStack blank = new ItemStack(AEItems.BLANK_PATTERN.asItem());
            if (MultiBlockPlacementService.countMatchingItems(player, paper) <= 0) {
                say(player, Component.translatable("card.neoecoprototype.expansion.no_paper", missing));
                return;
            }
            if (MultiBlockPlacementService.countMatchingItems(player, blank) <= 0) {
                say(player, Component.translatable("card.neoecoprototype.expansion.no_blank", missing));
                return;
            }
            if (!consumeOne(player, paper) || !consumeOne(player, blank)) {
                return;
            }
        }
        handTo(level, player.blockPosition(), new ItemStack(workOrder), player);
        List<GenericStack> inputs = new ArrayList<>(bill.size());
        long parts = 0;
        for (RequiredItem required : bill) {
            inputs.add(new GenericStack(AEItemKey.of(required.stack().getItem()), required.count()));
            parts += required.count();
        }
        handTo(level, player.blockPosition(), PatternDetailsHelper.encodeProcessingPattern(inputs,
                List.of(new GenericStack(AEItemKey.of(workOrder), 1))), player);
        cn.dancingsnow.neoecoprototype.NeoECOPrototype.LOGGER.info("card ticket printed: parts=" + parts
                + " inputKinds=" + inputs.size());
        say(player, Component.translatable("card.neoecoprototype.expansion.pattern", parts));
    }

    /**
     * Removes one of {@code wanted} from the player's own inventory - the nine hotbar slots, the
     * twenty-seven above them and the off hand, which is the scope eco's own {@code hasRequiredItems}
     * checks against.
     */
    private static boolean consumeOne(Player player, ItemStack wanted) {
        Inventory inventory = player.getInventory();
        List<ItemStack> haystack = new ArrayList<>(inventory.items);
        haystack.add(inventory.offhand.get(0));
        for (ItemStack stack : haystack) {
            if (ItemStack.isSameItemSameComponents(stack, wanted)) {
                stack.shrink(1);
                inventory.setChanged();
                return true;
            }
        }
        return false;
    }

    /**
     * The largest shape length the world already satisfies at this host, or below
     * {@code expandMin} when none of them fit.
     *
     * <p>Read off the world rather than off {@code selectedBuildLength}: a satisfied shape is one
     * whose plan needs nothing placed and whose only conflicts are positions already holding the
     * right block. That is the size of the machine standing here, which is what both "grow it" and
     * "change its tier" have to be measured against.</p>
     */
    private int largestSatisfiedLength(MultiBlockBuildController.Host host, Level level, BlockPos pos,
                                        @Nullable MultiBlockDefinition definition, int max) {
        if (definition == null) {
            return -1;
        }
        for (int length = max; length >= definition.getExpandMin(); length--) {
            Classified satisfied = classify(preview(host, level, pos, definition, length), Map.of(),
                    level, false);
            if (satisfied.foreign() == null && satisfied.moved().isEmpty()
                    && satisfied.plan().getMissingBlocks().isEmpty()) {
                return length;
            }
        }
        return definition.getExpandMin() - 1;
    }

    /**
     * Moves the positions eco's own formation wrote off the conflict list, splits what is left into
     * "ours to move" and "not ours to touch", and prices what is moved.
     *
     * <p>A conflict is ignored when the standing block is the block the target shape wants there - the
     * two then differ at most by properties the cluster sets on itself, which is not a reason to
     * refuse. Otherwise it is queued to move when the standing block is one this machine claims at its
     * current size: the outer shell, when growing ({@code tiered} is false and the block has to hold
     * nothing), or any block the next tier wants in a different shape, when swapping tiers. Anything
     * else stops the card - a drive that somehow ended up in the way must never be broken with cells
     * inside it.</p>
     */
    private Classified classify(MultiBlockPlacementPlan plan, Map<BlockPos, WorldPlannedBlock> owned,
                                Level level, boolean tiered) {
        Map<BlockPos, WorldPlannedBlock> wanted = plannedByPosition(plan);
        List<WorldPlannedBlock> moved = new ArrayList<>();
        List<WorldPlannedBlock> missing = new ArrayList<>(plan.getMissingBlocks());
        List<RequiredItem> bill = new ArrayList<>(plan.getRequiredItems());
        Set<BlockPos> settled = new HashSet<>();
        int reusedAsIs = 0;
        for (BlockPos conflict : plan.getConflictPositions()) {
            BlockState standing = level.getBlockState(conflict);
            WorldPlannedBlock wantBlock = wanted.get(conflict);
            BlockState want = wantBlock == null ? null : wantBlock.targetState();
            if (want == null) {
                return refused(plan, conflict);
            }
            // Checked before anything about ownership: a position holding the very block the target
            // shape wants differs at most by what formation writes, and that is no reason to refuse.
            // The caller that measures the machine's current size passes no ownership map at all, so
            // putting this check after the ownership test would make every formed machine look unbuilt.
            if (standing.getBlock() == want.getBlock()) {
                reusedAsIs++;
                settled.add(conflict);
                continue;
            }
            WorldPlannedBlock mine = owned.get(conflict);
            if (mine == null || mine.targetState().getBlock() != standing.getBlock()) {
                return refused(plan, conflict);
            }
            // A shell the bigger shape wants elsewhere, or a block whose tier is written into its id.
            // A block with no item to give back for is not ours to move at all.
            if (standing.getBlock().asItem() == Items.AIR
                    || (tiered ? want.getBlock() == mine.targetState().getBlock()
                    : !isShell(level, conflict))) {
                return refused(plan, conflict);
            }
            WorldPlannedBlock replacement = new WorldPlannedBlock(conflict, want,
                    wantBlock.requiredItem().copy());
            moved.add(replacement);
            missing.add(replacement);
            mergeItem(bill, replacement.requiredItem());
            settled.add(conflict);
        }
        if (settled.isEmpty()) {
            return new Classified(plan, moved, bill, null);
        }
        List<BlockPos> stillConflicting = new ArrayList<>(plan.getConflictPositions());
        stillConflicting.removeAll(settled);
        return new Classified(new MultiBlockPlacementPlan(plan.getAllBlocks(), missing, stillConflicting,
                bill, plan.getReusedBlockCount() + reusedAsIs), moved, bill, null);
    }

    private static Classified refused(MultiBlockPlacementPlan plan, BlockPos foreign) {
        return new Classified(plan, List.of(), List.of(), foreign);
    }

    /**
     * Whether a position holds one of the two blocks that can stand in the outer column and carry
     * nothing: eco's casing, and the computation system's cooling controller - {@code
     * createComputationSystem} puts the cooler in that column while the other two systems put only
     * casings there.
     *
     * <p>Both are safe to move. A casing's block entity is what writes the {@code invisible} property
     * and holds no items, and the cooling controller's is just {@code tier} plus {@code mirrored}
     * (checked against the 21.2.1 jar, not inferred).</p>
     */
    private static boolean isShell(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity == null || blockEntity instanceof ECOMachineCasingBlockEntity
                || blockEntity instanceof ECOComputationCoolingControllerBlockEntity;
    }

    /**
     * Hands one block back, item for block.
     *
     * <p>Deliberately not {@code Block#getDrops}: the card is what is in hand while this runs, so a
     * drop table that asks for a tool would silently return nothing and the player would pay for the
     * bigger shape with blocks they never got.</p>
     */
    private void giveBack(Level level, BlockPos pos, ServerPlayer player) {
        Block itemised = level.getBlockState(pos).getBlock();
        level.destroyBlock(pos, false, player);
        if (player.isCreative() || itemised.asItem() == Items.AIR) {
            return;
        }
        handTo(level, pos, new ItemStack(itemised.asItem()), player);
    }

    /** Into the inventory first, on the floor second - a reclaimed block must never just vanish. */
    private static void handTo(Level level, BlockPos where, ItemStack stack, ServerPlayer player) {
        if (!player.getInventory().add(stack.copy())) {
            level.addFreshEntity(new ItemEntity(level, where.getX() + 0.5D, where.getY() + 0.5D,
                    where.getZ() + 0.5D, stack.copy()));
        }
    }

    private MultiBlockPlacementPlan preview(MultiBlockBuildController.Host host, Level level, BlockPos pos,
                                            MultiBlockDefinition definition, int length) {
        return MultiBlockPlacementService.preview(level, pos, level.getBlockState(pos), definition,
                length, host.isMirrorBuild());
    }

    private static Map<BlockPos, WorldPlannedBlock> plannedByPosition(MultiBlockPlacementPlan plan) {
        Map<BlockPos, WorldPlannedBlock> planned = new LinkedHashMap<>();
        for (WorldPlannedBlock block : plan.getAllBlocks()) {
            planned.put(block.worldPos(), block);
        }
        return planned;
    }

    /** eco's own merge is private, and an unmerged bill would charge the same item twice. */
    private static void mergeItem(List<RequiredItem> bill, ItemStack stack) {
        for (int i = 0; i < bill.size(); i++) {
            RequiredItem entry = bill.get(i);
            if (ItemStack.isSameItemSameComponents(entry.stack(), stack)) {
                bill.set(i, entry.grow(stack.getCount()));
                return;
            }
        }
        bill.add(new RequiredItem(stack.copy(), stack.getCount()));
    }

    private void reportBlocked(ServerPlayer player, BlockPos where) {
        say(player, Component.translatable("card.neoecoprototype.expansion.blocked", where.getX(),
                where.getY(), where.getZ()));
    }

    /**
     * The bill minus what the planner counted as reused. Only the entries still unaffordable would be
     * worth showing, and {@code hasRequiredItems} does not say which those are, so this reports the
     * full bill when the player is short - the honest reading of "you cannot pay for this" without a
     * screen to itemise it against their inventory.
     */
    private Component shortfall(List<RequiredItem> required) {
        StringBuilder text = new StringBuilder();
        int shown = Math.min(required.size(), MAX_REPORTED_ITEMS);
        for (int i = 0; i < shown; i++) {
            RequiredItem item = required.get(i);
            if (i > 0) {
                text.append("，");
            }
            text.append(item.stack().getHoverName().getString()).append(" ×").append(item.count());
        }
        if (required.size() > shown) {
            text.append(" …");
        }
        return Component.literal(text.toString());
    }

    private void rebuildHost(ServerLevel level, BlockPos pos) {
        // The anchor cell was replaced, so eco's host block entity went with it and the machine has to
        // be re-formed through whichever block entity stands there now.
        if (level.getBlockEntity(pos) instanceof MultiBlockBuildController.Host fresh) {
            fresh.rebuildAfterBuild();
        }
    }

    /**
     * Places the new host block when a build stopped part-way through. The old one is already back in
     * the player's inventory, so the machine is left with a higher tier's host on a lower tier's
     * frame - which will not form - rather than with a hole in it.
     */
    private void finishAnchor(ServerLevel level, BlockPos pos, BlockState anchor, ServerPlayer player) {
        level.setBlock(pos, anchor, Block.UPDATE_ALL);
        rebuildHost(level, pos);
    }

    /** The new block wearing the old block's orientation, except for what formation writes itself. */
    private static BlockState carryOver(BlockState from, BlockState to) {
        for (Property<?> property : from.getProperties()) {
            if (!FORMED_PROPERTY.equals(property.getName())) {
                to = copyProperty(to, from, property);
            }
        }
        return to;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState to, BlockState from,
                                                                     Property<T> property) {
        if (!to.hasProperty(property)) {
            return to;
        }
        T value = from.getValue(property);
        return to.setValue(property, value);
    }

    private void say(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }

    private void say(ServerPlayer player, Component message) {
        player.displayClientMessage(message, true);
    }
}
