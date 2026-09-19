package com.arcanelens.client.renderer;

import com.arcanelens.entity.TheaterCloneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** Draws Stage Clone's decoy as the player who summoned it: their skin (and slim/wide arm model), plus the
 * armor and held items copied onto the entity for display (see TheaterCloneEntity). Falls back to the
 * default skin for that UUID if the client doesn't know the player, e.g. they've since logged out. */
public class TheaterCloneRenderer extends MobRenderer<TheaterCloneEntity, PlayerModel<TheaterCloneEntity>>
{
    private final PlayerModel<TheaterCloneEntity> wideModel;
    private final PlayerModel<TheaterCloneEntity> slimModel;

    public TheaterCloneRenderer(EntityRendererProvider.Context context)
    {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        this.wideModel = this.model;
        this.slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public void render(TheaterCloneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight)
    {
        this.model = isSlim(entity) ? slimModel : wideModel;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TheaterCloneEntity entity)
    {
        PlayerInfo info = playerInfo(entity);
        if (info != null)
        {
            return info.getSkinLocation();
        }
        UUID id = entity.getOwnerSkinId();
        return id == null ? DefaultPlayerSkin.getDefaultSkin() : DefaultPlayerSkin.getDefaultSkin(id);
    }

    private static boolean isSlim(TheaterCloneEntity entity)
    {
        PlayerInfo info = playerInfo(entity);
        if (info != null)
        {
            return "slim".equals(info.getModelName());
        }
        UUID id = entity.getOwnerSkinId();
        return id != null && "slim".equals(DefaultPlayerSkin.getSkinModelName(id));
    }

    private static PlayerInfo playerInfo(TheaterCloneEntity entity)
    {
        UUID id = entity.getOwnerSkinId();
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return id == null || connection == null ? null : connection.getPlayerInfo(id);
    }
}
