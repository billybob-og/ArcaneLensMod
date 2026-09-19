package com.arcanelens.client.renderer;

import com.arcanelens.ArcaneLens;
import com.arcanelens.client.model.FlorianBossModel;
import com.arcanelens.entity.FlorianCompanionEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** The Antler summon - the boss's rig and texture, drawn smaller so it reads as a lesser echo of him. */
public class FlorianCompanionRenderer extends MobRenderer<FlorianCompanionEntity, FlorianBossModel<FlorianCompanionEntity>>
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ArcaneLens.MODID, "textures/entity/florian_boss.png");

    public FlorianCompanionRenderer(EntityRendererProvider.Context context)
    {
        super(context, new FlorianBossModel<>(context.bakeLayer(FlorianBossModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(FlorianCompanionEntity entity)
    {
        return TEXTURE;
    }

    @Override
    protected void scale(FlorianCompanionEntity entity, PoseStack poseStack, float partialTickTime)
    {
        poseStack.scale(0.7F, 0.7F, 0.7F);
    }
}
