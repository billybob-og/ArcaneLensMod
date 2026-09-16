package com.arcanelens.item;

import com.arcanelens.capability.GodHubStateProvider;
import com.arcanelens.worldgen.ModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Set;

/**
 * Round-trip access to the God Challenge Hub - right-click outside the hub to enter (storing a return
 * position first, via IGodHubState - a dedicated capability rather than reusing
 * IPocketDimensionState's, so a player mid-Pocket-Dimension-trip doesn't get that spell's own return
 * point clobbered), right-click again while inside to return to exactly where/which way they left from.
 * Same enter/exit-by-current-dimension shape as PocketDimensionSpell, just as an Item instead of a
 * Spell since the hub isn't cast-from-a-lens content.
 *
 * <p>Not registered into the creative tab (see ModCreativeTabs) - same "obtainable via /give only for
 * now" precedent as ARENA_TRIGGER/SLEEPING_GOD_RELIC_ALTAR, until Phase B decides how this should
 * actually be obtained in survival.</p>
 */
public class GodHubMedallionItem extends Item
{
    private static final double HUB_SPAWN_X = 0.5;
    private static final double HUB_SPAWN_Y = 9.0;
    private static final double HUB_SPAWN_Z = 0.5;

    public GodHubMedallionItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer))
        {
            return InteractionResultHolder.pass(stack);
        }

        MinecraftServer server = serverPlayer.getServer();
        ServerLevel hubLevel = server == null ? null : server.getLevel(ModDimensions.GOD_CHALLENGE_HUB_KEY);
        if (server == null || hubLevel == null)
        {
            message(serverPlayer, "The way isn't ready yet - something still needs to open first.");
            return InteractionResultHolder.fail(stack);
        }

        if (serverPlayer.level().dimension() == ModDimensions.GOD_CHALLENGE_HUB_KEY)
        {
            exit(serverPlayer, server);
        }
        else
        {
            enter(serverPlayer, hubLevel);
        }

        return InteractionResultHolder.success(stack);
    }

    private void enter(ServerPlayer player, ServerLevel hubLevel)
    {
        player.getCapability(GodHubStateProvider.CAPABILITY).ifPresent(state ->
                state.setReturnPosition(player.level().dimension().location().toString(),
                        player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()));

        player.teleportTo(hubLevel, HUB_SPAWN_X, HUB_SPAWN_Y, HUB_SPAWN_Z, Set.of(), player.getYRot(), player.getXRot());
        player.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void exit(ServerPlayer player, MinecraftServer server)
    {
        String returnDimension = player.getCapability(GodHubStateProvider.CAPABILITY)
                .map(state -> state.getReturnDimension()).orElse("");
        ServerLevel returnLevel = returnDimension.isEmpty() ? null
                : server.getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(returnDimension)));
        if (returnLevel == null)
        {
            returnLevel = server.overworld();
        }

        ServerLevel finalReturnLevel = returnLevel;
        player.getCapability(GodHubStateProvider.CAPABILITY).ifPresent(state ->
                player.teleportTo(finalReturnLevel, state.getReturnX(), state.getReturnY(), state.getReturnZ(),
                        Set.of(), state.getReturnYaw(), state.getReturnPitch()));
        player.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void message(ServerPlayer player, String text)
    {
        player.displayClientMessage(Component.literal(text).withStyle(ChatFormatting.YELLOW), true);
    }
}
