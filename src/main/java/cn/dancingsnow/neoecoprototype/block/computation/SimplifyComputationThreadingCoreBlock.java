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
    private static final ResourceLocation CORE_ENERGIZED = ResourceLocation.fromNamespaceAndPath(
            NeoECOPrototype.MOD_ID, "textures/gui/panel/computation_core_energized.png");

    public SimplifyComputationThreadingCoreBlock(Properties properties) {
        this(properties, SimplifyTier.L1);
    }

    public SimplifyComputationThreadingCoreBlock(Properties properties, IECOTier tier) {
        super(properties, tier);
    }

    @Override
    public ResourceLocation getPanelTexture(BlockState state, IHostPanelTextureProvider.Part part) {
        if (part != IHostPanelTextureProvider.Part.COMPUTATION_CORE) {
            return null;
        }
        // All four of our tiers report the same tier index, so the artwork cannot key off getTier()'s number -
        // the energized threading core is its own block instance, and this is the only place its identity shows.
        return getTier() == SimplifyTier.L1_ENERGIZED_THREADING ? CORE_ENERGIZED : CORE;
    }
}
