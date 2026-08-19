package com.arcanelens.client.renderer;

import com.arcanelens.block.entity.LensPedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LensPedestalRenderer implements BlockEntityRenderer<LensPedestalBlockEntity>
{
    private final ItemRenderer itemRenderer;

    public LensPedestalRenderer(BlockEntityRendererProvider.Context context)
    {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(LensPedestalBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        ItemStack lens = blockEntity.getLens();
        if (lens.isEmpty() || blockEntity.getLevel() == null)
        {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 1.45, 0.5);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        itemRenderer.renderStatic(lens, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, blockEntity.getLevel(), 0);

        poseStack.popPose();
    }
}
