package com.arcanelens.block.entity;

import com.arcanelens.Config;
import com.arcanelens.registry.ModBlockEntities;
import com.arcanelens.util.PedestalLinker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SoulFeederBlockEntity extends BlockEntity
{
    public static final int SLOT_COUNT = 9;
    private static final int FEED_INTERVAL_TICKS = 10;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
        }
    };

    private final LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    private int rescanTimer;
    private int feedTimer;
    private List<BlockPos> linkedSoulPedestals = Collections.emptyList();

    public SoulFeederBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.SOUL_FEEDER.get(), pos, state);
    }

    public ItemStackHandler getItemHandler()
    {
        return itemHandler;
    }

    public void tick(Level level, BlockPos pos, BlockState state)
    {
        rescanTimer++;
        if (rescanTimer >= Config.linkRescanIntervalTicks)
        {
            rescanTimer = 0;
            linkedSoulPedestals = PedestalLinker.findLinkedSoulPedestals(level, pos, Config.linkRadius);
        }

        feedTimer++;
        if (feedTimer < FEED_INTERVAL_TICKS)
        {
            return;
        }
        feedTimer = 0;

        for (BlockPos linkedPos : linkedSoulPedestals)
        {
            if (level.getBlockEntity(linkedPos) instanceof SoulPedestalBlockEntity soulPedestal
                    && soulPedestal.getConversionItem().isEmpty())
            {
                ItemStack extracted = extractOne();
                if (!extracted.isEmpty())
                {
                    soulPedestal.setConversionItem(extracted);
                }
            }
        }
    }

    private ItemStack extractOne()
    {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            ItemStack extracted = itemHandler.extractItem(slot, 1, false);
            if (!extracted.isEmpty())
            {
                return extracted;
            }
        }
        return ItemStack.EMPTY;
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
    }
}
