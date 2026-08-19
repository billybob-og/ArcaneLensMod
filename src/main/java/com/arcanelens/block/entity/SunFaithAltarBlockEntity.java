package com.arcanelens.block.entity;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.god.GodRegistry;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Sun's dedicated Faith Altar - the simplest of the four mechanics, and distinct from all of them:
 * it just reads its own current light level (LevelReader.getMaxLocalRawBrightness, the same
 * day/night-adjusted combined sky+block light value vanilla uses for mob-spawn checks) each periodic
 * check and grants Faith proportional to it. No scanning, no event tracking - naturally high by day,
 * naturally low at night unless supplemented with nearby torches/lanterns, matching "the sun and
 * nearby light" as one unified factor exactly the way Minecraft's own lighting already combines them. */
public class SunFaithAltarBlockEntity extends BlockEntity
{
    private int scanTimer;
    @Nullable
    private UUID owner;

    public SunFaithAltarBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.SUN_FAITH_ALTAR.get(), pos, state);
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

        // Only works while the owner is actually pledged to the Sun right now - inert the moment
        // they swap to a different god.
        ServerPlayer ownerPlayer = serverLevel.getServer().getPlayerList().getPlayer(owner);
        if (ownerPlayer == null || !GodRegistry.isPledgedTo(ownerPlayer, "sun"))
        {
            return;
        }

        scanTimer++;
        if (scanTimer < Config.sunScanIntervalTicks)
        {
            return;
        }
        scanTimer = 0;

        int lightLevel = level.getMaxLocalRawBrightness(pos);
        int faith = lightLevel * Config.sunFaithPerLightLevel;
        if (faith <= 0)
        {
            return;
        }

        FaithSync.grantFaithWithFeedback(ownerPlayer, faith, "Quetzera's Altar");
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
