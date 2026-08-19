package com.arcanelens.menu;

import com.arcanelens.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** Opened directly from HungersPactItem, not tied to any block - no slots, same shape as TokenOfTheHungerMenu. */
public class SkillTreeMenu extends AbstractContainerMenu
{
    public SkillTreeMenu(int containerId, Inventory playerInventory)
    {
        super(ModMenuTypes.SKILL_TREE.get(), containerId);
    }

    public SkillTreeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData)
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
