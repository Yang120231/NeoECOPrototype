package cn.dancingsnow.neoecoprototype.client.renderer.blockentity;

import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationCoolingControllerBlockEntity;
import cn.dancingsnow.neoecoae.client.rendering.IFixedBlockEntityRenderer;
import cn.dancingsnow.neoecoprototype.client.model.SimplifyCoolingControllerBakedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Hands our formed cooling controller to {@link SimplifyCoolingControllerBakedModel}, inside the pass where
 * eco's {@code FixedModelLighting} is the buffer source - the only way this member gets the same lighting
 * treatment eco gave its own three tiers.
 *
 * <p>Only the fixed entry point is implemented: the block is not registered as an ordinary block entity
 * renderer, because the chunk mesh would then draw the suppressed quads too and the two would fight.
 */
public final class SimplifyCoolingControllerRenderer
        implements IFixedBlockEntityRenderer<ECOComputationCoolingControllerBlockEntity> {

    @Override
    public void renderFixed(ECOComputationCoolingControllerBlockEntity blockEntity, float partialTick,
                            PoseStack poseStack, MultiBufferSource bufferSource,
                            int packedLight, int packedOverlay) {
        var state = blockEntity.getBlockState();
        if (!state.getValue(NEBlock.FORMED)) {
            return;
        }
        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        if (model instanceof SimplifyCoolingControllerBakedModel fixed) {
            fixed.renderFixed(state, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }
}
