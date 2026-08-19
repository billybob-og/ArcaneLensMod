package com.arcanelens.item;

import com.arcanelens.Config;
import com.arcanelens.god.GodRegistry;
import com.arcanelens.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;

/**
 * The Theater Helmet's always-on passive perks while worn (no Faith cost, unlike the 3 active
 * abilities - see TheaterAbilityHandler): no fall damage ("Graceful Fall"), a 10% dodge chance
 * compensating for the mask's zero armor value, and a 15% chance for nearby hostile mobs to briefly
 * lose their target ("Audience's Gaze"). All three (and the 3 active abilities) additionally require
 * the wearer to be CURRENTLY pledged to Theater, not just wearing the mask - a later swap to a
 * different god leaves the mask a purely cosmetic zero-armor helmet until they swap back. The
 * hidden-nametag perk is client-render-only, see client/TheaterNametagHandler - it isn't pledge-gated
 * yet since the wearer's pledge isn't synced to other players' clients at all (only to the wearer's
 * own), a limitation worth revisiting if that perk needs the same treatment.
 */
public class TheaterHelmetPerkHandler
{
    private static final int RECHECK_INTERVAL_TICKS = 10;

    public static void onLivingFall(LivingFallEvent event)
    {
        if (event.getEntity() instanceof Player player && isWearingTheaterHelmet(player) && GodRegistry.isPledgedTo(player, "theater"))
        {
            event.setDamageMultiplier(0.0F);
        }
    }

    public static void onLivingAttack(LivingAttackEvent event)
    {
        if (!(event.getEntity() instanceof Player player) || !isWearingTheaterHelmet(player) || !GodRegistry.isPledgedTo(player, "theater"))
        {
            return;
        }
        if (player.getRandom().nextDouble() < Config.theaterDodgeChance)
        {
            event.setCanceled(true);
        }
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !(event.player instanceof ServerPlayer player))
        {
            return;
        }
        if (player.tickCount % RECHECK_INTERVAL_TICKS != 0 || !isWearingTheaterHelmet(player) || !GodRegistry.isPledgedTo(player, "theater"))
        {
            return;
        }
        if (!(player.level() instanceof ServerLevel level))
        {
            return;
        }

        AABB nearby = player.getBoundingBox().inflate(Config.theaterAggroLossRadius);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, nearby))
        {
            if (mob.getTarget() == player && mob.getRandom().nextDouble() < Config.theaterAggroLossChance)
            {
                mob.setTarget(null);
            }
        }
    }

    public static boolean isWearingTheaterHelmet(LivingEntity entity)
    {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.THEATER_HELMET.get());
    }

    public static boolean isEmptyHanded(Player player)
    {
        return player.getMainHandItem().isEmpty();
    }
}
