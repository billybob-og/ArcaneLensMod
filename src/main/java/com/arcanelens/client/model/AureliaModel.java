package com.arcanelens.client.model;

import com.arcanelens.ArcaneLens;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;

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
 * <p>Implements ArmedModel (not part of the raw export) so ItemInHandLayer can find the correct hand
 * to render her held spear at - translateToHand mirrors HumanoidModel's own implementation, just
 * delegating to rArm/lArm instead of vanilla's arm parts.</p>
 *
 * <p>No animations are authored yet (setupAnim is a no-op, matching the raw export) - she'll look
 * rigid while walking/attacking until a walk-cycle/attack animation is added.</p>
 */
public class AureliaModel<T extends Entity> extends EntityModel<T> implements ArmedModel
{
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(ArcaneLens.MODID, "aurelia"), "main");

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

        legs.addOrReplaceChild("RLeg", CubeListBuilder.create().texOffs(0, 64).addBox(-4.0F, -24.0F, -3.0F, 8.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 0.0F, -1.0F));

        legs.addOrReplaceChild("LLeg", CubeListBuilder.create().texOffs(48, 32).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, 0.0F, -4.0F, 16.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -24.0F, -1.0F));

        PartDefinition arms = partdefinition.addOrReplaceChild("Arms", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, -1.0F));

        arms.addOrReplaceChild("LArm", CubeListBuilder.create().texOffs(64, 0).addBox(0.0F, -20.0F, -4.0F, 6.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, 0.0F));

        arms.addOrReplaceChild("RArm", CubeListBuilder.create().texOffs(32, 64).addBox(-6.0F, -20.0F, -4.0F, 6.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, 0.0F, -10.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -40.0F, 1.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
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
