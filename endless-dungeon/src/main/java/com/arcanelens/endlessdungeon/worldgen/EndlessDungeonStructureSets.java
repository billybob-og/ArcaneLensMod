package com.arcanelens.endlessdungeon.worldgen;

import com.arcanelens.endlessdungeon.EndlessDungeonMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

public class EndlessDungeonStructureSets
{
    public static final ResourceKey<StructureSet> DUNGEON_PORTAL =
            ResourceKey.create(Registries.STRUCTURE_SET, new ResourceLocation(EndlessDungeonMod.MODID, "dungeon_portal"));

    public static void bootstrap(BootstapContext<StructureSet> context)
    {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        Holder<Structure> dungeonPortal = structures.getOrThrow(EndlessDungeonStructures.DUNGEON_PORTAL);

        // Findable but not trivial - between the base mod's CASTLE_DUNGEON (24/10, common) and FAITH_SHRINE
        // (48/16, deliberately rare/endgame-adjacent). Tune once actually explored for one in-game.
        context.register(DUNGEON_PORTAL, new StructureSet(
                dungeonPortal,
                new RandomSpreadStructurePlacement(32, 12, RandomSpreadType.LINEAR, 8675309)));
    }
}
