package com.arcanelens.client.model;

import com.arcanelens.ArcaneLens;
import com.arcanelens.entity.TheHungerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Hand-derived from the user's Blockbench export (Dirf_Dog_Boss.bbmodel / Dirf_Dog_Boss.java under /Modles) -
 * a model originally made for something else, repurposed for The Hunger's brief apparition. Unlike
 * SleepingGodModel's source, this export already uses valid Java identifiers, so no bone renaming was
 * needed. WALKING below is transcribed from the .bbmodel's own "Walking" keyframe animation (Blockbench
 * playback mode "once", not "loop") - it plays through once starting when the entity appears and then holds
 * its final frame for the rest of the apparition's brief lifetime, rather than cycling continuously; that's
 * just how a non-looping AnimationDefinition already behaves (channel sampling clamps to the last keyframe
 * past the animation's length), so no special hold-logic is needed beyond not marking it .looping(). As with
 * SleepingGodModel, rotation X/Y values are NEGATED relative to the raw .bbmodel numbers (vanilla's
 * (-1,-1,1) mirror scale on every rendered entity reverses the visual sense of X/Y rotation but not Z);
 * position values are used as authored.
 */
public class TheHungerModel<T extends TheHungerEntity> extends HierarchicalModel<T>
{
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(ArcaneLens.MODID, "the_hunger"), "main");

    public static final AnimationDefinition WALKING = AnimationDefinition.Builder.withLength(0.33333F)
            .addAnimation("L_Leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(9.5356665518F, 0.5193668308F, -2.5454991025F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.04167F, KeyframeAnimations.degreeVec(29.5356665518F, 0.5193668308F, -2.5454991025F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.08333F, KeyframeAnimations.degreeVec(14.5356665518F, 0.5193668308F, -2.5454991025F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(-0.4643334482F, 0.5193668308F, -2.5454991025F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.degreeVec(-12.9643334482F, 0.5193668308F, -2.5454991025F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.20833F, KeyframeAnimations.degreeVec(-17.9643334482F, 0.5193668308F, -2.5454991025F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-7.9643334482F, 0.5193668308F, -2.5454991025F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.degreeVec(-0.4643334482F, 0.5193668308F, -2.5454991025F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.33333F, KeyframeAnimations.degreeVec(12.0356665518F, 0.5193668308F, -2.5454991025F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("R_Leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-7.8392025119F, 0.6323749809F, -6.5790095237F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.04167F, KeyframeAnimations.degreeVec(-22.8392025119F, 0.6323749809F, -6.5790095237F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.08333F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.20833F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.degreeVec(-12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.33333F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("L_Arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.04167F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.08333F, KeyframeAnimations.degreeVec(45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("L_Arm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.04167F, KeyframeAnimations.posVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.08333F, KeyframeAnimations.posVec(0.0F, 1.0F, -2.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("R_Arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.04167F, KeyframeAnimations.degreeVec(42.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.08333F, KeyframeAnimations.degreeVec(52.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("R_Arm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.04167F, KeyframeAnimations.posVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.08333F, KeyframeAnimations.posVec(0.0F, 1.0F, -2.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("HeadEye", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.04167F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.08333F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.20833F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.33333F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("R_Arm2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("R_Arm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(2.0664350748F, -0.4222804063F, 1.3346978614F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(19.5664350748F, -0.4222804063F, 1.3346978614F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("L_Arm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("L_arm2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart lLeg;
    private final ModelPart rLeg;
    private final ModelPart lArm;
    private final ModelPart lArm1;
    private final ModelPart lArm2;
    private final ModelPart rArm;
    private final ModelPart rArm1;
    private final ModelPart rArm2;
    private final ModelPart headEye;

    public TheHungerModel(ModelPart root)
    {
        this.root = root;
        this.body = root.getChild("Body");
        this.lLeg = this.body.getChild("L_Leg");
        this.rLeg = this.body.getChild("R_Leg");
        this.lArm = this.body.getChild("L_Arm");
        this.lArm1 = this.lArm.getChild("L_Arm1");
        this.lArm2 = this.lArm.getChild("L_arm2");
        this.rArm = this.body.getChild("R_Arm");
        this.rArm1 = this.rArm.getChild("R_Arm1");
        this.rArm2 = this.rArm.getChild("R_Arm2");
        this.headEye = this.body.getChild("HeadEye");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Body's own Y offset was originally 18.0F (as transcribed straight from the raw .bbmodel export),
        // but that puts the legs' own bottom edge (Body's 18 + the leg boxes' own 6-unit downward extent =
        // 24 units, 1.5 blocks) well below the entity's actual Y=0 "feet" reference - invisible on the brief
        // apparition (an extra 1.5-block sink into the ground during a random 3-second encounter is easy to
        // miss), but obvious once the permanent Hunger Idol was carefully placed on a known floor and its
        // legs turned up embedded below it. Shifted up by that same 24 units so the legs' bottom now lands
        // exactly at Y=0, standing properly rather than sinking in.
        PartDefinition body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -6.0F, 3.0F, 0.0F, -0.0436F, 0.0F));

        body.addOrReplaceChild("Body_r1", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-5.0F, -9.0F, -7.0F, 10.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -6.0F, 0.0F, 3.1416F, 0.0F));

        body.addOrReplaceChild("L_Leg", CubeListBuilder.create().texOffs(8, 17)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, 0.0F, -3.0F, 0.0F, -0.0436F, 0.0F));

        body.addOrReplaceChild("R_Leg", CubeListBuilder.create().texOffs(0, 17)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.0F, 0.0F, -3.0F, 0.0F, -0.0436F, 0.0F));

        PartDefinition lArm = body.addOrReplaceChild("L_Arm", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-6.0F, -4.0F, -3.0F, 0.0F, -0.0436F, 0.0F));

        PartDefinition lArm1 = lArm.addOrReplaceChild("L_Arm1", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));

        lArm1.addOrReplaceChild("L_Arm1_r1", CubeListBuilder.create().texOffs(16, 17)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition lArm2 = lArm.addOrReplaceChild("L_arm2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        lArm2.addOrReplaceChild("L_Arm2_r1", CubeListBuilder.create().texOffs(24, 23)
                        .addBox(-1.0F, -0.3954F, -0.9176F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6109F, 0.0F, 0.0F));

        PartDefinition rArm = body.addOrReplaceChild("R_Arm", CubeListBuilder.create(),
                PartPose.offsetAndRotation(6.0F, -4.0F, -3.0F, 0.0F, -0.0436F, 0.0F));

        PartDefinition rArm1 = rArm.addOrReplaceChild("R_Arm1", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));

        rArm1.addOrReplaceChild("R_Arm1_r1", CubeListBuilder.create().texOffs(16, 23)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0436F, 0.0F));

        PartDefinition rArm2 = rArm.addOrReplaceChild("R_Arm2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        rArm2.addOrReplaceChild("R_Arm2_r1", CubeListBuilder.create().texOffs(24, 17)
                        .addBox(-0.9128F, -0.3965F, -0.916F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6109F, 0.0436F, 0.0F));

        PartDefinition headEye = body.addOrReplaceChild("HeadEye", CubeListBuilder.create(), PartPose.offset(0.0F, -9.0F, -3.0F));

        headEye.addOrReplaceChild("Eye_r1", CubeListBuilder.create().texOffs(36, 0)
                        .addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition teeth = body.addOrReplaceChild("Teeth", CubeListBuilder.create(), PartPose.offset(0.5F, -6.0F, -6.0F));

        PartDefinition toothSet2 = teeth.addOrReplaceChild("Tooth_Set2", CubeListBuilder.create(), PartPose.offset(-4.5F, 5.0F, -1.0F));

        toothSet2.addOrReplaceChild("Tooth4_r1", CubeListBuilder.create().texOffs(32, 17)
                        .addBox(-0.5F, -0.567F, -1.75F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.5F, -0.5F, 0.0F, -0.5236F, 0.0F, 0.0F));

        toothSet2.addOrReplaceChild("Tooth5_r1", CubeListBuilder.create().texOffs(24, 29)
                        .addBox(-0.5F, -0.7314F, -1.5783F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.5F, -0.5F, 0.0F, -1.0036F, 0.0F, 0.0F));

        toothSet2.addOrReplaceChild("Tooth6_r1", CubeListBuilder.create().texOffs(16, 29)
                        .addBox(0.0F, -1.0F, -2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.0036F, 0.0F, 0.0F));

        PartDefinition toothSet1 = teeth.addOrReplaceChild("Tooth_Set_1", CubeListBuilder.create(), PartPose.offset(-3.0F, 0.0F, 0.0F));

        toothSet1.addOrReplaceChild("Tooth3_r1", CubeListBuilder.create().texOffs(0, 29)
                        .addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        toothSet1.addOrReplaceChild("Tooth1_r1", CubeListBuilder.create().texOffs(8, 25)
                        .addBox(-0.5F, -0.5468F, -2.2113F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.0F, -0.5F, -1.0F, 0.4363F, 0.0F, 0.0F));

        toothSet1.addOrReplaceChild("Tooth2_r1", CubeListBuilder.create().texOffs(8, 29)
                        .addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public ModelPart root()
    {
        return root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animate(entity.walkAnimationState, WALKING, ageInTicks);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
