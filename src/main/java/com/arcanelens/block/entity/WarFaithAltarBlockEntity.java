package com.arcanelens.block.entity;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.god.GodRegistry;
import com.arcanelens.god.WarDeathTracker;
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

/** War's dedicated Faith Altar - a passive area-watch mechanic, distinct from Hunger's (feed a slot)
 * and Fertility's (scan static blocks): it periodically checks WarDeathTracker for anything that died
 * nearby since its last check and grants Faith per death. No interaction needed beyond placing it. */
public class WarFaithAltarBlockEntity extends BlockEntity
{
    private int scanTimer;
    private long lastScanTick;
    @Nullable
    private UUID owner;

    public WarFaithAltarBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.WAR_FAITH_ALTAR.get(), pos, state);
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

        // Only works while the owner is actually pledged to War right now - inert the moment they
        // swap to a different god. WarDeathTracker self-prunes after MAX_AGE_TICKS regardless, so
        // leaving lastScanTick stale here can't build up an unbounded backlog once re-pledged.
        ServerPlayer ownerPlayer = serverLevel.getServer().getPlayerList().getPlayer(owner);
        if (ownerPlayer == null || !GodRegistry.isPledgedTo(ownerPlayer, "war"))
        {
            return;
        }

        scanTimer++;
        if (scanTimer < Config.warScanIntervalTicks)
        {
            return;
        }
        scanTimer = 0;

        int deaths = WarDeathTracker.countNearbyDeathsSince(serverLevel, pos, Config.warScanRadius, lastScanTick);
        lastScanTick = serverLevel.getGameTime();
        setChanged();
        if (deaths <= 0)
        {
            return;
        }

        int faith = deaths * Config.warFaithPerNearbyDeath;
        FaithSync.grantFaithWithFeedback(ownerPlayer, faith, "Aurelia's Altar");
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("ScanTimer", scanTimer);
        tag.putLong("LastScanTick", lastScanTick);
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
        lastScanTick = tag.getLong("LastScanTick");
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
