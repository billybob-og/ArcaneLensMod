package com.arcanelens.client.renderer;

import com.arcanelens.ArcaneLens;
import com.arcanelens.capability.FaithProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * The Hunger's payoff for the ~75% watching-chance milestone: once a player's synced hungerEyesRevealed
 * flips true, their sun and moon permanently render as the user-authored "eye" textures instead of vanilla's
 * sun.png/moon_phases.png - a personal, one-way reveal (see TheHungerHandler / IFaith). Draws its own quads
 * right after vanilla's sky pass (Stage.AFTER_SKY) using the same position/rotation math as
 * LevelRenderer.renderSky, read directly from decompiled source rather than guessed.
 */
public class TheHungerSkyRenderer
{
    private static final ResourceLocation SUN_LOCATION =
            new ResourceLocation(ArcaneLens.MODID, "textures/environment/the_hunger_sun.png");
    private static final ResourceLocation MOON_LOCATION =
            new ResourceLocation(ArcaneLens.MODID, "textures/environment/the_hunger_moon.png");

    public static void onRenderLevelStage(RenderLevelStageEvent event)
    {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY)
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null
                || mc.level.effects().skyType() != DimensionSpecialEffects.SkyType.NORMAL)
        {
            return;
        }

        boolean revealed = mc.player.getCapability(FaithProvider.CAPABILITY)
                .map(cap -> cap.isHungerEyesRevealed() && !cap.isHungerEyesHidden()).orElse(false);
        if (!revealed)
        {
            return;
        }

        float partialTick = event.getPartialTick();
        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();

        float rainAlpha = 1.0F - mc.level.getRainLevel(partialTick);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rainAlpha);
        RenderSystem.depthMask(false);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(mc.level.getTimeOfDay(partialTick) * 360.0F));
        Matrix4f matrix4f = poseStack.last().pose();

        float sunHalfSize = 30.0F;
        RenderSystem.setShaderTexture(0, SUN_LOCATION);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, -sunHalfSize, 100.0F, -sunHalfSize).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, sunHalfSize, 100.0F, -sunHalfSize).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, sunHalfSize, 100.0F, sunHalfSize).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, -sunHalfSize, 100.0F, sunHalfSize).uv(0.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        float moonHalfSize = 20.0F;
        RenderSystem.setShaderTexture(0, MOON_LOCATION);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, -moonHalfSize, -100.0F, moonHalfSize).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, moonHalfSize, -100.0F, moonHalfSize).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, moonHalfSize, -100.0F, -moonHalfSize).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, -moonHalfSize, -100.0F, -moonHalfSize).uv(0.0F, 0.0F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        poseStack.popPose();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
