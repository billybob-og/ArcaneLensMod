package com.arcanelens.client.renderer;

import com.arcanelens.ArcaneLens;
import com.arcanelens.entity.TheaterCloneEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Placeholder art: reuses vanilla's own HumanoidModel + the default Steve skin (same "reuse stock
 * humanoid mesh" trick BrokenVesselRenderer already uses) until real decoy/clone art exists - a
 * generic humanoid shape is a reasonable stand-in for "a copy of the player" either way. */
public class TheaterCloneRenderer extends MobRenderer<TheaterCloneEntity, HumanoidModel<TheaterCloneEntity>>
{
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(ArcaneLens.MODID, "theater_clone"), "main");

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/player/wide/steve.png");

    public TheaterCloneRenderer(EntityRendererProvider.Context context)
    {
        super(context, new HumanoidModel<>(context.bakeLayer(LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(TheaterCloneEntity entity)
    {
        return TEXTURE;
    }
}
