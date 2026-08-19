package com.arcanelens.menu;

import com.arcanelens.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** Opened either by PledgeChoiceHandler (first Warped Hollow entry) or PackOfTheGodsItem (a later
 * swap) - same menu/screen/packet serve both flows, see ServerboundChoosePledgePacket. */
public class GodPledgeMenu extends AbstractContainerMenu
{
    public GodPledgeMenu(int containerId, Inventory playerInventory)
    {
        super(ModMenuTypes.GOD_PLEDGE.get(), containerId);
    }

    public GodPledgeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData)
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
