package com.arcanelens;

import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ArcaneLens.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue BASE_LENS_MAX_MANA = BUILDER
            .comment("Max mana capacity of a freshly-crafted, unmerged Magic Lens")
            .defineInRange("baseLensMaxMana", 1000, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue LINK_RADIUS = BUILDER
            .comment("Radius (in blocks) a Soul Pedestal scans for nearby Lens Pedestals to link to")
            .defineInRange("linkRadius", 2, 1, 16);

    private static final ForgeConfigSpec.IntValue LINK_RESCAN_INTERVAL_TICKS = BUILDER
            .comment("How often (in ticks) a Soul Pedestal rescans for linked Lens Pedestals")
            .defineInRange("linkRescanIntervalTicks", 40, 1, 72000);

    private static final ForgeConfigSpec.IntValue SOUL_SAND_BURN_TICKS = BUILDER
            .comment("Ticks it takes a lit Soul Pedestal to burn through one banked soul sand")
            .defineInRange("soulSandBurnTicks", 6000, 1, 72000);

    private static final ForgeConfigSpec.IntValue CONVERSION_INTERVAL_TICKS = BUILDER
            .comment("Ticks between each mana-conversion attempt on a lit Soul Pedestal's inserted item")
            .defineInRange("conversionIntervalTicks", 100, 1, 72000);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> RARITY_MANA_VALUES = BUILDER
            .comment("Mana granted per item consumed by a Soul Pedestal, keyed by item rarity, as \"RARITY:amount\" entries")
            .defineList("rarityManaValues", List.of("COMMON:5", "UNCOMMON:15", "RARE:40", "EPIC:100"), o -> o instanceof String);

    private static final ForgeConfigSpec.IntValue LENS_COMBINE_PRICE_BASE = BUILDER
            .comment("Base raw arcane material cost of a lens combine; scales by 1.75^(newMergeCount-1) per the resolved merge-cost rule")
            .defineInRange("lensCombinePriceBase", 4, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> RARITY_FAITH_VALUES = BUILDER
            .comment("Faith granted per item burned in soul fire near a Faith Altar, keyed by item rarity, as \"RARITY:amount\" entries")
            .defineList("rarityFaithValues", List.of("COMMON:1", "UNCOMMON:2", "RARE:5", "EPIC:12"), o -> o instanceof String);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> RARITY_DIMINISH_STACKS = BUILDER
            .comment("A given item's Faith yield halves every this many stacks of it burned through the same Faith Altar, "
                    + "keyed by item rarity, as \"RARITY:stacks\" entries - rarer items tolerate more stacks before decaying, "
                    + "since they're naturally scarcer; keeps bulk-farming a cheap item (e.g. dirt, kelp) from being worthwhile")
            .defineList("rarityDiminishStacks", List.of("COMMON:3", "UNCOMMON:6", "RARE:12", "EPIC:24"), o -> o instanceof String);

    private static final ForgeConfigSpec.IntValue FAITH_DIMINISH_STACKS_DEFAULT = BUILDER
            .comment("Fallback stacks-before-halving for any rarity not listed in rarityDiminishStacks")
            .defineInRange("faithDiminishStacksDefault", 3, 1, 1000);

    private static final ForgeConfigSpec.IntValue FAITH_SCAN_INTERVAL_TICKS = BUILDER
            .comment("Ticks between each scan a Faith Altar performs for burning items nearby")
            .defineInRange("faithScanIntervalTicks", 10, 1, 200);

    private static final ForgeConfigSpec.IntValue FAITH_SCAN_RADIUS = BUILDER
            .comment("Radius (in blocks) a Faith Altar scans around itself for items burning in soul fire (an ~8-block-wide cube by default)")
            .defineInRange("faithScanRadius", 4, 1, 16);

    private static final ForgeConfigSpec.IntValue BASE_FLIGHT_SECONDS = BUILDER
            .comment("Seconds of flight the full sleeping god armor set grants on its own, before any Faith-based bonus")
            .defineInRange("baseFlightSeconds", 3, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue FAITH_PER_FLIGHT_SECOND = BUILDER
            .comment("Faith required per additional second of flight beyond the base (see baseFlightSeconds) - "
                    + "this only caps how long a single flight can last; Faith itself is never spent by flying")
            .defineInRange("faithPerFlightSecond", 15, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue FLIGHT_COOLDOWN_TICKS = BUILDER
            .comment("Ticks the sleeping god armor's flight ability needs to recharge after each use (whether it ran out or was landed early)")
            .defineInRange("flightCooldownTicks", 1200, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue FLIGHT_LANDED_GRACE_TICKS = BUILDER
            .comment("Ticks after landing (before hitting the flight time cap) that the remaining flight time is "
                    + "preserved instead of starting the cooldown - taking off again within this window resumes "
                    + "exactly where you left off; letting it expire while grounded starts the full cooldown")
            .defineInRange("flightLandedGraceTicks", 100, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue WATCHING_CHECK_INTERVAL_TICKS = BUILDER
            .comment("Ticks between each roll of The Hunger's watching chance, per player who has defeated the sleeping god at least once")
            .defineInRange("watchingCheckIntervalTicks", 900, 1, 72000);

    private static final ForgeConfigSpec.DoubleValue BASE_WATCHING_CHANCE = BUILDER
            .comment("Base per-check chance (0-1) of a blessing/burden moment from The Hunger, before Faith/defeat scaling")
            .defineInRange("baseWatchingChance", 0.05, 0.0, 1.0);

    private static final ForgeConfigSpec.DoubleValue WATCHING_FAITH_SCALE = BUILDER
            .comment("Divisor for how much current Faith boosts the watching chance: multiplier = 1 + faith / watchingFaithScale")
            .defineInRange("watchingFaithScale", 100.0, 1.0, Double.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue MAX_WATCHING_CHANCE = BUILDER
            .comment("Hard cap (0-1) on the per-check watching chance, however high Faith/defeats push it - "
                    + "must stay above watchingEyesRevealThreshold or the eyes reveal becomes unreachable")
            .defineInRange("maxWatchingChance", 0.85, 0.0, 1.0);

    private static final ForgeConfigSpec.DoubleValue WATCHING_EYES_REVEAL_THRESHOLD = BUILDER
            .comment("Once a player's computed watching chance (0-1) first reaches this, their sun/moon permanently "
                    + "reveal The Hunger's eyes - a one-way milestone, never hidden again even if Faith later drops")
            .defineInRange("watchingEyesRevealThreshold", 0.75, 0.0, 1.0);

    private static final ForgeConfigSpec.IntValue FAITH_PER_HUNGER_TOKEN = BUILDER
            .comment("Faith a player must accumulate (after their eyes are revealed) before receiving another Token of the Hunger")
            .defineInRange("faithPerHungerToken", 100, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue BLESSING_BOON_DURATION_MULTIPLIER_PER_LEVEL = BUILDER
            .comment("Each level of The Hunger's Boon scales blessing effect duration by this much more: duration *= 1.0 + level * this")
            .defineInRange("blessingBoonDurationMultiplierPerLevel", 0.1, 0.0, Double.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue BURDEN_RESIST_CHANCE_PER_LEVEL = BUILDER
            .comment("Each level of The Hunger's Bindings shifts the blessing/burden coin flip toward blessing by this much (0-1)")
            .defineInRange("burdenResistChancePerLevel", 0.05, 0.0, 1.0);

    private static final ForgeConfigSpec.DoubleValue MAX_BURDEN_RESIST_CHANCE = BUILDER
            .comment("Hard cap (0-1) on the blessing chance from stacked Bindings levels - burdens never become fully impossible")
            .defineInRange("maxBurdenResistChance", 0.9, 0.5, 1.0);

    private static final ForgeConfigSpec.DoubleValue BONUS_BLESSING_CHANCE_PER_LEVEL = BUILDER
            .comment("Once Bindings levels push the blessing chance to its maxBurdenResistChance cap, further levels aren't "
                    + "wasted: each one past that point instead adds this much chance (0-1) of a second, independent "
                    + "blessing effect on top of a blessing moment - never rolled on burden moments")
            .defineInRange("bonusBlessingChancePerLevel", 0.01, 0.0, 1.0);

    private static final ForgeConfigSpec.DoubleValue MAX_BONUS_BLESSING_CHANCE = BUILDER
            .comment("Hard cap (0-1) on the bonus second-blessing chance from Bindings levels past the burden-resist cap")
            .defineInRange("maxBonusBlessingChance", 1.0, 0.0, 1.0);

    private static final ForgeConfigSpec.DoubleValue MAGNET_CHARM_RADIUS = BUILDER
            .comment("Radius (in blocks) the Magnet Charm pulls nearby dropped items from while equipped in a Curios charm slot")
            .defineInRange("magnetCharmRadius", 6.0, 1.0, 32.0);

    private static final ForgeConfigSpec.DoubleValue MAGNET_CHARM_PULL_STRENGTH = BUILDER
            .comment("Velocity added per tick toward the wearer for each item pulled by the Magnet Charm")
            .defineInRange("magnetCharmPullStrength", 0.15, 0.01, 2.0);

    private static final ForgeConfigSpec.IntValue FAITH_CHARM_INTERVAL_TICKS = BUILDER
            .comment("Ticks between each passive Faith trickle from the Faith Charm while equipped")
            .defineInRange("faithCharmIntervalTicks", 600, 1, 72000);

    private static final ForgeConfigSpec.IntValue FAITH_CHARM_AMOUNT_PER_INTERVAL = BUILDER
            .comment("Faith granted per trickle interval by the Faith Charm")
            .defineInRange("faithCharmAmountPerInterval", 1, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue MANA_CHARM_INTERVAL_TICKS = BUILDER
            .comment("Ticks between each passive mana trickle from the Mana Trickle Charm while equipped")
            .defineInRange("manaCharmIntervalTicks", 100, 1, 72000);

    private static final ForgeConfigSpec.IntValue MANA_CHARM_AMOUNT_PER_INTERVAL = BUILDER
            .comment("Mana granted per trickle interval, per Magic Lens carried, by the Mana Trickle Charm")
            .defineInRange("manaCharmAmountPerInterval", 2, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue MANA_BOOST_AMOUNT_PER_LEVEL = BUILDER
            .comment("Max mana added per Mana Boost skill tree level, applied directly to the MaxMana NBT of every "
                    + "Magic Lens the player holds (retroactively on purchase, and immediately on any lens crafted after)")
            .defineInRange("manaBoostAmountPerLevel", 1000, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue MANA_BOOST_MAX_LEVEL = BUILDER
            .comment("Max purchasable levels of the Mana Boost skill")
            .defineInRange("manaBoostMaxLevel", 3, 1, 100);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> MANA_BOOST_ARCANE_INK_COSTS = BUILDER
            .comment("Arcane Ink cost to buy each Mana Boost level, in order (index 0 = cost of level 1, etc.) - "
                    + "list length should match manaBoostMaxLevel")
            .defineList("manaBoostArcaneInkCosts", List.of("1", "1", "1"), o -> o instanceof String);

    private static final ForgeConfigSpec.DoubleValue COOLDOWN_REDUCTION_PER_LEVEL = BUILDER
            .comment("Spell cooldown reduction (0-1) per Cooldown Reduction skill tree level")
            .defineInRange("cooldownReductionPerLevel", 0.05, 0.0, 1.0);

    private static final ForgeConfigSpec.IntValue COOLDOWN_REDUCTION_MAX_LEVEL = BUILDER
            .comment("Max purchasable levels of the Cooldown Reduction skill")
            .defineInRange("cooldownReductionMaxLevel", 5, 1, 100);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> COOLDOWN_REDUCTION_ARCANE_INK_COSTS = BUILDER
            .comment("Arcane Ink cost to buy each Cooldown Reduction level, in order - "
                    + "list length should match cooldownReductionMaxLevel")
            .defineList("cooldownReductionArcaneInkCosts", List.of("1", "2", "3", "4", "5"), o -> o instanceof String);

    private static final ForgeConfigSpec.DoubleValue COST_REDUCTION_PER_LEVEL = BUILDER
            .comment("Spell mana cost reduction (0-1) per Cost Reduction skill tree level")
            .defineInRange("costReductionPerLevel", 0.05, 0.0, 1.0);

    private static final ForgeConfigSpec.IntValue COST_REDUCTION_MAX_LEVEL = BUILDER
            .comment("Max purchasable levels of the Cost Reduction skill")
            .defineInRange("costReductionMaxLevel", 5, 1, 100);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> COST_REDUCTION_ARCANE_INK_COSTS = BUILDER
            .comment("Arcane Ink cost to buy each Cost Reduction level, in order - "
                    + "list length should match costReductionMaxLevel")
            .defineList("costReductionArcaneInkCosts", List.of("1", "2", "3", "4", "5"), o -> o instanceof String);

    private static final ForgeConfigSpec.IntValue POCKET_DIMENSION_ARCANE_INK_COST = BUILDER
            .comment("Arcane Ink cost to unlock the Pocket Dimension spell (one-time, requires Mana Boost level 1 first)")
            .defineInRange("pocketDimensionArcaneInkCost", 3, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue POCKET_DIMENSION_EXPANSION_MAX_LEVEL = BUILDER
            .comment("Max purchasable levels of Pocket Dimension Expansion (level 1 = 12x12 room, level 2 = 15x15 room)")
            .defineInRange("pocketDimensionExpansionMaxLevel", 2, 1, 100);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> POCKET_DIMENSION_EXPANSION_ARCANE_INK_COSTS = BUILDER
            .comment("Arcane Ink cost to buy each Pocket Dimension Expansion level, in order - "
                    + "list length should match pocketDimensionExpansionMaxLevel")
            .defineList("pocketDimensionExpansionArcaneInkCosts", List.of("3", "5"), o -> o instanceof String);

    private static final ForgeConfigSpec.IntValue MAX_STORAGE_BLOCKS_PER_NETWORK = BUILDER
            .comment("Max Storage Blocks (Connectors don't count) allowed in one storage network - "
                    + "hard-floored at 15 so this can never be configured lower")
            .defineInRange("maxStorageBlocksPerNetwork", 15, 15, 256);

    private static final ForgeConfigSpec.IntValue SLOTS_PER_STORAGE_BLOCK = BUILDER
            .comment("Inventory slots each Storage Block contributes to its network's shared pool")
            .defineInRange("slotsPerStorageBlock", 5, 1, 64);

    private static final ForgeConfigSpec.IntValue SLOTS_PER_SLOT_UPGRADE = BUILDER
            .comment("Bonus pool slots granted per Slot Upgrade installed in a Terminal")
            .defineInRange("slotsPerSlotUpgrade", 1, 1, 64);

    private static final ForgeConfigSpec.IntValue STACK_SIZE_BONUS_PER_STACK_UPGRADE = BUILDER
            .comment("Max stack size added (on top of each item's own vanilla max) per Stack Upgrade installed in a Terminal")
            .defineInRange("stackSizeBonusPerStackUpgrade", 32, 1, 6400);

    private static final ForgeConfigSpec.IntValue STORAGE_SYSTEM_ARCANE_INK_COST = BUILDER
            .comment("Arcane Ink cost to unlock crafting the Storage Block and Terminal (one-time, requires Mana Boost level 1 first)")
            .defineInRange("storageSystemArcaneInkCost", 5, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue STORAGE_NETWORK_EXPANSION_MAX_LEVEL = BUILDER
            .comment("Max purchasable levels of Storage Network Expansion (each level raises the per-network "
                    + "Storage Block cap by storageBlocksPerExpansionLevel, on top of maxStorageBlocksPerNetwork)")
            .defineInRange("storageNetworkExpansionMaxLevel", 3, 1, 100);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> STORAGE_NETWORK_EXPANSION_ARCANE_INK_COSTS = BUILDER
            .comment("Arcane Ink cost to buy each Storage Network Expansion level, in order - "
                    + "list length should match storageNetworkExpansionMaxLevel")
            .defineList("storageNetworkExpansionArcaneInkCosts", List.of("4", "6", "9"), o -> o instanceof String);

    private static final ForgeConfigSpec.IntValue STORAGE_BLOCKS_PER_EXPANSION_LEVEL = BUILDER
            .comment("Extra Storage Blocks allowed per network per level of Storage Network Expansion purchased")
            .defineInRange("storageBlocksPerExpansionLevel", 5, 1, 256);

    private static final ForgeConfigSpec.IntValue ASSEMBLER_ARCANE_INK_COST = BUILDER
            .comment("Arcane Ink cost to unlock crafting the Arcane Assembler (one-time, requires Mana Boost level 1 first)")
            .defineInRange("assemblerArcaneInkCost", 6, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue ASSEMBLER_FUEL_TICKS_PER_ITEM = BUILDER
            .comment("Ticks of craft-progress powered by consuming one arcanelens:assembler_fuel item")
            .defineInRange("assemblerFuelTicksPerItem", 200, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue ASSEMBLER_CRAFT_TIME_TICKS = BUILDER
            .comment("Ticks of craft-progress needed to complete one auto-craft")
            .defineInRange("assemblerCraftTimeTicks", 100, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue ASSEMBLER_REFILL_INTERVAL_TICKS = BUILDER
            .comment("How often (in ticks) the Arcane Assembler tries to refill empty grid slots from its buffer/adjacent storage cluster")
            .defineInRange("assemblerRefillIntervalTicks", 20, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue ASSEMBLER_SPEED_MAX_LEVEL = BUILDER
            .comment("Max purchasable levels of Assembler Speed (each level reduces craft time)")
            .defineInRange("assemblerSpeedMaxLevel", 5, 1, 100);

    private static final ForgeConfigSpec.DoubleValue ASSEMBLER_SPEED_BONUS_PER_LEVEL = BUILDER
            .comment("Fraction of craft time removed per level of Assembler Speed")
            .defineInRange("assemblerSpeedBonusPerLevel", 0.05, 0.0, 1.0);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ASSEMBLER_SPEED_ARCANE_INK_COSTS = BUILDER
            .comment("Arcane Ink cost to buy each Assembler Speed level, in order - "
                    + "list length should match assemblerSpeedMaxLevel")
            .defineList("assemblerSpeedArcaneInkCosts", List.of("2", "3", "4", "5", "6"), o -> o instanceof String);

    private static final ForgeConfigSpec.IntValue ASSEMBLER_FUEL_EFFICIENCY_MAX_LEVEL = BUILDER
            .comment("Max purchasable levels of Assembler Fuel Efficiency (each level raises craft-progress gained per fuel item)")
            .defineInRange("assemblerFuelEfficiencyMaxLevel", 5, 1, 100);

    private static final ForgeConfigSpec.DoubleValue ASSEMBLER_FUEL_EFFICIENCY_BONUS_PER_LEVEL = BUILDER
            .comment("Fraction of extra craft-progress granted per fuel item, per level of Assembler Fuel Efficiency")
            .defineInRange("assemblerFuelEfficiencyBonusPerLevel", 0.05, 0.0, 5.0);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ASSEMBLER_FUEL_EFFICIENCY_ARCANE_INK_COSTS = BUILDER
            .comment("Arcane Ink cost to buy each Assembler Fuel Efficiency level, in order - "
                    + "list length should match assemblerFuelEfficiencyMaxLevel")
            .defineList("assemblerFuelEfficiencyArcaneInkCosts", List.of("2", "3", "4", "5", "6"), o -> o instanceof String);

    private static final ForgeConfigSpec.IntValue OVERLOAD_RITUAL_ARCANE_INK_COST = BUILDER
            .comment("Arcane Ink cost to unlock crafting the Overload Core and Warped Catalyst (one-time, requires Mana Boost level 1 first)")
            .defineInRange("overloadRitualArcaneInkCost", 8, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue OVERLOAD_RITUAL_CHARGE_TICKS = BUILDER
            .comment("Ticks the Overload Core's ritual charge-up takes to complete once started")
            .defineInRange("overloadRitualChargeTicks", 100, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue WARPED_ATTUNEMENT_ARCANE_INK_COST = BUILDER
            .comment("Arcane Ink cost to unlock Warped Attunement (one-time, requires Overload Ritual unlocked and Broken Vessel defeated at least once)")
            .defineInRange("warpedAttunementArcaneInkCost", 6, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue VESSEL_BOUND_FLIGHT_REGEN_TICKS_PER_REAL_TICK = BUILDER
            .comment("Real ticks needed to recover 1 tick of Vessel-Bound flight budget while grounded (lower = faster regen)")
            .defineInRange("vesselBoundFlightRegenTicksPerRealTick", 4, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue OVERLOAD_RITUAL_CAP_FRACTION_AFTER_VESSEL_DEFEAT = BUILDER
            .comment("Fraction of the network's effective cap required to start an Overload Ritual after Warped Attunement is purchased (1.0 = still full capacity)")
            .defineInRange("overloadRitualCapFractionAfterVesselDefeat", 0.75, 0.01, 1.0);

    private static final ForgeConfigSpec.IntValue MAX_WARPED_ANCHORS_PER_PLAYER = BUILDER
            .comment("Max Warped Anchors a single player can have bound at once - bounds the worst-case leaked force-loaded chunk count if one is ever lost without being unbound")
            .defineInRange("maxWarpedAnchorsPerPlayer", 1, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue SLEEPING_GOD_RELIC_FAITH_REWARD = BUILDER
            .comment("Faith granted the first time a player interacts with the Sleeping God Relic Shrine's altar in the Warped Hollow")
            .defineInRange("sleepingGodRelicFaithReward", 40, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue GOD_BOSS_REPEAT_KILL_FAITH_REWARD = BUILDER
            .comment("Faith granted for defeating a God Challenge Hub boss while the player already holds a live copy of that god's unique drop")
            .defineInRange("godBossRepeatKillFaithReward", 20, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue HUNGER_ALTAR_CONSUME_INTERVAL_TICKS = BUILDER
            .comment("Ticks between each item the Hunger's Faith Altar devours from its single slot")
            .defineInRange("hungerAltarConsumeIntervalTicks", 100, 1, 72000);

    private static final ForgeConfigSpec.DoubleValue NEUTRAL_HUNGER_FOOD_IMMUNITY_CHANCE = BUILDER
            .comment("Chance (0-1) a neutral (unpledged) player is immune to food-caused Poison/Confusion/Hunger, "
                    + "vs. a guaranteed immunity for a player pledged to the Hunger")
            .defineInRange("neutralHungerFoodImmunityChance", 0.1, 0.0, 1.0);

    private static final ForgeConfigSpec.IntValue HUNGER_LIFESTEAL_HITS_PER_HEAL = BUILDER
            .comment("Number of hits a Hunger-pledged player must land before a lifesteal heal fires")
            .defineInRange("hungerLifestealHitsPerHeal", 2, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue HUNGER_LIFESTEAL_HEAL_AMOUNT = BUILDER
            .comment("HP healed (2 HP = 1 heart) every hungerLifestealHitsPerHeal hits, for a player pledged to the Hunger")
            .defineInRange("hungerLifestealHealAmount", 1.0, 0.0, 20.0);

    private static final ForgeConfigSpec.DoubleValue NEUTRAL_HUNGER_LIFESTEAL_SCALE = BUILDER
            .comment("Multiplier on hungerLifestealHealAmount for a neutral (unpledged) player - same hit cadence, less healed per proc")
            .defineInRange("neutralHungerLifestealScale", 0.5, 0.0, 1.0);

    private static final ForgeConfigSpec.IntValue PACK_OF_THE_GODS_ARCANE_INK_COST = BUILDER
            .comment("Arcane Ink cost (alongside paper and a feather) to craft a Pack of the Gods, used to swap a prior god pledge")
            .defineInRange("packOfTheGodsArcaneInkCost", 4, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue FERTILITY_SCAN_INTERVAL_TICKS = BUILDER
            .comment("Ticks between each scan Fertility's Faith Altar performs for nearby grown vegetation")
            .defineInRange("fertilityScanIntervalTicks", 200, 1, 72000);

    private static final ForgeConfigSpec.IntValue FERTILITY_SCAN_RADIUS = BUILDER
            .comment("Radius (in blocks) Fertility's Faith Altar scans around itself for grass/flowers/trees")
            .defineInRange("fertilityScanRadius", 5, 1, 16);

    private static final ForgeConfigSpec.IntValue FERTILITY_MAX_NATURE_BLOCKS_COUNTED = BUILDER
            .comment("Cap on how many nature blocks count toward Faith in a single Fertility Altar scan, so a dense forest doesn't trivialize the economy")
            .defineInRange("fertilityMaxNatureBlocksCounted", 30, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue FERTILITY_FAITH_PER_NATURE_BLOCK = BUILDER
            .comment("Faith granted per counted nature block in a Fertility Altar scan")
            .defineInRange("fertilityFaithPerNatureBlock", 1, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue FERTILITY_FAITH_PER_HEART = BUILDER
            .comment("Faith required per bonus max heart granted to a Fertility-pledged (or neutral, at a reduced rate) player")
            .defineInRange("fertilityFaithPerHeart", 20, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue FERTILITY_MAX_BONUS_HEARTS = BUILDER
            .comment("Hard cap on bonus max hearts from Fertility - also the threshold (doubled, vanilla base included) for the passive nature-healing perk")
            .defineInRange("fertilityMaxBonusHearts", 10, 1, 100);

    private static final ForgeConfigSpec.DoubleValue NEUTRAL_FERTILITY_HEART_SCALE = BUILDER
            .comment("Fraction (0-1) of Fertility's bonus-heart scaling a neutral (unpledged) player gets, vs. a player pledged to Fertility")
            .defineInRange("neutralFertilityHeartScale", 0.5, 0.0, 1.0);

    private static final ForgeConfigSpec.IntValue WAR_SCAN_INTERVAL_TICKS = BUILDER
            .comment("Ticks between each check War's Faith Altar performs against WarDeathTracker for nearby deaths")
            .defineInRange("warScanIntervalTicks", 200, 1, 72000);

    private static final ForgeConfigSpec.IntValue WAR_SCAN_RADIUS = BUILDER
            .comment("Radius (in blocks) War's Faith Altar considers a death \"nearby\"")
            .defineInRange("warScanRadius", 8, 1, 32);

    private static final ForgeConfigSpec.IntValue WAR_FAITH_PER_NEARBY_DEATH = BUILDER
            .comment("Faith granted per nearby death counted in a War Altar check")
            .defineInRange("warFaithPerNearbyDeath", 3, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue WAR_FAITH_PER_STRENGTH_LEVEL = BUILDER
            .comment("Pledge progress required per Strength level granted to a War-pledged (or neutral, at a reduced rate) player")
            .defineInRange("warFaithPerStrengthLevel", 30, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue WAR_MAX_STRENGTH_LEVEL = BUILDER
            .comment("Hard cap on Strength level from War (3 = Strength III) - also the pledge-progress threshold for the extra-life perk")
            .defineInRange("warMaxStrengthLevel", 3, 1, 10);

    private static final ForgeConfigSpec.DoubleValue NEUTRAL_WAR_STRENGTH_SCALE = BUILDER
            .comment("Fraction (0-1) of War's Strength scaling a neutral (unpledged) player gets, vs. a player pledged to War")
            .defineInRange("neutralWarStrengthScale", 0.5, 0.0, 1.0);

    private static final ForgeConfigSpec.IntValue WAR_EXTRA_LIFE_COOLDOWN_TICKS = BUILDER
            .comment("Ticks before a War-pledged player's Totem-style extra life can trigger again after saving them (default: one full Minecraft day)")
            .defineInRange("warExtraLifeCooldownTicks", 24000, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue AURELIA_COMPANION_DURATION_TICKS = BUILDER
            .comment("Base duration Aurelia stays after being summoned by naming a Poppy \"My Love\" and right-clicking with it (default: 3 minutes) - "
                    + "scales up with pledge progress toward her, see aureliaCompanionBonusTicksPerFaith/aureliaCompanionMaxBonusDurationTicks")
            .defineInRange("aureliaCompanionDurationTicks", 3600, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue AURELIA_COMPANION_BONUS_TICKS_PER_FAITH = BUILDER
            .comment("Extra ticks of Aurelia companion duration per point of pledge progress toward her (see IFaith.getPledgeProgress())")
            .defineInRange("aureliaCompanionBonusTicksPerFaith", 2.0, 0.0, 1000.0);

    private static final ForgeConfigSpec.IntValue AURELIA_COMPANION_MAX_BONUS_DURATION_TICKS = BUILDER
            .comment("Cap on the extra duration from pledge progress (default: +3 minutes, on top of the base, so max total is 6 minutes)")
            .defineInRange("aureliaCompanionMaxBonusDurationTicks", 3600, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue AURELIA_COMPANION_MAX_HEALTH = BUILDER
            .comment("Aurelia companion's max health")
            .defineInRange("aureliaCompanionMaxHealth", 40.0, 1.0, 2048.0);

    private static final ForgeConfigSpec.DoubleValue AURELIA_COMPANION_ATTACK_DAMAGE = BUILDER
            .comment("Aurelia companion's melee attack damage")
            .defineInRange("aureliaCompanionAttackDamage", 6.0, 0.0, 2048.0);

    private static final ForgeConfigSpec.DoubleValue AURELIA_COMPANION_MOVEMENT_SPEED = BUILDER
            .comment("Aurelia companion's movement speed (vanilla player default is 0.1)")
            .defineInRange("aureliaCompanionMovementSpeed", 0.3, 0.0, 10.0);

    private static final ForgeConfigSpec.IntValue AURELIA_COMPANION_TARGET_SEARCH_RADIUS = BUILDER
            .comment("Radius (in blocks) the Aurelia companion searches for hostile mobs to proactively fight")
            .defineInRange("aureliaCompanionTargetSearchRadius", 16, 1, 128);

    private static final ForgeConfigSpec.IntValue SUN_SCAN_INTERVAL_TICKS = BUILDER
            .comment("Ticks between each check the Sun's Faith Altar performs of its own current light level")
            .defineInRange("sunScanIntervalTicks", 200, 1, 72000);

    private static final ForgeConfigSpec.IntValue SUN_FAITH_PER_LIGHT_LEVEL = BUILDER
            .comment("Faith granted per point of the Sun Altar's own light level (0-15) at each check")
            .defineInRange("sunFaithPerLightLevel", 2, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue SUN_FAITH_PER_ARMOR_POINT = BUILDER
            .comment("Pledge progress required per bonus Armor point granted to a Sun-pledged (or neutral, at a reduced rate) player during the day")
            .defineInRange("sunFaithPerArmorPoint", 15, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue SUN_MAX_BONUS_ARMOR = BUILDER
            .comment("Hard cap on bonus Armor points from the Sun")
            .defineInRange("sunMaxBonusArmor", 6, 1, 30);

    private static final ForgeConfigSpec.DoubleValue NEUTRAL_SUN_ARMOR_SCALE = BUILDER
            .comment("Fraction (0-1) of the Sun's Armor scaling a neutral (unpledged) player gets, vs. a player pledged to the Sun")
            .defineInRange("neutralSunArmorScale", 0.5, 0.0, 1.0);

    private static final ForgeConfigSpec.IntValue THEATER_SCAN_INTERVAL_TICKS = BUILDER
            .comment("Ticks between each check the Theater's Faith Altar performs of its combined Deception/Disguise/Drama/Spectacle conditions")
            .defineInRange("theaterScanIntervalTicks", 200, 1, 72000);

    private static final ForgeConfigSpec.IntValue THEATER_SCAN_RADIUS = BUILDER
            .comment("Radius (in blocks) the Theater Altar considers \"nearby\" for all four of its combined mechanics")
            .defineInRange("theaterScanRadius", 8, 1, 32);

    private static final ForgeConfigSpec.IntValue THEATER_FAITH_PER_DECEPTION = BUILDER
            .comment("Faith granted per nearby Deception (sneak-attack or hitting an unaware mob) counted in a Theater Altar check")
            .defineInRange("theaterFaithPerDeception", 3, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue THEATER_FAITH_PER_DISGUISED_PLAYER = BUILDER
            .comment("Faith granted per nearby player found invisible or wearing a mob head (Disguise) at a Theater Altar check")
            .defineInRange("theaterFaithPerDisguisedPlayer", 2, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue THEATER_FAITH_PER_DRAMA_PLAYER = BUILDER
            .comment("Faith granted per nearby player found alive under 3 hearts (Drama) at a Theater Altar check")
            .defineInRange("theaterFaithPerDramaPlayer", 4, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue THEATER_FAITH_PER_SPECTACLE_JUKEBOX = BUILDER
            .comment("Faith granted if a nearby Jukebox is playing a record (Spectacle) at a Theater Altar check")
            .defineInRange("theaterFaithPerSpectacleJukebox", 3, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue THEATER_FAITH_PER_SPECTACLE_BOOK = BUILDER
            .comment("Base Faith granted for right-clicking the Theater Altar with a Written Book (the other Spectacle sub-case)")
            .defineInRange("theaterFaithPerSpectacleBook", 10, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue THEATER_FAITH_PER_SPECTACLE_AUDIENCE_MEMBER = BUILDER
            .comment("Bonus Faith per other nearby player (an \"audience\") when reading a Written Book at the Theater Altar")
            .defineInRange("theaterFaithPerSpectacleAudienceMember", 2, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue THEATER_DODGE_CHANCE = BUILDER
            .comment("Chance (0-1) an incoming attack is dodged entirely while wearing the Theater Helmet - compensates for its zero armor value")
            .defineInRange("theaterDodgeChance", 0.1, 0.0, 1.0);

    private static final ForgeConfigSpec.DoubleValue THEATER_AGGRO_LOSS_CHANCE = BUILDER
            .comment("Chance (0-1), checked periodically, that a nearby hostile mob targeting a Theater Helmet wearer briefly loses that target")
            .defineInRange("theaterAggroLossChance", 0.15, 0.0, 1.0);

    private static final ForgeConfigSpec.IntValue THEATER_AGGRO_LOSS_RADIUS = BUILDER
            .comment("Radius (in blocks) the Theater Helmet's \"Audience's Gaze\" aggro-loss check covers around the wearer")
            .defineInRange("theaterAggroLossRadius", 5, 1, 32);

    private static final ForgeConfigSpec.IntValue THEATER_NAMETAG_HIDE_RADIUS = BUILDER
            .comment("Radius (in blocks) beyond which a Theater Helmet wearer's nametag is hidden from other players")
            .defineInRange("theaterNametagHideRadius", 4, 1, 64);

    private static final ForgeConfigSpec.IntValue THEATER_CLONE_LIFETIME_TICKS = BUILDER
            .comment("Ticks a Stage Clone decoy lasts before bursting into confetti on its own")
            .defineInRange("theaterCloneLifetimeTicks", 160, 1, 72000);

    private static final ForgeConfigSpec.IntValue THEATER_STAGE_CLONE_FAITH_COST = BUILDER
            .comment("Faith cost to summon a Stage Clone")
            .defineInRange("theaterStageCloneFaithCost", 15, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue THEATER_STAGE_CLONE_RETARGET_RADIUS = BUILDER
            .comment("Radius (in blocks) around the caster within which hostile mobs currently targeting them are retargeted onto a new Stage Clone")
            .defineInRange("theaterStageCloneRetargetRadius", 10, 1, 32);

    private static final ForgeConfigSpec.IntValue THEATER_CROWD_CONTROL_FAITH_COST = BUILDER
            .comment("Faith cost to use Crowd Control")
            .defineInRange("theaterCrowdControlFaithCost", 20, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue THEATER_CROWD_CONTROL_RADIUS = BUILDER
            .comment("Radius (in blocks) around Crowd Control's target point that mobs are affected")
            .defineInRange("theaterCrowdControlRadius", 8, 1, 32);

    private static final ForgeConfigSpec.IntValue THEATER_CROWD_CONTROL_RANGE = BUILDER
            .comment("Maximum distance (in blocks) Crowd Control's target point can be from the caster")
            .defineInRange("theaterCrowdControlRange", 24, 1, 64);

    private static final ForgeConfigSpec.IntValue THEATER_VANISHING_ACT_FAITH_COST = BUILDER
            .comment("Faith cost to trigger Vanishing Act")
            .defineInRange("theaterVanishingActFaithCost", 25, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue THEATER_VANISHING_ACT_CHARGE_TICKS = BUILDER
            .comment("Ticks of holding sneak (empty-handed, Theater Helmet worn) required to trigger Vanishing Act")
            .defineInRange("theaterVanishingActChargeTicks", 80, 1, 1200);

    private static final ForgeConfigSpec.IntValue THEATER_VANISHING_ACT_DURATION_TICKS = BUILDER
            .comment("Ticks Vanishing Act's invisibility lasts once triggered, unless broken early by attacking")
            .defineInRange("theaterVanishingActDurationTicks", 600, 1, 72000);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int baseLensMaxMana = 1000;
    public static int linkRadius = 2;
    public static int linkRescanIntervalTicks = 40;
    public static int soulSandBurnTicks = 6000;
    public static int conversionIntervalTicks = 100;
    public static int lensCombinePriceBase = 4;
    public static int faithDiminishStacksDefault = 3;
    public static int faithScanIntervalTicks = 10;
    public static int faithScanRadius = 4;
    public static int baseFlightSeconds = 3;
    public static int faithPerFlightSecond = 15;
    public static int flightCooldownTicks = 1200;
    public static int flightLandedGraceTicks = 100;
    public static int watchingCheckIntervalTicks = 900;
    public static double baseWatchingChance = 0.05;
    public static double watchingFaithScale = 100.0;
    public static double maxWatchingChance = 0.85;
    public static double watchingEyesRevealThreshold = 0.75;
    public static int faithPerHungerToken = 100;
    public static double blessingBoonDurationMultiplierPerLevel = 0.1;
    public static double burdenResistChancePerLevel = 0.05;
    public static double maxBurdenResistChance = 0.9;
    public static double bonusBlessingChancePerLevel = 0.01;
    public static double maxBonusBlessingChance = 1.0;
    public static double magnetCharmRadius = 6.0;
    public static double magnetCharmPullStrength = 0.15;
    public static int faithCharmIntervalTicks = 600;
    public static int faithCharmAmountPerInterval = 1;
    public static int manaCharmIntervalTicks = 100;
    public static int manaCharmAmountPerInterval = 2;
    public static int manaBoostAmountPerLevel = 1000;
    public static int manaBoostMaxLevel = 3;
    public static double cooldownReductionPerLevel = 0.05;
    public static int cooldownReductionMaxLevel = 5;
    public static double costReductionPerLevel = 0.05;
    public static int costReductionMaxLevel = 5;
    public static int pocketDimensionArcaneInkCost = 3;
    public static int pocketDimensionExpansionMaxLevel = 2;
    public static int maxStorageBlocksPerNetwork = 15;
    public static int slotsPerStorageBlock = 5;
    public static int slotsPerSlotUpgrade = 1;
    public static int stackSizeBonusPerStackUpgrade = 32;
    public static int storageSystemArcaneInkCost = 5;
    public static int storageNetworkExpansionMaxLevel = 3;
    public static int storageBlocksPerExpansionLevel = 5;
    public static int assemblerArcaneInkCost = 6;
    public static int assemblerFuelTicksPerItem = 200;
    public static int assemblerCraftTimeTicks = 100;
    public static int assemblerRefillIntervalTicks = 20;
    public static int assemblerSpeedMaxLevel = 5;
    public static double assemblerSpeedBonusPerLevel = 0.05;
    public static int assemblerFuelEfficiencyMaxLevel = 5;
    public static double assemblerFuelEfficiencyBonusPerLevel = 0.05;
    public static int overloadRitualArcaneInkCost = 8;
    public static int overloadRitualChargeTicks = 100;
    public static int warpedAttunementArcaneInkCost = 6;
    public static int vesselBoundFlightRegenTicksPerRealTick = 4;
    public static double overloadRitualCapFractionAfterVesselDefeat = 0.75;
    public static int maxWarpedAnchorsPerPlayer = 1;
    public static int sleepingGodRelicFaithReward = 40;
    public static int godBossRepeatKillFaithReward = 20;
    public static int hungerAltarConsumeIntervalTicks = 100;
    public static double neutralHungerFoodImmunityChance = 0.1;
    public static int hungerLifestealHitsPerHeal = 2;
    public static double hungerLifestealHealAmount = 1.0;
    public static double neutralHungerLifestealScale = 0.5;
    public static int packOfTheGodsArcaneInkCost = 4;
    public static int fertilityScanIntervalTicks = 200;
    public static int fertilityScanRadius = 5;
    public static int fertilityMaxNatureBlocksCounted = 30;
    public static int fertilityFaithPerNatureBlock = 1;
    public static int fertilityFaithPerHeart = 20;
    public static int fertilityMaxBonusHearts = 10;
    public static double neutralFertilityHeartScale = 0.5;
    public static int warScanIntervalTicks = 200;
    public static int warScanRadius = 8;
    public static int warFaithPerNearbyDeath = 3;
    public static int warFaithPerStrengthLevel = 30;
    public static int warMaxStrengthLevel = 3;
    public static double neutralWarStrengthScale = 0.5;
    public static int warExtraLifeCooldownTicks = 24000;
    public static int aureliaCompanionDurationTicks = 3600;
    public static double aureliaCompanionBonusTicksPerFaith = 2.0;
    public static int aureliaCompanionMaxBonusDurationTicks = 3600;
    public static double aureliaCompanionMaxHealth = 40.0;
    public static double aureliaCompanionAttackDamage = 6.0;
    public static double aureliaCompanionMovementSpeed = 0.3;
    public static int aureliaCompanionTargetSearchRadius = 16;
    public static int sunScanIntervalTicks = 200;
    public static int sunFaithPerLightLevel = 2;
    public static int sunFaithPerArmorPoint = 15;
    public static int sunMaxBonusArmor = 6;
    public static double neutralSunArmorScale = 0.5;
    public static int theaterScanIntervalTicks = 200;
    public static int theaterScanRadius = 8;
    public static int theaterFaithPerDeception = 3;
    public static int theaterFaithPerDisguisedPlayer = 2;
    public static int theaterFaithPerDramaPlayer = 4;
    public static int theaterFaithPerSpectacleJukebox = 3;
    public static int theaterFaithPerSpectacleBook = 10;
    public static int theaterFaithPerSpectacleAudienceMember = 2;
    public static double theaterDodgeChance = 0.1;
    public static double theaterAggroLossChance = 0.15;
    public static int theaterAggroLossRadius = 5;
    public static int theaterNametagHideRadius = 4;
    public static int theaterCloneLifetimeTicks = 160;
    public static int theaterStageCloneFaithCost = 15;
    public static int theaterStageCloneRetargetRadius = 10;
    public static int theaterCrowdControlFaithCost = 20;
    public static int theaterCrowdControlRadius = 8;
    public static int theaterCrowdControlRange = 24;
    public static int theaterVanishingActFaithCost = 25;
    public static int theaterVanishingActChargeTicks = 80;
    public static int theaterVanishingActDurationTicks = 600;
    public static final List<Integer> manaBoostArcaneInkCosts = new java.util.ArrayList<>();
    public static final List<Integer> cooldownReductionArcaneInkCosts = new java.util.ArrayList<>();
    public static final List<Integer> costReductionArcaneInkCosts = new java.util.ArrayList<>();
    public static final List<Integer> pocketDimensionExpansionArcaneInkCosts = new java.util.ArrayList<>();
    public static final List<Integer> storageNetworkExpansionArcaneInkCosts = new java.util.ArrayList<>();
    public static final List<Integer> assemblerSpeedArcaneInkCosts = new java.util.ArrayList<>();
    public static final List<Integer> assemblerFuelEfficiencyArcaneInkCosts = new java.util.ArrayList<>();
    public static final Map<Rarity, Integer> rarityManaValues = new EnumMap<>(Rarity.class);
    public static final Map<Rarity, Integer> rarityFaithValues = new EnumMap<>(Rarity.class);
    public static final Map<Rarity, Integer> rarityDiminishStacks = new EnumMap<>(Rarity.class);

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        baseLensMaxMana = BASE_LENS_MAX_MANA.get();
        linkRadius = LINK_RADIUS.get();
        linkRescanIntervalTicks = LINK_RESCAN_INTERVAL_TICKS.get();
        soulSandBurnTicks = SOUL_SAND_BURN_TICKS.get();
        conversionIntervalTicks = CONVERSION_INTERVAL_TICKS.get();
        lensCombinePriceBase = LENS_COMBINE_PRICE_BASE.get();
        faithDiminishStacksDefault = FAITH_DIMINISH_STACKS_DEFAULT.get();
        faithScanIntervalTicks = FAITH_SCAN_INTERVAL_TICKS.get();
        faithScanRadius = FAITH_SCAN_RADIUS.get();
        baseFlightSeconds = BASE_FLIGHT_SECONDS.get();
        faithPerFlightSecond = FAITH_PER_FLIGHT_SECOND.get();
        flightCooldownTicks = FLIGHT_COOLDOWN_TICKS.get();
        flightLandedGraceTicks = FLIGHT_LANDED_GRACE_TICKS.get();
        watchingCheckIntervalTicks = WATCHING_CHECK_INTERVAL_TICKS.get();
        baseWatchingChance = BASE_WATCHING_CHANCE.get();
        watchingFaithScale = WATCHING_FAITH_SCALE.get();
        maxWatchingChance = MAX_WATCHING_CHANCE.get();
        watchingEyesRevealThreshold = WATCHING_EYES_REVEAL_THRESHOLD.get();
        faithPerHungerToken = FAITH_PER_HUNGER_TOKEN.get();
        blessingBoonDurationMultiplierPerLevel = BLESSING_BOON_DURATION_MULTIPLIER_PER_LEVEL.get();
        burdenResistChancePerLevel = BURDEN_RESIST_CHANCE_PER_LEVEL.get();
        maxBurdenResistChance = MAX_BURDEN_RESIST_CHANCE.get();
        bonusBlessingChancePerLevel = BONUS_BLESSING_CHANCE_PER_LEVEL.get();
        maxBonusBlessingChance = MAX_BONUS_BLESSING_CHANCE.get();
        magnetCharmRadius = MAGNET_CHARM_RADIUS.get();
        magnetCharmPullStrength = MAGNET_CHARM_PULL_STRENGTH.get();
        faithCharmIntervalTicks = FAITH_CHARM_INTERVAL_TICKS.get();
        faithCharmAmountPerInterval = FAITH_CHARM_AMOUNT_PER_INTERVAL.get();
        manaCharmIntervalTicks = MANA_CHARM_INTERVAL_TICKS.get();
        manaCharmAmountPerInterval = MANA_CHARM_AMOUNT_PER_INTERVAL.get();
        manaBoostAmountPerLevel = MANA_BOOST_AMOUNT_PER_LEVEL.get();
        manaBoostMaxLevel = MANA_BOOST_MAX_LEVEL.get();
        cooldownReductionPerLevel = COOLDOWN_REDUCTION_PER_LEVEL.get();
        cooldownReductionMaxLevel = COOLDOWN_REDUCTION_MAX_LEVEL.get();
        costReductionPerLevel = COST_REDUCTION_PER_LEVEL.get();
        costReductionMaxLevel = COST_REDUCTION_MAX_LEVEL.get();
        pocketDimensionArcaneInkCost = POCKET_DIMENSION_ARCANE_INK_COST.get();
        pocketDimensionExpansionMaxLevel = POCKET_DIMENSION_EXPANSION_MAX_LEVEL.get();
        maxStorageBlocksPerNetwork = MAX_STORAGE_BLOCKS_PER_NETWORK.get();
        slotsPerStorageBlock = SLOTS_PER_STORAGE_BLOCK.get();
        slotsPerSlotUpgrade = SLOTS_PER_SLOT_UPGRADE.get();
        stackSizeBonusPerStackUpgrade = STACK_SIZE_BONUS_PER_STACK_UPGRADE.get();
        storageSystemArcaneInkCost = STORAGE_SYSTEM_ARCANE_INK_COST.get();
        storageNetworkExpansionMaxLevel = STORAGE_NETWORK_EXPANSION_MAX_LEVEL.get();
        storageBlocksPerExpansionLevel = STORAGE_BLOCKS_PER_EXPANSION_LEVEL.get();
        assemblerArcaneInkCost = ASSEMBLER_ARCANE_INK_COST.get();
        assemblerFuelTicksPerItem = ASSEMBLER_FUEL_TICKS_PER_ITEM.get();
        assemblerCraftTimeTicks = ASSEMBLER_CRAFT_TIME_TICKS.get();
        assemblerRefillIntervalTicks = ASSEMBLER_REFILL_INTERVAL_TICKS.get();
        assemblerSpeedMaxLevel = ASSEMBLER_SPEED_MAX_LEVEL.get();
        assemblerSpeedBonusPerLevel = ASSEMBLER_SPEED_BONUS_PER_LEVEL.get();
        assemblerFuelEfficiencyMaxLevel = ASSEMBLER_FUEL_EFFICIENCY_MAX_LEVEL.get();
        assemblerFuelEfficiencyBonusPerLevel = ASSEMBLER_FUEL_EFFICIENCY_BONUS_PER_LEVEL.get();
        overloadRitualArcaneInkCost = OVERLOAD_RITUAL_ARCANE_INK_COST.get();
        overloadRitualChargeTicks = OVERLOAD_RITUAL_CHARGE_TICKS.get();
        warpedAttunementArcaneInkCost = WARPED_ATTUNEMENT_ARCANE_INK_COST.get();
        vesselBoundFlightRegenTicksPerRealTick = VESSEL_BOUND_FLIGHT_REGEN_TICKS_PER_REAL_TICK.get();
        overloadRitualCapFractionAfterVesselDefeat = OVERLOAD_RITUAL_CAP_FRACTION_AFTER_VESSEL_DEFEAT.get();
        maxWarpedAnchorsPerPlayer = MAX_WARPED_ANCHORS_PER_PLAYER.get();
        sleepingGodRelicFaithReward = SLEEPING_GOD_RELIC_FAITH_REWARD.get();
        godBossRepeatKillFaithReward = GOD_BOSS_REPEAT_KILL_FAITH_REWARD.get();
        hungerAltarConsumeIntervalTicks = HUNGER_ALTAR_CONSUME_INTERVAL_TICKS.get();
        neutralHungerFoodImmunityChance = NEUTRAL_HUNGER_FOOD_IMMUNITY_CHANCE.get();
        hungerLifestealHitsPerHeal = HUNGER_LIFESTEAL_HITS_PER_HEAL.get();
        hungerLifestealHealAmount = HUNGER_LIFESTEAL_HEAL_AMOUNT.get();
        neutralHungerLifestealScale = NEUTRAL_HUNGER_LIFESTEAL_SCALE.get();
        packOfTheGodsArcaneInkCost = PACK_OF_THE_GODS_ARCANE_INK_COST.get();
        fertilityScanIntervalTicks = FERTILITY_SCAN_INTERVAL_TICKS.get();
        fertilityScanRadius = FERTILITY_SCAN_RADIUS.get();
        fertilityMaxNatureBlocksCounted = FERTILITY_MAX_NATURE_BLOCKS_COUNTED.get();
        fertilityFaithPerNatureBlock = FERTILITY_FAITH_PER_NATURE_BLOCK.get();
        fertilityFaithPerHeart = FERTILITY_FAITH_PER_HEART.get();
        fertilityMaxBonusHearts = FERTILITY_MAX_BONUS_HEARTS.get();
        neutralFertilityHeartScale = NEUTRAL_FERTILITY_HEART_SCALE.get();
        warScanIntervalTicks = WAR_SCAN_INTERVAL_TICKS.get();
        warScanRadius = WAR_SCAN_RADIUS.get();
        warFaithPerNearbyDeath = WAR_FAITH_PER_NEARBY_DEATH.get();
        warFaithPerStrengthLevel = WAR_FAITH_PER_STRENGTH_LEVEL.get();
        warMaxStrengthLevel = WAR_MAX_STRENGTH_LEVEL.get();
        neutralWarStrengthScale = NEUTRAL_WAR_STRENGTH_SCALE.get();
        warExtraLifeCooldownTicks = WAR_EXTRA_LIFE_COOLDOWN_TICKS.get();
        aureliaCompanionDurationTicks = AURELIA_COMPANION_DURATION_TICKS.get();
        aureliaCompanionBonusTicksPerFaith = AURELIA_COMPANION_BONUS_TICKS_PER_FAITH.get();
        aureliaCompanionMaxBonusDurationTicks = AURELIA_COMPANION_MAX_BONUS_DURATION_TICKS.get();
        aureliaCompanionMaxHealth = AURELIA_COMPANION_MAX_HEALTH.get();
        aureliaCompanionAttackDamage = AURELIA_COMPANION_ATTACK_DAMAGE.get();
        aureliaCompanionMovementSpeed = AURELIA_COMPANION_MOVEMENT_SPEED.get();
        aureliaCompanionTargetSearchRadius = AURELIA_COMPANION_TARGET_SEARCH_RADIUS.get();
        sunScanIntervalTicks = SUN_SCAN_INTERVAL_TICKS.get();
        sunFaithPerLightLevel = SUN_FAITH_PER_LIGHT_LEVEL.get();
        sunFaithPerArmorPoint = SUN_FAITH_PER_ARMOR_POINT.get();
        sunMaxBonusArmor = SUN_MAX_BONUS_ARMOR.get();
        neutralSunArmorScale = NEUTRAL_SUN_ARMOR_SCALE.get();
        theaterScanIntervalTicks = THEATER_SCAN_INTERVAL_TICKS.get();
        theaterScanRadius = THEATER_SCAN_RADIUS.get();
        theaterFaithPerDeception = THEATER_FAITH_PER_DECEPTION.get();
        theaterFaithPerDisguisedPlayer = THEATER_FAITH_PER_DISGUISED_PLAYER.get();
        theaterFaithPerDramaPlayer = THEATER_FAITH_PER_DRAMA_PLAYER.get();
        theaterFaithPerSpectacleJukebox = THEATER_FAITH_PER_SPECTACLE_JUKEBOX.get();
        theaterFaithPerSpectacleBook = THEATER_FAITH_PER_SPECTACLE_BOOK.get();
        theaterFaithPerSpectacleAudienceMember = THEATER_FAITH_PER_SPECTACLE_AUDIENCE_MEMBER.get();
        theaterDodgeChance = THEATER_DODGE_CHANCE.get();
        theaterAggroLossChance = THEATER_AGGRO_LOSS_CHANCE.get();
        theaterAggroLossRadius = THEATER_AGGRO_LOSS_RADIUS.get();
        theaterNametagHideRadius = THEATER_NAMETAG_HIDE_RADIUS.get();
        theaterCloneLifetimeTicks = THEATER_CLONE_LIFETIME_TICKS.get();
        theaterStageCloneFaithCost = THEATER_STAGE_CLONE_FAITH_COST.get();
        theaterStageCloneRetargetRadius = THEATER_STAGE_CLONE_RETARGET_RADIUS.get();
        theaterCrowdControlFaithCost = THEATER_CROWD_CONTROL_FAITH_COST.get();
        theaterCrowdControlRadius = THEATER_CROWD_CONTROL_RADIUS.get();
        theaterCrowdControlRange = THEATER_CROWD_CONTROL_RANGE.get();
        theaterVanishingActFaithCost = THEATER_VANISHING_ACT_FAITH_COST.get();
        theaterVanishingActChargeTicks = THEATER_VANISHING_ACT_CHARGE_TICKS.get();
        theaterVanishingActDurationTicks = THEATER_VANISHING_ACT_DURATION_TICKS.get();

        parseIntList(MANA_BOOST_ARCANE_INK_COSTS.get(), manaBoostArcaneInkCosts);
        parseIntList(COOLDOWN_REDUCTION_ARCANE_INK_COSTS.get(), cooldownReductionArcaneInkCosts);
        parseIntList(COST_REDUCTION_ARCANE_INK_COSTS.get(), costReductionArcaneInkCosts);
        parseIntList(POCKET_DIMENSION_EXPANSION_ARCANE_INK_COSTS.get(), pocketDimensionExpansionArcaneInkCosts);
        parseIntList(STORAGE_NETWORK_EXPANSION_ARCANE_INK_COSTS.get(), storageNetworkExpansionArcaneInkCosts);
        parseIntList(ASSEMBLER_SPEED_ARCANE_INK_COSTS.get(), assemblerSpeedArcaneInkCosts);
        parseIntList(ASSEMBLER_FUEL_EFFICIENCY_ARCANE_INK_COSTS.get(), assemblerFuelEfficiencyArcaneInkCosts);

        rarityManaValues.clear();
        for (String entry : RARITY_MANA_VALUES.get())
        {
            String[] parts = entry.split(":");
            if (parts.length != 2)
            {
                continue;
            }
            try
            {
                Rarity rarity = Rarity.valueOf(parts[0].trim().toUpperCase());
                int mana = Integer.parseInt(parts[1].trim());
                rarityManaValues.put(rarity, mana);
            }
            catch (IllegalArgumentException ignored)
            {
            }
        }

        rarityFaithValues.clear();
        for (String entry : RARITY_FAITH_VALUES.get())
        {
            String[] parts = entry.split(":");
            if (parts.length != 2)
            {
                continue;
            }
            try
            {
                Rarity rarity = Rarity.valueOf(parts[0].trim().toUpperCase());
                int faith = Integer.parseInt(parts[1].trim());
                rarityFaithValues.put(rarity, faith);
            }
            catch (IllegalArgumentException ignored)
            {
            }
        }

        rarityDiminishStacks.clear();
        for (String entry : RARITY_DIMINISH_STACKS.get())
        {
            String[] parts = entry.split(":");
            if (parts.length != 2)
            {
                continue;
            }
            try
            {
                Rarity rarity = Rarity.valueOf(parts[0].trim().toUpperCase());
                int stacks = Integer.parseInt(parts[1].trim());
                rarityDiminishStacks.put(rarity, stacks);
            }
            catch (IllegalArgumentException ignored)
            {
            }
        }
    }

    public static int getManaForRarity(Rarity rarity)
    {
        return rarityManaValues.getOrDefault(rarity, 0);
    }

    public static int getFaithForRarity(Rarity rarity)
    {
        return rarityFaithValues.getOrDefault(rarity, 0);
    }

    public static int getDiminishStacksForRarity(Rarity rarity)
    {
        return rarityDiminishStacks.getOrDefault(rarity, faithDiminishStacksDefault);
    }

    /** Cost to buy the next level of a repeatable skill (0-indexed current level) - falls back to the last
     * entry in the list if the level runs past the configured cost list, rather than crashing. */
    public static int getSkillCost(List<Integer> costs, int currentLevel)
    {
        if (costs.isEmpty())
        {
            return Integer.MAX_VALUE;
        }
        int index = Math.min(currentLevel, costs.size() - 1);
        return costs.get(index);
    }

    private static void parseIntList(List<? extends String> raw, List<Integer> out)
    {
        out.clear();
        for (String entry : raw)
        {
            try
            {
                out.add(Integer.parseInt(entry.trim()));
            }
            catch (NumberFormatException ignored)
            {
            }
        }
    }
}
