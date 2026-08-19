package com.arcanelens.client.screen;

import com.arcanelens.menu.TokenOfTheHungerMenu;
import com.arcanelens.network.NetworkHandler;
import com.arcanelens.network.packet.ServerboundChooseHungerBoonPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TokenOfTheHungerScreen extends AbstractContainerScreen<TokenOfTheHungerMenu>
{
    public TokenOfTheHungerScreen(TokenOfTheHungerMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 90;
    }

    @Override
    protected void init()
    {
        super.init();
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("The Hunger's Boon"), b -> choose(false))
                .bounds(left + 10, top + 30, 180, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("The Hunger's Bindings"), b -> choose(true))
                .bounds(left + 10, top + 55, 180, 20).build());
    }

    private void choose(boolean bindings)
    {
        NetworkHandler.CHANNEL.sendToServer(new ServerboundChooseHungerBoonPacket(bindings));
        this.onClose();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        // No slots on this screen - the default "Inventory" label has nowhere sensible to sit.
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF8B7355);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        graphics.drawString(this.font, "Choose a gift from The Hunger:", left + 10, top + 10, 0xFFFFFF, true);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
