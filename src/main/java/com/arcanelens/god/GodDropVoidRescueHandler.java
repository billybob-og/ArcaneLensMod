package com.arcanelens.god;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Rescues a dropped god-boss item from falling into the void, the other half of loss-prevention
 * alongside each drop's fireResistant() property (fire/lava) - see GodDropLossHandler for the remaining
 * loss path (durability). Checked once a second, not every tick - a dropped item falling has plenty of
 * time to be caught before it actually despawns, and almost nothing is ever below the void threshold
 * anyway, so this stays cheap regardless of how wide the scan area is. */
public class GodDropVoidRescueHandler
{
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final int VOID_THRESHOLD_OFFSET = 8;

    public static void onLevelTick(TickEvent.LevelTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !(event.level instanceof ServerLevel serverLevel))
        {
            return;
        }
        if (serverLevel.getGameTime() % CHECK_INTERVAL_TICKS != 0)
        {
            return;
        }

        int voidY = serverLevel.getMinBuildHeight() - VOID_THRESHOLD_OFFSET;
        AABB belowVoid = new AABB(-3.0E7, serverLevel.getMinBuildHeight() - 64, -3.0E7, 3.0E7, voidY, 3.0E7);
        List<ItemEntity> falling = serverLevel.getEntitiesOfClass(ItemEntity.class, belowVoid);
        if (falling.isEmpty())
        {
            return;
        }

        Set<Item> godDrops = GodRegistry.GODS.stream()
                .filter(GodDefinition::hasBossContent)
                .map(god -> god.uniqueDrop().get().get())
                .collect(Collectors.toSet());

        for (ItemEntity itemEntity : falling)
        {
            ItemStack stack = itemEntity.getItem();
            if (!godDrops.contains(stack.getItem()))
            {
                continue;
            }

            if (itemEntity.getOwner() instanceof Player owner)
            {
                itemEntity.setPos(owner.getX(), owner.getY() + 1.0, owner.getZ());
            }
            else
            {
                itemEntity.setPos(itemEntity.getX(), serverLevel.getMaxBuildHeight() - 4.0, itemEntity.getZ());
            }
            itemEntity.setDeltaMovement(0.0, 0.0, 0.0);
        }
    }
}
