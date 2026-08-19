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

public class SpellLearnedTrigger extends SimpleCriterionTrigger<SpellLearnedTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation(ArcaneLens.MODID, "spell_learned");

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context)
    {
        ResourceLocation spellId = json.has("spell") ? new ResourceLocation(GsonHelper.getAsString(json, "spell")) : null;
        return new TriggerInstance(predicate, spellId);
    }

    public void trigger(ServerPlayer player, ResourceLocation spellId)
    {
        this.trigger(player, instance -> instance.matches(spellId));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        @Nullable
        private final ResourceLocation spellId;

        public TriggerInstance(ContextAwarePredicate predicate, @Nullable ResourceLocation spellId)
        {
            super(SpellLearnedTrigger.ID, predicate);
            this.spellId = spellId;
        }

        public boolean matches(ResourceLocation spellId)
        {
            return this.spellId == null || this.spellId.equals(spellId);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject json = super.serializeToJson(context);
            if (spellId != null)
            {
                json.addProperty("spell", spellId.toString());
            }
            return json;
        }
    }
}
