package com.arcanelens.item;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.god.GodRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The Hunger's real perks (both gated on IFaith.getPledgedGod(), with a weaker neutral taste of each -
 * mirrors Fertility/War/Sun's own PerkHandler classes):
 *
 * <ul>
 * <li><b>Food immunity</b> - immunity to food-caused Poison/Confusion/Hunger, scoped precisely to the
 * act of eating (not a blanket "immune to these effects from any source", which would also block e.g.
 * a cave spider's poison). currentlyEatingFood brackets exactly the window between a food item's use
 * starting and finishing/stopping - MobEffectEvent.Applicable fires synchronously inside
 * finishUsingItem(), which runs strictly inside that window, so this correctly scopes to food alone
 * with no tick-countdown heuristics needed.</li>
 * <li><b>Lifesteal</b> - every hungerLifestealHitsPerHeal landed hits heals hungerLifestealHealAmount
 * HP, fitting Hunger's own appetite/greed theme in combat too (added per the user's own call, since
 * food immunity alone "isn't very good" as the pledge's only payoff).</li>
 * </ul>
 *
 * <p>Verified against the actual Forge sources: MobEffectEvent.Applicable is @HasResult (not
 * @Cancelable like MobEffectEvent.Added) - setResult(Event.Result.DENY) prevents the effect outright,
 * before it's ever applied.</p>
 */
public class HungerPerkHandler
{
    private static final Set<MobEffect> BLOCKED_EFFECTS = Set.of(MobEffects.POISON, MobEffects.CONFUSION, MobEffects.HUNGER);

    private static final Set<UUID> currentlyEatingFood = new HashSet<>();
    private static final Map<UUID, Integer> hitsSinceLastHeal = new HashMap<>();

    public static void onUseItemStart(LivingEntityUseItemEvent.Start event)
    {
        if (event.getEntity() instanceof ServerPlayer player && event.getItem().isEdible())
        {
            currentlyEatingFood.add(player.getUUID());
        }
    }

    public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event)
    {
        if (event.getEntity() instanceof ServerPlayer player)
        {
            currentlyEatingFood.remove(player.getUUID());
        }
    }

    public static void onUseItemStop(LivingEntityUseItemEvent.Stop event)
    {
        if (event.getEntity() instanceof ServerPlayer player)
        {
            currentlyEatingFood.remove(player.getUUID());
        }
    }

    public static void onMobEffectApplicable(MobEffectEvent.Applicable event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player) || !currentlyEatingFood.contains(player.getUUID()))
        {
            return;
        }
        if (!BLOCKED_EFFECTS.contains(event.getEffectInstance().getEffect()))
        {
            return;
        }

        String pledgedGod = player.getCapability(FaithProvider.CAPABILITY).map(cap -> cap.getPledgedGod()).orElse("");
        boolean immune;
        if (pledgedGod.equals("hunger"))
        {
            // Guaranteed - the full pledged perk.
            immune = true;
        }
        else if (pledgedGod.equals(GodRegistry.NEUTRAL_ID))
        {
            // A real but clearly weaker taste of the full perk - see Config.neutralHungerFoodImmunityChance.
            immune = player.getRandom().nextDouble() < Config.neutralHungerFoodImmunityChance;
        }
        else
        {
            // Never chosen, or pledged elsewhere - no perk at all.
            immune = false;
        }

        if (immune)
        {
            event.setResult(Event.Result.DENY);
        }
    }

    /** Every Config.hungerLifestealHitsPerHeal landed hits, heal Config.hungerLifestealHealAmount HP
     * (scaled down for neutral via Config.neutralHungerLifestealScale - same cadence, less healed per
     * proc, matching how Fertility/War/Sun scale a continuous value down for neutral rather than
     * changing trigger frequency). Counts any damage the player is the direct source of, matching
     * TheaterEventTracker's own LivingHurtEvent convention for "a player hit something". */
    public static void onLivingHurt(LivingHurtEvent event)
    {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0)
        {
            return;
        }

        player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
            String pledgedGod = cap.getPledgedGod();
            double healAmount;
            if (pledgedGod.equals("hunger"))
            {
                healAmount = Config.hungerLifestealHealAmount;
            }
            else if (pledgedGod.equals(GodRegistry.NEUTRAL_ID))
            {
                healAmount = Config.hungerLifestealHealAmount * Config.neutralHungerLifestealScale;
            }
            else
            {
                hitsSinceLastHeal.remove(player.getUUID());
                return;
            }

            UUID id = player.getUUID();
            int hits = hitsSinceLastHeal.merge(id, 1, Integer::sum);
            if (hits < Config.hungerLifestealHitsPerHeal)
            {
                return;
            }
            hitsSinceLastHeal.put(id, 0);

            if (healAmount > 0 && player.getHealth() < player.getMaxHealth())
            {
                player.heal((float) healAmount);
            }
        });
    }
}
