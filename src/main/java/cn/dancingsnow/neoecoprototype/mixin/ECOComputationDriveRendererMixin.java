package cn.dancingsnow.neoecoprototype.mixin;

/* eco 21.2.9 uses tier-only cable lookups; 21.2.10 also passes the host block. Each injection group
 * must still wrap one connected and two disconnected lookups across either signature. Our own drive
 * overrides renderFixed, so reaching these wrappers continues to mean the renderer is eco's. */

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.client.ComputationCableModels;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;

/**
 * Wears the glowing computation cable in someone else's frame.
 *
 * <p>The split Ted asked for is by host, not by cell: our own frame shows the plain pair, an eco frame
 * shows the {@code _normal} pair. Older eco versions key the lookup only on the inserted cell's tier;
 * newer ones also pass the host block. The wrappers below answer for our tiers and preserve eco's
 * original lookup, including that host argument, for every other tier. Our drive has its own
 * {@code renderFixed}, which this mixin cannot see.
 */
@Mixin(cn.dancingsnow.neoecoae.client.renderer.blockentity.ECOComputationDriveRenderer.class)
public final class ECOComputationDriveRendererMixin {
    @Group(name = "neoecoprototype$connectedCable", min = 1, max = 1)
    @WrapOperation(method = "renderFixed(Lcn/dancingsnow/neoecoae/blocks/entity/computation/ECOComputationDriveBlockEntity;"
            + "FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE",
                    target = "Lcn/dancingsnow/neoecoae/api/ECOComputationModels;getCableConnectedModel("
                            + "Lcn/dancingsnow/neoecoae/api/IECOTier;)Lnet/minecraft/resources/ResourceLocation;"), require = 0)
    private ResourceLocation neoecoprototype$connected(IECOTier tier, Operation<ResourceLocation> original) {
        var upper = neoecoprototype$upperHostCable(tier, false);
        return upper != null ? upper : original.call(tier);
    }

    @Group(name = "neoecoprototype$connectedCable", min = 1, max = 1)
    @WrapOperation(method = "renderFixed(Lcn/dancingsnow/neoecoae/blocks/entity/computation/ECOComputationDriveBlockEntity;"
            + "FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE",
                    target = "Lcn/dancingsnow/neoecoae/api/ECOComputationModels;getCableConnectedModel("
                            + "Lcn/dancingsnow/neoecoae/api/IECOTier;Lnet/minecraft/world/level/block/Block;)"
                            + "Lnet/minecraft/resources/ResourceLocation;"), require = 0)
    private ResourceLocation neoecoprototype$connectedWithHost(IECOTier tier, Block host, Operation<ResourceLocation> original) {
        var upper = neoecoprototype$upperHostCable(tier, false);
        return upper != null ? upper : original.call(tier, host);
    }

    @Group(name = "neoecoprototype$disconnectedCable", min = 2, max = 2)
    @WrapOperation(method = "renderFixed(Lcn/dancingsnow/neoecoae/blocks/entity/computation/ECOComputationDriveBlockEntity;"
            + "FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE",
                    target = "Lcn/dancingsnow/neoecoae/api/ECOComputationModels;getCableDisconnectedModel("
                            + "Lcn/dancingsnow/neoecoae/api/IECOTier;)Lnet/minecraft/resources/ResourceLocation;"), require = 0)
    private ResourceLocation neoecoprototype$disconnected(IECOTier tier, Operation<ResourceLocation> original) {
        var upper = neoecoprototype$upperHostCable(tier, true);
        return upper != null ? upper : original.call(tier);
    }

    @Group(name = "neoecoprototype$disconnectedCable", min = 2, max = 2)
    @WrapOperation(method = "renderFixed(Lcn/dancingsnow/neoecoae/blocks/entity/computation/ECOComputationDriveBlockEntity;"
            + "FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE",
                    target = "Lcn/dancingsnow/neoecoae/api/ECOComputationModels;getCableDisconnectedModel("
                            + "Lcn/dancingsnow/neoecoae/api/IECOTier;Lnet/minecraft/world/level/block/Block;)"
                            + "Lnet/minecraft/resources/ResourceLocation;"), require = 0)
    private ResourceLocation neoecoprototype$disconnectedWithHost(IECOTier tier, Block host, Operation<ResourceLocation> original) {
        var upper = neoecoprototype$upperHostCable(tier, true);
        return upper != null ? upper : original.call(tier, host);
    }

    /**
     * The model this addon wants, or null to let eco answer. A stub whose key is an eco tier is eco's
     * own drawing and stays that way; that happens whenever no accepted cell pushed our tier into the
     * lookup.
     */
    @Unique
    private ResourceLocation neoecoprototype$upperHostCable(IECOTier tier, boolean disconnected) {
        if (!(tier instanceof SimplifyTier)) return null;
        return disconnected ? ComputationCableModels.UPPER_DISCONNECTED : ComputationCableModels.UPPER_CONNECTED;
    }
}
