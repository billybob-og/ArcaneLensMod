package com.arcanelens.god;

import com.arcanelens.capability.FaithProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

/** With fire/lava/void loss prevented outright (see GodDropVoidRescueHandler and each drop's own
 * fireResistant() property), durability breaking is the main way a player can actually lose a god's
 * unique drop - tracked here so AbstractGodBossEntity's death hook knows a replacement is earned. */
public class GodDropLossHandler
{
    public static void onPlayerDestroyItem(PlayerDestroyItemEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
        {
            return;
        }

        for (GodDefinition god : GodRegistry.GODS)
        {
            if (god.hasBossContent() && event.getOriginal().is(god.uniqueDrop().get().get()))
            {
                player.getCapability(FaithProvider.CAPABILITY)
                        .ifPresent(cap -> cap.incrementGodDropsBroken(god.id()));
                return;
            }
        }
    }
}
