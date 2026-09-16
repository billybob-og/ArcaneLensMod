package com.arcanelens.endlessdungeon.item;

import com.arcanelens.endlessdungeon.block.DungeonPortalBlock;
import com.arcanelens.endlessdungeon.capability.DungeonReturnStateProvider;
import com.arcanelens.endlessdungeon.capability.IDungeonReturnState;
import com.arcanelens.endlessdungeon.dungeon.DungeonEntrancePlacer;
import com.arcanelens.endlessdungeon.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
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
 * Round-trip access to the dungeon - right-click outside the dungeon to enter (storing a return position
 * first, via the same IDungeonReturnState capability DungeonPortalBlock uses, so returning via this item or
 * via a physical return portal both send the player back to wherever they most recently entered from),
 * right-click again while inside to return to exactly where/which way they left from. Same enter/exit-by-
 * current-dimension shape as GodHubMedallionItem (the base mod's own item-based personal-dimension
 * teleporter) and DungeonPortalBlock, just reused here as a standalone item with no specific portal instance
 * to pair against - entry picks any existing landing from the shared pool at random (see
 * DungeonEntrancePlacer#ensureAnyEntranceExists), rather than a portal-specific paired one.
 *
 * <p>Not consumed on use and no cooldown, matching GodHubMedallionItem's own precedent - the crafting cost
 * (portal shards, godly-chest-exclusive) is the balancing factor, not a durability/charge mechanic.</p>
 */
public class DungeonTeleporterItem extends Item
{
    public DungeonTeleporterItem(Properties properties)
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

        // Same vanilla portal-cooldown mechanic DungeonPortalBlock already relies on, applied here for the
        // same reason: a single right-click can fire use() more than once if the button stays held for even
        // a couple ticks, which without a cooldown immediately alternates enter/exit/enter/... - a rapid
        // bounce between dimensions that's very hard to even tell apart from "did this go to the overworld
        // or the dungeon?" (confirmed by the user's own report). setPortalCooldown() blocks any further
        // teleport (via this item OR a physical portal - it's the same entity-level cooldown) for the same
        // vanilla window Nether portals use, well past a single click's worth of repeat-fires.
        if (serverPlayer.isOnPortalCooldown() || serverPlayer.getPersistentData().getBoolean(DungeonPortalBlock.IN_TRANSIT_KEY))
        {
            return InteractionResultHolder.fail(stack);
        }

        MinecraftServer server = serverPlayer.getServer();
        ServerLevel dungeonLevel = server == null ? null : server.getLevel(ModDimensions.DUNGEON_KEY);
        if (server == null || dungeonLevel == null)
        {
            return InteractionResultHolder.fail(stack);
        }

        // Landing/return positions can be the exact tile a real DungeonPortalBlock occupies (this item's own
        // entrance pool IS the physical portal system's own pool - see ensureAnyEntranceExists; a saved
        // return position can equally be inside an overworld portal, if that's how the player originally
        // entered). setPortalCooldown() alone wasn't enough to stop that block's own entityInside from firing
        // the instant the player arrives standing on/in it - its guard also checks this SAME flag, cleared
        // once DungeonPortalBlock.onPlayerTick confirms they've genuinely stepped off any portal tile.
        serverPlayer.setPortalCooldown();
        serverPlayer.getPersistentData().putBoolean(DungeonPortalBlock.IN_TRANSIT_KEY, true);
        if (serverPlayer.level().dimension() == ModDimensions.DUNGEON_KEY)
        {
            exit(serverPlayer, server);
        }
        else
        {
            enter(serverPlayer, dungeonLevel);
        }

        return InteractionResultHolder.success(stack);
    }

    private void enter(ServerPlayer player, ServerLevel dungeonLevel)
    {
        player.getCapability(DungeonReturnStateProvider.CAPABILITY).ifPresent(state ->
                state.setReturnPosition(player.level().dimension().location().toString(),
                        player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()));

        DungeonEntrancePlacer.ensureAnyEntranceExists(dungeonLevel, landingPos -> teleport(player, dungeonLevel,
                landingPos.getX() + 0.5, landingPos.getY(), landingPos.getZ() + 0.5, player.getYRot(), player.getXRot()));
    }

    private void exit(ServerPlayer player, MinecraftServer server)
    {
        IDungeonReturnState returnState = player.getCapability(DungeonReturnStateProvider.CAPABILITY).resolve().orElse(null);
        ServerLevel destLevel = null;
        if (returnState != null && !returnState.getReturnDimension().isEmpty())
        {
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(returnState.getReturnDimension()));
            destLevel = server.getLevel(key);
        }

        if (destLevel == null || returnState == null)
        {
            // No saved state, or its dimension no longer resolves - degrade to the overworld's own spawn
            // rather than stranding the player, same defensive shape as DungeonPortalBlock's return leg.
            ServerLevel overworld = server.overworld();
            BlockPos spawn = overworld.getSharedSpawnPos();
            teleport(player, overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0, 0);
        }
        else
        {
            teleport(player, destLevel, returnState.getReturnX(), returnState.getReturnY(), returnState.getReturnZ(),
                    returnState.getReturnYaw(), returnState.getReturnPitch());
        }
    }

    private static void teleport(ServerPlayer player, ServerLevel destLevel, double x, double y, double z, float yaw, float pitch)
    {
        player.teleportTo(destLevel, x, y, z, Set.of(), yaw, pitch);
        player.resetFallDistance();
        player.setDeltaMovement(player.getDeltaMovement().x, 0.0, player.getDeltaMovement().z);
        player.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
