package com.arcanelens.util;

import com.arcanelens.block.entity.LensPedestalBlockEntity;
import com.arcanelens.block.entity.SoulPedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PedestalLinker
{
    public static List<BlockPos> findLinkedLensPedestals(Level level, BlockPos origin, int radius)
    {
        return findLinked(level, origin, radius, LensPedestalBlockEntity.class);
    }

    public static List<BlockPos> findLinkedSoulPedestals(Level level, BlockPos origin, int radius)
    {
        return findLinked(level, origin, radius, SoulPedestalBlockEntity.class);
    }

    public static List<BlockPos> findLinked(Level level, BlockPos origin, int radius, Class<? extends BlockEntity> type)
    {
        List<BlockPos> result = new ArrayList<>();

        int minChunkX = (origin.getX() - radius) >> 4;
        int maxChunkX = (origin.getX() + radius) >> 4;
        int minChunkZ = (origin.getZ() - radius) >> 4;
        int maxChunkZ = (origin.getZ() + radius) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++)
        {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++)
            {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null)
                {
                    continue;
                }

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet())
                {
                    BlockPos pos = entry.getKey();
                    if (type.isInstance(entry.getValue()) && isWithinRadius(origin, pos, radius))
                    {
                        result.add(pos.immutable());
                    }
                }
            }
        }

        return result;
    }

    private static boolean isWithinRadius(BlockPos origin, BlockPos pos, int radius)
    {
        return Math.abs(pos.getX() - origin.getX()) <= radius
                && Math.abs(pos.getY() - origin.getY()) <= radius
                && Math.abs(pos.getZ() - origin.getZ()) <= radius;
    }
}
