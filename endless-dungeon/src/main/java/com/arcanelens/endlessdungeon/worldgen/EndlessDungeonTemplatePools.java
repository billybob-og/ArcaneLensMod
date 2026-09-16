package com.arcanelens.endlessdungeon.worldgen;

import com.arcanelens.endlessdungeon.EndlessDungeonMod;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.List;

public class EndlessDungeonTemplatePools
{
    public static final ResourceKey<StructureTemplatePool> DUNGEON_PORTAL_START =
            ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(EndlessDungeonMod.MODID, "dungeon_portal/start"));

    public static void bootstrap(BootstapContext<StructureTemplatePool> context)
    {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> emptyPool = pools.getOrThrow(Pools.EMPTY);

        // Real hand-built art, a single-room site - same "one static piece, no jigsaw connectors" shape as
        // the base mod's FAITH_SHRINE_START/VESSEL_HOLLOW_ROOMS. Its own portal block has to be the real,
        // registered DungeonPortalBlock already in the exported NBT (unlike the dungeon-side entrance
        // template's portal-marker-jigsaw trick - vanilla structure-gen has no final_state-style conversion
        // pass), so this template needs to be built in a dev client that has this addon installed.
        context.register(DUNGEON_PORTAL_START, new StructureTemplatePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.single(EndlessDungeonMod.MODID + ":dungeon_portal_site").apply(StructureTemplatePool.Projection.RIGID), 1))));
    }
}
