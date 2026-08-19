package com.arcanelens.advancement;

import com.arcanelens.ArcaneLens;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

import org.jetbrains.annotations.Nullable;

/** Copy-shape of SpellLearnedTrigger, keyed on SkillType#name() instead of a spell ResourceLocation - lets one
 * trigger back an advancement per Skill Tree node without coupling this package to network.packet.SkillType. */
public class SkillPurchasedTrigger extends SimpleCriterionTrigger<SkillPurchasedTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation(ArcaneLens.MODID, "skill_purchased");

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context)
    {
        String skill = json.has("skill") ? GsonHelper.getAsString(json, "skill") : null;
        return new TriggerInstance(predicate, skill);
    }

    public void trigger(ServerPlayer player, String skill)
    {
        this.trigger(player, instance -> instance.matches(skill));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        @Nullable
        private final String skill;

        public TriggerInstance(ContextAwarePredicate predicate, @Nullable String skill)
        {
            super(SkillPurchasedTrigger.ID, predicate);
            this.skill = skill;
        }

        public boolean matches(String skill)
        {
            return this.skill == null || this.skill.equals(skill);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject json = super.serializeToJson(context);
            if (skill != null)
            {
                json.addProperty("skill", skill);
            }
            return json;
        }
    }
}
