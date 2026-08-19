package com.arcanelens.client.renderer;

import com.arcanelens.client.model.AureliaModel;
import com.arcanelens.entity.AureliaCompanionEntity;
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

/** Aurelia's whole body renders at 0.65x scale (see AureliaCompanionRenderer.scale()) - since
 * ItemInHandLayer draws using the same already-scaled PoseStack the body model used, her held spear
 * would otherwise ALSO get shrunk by that 0.65x on top of whatever size spear.json's own display
 * transform already specifies (which is meant to look natural-sized on a normal player, per the
 * reference the user provided from Blockbench's own Display preview) - two compounding scale-downs
 * where only the body's was intended.
 *
 * <p>This can't be fixed by just wrapping the whole render() call in an extra
 * pushPose/scale/popPose, because that would ALSO scale the hand-position math (translateToHand plus
 * the fixed rotation/translate vanilla applies to orient the item), undoing the positioning already
 * tuned for her. So this reimplements renderArmWithItem - a faithful copy of vanilla's own version,
 * verified via javap against the actual Forge jar - with COUNTER_SCALE inserted at the one point that
 * matters: after the hand position is established, but before the item's own geometry is drawn from
 * it. COUNTER_SCALE is set a little above the exact 1/0.65 cancellation point, per the user's own call
 * that the spear should look a bit bigger on her specifically, not just "not shrunk."</p>
 */
public class AureliaItemInHandLayer extends ItemInHandLayer<AureliaCompanionEntity, AureliaModel<AureliaCompanionEntity>>
{
    private static final float BODY_SCALE = 0.65F;
    private static final float COUNTER_SCALE = (1.0F / BODY_SCALE) * 1.1F;

    private final ItemInHandRenderer itemInHandRenderer;

    public AureliaItemInHandLayer(RenderLayerParent<AureliaCompanionEntity, AureliaModel<AureliaCompanionEntity>> renderer, ItemInHandRenderer itemInHandRenderer)
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
        poseStack.translate((isLeft ? -1 : 1) / 16.0F, 0.125F, -0.625F);

        poseStack.scale(COUNTER_SCALE, COUNTER_SCALE, COUNTER_SCALE);
        itemInHandRenderer.renderItem(entity, stack, transformType, isLeft, poseStack, buffer, packedLight);

        poseStack.popPose();
    }
}
