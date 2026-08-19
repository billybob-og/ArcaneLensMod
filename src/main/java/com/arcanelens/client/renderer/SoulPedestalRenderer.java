package com.arcanelens.client.renderer;

import com.arcanelens.block.SoulPedestalBlock;
import com.arcanelens.block.entity.SoulPedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class SoulPedestalRenderer implements BlockEntityRenderer<SoulPedestalBlockEntity>
{
    // Bowl interior derived from the soul_pedestal.json model geometry (in 1/16ths of a block):
    // floor at y=17.5, rim top at y=19.25, walls enclosing x/z [4.5, 11.5].
    private static final double BOWL_MIN_X = 4.5 / 16.0;
    private static final double BOWL_MIN_Z = 4.5 / 16.0;
    private static final double BOWL_WIDTH = (11.5 - 4.5) / 16.0;
    private static final double BOWL_FLOOR_Y = 17.5 / 16.0;
    private static final double BOWL_FILL_HEIGHT = (19.25 - 17.5) / 16.0;

    private final ItemRenderer itemRenderer;

    public SoulPedestalRenderer(BlockEntityRendererProvider.Context context)
    {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(SoulPedestalBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        if (blockEntity.getLevel() == null)
        {
            return;
        }

        int fuel = blockEntity.getBlockState().getValue(SoulPedestalBlock.FUEL);
        if (fuel > 0)
        {
            float heightFraction = (float) (fuel / (double) SoulPedestalBlockEntity.MAX_FUEL);

            poseStack.pushPose();
            poseStack.translate(BOWL_MIN_X, BOWL_FLOOR_Y, BOWL_MIN_Z);
            poseStack.scale((float) BOWL_WIDTH, (float) (BOWL_FILL_HEIGHT * heightFraction), (float) BOWL_WIDTH);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.SOUL_SAND.defaultBlockState(), poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }

        ItemStack conversionItem = blockEntity.getConversionItem();
        if (!conversionItem.isEmpty())
        {
            poseStack.pushPose();
            poseStack.translate(0.5, 1.45, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees((blockEntity.getLevel().getGameTime() + partialTick) * 2.0f));
            poseStack.scale(0.4f, 0.4f, 0.4f);
            itemRenderer.renderStatic(conversionItem, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, blockEntity.getLevel(), 0);
            poseStack.popPose();
        }
    }
}
