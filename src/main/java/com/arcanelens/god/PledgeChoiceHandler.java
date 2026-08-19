package com.arcanelens.god;

import com.arcanelens.capability.FaithProvider;
import com.arcanelens.menu.GodPledgeMenu;
import com.arcanelens.worldgen.ModDimensions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Opens GodPledgeScreen the first time a player reaches the Warped Hollow - exact dual-event trigger
 * shape VesselLairPlacer already uses for the same "first time reaching this dimension" moment
 * (onPlayerChangedDimension for an ordinary portal trip, onPlayerLoggedIn for the relog/reconnect gap
 * that trigger alone can't cover). A short delay (OPEN_DELAY_TICKS) avoids popping the screen up
 * literally mid-portal-transition. */
public class PledgeChoiceHandler
{
    private static final long OPEN_DELAY_TICKS = 30;

    private static final Map<UUID, Long> pendingOpenTick = new HashMap<>();

    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player && event.getTo() == ModDimensions.BROKEN_VESSEL_KEY)
        {
            scheduleIfNeeded(player);
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player && player.level().dimension() == ModDimensions.BROKEN_VESSEL_KEY)
        {
            scheduleIfNeeded(player);
        }
    }

    private static void scheduleIfNeeded(ServerPlayer player)
    {
        boolean alreadyChosen = player.getCapability(FaithProvider.CAPABILITY)
                .map(cap -> !cap.getPledgedGod().isEmpty()).orElse(true);
        if (alreadyChosen)
        {
            return;
        }
        pendingOpenTick.put(player.getUUID(), player.level().getGameTime() + OPEN_DELAY_TICKS);
    }

    /** Naturally idempotent, same guarantee VesselLairPlacer relies on for the tower itself - closing
     * the screen without choosing leaves pledgedGod empty, so the next qualifying dimension entry or
     * login reschedules it. */
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !(event.player instanceof ServerPlayer player))
        {
            return;
        }

        Long targetTick = pendingOpenTick.get(player.getUUID());
        if (targetTick == null || player.level().getGameTime() < targetTick)
        {
            return;
        }

        pendingOpenTick.remove(player.getUUID());
        boolean alreadyChosen = player.getCapability(FaithProvider.CAPABILITY)
                .map(cap -> !cap.getPledgedGod().isEmpty()).orElse(true);
        if (alreadyChosen || player.level().dimension() != ModDimensions.BROKEN_VESSEL_KEY)
        {
            // Choice made some other way in the meantime (e.g. a Pack of the Gods), or the player
            // already left the dimension - no-op rather than opening a stale/irrelevant screen.
            return;
        }

        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (containerId, inventory, p) -> new GodPledgeMenu(containerId, inventory),
                Component.literal("Pledge Your Faith")));
    }
}
