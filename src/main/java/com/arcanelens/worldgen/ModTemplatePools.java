package com.arcanelens.worldgen;

import com.arcanelens.ArcaneLens;
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

public class ModTemplatePools
{
    public static final ResourceKey<StructureTemplatePool> ENTRANCE =
            ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(ArcaneLens.MODID, "castle_dungeon/entrance"));
    public static final ResourceKey<StructureTemplatePool> ROOMS =
            ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(ArcaneLens.MODID, "castle_dungeon/rooms"));
    public static final ResourceKey<StructureTemplatePool> ENTRANCE_CAP =
            ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(ArcaneLens.MODID, "castle_dungeon/entrance_cap"));
    public static final ResourceKey<StructureTemplatePool> FAITH_SHRINE_START =
            ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(ArcaneLens.MODID, "faith_shrine/start"));
    public static final ResourceKey<StructureTemplatePool> VESSEL_HOLLOW_ROOMS =
            ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(ArcaneLens.MODID, "vessel_hollow/rooms"));
    public static final ResourceKey<StructureTemplatePool> VESSEL_LAIR_ENTRANCE =
            ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(ArcaneLens.MODID, "vessel_lair/entrance"));
    // Not actually empty - this key has to be "arcanelens:empty" specifically because that's the literal
    // Pool value already baked into the jigsaw blocks inside vessel_lair_entrance.nbt/vessel_lair_room_1.nbt/
    // vessel_lair_room_2.nbt (hand-built with a structure block before this pool existed in code - verified
    // via direct NBT inspection). Renaming this key wouldn't rename anything inside those already-exported
    // NBT files, so it has to match what's already there rather than the other way around.
    public static final ResourceKey<StructureTemplatePool> VESSEL_LAIR_ROOMS =
            ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(ArcaneLens.MODID, "empty"));
    // Guarantees the tower is never just entrance+boss with zero middle rooms - see the registration
    // below for the full "why". Requires vessel_lair_entrance.nbt's own outgoing jigsaw to have its Pool
    // field repointed at this key in-game (a one-field edit + re-export, not a rebuild) - until that's
    // done, this pool exists in code but nothing actually references it yet.
    public static final ResourceKey<StructureTemplatePool> VESSEL_LAIR_FIRST_HOP =
            ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(ArcaneLens.MODID, "vessel_lair/first_hop"));

    public static void bootstrap(BootstapContext<StructureTemplatePool> context)
    {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> emptyPool = pools.getOrThrow(Pools.EMPTY);

        context.register(ENTRANCE, new StructureTemplatePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":castle_dungeon_entrance").apply(StructureTemplatePool.Projection.RIGID), 1))));

        context.register(ROOMS, new StructureTemplatePool(
                emptyPool,
                List.of(
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":castle_dungeon_tunnel").apply(StructureTemplatePool.Projection.RIGID), 2),
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":castle_dungeon_tunnel2").apply(StructureTemplatePool.Projection.RIGID), 2),
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":castle_dungeon_tunnel3").apply(StructureTemplatePool.Projection.RIGID), 1),
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":castle_dungeon_corner").apply(StructureTemplatePool.Projection.RIGID), 2),
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":castle_loot_room").apply(StructureTemplatePool.Projection.RIGID), 1),
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":arcane_ore_mine").apply(StructureTemplatePool.Projection.RIGID), 1),
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":castle_dungeon_fight_room").apply(StructureTemplatePool.Projection.RIGID), 2),
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":castle_dungeon_mana_room").apply(StructureTemplatePool.Projection.RIGID), 1),
                        // Rare - weight 1 is the same minimum unit already used for loot_room/arcane_ore_mine/
                        // mana_room/boss_entrance, the pool's existing "rare" tier relative to the weight-2
                        // tunnels/corners/fight_rooms.
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":research_room").apply(StructureTemplatePool.Projection.RIGID), 1),
                        // The boss entrance is a normal-sized room like any other - it just also contains an
                        // ArenaTriggerBlock (placed by hand in the build) that pastes the much larger arena
                        // template directly, the first time this room's chunk loads for real. See
                        // ArenaTriggerBlockEntity for why: a custom StructureProcessorType (the original
                        // approach) can't be registered by mods - BuiltInRegistries.STRUCTURE_PROCESSOR
                        // freezes before Forge ever constructs mod instances.
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":castle_dungeon_boss_entrance").apply(StructureTemplatePool.Projection.RIGID), 1))));

        context.register(ENTRANCE_CAP, new StructureTemplatePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":library").apply(StructureTemplatePool.Projection.RIGID), 1))));

        context.register(FAITH_SHRINE_START, new StructureTemplatePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":faith_shrine").apply(StructureTemplatePool.Projection.RIGID), 1))));

        // Real hand-built art, single-room sites (no jigsaw connectors needed - same "one static piece"
        // shape FAITH_SHRINE_START already uses, just with more than one weighted variant for repeat-visit
        // variety). Surface-placed like the old placeholder this replaces, not buried - see
        // ModStructures.VESSEL_HOLLOW.
        context.register(VESSEL_HOLLOW_ROOMS, new StructureTemplatePool(
                emptyPool,
                List.of(
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":vessel_hollow_ruin_1").apply(StructureTemplatePool.Projection.RIGID), 1),
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":vessel_hollow_ruin_2").apply(StructureTemplatePool.Projection.RIGID), 1))));

        context.register(VESSEL_LAIR_ENTRANCE, new StructureTemplatePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":vessel_lair_entrance").apply(StructureTemplatePool.Projection.RIGID), 1))));

        // Room 1 and Room 2 both loop back into this same pool via their own outgoing jigsaw (a plain
        // random room each hop), while the boss room has no outgoing jigsaw of its own - once picked, that
        // branch just ends there, with its built-in Command Trigger handling the actual boss encounter.
        // Weighted so the boss room is picked roughly as often as both rooms combined, keeping the tower
        // short on average (a few hops at most) - VesselLairPlacer's max_depth is a generous backstop, but
        // a low weight here would make relying on hitting that backstop far more likely than it should be.
        context.register(VESSEL_LAIR_ROOMS, new StructureTemplatePool(
                emptyPool,
                List.of(
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":vessel_lair_room_1").apply(StructureTemplatePool.Projection.RIGID), 1),
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":vessel_lair_room_2").apply(StructureTemplatePool.Projection.RIGID), 1),
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":vessel_lair_boss_room").apply(StructureTemplatePool.Projection.RIGID), 3))));

        // Room 1/Room 2 only, no boss room - the entrance's own outgoing jigsaw should point here instead
        // of directly at VESSEL_LAIR_ROOMS, so the very first hop after the entrance can never be the boss
        // room. Once past that first hop, Room 1/Room 2's own outgoing jigsaws still point at the full
        // VESSEL_LAIR_ROOMS pool above (unchanged), so the tower can still end at 3 pieces (entrance + 1
        // room + boss) or keep chaining longer - only the guaranteed *minimum* changes, not the rest of the
        // length distribution.
        context.register(VESSEL_LAIR_FIRST_HOP, new StructureTemplatePool(
                emptyPool,
                List.of(
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":vessel_lair_room_1").apply(StructureTemplatePool.Projection.RIGID), 1),
                        Pair.of(StructurePoolElement.single(ArcaneLens.MODID + ":vessel_lair_room_2").apply(StructureTemplatePool.Projection.RIGID), 1))));
    }
}
