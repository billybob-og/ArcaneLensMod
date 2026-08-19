package com.arcanelens.block;

import com.arcanelens.Config;
import com.arcanelens.advancement.ModCriteriaTriggers;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.capability.FaithSync;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The hand-placed fixture inside the Sleeping God Relic Shrine (see RelicShrinePlacer). No BlockEntity -
 * all state lives in the player's own IFaith capability (hasClaimedSleepingGodRelic), same as how
 * OverloadCoreBlock reads the storage network without joining it: this block reads/writes capability state
 * without needing any of its own.
 */
public class SleepingGodRelicBlock extends Block
{
    public SleepingGodRelicBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer))
        {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        serverPlayer.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.hasClaimedSleepingGodRelic())
            {
                message(serverPlayer, ChatFormatting.DARK_GRAY, "It has nothing left to give.");
                return;
            }

            cap.addFaith(Config.sleepingGodRelicFaithReward);
            cap.setHasClaimedSleepingGodRelic(true);
            FaithSync.syncToClient(serverPlayer);
            ModCriteriaTriggers.SLEEPING_GOD_RELIC_FOUND.trigger(serverPlayer);
            message(serverPlayer, ChatFormatting.LIGHT_PURPLE,
                    "The relic gives up what little warmth it has left. Something of the god who slept here passes into you.");
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, 0.8F);
        });

        return InteractionResult.CONSUME;
    }

    private void message(ServerPlayer player, ChatFormatting color, String text)
    {
        player.displayClientMessage(Component.literal(text).withStyle(color), true);
    }
}
