package com.arcanelens.client.renderer;

import com.arcanelens.ArcaneLens;
import com.arcanelens.entity.BrokenVesselEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Reuses vanilla's own HumanoidModel unmodified rather than a bespoke Blockbench model - the boss's texture
 * (Modles/BrokenVessel.png, moved to textures/entity/broken_vessel.png) is a genuine "2x resolution" skin,
 * same proportions as a normal 64x64 player skin just doubled. Model UV coordinates are resolution-
 * independent (normalized 0-1, baked from vanilla's own hardcoded 64x64 reference), so simply supplying the
 * 128x128 texture as-is renders at the higher effective density automatically - no re-mapping needed, which
 * is also why this doesn't need its own custom client/model class the way SleepingGodModel does.
 */
public class BrokenVesselRenderer extends MobRenderer<BrokenVesselEntity, HumanoidModel<BrokenVesselEntity>>
{
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(ArcaneLens.MODID, "broken_vessel"), "main");

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ArcaneLens.MODID, "textures/entity/broken_vessel.png");

    public BrokenVesselRenderer(EntityRendererProvider.Context context)
    {
        super(context, new HumanoidModel<>(context.bakeLayer(LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(BrokenVesselEntity entity)
    {
        return TEXTURE;
    }

    @Override
    protected void scale(BrokenVesselEntity entity, PoseStack poseStack, float partialTickTime)
    {
        // Boss-scale stand-in, same approach SleepingGodRenderer takes - retune once real proportions are
        // confirmed in-game.
        poseStack.scale(2.0f, 2.0f, 2.0f);
    }
}
