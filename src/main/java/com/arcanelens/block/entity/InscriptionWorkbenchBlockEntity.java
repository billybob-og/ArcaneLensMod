package com.arcanelens.block.entity;

import com.arcanelens.item.DiamondEngraverItem;
import com.arcanelens.item.MagicLensItem;
import com.arcanelens.menu.InscriptionWorkbenchMenu;
import com.arcanelens.registry.ModBlockEntities;
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

public class InscriptionWorkbenchBlockEntity extends BlockEntity implements MenuProvider
{
    public static final int SLOT_LENS = 0;
    public static final int SLOT_ENGRAVER = 1;

    private final ItemStackHandler itemHandler = new ItemStackHandler(2)
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
                case SLOT_LENS -> stack.getItem() instanceof MagicLensItem;
                case SLOT_ENGRAVER -> stack.getItem() instanceof DiamondEngraverItem;
                default -> false;
            };
        }
    };

    private final LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    public InscriptionWorkbenchBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.INSCRIPTION_WORKBENCH.get(), pos, state);
    }

    public ItemStackHandler getItemHandler()
    {
        return itemHandler;
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("block.arcanelens.inscription_workbench");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player)
    {
        return new InscriptionWorkbenchMenu(containerId, playerInventory, this);
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

        // Migrate saves from before the Diamond Engraver slot existed: ItemStackHandler.deserializeNBT
        // restores whatever slot count was persisted, which would otherwise silently shrink this back to 1.
        if (itemHandler.getSlots() < 2)
        {
            ItemStack existingLens = itemHandler.getStackInSlot(SLOT_LENS);
            itemHandler.setSize(2);
            itemHandler.setStackInSlot(SLOT_LENS, existingLens);
        }
    }
}
