package com.arcanelens.registry;

import com.arcanelens.ArcaneLens;
import com.arcanelens.entity.AureliaCompanionEntity;
import com.arcanelens.entity.BrokenVesselEntity;
import com.arcanelens.entity.boss.AureliaBossEntity;
import com.arcanelens.entity.boss.FlorianBossEntity;
import com.arcanelens.entity.FlorianCompanionEntity;
import com.arcanelens.entity.HungerIdolEntity;
import com.arcanelens.entity.SleepingGodEntity;
import com.arcanelens.entity.TheHungerEntity;
import com.arcanelens.entity.TheaterCloneEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = ArcaneLens.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityTypes
{
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ArcaneLens.MODID);

    public static final RegistryObject<EntityType<SleepingGodEntity>> SLEEPING_GOD = ENTITY_TYPES.register("sleeping_god",
            () -> EntityType.Builder.of(SleepingGodEntity::new, MobCategory.MONSTER)
                    .sized(1.5f, 4.2f) // placeholder boss-scale hitbox; retune once the real model exists
                    .clientTrackingRange(12)
                    .fireImmune()
                    .build(new ResourceLocation(ArcaneLens.MODID, "sleeping_god").toString()));

    public static final RegistryObject<EntityType<BrokenVesselEntity>> BROKEN_VESSEL = ENTITY_TYPES.register("broken_vessel",
            () -> EntityType.Builder.of(BrokenVesselEntity::new, MobCategory.MONSTER)
                    .sized(0.9f, 2.2f) // placeholder boss-scale hitbox; retune once real proportions are confirmed
                    .clientTrackingRange(12)
                    .fireImmune()
                    .build(new ResourceLocation(ArcaneLens.MODID, "broken_vessel").toString()));

    // A brief, non-hostile apparition (see TheHungerEntity) - no AI/attributes needed since it's a plain
    // Entity, not a Mob. Never persisted (it only ever lives a few seconds) and never manually summonable.
    public static final RegistryObject<EntityType<TheHungerEntity>> THE_HUNGER = ENTITY_TYPES.register("the_hunger",
            () -> EntityType.Builder.<TheHungerEntity>of(TheHungerEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.5f)
                    .clientTrackingRange(10)
                    .noSave()
                    .noSummon()
                    .build(new ResourceLocation(ArcaneLens.MODID, "the_hunger").toString()));

    // The permanent resident at the top of the Hunger Tower (see HungerTowerPlacer) - unlike THE_HUNGER this
    // is meant to persist across restarts, so no .noSave(); still never manually summonable.
    public static final RegistryObject<EntityType<HungerIdolEntity>> HUNGER_IDOL = ENTITY_TYPES.register("hunger_idol",
            () -> EntityType.Builder.<HungerIdolEntity>of(HungerIdolEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.5f)
                    .clientTrackingRange(10)
                    .noSummon()
                    .build(new ResourceLocation(ArcaneLens.MODID, "hunger_idol").toString()));

    // Stage Clone's decoy (see TheaterAbilityHandler) - a plain Mob, no special hostility/AI, summoned
    // and directly discarded by mod logic, never naturally spawned.
    public static final RegistryObject<EntityType<TheaterCloneEntity>> THEATER_CLONE = ENTITY_TYPES.register("theater_clone",
            () -> EntityType.Builder.<TheaterCloneEntity>of(TheaterCloneEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(10)
                    .noSave()
                    .noSummon()
                    .build(new ResourceLocation(ArcaneLens.MODID, "theater_clone").toString()));

    // Aurelia's "My Love" summon (see item/AureliaSummonHandler) - a real combat companion, so
    // MobCategory.CREATURE rather than MISC, but still .noSave()/.noSummon() since she's temporary and
    // only ever created by mod logic, never naturally spawned or meant to persist across a restart.
    public static final RegistryObject<EntityType<AureliaCompanionEntity>> AURELIA_COMPANION = ENTITY_TYPES.register("aurelia_companion",
            () -> EntityType.Builder.of(AureliaCompanionEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f) // exact player hitbox (Player.STANDING_DIMENSIONS)
                    .clientTrackingRange(10)
                    .noSave()
                    .noSummon()
                    .build(new ResourceLocation(ArcaneLens.MODID, "aurelia_companion").toString()));

    // Aurelia's God Challenge Hub boss fight (see entity/boss/AureliaBossEntity) - a real persistent
    // boss like Sleeping God/Broken Vessel (no .noSave()/.noSummon()), only ever spawned by
    // GodChallengeService, never naturally. Hitbox scaled up from the companion's exact-player-size
    // proportions to roughly match the boss's larger 0.8x render scale - placeholder, retune once the
    // real model's actual boss-scale proportions are confirmed.
    public static final RegistryObject<EntityType<AureliaBossEntity>> AURELIA_BOSS = ENTITY_TYPES.register("aurelia_boss",
            () -> EntityType.Builder.of(AureliaBossEntity::new, MobCategory.MONSTER)
                    .sized(1.0f, 2.9f)
                    .clientTrackingRange(12)
                    .build(new ResourceLocation(ArcaneLens.MODID, "aurelia_boss").toString()));

    // Florian's God Challenge Hub boss fight (see entity/boss/FlorianBossEntity) - persistent boss like
    // Aurelia's, only spawned by GodChallengeService. Hitbox roughly matches the stag's body/head height.
    public static final RegistryObject<EntityType<FlorianBossEntity>> FLORIAN_BOSS = ENTITY_TYPES.register("florian_boss",
            () -> EntityType.Builder.of(FlorianBossEntity::new, MobCategory.MONSTER)
                    .sized(1.4f, 2.4f)
                    .clientTrackingRange(12)
                    .build(new ResourceLocation(ArcaneLens.MODID, "florian_boss").toString()));

    // The temporary summon from Florian's Antler - same lifecycle as AURELIA_COMPANION (never saved or
    // naturally spawned), a smaller hitbox to match its 0.7x render scale.
    public static final RegistryObject<EntityType<FlorianCompanionEntity>> FLORIAN_COMPANION = ENTITY_TYPES.register("florian_companion",
            () -> EntityType.Builder.of(FlorianCompanionEntity::new, MobCategory.CREATURE)
                    .sized(1.0f, 1.7f)
                    .clientTrackingRange(10)
                    .noSave()
                    .noSummon()
                    .build(new ResourceLocation(ArcaneLens.MODID, "florian_companion").toString()));

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event)
    {
        event.put(FLORIAN_BOSS.get(), FlorianBossEntity.createAttributes().build());
        event.put(FLORIAN_COMPANION.get(), FlorianCompanionEntity.createAttributes().build());
        event.put(SLEEPING_GOD.get(), SleepingGodEntity.createAttributes().build());
        event.put(BROKEN_VESSEL.get(), BrokenVesselEntity.createAttributes().build());
        event.put(THEATER_CLONE.get(), TheaterCloneEntity.createAttributes().build());
        event.put(AURELIA_COMPANION.get(), AureliaCompanionEntity.createAttributes().build());
        event.put(AURELIA_BOSS.get(), AureliaBossEntity.createAttributes().build());
    }
}
