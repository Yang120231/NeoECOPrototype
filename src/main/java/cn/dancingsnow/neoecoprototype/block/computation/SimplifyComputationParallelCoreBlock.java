package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider;
import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationParallelCore;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The parallel core sits in the computation panel's upper and lower rows, and eco asks this block - not the
 * block entity - which texture to cut those cells from.
 *
 * <p>The contract is measured off {@code ComputationControllerTextures.member}: a custom texture is always
 * read at its own top-left corner, 10x5 for the upper row and 10x6 for the lower, so the two files here are
 * single-sprite strips cropped from the artwork sheet at the coordinates eco uses for its own glyphs.
 */
public class SimplifyComputationParallelCoreBlock extends ECOComputationParallelCore
        implements IHostPanelTextureProvider {
    private static final ResourceLocation UPPER = ResourceLocation.fromNamespaceAndPath(
            NeoECOPrototype.MOD_ID, "textures/gui/panel/computation_upper.png");
    private static final ResourceLocation LOWER = ResourceLocation.fromNamespaceAndPath(
            NeoECOPrototype.MOD_ID, "textures/gui/panel/computation_lower.png");

    public SimplifyComputationParallelCoreBlock(Properties properties) {
        this(properties, SimplifyTier.L1);
    }

    public SimplifyComputationParallelCoreBlock(Properties properties, IECOTier tier) {
        super(properties, tier);
    }

    @Override
    public ResourceLocation getPanelTexture(BlockState state, IHostPanelTextureProvider.Part part) {
        return switch (part) {
            case COMPUTATION_UPPER -> UPPER;
            case COMPUTATION_LOWER -> LOWER;
            default -> null;
        };
    }
}
