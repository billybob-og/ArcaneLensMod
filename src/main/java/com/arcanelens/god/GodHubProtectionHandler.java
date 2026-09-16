package com.arcanelens.god;

import com.arcanelens.worldgen.ModDimensions;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.BlockEvent;

/** The God Challenge Hub is a fixed, hand-built space (entrances, arenas) that has to stay intact for
 * the challenge blocks/arena structures to keep working - block-breaking is refused there entirely,
 * the same "protect a fixed utility dimension" idea as ArenaSpawnHandler protects arena bounds from
 * monster spawns, just for player breaking instead. */
public class GodHubProtectionHandler
{
    public static void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if (event.getLevel() instanceof Level level && level.dimension() == ModDimensions.GOD_CHALLENGE_HUB_KEY)
        {
            event.setCanceled(true);
        }
    }
}
