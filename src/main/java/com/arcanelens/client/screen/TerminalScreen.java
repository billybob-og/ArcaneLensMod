package com.arcanelens.client.screen;

import com.arcanelens.menu.TerminalMenu;
import com.arcanelens.menu.TerminalSortMode;
import com.arcanelens.network.NetworkHandler;
import com.arcanelens.network.packet.ServerboundScrollTerminalPacket;
import com.arcanelens.network.packet.ServerboundSetTerminalSearchPacket;
import com.arcanelens.network.packet.ServerboundSetTerminalSortPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * Renders a Terminal's fixed on-screen pool-slot grid, upgrade slots, and player inventory. Scrolling
 * never repositions or rebuilds any Slot (see TerminalMenu/ScrollingPoolWindow) - it just updates
 * scrollOffset locally for immediate feedback and tells the server the same, so the next frame's slot
 * contents (resolved live through ScrollingPoolWindow) simply reflect the new window position. The search
 * box works the same way: typing updates TerminalMenu.searchQuery locally (ScrollingPoolWindow's filtered
 * mapping picks it up immediately) and tells the server the same text.
 */
public class TerminalScreen extends AbstractContainerScreen<TerminalMenu>
{
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SEARCH_BOX_X = TerminalMenu.POOL_GRID_X;
    private static final int SEARCH_BOX_Y = TerminalMenu.POOL_GRID_Y + TerminalMenu.POOL_VISIBLE_ROWS * 18 + 3;
    private static final int SORT_BUTTON_WIDTH = 40;
    // Search box gives up the width the sort button needs (plus a 2px gap) so the pair still lines up
    // flush with the pool grid's right edge, same as the search box alone used to.
    private static final int SEARCH_BOX_WIDTH = TerminalMenu.POOL_COLUMNS * 18 - SORT_BUTTON_WIDTH - 2;
    private static final int SEARCH_BOX_HEIGHT = 14;
    private static final int SORT_BUTTON_X = SEARCH_BOX_X + SEARCH_BOX_WIDTH + 2;

    private EditBox searchBox;
    private Button sortButton;

    public TerminalScreen(TerminalMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        this.imageWidth = 190;
        this.imageHeight = 245;
        this.inventoryLabelY = TerminalMenu.PLAYER_INV_Y - 10;
    }

    @Override
    protected void init()
    {
        super.init();
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        searchBox = new EditBox(this.font, left + SEARCH_BOX_X, top + SEARCH_BOX_Y, SEARCH_BOX_WIDTH, SEARCH_BOX_HEIGHT,
                Component.literal("Search"));
        searchBox.setMaxLength(64);
        searchBox.setValue(this.menu.getSearchQuery());
        searchBox.setResponder(this::onSearchChanged);
        this.addRenderableWidget(searchBox);

        sortButton = Button.builder(sortButtonLabel(), b -> cycleSort())
                .bounds(left + SORT_BUTTON_X, top + SEARCH_BOX_Y, SORT_BUTTON_WIDTH, SEARCH_BOX_HEIGHT).build();
        this.addRenderableWidget(sortButton);
    }

    private void onSearchChanged(String text)
    {
        this.menu.setSearchQuery(text);
        NetworkHandler.CHANNEL.sendToServer(new ServerboundSetTerminalSearchPacket(text));
    }

    private void cycleSort()
    {
        TerminalSortMode next = this.menu.getSortMode().next();
        this.menu.setSortMode(next);
        NetworkHandler.CHANNEL.sendToServer(new ServerboundSetTerminalSortPacket(next));
        sortButton.setMessage(sortButtonLabel());
    }

    private Component sortButtonLabel()
    {
        return Component.literal(this.menu.getSortMode().getLabel());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        int maxScroll = Math.max(0, this.menu.getPoolSlotCount() - TerminalMenu.VISIBLE_WINDOW);
        int newOffset = Mth.clamp(this.menu.getScrollOffset() - (int) Math.signum(delta) * TerminalMenu.POOL_COLUMNS, 0, maxScroll);
        if (newOffset != this.menu.getScrollOffset())
        {
            this.menu.scrollTo(newOffset);
            NetworkHandler.CHANNEL.sendToServer(new ServerboundScrollTerminalPacket(newOffset));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF8B7355);

        // Only draw frames for slots actually backed by connected capacity (member Storage Blocks +
        // bonus slots) - an unconnected Terminal should visibly show zero pool storage of its own,
        // not a full 9x5 grid of dead slots that implies built-in capacity it doesn't have. While a
        // search is active, "backed" instead means "matched the search" (see TerminalMenu.getPoolSlotCount).
        for (int i = 0; i < TerminalMenu.VISIBLE_WINDOW; i++)
        {
            if (!this.menu.isWindowSlotActive(i))
            {
                continue;
            }
            int x = TerminalMenu.POOL_GRID_X + (i % TerminalMenu.POOL_COLUMNS) * 18;
            int y = TerminalMenu.POOL_GRID_Y + (i / TerminalMenu.POOL_COLUMNS) * 18;
            drawSlotFrame(graphics, left, top, x, y);
        }

        for (int i = 0; i < 3; i++)
        {
            drawSlotFrame(graphics, left, top, TerminalMenu.UPGRADE_ROW_X + i * 18, TerminalMenu.UPGRADE_ROW_Y);
        }

        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                drawSlotFrame(graphics, left, top, TerminalMenu.PLAYER_INV_X + col * 18, TerminalMenu.PLAYER_INV_Y + row * 18);
            }
        }
        for (int col = 0; col < 9; col++)
        {
            drawSlotFrame(graphics, left, top, TerminalMenu.PLAYER_INV_X + col * 18, TerminalMenu.HOTBAR_Y);
        }

        if (this.menu.getPoolSlotCount() > TerminalMenu.VISIBLE_WINDOW)
        {
            drawScrollbar(graphics, left, top);
        }
    }

    private void drawScrollbar(GuiGraphics graphics, int left, int top)
    {
        int trackX = left + TerminalMenu.POOL_GRID_X + TerminalMenu.POOL_COLUMNS * 18 + 4;
        int trackY = top + TerminalMenu.POOL_GRID_Y;
        int trackHeight = TerminalMenu.POOL_VISIBLE_ROWS * 18 - 2;
        graphics.fill(trackX, trackY, trackX + SCROLLBAR_WIDTH, trackY + trackHeight, 0xFF373737);

        int totalSlots = this.menu.getPoolSlotCount();
        int maxScroll = Math.max(0, totalSlots - TerminalMenu.VISIBLE_WINDOW);
        int thumbHeight = Math.max(8, trackHeight * TerminalMenu.VISIBLE_WINDOW / totalSlots);
        int thumbY = trackY + (maxScroll == 0 ? 0 : (trackHeight - thumbHeight) * this.menu.getScrollOffset() / maxScroll);
        graphics.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFBBBBBB);
    }

    private void drawSlotFrame(GuiGraphics graphics, int left, int top, int slotX, int slotY)
    {
        graphics.fill(left + slotX - 1, top + slotY - 1, left + slotX + 17, top + slotY + 17, 0xFF373737);
        graphics.fill(left + slotX, top + slotY, left + slotX + 16, top + slotY + 16, 0xFF8B8B8B);
    }
}
