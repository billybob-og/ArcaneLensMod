package com.arcanelens.item;

import com.arcanelens.menu.GodPledgeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** Opens the same GodPledgeMenu/GodPledgeScreen a first-time entry into the Warped Hollow does (see
 * PledgeChoiceHandler) - the pack itself isn't consumed here, only inside
 * ServerboundChoosePledgePacket's handler, and only when the player already has a pledge (i.e. this
 * is a swap, not a first-time free choice). */
public class PackOfTheGodsItem extends Item
{
    public PackOfTheGodsItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer)
        {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (containerId, inventory, p) -> new GodPledgeMenu(containerId, inventory),
                    Component.literal("Pledge Your Faith")));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
