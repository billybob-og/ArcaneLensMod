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

/** Copy-shape of SkillPurchasedTrigger, keyed on the chosen god's id - fires once on a player's first
 * pledge (see ServerboundChoosePledgePacket), never on a later swap. */
public class PledgeMadeTrigger extends SimpleCriterionTrigger<PledgeMadeTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation(ArcaneLens.MODID, "pledge_made");

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context)
    {
        String god = json.has("god") ? GsonHelper.getAsString(json, "god") : null;
        return new TriggerInstance(predicate, god);
    }

    public void trigger(ServerPlayer player, String god)
    {
        this.trigger(player, instance -> instance.matches(god));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        @Nullable
        private final String god;

        public TriggerInstance(ContextAwarePredicate predicate, @Nullable String god)
        {
            super(PledgeMadeTrigger.ID, predicate);
            this.god = god;
        }

        public boolean matches(String god)
        {
            return this.god == null || this.god.equals(god);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject json = super.serializeToJson(context);
            if (god != null)
            {
                json.addProperty("god", god);
            }
            return json;
        }
    }
}
