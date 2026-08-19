package com.arcanelens.advancement;

import com.arcanelens.ArcaneLens;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class SleepingGodRelicFoundTrigger extends SimpleCriterionTrigger<SleepingGodRelicFoundTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation(ArcaneLens.MODID, "sleeping_god_relic_found");

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context)
    {
        return new TriggerInstance(predicate);
    }

    public void trigger(ServerPlayer player)
    {
        this.trigger(player, instance -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public TriggerInstance(ContextAwarePredicate predicate)
        {
            super(SleepingGodRelicFoundTrigger.ID, predicate);
        }
    }
}
