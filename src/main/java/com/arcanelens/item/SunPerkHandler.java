package com.arcanelens.item;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.god.GodRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;

import java.util.UUID;

/**
 * The Sun's real perk: by day, Night Vision + Glowing + Haste I + Speed I (all flat, per the user's
 * own buffed-up design note) plus a scaling Armor bonus; by night, a fixed-severity tradeoff
 * (Slowness I/Hunger I/Weakness I) for a Sun-pledged player specifically - neutral players get the
 * daytime taste but never the night cost, since they haven't actually committed. The Armor bonus is
 * capped/scaled the same way Fertility's bonus hearts are, off IFaith.getPledgeProgress() rather than
 * lifetime Faith; the flat effects (Night Vision/Glowing/Haste/Speed) aren't scaled at all, matching
 * how Night Vision was already applied at full strength to both pledged and neutral players.
 *
 * <p>Note: MobEffects.HUNGER here is the plain vanilla food-drain effect, unrelated to "the Hunger"
 * god/pledge elsewhere in this mod - same name, different thing, worth not confusing in code.</p>
 */
public class SunPerkHandler
{
    private static final UUID BONUS_ARMOR_MODIFIER_ID = UUID.fromString("2d6f9a13-7c4e-4b8a-8e2b-1a5c3f7d9e42");
    private static final int RECHECK_INTERVAL_TICKS = 20;
    private static final int EFFECT_DURATION_TICKS = 60;

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
            String pledgedGod = cap.getPledgedGod();
            boolean tastesSun = pledgedGod.equals("sun") || pledgedGod.equals(GodRegistry.NEUTRAL_ID);
            boolean isDay = player.level().isDay();

            if (tastesSun && isDay)
            {
                // Infinite duration (rather than the short refreshed-every-tick-check duration everything
                // else here uses) - a finite duration close to its own recheck window kept these two visibly
                // flickering (Night Vision's tint fades in/out as it nears expiry, Glowing's outline
                // blinks off then back on). Guarded by hasEffect so this doesn't spam addEffect every
                // recheck; removeEffect below is what actually ends them once conditions lapse.
                if (!player.hasEffect(MobEffects.NIGHT_VISION))
                {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, MobEffectInstance.INFINITE_DURATION, 0, true, false, true));
                }
                if (!player.hasEffect(MobEffects.GLOWING))
                {
                    player.addEffect(new MobEffectInstance(MobEffects.GLOWING, MobEffectInstance.INFINITE_DURATION, 0, true, false, true));
                }
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, EFFECT_DURATION_TICKS, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, EFFECT_DURATION_TICKS, 0, true, false, true));
                applyBonusArmor(player, computeBonusArmor(pledgedGod, cap.getPledgeProgress()));
            }
            else
            {
                player.removeEffect(MobEffects.NIGHT_VISION);
                player.removeEffect(MobEffects.GLOWING);
                applyBonusArmor(player, 0);
            }

            if (pledgedGod.equals("sun") && !isDay)
            {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION_TICKS, 0));
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, EFFECT_DURATION_TICKS, 0));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_DURATION_TICKS, 0));
            }
        });
    }

    private static double computeBonusArmor(String pledgedGod, int pledgeProgress)
    {
        double rawArmor = Math.min(Config.sunMaxBonusArmor, (double) pledgeProgress / Config.sunFaithPerArmorPoint);
        if (pledgedGod.equals("sun"))
        {
            return rawArmor;
        }
        if (pledgedGod.equals(GodRegistry.NEUTRAL_ID))
        {
            return rawArmor * Config.neutralSunArmorScale;
        }
        return 0.0;
    }

    private static void applyBonusArmor(ServerPlayer player, double bonusArmor)
    {
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor == null)
        {
            return;
        }

        armor.removeModifier(BONUS_ARMOR_MODIFIER_ID);
        if (bonusArmor > 0)
        {
            armor.addTransientModifier(new AttributeModifier(BONUS_ARMOR_MODIFIER_ID,
                    "arcanelens:sun_bonus_armor", bonusArmor, AttributeModifier.Operation.ADDITION));
        }
    }
}
