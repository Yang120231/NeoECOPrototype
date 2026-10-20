package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider;
import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationThreadingCore;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/** The threading core is the middle row of the computation panel; see the parallel core for the contract. */
public class SimplifyComputationThreadingCoreBlock extends ECOComputationThreadingCore
        implements IHostPanelTextureProvider {
    private static final ResourceLocation CORE = ResourceLocation.fromNamespaceAndPath(
            NeoECOPrototype.MOD_ID, "textures/gui/panel/computation_core.png");

    public SimplifyComputationThreadingCoreBlock(Properties properties) {
        this(properties, SimplifyTier.L1);
    }

    public SimplifyComputationThreadingCoreBlock(Properties properties, IECOTier tier) {
        super(properties, tier);
    }

    @Override
    public ResourceLocation getPanelTexture(BlockState state, IHostPanelTextureProvider.Part part) {
        return part == IHostPanelTextureProvider.Part.COMPUTATION_CORE ? CORE : null;
    }
}
