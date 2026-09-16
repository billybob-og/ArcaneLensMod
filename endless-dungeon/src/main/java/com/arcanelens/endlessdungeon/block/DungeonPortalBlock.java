package com.arcanelens.endlessdungeon.block;

import com.arcanelens.endlessdungeon.capability.DungeonReturnStateProvider;
import com.arcanelens.endlessdungeon.capability.IDungeonReturnState;
import com.arcanelens.endlessdungeon.dungeon.DungeonEntrancePlacer;
import com.arcanelens.endlessdungeon.registry.ModBlocks;
import com.arcanelens.endlessdungeon.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * The walkable interior tile of a dungeon entry portal - one block class handles BOTH directions (unlike the
 * base mod's paired OverloadPortalBlock), since the only real difference between the two legs is what happens
 * on entry, decided by which dimension the entering entity is currently in:
 *
 * <p><b>Forward</b> (anywhere other than the dungeon dimension) - remembers the player's current position in
 * their {@link IDungeonReturnState} capability (this specific entry point, not which entrance they land at),
 * then asks {@link DungeonEntrancePlacer#ensureEntranceSpawnedFor} for a landing spot: a random existing
 * entrance if the pool already has one, or a freshly-spawned one if this is the first-ever use of THIS
 * specific overworld portal instance.</p>
 *
 * <p><b>Return</b> (already in the dungeon dimension) - reads the capability and teleports back to the exact
 * remembered position/facing; falls back to the destination level's own spawn point if there's no saved
 * state (e.g. a player who reached the dungeon via the old fixed-origin/debug path) or its dimension no
 * longer resolves - a stranded dungeon player always needs somewhere to go, unlike
 * PocketDimensionSpell's "nowhere remembered, do nothing" case where the player is already free.</p>
 *
 * <p>Mirrors OverloadPortalBlock's IN_TRANSIT persistent-data-flag anti-bounce mechanism and fall-state reset
 * verbatim - see that class's own javadoc for why vanilla's time-based portal cooldown alone isn't enough
 * when the destination has zero displacement from the portal block itself.</p>
 */
public class DungeonPortalBlock extends Block
{
    public static final BooleanProperty UPPER = BooleanProperty.create("upper");

    // Public (not private) - DungeonTeleporterItem sets this same flag before teleporting a player onto a
    // landing spot that may itself be a return-portal tile, so THIS block's own entityInside guard (checked
    // right below) doesn't fire again the instant they arrive. See that item's own javadoc for why
    // setPortalCooldown() alone wasn't enough to cover that case.
    public static final String IN_TRANSIT_KEY = "endless_dungeon_portal_transit";

    // Same idea as vanilla Nether portals' own multi-tick "stand inside before it actually fires" delay -
    // added because the frame is 2x2, so a player just walking PAST it (not trying to use it at all) can
    // easily graze one of its 4 tiles and get teleported instantly with the old immediate-trigger design
    // (confirmed by the user's own report). 20 ticks (~1 second) is long enough that brushing through
    // doesn't trigger it, short enough that a deliberate stand-in doesn't feel sluggish.
    private static final String DWELL_KEY = "endless_dungeon_portal_dwell";
    private static final int DWELL_TICKS_REQUIRED = 20;

    public DungeonPortalBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(UPPER, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(UPPER);
    }

    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity)
    {
        // TEMPORARY debug logging - narrowing down why entityInside isn't completing a teleport in a real
        // (non-dev-client) instance despite the player confirmed standing exactly on the block.
        LOGGER.info("DungeonPortalBlock.entityInside: pos={} entity={} clientSide={} isServerLevel={} portalCooldown={} inTransit={}",
                pos, entity, level.isClientSide, level instanceof ServerLevel,
                entity.isOnPortalCooldown(), entity.getPersistentData().getBoolean(IN_TRANSIT_KEY));

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)
                || entity.isOnPortalCooldown() || entity.getPersistentData().getBoolean(IN_TRANSIT_KEY))
        {
            return;
        }
        if (!entity.canChangeDimensions())
        {
            LOGGER.info("DungeonPortalBlock.entityInside: canChangeDimensions() returned false, blocking");
            return;
        }

        int dwell = entity.getPersistentData().getInt(DWELL_KEY) + 1;
        LOGGER.info("DungeonPortalBlock.entityInside: dwell={} required={}", dwell, DWELL_TICKS_REQUIRED);
        if (dwell < DWELL_TICKS_REQUIRED)
        {
            entity.getPersistentData().putInt(DWELL_KEY, dwell);
            return;
        }
        entity.getPersistentData().putInt(DWELL_KEY, 0);
        LOGGER.info("DungeonPortalBlock.entityInside: dwell threshold reached, proceeding to teleport");

        // Set BEFORE doing anything else, not just once a destination is known - a first-time forward trip
        // spawns a brand-new entrance, which takes several ticks (IncrementalStructurePlacer's multi-tick
        // paste). Without this guard active for that whole wait, every tick the entity is still standing in
        // the portal re-triggers entityInside with nothing yet blocking it - and since the portal frame is
        // multiple tiles wide, even small movement shifts which exact tile position fires, so each retrigger
        // looked like a genuinely new, unrelated portal use and kicked off its own parallel entrance
        // placement (confirmed in testing: walking into a 2x2 frame produced dozens of orphaned entrances
        // before the first one finished pasting). Sets unconditionally for both legs - the return leg is
        // already synchronous so this is a no-op timing-wise there, but keeps the guard's placement uniform.
        entity.setPortalCooldown();
        entity.getPersistentData().putBoolean(IN_TRANSIT_KEY, true);

        if (serverLevel.dimension() == ModDimensions.DUNGEON_KEY)
        {
            enterReturn(serverLevel, entity);
        }
        else
        {
            enterForward(serverLevel, pos, entity);
        }
    }

    private void enterForward(ServerLevel serverLevel, BlockPos pos, Entity entity)
    {
        ServerLevel dungeonLevel = serverLevel.getServer().getLevel(ModDimensions.DUNGEON_KEY);
        if (dungeonLevel == null)
        {
            // Same defensive shape as OverloadPortalBlock - a malformed/missing dimension registration
            // should never crash the game, just silently no-op the teleport.
            return;
        }

        if (entity instanceof ServerPlayer player)
        {
            player.getCapability(DungeonReturnStateProvider.CAPABILITY).ifPresent(returnState ->
                    returnState.setReturnPosition(serverLevel.dimension().location().toString(),
                            entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot()));
        }

        // A portal frame is multiple tiles wide (e.g. 2x2) - tracking "has this portal been used before" by
        // the EXACT tile touched would treat each tile as its own separate portal identity, since a player
        // rarely enters on the exact same block twice (confirmed in testing: walking in on a different tile
        // than a previous attempt looked like a brand-new, never-used portal and spawned another entrance
        // each time). Using the whole contiguous frame's own canonical anchor (its lowest-coordinate block,
        // found by flood-filling connected DUNGEON_PORTAL blocks) makes every tile of the same frame resolve
        // to the same identity regardless of which one was actually touched.
        BlockPos anchor = findPortalAnchor(serverLevel, pos);
        GlobalPos portalPos = GlobalPos.of(serverLevel.dimension(), anchor);
        DungeonEntrancePlacer.ensureEntranceSpawnedFor(dungeonLevel, portalPos, landingPos ->
                teleport(entity, dungeonLevel, landingPos.getX() + 0.5, landingPos.getY(), landingPos.getZ() + 0.5,
                        entity.getYRot(), entity.getXRot()));
    }

    /** Flood-fills every DUNGEON_PORTAL block connected to `start` (6-directional adjacency) and returns the
     * lowest-coordinate one found - a deterministic canonical identity for the whole frame, independent of
     * which specific tile triggered entityInside. Bounded by the frame's own real size (a handful of blocks
     * for any portal built so far), not a search over the world. */
    private static BlockPos findPortalAnchor(ServerLevel level, BlockPos start)
    {
        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);
        BlockPos anchor = start;

        while (!queue.isEmpty())
        {
            BlockPos current = queue.poll();
            if (current.getX() < anchor.getX()
                    || (current.getX() == anchor.getX() && current.getY() < anchor.getY())
                    || (current.getX() == anchor.getX() && current.getY() == anchor.getY() && current.getZ() < anchor.getZ()))
            {
                anchor = current;
            }
            for (Direction direction : Direction.values())
            {
                BlockPos neighbor = current.relative(direction);
                if (!visited.contains(neighbor) && level.getBlockState(neighbor).is(ModBlocks.DUNGEON_PORTAL.get()))
                {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return anchor;
    }

    private void enterReturn(ServerLevel serverLevel, Entity entity)
    {
        if (!(entity instanceof ServerPlayer player))
        {
            // No capability on non-players (see class javadoc) - nothing to return them to.
            return;
        }

        IDungeonReturnState returnState = player.getCapability(DungeonReturnStateProvider.CAPABILITY).resolve().orElse(null);
        ServerLevel destLevel = null;
        if (returnState != null && !returnState.getReturnDimension().isEmpty())
        {
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(returnState.getReturnDimension()));
            destLevel = serverLevel.getServer().getLevel(key);
        }

        if (destLevel == null)
        {
            // No saved state, or its dimension no longer resolves - degrade to that level's own spawn rather
            // than stranding the player (see class javadoc for why this differs from PocketDimensionSpell's
            // "nowhere remembered, do nothing" case).
            destLevel = serverLevel.getServer().overworld();
            BlockPos spawn = destLevel.getSharedSpawnPos();
            teleport(entity, destLevel, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0, 0);
        }
        else
        {
            teleport(entity, destLevel, returnState.getReturnX(), returnState.getReturnY(), returnState.getReturnZ(),
                    returnState.getReturnYaw(), returnState.getReturnPitch());
        }
    }

    private static void teleport(Entity entity, ServerLevel destLevel, double x, double y, double z, float yaw, float pitch)
    {
        // setPortalCooldown/IN_TRANSIT are already set (see entityInside) by the time this runs.
        entity.teleportTo(destLevel, x, y, z, Set.<RelativeMovement>of(), yaw, pitch);

        // teleportTo doesn't reset fall state on its own - see OverloadPortalBlock's own javadoc for why this
        // needs it explicit (the dwell time before this actually fires - see DWELL_TICKS_REQUIRED - only
        // delays the trigger, the teleport itself is still instant once it does).
        entity.resetFallDistance();
        entity.setDeltaMovement(entity.getDeltaMovement().x, 0.0, entity.getDeltaMovement().z);
    }

    /** Resets the dwell counter and clears IN_TRANSIT once a player has genuinely stepped out of both portal
     * tiles - mirrors OverloadPortalBlock.onPlayerTick exactly (see that class's javadoc for the non-player
     * gap this accepts), just also covering DWELL_KEY (which entityInside accumulates even before IN_TRANSIT
     * is ever set, so it can't wait for that same early-return the old IN_TRANSIT-only version used). */
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !(event.player instanceof ServerPlayer player))
        {
            return;
        }

        boolean inTransit = player.getPersistentData().getBoolean(IN_TRANSIT_KEY);
        boolean dwelling = player.getPersistentData().getInt(DWELL_KEY) > 0;
        if (!inTransit && !dwelling)
        {
            return;
        }

        Level level = player.level();
        BlockPos feet = player.blockPosition();
        boolean stillInPortal = level.getBlockState(feet).is(ModBlocks.DUNGEON_PORTAL.get())
                || level.getBlockState(feet.above()).is(ModBlocks.DUNGEON_PORTAL.get());
        if (!stillInPortal)
        {
            player.getPersistentData().putBoolean(IN_TRANSIT_KEY, false);
            player.getPersistentData().putInt(DWELL_KEY, 0);
        }
    }
}
