package com.arcanelens.advancement;

import net.minecraft.advancements.CriteriaTriggers;

public class ModCriteriaTriggers
{
    public static final PedestalsLinkedTrigger PEDESTALS_LINKED = CriteriaTriggers.register(new PedestalsLinkedTrigger());
    public static final LensManaMaxedTrigger LENS_MANA_MAXED = CriteriaTriggers.register(new LensManaMaxedTrigger());
    public static final LensesCombinedTrigger LENSES_COMBINED = CriteriaTriggers.register(new LensesCombinedTrigger());
    public static final LensSlotsFilledTrigger LENS_SLOTS_FILLED = CriteriaTriggers.register(new LensSlotsFilledTrigger());
    public static final SpellLearnedTrigger SPELL_LEARNED = CriteriaTriggers.register(new SpellLearnedTrigger());
    public static final HungerPointsTheWayTrigger HUNGER_POINTS_THE_WAY = CriteriaTriggers.register(new HungerPointsTheWayTrigger());
    public static final SleepingGodRelicFoundTrigger SLEEPING_GOD_RELIC_FOUND = CriteriaTriggers.register(new SleepingGodRelicFoundTrigger());
    public static final SkillPurchasedTrigger SKILL_PURCHASED = CriteriaTriggers.register(new SkillPurchasedTrigger());
    public static final PledgeMadeTrigger PLEDGE_MADE = CriteriaTriggers.register(new PledgeMadeTrigger());

    public static void register()
    {
        // static init above registers each trigger; calling this forces class-load
    }
}
