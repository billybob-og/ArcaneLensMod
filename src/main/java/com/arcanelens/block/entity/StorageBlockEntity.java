package com.arcanelens.block.entity;

import com.arcanelens.Config;
import com.arcanelens.registry.ModBlockEntities;
import com.arcanelens.util.StorageNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A single "dumb" storage unit - it knows nothing about clusters, neighbors, or networks. All cluster
 * discovery lives in StorageNetworkManager; a Terminal's virtual inventory delegates straight through to
 * whichever member StorageBlockEntity backs a given pool slot, by direct reference, never by copying.
 *
 * <p>insertItem is overridden (rather than relying on ItemStackHandler's default, which clamps to
 * stack.getMaxStackSize()) so a Stack Upgrade's bonus applies even to items inserted directly into this
 * block's own capability - e.g. a hopper feeding it straight from above, entirely bypassing the owning
 * Terminal's pool logic. The effective limit is looked up live via
 * StorageNetworkManager.findOwningTerminal each call rather than cached, matching this class's existing
 * "no cluster state cached here" design - falls back to the item's own vanilla max if unclaimed.</p>
 */
public class StorageBlockEntity extends BlockEntity
{
    private final ItemStackHandler itemHandler = new ItemStackHandler(Config.slotsPerStorageBlock)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot)
        {
            // Real enforcement is in insertItem below (matches StorageNetworkItemHandler's own
            // pattern) - this is just the handler-interface-level ceiling.
            return 6400;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
        {
            if (stack.isEmpty())
            {
                return stack;
            }

            int limit = getEffectiveLimit(stack);
            ItemStack existing = getStackInSlot(slot);

            if (existing.isEmpty())
            {
                int toInsert = Math.min(stack.getCount(), limit);
                if (!simulate)
                {
                    setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(stack, toInsert));
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
                setStackInSlot(slot, updated);
            }
            return toAdd == stack.getCount() ? ItemStack.EMPTY
                    : ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - toAdd);
        }
    };

    private int getEffectiveLimit(ItemStack stack)
    {
        if (level instanceof ServerLevel serverLevel)
        {
            var terminal = StorageNetworkManager.findOwningTerminal(serverLevel, worldPosition);
            if (terminal != null)
            {
                return terminal.getEffectiveSlotLimit(stack);
            }
        }
        return stack.getMaxStackSize();
    }

    private final LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    public StorageBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.STORAGE_BLOCK.get(), pos, state);
    }

    public ItemStackHandler getItemHandler()
    {
        return itemHandler;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == ForgeCapabilities.ITEM_HANDLER)
        {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        resizeToConfig();
    }

    /** Resizes the handler to the current Config.slotsPerStorageBlock, snapshotting existing contents
     * first and dropping anything that no longer fits - extends InscriptionWorkbenchBlockEntity.load()'s
     * grow-only migration idiom (ItemStackHandler.setSize clears the backing array) to also handle a
     * config value being lowered while slots are occupied. */
    private void resizeToConfig()
    {
        int target = Config.slotsPerStorageBlock;
        int current = itemHandler.getSlots();
        if (current == target)
        {
            return;
        }

        ItemStack[] existing = new ItemStack[current];
        for (int i = 0; i < current; i++)
        {
            existing[i] = itemHandler.getStackInSlot(i);
        }

        itemHandler.setSize(target);
        for (int i = 0; i < Math.min(current, target); i++)
        {
            itemHandler.setStackInSlot(i, existing[i]);
        }

        if (target < current && level instanceof ServerLevel serverLevel)
        {
            for (int i = target; i < current; i++)
            {
                if (!existing[i].isEmpty())
                {
                    Containers.dropItemStack(serverLevel, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), existing[i]);
                }
            }
        }
    }
}
