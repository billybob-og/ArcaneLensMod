// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class florianBoss<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "florianboss"), "main");
	private final ModelPart Legs;
	private final ModelPart FrontLegs;
	private final ModelPart LLeg;
	private final ModelPart LLeg1;
	private final ModelPart LLeg1pt2;
	private final ModelPart RLeg;
	private final ModelPart RLeg1;
	private final ModelPart RLeg1pt2;
	private final ModelPart BackLegs;
	private final ModelPart LLeg2;
	private final ModelPart LLeg2pt2;
	private final ModelPart LLeg(2);
	private final ModelPart RLeg2;
	private final ModelPart RLeg(2);
	private final ModelPart RLeg2pt2;
	private final ModelPart Head;
	private final ModelPart head2;
	private final ModelPart Antlers;
	private final ModelPart Lantler;
	private final ModelPart RAntler;
	private final ModelPart bb_main;

	public florianBoss(ModelPart root) {
		this.Legs = root.getChild("Legs");
		this.FrontLegs = this.Legs.getChild("FrontLegs");
		this.LLeg = this.FrontLegs.getChild("LLeg");
		this.LLeg1 = this.LLeg.getChild("LLeg1");
		this.LLeg1pt2 = this.LLeg.getChild("LLeg1pt2");
		this.RLeg = this.FrontLegs.getChild("RLeg");
		this.RLeg1 = this.RLeg.getChild("RLeg1");
		this.RLeg1pt2 = this.RLeg.getChild("RLeg1pt2");
		this.BackLegs = this.Legs.getChild("BackLegs");
		this.LLeg2 = this.BackLegs.getChild("LLeg2");
		this.LLeg2pt2 = this.LLeg2.getChild("LLeg2pt2");
		this.LLeg(2) = this.LLeg2.getChild("LLeg(2)");
		this.RLeg2 = this.BackLegs.getChild("RLeg2");
		this.RLeg(2) = this.RLeg2.getChild("RLeg(2)");
		this.RLeg2pt2 = this.RLeg2.getChild("RLeg2pt2");
		this.Head = root.getChild("Head");
		this.head2 = this.Head.getChild("head2");
		this.Antlers = this.Head.getChild("Antlers");
		this.Lantler = this.Antlers.getChild("Lantler");
		this.RAntler = this.Antlers.getChild("RAntler");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Legs = partdefinition.addOrReplaceChild("Legs", CubeListBuilder.create(), PartPose.offset(5.0F, 17.0F, 11.0F));

		PartDefinition FrontLegs = Legs.addOrReplaceChild("FrontLegs", CubeListBuilder.create(), PartPose.offset(-5.0F, 7.0F, -11.0F));

		PartDefinition LLeg = FrontLegs.addOrReplaceChild("LLeg", CubeListBuilder.create(), PartPose.offset(5.0F, 0.0F, -11.0F));

		PartDefinition LLeg1 = LLeg.addOrReplaceChild("LLeg1", CubeListBuilder.create().texOffs(16, 50).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, -7.0F, 0.5F));

		PartDefinition LLeg1pt2 = LLeg.addOrReplaceChild("LLeg1pt2", CubeListBuilder.create().texOffs(64, 54).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, -14.0F, 0.5F));

		PartDefinition RLeg = FrontLegs.addOrReplaceChild("RLeg", CubeListBuilder.create(), PartPose.offset(5.0F, -7.0F, -11.0F));

		PartDefinition RLeg1 = RLeg.addOrReplaceChild("RLeg1", CubeListBuilder.create().texOffs(28, 57).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.5F, 0.0F, 0.5F));

		PartDefinition RLeg1pt2 = RLeg.addOrReplaceChild("RLeg1pt2", CubeListBuilder.create().texOffs(64, 44).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.5F, -7.0F, 0.5F));

		PartDefinition BackLegs = Legs.addOrReplaceChild("BackLegs", CubeListBuilder.create(), PartPose.offset(-11.0F, 7.0F, 0.0F));

		PartDefinition LLeg2 = BackLegs.addOrReplaceChild("LLeg2", CubeListBuilder.create(), PartPose.offset(6.0F, 0.0F, -11.0F));

		PartDefinition LLeg2pt2 = LLeg2.addOrReplaceChild("LLeg2pt2", CubeListBuilder.create().texOffs(16, 60).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(5.5F, -14.0F, 11.5F));

		PartDefinition LLeg(2) = LLeg2.addOrReplaceChild("LLeg(2)", CubeListBuilder.create().texOffs(52, 57).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(5.5F, -7.0F, 11.5F));

		PartDefinition RLeg2 = BackLegs.addOrReplaceChild("RLeg2", CubeListBuilder.create(), PartPose.offset(11.0F, -7.0F, 0.0F));

		PartDefinition RLeg(2) = RLeg2.addOrReplaceChild("RLeg(2)", CubeListBuilder.create().texOffs(40, 57).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.5F, 0.0F, 0.5F));

		PartDefinition RLeg2pt2 = RLeg2.addOrReplaceChild("RLeg2pt2", CubeListBuilder.create().texOffs(64, 34).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.5F, -7.0F, 0.5F));

		PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition Neck_r1 = Head.addOrReplaceChild("Neck_r1", CubeListBuilder.create().texOffs(34, 34).addBox(-3.0F, -5.0F, -14.0F, 6.0F, 5.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -18.0F, -11.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition head2 = Head.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(0, 34).addBox(-3.0F, -5.0F, -7.5F, 6.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(34, 48).addBox(-3.0F, -7.0F, -3.5F, 6.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -32.0F, -8.5F));

		PartDefinition Antlers = Head.addOrReplaceChild("Antlers", CubeListBuilder.create(), PartPose.offset(2.0F, -39.0F, -6.0F));

		PartDefinition Lantler = Antlers.addOrReplaceChild("Lantler", CubeListBuilder.create().texOffs(0, 50).addBox(-5.0F, -13.0F, -1.0F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(64, 64).addBox(-8.0F, -13.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Antlerpt2_r1 = Lantler.addOrReplaceChild("Antlerpt2_r1", CubeListBuilder.create().texOffs(8, 65).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -3.0F, -0.25F, -0.0693F, -0.0531F, -0.6527F));

		PartDefinition RAntler = Antlers.addOrReplaceChild("RAntler", CubeListBuilder.create().texOffs(0, 65).addBox(6.0F, -10.0F, -0.75F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(8, 50).addBox(3.0F, -10.0F, -0.75F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -3.0F, -0.25F));

		PartDefinition Antlerpt3_r1 = RAntler.addOrReplaceChild("Antlerpt3_r1", CubeListBuilder.create().texOffs(28, 67).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, -0.0618F, 0.0617F, 0.7835F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -23.0F, -12.0F, 14.0F, 9.0F, 25.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Legs.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}