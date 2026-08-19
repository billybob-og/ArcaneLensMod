package com.arcanelens.client.renderer;

import com.arcanelens.ArcaneLens;
import com.arcanelens.client.model.SleepingGodModel;
import com.arcanelens.entity.SleepingGodEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SleepingGodRenderer extends MobRenderer<SleepingGodEntity, SleepingGodModel<SleepingGodEntity>>
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ArcaneLens.MODID, "textures/entity/sleeping_god.png");

    public SleepingGodRenderer(EntityRendererProvider.Context context)
    {
        super(context, new SleepingGodModel<>(context.bakeLayer(SleepingGodModel.LAYER_LOCATION)), 1.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(SleepingGodEntity entity)
    {
        return TEXTURE;
    }

    @Override
    protected void scale(SleepingGodEntity entity, PoseStack poseStack, float partialTickTime)
    {
        // Boss-scale stand-in; retune once the model's real proportions are confirmed in-game.
        poseStack.scale(2.3f, 2.3f, 2.3f);
    }
}
