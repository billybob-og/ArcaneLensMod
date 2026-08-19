package com.arcanelens.item;

import com.arcanelens.entity.BrokenVesselEntity;
import com.arcanelens.registry.ModEntityTypes;
import com.arcanelens.worldgen.ModDimensions;
import com.arcanelens.worldgen.VesselLairPlacer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * The only way to re-fight Broken Vessel once the lair's original one-time command-block summon (see
 * VesselLairPlacer) has already been used - there's just one shared lair, not a fresh per-instance arena
 * like the castle dungeon, so a repeatable trigger has to live somewhere else. Restricted to actually
 * standing inside the lair (VesselLairPlacer.getLairBoundingBox) so this can't be used to drop a boss
 * anywhere in the Warped Hollow, and to there being no Broken Vessel already alive nearby, guarding against
 * double-summon stacking if two players use Charms at once. Consumed only on a successful summon - every
 * failure path returns the stack untouched.
 */
public class VesselSummoningCharmItem extends Item
{
    public VesselSummoningCharmItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer))
        {
            return InteractionResultHolder.pass(stack);
        }

        if (level.dimension() != ModDimensions.BROKEN_VESSEL_KEY)
        {
            message(serverPlayer, "This only works within the Warped Hollow.");
            return InteractionResultHolder.fail(stack);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        AABB lairBounds = VesselLairPlacer.getLairBoundingBox();
        if (!lairBounds.contains(player.position()))
        {
            message(serverPlayer, "You must be inside Broken Vessel's lair to summon it.");
            return InteractionResultHolder.fail(stack);
        }

        // Server-tick-authoritative, not a client-side guess - two players both right-clicking Charms in
        // the same tick both re-check this same live entity list, so at most one summon can ever pass
        // (same reasoning OverloadCoreBlockEntity's ritual-start capacity check already relies on).
        List<BrokenVesselEntity> alive = serverLevel.getEntitiesOfClass(BrokenVesselEntity.class, lairBounds);
        if (!alive.isEmpty())
        {
            message(serverPlayer, "Broken Vessel already stirs nearby.");
            return InteractionResultHolder.fail(stack);
        }

        BrokenVesselEntity boss = ModEntityTypes.BROKEN_VESSEL.get().create(serverLevel);
        if (boss == null)
        {
            return InteractionResultHolder.fail(stack);
        }

        // No fixed spawn point exists anymore - the tower's actual layout (and so its boss room's real
        // position) varies by which rooms got randomly picked during generation (see VesselLairPlacer).
        // Spawning a few blocks in front of the summoning player, already inside the lair bounds this whole
        // method already gated on, sidesteps needing to know that position at all.
        BlockPos spawn = player.blockPosition().relative(player.getDirection(), 3);
        boss.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), 0.0f);
        serverLevel.addFreshEntity(boss);

        stack.shrink(1);
        message(serverPlayer, ChatFormatting.LIGHT_PURPLE, "The warped shard stirs - Broken Vessel rises again.");
        return InteractionResultHolder.consume(stack);
    }

    private void message(ServerPlayer player, String text)
    {
        message(player, ChatFormatting.YELLOW, text);
    }

    private void message(ServerPlayer player, ChatFormatting color, String text)
    {
        player.displayClientMessage(Component.literal(text).withStyle(color), true);
    }
}
