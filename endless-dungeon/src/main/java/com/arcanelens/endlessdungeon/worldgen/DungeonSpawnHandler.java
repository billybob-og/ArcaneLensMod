package com.arcanelens.endlessdungeon.worldgen;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages every minecraft:mob_spawner block in the dungeon dimension directly, bypassing vanilla's own
 * BaseSpawner tick/placement logic entirely - confirmed via both MobSpawnEvent.PositionCheck AND
 * MobSpawnEvent.FinalizeSpawn never firing at all for spawner-block-triggered attempts (debug-logged, zero
 * hits after a full minute standing next to an active spawner) that Forge simply doesn't expose a hook into
 * BaseSpawner's own spawn cycle - those two events only cover NATURAL (NaturalSpawner) spawning. Rather than
 * reach for a Mixin (no existing Mixin infrastructure in this project) to intercept BaseSpawner directly,
 * this reimplements just enough of its behavior standalone: reads each spawner block entity's OWN NBT
 * (SpawnData/MaxNearbyEntities/SpawnCount/SpawnRange/Min-MaxSpawnDelay - the exact same fields the structure
 * NBT already authors, vanilla-persisted by the block entity itself, so no separate tracking/persistence of
 * our own is needed) and spawns entities directly via EntityType.create + addFreshEntity, skipping
 * SpawnPlacements.checkSpawnRules (the darkness/placement-rule check that was almost certainly the real
 * blocker) entirely - keeping only a basic level.noCollision check so mobs don't spawn stuck inside walls.
 * The vanilla spawner block itself is left untouched (still renders/animates normally); this just runs a
 * second, independent spawn attempt loop alongside its own (permanently unsuccessful) one.
 */
public class DungeonSpawnHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHECK_INTERVAL_TICKS = 10;
    private static final double RANGE = 16.0;
    private static final double RANGE_SQR = RANGE * RANGE;

    // Per-spawner-position cooldown, mirroring vanilla's own Delay/MinSpawnDelay/MaxSpawnDelay fields - not
    // persisted (same reasoning as DungeonEntrancePlacer's own pendingSpawns: only matters within the next
    // handful of ticks, an empty map on restart is always a safe starting state, worst case one extra
    // immediate attempt right after a relaunch).
    private static final Map<BlockPos, Long> nextAttemptTick = new HashMap<>();

    public static void onLevelTick(TickEvent.LevelTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.side.isClient()
                || !(event.level instanceof ServerLevel level) || level.dimension() != ModDimensions.DUNGEON_KEY)
        {
            return;
        }
        if (level.getGameTime() % CHECK_INTERVAL_TICKS != 0)
        {
            return;
        }

        for (ServerPlayer player : level.players())
        {
            BlockPos playerPos = player.blockPosition();
            int chunkRadius = (int) Math.ceil(RANGE / 16.0) + 1;
            int centerX = playerPos.getX() >> 4;
            int centerZ = playerPos.getZ() >> 4;
            for (int dx = -chunkRadius; dx <= chunkRadius; dx++)
            {
                for (int dz = -chunkRadius; dz <= chunkRadius; dz++)
                {
                    LevelChunk chunk = level.getChunkSource().getChunkNow(centerX + dx, centerZ + dz);
                    if (chunk == null)
                    {
                        continue;
                    }
                    for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet())
                    {
                        if (!(entry.getValue() instanceof SpawnerBlockEntity spawnerBe))
                        {
                            continue;
                        }
                        BlockPos pos = entry.getKey();
                        if (pos.distSqr(playerPos) > RANGE_SQR)
                        {
                            continue;
                        }
                        tryFire(level, pos, spawnerBe);
                    }
                }
            }
        }
    }

    private static void tryFire(ServerLevel level, BlockPos pos, SpawnerBlockEntity spawnerBe)
    {
        long now = level.getGameTime();
        Long next = nextAttemptTick.get(pos);
        if (next != null && now < next)
        {
            return;
        }

        CompoundTag tag = spawnerBe.saveWithoutMetadata();
        CompoundTag entityTag = tag.getCompound("SpawnData").getCompound("entity");
        String entityId = entityTag.getString("id");
        Optional<EntityType<?>> typeOpt = EntityType.byString(entityId);
        if (typeOpt.isEmpty())
        {
            LOGGER.warn("Spawner at {} has no valid entity id in its SpawnData ('{}') - skipping", pos, entityId);
            nextAttemptTick.put(pos, now + 200);
            return;
        }
        EntityType<?> type = typeOpt.get();

        int maxNearby = tag.contains("MaxNearbyEntities") ? tag.getShort("MaxNearbyEntities") : 6;
        int spawnCount = tag.contains("SpawnCount") ? tag.getShort("SpawnCount") : 4;
        int spawnRange = tag.contains("SpawnRange") ? tag.getShort("SpawnRange") : 4;
        int minDelay = tag.contains("MinSpawnDelay") ? tag.getShort("MinSpawnDelay") : 200;
        int maxDelay = tag.contains("MaxSpawnDelay") ? tag.getShort("MaxSpawnDelay") : 800;

        AABB nearbyBox = new AABB(pos).inflate(spawnRange + 4.0);
        List<Entity> nearby = level.getEntities((Entity) null, nearbyBox, e -> e.getType() == type);
        RandomSource random = level.getRandom();
        if (nearby.size() >= maxNearby)
        {
            nextAttemptTick.put(pos, now + minDelay);
            return;
        }

        boolean spawnedAny = false;
        for (int i = 0; i < spawnCount; i++)
        {
            double x = pos.getX() + 0.5 + (random.nextDouble() - random.nextDouble()) * spawnRange;
            double y = pos.getY() + random.nextInt(3) - 1;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - random.nextDouble()) * spawnRange;

            Entity entity = type.create(level);
            if (entity == null)
            {
                continue;
            }
            entity.moveTo(x, y, z, random.nextFloat() * 360F, 0F);
            if (entity instanceof Mob mob)
            {
                if (!level.noCollision(mob))
                {
                    continue;
                }
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(x, y, z)), MobSpawnType.SPAWNER, null, null);
            }
            level.addFreshEntity(entity);
            spawnedAny = true;
        }

        if (spawnedAny)
        {
            level.levelEvent(2004, pos, 0);
        }
        nextAttemptTick.put(pos, now + minDelay + random.nextInt(Math.max(1, maxDelay - minDelay)));
    }
}
