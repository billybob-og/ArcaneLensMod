package com.arcanelens.item;

import com.arcanelens.capability.SkillTreeProvider;
import com.arcanelens.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * The Arcane Assembler recipe stays visible and craftable-looking at all times (matching
 * StorageSystemUnlockHandler/ManaBoostHandler's precedent of hooking ItemCraftedEvent rather than
 * inventing a player-aware CustomRecipe) - this just voids the result and tells the player why if they
 * haven't purchased the Arcane Assembler skill yet. Ingredients are already consumed by the time this
 * event fires and there's no simple "uncraft" API, so this deliberately doesn't refund them.
 */
public class AssemblerUnlockHandler
{
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event)
    {
        ItemStack stack = event.getCrafting();
        if (!stack.is(ModItems.ARCANE_ASSEMBLER.get()) || !(event.getEntity() instanceof ServerPlayer player))
        {
            return;
        }

        boolean unlocked = player.getCapability(SkillTreeProvider.CAPABILITY)
                .map(cap -> cap.isArcaneAssemblerUnlocked()).orElse(false);
        if (unlocked)
        {
            return;
        }

        stack.shrink(stack.getCount());
        player.displayClientMessage(Component.literal(
                        "You haven't unlocked the Arcane Assembler yet - purchase it in the Skill Tree first.")
                .withStyle(ChatFormatting.RED), false);
    }
}
