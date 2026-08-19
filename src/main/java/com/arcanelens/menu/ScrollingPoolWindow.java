package com.arcanelens.menu;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;

/**
 * Translates a small fixed set of on-screen "window" slot indices (0..windowSize-1) into positions
 * within the full virtual pool (StorageNetworkItemHandler), offset by the owning TerminalMenu's current
 * scrollOffset. Exists because vanilla Slot's x/y (and SlotItemHandler's backing index) are immutable
 * once constructed in this Minecraft version, so scrolling can't reposition or rebind Slot objects at
 * all. Instead, a fixed number of Slot objects exist forever at fixed screen positions, and THIS class is
 * what changes which real pool slot each one currently represents - purely by re-reading scrollOffset on
 * every call, so no Slot object ever needs to change once created.
 */
public class ScrollingPoolWindow implements IItemHandlerModifiable
{
    private final StorageNetworkItemHandler poolHandler;
    private final TerminalMenu menu;
    private final int windowSize;

    public ScrollingPoolWindow(StorageNetworkItemHandler poolHandler, TerminalMenu menu, int windowSize)
    {
        this.poolHandler = poolHandler;
        this.menu = menu;
        this.windowSize = windowSize;
    }

    /** Indirects through the menu's current display order (occupied slots first, then empty ones, see
     * TerminalMenu.getVisiblePoolIndices) rather than a direct 1:1 offset - this is the ONLY place that
     * indirection happens, so every accessor below stays correct automatically. Returns -1 (naturally
     * rejected by inRange) if this window position is past the end of the current view (e.g. scrolled
     * past the last search match). */
    private int realIndex(int windowIndex)
    {
        List<Integer> displayOrder = menu.getVisiblePoolIndices();
        int viewIndex = windowIndex + menu.getScrollOffset();
        return viewIndex >= 0 && viewIndex < displayOrder.size() ? displayOrder.get(viewIndex) : -1;
    }

    private boolean inRange(int windowIndex)
    {
        int real = realIndex(windowIndex);
        return real >= 0 && real < poolHandler.getSlots();
    }

    @Override
    public int getSlots()
    {
        return windowSize;
    }

    @Override
    public ItemStack getStackInSlot(int windowIndex)
    {
        return inRange(windowIndex) ? poolHandler.getStackInSlot(realIndex(windowIndex)) : ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int windowIndex, ItemStack stack)
    {
        if (inRange(windowIndex))
        {
            poolHandler.setStackInSlot(realIndex(windowIndex), stack);
        }
    }

    @Override
    public ItemStack insertItem(int windowIndex, ItemStack stack, boolean simulate)
    {
        return inRange(windowIndex) ? poolHandler.insertItem(realIndex(windowIndex), stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int windowIndex, int amount, boolean simulate)
    {
        return inRange(windowIndex) ? poolHandler.extractItem(realIndex(windowIndex), amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int windowIndex)
    {
        return inRange(windowIndex) ? poolHandler.getSlotLimit(realIndex(windowIndex)) : 0;
    }

    @Override
    public boolean isItemValid(int windowIndex, ItemStack stack)
    {
        return inRange(windowIndex) && poolHandler.isItemValid(realIndex(windowIndex), stack);
    }
}
