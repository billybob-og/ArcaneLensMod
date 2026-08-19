package com.arcanelens.client.screen;

import com.arcanelens.block.entity.ArcaneAssemblerBlockEntity;
import com.arcanelens.menu.ArcaneAssemblerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ArcaneAssemblerScreen extends AbstractContainerScreen<ArcaneAssemblerMenu>
{
    private static final int FUEL_X = 8;
    private static final int FUEL_Y = 18;
    private static final int BUFFER_X = 8;
    private static final int BUFFER_Y = 40;
    private static final int GRID_X = 98;
    private static final int GRID_Y = 18;
    private static final int OUTPUT_X = 172;
    private static final int OUTPUT_Y = 36;
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 94;
    private static final int HOTBAR_Y = 152;

    public ArcaneAssemblerScreen(ArcaneAssemblerMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 178;
        this.inventoryLabelY = PLAYER_INV_Y - 10;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF8B7355);

        drawSlotFrame(graphics, left, top, FUEL_X, FUEL_Y);

        for (int i = 0; i < ArcaneAssemblerBlockEntity.BUFFER_SLOTS; i++)
        {
            drawSlotFrame(graphics, left, top, BUFFER_X + (i % 4) * 18, BUFFER_Y + (i / 4) * 18);
        }

        for (int i = 0; i < ArcaneAssemblerBlockEntity.GRID_SLOTS; i++)
        {
            drawSlotFrame(graphics, left, top, GRID_X + (i % 3) * 18, GRID_Y + (i / 3) * 18);
        }

        drawSlotFrame(graphics, left, top, OUTPUT_X, OUTPUT_Y);

        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                drawSlotFrame(graphics, left, top, PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18);
            }
        }
        for (int col = 0; col < 9; col++)
        {
            drawSlotFrame(graphics, left, top, PLAYER_INV_X + col * 18, HOTBAR_Y);
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

        ArcaneAssemblerBlockEntity blockEntity = this.menu.getBlockEntity();
        String status = "Fuel: " + blockEntity.getFuelTicksRemaining() + "t  Progress: "
                + blockEntity.getCraftProgressTicks() + "/" + blockEntity.getEffectiveCraftTime();
        graphics.drawString(this.font, status, left + 8, top + 80, 0xFFFFFF, true);
    }
}
