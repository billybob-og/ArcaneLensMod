package com.arcanelens.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * One of a fixed set of on-screen "window" slots in a Terminal's scrolling pool grid. Its x/y and backing
 * windowIndex never change once created (see ScrollingPoolWindow for why) - scrolling instead changes
 * which real pool slot that shared window handler currently maps windowIndex to. isActive() reflects
 * whether the current scroll position maps this window cell to a real pool slot at all (false past the
 * end of a partially-full last page) - it gates both rendering (AbstractContainerScreen skips inactive
 * slots) and, via the mayPlace/mayPickup overrides below, interaction; TerminalMenu additionally guards
 * clicked()/quickMoveStack explicitly rather than trusting that plumbing alone.
 */
public class NetworkPoolSlot extends SlotItemHandler
{
    private final TerminalMenu menu;
    private final int windowIndex;

    public NetworkPoolSlot(IItemHandler window, int windowIndex, TerminalMenu menu, int x, int y)
    {
        super(window, windowIndex, x, y);
        this.menu = menu;
        this.windowIndex = windowIndex;
    }

    public int getWindowIndex()
    {
        return windowIndex;
    }

    @Override
    public boolean isActive()
    {
        return menu.isWindowSlotActive(windowIndex);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack)
    {
        return isActive() && super.mayPlace(stack);
    }

    @Override
    public boolean mayPickup(@NotNull Player player)
    {
        return isActive() && super.mayPickup(player);
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack)
    {
        return menu.getTerminal().getEffectiveSlotLimit(stack);
    }
}
