package cn.dancingsnow.neoecoprototype.block.crafting;

import cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

/**
 * F1 crafting host. Publishes which of the two interface blocks the finished machine holds so the
 * blockstate file can give each case its own formed model. The calculator writes it; placement never
 * does.
 */
public class SimplifyCraftingSystemBlock extends ECOCraftingSystem
        implements IHostPanelTextureProvider {
    /**
     * True when the interface cell holds {@code simplify_crafting_network_interface} -- the 通讯接口, the
     * one that opens eco's interface UI. Same rule as C1 and L1: the id with "network" in it is the panel
     * the player uses, and the plain one is the endpoint the one-click builder places.
     */
    public static final BooleanProperty COMMUNICATION_INTERFACE =
            BooleanProperty.create("communication_interface");

    /**
     * The easter egg: once per host, the machine rolls a one-in-sixteen chance to grow a face, and the
     * result is saved into the host so re-forming never re-rolls it. Only meaningful with
     * {@code formed=true}, which is where the blockstate hands over to the {@code *_face} models.
     */
    public static final BooleanProperty HAS_MIND = BooleanProperty.create("has_mind");

    /**
     * How contented the mind currently is, 0..2, published by the host once a second and only when it
     * crosses a band. It rides the same deferred write as {@link #HAS_MIND} - the value is applied on the
     * next server tick, never from inside AE2's cluster recalculation - and only the {@code *_face} models
     * read it, so a machine without a face has exactly one appearance whatever this says.
     */
    public static final IntegerProperty MIND_TIER = IntegerProperty.create("mind_tier", 0, 2);

    public SimplifyCraftingSystemBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(COMMUNICATION_INTERFACE, false)
                .setValue(HAS_MIND, false)
                .setValue(MIND_TIER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COMMUNICATION_INTERFACE, HAS_MIND, MIND_TIER);
    }

    /** The L1 badge, cut by eco at 13x18, and the host bracket, read as two 13x8 halves. */
    private static final ResourceLocation PANEL_TIER = panel("crafting_tier.png");
    private static final ResourceLocation PANEL_MARKER = panel("crafting_marker.png");
    private static final ResourceLocation MODE_ON = panel("crafting_mode_button_on.png");
    private static final ResourceLocation MODE_ON_HOVER = panel("crafting_mode_button_on_hover.png");

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
            case CRAFTING_TIER -> PANEL_TIER;
            case CRAFTING_MARKER -> PANEL_MARKER;
            default -> null;
        };
    }

    /**
     * The two mode buttons' tiles. {@code index} says which: eco derives it from the JSON's {@code mode}
     * field, so 0 is active cooling and 1 is overclock - the element ids beside them carry a
     * {@code _button} suffix and are not what that comparison looks at. One green tile serves both, so the
     * answer ignores it, and the off state is left to eco because its off tile is the same pixels as the
     * artwork sheet's.
     */
    @Override
    @Nullable
    public ResourceLocation getPanelTexture(BlockState state, IHostPanelTextureProvider.Part part,
                                            int index, boolean on, boolean hover) {
        if (part != IHostPanelTextureProvider.Part.CRAFTING_MODE || !on) {
            return null;
        }
        return hover ? MODE_ON_HOVER : MODE_ON;
    }
}
