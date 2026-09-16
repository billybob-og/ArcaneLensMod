package com.arcanelens.block;

import com.arcanelens.block.entity.GodChallengeAltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/** One placed per god's arena entrance in the God Challenge Hub - see GodChallengeAltarBlockEntity for
 * why which god it challenges is fixed per-instance rather than chosen from a menu. */
public class GodChallengeAltarBlock extends Block implements EntityBlock
{
    public GodChallengeAltarBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!level.isClientSide)
        {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GodChallengeAltarBlockEntity altar && player instanceof ServerPlayer serverPlayer)
            {
                if (altar.getGodId().isEmpty())
                {
                    serverPlayer.displayClientMessage(Component.literal("This challenge isn't ready yet."), true);
                    return InteractionResult.sidedSuccess(false);
                }
                NetworkHooks.openScreen(serverPlayer, altar, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new GodChallengeAltarBlockEntity(pos, state);
    }
}
