package com.arcanelens.client.renderer;

import com.arcanelens.client.model.AureliaBossModel;
import com.arcanelens.entity.boss.AureliaBossEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Same reasoning/math as the now-deleted AureliaItemInHandLayer (the companion's own, removed along
 * with her held item per the friend's request) - her boss body renders at 0.8x scale (see
 * AureliaBossRenderer.scale()), so ItemInHandLayer's default rendering would shrink the spear by that
 * same 0.8x on top of spear.json's own display transform (tuned to look natural-sized on a normal
 * player), unless corrected. Reimplements renderArmWithItem with a COUNTER_SCALE inserted after the
 * hand position is established but before the item's own geometry draws, exactly as before. */
public class AureliaBossItemInHandLayer extends ItemInHandLayer<AureliaBossEntity, AureliaBossModel>
{
    private static final float BODY_SCALE = 0.8F;
    private static final float COUNTER_SCALE = (1.0F / BODY_SCALE) * 1.1F;

    private final ItemInHandRenderer itemInHandRenderer;

    public AureliaBossItemInHandLayer(RenderLayerParent<AureliaBossEntity, AureliaBossModel> renderer, ItemInHandRenderer itemInHandRenderer)
    {
        super(renderer, itemInHandRenderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    protected void renderArmWithItem(LivingEntity entity, ItemStack stack, ItemDisplayContext transformType, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        if (stack.isEmpty())
        {
            return;
        }

        poseStack.pushPose();
        ((ArmedModel) this.getParentModel()).translateToHand(arm, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean isLeft = arm == HumanoidArm.LEFT;
        // Z = -1.875 confirmed correct by testing, not derived - a hand-worked rotation trace predicted
        // the opposite sign (+Z as "toward the hand") and was empirically wrong, so this was tuned
        // against actual screenshots instead. translateToHand puts us at the arm's pivot (the shoulder,
        // post pivot-fix); this pushes down the arm's length to reach the hand.
        poseStack.translate((isLeft ? -1 : 1) / 16.0F, 0.125F, -1.875F);

        poseStack.scale(COUNTER_SCALE, COUNTER_SCALE, COUNTER_SCALE);
        itemInHandRenderer.renderItem(entity, stack, transformType, isLeft, poseStack, buffer, packedLight);

        poseStack.popPose();
    }
}
