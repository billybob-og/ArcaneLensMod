package com.arcanelens.item;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Abilities;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The full Sleeping God armor set's payoff: grants creative-style flight while wearing it. Total
 * accumulated Faith (see FaithAltarBlockEntity) permanently caps how long a single flight can last
 * (Config.faithPerFlightSecond) - Faith is never spent by flying. After landing (whether the cap was hit
 * or the player stopped early), the ability needs Config.flightCooldownTicks to recharge before it can be
 * used again. Only ever grants/revokes the "mayfly" ability it itself gave out - never touches
 * creative/spectator players.
 */
public class SleepingGodFlightHandler
{
    private static final Set<UUID> grantedFlight = new HashSet<>();
    private static final Set<UUID> wasFlying = new HashSet<>();
    private static final Map<UUID, Integer> flightTicksUsed = new HashMap<>();
    private static final Map<UUID, Integer> cooldownTicksRemaining = new HashMap<>();
    private static final Map<UUID, Integer> landedGraceTicksRemaining = new HashMap<>();

    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !(event.player instanceof ServerPlayer player))
        {
            return;
        }

        Abilities abilities = player.getAbilities();
        if (abilities.instabuild)
        {
            // Creative/spectator already has its own flight - never interfere with it.
            return;
        }

        UUID id = player.getUUID();

        if (!hasFullSet(player))
        {
            if (grantedFlight.contains(id))
            {
                revoke(player, abilities);
            }
            clearState(id);
            return;
        }

        int cooldown = cooldownTicksRemaining.getOrDefault(id, 0);
        if (cooldown > 0)
        {
            cooldownTicksRemaining.put(id, cooldown - 1);
            if (grantedFlight.contains(id))
            {
                revoke(player, abilities);
            }
            return;
        }

        int maxFlightTicks = (Config.baseFlightSeconds + getFaith(player) / Config.faithPerFlightSecond) * 20;
        if (maxFlightTicks <= 0)
        {
            if (grantedFlight.contains(id))
            {
                revoke(player, abilities);
            }
            return;
        }

        if (!grantedFlight.contains(id))
        {
            abilities.mayfly = true;
            player.onUpdateAbilities();
            grantedFlight.add(id);
        }

        if (abilities.flying)
        {
            // Taking back off cancels any pending grace countdown - resume exactly where it left off.
            landedGraceTicksRemaining.remove(id);
            wasFlying.add(id);
            int used = flightTicksUsed.merge(id, 1, Integer::sum);
            if (used >= maxFlightTicks)
            {
                // Ran out mid-flight - drop them out of flight immediately, same as running out of elytra rockets.
                abilities.flying = false;
                startCooldown(player, abilities, id);
            }
        }
        else if (wasFlying.contains(id))
        {
            // Landed on their own before hitting the cap - give a short grace window before the cooldown
            // starts, so a brief touchdown (repositioning, ducking into a building) doesn't burn the whole
            // remaining flight time. Taking off again within the window resumes with time already used
            // still counted; letting the window expire while grounded starts the full cooldown.
            int grace = landedGraceTicksRemaining.getOrDefault(id, Config.flightLandedGraceTicks);
            if (grace <= 0)
            {
                startCooldown(player, abilities, id);
            }
            else
            {
                landedGraceTicksRemaining.put(id, grace - 1);
            }
        }
    }

    private static void startCooldown(ServerPlayer player, Abilities abilities, UUID id)
    {
        abilities.mayfly = false;
        player.onUpdateAbilities();
        grantedFlight.remove(id);
        wasFlying.remove(id);
        flightTicksUsed.remove(id);
        landedGraceTicksRemaining.remove(id);
        cooldownTicksRemaining.put(id, Config.flightCooldownTicks);
    }

    private static void revoke(ServerPlayer player, Abilities abilities)
    {
        abilities.mayfly = false;
        abilities.flying = false;
        player.onUpdateAbilities();
        grantedFlight.remove(player.getUUID());
    }

    private static void clearState(UUID id)
    {
        wasFlying.remove(id);
        flightTicksUsed.remove(id);
        cooldownTicksRemaining.remove(id);
        landedGraceTicksRemaining.remove(id);
    }

    private static boolean hasFullSet(ServerPlayer player)
    {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SLEEPING_GOD_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.SLEEPING_GOD_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.SLEEPING_GOD_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.SLEEPING_GOD_BOOTS.get());
    }

    private static int getFaith(ServerPlayer player)
    {
        return player.getCapability(FaithProvider.CAPABILITY).map(cap -> cap.getFaith()).orElse(0);
    }
}
