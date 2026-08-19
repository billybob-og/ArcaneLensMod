package com.arcanelens.client.model;

import com.arcanelens.ArcaneLens;
import com.arcanelens.entity.SleepingGodEntity;
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
 * Hand-cleaned from the user's Blockbench "Java Block/Item" export (TheOneWhoSleeps.bbmodel /
 * TheOneWhoSleeps.java under /Modles) - the raw export used bone names containing spaces (e.g. "Left Arm1"),
 * which aren't valid Java identifiers, so those were renamed (spaces stripped) throughout. The mesh/cube
 * data itself is unchanged from the export. MALE_SWING/SPELL_CAST_1/SPELL_CAST_2/FLY_START below are
 * transcribed directly from the .bbmodel's keyframe data (rotation/position per bone per time, in seconds)
 * via Mojang's KeyframeAnimations system. Rotation X/Y values are NEGATED relative to the raw .bbmodel
 * numbers - vanilla's rendering pipeline applies a (-1,-1,1) mirror scale to every entity (see
 * LivingEntityRenderer's render/setupRotations), which reverses the visual sense of rotations around X and Y
 * (only one of their two in-plane axes gets flipped) but NOT around Z (both in-plane axes flipped, so the
 * reversal cancels out) - so only X and Y need negating; Z is used as authored.
 */
