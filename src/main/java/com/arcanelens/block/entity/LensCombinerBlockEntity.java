package com.arcanelens.block.entity;

import com.arcanelens.item.MagicLensItem;
import com.arcanelens.menu.LensCombinerMenu;
import com.arcanelens.registry.ModBlockEntities;
import com.arcanelens.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LensCombinerBlockEntity extends BlockEntity implements MenuProvider
{
    public static final int SLOT_BOTTOM = 0;
    public static final int SLOT_TOP = 1;
    public static final int SLOT_ORE = 2;
    public static final int SLOT_OUTPUT = 3;

    private final ItemStackHandler itemHandler = new ItemStackHandler(4)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack)
        {
            return switch (slot)
            {
                case SLOT_BOTTOM, SLOT_TOP -> stack.getItem() instanceof MagicLensItem;
                case SLOT_ORE -> stack.getItem() == ModItems.RAW_ARCANE_MATERIAL.get();
                default -> false;
            };
        }
    };

    private final LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    public LensCombinerBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.LENS_COMBINER.get(), pos, state);
    }

    public ItemStackHandler getItemHandler()
    {
        return itemHandler;
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("block.arcanelens.lens_combiner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player)
    {
        return new LensCombinerMenu(containerId, playerInventory, this);
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
