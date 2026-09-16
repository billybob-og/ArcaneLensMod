package com.arcanelens.client.model;

import com.arcanelens.ArcaneLens;
import com.arcanelens.entity.boss.AureliaBossEntity;
import com.arcanelens.entity.boss.ai.AureliaChargeGoal;
import com.arcanelens.entity.boss.ai.AureliaSpearThrustGoal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/** Same rig/geometry as AureliaModel (the companion's rig - see that class's javadoc for the pivot-fix
 * history), copied rather than shared because the boss needs entirely different animation logic (real
 * combat moves, not a simple punch) and the two entities aren't related by inheritance. Walk cycle and
 * head-tracking are reused verbatim (the companion's own already reads correctly); THRUST and CHARGE
 * are new, boss-specific poses layered on top - see AureliaBossEntity.Action.
 *
 * <p>Implements ArmedModel (unlike the companion's own AureliaModel, which dropped it along with her
 * held item per the friend's request) so AureliaBossItemInHandLayer can find the right hand to render
 * her spear at - she's meant to visibly carry it into the fight, matching the spear-thrust/charge
 * moveset.</p> */
public class AureliaBossModel extends EntityModel<AureliaBossEntity> implements ArmedModel
{
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(ArcaneLens.MODID, "aurelia_boss"), "main");

    private static final float MAX_HEAD_YAW_DEGREES = 50.0F;
    private static final float MAX_HEAD_PITCH_DEGREES = 40.0F;

    private final ModelPart legs;
    private final ModelPart rLeg;
    private final ModelPart lLeg;
    private final ModelPart torso;
    private final ModelPart arms;
    private final ModelPart lArm;
    private final ModelPart rArm;
    private final ModelPart head;

    public AureliaBossModel(ModelPart root)
    {
        this.legs = root.getChild("Legs");
        this.rLeg = this.legs.getChild("RLeg");
        this.lLeg = this.legs.getChild("LLeg");
        this.torso = root.getChild("Torso");
        this.arms = root.getChild("Arms");
        this.lArm = this.arms.getChild("LArm");
        this.rArm = this.arms.getChild("RArm");
        this.head = root.getChild("Head");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition legs = partdefinition.addOrReplaceChild("Legs", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, -1.0F));

        legs.addOrReplaceChild("RLeg", CubeListBuilder.create().texOffs(0, 64).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -24.0F, -1.0F));

        legs.addOrReplaceChild("LLeg", CubeListBuilder.create().texOffs(48, 32).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -24.0F, 0.0F));

        partdefinition.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, 0.0F, -4.0F, 16.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -24.0F, -1.0F));

        PartDefinition arms = partdefinition.addOrReplaceChild("Arms", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, -1.0F));

        arms.addOrReplaceChild("LArm", CubeListBuilder.create().texOffs(64, 0).addBox(0.0F, 0.0F, -4.0F, 6.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -20.0F, 0.0F));

        arms.addOrReplaceChild("RArm", CubeListBuilder.create().texOffs(32, 64).addBox(-6.0F, 0.0F, -4.0F, 6.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, -20.0F, 0.0F));

        partdefinition.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, 0.0F, -10.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -40.0F, 1.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(AureliaBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        float clampedYaw = Mth.clamp(netHeadYaw, -MAX_HEAD_YAW_DEGREES, MAX_HEAD_YAW_DEGREES);
        float clampedPitch = Mth.clamp(headPitch, -MAX_HEAD_PITCH_DEGREES, MAX_HEAD_PITCH_DEGREES);
        head.yRot = clampedYaw * ((float) Math.PI / 180F);
        head.xRot = clampedPitch * ((float) Math.PI / 180F);

        rLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        lLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        rArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.0F * limbSwingAmount;
        lArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.0F * limbSwingAmount;
        rArm.zRot = 0.0F;
        lArm.zRot = 0.0F;
        torso.xRot = 0.0F;
        torso.yRot = 0.0F;

        AureliaBossEntity.Action action = entity.getAction();
        if (action == AureliaBossEntity.Action.THRUST)
        {
            // Draws the spear arm back through the windup, releasing into a forward thrust right as the
            // hit actually lands (the goal applies damage at windupTicksRemaining == 0, which is exactly
            // when this action ends - see AureliaSpearThrustGoal). Smoothstep-eased rather than a
            // straight lerp so the motion accelerates into the release instead of moving at a flat,
            // mechanical rate the whole way through.
            float remaining = entity.getActionEndTick() - ageInTicks;
            float linear = 1.0F - Mth.clamp(remaining / AureliaSpearThrustGoal.WINDUP_TICKS, 0.0F, 1.0F);
            float eased = smoothstep(linear);
            rArm.xRot -= Mth.lerp(eased, -0.6F, 0.9F);
            torso.yRot = 0.15F * (1.0F - eased);
        }
        else if (action == AureliaBossEntity.Action.CHARGE)
        {
            // Eases into the lean-forward sprint pose over the windup instead of snapping straight to
            // it, then holds it through the dash itself (a straight-line velocity burst, not something
            // worth choreographing frame-by-frame on top of yet).
            float remaining = entity.getActionEndTick() - ageInTicks;
            float totalDuration = AureliaChargeGoal.WINDUP_TICKS + AureliaChargeGoal.CHARGE_DURATION_TICKS;
            float elapsed = totalDuration - remaining;
            float leanIn = smoothstep(Mth.clamp(elapsed / AureliaChargeGoal.WINDUP_TICKS, 0.0F, 1.0F));
            torso.xRot = 0.3F * leanIn;
            rArm.xRot -= 0.3F * leanIn;
            lArm.xRot -= 0.3F * leanIn;
        }
    }

    /** Ease-in/ease-out S-curve (3t^2-2t^3) for a linear 0..1 progress value - accelerates out of the
     * start pose and decelerates into the end pose instead of moving at a flat, mechanical rate the
     * whole way through, which is most of what makes an interpolated pose read as "flowey" rather than
     * robotic without needing hand-keyframed in-between poses. */
    private static float smoothstep(float linear)
    {
        return linear * linear * (3.0F - 2.0F * linear);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        legs.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        arms.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack)
    {
        (arm == HumanoidArm.LEFT ? lArm : rArm).translateAndRotate(poseStack);
    }
}
