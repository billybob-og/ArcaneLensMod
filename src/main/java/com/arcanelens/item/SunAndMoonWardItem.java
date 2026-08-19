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

/** Right-click toggles hungerEyesHidden - a reversible override that reverts the revealed sun/moon eye
 * textures (see TheHungerSkyRenderer) back to normal, unlike the one-way hungerEyesRevealed reveal itself. */
public class SunAndMoonWardItem extends Item
{
    public SunAndMoonWardItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer)
        {
            serverPlayer.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
                boolean nowHidden = !cap.isHungerEyesHidden();
                cap.setHungerEyesHidden(nowHidden);
                FaithSync.syncToClient(serverPlayer);
                serverPlayer.displayClientMessage(Component.literal(nowHidden
                        ? "The sky settles back into a familiar shape." : "The sun and moon reveal their true watching face once more.")
                        .withStyle(ChatFormatting.GRAY), true);
                serverPlayer.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6F, 1.0F);
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
