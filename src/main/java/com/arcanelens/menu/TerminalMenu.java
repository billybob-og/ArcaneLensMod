package com.arcanelens.menu;

import com.arcanelens.block.entity.TerminalBlockEntity;
import com.arcanelens.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Browses a Terminal's whole merged storage pool as one scrollable grid, plus its 3 fixed upgrade slots.
 * Only POOL_COLUMNS x POOL_VISIBLE_ROWS window Slot objects ever exist for the pool (vanilla Slot's x/y
 * are immutable in this Minecraft version, so slots can't be repositioned or rebound on scroll) - each is
 * bound to a ScrollingPoolWindow, which is what actually changes which real pool index a window cell
 * represents as scrollOffset changes. See ScrollingPoolWindow/NetworkPoolSlot for the mechanism.
 *
 * Slot index layout in `this.slots`: [0, UPGRADE_SLOT_COUNT) upgrades, then [poolWindowStart,
 * poolWindowStart + VISIBLE_WINDOW) pool window slots, then player inventory (36).
 */
public class TerminalMenu extends AbstractContainerMenu
{
    public static final int POOL_COLUMNS = 9;
    public static final int POOL_VISIBLE_ROWS = 5;
    public static final int POOL_GRID_X = 8;
    public static final int POOL_GRID_Y = 18;
    public static final int UPGRADE_ROW_X = 8;
    // Shifted down 15px from the original 118/145/203 to make room for the search box row just below
    // the pool grid (see TerminalScreen.SEARCH_BOX_Y).
    public static final int UPGRADE_ROW_Y = 133;
    public static final int PLAYER_INV_X = 8;
    public static final int PLAYER_INV_Y = 160;
    public static final int HOTBAR_Y = 218;
    public static final int VISIBLE_WINDOW = POOL_COLUMNS * POOL_VISIBLE_ROWS;
    private static final int UPGRADE_SLOT_COUNT = TerminalBlockEntity.UPGRADE_SLOTS;

    private final TerminalBlockEntity terminal;
    private final StorageNetworkItemHandler poolHandler;
    private final int poolWindowStartIndex;
    private final boolean remoteAccess;
    private int scrollOffset;
    private String searchQuery = "";
    private TerminalSortMode sortMode = TerminalSortMode.DEFAULT;

    public TerminalMenu(int containerId, Inventory playerInventory, TerminalBlockEntity terminal)
    {
        this(containerId, playerInventory, terminal, terminal.getHomeClusterMembers(playerInventory.player.level()));
    }

    public TerminalMenu(int containerId, Inventory playerInventory, TerminalBlockEntity terminal, List<BlockPos> memberPositions)
    {
        this(containerId, playerInventory, terminal, memberPositions, false);
    }

    /** remoteAccess bypasses the usual distance check in stillValid() - set only when this menu is opened
     * via a bound Warped Anchor (see TerminalBlockEntity.openRemotely), never by a normal in-person
     * right-click on the block, which keeps the standard "walk away and it closes" behavior everywhere
     * else. */
    public TerminalMenu(int containerId, Inventory playerInventory, TerminalBlockEntity terminal, List<BlockPos> memberPositions, boolean remoteAccess)
    {
        super(ModMenuTypes.TERMINAL.get(), containerId);
        this.terminal = terminal;
        this.remoteAccess = remoteAccess;
        this.poolHandler = new StorageNetworkItemHandler(playerInventory.player.level(), memberPositions, terminal);

        for (int i = 0; i < UPGRADE_SLOT_COUNT; i++)
        {
            this.addSlot(new SlotItemHandler(terminal.getUpgradeHandler(), i, UPGRADE_ROW_X + i * 18, UPGRADE_ROW_Y));
        }

        ScrollingPoolWindow window = new ScrollingPoolWindow(poolHandler, this, VISIBLE_WINDOW);
        this.poolWindowStartIndex = this.slots.size();
        for (int i = 0; i < VISIBLE_WINDOW; i++)
        {
            int x = POOL_GRID_X + (i % POOL_COLUMNS) * 18;
            int y = POOL_GRID_Y + (i / POOL_COLUMNS) * 18;
            this.addSlot(new NetworkPoolSlot(window, i, this, x, y));
        }

        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++)
        {
            this.addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * 18, HOTBAR_Y));
        }
    }

    public TerminalMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData)
    {
        this(containerId, playerInventory, getTerminal(playerInventory, extraData), readMembers(extraData));
    }

    private static TerminalBlockEntity getTerminal(Inventory playerInventory, FriendlyByteBuf extraData)
    {
        BlockPos pos = extraData.readBlockPos();
        if (playerInventory.player.level().getBlockEntity(pos) instanceof TerminalBlockEntity terminal)
        {
            return terminal;
        }
        throw new IllegalStateException("No TerminalBlockEntity at " + pos);
    }

    private static List<BlockPos> readMembers(FriendlyByteBuf extraData)
    {
        int count = extraData.readVarInt();
        List<BlockPos> members = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
        {
            members.add(extraData.readBlockPos());
        }
        return members;
    }

    public TerminalBlockEntity getTerminal()
    {
        return terminal;
    }

    /** Total slots the current view exposes - always getVisiblePoolIndices().size(), which is the full
     * pool size when no search is active (just reordered) or the match count while searching. */
    public int getPoolSlotCount()
    {
        return getVisiblePoolIndices().size();
    }

    public int getScrollOffset()
    {
        return scrollOffset;
    }

    public boolean isWindowSlotActive(int windowIndex)
    {
        return windowIndex + scrollOffset < getPoolSlotCount();
    }

    public String getSearchQuery()
    {
        return searchQuery;
    }

    public TerminalSortMode getSortMode()
    {
        return sortMode;
    }

    /** The window-index-to-real-pool-index order the grid displays, recomputed live every call rather
     * than cached (matching this codebase's "always recompute live" convention elsewhere -
     * StorageNetworkManager's BFS - the pool is small enough that this is cheap). Occupied slots always
     * come first (in their underlying pool order), so an item that lands in some arbitrary physical
     * Storage Block - e.g. a hopper feeding a member block directly, bypassing this Terminal entirely -
     * still shows up at the front of the grid instead of wherever that block happens to sit in the
     * cluster's BFS order. While a search is active, only matching occupied slots are shown at all (no
     * trailing empty slots), so scrolling doesn't run past the last match. Within the occupied portion,
     * sortMode layers an additional ordering on top (see TerminalSortMode) - empty slots are never sorted,
     * they always trail last regardless of sortMode. */
    public List<Integer> getVisiblePoolIndices()
    {
        String needle = searchQuery.isEmpty() ? null : searchQuery.toLowerCase(Locale.ROOT);
        List<Integer> occupied = new ArrayList<>();
        List<Integer> empty = new ArrayList<>();
        for (int i = 0; i < poolHandler.getSlots(); i++)
        {
            ItemStack stack = poolHandler.getStackInSlot(i);
            if (stack.isEmpty())
            {
                if (needle == null)
                {
                    empty.add(i);
                }
            }
            else if (needle == null || stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle))
            {
                occupied.add(i);
            }
        }

        Comparator<Integer> comparator = switch (sortMode)
        {
            case NAME -> Comparator.comparing(i -> poolHandler.getStackInSlot(i).getHoverName().getString().toLowerCase(Locale.ROOT));
            case COUNT -> Comparator.<Integer>comparingInt(i -> poolHandler.getStackInSlot(i).getCount()).reversed();
            case DEFAULT -> null;
        };
        if (comparator != null)
        {
            occupied.sort(comparator);
        }

        occupied.addAll(empty);
        return occupied;
    }

    /** Re-clamps against this menu's own known pool size (never trusts a raw client-sent value). Safe to
     * call identically from both the client (immediate local feedback on scroll, see TerminalScreen) and
     * the server (the scroll packet's handler) - no Slot repositioning happens here at all; every window
     * slot's rendered content simply reflects whatever ScrollingPoolWindow resolves next frame/tick. */
    public void scrollTo(int newOffset)
    {
        int maxOffset = Math.max(0, getPoolSlotCount() - VISIBLE_WINDOW);
        scrollOffset = Math.max(0, Math.min(newOffset, maxOffset));
    }

    /** Called from both the client (typing in the search box, see TerminalScreen) and the server (the
     * search packet's handler) - scroll resets to the top of the new filtered results, since an old
     * offset would almost never make sense against a different-sized filtered list. */
    public void setSearchQuery(String query)
    {
        this.searchQuery = query == null ? "" : query;
        scrollOffset = 0;
    }

    /** Called from both the client (clicking the sort button, see TerminalScreen) and the server (the
     * sort packet's handler) - scroll resets to the top, same reasoning as setSearchQuery: an old offset
     * makes little sense once every item's position in the grid has potentially moved. */
    public void setSortMode(TerminalSortMode sortMode)
    {
        this.sortMode = sortMode == null ? TerminalSortMode.DEFAULT : sortMode;
        scrollOffset = 0;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return remoteAccess || terminal.getBlockPos().distSqr(player.blockPosition()) <= 64.0;
    }

    /** Defensive redundancy alongside NetworkPoolSlot.isActive(): even if isActive() somehow isn't fully
     * respected by click dispatch (unverified without decompiled source, see plan notes), a scrolled-past
     * window slot can never be interacted with through this entry point either. */
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player)
    {
        if (slotId >= 0 && slotId < this.slots.size())
        {
            Slot slot = this.slots.get(slotId);
            if (slot instanceof NetworkPoolSlot poolSlot && !poolSlot.isActive())
            {
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem())
        {
            return result;
        }

        ItemStack stackInSlot = slot.getItem();
        result = stackInSlot.copy();
        int poolWindowEnd = poolWindowStartIndex + VISIBLE_WINDOW;
        int playerInvStart = poolWindowEnd;
        int playerInvEnd = playerInvStart + 36;

        if (index < playerInvStart)
        {
            // From an upgrade slot or a pool window slot - into the player inventory.
            if (!this.moveItemStackTo(stackInSlot, playerInvStart, playerInvEnd, true))
            {
                return ItemStack.EMPTY;
            }
        }
        else
        {
            // From the player inventory/hotbar - try the upgrade slots first (harmless no-op for
            // non-upgrade items, since upgradeHandler.isItemValid rejects them), then the pool as a
            // whole via the real backing handler - not just the currently visible scroll window's
            // Slots, so a shift-click always finds room anywhere in the pool (merging into existing
            // partial stacks first, then the next empty slot) instead of failing just because the
            // visible window happens to be full while space exists elsewhere in the pool.
            this.moveItemStackTo(stackInSlot, 0, UPGRADE_SLOT_COUNT, false);
            if (!stackInSlot.isEmpty())
            {
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(poolHandler, stackInSlot.copy(), false);
                stackInSlot.setCount(remainder.getCount());
            }
            if (stackInSlot.getCount() == result.getCount())
            {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty())
        {
            slot.set(ItemStack.EMPTY);
        }
        else
        {
            slot.setChanged();
        }
        return result;
    }
}
