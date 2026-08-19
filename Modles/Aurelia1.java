// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class Aurelia1<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "aurelia1"), "main");
	private final ModelPart Legs;
	private final ModelPart RLeg;
	private final ModelPart LLeg;
	private final ModelPart Torso;
	private final ModelPart Arms;
	private final ModelPart LArm;
	private final ModelPart RArm;
	private final ModelPart Head;

	public Aurelia1(ModelPart root) {
		this.Legs = root.getChild("Legs");
		this.RLeg = this.Legs.getChild("RLeg");
		this.LLeg = this.Legs.getChild("LLeg");
		this.Torso = root.getChild("Torso");
		this.Arms = root.getChild("Arms");
		this.LArm = this.Arms.getChild("LArm");
		this.RArm = this.Arms.getChild("RArm");
		this.Head = root.getChild("Head");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Legs = partdefinition.addOrReplaceChild("Legs", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, -1.0F));

		PartDefinition RLeg = Legs.addOrReplaceChild("RLeg", CubeListBuilder.create().texOffs(0, 64).addBox(-4.0F, -24.0F, -3.0F, 8.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 0.0F, -1.0F));

		PartDefinition LLeg = Legs.addOrReplaceChild("LLeg", CubeListBuilder.create().texOffs(48, 32).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, 0.0F));

		PartDefinition Torso = partdefinition.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, 0.0F, -4.0F, 16.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, -1.0F));

		PartDefinition Arms = partdefinition.addOrReplaceChild("Arms", CubeListBuilder.create(), PartPose.offset(0.0F, 44.0F, -1.0F));

		PartDefinition LArm = Arms.addOrReplaceChild("LArm", CubeListBuilder.create().texOffs(64, 0).addBox(0.0F, -20.0F, -4.0F, 6.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, 0.0F));

		PartDefinition RArm = Arms.addOrReplaceChild("RArm", CubeListBuilder.create().texOffs(32, 64).addBox(-6.0F, -20.0F, -4.0F, 6.0F, 24.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 0.0F, 0.0F));

		PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, 0.0F, -10.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 48.0F, 1.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Legs.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Arms.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}