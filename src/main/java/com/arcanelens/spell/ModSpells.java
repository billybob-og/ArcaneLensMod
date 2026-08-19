package com.arcanelens.spell;

import com.arcanelens.api.spell.Spell;
import com.arcanelens.api.spell.SpellRegistry;
import com.arcanelens.spell.spells.ArcaneShieldSpell;
import com.arcanelens.spell.spells.ArrowVolleySpell;
import com.arcanelens.spell.spells.BlinkSpell;
import com.arcanelens.spell.spells.ChainLightningSpell;
import com.arcanelens.spell.spells.FireballSpell;
import com.arcanelens.spell.spells.FrostBoltSpell;
import com.arcanelens.spell.spells.GravityPullSpell;
import com.arcanelens.spell.spells.HasteSurgeSpell;
import com.arcanelens.spell.spells.HealSpell;
import com.arcanelens.spell.spells.InvisibilitySpell;
import com.arcanelens.spell.spells.LifeDrainSpell;
import com.arcanelens.spell.spells.LightningStrikeSpell;
import com.arcanelens.spell.spells.MeteorShowerSpell;
import com.arcanelens.spell.spells.PocketDimensionSpell;
import com.arcanelens.spell.spells.PoisonBoltSpell;
import com.arcanelens.spell.spells.ShockwaveSpell;
import com.arcanelens.spell.spells.SummonAllySpell;
import net.minecraftforge.registries.RegistryObject;

public class ModSpells
{
    public static final RegistryObject<Spell> FIREBALL = SpellRegistry.SPELLS.register("fireball", FireballSpell::new);
    public static final RegistryObject<Spell> HEAL = SpellRegistry.SPELLS.register("heal", HealSpell::new);
    public static final RegistryObject<Spell> FROST_BOLT = SpellRegistry.SPELLS.register("frost_bolt", FrostBoltSpell::new);
    public static final RegistryObject<Spell> BLINK = SpellRegistry.SPELLS.register("blink", BlinkSpell::new);
    public static final RegistryObject<Spell> LIGHTNING_STRIKE = SpellRegistry.SPELLS.register("lightning_strike", LightningStrikeSpell::new);

    public static final RegistryObject<Spell> CHAIN_LIGHTNING = SpellRegistry.SPELLS.register("chain_lightning", ChainLightningSpell::new);
    public static final RegistryObject<Spell> METEOR_SHOWER = SpellRegistry.SPELLS.register("meteor_shower", MeteorShowerSpell::new);
    public static final RegistryObject<Spell> POISON_BOLT = SpellRegistry.SPELLS.register("poison_bolt", PoisonBoltSpell::new);
    public static final RegistryObject<Spell> LIFE_DRAIN = SpellRegistry.SPELLS.register("life_drain", LifeDrainSpell::new);
    public static final RegistryObject<Spell> ARCANE_SHIELD = SpellRegistry.SPELLS.register("arcane_shield", ArcaneShieldSpell::new);
    public static final RegistryObject<Spell> HASTE_SURGE = SpellRegistry.SPELLS.register("haste_surge", HasteSurgeSpell::new);
    public static final RegistryObject<Spell> GRAVITY_PULL = SpellRegistry.SPELLS.register("gravity_pull", GravityPullSpell::new);
    public static final RegistryObject<Spell> SHOCKWAVE = SpellRegistry.SPELLS.register("shockwave", ShockwaveSpell::new);
    public static final RegistryObject<Spell> SUMMON_ALLY = SpellRegistry.SPELLS.register("summon_ally", SummonAllySpell::new);
    public static final RegistryObject<Spell> ARROW_VOLLEY = SpellRegistry.SPELLS.register("arrow_volley", ArrowVolleySpell::new);
    public static final RegistryObject<Spell> INVISIBILITY = SpellRegistry.SPELLS.register("invisibility", InvisibilitySpell::new);

    // Skill-tree-only unlock (see ServerboundPurchaseSkillPacket) - excluded from the Ancient Spell Book
    // loot injection pool in ModLootTableProvider so it never turns up as ordinary dungeon loot.
    public static final RegistryObject<Spell> POCKET_DIMENSION = SpellRegistry.SPELLS.register("pocket_dimension", PocketDimensionSpell::new);

    public static void register()
    {
        // static init trigger
    }
}
