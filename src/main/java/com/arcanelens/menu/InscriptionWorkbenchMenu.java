package com.arcanelens.menu;

import com.arcanelens.block.entity.InscriptionWorkbenchBlockEntity;
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

public class InscriptionWorkbenchMenu extends AbstractContainerMenu
{
    private final InscriptionWorkbenchBlockEntity blockEntity;

    public InscriptionWorkbenchMenu(int containerId, Inventory playerInventory, InscriptionWorkbenchBlockEntity blockEntity)
    {
        super(ModMenuTypes.INSCRIPTION_WORKBENCH.get(), containerId);
        this.blockEntity = blockEntity;

        IItemHandler handler = blockEntity.getItemHandler();
        this.addSlot(new SlotItemHandler(handler, InscriptionWorkbenchBlockEntity.SLOT_LENS, 70, 20));
        this.addSlot(new SlotItemHandler(handler, InscriptionWorkbenchBlockEntity.SLOT_ENGRAVER, 98, 20));

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

    public InscriptionWorkbenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData)
    {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    private static InscriptionWorkbenchBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData)
    {
        BlockPos pos = extraData.readBlockPos();
        var be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof InscriptionWorkbenchBlockEntity workbench)
        {
            return workbench;
        }
        throw new IllegalStateException("No InscriptionWorkbenchBlockEntity at " + pos);
    }

    public ItemStack getLensStack()
    {
        return blockEntity.getItemHandler().getStackInSlot(InscriptionWorkbenchBlockEntity.SLOT_LENS);
    }

    public ItemStack getEngraverStack()
    {
        return blockEntity.getItemHandler().getStackInSlot(InscriptionWorkbenchBlockEntity.SLOT_ENGRAVER);
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
            if (index < 2)
            {
                if (!this.moveItemStackTo(stackInSlot, 2, 38, true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else
            {
                if (!this.moveItemStackTo(stackInSlot, 0, 2, false))
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