public class SleepingGodModel<T extends SleepingGodEntity> extends HierarchicalModel<T>
{
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(ArcaneLens.MODID, "sleeping_god"), "main");

    public static final AnimationDefinition FLY_START = AnimationDefinition.Builder.withLength(0.29167F)
            .addAnimation("Head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("Body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("Body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightLeg1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftArm2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftArm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftArm1", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.5F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.posVec(0.0F, 0.0F, 1.5F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftLeg1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftLeg1", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.posVec(0.0F, 1.0F, -1.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftLeg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftLeg2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.posVec(0.0F, 0.2414814566F, 0.0647047613F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightLeg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightLeg2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.posVec(0.0F, 0.25F, -0.75F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightArm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightArm2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightArm2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.5F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.29167F, KeyframeAnimations.posVec(0.0F, 0.0F, 1.5F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition SPELL_CAST_1 = AnimationDefinition.Builder.withLength(0.5F)
            .addAnimation("Head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("Body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftArm2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftArm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.degreeVec(-92.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftArm1", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.posVec(0.0F, 6.1513216762F, -5.7668105838F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftLeg1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.degreeVec(30.0F, 0.0F, -7.5F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftLeg1", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.posVec(-0.6655427348F, 0.6906742167F, 1.6096090498F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftLeg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.degreeVec(15.0F, 0.0F, -7.5F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightLeg1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 12.8571F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightLeg1", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightLeg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.degreeVec(37.5F, 0.0F, 17.5F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightLeg2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.posVec(1.378201642F, 0.6171671205F, 2.1954681096F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightLeg2", new AnimationChannel(AnimationChannel.Targets.SCALE,
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightArm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightArm1", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightArm2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightArm2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.16667F, KeyframeAnimations.posVec(0.0F, 6.1031358853F, -5.8353300323F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition SPELL_CAST_2 = AnimationDefinition.Builder.withLength(0.91667F)
            .addAnimation("Head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(-37.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("Head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -0.7933533403F, -0.608761429F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftArm2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -127.5F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -100.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.66667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -127.5F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftArm2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.25F, KeyframeAnimations.posVec(1.7712985919F, -2.6196376273F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.66667F, KeyframeAnimations.posVec(1.7712985919F, -2.6196376273F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftArm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -130.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -100.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.66667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -130.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftArm1", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.25F, KeyframeAnimations.posVec(-2.0251162198F, 6.9209034306F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.posVec(-3.3901441728F, 5.015096592F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.66667F, KeyframeAnimations.posVec(-2.0251162198F, 6.9209034306F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftLeg1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftLeg1", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 1.6118988249F, 2.5301743374F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 1.9635533906F, -4.1875144214F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftLeg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightLeg1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightLeg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightLeg2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 1.4450277708F, -3.890432674F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 2.2187351978F, 4.5925906196F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightArm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 135.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 105.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.66667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 135.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightArm1", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.25F, KeyframeAnimations.posVec(-2.8284271248F, -2.8284271248F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.66667F, KeyframeAnimations.posVec(-2.8284271248F, -2.8284271248F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightArm2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 135.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 105.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.66667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 135.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("RightArm2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.25F, KeyframeAnimations.posVec(1.4142135624F, 7.071067812F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.posVec(2.9415116139F, 4.7532593726F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.66667F, KeyframeAnimations.posVec(1.4142135624F, 7.071067812F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition MALE_SWING = AnimationDefinition.Builder.withLength(0.75F)
            .addAnimation("Head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-17.5F, -10.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.625F, KeyframeAnimations.degreeVec(-2.5F, -10.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("Body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 10.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.625F, KeyframeAnimations.degreeVec(0.0F, -10.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftArm2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-81.1277F, 32.1883F, 4.7535F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.625F, KeyframeAnimations.degreeVec(-63.856F, -29.7174F, -4.3097F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftArm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.degreeVec(-81.1277F, 32.1883F, 4.7535F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.625F, KeyframeAnimations.degreeVec(-62.6509F, -22.8253F, -1.3059F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("LeftArm1", new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.5F, KeyframeAnimations.posVec(3.3361235006F, 5.3413063092F, -5.3965344171F), AnimationChannel.Interpolations.LINEAR),
                new Keyframe(0.625F, KeyframeAnimations.posVec(-2.691016133F, 3.676082707F, -4.2951179687F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    private final ModelPart root;
    private final ModelPart waist;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm2;
    private final ModelPart leftArm1;
    private final ModelPart leftLeg1;
    private final ModelPart leftLeg2;
    private final ModelPart rightLeg1;
    private final ModelPart rightLeg2;
    private final ModelPart rightArm1;
    private final ModelPart rightArm2;

    public SleepingGodModel(ModelPart root)
    {
        this.root = root;
        this.waist = root.getChild("Waist");
        this.head = this.waist.getChild("Head");
        this.body = this.waist.getChild("Body");
        this.leftArm2 = root.getChild("LeftArm2");
        this.leftArm1 = root.getChild("LeftArm1");
        this.leftLeg1 = root.getChild("LeftLeg1");
        this.leftLeg2 = root.getChild("LeftLeg2");
        this.rightLeg1 = root.getChild("RightLeg1");
        this.rightLeg2 = root.getChild("RightLeg2");
        this.rightArm1 = root.getChild("RightArm1");
        this.rightArm2 = root.getChild("RightArm2");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition waist = partdefinition.addOrReplaceChild("Waist", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));

        waist.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -12.0F, 0.0F));

        waist.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -12.0F, 0.0F));

        PartDefinition leftArm2 = partdefinition.addOrReplaceChild("LeftArm2", CubeListBuilder.create(), PartPose.offset(6.0F, 0.0F, 0.0F));

        leftArm2.addOrReplaceChild("LeftArm2_r1", CubeListBuilder.create().texOffs(32, 48).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        partdefinition.addOrReplaceChild("LeftArm1", CubeListBuilder.create().texOffs(32, 48).addBox(-2.5F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(6.5F, 6.0F, 0.0F));

        partdefinition.addOrReplaceChild("LeftLeg1", CubeListBuilder.create().texOffs(16, 48).addBox(-2.1F, -0.25F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.25F, 18.25F, 0.0F));

        PartDefinition leftLeg2 = partdefinition.addOrReplaceChild("LeftLeg2", CubeListBuilder.create(), PartPose.offset(2.25F, 12.0F, 0.0F));

        leftLeg2.addOrReplaceChild("LeftLeg2_r1", CubeListBuilder.create().texOffs(16, 48).addBox(-1.85F, -6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        partdefinition.addOrReplaceChild("RightLeg1", CubeListBuilder.create().texOffs(0, 16).addBox(-1.9F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition rightLeg2 = partdefinition.addOrReplaceChild("RightLeg2", CubeListBuilder.create(), PartPose.offset(-1.75F, 18.0F, 0.0F));

        rightLeg2.addOrReplaceChild("RightLeg2_r1", CubeListBuilder.create().texOffs(0, 16).addBox(-1.9F, -6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        partdefinition.addOrReplaceChild("RightArm1", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-6.0F, 0.0F, 0.0F));

        PartDefinition rightArm2 = partdefinition.addOrReplaceChild("RightArm2", CubeListBuilder.create(), PartPose.offset(-6.0F, 6.0F, 0.0F));

        rightArm2.addOrReplaceChild("RightArm2_r1", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public ModelPart root()
    {
        return root;
    }

    private int lastLoggedTick = Integer.MIN_VALUE;

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        head.xRot = headPitch * ((float) Math.PI / 180F);

        // FlyStart holds its pose indefinitely once flying begins, but shares several bones (Head, Body,
        // arms, legs) with the attack animations - applying both at once summed their rotations into a
        // twisted mess. An attack animation should temporarily override the held flight pose, not add to it.
        boolean attackAnimationActive = entity.meleeAnimationState.isStarted()
                || entity.rangedCastOneAnimationState.isStarted() || entity.rangedCastTwoAnimationState.isStarted();
        if (!attackAnimationActive)
        {
            this.animate(entity.flyStartAnimationState, FLY_START, ageInTicks);
        }
        this.animate(entity.meleeAnimationState, MALE_SWING, ageInTicks);
        // Played at 1.75x speed - the base-authored 0.5s felt slow for a ranged cast; keep in sync with
        // RANGED_CAST_1_ANIMATION_TICKS and CAST_1_EFFECT_DELAY_TICKS if this changes.
        this.animate(entity.rangedCastOneAnimationState, SPELL_CAST_1, ageInTicks, 1.75F);
        this.animate(entity.rangedCastTwoAnimationState, SPELL_CAST_2, ageInTicks);

        int currentTick = (int) ageInTicks;
        boolean flyLogging = entity.flyStartAnimationState.isStarted() && entity.flyStartAnimationState.getAccumulatedTime() < 400L;
        if (currentTick != lastLoggedTick
                && (entity.meleeAnimationState.isStarted() || entity.rangedCastOneAnimationState.isStarted()
                    || entity.rangedCastTwoAnimationState.isStarted() || flyLogging))
        {
            lastLoggedTick = currentTick;
            System.out.println("[SleepingGodDebug] MODEL tick=" + currentTick
                    + " ageInTicks=" + ageInTicks
                    + " meleeAcc=" + entity.meleeAnimationState.getAccumulatedTime()
                    + " c1Acc=" + entity.rangedCastOneAnimationState.getAccumulatedTime()
                    + " c2Acc=" + entity.rangedCastTwoAnimationState.getAccumulatedTime()
                    + " flyAcc=" + entity.flyStartAnimationState.getAccumulatedTime()
                    + " leftArm2.xRot=" + leftArm2.xRot + " leftArm2.yRot=" + leftArm2.yRot + " leftArm2.zRot=" + leftArm2.zRot
                    + " leftArm1.xRot=" + leftArm1.xRot
                    + " leftLeg1={" + leftLeg1.xRot + "," + leftLeg1.yRot + "," + leftLeg1.zRot + "}"
                    + " leftLeg2={" + leftLeg2.xRot + "," + leftLeg2.yRot + "," + leftLeg2.zRot + "}"
                    + " rightLeg1={" + rightLeg1.xRot + "," + rightLeg1.yRot + "," + rightLeg1.zRot + "}"
                    + " rightLeg2={" + rightLeg2.xRot + "," + rightLeg2.yRot + "," + rightLeg2.zRot + "}"
                    + " rightLeg2.pos={" + rightLeg2.x + "," + rightLeg2.y + "," + rightLeg2.z + "}"
                    + " head={" + head.xRot + "," + head.yRot + "," + head.zRot + "}"
                    + " body.pos={" + body.x + "," + body.y + "," + body.z + "} body.rot={" + body.xRot + "," + body.yRot + "," + body.zRot + "}");
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
