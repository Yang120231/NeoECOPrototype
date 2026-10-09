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

    /** The parts one upgrade step trades away, and the parts it wants in their place. */
    private record Exchange(List<GenericStack> inputs, List<GenericStack> outputs) {
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
        if (player == null) {
            return InteractionResult.PASS;
        }
        boolean grow = player.isShiftKeyDown();
        Exchange exchange = grow ? null : exchangeFor(host, level, pos, player);
        if (!grow && exchange == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }
        if (grow) {
            grow(host, (ServerLevel) level, pos, serverPlayer);
        } else {
            handOverPattern(exchange, (ServerLevel) level, serverPlayer);
        }
        return InteractionResult.CONSUME;
    }

    private void grow(MultiBlockBuildController.Host host, ServerLevel level, BlockPos pos,
                      ServerPlayer player) {
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
            upgradeTier(host, level, pos, player, definition, current);
            return;
        }

        Classified grown = classify(preview(host, level, pos, definition, target),
                plannedByPosition(preview(host, level, pos, definition, current)), level, false);
        if (grown.foreign() != null) {
            reportBlocked(player, grown.foreign());
            return;
        }
        MultiBlockPlacementPlan plan = grown.plan();
        if (!player.isCreative() && !MultiBlockPlacementService.hasRequiredItems(player, grown.bill())) {
            say(player, Component.translatable("card.neoecoprototype.expansion.short_of",
                    shortfall(grown.bill())));
            return;
        }
        // Paid for first, so a player who cannot afford the larger shape keeps the machine they had.
        for (WorldPlannedBlock moved : grown.moved()) {
            giveBack(level, moved.worldPos(), player);
        }

        host.setSelectedBuildLength(target);
        if (!MultiBlockPlacementService.buildInstant(level, plan, player)) {
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
                             ServerPlayer player, MultiBlockDefinition current, int length) {
        MultiBlockDefinition target = nextTier(current);
        if (target == null) {
            say(player, Component.translatable("card.neoecoprototype.expansion.at_max", length));
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
        List<RequiredItem> bill = new ArrayList<>(swap.bill());
        mergeItem(bill, new ItemStack(anchor.getBlock().asItem()));
        if (!player.isCreative() && !MultiBlockPlacementService.hasRequiredItems(player, bill)) {
            say(player, Component.translatable("card.neoecoprototype.expansion.short_of",
                    shortfall(bill)));
            return;
        }

        for (WorldPlannedBlock moved : swap.moved()) {
            giveBack(level, moved.worldPos(), player);
        }
        giveBack(level, pos, player);
        if (!MultiBlockPlacementService.buildInstant(level, swap.plan(), player)) {
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
        say(player, Component.translatable("card.neoecoprototype.expansion.upgraded",
                swap.moved().size() + 1));
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

    /** The swap this click would make, or null when there is nothing to trade and no reason to act. */
    private @Nullable Exchange exchangeFor(MultiBlockBuildController.Host host, Level level, BlockPos pos,
                                           Player player) {
        if (!host.canPlayerInteract(player) || host.isBuildInProgress()) {
            return null;
        }
        MultiBlockDefinition current = host.getBuildDefinition();
        if (current == null) {
            return null;
        }
        MultiBlockDefinition target = nextTier(current);
        if (target == null || !hasBlankPattern(player)) {
            return null;
        }
        // anchorTarget already answers both questions a plain click cares about: whether the next tier
        // wants this machine at exactly this size, and whether it is filled out to the ceiling.
        if (anchorTarget(host, level, pos, target) == null) {
            return null;
        }
        return exchangeOf(host, level, pos, current, target, current.getExpandMax());
    }

    /**
     * The bill as a pattern instead of a checkout: everything standing here that the next tier wants
     * differently, in one column, the same parts of the next tier in the other.
     *
     * <p>A straight trade - no difference is computed, which is why the parts that come back out of
     * the machine are exactly the parts the exchange pattern spends.</p>
     */
    private @Nullable Exchange exchangeOf(MultiBlockBuildController.Host host, Level level, BlockPos pos,
                                          MultiBlockDefinition current, MultiBlockDefinition target,
                                          int length) {
        Map<BlockPos, WorldPlannedBlock> owned = plannedByPosition(preview(host, level, pos, current, length));
        Classified swap = classify(preview(host, level, pos, target, length), owned, level, true);
        if (swap.foreign() != null || swap.moved().isEmpty()) {
            return null;
        }
        Map<Item, Integer> inputs = new LinkedHashMap<>();
        Map<Item, Integer> outputs = new LinkedHashMap<>();
        for (WorldPlannedBlock moved : swap.moved()) {
            addPart(inputs, owned.get(moved.worldPos()).targetState().getBlock());
            addPart(outputs, moved.targetState().getBlock());
        }
        addPart(inputs, level.getBlockState(pos).getBlock());
        addPart(outputs, target.getOwner().value());
        return new Exchange(stacks(inputs), stacks(outputs));
    }

    private void handOverPattern(Exchange exchange, ServerLevel level, ServerPlayer player) {
        if (!consumeOne(player, new ItemStack(AEItems.BLANK_PATTERN.asItem()))) {
            return;
        }
        ItemStack pattern = PatternDetailsHelper.encodeProcessingPattern(exchange.inputs(),
                exchange.outputs());
        handTo(level, player.blockPosition(), pattern, player);
        say(player, Component.translatable("card.neoecoprototype.expansion.upgrade.pattern",
                total(exchange.inputs()), total(exchange.outputs())));
    }

    private static long total(List<GenericStack> parts) {
        long sum = 0;
        for (GenericStack part : parts) {
            sum += part.amount();
        }
        return sum;
    }

    private static void addPart(Map<Item, Integer> parts, Block block) {
        Item item = block.asItem();
        if (item == Items.AIR) {
            return;
        }
        parts.merge(item, 1, Integer::sum);
    }

    private static List<GenericStack> stacks(Map<Item, Integer> parts) {
        List<GenericStack> stacks = new ArrayList<>(parts.size());
        parts.forEach((item, count) -> stacks.add(new GenericStack(AEItemKey.of(item), count)));
        return stacks;
    }

    private static boolean hasBlankPattern(Player player) {
        return MultiBlockPlacementService.countMatchingItems(player,
                new ItemStack(AEItems.BLANK_PATTERN.asItem())) > 0;
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
            WorldPlannedBlock want = wanted.get(conflict);
            WorldPlannedBlock mine = owned.get(conflict);
            if (want == null || mine == null || mine.targetState().getBlock() != standing.getBlock()) {
                return refused(plan, conflict);
            }
            if (standing.getBlock() == want.targetState().getBlock()) {
                reusedAsIs++;
                settled.add(conflict);
                continue;
            }
            // A shell the bigger shape wants elsewhere, or a block whose tier is written into its id.
            // A block with no item to give back for is not ours to move at all.
            if (standing.getBlock().asItem() == Items.AIR
                    || (tiered ? want.targetState().getBlock() == mine.targetState().getBlock()
                    : !isShell(level, conflict))) {
                return refused(plan, conflict);
            }
            WorldPlannedBlock replacement = new WorldPlannedBlock(conflict, want.targetState(),
                    want.requiredItem().copy());
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
