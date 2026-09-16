package com.arcanelens.menu;

import com.arcanelens.block.entity.GodChallengeAltarBlockEntity;
import com.arcanelens.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class GodChallengeMenu extends AbstractContainerMenu
{
    private final GodChallengeAltarBlockEntity blockEntity;

    public GodChallengeMenu(int containerId, Inventory playerInventory, GodChallengeAltarBlockEntity blockEntity)
    {
        super(ModMenuTypes.GOD_CHALLENGE.get(), containerId);
        this.blockEntity = blockEntity;
    }

    public GodChallengeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData)
    {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    private static GodChallengeAltarBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData)
    {
        BlockPos pos = extraData.readBlockPos();
        var be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof GodChallengeAltarBlockEntity altar)
        {
            return altar;
        }
        throw new IllegalStateException("No GodChallengeAltarBlockEntity at " + pos);
    }

    public GodChallengeAltarBlockEntity getBlockEntity()
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
