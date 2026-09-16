package com.arcanelens.entity.boss;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.god.GodDefinition;
import com.arcanelens.god.GodRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.level.Level;

/** Shared base for God Challenge Hub boss fights - the generic parts every such fight needs (a synced
 * "current action" state for animation, a boss bar, and the granted/broken drop-or-Faith death payout),
 * factored out so each god's boss subclass (AureliaBossEntity, etc.) only has to define its own combat
 * specifics on top. Deliberately independent of SleepingGodEntity/BrokenVesselEntity - see the God Boss
 * Hub plan's hard constraint that those stay untouched - though DATA_ACTION/DATA_ACTION_END_TICK follow
 * the same synced "when does this animation end" shape Sleeping God's own DATA_ACTION_END_TICK uses,
 * since that pattern is proven to work for phase-driven boss animation.
 *
 * <p>Action is stored as a plain synced int ordinal, not a shared enum - each concrete boss has its own
 * moveset (Aurelia's spear thrust + charge won't look like the next god's kit), so the MEANING of a
 * given ordinal is left to each subclass's own enum wrapping getActionOrdinal()/setAction(), while this
 * class only owns the sync/expiry plumbing.</p> */
public abstract class AbstractGodBossEntity extends Monster
{
    private static final EntityDataAccessor<Integer> DATA_ACTION =
            SynchedEntityData.defineId(AbstractGodBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION_END_TICK =
            SynchedEntityData.defineId(AbstractGodBossEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent;

    protected AbstractGodBossEntity(EntityType<? extends Monster> type, Level level,
                                     Component bossBarName, BossEvent.BossBarColor color)
    {
        super(type, level);
        this.bossEvent = new ServerBossEvent(bossBarName, color, BossEvent.BossBarOverlay.PROGRESS);
    }

    /** The GodRegistry id this boss belongs to (e.g. "war" for Aurelia) - used to look up the
     * GodDefinition for its unique drop and to key the per-player granted/broken counters. */
    protected abstract String getGodId();

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_ACTION, 0);
        this.entityData.define(DATA_ACTION_END_TICK, 0);
    }

    public int getActionOrdinal()
    {
        return this.entityData.get(DATA_ACTION);
    }

    public int getActionEndTick()
    {
        return this.entityData.get(DATA_ACTION_END_TICK);
    }

    /** Ordinal 0 is reserved for "no action" by convention (every subclass's own Action enum should
     * put NONE first) - customServerAiStep automatically clears back to it once durationTicks elapses. */
    protected void setAction(int actionOrdinal, int durationTicks)
    {
        this.entityData.set(DATA_ACTION, actionOrdinal);
        this.entityData.set(DATA_ACTION_END_TICK, this.tickCount + durationTicks);
    }

    @Override
    public void customServerAiStep()
    {
        super.customServerAiStep();
        if (getActionOrdinal() != 0 && this.tickCount >= getActionEndTick())
        {
            this.entityData.set(DATA_ACTION, 0);
        }
        this.bossEvent.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0.0F, 1.0F));
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player)
    {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player)
    {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void die(DamageSource damageSource)
    {
        super.die(damageSource);
        bossEvent.removeAllPlayers();
        if (damageSource.getEntity() instanceof ServerPlayer player)
        {
            grantDropOrFaith(player);
        }
    }

    private void grantDropOrFaith(ServerPlayer player)
    {
        String godId = getGodId();
        GodDefinition god = GodRegistry.GODS.stream().filter(g -> g.id().equals(godId)).findFirst().orElse(null);
        if (god == null || !god.hasBossContent())
        {
            return;
        }

        player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.getGodDropsBroken(godId) >= cap.getGodDropsGranted(godId))
            {
                ItemStack drop = new ItemStack(god.uniqueDrop().get().get());
                if (!player.getInventory().add(drop))
                {
                    player.drop(drop, false);
                }
                cap.incrementGodDropsGranted(godId);
            }
            else
            {
                cap.addFaith(Config.godBossRepeatKillFaithReward);
            }
            FaithSync.syncToClient(player);
        });
    }
}
