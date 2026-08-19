package com.arcanelens.item;

import com.arcanelens.menu.SkillTreeMenu;
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

/** Reusable key granted by the Hunger Idol exchange (see HungerIdolEntity) - right-click opens the skill
 * tree screen, same NetworkHooks.openScreen pattern as TokenOfTheHungerItem. Not consumed on use. */
public class HungersPactItem extends Item
{
    public HungersPactItem(Properties properties)
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
                    (containerId, inventory, p) -> new SkillTreeMenu(containerId, inventory),
                    Component.literal("The Hunger's Pact")));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
