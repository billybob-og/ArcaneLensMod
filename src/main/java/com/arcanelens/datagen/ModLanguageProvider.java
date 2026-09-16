package com.arcanelens.datagen;

import com.arcanelens.ArcaneLens;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider
{
    public ModLanguageProvider(PackOutput output)
    {
        super(output, ArcaneLens.MODID, "en_us");
    }

    @Override
    protected void addTranslations()
    {
        add("itemGroup.arcanelens.arcane_lens_tab", "Arcane Lens");
        add("item.arcanelens.magic_lens", "Magic Lens");
        add("item.arcanelens.arcane_guide", "Arcane Guide");
        add("block.arcanelens.inscription_workbench", "Inscription Workbench");
        add("block.arcanelens.soul_pedestal", "Soul Pedestal");
        add("block.arcanelens.lens_pedestal", "Lens Pedestal");
        add("item.arcanelens.ancient_spell_book", "Ancient Spell Book");
        add("block.arcanelens.arcane_ore", "Arcane Ore");
        add("item.arcanelens.raw_arcane_material", "Raw Arcane Material");
        add("block.arcanelens.lens_combiner", "Lens Combiner");
        add("item.arcanelens.diamond_engraver", "Diamond Engraver");
        add("block.arcanelens.soul_feeder", "Soul Feeder");
        add("block.arcanelens.arena_trigger", "Arena Trigger");
        add("block.arcanelens.faith_altar", "Faith Altar");
        add("block.arcanelens.god_challenge_altar", "God Challenge Altar");
        add("block.arcanelens.command_trigger", "Command Trigger");
        add("block.arcanelens.trigger_plate", "Trigger Plate");
        add("item.arcanelens.weird_amulet", "Weird Amulet");
        add("item.arcanelens.god_hub_medallion", "God Hub Medallion");
        add("entity.arcanelens.the_hunger", "The Hunger");
        add("item.arcanelens.token_of_the_hunger", "Token of the Hunger");
        add("item.arcanelens.hungers_boon", "The Hunger's Boon");
        add("item.arcanelens.hungers_bindings", "The Hunger's Bindings");
        add("item.arcanelens.sun_and_moon_ward", "Sun and Moon Ward");
        add("item.arcanelens.magnet_charm", "Magnet Charm");
        add("item.arcanelens.faith_charm", "Faith Charm");
        add("item.arcanelens.mana_orb_charm", "Mana Orb Charm");
        add("item.arcanelens.burning_charcoal_charm", "Burning Charcoal Charm");
        add("item.arcanelens.antivenom_charm", "Antivenom Charm");
        add("item.arcanelens.preserved_wither_rose_charm", "Preserved Wither Rose Charm");
        add("item.arcanelens.iron_bracelet", "Iron Bracelet");
        add("item.arcanelens.gold_bracelet", "Gold Bracelet");
        add("item.arcanelens.netherite_bracelet", "Netherite Bracelet");
        add("item.arcanelens.arcane_ink", "Arcane Ink");
        add("item.arcanelens.hungers_pact", "The Hunger's Pact");
        add("entity.arcanelens.sleeping_god", "The One Who Sleeps");
        add("entity.arcanelens.theater_clone", "Stage Clone");
        add("item.arcanelens.sleeping_god_helmet", "Helm of the Minor God");
        add("item.arcanelens.sleeping_god_chestplate", "Chestplate of the Minor God");
        add("item.arcanelens.sleeping_god_leggings", "Leggings of the Minor God");
        add("item.arcanelens.sleeping_god_boots", "Boots of the Minor God");
        add("key.categories.arcanelens", "Arcane Lens");
        add("key.arcanelens.cast_spell", "Cast Spell");
        add("key.arcanelens.cycle_spell", "Cycle Spell");

        add("advancements.arcanelens.root.title", "Arcane Lens");
        add("advancements.arcanelens.root.description", "Enter a world touched by arcane power");
        add("advancements.arcanelens.make_a_lens.title", "A Lens of One's Own");
        add("advancements.arcanelens.make_a_lens.description", "Craft a Magic Lens");
        add("advancements.arcanelens.link_pedestals.title", "Soulbound Circuit");
        add("advancements.arcanelens.link_pedestals.description", "Link a Soul Pedestal to a Lens Pedestal");
        add("advancements.arcanelens.max_a_lens.title", "Fully Inscribed");
        add("advancements.arcanelens.max_a_lens.description", "Fill all 5 spell slots on a Magic Lens");
        add("advancements.arcanelens.overcharge_a_lens.title", "Overcharged");
        add("advancements.arcanelens.overcharge_a_lens.description", "Charge a Magic Lens to its maximum mana");
        add("advancements.arcanelens.combine_lenses.title", "Two Become One");
        add("advancements.arcanelens.combine_lenses.description", "Combine two Magic Lenses at the Lens Combiner");
        add("advancements.arcanelens.fill_25_slots.title", "Spellbound");
        add("advancements.arcanelens.fill_25_slots.description", "Fill a Magic Lens with 25 total spell slots");
        add("advancements.arcanelens.learn_all_spells.title", "Apprentice Lens User");
        add("advancements.arcanelens.learn_all_spells.description", "Learn all 5 starter spells");
        add("advancements.arcanelens.master_of_the_arcane.title", "Master of the Arcane");
        add("advancements.arcanelens.master_of_the_arcane.description", "Learn all 16 spells");
        add("advancements.arcanelens.defeat_sleeping_god.title", "The New Minor God");
        add("advancements.arcanelens.defeat_sleeping_god.description", "Defeat The One Who Sleeps");
        add("advancements.arcanelens.defeat_broken_vessel.title", "What the Fungus Wore");
        add("advancements.arcanelens.defeat_broken_vessel.description", "Defeat Broken Vessel");
        add("advancements.arcanelens.hunger_points_the_way.title", "A Place, Named");
        add("advancements.arcanelens.hunger_points_the_way.description", "Learn where the Hunger says something of the sleeping god remains");
        add("advancements.arcanelens.find_warped_relic.title", "What Slept, Named");
        add("advancements.arcanelens.find_warped_relic.description", "Find the Sleeping God's relic within the Warped Hollow");
        add("advancements.arcanelens.storage_system_unlocked.title", "Wired Together");
        add("advancements.arcanelens.storage_system_unlocked.description", "Purchase the Storage System in the Skill Tree");
        add("advancements.arcanelens.arcane_assembler_unlocked.title", "Hands Off");
        add("advancements.arcanelens.arcane_assembler_unlocked.description", "Purchase the Arcane Assembler in the Skill Tree");
        add("advancements.arcanelens.overload_ritual_unlocked.title", "Deliberate Strain");
        add("advancements.arcanelens.overload_ritual_unlocked.description", "Purchase the Overload Ritual in the Skill Tree");
        add("advancements.arcanelens.pledge_made.title", "A Choice Made");
        add("advancements.arcanelens.pledge_made.description", "Pledge your Faith to a god in the Warped Hollow");
        add("advancements.arcanelens.pledge_made_hunger.title", "Sworn to the Hunger");
        add("advancements.arcanelens.pledge_made_hunger.description", "Pledge your Faith to the Hunger");
        add("advancements.arcanelens.pledge_made_fertility.title", "Sworn to Florian");
        add("advancements.arcanelens.pledge_made_fertility.description", "Pledge your Faith to Florian");
        add("advancements.arcanelens.pledge_made_war.title", "Sworn to Aurelia");
        add("advancements.arcanelens.pledge_made_war.description", "Pledge your Faith to Aurelia");
        add("advancements.arcanelens.pledge_made_sun.title", "Sworn to Quetzera");
        add("advancements.arcanelens.pledge_made_sun.description", "Pledge your Faith to Quetzera");
        add("advancements.arcanelens.pledge_made_theater.title", "Sworn to Janus");
        add("advancements.arcanelens.pledge_made_theater.description", "Pledge your Faith to Janus");
        add("block.arcanelens.hunger_faith_altar", "Hunger's Faith Altar");
        add("block.arcanelens.fertility_faith_altar", "Florian's Faith Altar");
        add("block.arcanelens.war_faith_altar", "Aurelia's Faith Altar");
        add("block.arcanelens.sun_faith_altar", "Quetzera's Faith Altar");
        add("block.arcanelens.theater_faith_altar", "Janus's Faith Altar");
        add("item.arcanelens.theater_helmet", "Mask of Janus");
        add("item.arcanelens.pack_of_the_gods", "A Godly Contract");
        add("item.arcanelens.spear", "Aurelia's Spear");
        add("entity.arcanelens.aurelia_companion", "Aurelia");
        add("advancements.arcanelens.unlock_the_pact.title", "A Bargain Struck");
        add("advancements.arcanelens.unlock_the_pact.description", "Obtain The Hunger's Pact from the Hunger Idol");
        add("advancements.arcanelens.step_beyond.title", "Step Beyond");
        add("advancements.arcanelens.step_beyond.description", "Learn the Pocket Dimension spell");
        add("advancements.arcanelens.living_network.title", "A Mind of Many Chests");
        add("advancements.arcanelens.living_network.description", "Craft a Living Terminal");
        add("advancements.arcanelens.self_sufficient.title", "Hands Off");
        add("advancements.arcanelens.self_sufficient.description", "Craft an Arcane Assembler");
        add("advancements.arcanelens.accessorize.title", "Adorned");
        add("advancements.arcanelens.accessorize.description", "Obtain a Charm or Bracelet");

        add("block.arcanelens.storage_block", "Living Chest");
        add("block.arcanelens.terminal", "Living Terminal");
        add("block.arcanelens.storage_connector", "Storage Connector");
        add("item.arcanelens.stack_upgrade", "Compactor Upgrade");
        add("item.arcanelens.slot_upgrade", "Slot Upgrade");
        add("block.arcanelens.arcane_assembler", "Arcane Assembler");

        add("block.arcanelens.overload_core", "Overload Core");
        add("item.arcanelens.warped_catalyst", "Warped Catalyst");
        add("block.arcanelens.overload_portal", "Overload Portal");
        add("block.arcanelens.sleeping_god_relic_altar", "Relic of the God of Sleep");
        add("item.arcanelens.warped_vessel_shard", "Warped Vessel Shard");
        add("item.arcanelens.warped_attunement_template", "Warped Attunement Template");
        add("item.arcanelens.vessel_bound_sleeping_god_helmet", "Vessel-Bound Helm of the Minor God");
        add("item.arcanelens.vessel_bound_sleeping_god_chestplate", "Vessel-Bound Chestplate of the Minor God");
        add("item.arcanelens.vessel_bound_sleeping_god_leggings", "Vessel-Bound Leggings of the Minor God");
        add("item.arcanelens.vessel_bound_sleeping_god_boots", "Vessel-Bound Boots of the Minor God");
        add("item.arcanelens.warped_anchor", "Warped Anchor");
        add("item.arcanelens.vessel_summoning_charm", "Vessel Summoning Charm");
    }
}
