package com.arcanelens.item;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.entity.AureliaCompanionEntity;
import com.arcanelens.god.GodRegistry;
import com.arcanelens.registry.ModEntityTypes;
import com.arcanelens.registry.ModItems;
import com.arcanelens.spell.SummonExpiryHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

/** "My Love" - rename a Poppy to that exact text via anvil, then right-click with it to summon
 * Aurelia as a temporary combat companion, for a player pledged to her. Handles BOTH
 * PlayerInteractEvent.RightClickItem (aiming at nothing) AND RightClickBlock (aiming at a surface the
 * Poppy could otherwise plant on, e.g. grass/dirt/farmland - which is most of the time in practice, so
 * only handling RightClickItem meant the summon silently never fired for a player just aiming roughly
 * forward/down as normal). Both fire server-side (confirmed via ForgeHooks.onItemRightClick and
 * onRightClickBlock both being called from ServerPlayerGameMode, unlike RightClickEmpty which is
 * client-only - see TheaterAbilityHandler's own javadoc for how that one differs), so this needs no
 * network packet either way. */
public class AureliaSummonHandler
{
    private static final String TRIGGER_NAME = "My Love";

    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event)
    {
        if (trySummon(event.getEntity(), event.getItemStack()))
        {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (trySummon(event.getEntity(), event.getItemStack()))
        {
            // Deny the block-use path specifically so the Poppy doesn't also get planted on whatever
            // surface was being aimed at - a plain setCanceled(true) alone isn't enough to stop that.
            event.setUseBlock(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    /** Returns true if the summon actually happened (and the triggering interaction should be
     * consumed), false if this wasn't a qualifying "My Love" Poppy/pledge/etc. - left as a no-op PASS
     * either way so an ordinary Poppy (planting flowers, feeding rabbits) is completely unaffected. */
    private static boolean trySummon(Player player, ItemStack stack)
    {
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)
                || !(player.level() instanceof ServerLevel level)
                || !stack.is(Items.POPPY) || !stack.hasCustomHoverName()
                || !stack.getHoverName().getString().equals(TRIGGER_NAME)
                || !GodRegistry.isPledgedTo(serverPlayer, "war"))
        {
            return false;
        }

        AureliaCompanionEntity companion = ModEntityTypes.AURELIA_COMPANION.get().create(level);
        if (companion == null)
        {
            return false;
        }

        companion.moveTo(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.getYRot(), 0.0F);
        companion.tame(serverPlayer);
        companion.setOrderedToSit(false);
        companion.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SPEAR.get()));
        companion.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        companion.getPersistentData().putLong(SummonExpiryHandler.EXPIRY_TAG,
                level.getGameTime() + computeDurationTicks(serverPlayer));
        level.addFreshEntity(companion);

        stack.shrink(1);

        level.sendParticles(ParticleTypes.HEART, serverPlayer.getX(), serverPlayer.getY() + 1, serverPlayer.getZ(), 12, 0.4, 0.4, 0.4, 0.0);
        level.playSound(null, serverPlayer.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.2f);
        serverPlayer.displayClientMessage(Component.literal("Aurelia answers.").withStyle(ChatFormatting.RED), true);
        return true;
    }

    /** Base duration plus a bonus scaling with progress toward Aurelia specifically (not lifetime
     * Faith - see IFaith.getPledgeProgress()), capped the same way Fertility/War/Sun's own scaling
     * perks are. */
    private static int computeDurationTicks(ServerPlayer player)
    {
        int bonus = player.getCapability(FaithProvider.CAPABILITY)
                .map(cap -> (int) Math.min(Config.aureliaCompanionMaxBonusDurationTicks,
                        cap.getPledgeProgress() * Config.aureliaCompanionBonusTicksPerFaith))
                .orElse(0);
        return Config.aureliaCompanionDurationTicks + bonus;
    }
}
