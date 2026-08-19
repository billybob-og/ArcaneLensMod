package com.arcanelens.block.entity;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.god.GodRegistry;
import com.arcanelens.god.NatureBlocks;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Fertility's dedicated Faith Altar - a passive presence mechanic, distinct from both the base
 * Faith Altar (burn an item) and the Hunger's altar (feed a slot): it periodically counts grown
 * vegetation (grass, flowers, trees) in the area around itself and grants Faith proportional to
 * that count, capped per scan so a dense forest doesn't trivialize the economy. No interaction
 * needed at all beyond placing it - being built somewhere lush is the whole mechanic. */
public class FertilityFaithAltarBlockEntity extends BlockEntity
{
    private int scanTimer;
    @Nullable
    private UUID owner;

    public FertilityFaithAltarBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.FERTILITY_FAITH_ALTAR.get(), pos, state);
    }

    @Nullable
    public UUID getOwner()
    {
        return owner;
    }

    public void setOwner(@Nullable UUID owner)
    {
        this.owner = owner;
        setChanged();
    }

    public void tick(Level level, BlockPos pos)
    {
        if (!(level instanceof ServerLevel serverLevel) || owner == null)
        {
            return;
        }

        // Only works while the owner is actually pledged to Fertility right now - inert (no scan,
        // no Faith) the moment they swap to a different god.
        ServerPlayer ownerPlayer = serverLevel.getServer().getPlayerList().getPlayer(owner);
        if (ownerPlayer == null || !GodRegistry.isPledgedTo(ownerPlayer, "fertility"))
        {
            return;
        }

        scanTimer++;
        if (scanTimer < Config.fertilityScanIntervalTicks)
        {
            return;
        }
        scanTimer = 0;

        int natureBlocks = countNatureBlocks(serverLevel, new AABB(pos).inflate(Config.fertilityScanRadius));
        if (natureBlocks <= 0)
        {
            return;
        }

        int faith = Math.min(natureBlocks, Config.fertilityMaxNatureBlocksCounted) * Config.fertilityFaithPerNatureBlock;
        if (faith <= 0)
        {
            return;
        }

        FaithSync.grantFaithWithFeedback(ownerPlayer, faith, "Florian's Altar");
    }

    /** Grass blocks/short grass/ferns (ground cover), BlockTags.FLOWERS, and BlockTags.LOGS/LEAVES
     * (trees) - matches the "grown grass and flowers plus possible trees" brief exactly. */
    private static int countNatureBlocks(ServerLevel level, AABB box)
    {
        int count = 0;
        BlockPos min = new BlockPos((int) Math.floor(box.minX), (int) Math.floor(box.minY), (int) Math.floor(box.minZ));
        BlockPos max = new BlockPos((int) Math.floor(box.maxX), (int) Math.floor(box.maxY), (int) Math.floor(box.maxZ));
        for (BlockPos check : BlockPos.betweenClosed(min, max))
        {
            BlockState state = level.getBlockState(check);
            if (NatureBlocks.isNatureBlock(state))
            {
                count++;
            }
        }
        return count;
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("ScanTimer", scanTimer);
        if (owner != null)
        {
            tag.putUUID("Owner", owner);
        }
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        scanTimer = tag.getInt("ScanTimer");
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
