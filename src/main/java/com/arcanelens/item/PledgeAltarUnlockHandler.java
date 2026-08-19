package com.arcanelens.item;

import com.arcanelens.capability.FaithProvider;
import com.arcanelens.god.GodDefinition;
import com.arcanelens.god.GodRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Copy-shape of StorageSystemUnlockHandler - a dedicated god's Faith Altar recipe stays visible and
 * craftable-looking at all times, this just voids the result and tells the player why if they haven't
 * pledged to that specific god. Once placed, an already-built altar keeps working regardless of a
 * later swap (see ServerboundChoosePledgePacket) - only future crafting is gated.
 *
 * <p>Data-driven off GodRegistry.GODS rather than one hardcoded check per god - each available
 * GodDefinition's altarBlock resolves to its BlockItem by registry name (the two always share a
 * name, per the ModBlocks/ModItems registration convention), so a future god's altar is
 * automatically covered here with zero changes to this class.</p>
 */
public class PledgeAltarUnlockHandler
{
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event)
    {
        ItemStack stack = event.getCrafting();
        if (!(event.getEntity() instanceof ServerPlayer player))
        {
            return;
        }

        for (GodDefinition god : GodRegistry.GODS)
        {
            if (!god.available() || god.altarBlock() == null)
            {
                continue;
            }

            Item altarItem = ForgeRegistries.ITEMS.getValue(ForgeRegistries.BLOCKS.getKey(god.altarBlock().get().get()));
            if (altarItem == null || !stack.is(altarItem))
            {
                continue;
            }

            boolean pledgedToThisGod = player.getCapability(FaithProvider.CAPABILITY)
                    .map(cap -> cap.getPledgedGod().equals(god.id())).orElse(false);
            if (pledgedToThisGod)
            {
                return;
            }

            stack.shrink(stack.getCount());
            player.displayClientMessage(Component.literal(
                            "You haven't pledged to " + god.displayName().getString() + " - this altar has nothing to offer you.")
                    .withStyle(ChatFormatting.RED), false);
            return;
        }
    }
}
