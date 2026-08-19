package com.arcanelens.block;

import com.arcanelens.block.entity.TheaterFaithAltarBlockEntity;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Unlike the other three dedicated altars, this one has a real interaction - right-click with a
 * Written Book for the Spectacle sub-case (see TheaterFaithAltarBlockEntity.grantSpectacleBookFaith).
 * Everything else (Deception/Disguise/Drama/the Jukebox half of Spectacle) is scanned passively. */
public class TheaterFaithAltarBlock extends Block implements EntityBlock
{
    public TheaterFaithAltarBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.WRITTEN_BOOK))
        {
            return InteractionResult.PASS;
        }
        if (level.isClientSide)
        {
            return InteractionResult.sidedSuccess(true);
        }

        if (level.getBlockEntity(pos) instanceof TheaterFaithAltarBlockEntity altar && level instanceof ServerLevel serverLevel)
        {
            altar.grantSpectacleBookFaith(serverLevel, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer != null && level.getBlockEntity(pos) instanceof TheaterFaithAltarBlockEntity altar)
        {
            altar.setOwner(placer.getUUID());
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
    {
        if (random.nextInt(6) == 0)
        {
            level.addParticle(ParticleTypes.WITCH,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    0.0, 0.02, 0.0);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TheaterFaithAltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (level.isClientSide || type != ModBlockEntities.THEATER_FAITH_ALTAR.get())
        {
            return null;
        }
        return (lvl, pos, st, be) ->
        {
            if (be instanceof TheaterFaithAltarBlockEntity altar)
            {
                altar.tick(lvl, pos);
            }
        };
    }
}
