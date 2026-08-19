package com.arcanelens.item;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.god.GodRegistry;
import com.arcanelens.god.NatureBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;

import java.util.UUID;

/**
 * Fertility's real perk: extra max hearts scaling with Faith (capped), plus a small passive heal
 * while standing near nature once that cap is fully reached ("two rows of hearts") - matches the
 * design brief exactly. Recomputed once a second (RECHECK_INTERVAL_TICKS), not every tick, since
 * neither the attribute modifier nor the nature-block scan needs finer granularity than that.
 */
public class FertilityPerkHandler
{
    private static final UUID BONUS_HEARTS_MODIFIER_ID = UUID.fromString("7a3c1e9a-4f2b-4d6e-9a1c-2f8b6d3e5c11");
    private static final int RECHECK_INTERVAL_TICKS = 20;
    private static final int HEAL_INTERVAL_TICKS = 60;
    private static final int NATURE_CHECK_RADIUS = 3;

    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !(event.player instanceof ServerPlayer player))
        {
            return;
        }
        if (player.tickCount % RECHECK_INTERVAL_TICKS != 0)
        {
            return;
        }

        player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
            String pledgedGod = cap.getPledgedGod();
            // Progress toward the CURRENT pledge, not lifetime Faith - a player who already has 1000+
            // Faith by the time they reach the Warped Hollow shouldn't instantly max this out on
            // pledging. See IFaith.getPledgeProgress()/FaithImpl.setPledgedGod().
            double bonusHearts = computeBonusHearts(pledgedGod, cap.getPledgeProgress());
            applyBonusHearts(player, bonusHearts);

            boolean atFullBonus = pledgedGod.equals("fertility") && bonusHearts >= Config.fertilityMaxBonusHearts;
            if (atFullBonus && player.tickCount % HEAL_INTERVAL_TICKS == 0 && player.getHealth() < player.getMaxHealth()
                    && isNearNature(player))
            {
                player.heal(1.0F);
            }
        });
    }

    private static double computeBonusHearts(String pledgedGod, int pledgeProgress)
    {
        double rawHearts = Math.min(Config.fertilityMaxBonusHearts, (double) pledgeProgress / Config.fertilityFaithPerHeart);
        if (pledgedGod.equals("fertility"))
        {
            return rawHearts;
        }
        if (pledgedGod.equals(GodRegistry.NEUTRAL_ID))
        {
            return rawHearts * Config.neutralFertilityHeartScale;
        }
        return 0.0;
    }

    private static void applyBonusHearts(ServerPlayer player, double bonusHearts)
    {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null)
        {
            return;
        }

        maxHealth.removeModifier(BONUS_HEARTS_MODIFIER_ID);
        if (bonusHearts > 0)
        {
            maxHealth.addTransientModifier(new AttributeModifier(BONUS_HEARTS_MODIFIER_ID,
                    "arcanelens:fertility_bonus_hearts", bonusHearts * 2.0, AttributeModifier.Operation.ADDITION));
        }
    }

    private static boolean isNearNature(ServerPlayer player)
    {
        Level level = player.level();
        AABB box = player.getBoundingBox().inflate(NATURE_CHECK_RADIUS);
        BlockPos min = new BlockPos((int) Math.floor(box.minX), (int) Math.floor(box.minY), (int) Math.floor(box.minZ));
        BlockPos max = new BlockPos((int) Math.floor(box.maxX), (int) Math.floor(box.maxY), (int) Math.floor(box.maxZ));
        for (BlockPos check : BlockPos.betweenClosed(min, max))
        {
            BlockState state = level.getBlockState(check);
            if (NatureBlocks.isNatureBlock(state))
            {
                return true;
            }
        }
        return false;
    }
}
