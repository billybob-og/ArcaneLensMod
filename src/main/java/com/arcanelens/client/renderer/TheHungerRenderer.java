package com.arcanelens.client.renderer;

import com.arcanelens.ArcaneLens;
import com.arcanelens.client.model.TheHungerModel;
import com.arcanelens.entity.TheHungerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * TheHungerEntity is a plain Entity (no AI, no living-entity features), so it can't reuse MobRenderer/
 * LivingEntityRenderer - this renders the model directly, mirroring the essential parts of
 * LivingEntityRenderer's own render() (setupAnim -> pick a RenderType -> renderToBuffer) without any of the
 * head-tracking/body-rotation machinery a living entity needs, since this apparition never turns to look
 * at anything - it just faces however it was spawned.
 */
public class TheHungerRenderer extends EntityRenderer<TheHungerEntity>
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ArcaneLens.MODID, "textures/entity/the_hunger.png");

    private final TheHungerModel<TheHungerEntity> model;

    public TheHungerRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        this.model = new TheHungerModel<>(context.bakeLayer(TheHungerModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(TheHungerEntity entity)
    {
        return TEXTURE;
    }

    @Override
    public void render(TheHungerEntity entity, float yRot, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entity.getYRot()));
        // LivingEntityRenderer applies this same (-1,-1,1) mirror automatically for every living entity -
        // this renderer doesn't extend it (see the class javadoc for why), so it has to be done by hand here.
        // The model's own rotation values were transcribed assuming this mirror is present (see TheHungerModel's
        // javadoc) - without it, the whole body renders upside-down and every limb rotation reads backwards,
        // which is exactly what testing showed once the permanent Hunger Idol made the pose easy to scrutinize
        // (the brief 3-second apparition had the same bug all along, just easy to miss at a glance).
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        // Capped well past the walk animation's own ~6.67-tick length (it's non-looping, so anything beyond
        // that just holds the last keyframe) - entity.tickCount alone grows unbounded for a permanent entity
        // (the Hunger Idol atop the tower, unlike the brief 60-tick apparition), and feeding a very large
        // value into the animation system's per-channel time math produces a garbled pose (seen in testing:
        // upside-down body, broken arm angles) rather than cleanly holding the final frame.
        float ageInTicks = Math.min(entity.tickCount + partialTick, 40.0F);
        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, yRot, partialTick, poseStack, bufferSource, packedLight);
    }
}
