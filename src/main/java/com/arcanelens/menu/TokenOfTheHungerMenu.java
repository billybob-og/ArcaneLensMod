package com.arcanelens.menu;

import com.arcanelens.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** Opened directly from TokenOfTheHungerItem, not tied to any block - no position to look up. */
public class TokenOfTheHungerMenu extends AbstractContainerMenu
{
    public TokenOfTheHungerMenu(int containerId, Inventory playerInventory)
    {
        super(ModMenuTypes.TOKEN_OF_THE_HUNGER.get(), containerId);
    }

    public TokenOfTheHungerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData)
    {
        this(containerId, playerInventory);
    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        return ItemStack.EMPTY;
    }
}
