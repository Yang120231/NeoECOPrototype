package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider;
import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * C1 computation host. {@code communication_interface} carries whether the interface cell holds the
 * communication interface, so the blockstate file can hand that case its own formed model instead of
 * making a resource pack read the world. The calculator measures it and hands it here; placement never
 * sets it.
 *
 * <p>The host's block entity is eco's own {@code ECOComputationSystemBlockEntity} on purpose. AE2 files
 * every grid node under {@code owner.getClass()} and looks machines up by that exact key
 * ({@code Grid.add} has a single {@code put}, no superclass walk), so a subclass here is invisible to
 * eco's {@code getMachines(ECOComputationSystemBlockEntity.class)} -- which is how the subsystem gets
 * into AE2's CPU list and, through the same collection, into crafting execution.
 *
 * <p>{@code energized_threading_core} stays out: the threading core draws its own advanced face, so the
 * host never needed to know which hand of the threading line it was standing in front of.
 * {@code energized_parallel_core} is back, one property, because the artwork for it is now real: the
 * formed face swaps two sheets ({@code controller_formed_energized} and its light) when the energized core
 * stands in the cell behind the host. It was removed in 1.2.10 on the grounds that every combination model
 * was a copy of the plain face and the property only bought a 512-entry blockstate -- that premise is gone
 * now that the sheets differ.
 */
public class SimplifyComputationSystemBlock extends ECOComputationSystem
        implements IHostPanelTextureProvider {
    /** True when the interface cell holds {@code simplify_computation_network_interface}, the GUI one. */
    public static final BooleanProperty COMMUNICATION_INTERFACE =
            BooleanProperty.create("communication_interface");

    /** True when the cell directly behind the host holds the energized parallel core. */
    public static final BooleanProperty ENERGIZED_PARALLEL_CORE =
            BooleanProperty.create("energized_parallel_core");

    /** The one shape read the host has to publish: which interface, and whether the energized core is in. */
    public record Shape(boolean communicationInterface, boolean energizedParallelCore) { }

    /** What the calculator measured, parked per dimension and position until a tick may write it. */
    private static final Map<ResourceKey<Level>, Map<Long, Shape>> PENDING_SHAPE =
            new ConcurrentHashMap<>();

    private static Map<Long, Shape> pendingFor(Level level) {
        return PENDING_SHAPE.computeIfAbsent(level.dimension(), d -> new ConcurrentHashMap<>());
    }

    public SimplifyComputationSystemBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(COMMUNICATION_INTERFACE, false)
                .setValue(ENERGIZED_PARALLEL_CORE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COMMUNICATION_INTERFACE, ENERGIZED_PARALLEL_CORE);
    }

    /**
     * Records what the machine turned out to hold and asks for a tick that can write it. The calculator
     * cannot write on its own stack: it runs inside AE2's cluster recalculation, which holds a global
     * "modification in progress" latch, and a block change made anywhere on that stack wedges every
     * multiblock for the rest of the session -- measured, not theorised. Repeated calls collapse into the
     * one scheduled tick, and a tick that finds nothing to change writes nothing.
     */
    public void publishShape(ServerLevel level, BlockPos pos, Shape shape) {
        pendingFor(level).put(pos.asLong(), shape);
        level.scheduleTick(pos, this, 1);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        // The only reader of a parked shape is the tick scheduled for this position, and Minecraft skips a
        // block tick whose block no longer matches -- so once the host is gone nothing will ever consume the
        // entry. The table is keyed by dimension rather than by world, which is what lets the next host at
        // the same coordinate read a shape left behind by a machine that no longer exists.
        if (!level.isClientSide) {
            pendingFor(level).remove(pos.asLong());
        }
    }

    /**
     * Drops every parked shape. Called when a server stops, because the table is static and keyed by
     * dimension id: in one client process a second save would otherwise inherit the first one's entries.
     */
    public static void clearPendingShapes() {
        PENDING_SHAPE.clear();
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        Shape shape = pendingFor(level).remove(pos.asLong());
        BlockState next = state;
        if (shape != null && state.hasProperty(COMMUNICATION_INTERFACE)) {
            // Both bits describe the formed look only: an unformed host must not wear a formed face.
            next = next.setValue(COMMUNICATION_INTERFACE, state.getValue(NEBlock.FORMED)
                            && shape.communicationInterface())
                    .setValue(ENERGIZED_PARALLEL_CORE, state.getValue(NEBlock.FORMED)
                            && shape.energizedParallelCore());
        }
        // eco lights these two from a shell cell beside the host and reads them back to decide whether the
        // cluster registers its CPUs through a switch frequency. An L1 machine has no switch block and so
        // no frequency, and leaving them set sends our CPUs into a logical network that does not exist -
        // the machine looks connected but never appears in the network's CPU list. Nothing clears them any
        // more either: our calculator replaces the eco method that would, so a host an older world saved
        // with them set would carry them forever.
        next = pinned(next, ECOComputationSystem.NETWORK_SWITCH);
        next = pinned(next, ECOComputationSystem.HIGH_ENERGY_NETWORK_SWITCH);
        if (next != state) {
            level.setBlock(pos, next, Block.UPDATE_CLIENTS);
        }
    }

    private static BlockState pinned(BlockState state, Property<Boolean> bit) {
        return state.hasProperty(bit) && state.getValue(bit) ? state.setValue(bit, false) : state;
    }

    /** The L1 badge, cut by eco at 13x18, and the host bracket, read as two 13x8 halves. */
    private static final ResourceLocation PANEL_TIER = panel("computation_tier.png");
    private static final ResourceLocation PANEL_MARKER = panel("computation_marker.png");
    private static final ResourceLocation SWITCH_ON = panel("computation_switch_on.png");
    private static final ResourceLocation SWITCH_ON_HOVER = panel("computation_switch_on_hover.png");

    @Nullable
    private static ResourceLocation panel(String file) {
        return ResourceLocation.fromNamespaceAndPath(
                cn.dancingsnow.neoecoprototype.NeoECOPrototype.MOD_ID, "textures/gui/panel/" + file);
    }

    @Override
    @Nullable
    public ResourceLocation getPanelTexture(BlockState state,
                                            IHostPanelTextureProvider.Part part) {
        return switch (part) {
            case COMPUTATION_TIER -> PANEL_TIER;
            case COMPUTATION_MARKER -> PANEL_MARKER;
            default -> null;
        };
    }

    /**
     * The three switches' tiles. {@code index} is which one eco is dressing - 0 substitutions,
     * 1 cycle planning, 2 fast planner, read off its own name comparison in {@code createSwitch} - and the
     * artwork has one green tile for the engaged state, so the answer does not branch on it.
     *
     * <p>Off is left to eco on purpose: eco's off tile and the artwork sheet's are the same pixels.
     * {@code COMPUTATION_STATUS} stays unanswered for the same kind of reason in reverse - the {@code lit}
     * eco hands us is its own predicate (lamp 0 is {@code endgamePink}), not "this host holds an energized
     * parallel core", and the channel carries art only. Swapping colours there would rename a signal the
     * tooltip next to it still describes differently.
     */
    @Override
    @Nullable
    public ResourceLocation getPanelTexture(BlockState state,
                                            IHostPanelTextureProvider.Part part,
                                            int index, boolean on, boolean hover) {
        if (part != IHostPanelTextureProvider.Part.COMPUTATION_SWITCH || !on) {
            return null;
        }
        return hover ? SWITCH_ON_HOVER : SWITCH_ON;
    }
}
