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
 * vs. vanilla's 32 units / 64x64), so scale() below scales the render size back down - not all the way
 * to a flat 0.5x (an exact player-height match), since that read as noticeably small in-game; 0.65x is
 * a slightly-larger-than-player middle ground, fitting a "war goddess" being a bit more imposing than
 * a normal person. Purely a visual judgment call, easy to retune further either way.</p>
 *
 * <p>Her SPEAR is equipped in her MAINHAND slot (see AureliaSummonHandler) and rendered via
 * AureliaItemInHandLayer (not the stock ItemInHandLayer) - see that class for why: her 0.65x body
 * scale would otherwise also shrink the spear on top of its own display-transform size. AureliaModel
 * implements ArmedModel specifically so this layer can find the correct hand to attach to.</p>
 */
public class AureliaCompanionRenderer extends MobRenderer<AureliaCompanionEntity, AureliaModel<AureliaCompanionEntity>>
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ArcaneLens.MODID, "textures/entity/aurelia2.png");

    public AureliaCompanionRenderer(EntityRendererProvider.Context context)
    {
        super(context, new AureliaModel<>(context.bakeLayer(AureliaModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new AureliaItemInHandLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(AureliaCompanionEntity entity)
    {
        return TEXTURE;
    }

    @Override
    protected void scale(AureliaCompanionEntity entity, PoseStack poseStack, float partialTickTime)
    {
        poseStack.scale(0.65F, 0.65F, 0.65F);
    }
}
