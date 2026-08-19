package com.arcanelens.item;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.god.GodRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * War's real perk: permanent Strength scaling with pledge progress (capped), plus a Totem-of-Undying-
 * style save from death once progress reaches that cap - both gated the same way Fertility's perks
 * are, off IFaith.getPledgeProgress() rather than lifetime Faith (see FaithImpl's design notes).
 *
 * <p>The extra life mirrors vanilla's own checkTotemDeathProtection exactly (same effect grants,
 * same particle/sound broadcast byte 35, confirmed by decompiling LivingEntity) except the survival
 * health is 5 hearts (10.0F) instead of vanilla's 1, per this god's own design brief, and it's a
 * passive perk gated by pledge + progress + a cooldown instead of consuming a held item.</p>
 */
public class WarPerkHandler
{
    private static final int RECHECK_INTERVAL_TICKS = 20;
    private static final int STRENGTH_EFFECT_DURATION_TICKS = 60;

    private static final Map<UUID, Long> extraLifeCooldownUntil = new HashMap<>();

    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !(event.player instanceof ServerPlayer player))
        {
            return;
        }
        if (player.tickCount % RECHECK_INTERVAL_TICKS != 0)
        {
            return;
        }

        player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
            int amplifier = computeStrengthAmplifier(cap.getPledgedGod(), cap.getPledgeProgress());
            if (amplifier >= 0)
            {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, STRENGTH_EFFECT_DURATION_TICKS, amplifier, true, false, true));
            }
        });
    }

    private static int computeStrengthAmplifier(String pledgedGod, int pledgeProgress)
    {
        double rawLevel = Math.min(Config.warMaxStrengthLevel, (double) pledgeProgress / Config.warFaithPerStrengthLevel);
        if (pledgedGod.equals("war"))
        {
            return (int) rawLevel - 1;
        }
        if (pledgedGod.equals(GodRegistry.NEUTRAL_ID))
        {
            return (int) (rawLevel * Config.neutralWarStrengthScale) - 1;
        }
        return -1;
    }

    public static void onLivingDeath(LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
        {
            return;
        }

        Long cooldownUntil = extraLifeCooldownUntil.get(player.getUUID());
        if (cooldownUntil != null && player.level().getGameTime() < cooldownUntil)
        {
            return;
        }

        boolean eligible = player.getCapability(FaithProvider.CAPABILITY).map(cap ->
                cap.getPledgedGod().equals("war")
                        && cap.getPledgeProgress() >= Config.warFaithPerStrengthLevel * Config.warMaxStrengthLevel
        ).orElse(false);
        if (!eligible)
        {
            return;
        }

        event.setCanceled(true);
        player.setHealth(10.0F);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
        player.level().broadcastEntityEvent(player, (byte) 35);
        extraLifeCooldownUntil.put(player.getUUID(), player.level().getGameTime() + Config.warExtraLifeCooldownTicks);
    }
}
