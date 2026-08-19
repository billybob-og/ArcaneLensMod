package com.arcanelens.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tracks which castle_dungeon instances (keyed by their structure start's chunk position) have already had
 * their boss arena pasted, so a dungeon that happens to roll the boss entrance room more than once (or ever,
 * once the boosted testing weight is reverted) only ever gets one real arena - a second copy just becomes a
 * dead-end room with no arena below it.
 *
 * Also records each pasted arena's world bounding box: the arena is pasted directly by code rather than
 * through the jigsaw pool system, so it's invisible to the dungeon structure's own recorded pieces/bounding
 * box - meaning the normal structure-wide "no monster spawns" override (ModStructures' spawnOverrides) never
 * covers the arena's space. ArenaSpawnHandler uses these bounds to block natural monster spawns there too.
 */
public class BossArenaSavedData extends SavedData
{
    private static final String DATA_NAME = "arcanelens_boss_arenas";

    private final Set<Long> dungeonsWithArena = new HashSet<>();
    private final List<int[]> arenaBounds = new ArrayList<>();

    public static BossArenaSavedData load(CompoundTag tag)
    {
        BossArenaSavedData data = new BossArenaSavedData();
        for (Tag entry : tag.getList("DungeonsWithArena", Tag.TAG_LONG))
        {
            data.dungeonsWithArena.add(((LongTag) entry).getAsLong());
        }
        for (Tag entry : tag.getList("ArenaBounds", Tag.TAG_INT_ARRAY))
        {
            data.arenaBounds.add(((IntArrayTag) entry).getAsIntArray());
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        ListTag dungeonList = new ListTag();
        for (long key : dungeonsWithArena)
        {
            dungeonList.add(LongTag.valueOf(key));
        }
        tag.put("DungeonsWithArena", dungeonList);

        ListTag boundsList = new ListTag();
        for (int[] bounds : arenaBounds)
        {
            boundsList.add(new IntArrayTag(bounds));
        }
        tag.put("ArenaBounds", boundsList);
        return tag;
    }

    public static BossArenaSavedData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(BossArenaSavedData::load, BossArenaSavedData::new, DATA_NAME);
    }

    /** True if this is the first time this dungeon instance has tried to place its arena (and records it as used). */
    public boolean tryClaim(ChunkPos dungeonOrigin)
    {
        boolean isNew = dungeonsWithArena.add(dungeonOrigin.toLong());
        if (isNew)
        {
            setDirty();
        }
        return isNew;
    }

    public void recordArenaBounds(BoundingBox box)
    {
        arenaBounds.add(new int[] { box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ() });
        setDirty();
    }

    public boolean isInsideAnyArena(BlockPos pos)
    {
        for (int[] b : arenaBounds)
        {
            if (pos.getX() >= b[0] && pos.getX() <= b[3]
                    && pos.getY() >= b[1] && pos.getY() <= b[4]
                    && pos.getZ() >= b[2] && pos.getZ() <= b[5])
            {
                return true;
            }
        }
        return false;
    }
}
