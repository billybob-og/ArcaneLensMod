package com.arcanelens.item;

import com.arcanelens.entity.FlorianCompanionEntity;
import com.arcanelens.registry.ModEntityTypes;
import com.arcanelens.spell.SummonExpiryHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Florian's boss drop - right-click to call a debuffed Florian (see FlorianCompanionEntity) to fight
 * beside you for a short while. Reusable rather than consumed, since it's a rare, loss-protected god
 * drop; the cooldown is longer than the summon lasts so two can never overlap. */
public class FloriansAntlerItem extends Item
{
    private static final int DURATION_TICKS = 20 * 60;
    private static final int COOLDOWN_TICKS = 20 * 60 * 5;

    public FloriansAntlerItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel))
        {
            return InteractionResultHolder.pass(stack);
        }

        FlorianCompanionEntity companion = ModEntityTypes.FLORIAN_COMPANION.get().create(serverLevel);
        if (companion == null)
        {
            return InteractionResultHolder.fail(stack);
        }

        companion.moveTo(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.getYRot(), 0.0F);
        companion.tame(serverPlayer);
        companion.setOrderedToSit(false);
        companion.getPersistentData().putLong(SummonExpiryHandler.EXPIRY_TAG, serverLevel.getGameTime() + DURATION_TICKS);
        serverLevel.addFreshEntity(companion);

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, serverPlayer.getX(), serverPlayer.getY() + 1, serverPlayer.getZ(), 16, 0.5, 0.5, 0.5, 0.0);
        serverLevel.playSound(null, serverPlayer.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 0.8f);
        serverPlayer.displayClientMessage(Component.literal("Florian answers.").withStyle(ChatFormatting.GREEN), true);
        return InteractionResultHolder.success(stack);
    }
}
