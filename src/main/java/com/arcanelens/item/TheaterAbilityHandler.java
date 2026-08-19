package com.arcanelens.item;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.entity.TheaterCloneEntity;
import com.arcanelens.god.GodRegistry;
import com.arcanelens.network.NetworkHandler;
import com.arcanelens.network.packet.ServerboundTheaterAbilityPacket;
import com.arcanelens.registry.ModEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The Theater Helmet's 3 active abilities. Trigger inputs are literal empty-handed clicks (per the
 * user's own call, over inventing new keybinds) - plain right-click for Stage Clone, sneak+right-click
 * for Crowd Control, both via PlayerInteractEvent.RightClickEmpty (which naturally only fires when
 * targeting nothing, so it doesn't conflict with block/entity interactions). Vanishing Act is
 * different in shape - hold sneak for Config.theaterVanishingActChargeTicks to trigger it, then it
 * runs on its own timer (Config.theaterVanishingActDurationTicks) or until the player attacks.
 *
 * <p>RightClickEmpty only ever fires on the client (posted from Minecraft.class's own input handling,
 * never from anything the server runs) - onRightClickEmpty below only does the client-side eligibility
 * check and sends ServerboundTheaterAbilityPacket; the actual ability logic (spendFaith/spawning/
 * retargeting) has to run server-side, in that packet's handle().</p>
 */
public class TheaterAbilityHandler
{
    private static final List<SoundEvent> EERIE_SOUNDS = List.of(
            SoundEvents.CREEPER_PRIMED, SoundEvents.GHAST_SCREAM, SoundEvents.ENDERMAN_TELEPORT);

    private static final Map<UUID, Integer> sneakHoldTicks = new HashMap<>();
    private static final Set<UUID> vanishingActActive = new HashSet<>();

    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event)
    {
        Player player = event.getEntity();
        if (!player.level().isClientSide || event.getHand() != InteractionHand.MAIN_HAND
                || !TheaterHelmetPerkHandler.isWearingTheaterHelmet(player) || !TheaterHelmetPerkHandler.isEmptyHanded(player)
                || !GodRegistry.isPledgedTo(player, "theater"))
        {
            return;
        }

        NetworkHandler.CHANNEL.sendToServer(new ServerboundTheaterAbilityPacket(player.isShiftKeyDown()));
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !(event.player instanceof ServerPlayer player))
        {
            return;
        }

        UUID id = player.getUUID();

        if (vanishingActActive.contains(id) && !player.hasEffect(MobEffects.INVISIBILITY))
        {
            // Ran out on its own (Config.theaterVanishingActDurationTicks elapsed) - just stop tracking it.
            vanishingActActive.remove(id);
        }

        boolean eligible = TheaterHelmetPerkHandler.isWearingTheaterHelmet(player) && TheaterHelmetPerkHandler.isEmptyHanded(player)
                && GodRegistry.isPledgedTo(player, "theater");
        if (!eligible || !player.isShiftKeyDown() || player.hasEffect(MobEffects.INVISIBILITY))
        {
            sneakHoldTicks.remove(id);
            return;
        }

        int held = sneakHoldTicks.merge(id, 1, Integer::sum);
        if (held >= Config.theaterVanishingActChargeTicks)
        {
            sneakHoldTicks.remove(id);
            tryVanishingAct(player);
        }
    }

    /** Attacking breaks Vanishing Act early, per the design brief - but only the Vanishing Act's own
     * invisibility, never a genuinely-drunk invisibility potion the player happens to also have. */
    public static void onAttackEntity(AttackEntityEvent event)
    {
        Player player = event.getEntity();
        if (!player.level().isClientSide && vanishingActActive.remove(player.getUUID()))
        {
            player.removeEffect(MobEffects.INVISIBILITY);
        }
    }

    public static void tryStageClone(ServerPlayer player)
    {
        if (!spendFaith(player, Config.theaterStageCloneFaithCost) || !(player.level() instanceof ServerLevel level))
        {
            return;
        }

        TheaterCloneEntity clone = ModEntityTypes.THEATER_CLONE.get().create(level);
        if (clone == null)
        {
            return;
        }
        clone.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
        level.addFreshEntity(clone);

        AABB retargetRange = player.getBoundingBox().inflate(Config.theaterStageCloneRetargetRadius);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, retargetRange))
        {
            if (mob.getTarget() == player)
            {
                mob.setTarget(clone);
            }
        }

        player.displayClientMessage(Component.literal("A decoy takes the stage.").withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }

    public static void tryCrowdControl(ServerPlayer player)
    {
        if (!spendFaith(player, Config.theaterCrowdControlFaithCost) || !(player.level() instanceof ServerLevel level))
        {
            return;
        }

        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(look.scale(Config.theaterCrowdControlRange));
        ClipContext clipContext = new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        BlockHitResult hit = level.clip(clipContext);
        Vec3 targetPos = hit.getType() == HitResult.Type.MISS ? endPos : hit.getLocation();

        SoundEvent sound = EERIE_SOUNDS.get(player.getRandom().nextInt(EERIE_SOUNDS.size()));
        level.playSound(null, targetPos.x, targetPos.y, targetPos.z, sound, SoundSource.HOSTILE, 1.5F, 1.0F);

        AABB affected = new AABB(targetPos, targetPos).inflate(Config.theaterCrowdControlRadius);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, affected))
        {
            if (mob instanceof Enemy)
            {
                mob.setTarget(null);
                mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
            }
            else
            {
                Vec3 away = mob.position().subtract(targetPos);
                if (away.lengthSqr() > 1.0E-4)
                {
                    away = away.normalize().scale(0.6);
                    mob.setDeltaMovement(mob.getDeltaMovement().add(away.x, 0.2, away.z));
                }
            }
        }

        player.displayClientMessage(Component.literal("The crowd reacts.").withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }

    private static void tryVanishingAct(ServerPlayer player)
    {
        if (!spendFaith(player, Config.theaterVanishingActFaithCost))
        {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Config.theaterVanishingActDurationTicks, 0, false, true, true));
        vanishingActActive.add(player.getUUID());
        player.displayClientMessage(Component.literal("You slip out of sight.").withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }

    private static boolean spendFaith(ServerPlayer player, int cost)
    {
        boolean[] success = {false};
        player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.getFaith() < cost)
            {
                player.displayClientMessage(Component.literal("Not enough Faith.").withStyle(ChatFormatting.RED), true);
                return;
            }
            cap.addFaith(-cost);
            FaithSync.syncToClient(player);
            success[0] = true;
        });
        return success[0];
    }
}
