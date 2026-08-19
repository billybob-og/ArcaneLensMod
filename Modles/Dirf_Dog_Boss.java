// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class CustomModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "custommodel"), "main");
	private final ModelPart Body;
	private final ModelPart L_Leg;
	private final ModelPart R_Leg;
	private final ModelPart L_Arm;
	private final ModelPart L_Arm1;
	private final ModelPart L_arm2;
	private final ModelPart R_Arm;
	private final ModelPart R_Arm1;
	private final ModelPart R_Arm2;
	private final ModelPart HeadEye;
	private final ModelPart Teeth;
	private final ModelPart Tooth_Set2;
	private final ModelPart Tooth_Set_1;

	public CustomModel(ModelPart root) {
		this.Body = root.getChild("Body");
		this.L_Leg = this.Body.getChild("L_Leg");
		this.R_Leg = this.Body.getChild("R_Leg");
		this.L_Arm = this.Body.getChild("L_Arm");
		this.L_Arm1 = this.L_Arm.getChild("L_Arm1");
		this.L_arm2 = this.L_Arm.getChild("L_arm2");
		this.R_Arm = this.Body.getChild("R_Arm");
		this.R_Arm1 = this.R_Arm.getChild("R_Arm1");
		this.R_Arm2 = this.R_Arm.getChild("R_Arm2");
		this.HeadEye = this.Body.getChild("HeadEye");
		this.Teeth = this.Body.getChild("Teeth");
		this.Tooth_Set2 = this.Teeth.getChild("Tooth_Set2");
		this.Tooth_Set_1 = this.Teeth.getChild("Tooth_Set_1");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 18.0F, 3.0F, 0.0F, -0.0436F, 0.0F));

		PartDefinition Body_r1 = Body.addOrReplaceChild("Body_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -9.0F, -7.0F, 10.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -6.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition L_Leg = Body.addOrReplaceChild("L_Leg", CubeListBuilder.create().texOffs(8, 17).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, -3.0F, 0.0F, -0.0436F, 0.0F));

		PartDefinition R_Leg = Body.addOrReplaceChild("R_Leg", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, -3.0F, 0.0F, -0.0436F, 0.0F));

		PartDefinition L_Arm = Body.addOrReplaceChild("L_Arm", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.0F, -4.0F, -3.0F, 0.0F, -0.0436F, 0.0F));

		PartDefinition L_Arm1 = L_Arm.addOrReplaceChild("L_Arm1", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition L_Arm1_r1 = L_Arm1.addOrReplaceChild("L_Arm1_r1", CubeListBuilder.create().texOffs(16, 17).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

		PartDefinition L_arm2 = L_Arm.addOrReplaceChild("L_arm2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition L_Arm2_r1 = L_arm2.addOrReplaceChild("L_Arm2_r1", CubeListBuilder.create().texOffs(24, 23).addBox(-1.0F, -0.3954F, -0.9176F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6109F, 0.0F, 0.0F));

		PartDefinition R_Arm = Body.addOrReplaceChild("R_Arm", CubeListBuilder.create(), PartPose.offsetAndRotation(6.0F, -4.0F, -3.0F, 0.0F, -0.0436F, 0.0F));

		PartDefinition R_Arm1 = R_Arm.addOrReplaceChild("R_Arm1", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition R_Arm1_r1 = R_Arm1.addOrReplaceChild("R_Arm1_r1", CubeListBuilder.create().texOffs(16, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0436F, 0.0F));

		PartDefinition R_Arm2 = R_Arm.addOrReplaceChild("R_Arm2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition R_Arm2_r1 = R_Arm2.addOrReplaceChild("R_Arm2_r1", CubeListBuilder.create().texOffs(24, 17).addBox(-0.9128F, -0.3965F, -0.916F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6109F, 0.0436F, 0.0F));

		PartDefinition HeadEye = Body.addOrReplaceChild("HeadEye", CubeListBuilder.create(), PartPose.offset(0.0F, -9.0F, -3.0F));

		PartDefinition Eye_r1 = HeadEye.addOrReplaceChild("Eye_r1", CubeListBuilder.create().texOffs(36, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Teeth = Body.addOrReplaceChild("Teeth", CubeListBuilder.create(), PartPose.offset(0.5F, -6.0F, -6.0F));

		PartDefinition Tooth_Set2 = Teeth.addOrReplaceChild("Tooth_Set2", CubeListBuilder.create(), PartPose.offset(-4.5F, 5.0F, -1.0F));

		PartDefinition Tooth4_r1 = Tooth_Set2.addOrReplaceChild("Tooth4_r1", CubeListBuilder.create().texOffs(32, 17).addBox(-0.5F, -0.567F, -1.75F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5F, -0.5F, 0.0F, -0.5236F, 0.0F, 0.0F));

		PartDefinition Tooth5_r1 = Tooth_Set2.addOrReplaceChild("Tooth5_r1", CubeListBuilder.create().texOffs(24, 29).addBox(-0.5F, -0.7314F, -1.5783F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -0.5F, 0.0F, -1.0036F, 0.0F, 0.0F));

		PartDefinition Tooth6_r1 = Tooth_Set2.addOrReplaceChild("Tooth6_r1", CubeListBuilder.create().texOffs(16, 29).addBox(0.0F, -1.0F, -2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.0036F, 0.0F, 0.0F));

		PartDefinition Tooth_Set_1 = Teeth.addOrReplaceChild("Tooth_Set_1", CubeListBuilder.create(), PartPose.offset(-3.0F, 0.0F, 0.0F));

		PartDefinition Tooth3_r1 = Tooth_Set_1.addOrReplaceChild("Tooth3_r1", CubeListBuilder.create().texOffs(0, 29).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition Tooth1_r1 = Tooth_Set_1.addOrReplaceChild("Tooth1_r1", CubeListBuilder.create().texOffs(8, 25).addBox(-0.5F, -0.5468F, -2.2113F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -0.5F, -1.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition Tooth2_r1 = Tooth_Set_1.addOrReplaceChild("Tooth2_r1", CubeListBuilder.create().texOffs(8, 29).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}