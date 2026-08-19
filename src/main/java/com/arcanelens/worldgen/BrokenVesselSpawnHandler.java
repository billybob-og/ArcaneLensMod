package com.arcanelens.worldgen;

import com.arcanelens.entity.BrokenVesselEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.living.MobSpawnEvent;

/**
 * The whole Warped Hollow is curated content, same reasoning as PocketDimensionSpawnHandler - cancel every
 * natural monster spawn unconditionally rather than tracking bounding boxes the way ArenaSpawnHandler does
 * for the castle dungeon's per-instance arenas. Any deliberate hostiles live in the hand-built jigsaw rooms
 * or the lair itself, spawned directly rather than through natural spawning.
 *
 * <p>Broken Vessel itself is explicitly excluded - FinalizeSpawn fires for every mob spawn that goes
 * through Mob.finalizeSpawn(), which includes the lair's own /summon command (run by its built-in Command
 * Trigger), not just natural spawns. Without this exclusion, the command reports success in chat (the
 * entity object already exists by that point) but the boss never actually appears, cancelled by this same
 * handler moments later - confirmed by testing.</p>
 */
public class BrokenVesselSpawnHandler
{
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event)
    {
        if (event.getLevel() instanceof ServerLevel serverLevel
                && serverLevel.dimension() == ModDimensions.BROKEN_VESSEL_KEY
                && !(event.getEntity() instanceof BrokenVesselEntity))
        {
            event.setSpawnCancelled(true);
        }
    }
}
