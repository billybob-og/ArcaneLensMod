package com.arcanelens.item;

import com.arcanelens.capability.FaithProvider;
import com.arcanelens.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

/** Copy-shape of PledgeAltarUnlockHandler - the Theater Helmet is the one god-themed item that isn't
 * an altar, so it needs its own small gate rather than being covered by that class's altar-block-only
 * data-driven loop. */
public class TheaterHelmetUnlockHandler
{
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event)
    {
        ItemStack stack = event.getCrafting();
        if (!stack.is(ModItems.THEATER_HELMET.get()) || !(event.getEntity() instanceof ServerPlayer player))
        {
            return;
        }

        boolean pledgedToTheater = player.getCapability(FaithProvider.CAPABILITY)
                .map(cap -> cap.getPledgedGod().equals("theater")).orElse(false);
        if (pledgedToTheater)
        {
            return;
        }

        stack.shrink(stack.getCount());
        player.displayClientMessage(Component.literal(
                        "You haven't pledged to the Theater - this mask has nothing to offer you.")
                .withStyle(ChatFormatting.RED), false);
    }
}
