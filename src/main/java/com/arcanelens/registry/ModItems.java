package com.arcanelens.registry;

import com.arcanelens.ArcaneLens;
import com.arcanelens.item.AncientSpellBookItem;
import com.arcanelens.item.AntivenomCharmItem;
import com.arcanelens.item.AureliasSpearItem;
import com.arcanelens.item.ArcaneGuideBookItem;
import com.arcanelens.item.BraceletItem;
import com.arcanelens.item.BurningCharcoalCharmItem;
import com.arcanelens.item.DiamondEngraverItem;
import com.arcanelens.item.FaithCharmItem;
import com.arcanelens.item.FireResistantArmorItem;
import com.arcanelens.item.FloriansAntlerItem;
import com.arcanelens.item.GodHubMedallionItem;
import com.arcanelens.item.HungersPactItem;
import com.arcanelens.item.MagicLensItem;
import com.arcanelens.item.MagnetCharmItem;
import com.arcanelens.item.ManaOrbCharmItem;
import com.arcanelens.item.PackOfTheGodsItem;
import com.arcanelens.item.PreservedWitherRoseCharmItem;
import com.arcanelens.item.SleepingGodArmorMaterial;
import com.arcanelens.item.SunAndMoonWardItem;
import com.arcanelens.item.TheaterArmorMaterial;
import com.arcanelens.item.TheHungersBoonItem;
import com.arcanelens.item.TokenOfTheHungerItem;
import com.arcanelens.item.VesselBoundArmorItem;
import com.arcanelens.item.VesselBoundElytraItem;
import com.arcanelens.item.VesselSummoningCharmItem;
import com.arcanelens.item.WarpedAnchorItem;
import com.arcanelens.item.WeirdAmuletItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ArcaneLens.MODID);

    public static final RegistryObject<Item> MAGIC_LENS = ITEMS.register("magic_lens",
            () -> new MagicLensItem(new Item.Properties().stacksTo(1)));

    // Always obtainable via /give or the creative tab, unlike the raw patchouli:guide_book item - see
    // ArcaneGuideBookItem's own javadoc for why a plain untagged patchouli:guide_book doesn't work. Its
    // crafting recipe (ModRecipeProvider) is a plain ShapelessRecipeBuilder entry, same as any other item.
    public static final RegistryObject<Item> ARCANE_GUIDE_BOOK = ITEMS.register("arcane_guide",
            () -> new ArcaneGuideBookItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> INSCRIPTION_WORKBENCH = ITEMS.register("inscription_workbench",
            () -> new BlockItem(ModBlocks.INSCRIPTION_WORKBENCH.get(), new Item.Properties()));

    public static final RegistryObject<Item> SOUL_PEDESTAL = ITEMS.register("soul_pedestal",
            () -> new BlockItem(ModBlocks.SOUL_PEDESTAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> LENS_PEDESTAL = ITEMS.register("lens_pedestal",
            () -> new BlockItem(ModBlocks.LENS_PEDESTAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> ANCIENT_SPELL_BOOK = ITEMS.register("ancient_spell_book",
            () -> new AncientSpellBookItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ARCANE_ORE = ITEMS.register("arcane_ore",
            () -> new BlockItem(ModBlocks.ARCANE_ORE.get(), new Item.Properties()));

    public static final RegistryObject<Item> LENS_COMBINER = ITEMS.register("lens_combiner",
            () -> new BlockItem(ModBlocks.LENS_COMBINER.get(), new Item.Properties()));

    public static final RegistryObject<Item> RAW_ARCANE_MATERIAL = ITEMS.register("raw_arcane_material",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> DIAMOND_ENGRAVER = ITEMS.register("diamond_engraver",
            () -> new DiamondEngraverItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SOUL_FEEDER = ITEMS.register("soul_feeder",
            () -> new BlockItem(ModBlocks.SOUL_FEEDER.get(), new Item.Properties()));

    // Diamond-tier stats/toughness, repaired with Warped Fungus instead of diamonds, own texture
    // (see SleepingGodArmorMaterial) - no custom spell yet (that's deferred along with the faith system).
    public static final RegistryObject<Item> SLEEPING_GOD_HELMET = ITEMS.register("sleeping_god_helmet",
            () -> new FireResistantArmorItem(SleepingGodArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> SLEEPING_GOD_CHESTPLATE = ITEMS.register("sleeping_god_chestplate",
            () -> new FireResistantArmorItem(SleepingGodArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));

    public static final RegistryObject<Item> SLEEPING_GOD_LEGGINGS = ITEMS.register("sleeping_god_leggings",
            () -> new FireResistantArmorItem(SleepingGodArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final RegistryObject<Item> SLEEPING_GOD_BOOTS = ITEMS.register("sleeping_god_boots",
            () -> new FireResistantArmorItem(SleepingGodArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final RegistryObject<Item> ARENA_TRIGGER = ITEMS.register("arena_trigger",
            () -> new BlockItem(ModBlocks.ARENA_TRIGGER.get(), new Item.Properties()));

    // Same "obtainable via /give for building, not shown in the creative tab" precedent as ARENA_TRIGGER -
    // hand-placed only inside the Sleeping God Relic Shrine build.
    public static final RegistryObject<Item> SLEEPING_GOD_RELIC_ALTAR = ITEMS.register("sleeping_god_relic_altar",
            () -> new BlockItem(ModBlocks.SLEEPING_GOD_RELIC_ALTAR.get(), new Item.Properties()));

    // Same "obtainable via /give only for now, not in the creative tab" precedent as ARENA_TRIGGER -
    // how this should actually be obtained in survival is a Phase B decision (see the God Boss Hub plan).
    public static final RegistryObject<Item> GOD_HUB_MEDALLION = ITEMS.register("god_hub_medallion",
            () -> new GodHubMedallionItem(new Item.Properties().stacksTo(1)));

    // Same "obtainable via /give only for now, not in the creative tab" precedent as ARENA_TRIGGER -
    // one placed per god's arena entrance, godId configured per-instance (see GodChallengeAltarBlockEntity).
    public static final RegistryObject<Item> GOD_CHALLENGE_ALTAR = ITEMS.register("god_challenge_altar",
            () -> new BlockItem(ModBlocks.GOD_CHALLENGE_ALTAR.get(), new Item.Properties()));

    public static final RegistryObject<Item> FAITH_ALTAR = ITEMS.register("faith_altar",
            () -> new BlockItem(ModBlocks.FAITH_ALTAR.get(), new Item.Properties()));

    public static final RegistryObject<Item> COMMAND_TRIGGER = ITEMS.register("command_trigger",
            () -> new BlockItem(ModBlocks.COMMAND_TRIGGER.get(), new Item.Properties()));

    public static final RegistryObject<Item> TRIGGER_PLATE = ITEMS.register("trigger_plate",
            () -> new BlockItem(ModBlocks.TRIGGER_PLATE.get(), new Item.Properties()));

    // Right-click for a private Faith readout - see WeirdAmuletItem.
    public static final RegistryObject<Item> WEIRD_AMULET = ITEMS.register("weird_amulet",
            () -> new WeirdAmuletItem(new Item.Properties()));

    // Right-click opens a choice GUI between HUNGERS_BOON and HUNGERS_BINDINGS - see TokenOfTheHungerItem.
    public static final RegistryObject<Item> TOKEN_OF_THE_HUNGER = ITEMS.register("token_of_the_hunger",
            () -> new TokenOfTheHungerItem(new Item.Properties()));

    // Right-click to consume, permanently extends blessing effect duration - see TheHungersBoonItem.
    public static final RegistryObject<Item> HUNGERS_BOON = ITEMS.register("hungers_boon",
            () -> new TheHungersBoonItem(new Item.Properties()));

    // No custom use() - burned in soul fire at a Faith Altar instead (see FaithAltarBlockEntity), which
    // increments burdenResistBoonLevel rather than granting ordinary Faith.
    public static final RegistryObject<Item> HUNGERS_BINDINGS = ITEMS.register("hungers_bindings",
            () -> new Item(new Item.Properties()));

    // Right-click toggles hungerEyesHidden - see SunAndMoonWardItem.
    public static final RegistryObject<Item> SUN_AND_MOON_WARD = ITEMS.register("sun_and_moon_ward",
            () -> new SunAndMoonWardItem(new Item.Properties()));

    // Worn in a Curios charm slot - pulls nearby dropped items toward the wearer, see MagnetCharmItem.
    // stacksTo(1): a wearable accessory (only one is ever equippable at a time anyway), matching the
    // same reasoning already applied to Stack/Slot Upgrade - found stacking unintentionally, fixed here.
    public static final RegistryObject<Item> MAGNET_CHARM = ITEMS.register("magnet_charm",
            () -> new MagnetCharmItem(new Item.Properties().stacksTo(1)));

    // Worn in a Curios charm slot - passive Faith trickle, see FaithCharmItem.
    public static final RegistryObject<Item> FAITH_CHARM = ITEMS.register("faith_charm",
            () -> new FaithCharmItem(new Item.Properties().stacksTo(1)));

    // Worn in a Curios charm slot - passive Magic Lens mana trickle, see ManaOrbCharmItem.
    public static final RegistryObject<Item> MANA_ORB_CHARM = ITEMS.register("mana_orb_charm",
            () -> new ManaOrbCharmItem(new Item.Properties().stacksTo(1)));

    // Worn in a Curios charm slot - permanent fire resistance, see BurningCharcoalCharmItem.
    public static final RegistryObject<Item> BURNING_CHARCOAL_CHARM = ITEMS.register("burning_charcoal_charm",
            () -> new BurningCharcoalCharmItem(new Item.Properties().stacksTo(1)));

    // Worn in a Curios charm slot - cures Poison on contact, see AntivenomCharmItem.
    public static final RegistryObject<Item> ANTIVENOM_CHARM = ITEMS.register("antivenom_charm",
            () -> new AntivenomCharmItem(new Item.Properties().stacksTo(1)));

    // Worn in a Curios charm slot - cures Wither on contact, see PreservedWitherRoseCharmItem.
    public static final RegistryObject<Item> PRESERVED_WITHER_ROSE_CHARM = ITEMS.register("preserved_wither_rose_charm",
            () -> new PreservedWitherRoseCharmItem(new Item.Properties().stacksTo(1)));

    // Worn in its own Curios bracelet slot - grants +1/+2/+3 max charm slots, see BraceletItem. Same
    // stacksTo(1) reasoning as the charms above.
    public static final RegistryObject<Item> IRON_BRACELET = ITEMS.register("iron_bracelet",
            () -> new BraceletItem(new Item.Properties().stacksTo(1), 1));

    public static final RegistryObject<Item> GOLD_BRACELET = ITEMS.register("gold_bracelet",
            () -> new BraceletItem(new Item.Properties().stacksTo(1), 2));

    public static final RegistryObject<Item> NETHERITE_BRACELET = ITEMS.register("netherite_bracelet",
            () -> new BraceletItem(new Item.Properties().stacksTo(1), 3));

    // Rare currency, meant primarily as dungeon loot - also has a deliberately expensive Nether Star recipe
    // as a backup path (see ModRecipeProvider) for the future skill tree system.
    public static final RegistryObject<Item> ARCANE_INK = ITEMS.register("arcane_ink",
            () -> new Item(new Item.Properties()));

    // Reusable key granted by the Hunger Idol exchange - right-click opens the skill tree, see HungersPactItem.
    // Not craftable, no recipe - only obtainable from the exchange.
    public static final RegistryObject<Item> HUNGERS_PACT = ITEMS.register("hungers_pact",
            () -> new HungersPactItem(new Item.Properties().stacksTo(1)));

    // A "dumb" 5-slot storage unit - touching Storage Blocks merge into one shared pool, browsed through
    // a Terminal. Recipe (added in datagen) requires the Storage System skill tree unlock to actually craft.
    public static final RegistryObject<Item> STORAGE_BLOCK = ITEMS.register("storage_block",
            () -> new BlockItem(ModBlocks.STORAGE_BLOCK.get(), new Item.Properties()));

    // Bridges two otherwise-disconnected Storage Block clusters into one network - contributes no slots
    // itself, see StorageConnectorBlock/StorageNetworkManager.
    public static final RegistryObject<Item> STORAGE_CONNECTOR = ITEMS.register("storage_connector",
            () -> new BlockItem(ModBlocks.STORAGE_CONNECTOR.get(), new Item.Properties()));

    // Placed adjacent to a Storage Block to browse its whole connected cluster - see TerminalBlockEntity.
    public static final RegistryObject<Item> TERMINAL = ITEMS.register("terminal",
            () -> new BlockItem(ModBlocks.TERMINAL.get(), new Item.Properties()));

    // Auto-crafts real crafting-table recipes from its 3x3 grid, fueled by crop/meat items instead of
    // redstone - see ArcaneAssemblerBlockEntity. Recipe (added in datagen) requires its own skill tree
    // unlock to actually craft, separate from the Storage System unlock.
    public static final RegistryObject<Item> ARCANE_ASSEMBLER = ITEMS.register("arcane_assembler",
            () -> new BlockItem(ModBlocks.ARCANE_ASSEMBLER.get(), new Item.Properties()));

    // Installed in a Terminal's upgrade slots - +Config.stackSizeBonusPerStackUpgrade max stack size per
    // copy, up to TerminalBlockEntity.UPGRADE_SLOTS installed. Crafts freely, not skill-tree gated.
    // stacksTo(1): TerminalBlockEntity.countUpgrades sums stack.getCount() per slot, so a stackable item
    // would let one slot alone provide many upgrades' worth of bonus - one physical item per slot instead.
    public static final RegistryObject<Item> STACK_UPGRADE = ITEMS.register("stack_upgrade",
            () -> new Item(new Item.Properties().stacksTo(1)));

    // Placed adjacent to a Living Chest to anchor the Overload Ritual - see OverloadCoreBlockEntity.
    // Recipe (added in datagen) requires the Overload Ritual skill tree unlock to actually craft.
    public static final RegistryObject<Item> OVERLOAD_CORE = ITEMS.register("overload_core",
            () -> new BlockItem(ModBlocks.OVERLOAD_CORE.get(), new Item.Properties()));

    // Consumed by OverloadCoreBlockEntity.tryStartRitual - a plain item, no special use()/useOn() of its
    // own, matching the ARCANE_INK precedent of not needing a dedicated Item subclass for a stat-less
    // material. Recipe (added in datagen) requires the same Overload Ritual skill tree unlock.
    public static final RegistryObject<Item> WARPED_CATALYST = ITEMS.register("warped_catalyst",
            () -> new Item(new Item.Properties()));

    // Boss-loot from Broken Vessel, analogous to a Netherite Ingot - the "addition" material for the
    // Vessel-Bound smithing upgrade recipes below.
    public static final RegistryObject<Item> WARPED_VESSEL_SHARD = ITEMS.register("warped_vessel_shard",
            () -> new Item(new Item.Properties()));

    // Boss-loot from Broken Vessel - the "template" slot item for the same smithing recipes, duplicable
    // via the same vanilla template-duplication recipe shape Ancient City smithing templates already use.
    public static final RegistryObject<Item> WARPED_ATTUNEMENT_TEMPLATE = ITEMS.register("warped_attunement_template",
            () -> new Item(new Item.Properties()));

    // Smithing-upgraded from the original Sleeping God pieces (see the 4 SmithingTransformRecipes in
    // datagen) - built on the same SleepingGodArmorMaterial (an attunement, not a stat upgrade), with a
    // per-item texture override (VesselBoundArmorItem) and an improved flight passive
    // (VesselBoundFlightHandler) instead of the original's grace/cooldown flight model.
    public static final RegistryObject<Item> VESSEL_BOUND_SLEEPING_GOD_HELMET = ITEMS.register("vessel_bound_sleeping_god_helmet",
            () -> new VesselBoundArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> VESSEL_BOUND_SLEEPING_GOD_CHESTPLATE = ITEMS.register("vessel_bound_sleeping_god_chestplate",
            () -> new VesselBoundArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));

    // The Vessel-Bound chestplate with an Elytra smithed in (Attunement Template + Vessel-Bound Chestplate +
    // Elytra). Counts as the set's chestplate everywhere (see VesselBoundFlightHandler) and can glide.
    public static final RegistryObject<Item> VESSEL_BOUND_ELYTRA_CHESTPLATE = ITEMS.register("vessel_bound_elytra_chestplate",
            () -> new VesselBoundElytraItem(new Item.Properties()));

    public static final RegistryObject<Item> VESSEL_BOUND_SLEEPING_GOD_LEGGINGS = ITEMS.register("vessel_bound_sleeping_god_leggings",
            () -> new VesselBoundArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final RegistryObject<Item> VESSEL_BOUND_SLEEPING_GOD_BOOTS = ITEMS.register("vessel_bound_sleeping_god_boots",
            () -> new VesselBoundArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    // Installed in a Terminal's upgrade slots - +Config.slotsPerSlotUpgrade bonus pool slots per copy,
    // up to TerminalBlockEntity.UPGRADE_SLOTS installed. Crafts freely, not skill-tree gated.
    // stacksTo(1): see STACK_UPGRADE's comment - same one-per-slot reasoning.
    public static final RegistryObject<Item> SLOT_UPGRADE = ITEMS.register("slot_upgrade",
            () -> new Item(new Item.Properties().stacksTo(1)));

    // Binds to a Terminal (right-click it), then re-opens that Terminal's UI from anywhere (right-click in
    // open air). Gated behind Warped Attunement via OverloadRitualUnlockHandler. stacksTo(1): each copy
    // carries its own independent binding in NBT, same one-instance-per-item reasoning as the charms/
    // bracelets.
    public static final RegistryObject<Item> WARPED_ANCHOR = ITEMS.register("warped_anchor",
            () -> new WarpedAnchorItem(new Item.Properties().stacksTo(1)));

    // Re-summons Broken Vessel inside its lair once the lair's one-time original summon has already been
    // used - see VesselSummoningCharmItem for the bounding-box/no-boss-alive gate.
    public static final RegistryObject<Item> VESSEL_SUMMONING_CHARM = ITEMS.register("vessel_summoning_charm",
            () -> new VesselSummoningCharmItem(new Item.Properties()));

    public static final RegistryObject<Item> HUNGER_FAITH_ALTAR = ITEMS.register("hunger_faith_altar",
            () -> new BlockItem(ModBlocks.HUNGER_FAITH_ALTAR.get(), new Item.Properties()));

    public static final RegistryObject<Item> FERTILITY_FAITH_ALTAR = ITEMS.register("fertility_faith_altar",
            () -> new BlockItem(ModBlocks.FERTILITY_FAITH_ALTAR.get(), new Item.Properties()));

    public static final RegistryObject<Item> WAR_FAITH_ALTAR = ITEMS.register("war_faith_altar",
            () -> new BlockItem(ModBlocks.WAR_FAITH_ALTAR.get(), new Item.Properties()));

    public static final RegistryObject<Item> SUN_FAITH_ALTAR = ITEMS.register("sun_faith_altar",
            () -> new BlockItem(ModBlocks.SUN_FAITH_ALTAR.get(), new Item.Properties()));

    public static final RegistryObject<Item> THEATER_FAITH_ALTAR = ITEMS.register("theater_faith_altar",
            () -> new BlockItem(ModBlocks.THEATER_FAITH_ALTAR.get(), new Item.Properties()));

    // Deliberately zero-defense (see TheaterArmorMaterial) - the 10% dodge chance compensates instead.
    // Unlike VESSEL_BOUND_SLEEPING_GOD_HELMET this doesn't extend any Sleeping God flight class - it's
    // a completely separate item competing for the same HEAD slot, not a variant of that set.
    public static final RegistryObject<Item> THEATER_HELMET = ITEMS.register("theater_helmet",
            () -> new ArmorItem(TheaterArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new Item.Properties()));

    // Lets a player who already has a pledge swap it for another (see ServerboundChoosePledgePacket) -
    // consumed only on a swap, never on the free first-time choice.
    public static final RegistryObject<Item> PACK_OF_THE_GODS = ITEMS.register("pack_of_the_gods",
            () -> new PackOfTheGodsItem(new Item.Properties()));

    // Was Aurelia's companion-held prop (no combat stats, just a rendered visual) before that got
    // removed per her model's own request - now repurposed as her actual God Challenge Hub boss drop
    // (see AbstractGodBossEntity's death-drop logic), so it's upgraded to a real weapon. Slightly
    // stronger than a diamond sword (modifier 4 vs. diamond's 3, same swing speed) to read as a
    // boss-tier reward. fireResistant() per the god-drop loss-prevention design - see GodDropLossHandler
    // and GodDropVoidRescueHandler, which together with this cover fire/lava/void; only durability
    // breaking is left as a real loss path, tracked via the granted/broken counters on IFaith.
    // Boosted past that first pass (modifier 6, +2 entity reach - see AureliasSpearItem) since the fight it
    // rewards is a hard one.
    public static final RegistryObject<Item> SPEAR = ITEMS.register("spear",
            () -> new AureliasSpearItem(Tiers.DIAMOND, 6, -2.4F, new Item.Properties().fireResistant()));

    // Florian's God Challenge Hub boss drop - right-click summons a debuffed Florian for a short while (see
    // FloriansAntlerItem). fireResistant() for the same god-drop loss-prevention reasons as the Spear.
    public static final RegistryObject<Item> FLORIANS_ANTLER = ITEMS.register("florians_antler",
            () -> new FloriansAntlerItem(new Item.Properties().stacksTo(1).fireResistant()));
}
