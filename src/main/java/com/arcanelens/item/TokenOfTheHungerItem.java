package com.arcanelens.item;

import com.arcanelens.menu.TokenOfTheHungerMenu;
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

/** Right-click opens a simple choice GUI (TokenOfTheHungerMenu/Screen) letting the player pick between
 * The Hunger's Boon and The Hunger's Bindings - the token itself is consumed once a choice is made
 * (see ServerboundChooseHungerBoonPacket), not here. */
public class TokenOfTheHungerItem extends Item
{
    public TokenOfTheHungerItem(Properties properties)
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
                    (containerId, inventory, p) -> new TokenOfTheHungerMenu(containerId, inventory),
                    Component.literal("Token of the Hunger")));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
