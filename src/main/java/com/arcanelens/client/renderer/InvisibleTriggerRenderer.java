package com.arcanelens.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Command Trigger and Trigger Plate are fully invisible (empty block model) in normal play, but that makes
 * them impossible to find again once placed. Draws a plain wireframe box, but only for players in creative
 * mode - survival/adventure players never see anything, matching the "invisible admin fixture" intent.
 */
public class InvisibleTriggerRenderer<T extends BlockEntity> implements BlockEntityRenderer<T>
{
    private final float red;
    private final float green;
    private final float blue;

    public InvisibleTriggerRenderer(BlockEntityRendererProvider.Context context, float red, float green, float blue)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.getAbilities().instabuild)
        {
            return;
        }

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, consumer, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, red, green, blue, 1.0F);
    }
}
