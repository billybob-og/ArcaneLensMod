package com.arcanelens.menu;

import com.arcanelens.block.entity.LensCombinerBlockEntity;
import com.arcanelens.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class LensCombinerMenu extends AbstractContainerMenu
{
    private final LensCombinerBlockEntity blockEntity;

    public LensCombinerMenu(int containerId, Inventory playerInventory, LensCombinerBlockEntity blockEntity)
    {
        super(ModMenuTypes.LENS_COMBINER.get(), containerId);
        this.blockEntity = blockEntity;

        IItemHandler handler = blockEntity.getItemHandler();
        this.addSlot(new SlotItemHandler(handler, LensCombinerBlockEntity.SLOT_BOTTOM, 44, 40));
        this.addSlot(new SlotItemHandler(handler, LensCombinerBlockEntity.SLOT_TOP, 44, 16));
        this.addSlot(new SlotItemHandler(handler, LensCombinerBlockEntity.SLOT_ORE, 80, 28));
        this.addSlot(new SlotItemHandler(handler, LensCombinerBlockEntity.SLOT_OUTPUT, 116, 28)
        {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack)
            {
                return false;
            }
        });

        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 184 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++)
        {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 242));
        }
    }

    public LensCombinerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData)
    {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    private static LensCombinerBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData)
    {
        BlockPos pos = extraData.readBlockPos();
        var be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof LensCombinerBlockEntity combiner)
        {
            return combiner;
        }
        throw new IllegalStateException("No LensCombinerBlockEntity at " + pos);
    }

    public LensCombinerBlockEntity getBlockEntity()
    {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return blockEntity.getBlockPos().distSqr(player.blockPosition()) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem())
        {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();
            if (index < 4)
            {
                if (!this.moveItemStackTo(stackInSlot, 4, 40, true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else
            {
                if (!this.moveItemStackTo(stackInSlot, 0, 3, false))
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
        }
        return result;
    }
}
