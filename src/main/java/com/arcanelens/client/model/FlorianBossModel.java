package com.arcanelens.client.model;

import com.arcanelens.ArcaneLens;
import com.arcanelens.entity.boss.FlorianFighter;
import com.arcanelens.entity.boss.ai.FlorianChargeGoal;
import com.arcanelens.entity.boss.ai.FlorianGoreGoal;
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
import net.minecraft.world.entity.Mob;

/** Florian's stag rig (Modles/florianBoss.bbmodel). Each leg is hip -> knee: the hip bone holds the thigh
 * cube and pivots under the torso, the knee bone is its child holding the shin cube - so the shin follows
 * the thigh's swing and can bend on top of it. (The .bbmodel's own outliner nests these the other way
 * round; the pivots and cubes are what this was built from, not that nesting.)
 *
 * <p>Walking is fully procedural, like AureliaBossModel's: diagonal leg pairs swing in opposite phase, and
 * the knee only ever flexes forward-folding (max(0, sin)) so it bends while the leg lifts and stays
 * straight while it pushes off the ground. Tune the three constants below to taste.</p> */
public class FlorianBossModel<T extends Mob & FlorianFighter> extends EntityModel<T>
{
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(ArcaneLens.MODID, "florian_boss"), "main");

    private static final float MAX_HEAD_YAW_DEGREES = 50.0F;
    private static final float MAX_HEAD_PITCH_DEGREES = 40.0F;
    private static final float DEG_TO_RAD = (float) Math.PI / 180F;

    private static final float WALK_SPEED = 0.5F;
    private static final float HIP_SWING = 0.9F;
    private static final float KNEE_FLEX = 1.1F;
    private static final float GORE_HEAD_DOWN = 0.8F;
    private static final float CHARGE_HEAD_DOWN = 0.6F;

    private final ModelPart legs;
    private final ModelPart head;
    private final ModelPart torso;
    private final ModelPart skull;
    private final ModelPart frontLeftHip;
    private final ModelPart frontLeftKnee;
    private final ModelPart frontRightHip;
    private final ModelPart frontRightKnee;
    private final ModelPart backLeftHip;
    private final ModelPart backLeftKnee;
    private final ModelPart backRightHip;
    private final ModelPart backRightKnee;

    public FlorianBossModel(ModelPart root)
    {
        this.legs = root.getChild("Legs");
        this.head = root.getChild("Head");
        this.torso = root.getChild("Torso");
        this.skull = this.head.getChild("head2");

        ModelPart frontLegs = this.legs.getChild("FrontLegs");
        this.frontLeftHip = frontLegs.getChild("LLeg").getChild("FrontLeftHip");
        this.frontLeftKnee = this.frontLeftHip.getChild("FrontLeftKnee");
        this.frontRightHip = frontLegs.getChild("RLeg").getChild("FrontRightHip");
        this.frontRightKnee = this.frontRightHip.getChild("FrontRightKnee");

        ModelPart backLegs = this.legs.getChild("BackLegs");
        this.backLeftHip = backLegs.getChild("LLeg2").getChild("BackLeftHip");
        this.backLeftKnee = this.backLeftHip.getChild("BackLeftKnee");
        this.backRightHip = backLegs.getChild("RLeg2").getChild("BackRightHip");
        this.backRightKnee = this.backRightHip.getChild("BackRightKnee");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();

        PartDefinition legs = root.addOrReplaceChild("Legs", CubeListBuilder.create(), PartPose.offset(5.0F, 17.0F, 11.0F));

        PartDefinition frontLegs = legs.addOrReplaceChild("FrontLegs", CubeListBuilder.create(), PartPose.offset(-5.0F, 7.0F, -11.0F));

        PartDefinition lLeg = frontLegs.addOrReplaceChild("LLeg", CubeListBuilder.create(), PartPose.offset(5.0F, 0.0F, -11.0F));
        PartDefinition frontLeftHip = lLeg.addOrReplaceChild("FrontLeftHip", CubeListBuilder.create().texOffs(64, 54).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, -14.0F, 0.5F));
        frontLeftHip.addOrReplaceChild("FrontLeftKnee", CubeListBuilder.create().texOffs(16, 50).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.0F, 0.0F));

        PartDefinition rLeg = frontLegs.addOrReplaceChild("RLeg", CubeListBuilder.create(), PartPose.offset(5.0F, -7.0F, -11.0F));
        PartDefinition frontRightHip = rLeg.addOrReplaceChild("FrontRightHip", CubeListBuilder.create().texOffs(64, 44).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.5F, -7.0F, 0.5F));
        frontRightHip.addOrReplaceChild("FrontRightKnee", CubeListBuilder.create().texOffs(28, 57).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.0F, 0.0F));

        PartDefinition backLegs = legs.addOrReplaceChild("BackLegs", CubeListBuilder.create(), PartPose.offset(-11.0F, 7.0F, 0.0F));

        PartDefinition lLeg2 = backLegs.addOrReplaceChild("LLeg2", CubeListBuilder.create(), PartPose.offset(6.0F, 0.0F, -11.0F));
        PartDefinition backLeftHip = lLeg2.addOrReplaceChild("BackLeftHip", CubeListBuilder.create().texOffs(16, 60).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(5.5F, -14.0F, 11.5F));
        backLeftHip.addOrReplaceChild("BackLeftKnee", CubeListBuilder.create().texOffs(52, 57).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.0F, 0.0F));

        PartDefinition rLeg2 = backLegs.addOrReplaceChild("RLeg2", CubeListBuilder.create(), PartPose.offset(11.0F, -7.0F, 0.0F));
        PartDefinition backRightHip = rLeg2.addOrReplaceChild("BackRightHip", CubeListBuilder.create().texOffs(64, 34).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.5F, -7.0F, 0.5F));
        backRightHip.addOrReplaceChild("BackRightKnee", CubeListBuilder.create().texOffs(40, 57).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.0F, 0.0F));

        PartDefinition head = root.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        head.addOrReplaceChild("Neck_r1", CubeListBuilder.create().texOffs(34, 34).addBox(-3.0F, -5.0F, -14.0F, 6.0F, 5.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -18.0F, -11.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition skull = head.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(0, 34).addBox(-3.0F, -5.0F, -7.5F, 6.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(34, 48).addBox(-3.0F, -7.0F, -3.5F, 6.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -32.0F, -8.5F));

        // Nested under the skull (not the whole Head, like the raw export) so the antlers turn with it.
        PartDefinition antlers = skull.addOrReplaceChild("Antlers", CubeListBuilder.create(), PartPose.offset(2.0F, -7.0F, 2.5F));

        PartDefinition lAntler = antlers.addOrReplaceChild("Lantler", CubeListBuilder.create().texOffs(0, 50).addBox(-5.0F, -13.0F, -1.0F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(64, 64).addBox(-8.0F, -13.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        lAntler.addOrReplaceChild("Antlerpt2_r1", CubeListBuilder.create().texOffs(8, 65).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -3.0F, -0.25F, -0.0693F, -0.0531F, -0.6527F));

        PartDefinition rAntler = antlers.addOrReplaceChild("RAntler", CubeListBuilder.create().texOffs(0, 65).addBox(6.0F, -10.0F, -0.75F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 50).addBox(3.0F, -10.0F, -0.75F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -3.0F, -0.25F));
        rAntler.addOrReplaceChild("Antlerpt3_r1", CubeListBuilder.create().texOffs(28, 67).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, -0.0618F, 0.0617F, 0.7835F));

        root.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -23.0F, -12.0F, 14.0F, 9.0F, 25.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        skull.yRot = Mth.clamp(netHeadYaw, -MAX_HEAD_YAW_DEGREES, MAX_HEAD_YAW_DEGREES) * DEG_TO_RAD;
        skull.xRot = Mth.clamp(headPitch, -MAX_HEAD_PITCH_DEGREES, MAX_HEAD_PITCH_DEGREES) * DEG_TO_RAD;

        // Head-down antler poses layered on the tracking above. The gore action ends right as the hit
        // lands, so the head snapping back up on its own reads as the toss.
        FlorianFighter.Action action = entity.getFlorianAction();
        if (action == FlorianFighter.Action.GORE)
        {
            float remaining = entity.getActionEndTick() - ageInTicks;
            float linear = 1.0F - Mth.clamp(remaining / FlorianGoreGoal.WINDUP_TICKS, 0.0F, 1.0F);
            skull.xRot += GORE_HEAD_DOWN * smoothstep(linear);
        }
        else if (action == FlorianFighter.Action.CHARGE)
        {
            float remaining = entity.getActionEndTick() - ageInTicks;
            float total = FlorianChargeGoal.WINDUP_TICKS + FlorianChargeGoal.CHARGE_DURATION_TICKS;
            float elapsed = total - remaining;
            skull.xRot += CHARGE_HEAD_DOWN * smoothstep(Mth.clamp(elapsed / FlorianChargeGoal.WINDUP_TICKS, 0.0F, 1.0F));
        }

        float phase = limbSwing * WALK_SPEED;
        animateLeg(frontLeftHip, frontLeftKnee, phase, limbSwingAmount);
        animateLeg(backRightHip, backRightKnee, phase, limbSwingAmount);
        animateLeg(frontRightHip, frontRightKnee, phase + (float) Math.PI, limbSwingAmount);
        animateLeg(backLeftHip, backLeftKnee, phase + (float) Math.PI, limbSwingAmount);
    }

    private static float smoothstep(float linear)
    {
        return linear * linear * (3.0F - 2.0F * linear);
    }

    /** Positive xRot swings a foot toward the tail, so cos() runs the hip through its stride and sin() > 0
     * is exactly the swing-forward half of it - the half where the knee should fold. */
    private static void animateLeg(ModelPart hip, ModelPart knee, float phase, float amount)
    {
        hip.xRot = Mth.cos(phase) * HIP_SWING * amount;
        knee.xRot = Math.max(0.0F, Mth.sin(phase)) * KNEE_FLEX * amount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        legs.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
