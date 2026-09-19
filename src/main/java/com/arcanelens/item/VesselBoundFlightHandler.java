package com.arcanelens.item;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Abilities;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The Vessel-Bound Sleeping God set's payoff - the same Faith-scaled flight budget
 * SleepingGodFlightHandler grants, upgraded per the author's explicit request: no landing-grace window, no
 * hard post-exhaustion cooldown. Instead, "time used" continuously ticks back down toward 0 whenever the
 * player isn't currently flying, at Config.vesselBoundFlightRegenTicksPerRealTick real ticks per point
 * recovered - landing with the budget nearly spent just means a short wait before *some* flight time is
 * back, never a full lockout. Because the smithing upgrade consumes the original piece (see the 4
 * SmithingTransformRecipes), a player can only ever be wearing one full matching set at a time - this and
 * SleepingGodFlightHandler never contend for the same player.
 *
 * <p>Also grants the set's one genuinely new passive (not inherited from the original): full-set immunity
 * to Poison and Wither, alongside the flight grant rather than instead of it.</p>
 */
public class VesselBoundFlightHandler
{
    private static final Set<UUID> grantedFlight = new HashSet<>();
    private static final Map<UUID, Integer> flightTicksUsed = new HashMap<>();
    private static final Map<UUID, Integer> regenAccumulator = new HashMap<>();

    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !(event.player instanceof ServerPlayer player))
        {
            return;
        }

        if (!hasFullSet(player))
        {
            Abilities noSetAbilities = player.getAbilities();
            if (grantedFlight.contains(player.getUUID()) && !noSetAbilities.instabuild)
            {
                revoke(player, noSetAbilities);
            }
            clearState(player.getUUID());
            return;
        }

        // Immunity applies unconditionally while the full set is worn, independent of the flight budget.
        if (player.hasEffect(MobEffects.POISON))
        {
            player.removeEffect(MobEffects.POISON);
        }
        if (player.hasEffect(MobEffects.WITHER))
        {
            player.removeEffect(MobEffects.WITHER);
        }

        Abilities abilities = player.getAbilities();
        if (abilities.instabuild)
        {
            // Creative/spectator already has its own flight - never interfere with it.
            return;
        }

        UUID id = player.getUUID();
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
            // No regen progress accumulates mid-flight - resuming flight after a brief touchdown doesn't
            // get a "free" partial regen tick it didn't actually wait for.
            regenAccumulator.remove(id);
            int used = flightTicksUsed.getOrDefault(id, 0) + 1;
            if (used >= maxFlightTicks)
            {
                used = maxFlightTicks;
                // Ran out mid-flight - drop them out of flight immediately, same as running out of elytra
                // rockets (and matching SleepingGodFlightHandler's own equivalent cutoff).
                abilities.flying = false;
            }
            flightTicksUsed.put(id, used);
        }
        else
        {
            int used = flightTicksUsed.getOrDefault(id, 0);
            if (used > 0)
            {
                int accumulated = regenAccumulator.merge(id, 1, Integer::sum);
                if (accumulated >= Config.vesselBoundFlightRegenTicksPerRealTick)
                {
                    regenAccumulator.put(id, 0);
                    flightTicksUsed.put(id, used - 1);
                }
            }
        }
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
        flightTicksUsed.remove(id);
        regenAccumulator.remove(id);
    }

    private static boolean hasFullSet(ServerPlayer player)
    {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.VESSEL_BOUND_SLEEPING_GOD_HELMET.get())
                && (player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.VESSEL_BOUND_SLEEPING_GOD_CHESTPLATE.get())
                || player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.VESSEL_BOUND_ELYTRA_CHESTPLATE.get()))
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.VESSEL_BOUND_SLEEPING_GOD_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.VESSEL_BOUND_SLEEPING_GOD_BOOTS.get());
    }

    private static int getFaith(ServerPlayer player)
    {
        return player.getCapability(FaithProvider.CAPABILITY).map(cap -> cap.getFaith()).orElse(0);
    }
}
