package com.arcanelens.spell;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingEvent;

/** Generic expiry for any temporary summon (tagged via EXPIRY_TAG on its own persistent data) - not
 * Wolf-specific despite SummonAllySpell being the first thing to use it; AureliaCompanionEntity shares
 * this same mechanism rather than each summon type needing its own tick-based expiry handler. */
public class SummonExpiryHandler
{
    public static final String EXPIRY_TAG = "ArcaneLensSummonExpiry";

    public static void onLivingTick(LivingEvent.LivingTickEvent event)
    {
        if (event.getEntity().level().isClientSide || !(event.getEntity() instanceof Mob mob))
        {
            return;
        }

        CompoundTag data = mob.getPersistentData();
        if (data.contains(EXPIRY_TAG) && mob.level().getGameTime() >= data.getLong(EXPIRY_TAG))
        {
            mob.discard();
        }
    }
}
