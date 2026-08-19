package com.arcanelens.item;

import com.arcanelens.capability.SkillTreeProvider;
import com.arcanelens.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Copy-shape of StorageSystemUnlockHandler/AssemblerUnlockHandler - gates the Overload Core and Warped
 * Catalyst recipes behind the Overload Ritual skill unlock, and separately gates the Warped Anchor (added
 * in a later step) behind Warped Attunement specifically, since that's a different (later, child) node.
 */
public class OverloadRitualUnlockHandler
{
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event)
    {
        ItemStack stack = event.getCrafting();
        if (!(event.getEntity() instanceof ServerPlayer player))
        {
            return;
        }

        boolean isRitualItem = stack.is(ModItems.OVERLOAD_CORE.get()) || stack.is(ModItems.WARPED_CATALYST.get());
        if (isRitualItem)
        {
            boolean unlocked = player.getCapability(SkillTreeProvider.CAPABILITY)
                    .map(cap -> cap.isOverloadRitualUnlocked()).orElse(false);
            if (!unlocked)
            {
                stack.shrink(stack.getCount());
                player.displayClientMessage(Component.literal(
                                "You haven't unlocked the Overload Ritual yet - purchase it in the Skill Tree first.")
                        .withStyle(ChatFormatting.RED), false);
            }
        }

        if (stack.is(ModItems.WARPED_ANCHOR.get()))
        {
            boolean unlocked = player.getCapability(SkillTreeProvider.CAPABILITY)
                    .map(cap -> cap.isWarpedAttunementUnlocked()).orElse(false);
            if (!unlocked)
            {
                stack.shrink(stack.getCount());
                player.displayClientMessage(Component.literal(
                                "You haven't unlocked Warped Attunement yet - purchase it in the Skill Tree first.")
                        .withStyle(ChatFormatting.RED), false);
            }
        }
    }
}
