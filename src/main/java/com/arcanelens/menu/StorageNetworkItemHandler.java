package com.arcanelens.menu;

import com.arcanelens.block.entity.StorageBlockEntity;
import com.arcanelens.block.entity.TerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A delegating "virtual" inventory presenting a Terminal's whole merged pool - every member Storage
 * Block's real ItemStackHandler, plus the Terminal's own upgrade-granted bonus slots - as one flat,
 * contiguous slot range. Never copies or merges item state: every accessor resolves to the real backing
 * handler and delegates straight through, so breaking a member block only ever drops that block's own
 * real contents (see StorageBlock.onRemove) - there's no "who owned which item" bookkeeping to get wrong.
 *
 * insert/extract are hand-written against raw getStackInSlot/setStackInSlot rather than
 * ItemStackHandler.insertItem/extractItem, which internally clamp to the item's own vanilla max stack
 * size - that clamp would make a Stack Upgrade's bonus impossible to apply. The effective limit always
 * comes from TerminalBlockEntity.getEffectiveSlotLimit, the same helper NetworkPoolSlot uses for its own
 * getMaxStackSize(), so the two can never silently disagree.
 *
 * If a member block is broken by another player while this handler's owning menu session is still open,
 * that segment's isRemoved() check makes every slot in it resolve to nothing for the rest of the session
 * (permanently inert/empty) rather than attempting a live reactive rebuild.
 */
public class StorageNetworkItemHandler implements IItemHandlerModifiable
{
    private record Segment(StorageBlockEntity storage, int slotsInSegment) {}

    private record Resolved(IItemHandlerModifiable handler, int localIndex) {}

    private final List<Segment> segments = new ArrayList<>();
    private final TerminalBlockEntity terminal;
    private final int[] segmentStartIndex;

    public StorageNetworkItemHandler(Level level, List<BlockPos> memberPositions, TerminalBlockEntity terminal)
    {
        this.terminal = terminal;

        for (BlockPos pos : memberPositions)
        {
            if (level.getBlockEntity(pos) instanceof StorageBlockEntity storage)
            {
                segments.add(new Segment(storage, storage.getItemHandler().getSlots()));
            }
        }

        segmentStartIndex = new int[segments.size() + 1];
        int running = 0;
        for (int i = 0; i < segments.size(); i++)
        {
            segmentStartIndex[i] = running;
            running += segments.get(i).slotsInSegment();
        }
        segmentStartIndex[segments.size()] = running;
    }

    /** Recomputed live every call rather than cached - terminal.getBonusSlotHandler() can shrink while
     * this menu session is still open (a player removing a Slot Upgrade), and a cached total would let
     * callers iterate past the bonus handler's real current size, crashing on an out-of-range index. */
    @Override
    public int getSlots()
    {
        return segmentStartIndex[segments.size()] + terminal.getBonusSlotHandler().getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        Resolved r = resolve(slot);
        return r == null ? ItemStack.EMPTY : r.handler().getStackInSlot(r.localIndex());
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        Resolved r = resolve(slot);
        if (r != null)
        {
            r.handler().setStackInSlot(r.localIndex(), stack);
        }
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        Resolved r = resolve(slot);
        if (r == null || stack.isEmpty())
        {
            return stack;
        }

        ItemStack existing = r.handler().getStackInSlot(r.localIndex());
        int limit = terminal.getEffectiveSlotLimit(stack);

        if (existing.isEmpty())
        {
            int toInsert = Math.min(stack.getCount(), limit);
            if (!simulate)
            {
                r.handler().setStackInSlot(r.localIndex(), ItemHandlerHelper.copyStackWithSize(stack, toInsert));
            }
            return toInsert == stack.getCount() ? ItemStack.EMPTY
                    : ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - toInsert);
        }

        if (!ItemHandlerHelper.canItemStacksStack(existing, stack))
        {
            return stack;
        }

        int room = limit - existing.getCount();
        if (room <= 0)
        {
            return stack;
        }

        int toAdd = Math.min(room, stack.getCount());
        if (!simulate)
        {
            ItemStack updated = existing.copy();
            updated.grow(toAdd);
            r.handler().setStackInSlot(r.localIndex(), updated);
        }
        return toAdd == stack.getCount() ? ItemStack.EMPTY
                : ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - toAdd);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        Resolved r = resolve(slot);
        if (r == null || amount <= 0)
        {
            return ItemStack.EMPTY;
        }

        ItemStack existing = r.handler().getStackInSlot(r.localIndex());
        if (existing.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        int toExtract = Math.min(amount, existing.getCount());
        ItemStack result = existing.copy();
        result.setCount(toExtract);

        if (!simulate)
        {
            if (toExtract == existing.getCount())
            {
                r.handler().setStackInSlot(r.localIndex(), ItemStack.EMPTY);
            }
            else
            {
                ItemStack remainder = existing.copy();
                remainder.shrink(toExtract);
                r.handler().setStackInSlot(r.localIndex(), remainder);
            }
        }
        return result;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        // Real enforcement lives in insertItem/NetworkPoolSlot.getMaxStackSize via
        // TerminalBlockEntity.getEffectiveSlotLimit - this is just the handler-interface-level ceiling,
        // set generously high so nothing upstream clamps before the real per-item limit is applied.
        return 6400;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        return true;
    }

    private Resolved resolve(int slot)
    {
        if (slot < 0)
        {
            return null;
        }
        for (int i = 0; i < segments.size(); i++)
        {
            Segment segment = segments.get(i);
            int start = segmentStartIndex[i];
            int end = start + segment.slotsInSegment();
            if (slot >= start && slot < end)
            {
                return segment.storage().isRemoved() ? null : new Resolved(segment.storage().getItemHandler(), slot - start);
            }
        }

        // Bounds-checked against the bonus handler's CURRENT size (not a cached total) - it can shrink
        // while this menu session is open (a player removing a Slot Upgrade), same reasoning as getSlots().
        int bonusStart = segmentStartIndex[segments.size()];
        int bonusIndex = slot - bonusStart;
        if (bonusIndex >= 0 && bonusIndex < terminal.getBonusSlotHandler().getSlots())
        {
            return new Resolved(terminal.getBonusSlotHandler(), bonusIndex);
        }
        return null;
    }
}
