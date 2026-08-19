package com.arcanelens.item;

import com.arcanelens.capability.FaithProvider;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.event.TickEvent;

/**
 * The Hunger's Bindings only needs to be released by fire - any fire, anywhere, not tied to a Faith Altar or
 * soul fire specifically (unlike ordinary Faith burning, see FaithAltarBlockEntity). Scans every tick for a
 * Bindings item entity that vanilla itself has actually set on fire and credits the owning player a
 * burden-resist boon level, discarding it once found.
 *
 * Trusts itemEntity.isOnFire() exclusively rather than "is there fire somewhere nearby": an earlier version
 * credited items merely passing near a fire block, which snatched several out of the world one tick before
 * they actually reached the flame (visually vanishing mid-air) - a worse bug than the under-count it was
 * meant to fix. Scanning every tick (rather than every 5-10) is what actually closes the gap: an item only
 * stays on fire for ~5 seconds (health 5, 1 damage per 20 ticks) before vanilla destroys it outright, so a
 * slower scan could miss that whole window for an item that reaches the fire late in a scatter.
 */
public class HungersBindingsHandler
{
    public static void onLevelTick(TickEvent.LevelTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !(event.level instanceof ServerLevel serverLevel))
        {
            return;
        }

        for (ItemEntity itemEntity : serverLevel.getEntities(EntityTypeTest.forClass(ItemEntity.class),
                entity -> entity.isAlive() && entity.isOnFire() && entity.getItem().is(ModItems.HUNGERS_BINDINGS.get())))
        {
            if (!(itemEntity.getOwner() instanceof ServerPlayer player))
            {
                // No known player to credit (e.g. dropped by a non-player source) - let it burn out normally.
                continue;
            }

            int count = itemEntity.getItem().getCount();
            itemEntity.discard();
            player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
                for (int i = 0; i < count; i++)
                {
                    cap.incrementBurdenResistBoonLevel();
                }
            });
            FaithSync.syncToClient(player);
        }
    }
}
