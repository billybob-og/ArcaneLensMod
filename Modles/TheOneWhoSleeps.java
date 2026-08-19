// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class CustomModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "custommodel"), "main");
	private final ModelPart Waist;
	private final ModelPart Head;
	private final ModelPart Body;
	private final ModelPart LeftArm2;
	private final ModelPart Left Arm1;
	private final ModelPart Left Leg1;
	private final ModelPart Left Leg2;
	private final ModelPart Right Leg1;
	private final ModelPart Right Leg2;
	private final ModelPart Right Arm1;
	private final ModelPart Right Arm2;

	public CustomModel(ModelPart root) {
		this.Waist = root.getChild("Waist");
		this.Head = this.Waist.getChild("Head");
		this.Body = this.Waist.getChild("Body");
		this.LeftArm2 = root.getChild("LeftArm2");
		this.Left Arm1 = root.getChild("Left Arm1");
		this.Left Leg1 = root.getChild("Left Leg1");
		this.Left Leg2 = root.getChild("Left Leg2");
		this.Right Leg1 = root.getChild("Right Leg1");
		this.Right Leg2 = root.getChild("Right Leg2");
		this.Right Arm1 = root.getChild("Right Arm1");
		this.Right Arm2 = root.getChild("Right Arm2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Waist = partdefinition.addOrReplaceChild("Waist", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));

		PartDefinition Head = Waist.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition Body = Waist.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition LeftArm2 = partdefinition.addOrReplaceChild("LeftArm2", CubeListBuilder.create(), PartPose.offset(6.0F, 0.0F, 0.0F));

		PartDefinition Left Arm2_r1 = LeftArm2.addOrReplaceChild("Left Arm2_r1", CubeListBuilder.create().texOffs(32, 48).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

		PartDefinition Left Arm1 = partdefinition.addOrReplaceChild("Left Arm1", CubeListBuilder.create().texOffs(32, 48).addBox(-2.5F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(6.5F, 6.0F, 0.0F));

		PartDefinition Left Leg1 = partdefinition.addOrReplaceChild("Left Leg1", CubeListBuilder.create().texOffs(16, 48).addBox(-2.1F, -0.25F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.25F, 18.25F, 0.0F));

		PartDefinition Left Leg2 = partdefinition.addOrReplaceChild("Left Leg2", CubeListBuilder.create(), PartPose.offset(2.25F, 12.0F, 0.0F));

		PartDefinition Left Leg2_r1 = Left Leg2.addOrReplaceChild("Left Leg2_r1", CubeListBuilder.create().texOffs(16, 48).addBox(-1.85F, -6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

		PartDefinition Right Leg1 = partdefinition.addOrReplaceChild("Right Leg1", CubeListBuilder.create().texOffs(0, 16).addBox(-1.9F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

		PartDefinition Right Leg2 = partdefinition.addOrReplaceChild("Right Leg2", CubeListBuilder.create(), PartPose.offset(-1.75F, 18.0F, 0.0F));

		PartDefinition Right Leg2_r1 = Right Leg2.addOrReplaceChild("Right Leg2_r1", CubeListBuilder.create().texOffs(0, 16).addBox(-1.9F, -6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

		PartDefinition Right Arm1 = partdefinition.addOrReplaceChild("Right Arm1", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 0.0F, 0.0F));

		PartDefinition Right Arm2 = partdefinition.addOrReplaceChild("Right Arm2", CubeListBuilder.create(), PartPose.offset(-6.0F, 6.0F, 0.0F));

		PartDefinition Right Arm2_r1 = Right Arm2.addOrReplaceChild("Right Arm2_r1", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Waist.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		LeftArm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Left Arm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Left Leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Left Leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Right Leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Right Leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Right Arm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Right Arm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}