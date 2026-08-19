package com.arcanelens.item;

import com.arcanelens.capability.FaithProvider;
import com.arcanelens.capability.FaithSync;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Right-click to consume (mirrors WeirdAmuletItem's use() pattern) - permanently increases blessingBoonLevel,
 * which extends blessing effect duration in TheHungerHandler. */
public class TheHungersBoonItem extends Item
{
    public TheHungersBoonItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer)
        {
            serverPlayer.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> cap.incrementBlessingBoonLevel());
            FaithSync.syncToClient(serverPlayer);
            stack.shrink(1);
            serverPlayer.displayClientMessage(
                    Component.literal("You feel The Hunger's favor take deeper root.").withStyle(ChatFormatting.GOLD), true);
            serverPlayer.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.0F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
