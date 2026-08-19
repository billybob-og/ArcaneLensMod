package com.arcanelens.item;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.capability.IFaith;
import com.arcanelens.entity.TheHungerEntity;
import com.arcanelens.god.GodRegistry;
import com.arcanelens.registry.ModEntityTypes;
import com.arcanelens.registry.ModItems;
import com.arcanelens.worldgen.HungerTowerPlacer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * "Something has taken an interest in you." The Hunger is a presence separate from The One Who Sleeps that
 * starts watching a player once they've beaten the boss at least once. Each triggered moment is randomly a
 * blessing or a burden, pairing a brief physical apparition (TheHungerEntity) and a message+sound with a mild
 * (~level I, short-duration) vanilla status effect matching its flavor - both the frequency and the odds of a
 * permanent sun/moon "eyes" reveal scale with the player's current Faith and a permanent 1.25x-per-defeat
 * multiplier (repeat kills of the re-summonable boss matter). Past the eyes reveal, accumulated Faith
 * periodically grants a Token of the Hunger, and burned/consumed boon items shift the blessing/burden odds
 * and blessing duration.
 */
public class TheHungerHandler
{
    private record Moment(Component message, net.minecraft.world.effect.MobEffect effect, int durationTicks) {}

    private static final List<Moment> BURDEN_MOMENTS = List.of(
            new Moment(Component.literal("You feel eyes upon you.").withStyle(ChatFormatting.DARK_GRAY),
                    MobEffects.WEAKNESS, 300),
            new Moment(Component.literal("Something stirs in the dark, watching.").withStyle(ChatFormatting.DARK_GRAY),
                    MobEffects.DARKNESS, 150),
            new Moment(Component.literal("The Hunger turns its gaze toward you.").withStyle(ChatFormatting.DARK_GRAY),
                    MobEffects.HUNGER, 300),
            new Moment(Component.literal("A cold weight settles behind your thoughts.").withStyle(ChatFormatting.DARK_GRAY),
                    MobEffects.DIG_SLOWDOWN, 300));

    private static final List<Moment> BLESSING_MOMENTS = List.of(
            new Moment(Component.literal("A warmth settles over you, unseen and approving.").withStyle(ChatFormatting.GOLD),
                    MobEffects.REGENERATION, 100),
            new Moment(Component.literal("The Hunger's gaze lingers, and it is not unkind.").withStyle(ChatFormatting.GOLD),
                    MobEffects.DAMAGE_RESISTANCE, 300),
            new Moment(Component.literal("Something ancient seems... pleased.").withStyle(ChatFormatting.GOLD),
                    MobEffects.LUCK, 600),
            new Moment(Component.literal("You feel quietly favored.").withStyle(ChatFormatting.GOLD),
                    MobEffects.DIG_SPEED, 300));

