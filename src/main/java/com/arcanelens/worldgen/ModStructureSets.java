package com.arcanelens.worldgen;

import com.arcanelens.ArcaneLens;
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

public class ModStructureSets
{
    public static final ResourceKey<StructureSet> CASTLE_DUNGEON =
            ResourceKey.create(Registries.STRUCTURE_SET, new ResourceLocation(ArcaneLens.MODID, "castle_dungeon"));
    public static final ResourceKey<StructureSet> FAITH_SHRINE =
            ResourceKey.create(Registries.STRUCTURE_SET, new ResourceLocation(ArcaneLens.MODID, "faith_shrine"));
    public static final ResourceKey<StructureSet> VESSEL_HOLLOW =
            ResourceKey.create(Registries.STRUCTURE_SET, new ResourceLocation(ArcaneLens.MODID, "vessel_hollow"));

    public static void bootstrap(BootstapContext<StructureSet> context)
    {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        Holder<Structure> castleDungeon = structures.getOrThrow(ModStructures.CASTLE_DUNGEON);

        context.register(CASTLE_DUNGEON, new StructureSet(
                castleDungeon,
                new RandomSpreadStructurePlacement(24, 10, RandomSpreadType.LINEAR, 1234567)));

        Holder<Structure> faithShrine = structures.getOrThrow(ModStructures.FAITH_SHRINE);

        // Deliberately rarer/more spread out than the dungeon - this is meant to be a special, endgame-adjacent
        // find, not something a player stumbles into every other trip.
        context.register(FAITH_SHRINE, new StructureSet(
                faithShrine,
                new RandomSpreadStructurePlacement(48, 16, RandomSpreadType.LINEAR, 7654321)));

        Holder<Structure> vesselHollow = structures.getOrThrow(ModStructures.VESSEL_HOLLOW);

        // Tighter than CASTLE_DUNGEON's spacing (was the same 24/10, then 16/7 - still too sparse in
        // playtesting). ~4x denser than the original ((24/12)^2), separation kept at roughly the same
        // proportion of spacing throughout so sites still don't cluster right on top of each other.
        context.register(VESSEL_HOLLOW, new StructureSet(
                vesselHollow,
                new RandomSpreadStructurePlacement(12, 5, RandomSpreadType.LINEAR, 9876543)));
    }
}
