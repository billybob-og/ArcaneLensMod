package com.arcanelens.advancement;

import com.arcanelens.ArcaneLens;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class LensSlotsFilledTrigger extends SimpleCriterionTrigger<LensSlotsFilledTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation(ArcaneLens.MODID, "lens_slots_filled");

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context)
    {
        MinMaxBounds.Ints count = MinMaxBounds.Ints.fromJson(json.get("count"));
        return new TriggerInstance(predicate, count);
    }

    public void trigger(ServerPlayer player, int count)
    {
        this.trigger(player, instance -> instance.matches(count));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final MinMaxBounds.Ints count;

        public TriggerInstance(ContextAwarePredicate predicate, MinMaxBounds.Ints count)
        {
            super(LensSlotsFilledTrigger.ID, predicate);
            this.count = count;
        }

        public boolean matches(int count)
        {
            return this.count.matches(count);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject json = super.serializeToJson(context);
            json.add("count", this.count.serializeToJson());
            return json;
        }
    }
}