    private static final Map<UUID, Integer> ticksUntilCheck = new HashMap<>();
    private static final Map<UUID, Integer> lastKnownFaith = new HashMap<>();

    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !(event.player instanceof ServerPlayer player))
        {
            return;
        }

        player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.getSleepingGodDefeats() < 1)
            {
                // The Hunger is dormant until the boss has been put back down at least once.
                return;
            }

            String pledgedGod = cap.getPledgedGod();
            if (!pledgedGod.isEmpty() && !pledgedGod.equals(GodRegistry.NEUTRAL_ID))
            {
                // Faith left undirected (never chosen) or explicitly declined (neutral) is exactly
                // what the Hunger absorbs by default, per the god-pledge system's own premise - once
                // it's deliberately directed at a specific god instead, even Hunger itself (which
                // trades this ambient courting/testing for its real, guaranteed pledge perks - see
                // HungerPerkHandler), the Hunger stops actively watching/appearing.
                return;
            }

            UUID id = player.getUUID();

            if (cap.isHungerEyesRevealed())
            {
                grantTokensForFaithGained(player, cap, id);
            }

            int remaining = ticksUntilCheck.getOrDefault(id, Config.watchingCheckIntervalTicks);
            if (remaining > 0)
            {
                ticksUntilCheck.put(id, remaining - 1);
                return;
            }
            ticksUntilCheck.put(id, Config.watchingCheckIntervalTicks);

            double chance = Math.min(Config.maxWatchingChance, Math.max(0.0,
                    Config.baseWatchingChance * (1.0 + cap.getFaith() / Config.watchingFaithScale)
                            * Math.pow(1.25, cap.getSleepingGodDefeats())));

            ThreadLocalRandom random = ThreadLocalRandom.current();

            if (!cap.isHungerEyesRevealed() && chance >= Config.watchingEyesRevealThreshold)
            {
                cap.setHungerEyesRevealed(true);
                FaithSync.syncToClient(player);
                // Always the Overworld, regardless of which dimension the triggering player is currently
                // in - the tower's "already placed" flag is stored per-dimension, so using whatever level
                // the player happened to be standing in could place a second tower in another dimension
                // entirely if a different player's first reveal happens there instead.
                HungerTowerPlacer.placeIfNeeded(player.server.overworld());
            }

            if (random.nextDouble() < chance)
            {
                spawnApparition(player, random);

                double blessingChance = Math.min(Config.maxBurdenResistChance,
                        0.5 + cap.getBurdenResistBoonLevel() * Config.burdenResistChancePerLevel);

                if (random.nextDouble() < blessingChance)
                {
                    Moment moment = BLESSING_MOMENTS.get(random.nextInt(BLESSING_MOMENTS.size()));
                    player.displayClientMessage(moment.message(), true);
                    player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.0F);
                    int duration = (int) (moment.durationTicks()
                            * (1.0 + cap.getBlessingBoonLevel() * Config.blessingBoonDurationMultiplierPerLevel));
                    player.addEffect(new MobEffectInstance(moment.effect(), duration, 0));

                    applyBonusBlessingIfEarned(player, cap, random, moment);
                }
                else
                {
                    Moment moment = BURDEN_MOMENTS.get(random.nextInt(BURDEN_MOMENTS.size()));
                    player.displayClientMessage(moment.message(), true);
                    player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSE, SoundSource.PLAYERS, 0.6F, 1.0F);
                    player.addEffect(new MobEffectInstance(moment.effect(), moment.durationTicks(), 0));
                }
            }
        });
    }

    /** Once Bindings levels push blessingChance to its maxBurdenResistChance cap, burning more isn't wasted:
     * each level past that point instead adds a stacking chance of a second, independent blessing effect on
     * top of a blessing moment - never rolled on burden moments. Always picks a different effect than the
     * primary moment: vanilla's addEffect() silently discards a same-effect application whose duration isn't
     * longer than what's already running, so rolling the same effect twice would make the bonus a no-op. */
    private static void applyBonusBlessingIfEarned(ServerPlayer player, IFaith cap, ThreadLocalRandom random, Moment primary)
    {
        double capLevel = (Config.maxBurdenResistChance - 0.5) / Config.burdenResistChancePerLevel;
        int levelsPastCap = (int) (cap.getBurdenResistBoonLevel() - Math.ceil(capLevel));
        if (levelsPastCap <= 0)
        {
            return;
        }

        double bonusChance = Math.min(Config.maxBonusBlessingChance, levelsPastCap * Config.bonusBlessingChancePerLevel);
        if (random.nextDouble() >= bonusChance)
        {
            return;
        }

        List<Moment> candidates = BLESSING_MOMENTS.stream().filter(m -> m != primary).toList();
        Moment bonus = candidates.get(random.nextInt(candidates.size()));
        player.sendSystemMessage(Component.literal("The Hunger lingers a moment longer, generous today.").withStyle(ChatFormatting.GOLD));
        int duration = (int) (bonus.durationTicks()
                * (1.0 + cap.getBlessingBoonLevel() * Config.blessingBoonDurationMultiplierPerLevel));
        player.addEffect(new MobEffectInstance(bonus.effect(), duration, 0));
    }

    /** Grants Tokens of the Hunger as Faith accumulates, tracked as a delta against the last tick's known
     * Faith (rather than total Faith) so this only fires on genuine gains, and loops so a single large jump
     * (e.g. via the debug command) correctly grants more than one token instead of losing the excess. */
    private static void grantTokensForFaithGained(ServerPlayer player, IFaith cap, UUID id)
    {
        int currentFaith = cap.getFaith();
        int previousFaith = lastKnownFaith.getOrDefault(id, currentFaith);
        lastKnownFaith.put(id, currentFaith);

        int gained = currentFaith - previousFaith;
        if (gained <= 0)
        {
            return;
        }

        int sinceLastToken = cap.getFaithSinceLastToken() + gained;
        int tokensToGrant = sinceLastToken / Config.faithPerHungerToken;
        if (tokensToGrant <= 0)
        {
            cap.setFaithSinceLastToken(sinceLastToken);
            return;
        }

        cap.setFaithSinceLastToken(sinceLastToken - tokensToGrant * Config.faithPerHungerToken);

        ItemStack tokens = new ItemStack(ModItems.TOKEN_OF_THE_HUNGER.get(), tokensToGrant);
        if (!player.getInventory().add(tokens))
        {
            player.drop(tokens, false);
        }
        player.displayClientMessage(Component.literal("The Hunger presses a token into your thoughts.")
                .withStyle(ChatFormatting.GOLD), true);
    }

    /** Spawns a brief, normally-broadcast apparition of The Hunger a few blocks in front of the player (with
     * a randomized angular offset, so it's sometimes dead-ahead, sometimes off to a side), facing back toward
     * them. A real entity visible to everyone nearby, not a private/client-only illusion. */
    private static void spawnApparition(ServerPlayer player, ThreadLocalRandom random)
    {
        Level level = player.level();

        // Spawn point = player position + distance in a direction offset from dead-ahead by up to +-60
        // degrees ("sometimes dead-ahead, sometimes off to a side"). Facing the player back is then just
        // that same spawn direction rotated 180 degrees - no atan2 needed since we already know the angle
        // the spawn point was placed at.
        double angleOffsetDeg = (random.nextDouble() - 0.5) * 120.0;
        double distance = 3.0 + random.nextDouble() * 2.0;
        double spawnYawDeg = player.getYRot() + angleOffsetDeg;
        float spawnYawRad = (float) Math.toRadians(spawnYawDeg);

        double spawnX = player.getX() - Mth.sin(spawnYawRad) * distance;
        double spawnZ = player.getZ() + Mth.cos(spawnYawRad) * distance;
        float facingYaw = (float) (spawnYawDeg + 180.0);

        TheHungerEntity apparition = new TheHungerEntity(ModEntityTypes.THE_HUNGER.get(), level);
        apparition.moveTo(spawnX, player.getY(), spawnZ, facingYaw, 0.0F);
        level.addFreshEntity(apparition);
    }
}
