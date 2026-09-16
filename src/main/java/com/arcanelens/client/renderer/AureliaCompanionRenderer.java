package com.arcanelens.client.renderer;

import com.arcanelens.ArcaneLens;
import com.arcanelens.client.model.AureliaModel;
import com.arcanelens.entity.AureliaCompanionEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Aurelia's real rigged body model (see AureliaModel), textured as Aurelia2 - the "different skin"
 * she's specifically meant to summon with (Aurelia1's texture is the same rig's other, "normal" look,
 * for whatever future context that ends up being used in - not this one).
 *
 * <p>The model geometry is authored at 2x vanilla humanoid scale (64 units tall / a 128x128 texture,
 * vs. vanilla's 32 units / 64x64), so scale() below scales the render size back down to exactly 0.5x -
 * an exact player-height match, per the friend who made her model/skin asking for player-sized
 * rendering rather than the previous "slightly larger" 0.65x.</p>
 *
 * <p>No held item/spear is rendered - she fights bare-handed (see AureliaSummonHandler, which no
 * longer equips anything into her mainhand slot), also per that request.</p>
 */
public class AureliaCompanionRenderer extends MobRenderer<AureliaCompanionEntity, AureliaModel>
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ArcaneLens.MODID, "textures/entity/aurelia2.png");

    public AureliaCompanionRenderer(EntityRendererProvider.Context context)
    {
        super(context, new AureliaModel(context.bakeLayer(AureliaModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(AureliaCompanionEntity entity)
    {
        return TEXTURE;
    }

    @Override
    protected void scale(AureliaCompanionEntity entity, PoseStack poseStack, float partialTickTime)
    {
        poseStack.scale(0.5F, 0.5F, 0.5F);
    }
}
