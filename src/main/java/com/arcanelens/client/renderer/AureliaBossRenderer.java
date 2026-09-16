package com.arcanelens.client.renderer;

import com.arcanelens.ArcaneLens;
import com.arcanelens.client.model.AureliaBossModel;
import com.arcanelens.entity.boss.AureliaBossEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Uses the Aurelia1 texture - the rig's other "normal" look, distinct from Aurelia2 (which stays
 * reserved for the companion summon specifically) - at a noticeably larger scale (0.8x vs. the
 * companion's exact-player-height 0.5x), since a boss should read as more imposing than a temporary
 * summon; easy to retune further either way. */
public class AureliaBossRenderer extends MobRenderer<AureliaBossEntity, AureliaBossModel>
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ArcaneLens.MODID, "textures/entity/aurelia1.png");

    public AureliaBossRenderer(EntityRendererProvider.Context context)
    {
        super(context, new AureliaBossModel(context.bakeLayer(AureliaBossModel.LAYER_LOCATION)), 0.8f);
        this.addLayer(new AureliaBossItemInHandLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(AureliaBossEntity entity)
    {
        return TEXTURE;
    }

    @Override
    protected void scale(AureliaBossEntity entity, PoseStack poseStack, float partialTickTime)
    {
        poseStack.scale(0.8F, 0.8F, 0.8F);
    }
}
