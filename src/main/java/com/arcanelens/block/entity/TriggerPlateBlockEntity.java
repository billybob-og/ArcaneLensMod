package com.arcanelens.block.entity;

import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * A pure signal relay - an invisible alternative to tripwire. Holds no mode state of its own; every tick it
 * detects a player touching it, it floods outward through every directly-connected chain of Trigger Plates
 * (like a web - a plate can pass the touch on to another plate, not just a Command Trigger directly), firing
 * every CommandTriggerBlockEntity the flood reaches and not searching past it. Runs as an instant flood-fill
 * each tick (not one hop per tick) so a long chain doesn't feel laggy, and re-touching an already-touched
 * plate the same tick is skipped (visited set) so a looped layout of plates can't recurse forever.
 */
public class TriggerPlateBlockEntity extends BlockEntity
{
    public TriggerPlateBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.TRIGGER_PLATE.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos)
    {
        if (!(level instanceof ServerLevel serverLevel))
        {
            return;
        }

        boolean touchedNow = !serverLevel.getEntitiesOfClass(Player.class, new AABB(pos)).isEmpty();
        if (!touchedNow)
        {
            return;
        }

        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        visited.add(pos);
        queue.add(pos);

        while (!queue.isEmpty())
        {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values())
            {
                BlockPos neighborPos = current.relative(direction);
                if (!visited.add(neighborPos))
                {
                    continue;
                }

                BlockEntity neighbor = serverLevel.getBlockEntity(neighborPos);
                if (neighbor instanceof CommandTriggerBlockEntity target)
                {
                    target.markRelayedTouch();
                    // Don't keep searching past a Command Trigger - the web stops here on this branch.
                }
                else if (neighbor instanceof TriggerPlateBlockEntity)
                {
                    queue.add(neighborPos);
                }
            }
        }
    }
}
