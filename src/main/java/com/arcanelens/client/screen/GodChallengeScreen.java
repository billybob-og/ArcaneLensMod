package com.arcanelens.client.screen;

import com.arcanelens.god.GodDefinition;
import com.arcanelens.god.GodRegistry;
import com.arcanelens.menu.GodChallengeMenu;
import com.arcanelens.network.NetworkHandler;
import com.arcanelens.network.packet.ServerboundChallengeGodPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** A single-god confirmation, not a list - which god this challenges is already fixed by which
 * entrance's altar opened it (see GodChallengeAltarBlockEntity), so there's nothing to pick, just
 * confirm or back out. */
public class GodChallengeScreen extends AbstractContainerScreen<GodChallengeMenu>
{
    private final GodDefinition god;

    public GodChallengeScreen(GodChallengeMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        this.imageWidth = 220;
        this.imageHeight = 120;
        String godId = menu.getBlockEntity().getGodId();
        this.god = GodRegistry.GODS.stream().filter(g -> g.id().equals(godId)).findFirst().orElse(null);
    }

    @Override
    protected void init()
    {
        super.init();
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Begin Challenge"), b -> confirm())
                .bounds(left + 20, top + 90, this.imageWidth - 40, 20).build());
    }

    private void confirm()
    {
        NetworkHandler.CHANNEL.sendToServer(new ServerboundChallengeGodPacket());
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

        if (god == null)
        {
            graphics.drawCenteredString(this.font, "This challenge isn't ready yet.", left + this.imageWidth / 2, top + 20, 0xFFFFFF);
            return;
        }

        graphics.drawCenteredString(this.font, "Challenge " + god.displayName().getString() + "?",
                left + this.imageWidth / 2, top + 15, 0xFFFFFF);
        if (god.description() != null)
        {
            graphics.drawWordWrap(this.font, god.description(), left + 15, top + 35, this.imageWidth - 30, 0xCCCCCC);
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
