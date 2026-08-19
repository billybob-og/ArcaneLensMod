package com.arcanelens.client.screen;

import com.arcanelens.god.GodDefinition;
import com.arcanelens.god.GodRegistry;
import com.arcanelens.menu.GodPledgeMenu;
import com.arcanelens.network.NetworkHandler;
import com.arcanelens.network.packet.ServerboundChoosePledgePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

/** One button per GodRegistry.GODS entry plus "Remain Neutral" - a future god only needs a new
 * GodDefinition entry, never a change here. Placeholder (unavailable) gods render as vanilla's own
 * greyed-out disabled buttons with a tooltip, not custom art. Hovering an available god's button shows
 * a preview panel with that god's name and an enlarged render of their altar block's item (the one
 * piece of art every real GodDefinition already has, via altarBlock) - a future locked god with a null
 * altarBlock simply shows no preview, which also keeps its "???" mystery intact. */
public class GodPledgeScreen extends AbstractContainerScreen<GodPledgeMenu>
{
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 5;
    private static final int TOP_MARGIN = 30;
    private static final int BOTTOM_MARGIN = 10;
    private static final int BUTTON_WIDTH = 180;
    private static final int PREVIEW_PANEL_WIDTH = 100;
    private static final int PREVIEW_ICON_SCALE = 4;

    private final Map<Button, GodDefinition> godButtons = new HashMap<>();

    public GodPledgeScreen(GodPledgeMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        this.imageWidth = 200 + PREVIEW_PANEL_WIDTH;
        int rowCount = GodRegistry.GODS.size() + 1; // +1 for "Remain Neutral"
        this.imageHeight = TOP_MARGIN + rowCount * (BUTTON_HEIGHT + BUTTON_GAP) + BOTTOM_MARGIN;
    }

    @Override
    protected void init()
    {
        super.init();
        godButtons.clear();
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        int y = top + TOP_MARGIN;
        for (GodDefinition god : GodRegistry.GODS)
        {
            Button button = Button.builder(god.displayName(), b -> choose(god.id()))
                    .bounds(left + 10, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
            if (!god.available())
            {
                button.active = false;
                if (god.lockedFlavorText() != null)
                {
                    button.setTooltip(Tooltip.create(god.lockedFlavorText()));
                }
            }
            this.addRenderableWidget(button);
            godButtons.put(button, god);
            y += BUTTON_HEIGHT + BUTTON_GAP;
        }

        this.addRenderableWidget(Button.builder(Component.literal("Remain Neutral"), b -> choose(GodRegistry.NEUTRAL_ID))
                .bounds(left + 10, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    private void choose(String godId)
    {
        NetworkHandler.CHANNEL.sendToServer(new ServerboundChoosePledgePacket(godId));
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

        int panelLeft = left + 200;
        graphics.fill(panelLeft, top, panelLeft + PREVIEW_PANEL_WIDTH, top + this.imageHeight, 0xFF6B5642);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        graphics.drawString(this.font, "Pledge your Faith to a god:", left + 10, top + 10, 0xFFFFFF, true);

        renderHoveredPreview(graphics, left + 200, top);
    }

    /** Widget hover state is only current after super.render() has run this frame, so this looks it up
     * fresh each time rather than tracking it separately - a button being hovered/not can change every
     * frame purely from mouse movement, with no click involved. */
    private void renderHoveredPreview(GuiGraphics graphics, int panelLeft, int top)
    {
        GodDefinition hovered = null;
        for (Map.Entry<Button, GodDefinition> entry : godButtons.entrySet())
        {
            if (entry.getKey().isHovered() && entry.getValue().available() && entry.getValue().altarBlock() != null)
            {
                hovered = entry.getValue();
                break;
            }
        }
        if (hovered == null)
        {
            return;
        }

        int panelCenterX = panelLeft + PREVIEW_PANEL_WIDTH / 2;

        Component name = hovered.displayName();
        graphics.drawCenteredString(this.font, name, panelCenterX, top + 10, 0xFFFFFF);

        Block altarBlock = hovered.altarBlock().get().get();
        ItemStack stack = new ItemStack(altarBlock.asItem());

        int iconScreenSize = 16 * PREVIEW_ICON_SCALE;
        int iconX = panelCenterX - iconScreenSize / 2;
        int iconY = top + 30;

        graphics.pose().pushPose();
        graphics.pose().translate(iconX, iconY, 0);
        graphics.pose().scale(PREVIEW_ICON_SCALE, PREVIEW_ICON_SCALE, 1);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();

        if (hovered.description() != null)
        {
            int textLeft = panelLeft + 5;
            int textTop = iconY + iconScreenSize + 10;
            int textWidth = PREVIEW_PANEL_WIDTH - 10;
            graphics.drawWordWrap(this.font, hovered.description(), textLeft, textTop, textWidth, 0xCCCCCC);
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
