package com.arcanelens.client.model;

import com.arcanelens.ArcaneLens;
import com.arcanelens.entity.AureliaCompanionEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

/**
 * Adapted from the user's Blockbench Java Entity Model export (Aurelia1.bbmodel / Aurelia1.java under
 * /Modles, rigged with 6 named bones - Legs > RLeg/LLeg, Torso, Arms > LArm/RArm, Head). Field names
 * lowerCamelCased for Java convention (SleepingGodModel's own precedent) - the getChild(...)/
 * addOrReplaceChild(...) string literals and cube geometry are unchanged from the export. Both
 * Aurelia1 and Aurelia2 share this exact geometry (only their textures differ - see
 * AureliaCompanionRenderer), so one rig covers both looks.
 *
 * <p>All four top-level PartPose.offset Y values are changed from the raw export (Legs 24->24, Torso
 * 24->-24, Arms 44->-4, Head 48->-40): Blockbench's exporter appears to pass GROUP pivot Y straight
 * through from its own Y-up viewport without flipping it to Minecraft's PartPose convention (a larger
 * Y offset means further DOWN from the model's own local origin, not further up - see vanilla
 * HumanoidModel's own Head/Body sharing offset Y=0, with their box shapes - not their offsets - doing
 * the actual up/down separation). Each offset was solved in two passes: first from the exported cube
 * box's own local Y span, so neighboring parts' edges actually meet instead of overlapping (which is
 * what the raw export did - every part landing near the same height); then a uniform -24 shift on all
 * four, since the renderer anchors the model's "feet" at local Y=24 (matching vanilla's own leg
 * offset) regardless of what the model itself considers its origin - without that second pass her legs
 * rendered 1.5 blocks into the ground even though the internal proportions were already correct.</p>
 *
 * <p>The four limb leaves (RLeg/LLeg/LArm/RArm) originally had their box geometry positioned so the
 * part's own local origin (where xRot/yRot/zRot all pivot around) landed at the BOTTOM of the leg
 * boxes and partway down the arm boxes, not at the hip/shoulder end - harmless while setupAnim was a
 * no-op, but once real rotation was added it swung the wrong end: the hip/shoulder end whipped away
 * from the torso while the foot/hand end stayed anchored, instead of the normal pendulum-from-the-top
 * swing. Fixed by moving each leaf's PartPose.offset.y to where its box's top edge used to sit and
 * re-zeroing that box's own Y origin to start right at the new pivot - the box's absolute rendered
 * position at rest is unchanged, only where it pivots from moved.</p>
 *
 * <p>Bone-level walk animation reimplements vanilla HumanoidModel's own limb-swing formulas directly
 * against this rig's leaf parts (rLeg/lLeg/rArm/lArm), since this class doesn't extend HumanoidModel
 * itself. Attack swing reads AureliaCompanionEntity#getAttackStartTick() (a synced "when did she last
 * attack" tick) rather than LivingEntity#swing()/getAttackAnim() - that vanilla mechanism turned out
 * not to reliably drive an AI-melee mob's attack animation even after explicitly calling swing() on
 * hit, so this uses the same synced-"when does this end" shape SleepingGodEntity's own
 * DATA_ACTION_END_TICK uses for its real custom swing animation (Broken Vessel currently has no attack
 * animation at all, so it's not a precedent here - see AureliaCompanionEntity's own javadoc). Head
 * yaw/pitch is clamped to a narrower
 * range than vanilla's own defaults specifically because this rig's head box isn't quite pivot-centered
 * (off by 1-2 units in X/Z from the raw Blockbench export) - a full-range turn visibly swings/arcs
 * instead of rotating cleanly in place; clamping the range keeps that arc small enough not to read as
 * broken without touching the box geometry itself (which drives the visible position at every other
 * rotation too, not just yaw/pitch, so isn't a safe thing to retune blind again after the leg/arm pivot
 * saga above).</p>
 *
 * <p>An idle "spam crouch" flourish (torso/head dipping down, legs shifting back) was attempted here
 * and pulled back out - even reduced to a translation-only nudge of about 1/8 of a block, it still
 * produced a dramatic broken pose (torso/head collapsing to leg level) that the tiny magnitude involved
 * doesn't obviously explain, meaning there's a real logic bug in it somewhere that blind numeric tuning
 * across several attempts didn't find. Left out entirely for now rather than ship it broken; worth
 * revisiting with someone actually watching it live rather than guessing at numbers between builds.</p>
 */
public class AureliaModel extends EntityModel<AureliaCompanionEntity>
{
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(ArcaneLens.MODID, "aurelia"), "main");

    private static final int ATTACK_DURATION_TICKS = 6;
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

    public AureliaModel(ModelPart root)
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
    public void setupAnim(AureliaCompanionEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
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

        float ticksSinceAttack = ageInTicks - entity.getAttackStartTick();
        if (ticksSinceAttack >= 0.0F && ticksSinceAttack < ATTACK_DURATION_TICKS)
        {
            float progress = ticksSinceAttack / ATTACK_DURATION_TICKS;
            rArm.xRot -= Mth.sin(progress * (float) Math.PI) * 1.2F;
            rArm.zRot = Mth.sin(progress * (float) Math.PI * 2.0F) * -0.2F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        legs.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        arms.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
