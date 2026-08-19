package com.arcanelens.client.screen;

import com.arcanelens.item.MagicLensItem;
import com.arcanelens.menu.LensCombinerMenu;
import com.arcanelens.network.NetworkHandler;
import com.arcanelens.network.packet.ServerboundCombineLensesPacket;
import com.arcanelens.util.LensMerger;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class LensCombinerScreen extends AbstractContainerScreen<LensCombinerMenu>
{
    public LensCombinerScreen(LensCombinerMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 268;
        this.inventoryLabelY = 174;
    }

    @Override
    protected void init()
    {
        super.init();
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Combine"), b ->
                        NetworkHandler.CHANNEL.sendToServer(new ServerboundCombineLensesPacket()))
                .bounds(left + 60, top + 60, 60, 18).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF8B7355);

        drawSlotFrame(graphics, left, top, 44, 16);
        drawSlotFrame(graphics, left, top, 44, 40);
        drawSlotFrame(graphics, left, top, 80, 28);
        drawSlotFrame(graphics, left, top, 116, 28);

        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                drawSlotFrame(graphics, left, top, 8 + col * 18, 184 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++)
        {
            drawSlotFrame(graphics, left, top, 8 + col * 18, 242);
        }
    }

    private void drawSlotFrame(GuiGraphics graphics, int left, int top, int slotX, int slotY)
    {
        graphics.fill(left + slotX - 1, top + slotY - 1, left + slotX + 17, top + slotY + 17, 0xFF373737);
        graphics.fill(left + slotX, top + slotY, left + slotX + 16, top + slotY + 16, 0xFF8B8B8B);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        ItemStack bottom = this.menu.slots.get(0).getItem();
        ItemStack top1 = this.menu.slots.get(1).getItem();

        String status;
        if (bottom.isEmpty() || top1.isEmpty())
        {
            status = "Insert bottom + top lens";
        }
        else if (!LensMerger.canMerge(bottom, top1))
        {
            status = "Merge cap reached";
        }
        else
        {
            int newMergeCount = LensMerger.computeNewMergeCount(bottom, top1);
            int price = LensMerger.computePrice(newMergeCount);
            status = "Cost: " + price + " material (tier " + newMergeCount + "/" + MagicLensItem.MAX_MERGE_COUNT + ")";
        }

        graphics.drawString(this.font, status, left + 8, top + 84, 0xFFFFFF, true);
    }
}
