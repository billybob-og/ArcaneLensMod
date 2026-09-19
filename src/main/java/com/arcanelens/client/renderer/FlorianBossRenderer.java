package com.arcanelens.client.renderer;

import com.arcanelens.ArcaneLens;
import com.arcanelens.client.model.FlorianBossModel;
import com.arcanelens.entity.boss.FlorianBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FlorianBossRenderer extends MobRenderer<FlorianBossEntity, FlorianBossModel<FlorianBossEntity>>
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ArcaneLens.MODID, "textures/entity/florian_boss.png");

    public FlorianBossRenderer(EntityRendererProvider.Context context)
    {
        super(context, new FlorianBossModel<>(context.bakeLayer(FlorianBossModel.LAYER_LOCATION)), 1.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(FlorianBossEntity entity)
    {
        return TEXTURE;
    }
}
