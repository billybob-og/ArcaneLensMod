package com.arcanelens.menu;

import com.arcanelens.block.entity.CommandTriggerBlockEntity;
import com.arcanelens.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class CommandTriggerMenu extends AbstractContainerMenu
{
    private final CommandTriggerBlockEntity blockEntity;

    public CommandTriggerMenu(int containerId, Inventory playerInventory, CommandTriggerBlockEntity blockEntity)
    {
        super(ModMenuTypes.COMMAND_TRIGGER.get(), containerId);
        this.blockEntity = blockEntity;
    }

    public CommandTriggerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData)
    {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    private static CommandTriggerBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData)
    {
        BlockPos pos = extraData.readBlockPos();
        var be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof CommandTriggerBlockEntity trigger)
        {
            return trigger;
        }
        throw new IllegalStateException("No CommandTriggerBlockEntity at " + pos);
    }

    public CommandTriggerBlockEntity getBlockEntity()
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
        return ItemStack.EMPTY;
    }
}
