package com.arcanelens.worldgen;

import com.arcanelens.entity.SleepingGodEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.living.MobSpawnEvent;

/**
 * Blocks natural monster spawns inside boss arenas. The dungeon-wide "no monster spawns" override in
 * ModStructures only covers the jigsaw-tracked pieces (entrance, tunnels, rooms) - the arena is pasted
 * directly by ArenaTriggerBlockEntity, bypassing the jigsaw pool system entirely, so it's invisible to that
 * mechanism. This covers the gap using the arena bounds recorded in BossArenaSavedData.
 */
public class ArenaSpawnHandler
{
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event)
    {
        if (event.getEntity() instanceof SleepingGodEntity)
        {
            return;
        }
        if (event.getEntity().getType().getCategory() != MobCategory.MONSTER)
        {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel serverLevel))
        {
            return;
        }

        BlockPos pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());
        if (BossArenaSavedData.get(serverLevel).isInsideAnyArena(pos))
        {
            event.setSpawnCancelled(true);
        }
    }
}
