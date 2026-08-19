package com.arcanelens.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * A brief, wordless apparition - The Hunger itself, visible to everyone nearby for a few seconds whenever a
 * player's blessing/burden moment fires (see TheHungerHandler). No AI, no attributes, no collision - it just
 * stands (briefly animating a "step" into view, then holding) and vanishes on its own.
 *
 * A permanent variant (see HungerIdolEntity, the resident at the top of the Hunger Tower) skips the
 * lifespan/discard entirely via the protected {@code expires} constructor param below - everything else
 * about this class (no AI/attributes/collision, the intro-then-hold animation) is shared as-is.
 */
public class TheHungerEntity extends Entity
{
    private static final int LIFESPAN_TICKS = 60;

    public final AnimationState walkAnimationState = new AnimationState();
    private final boolean expires;
    private int ticksAlive;

    public TheHungerEntity(EntityType<? extends TheHungerEntity> entityType, Level level)
    {
        this(entityType, level, true);
    }

    protected TheHungerEntity(EntityType<? extends TheHungerEntity> entityType, Level level, boolean expires)
    {
        super(entityType, level);
        this.noPhysics = true;
        this.setInvulnerable(true);
        this.expires = expires;
    }

    @Override
    public void tick()
    {
        super.tick();
        if (this.level().isClientSide)
        {
            if (!this.walkAnimationState.isStarted())
            {
                this.walkAnimationState.start(this.tickCount);
            }
            return;
        }

        if (!this.expires)
        {
            return;
        }

        this.ticksAlive++;
        if (this.ticksAlive >= LIFESPAN_TICKS)
        {
            this.discard();
        }
    }

    @Override
    public boolean isPickable()
    {
        return false;
    }

    @Override
    public boolean isPushable()
    {
        return false;
    }

    @Override
    protected void defineSynchedData()
    {
        // No synced state needed - the model has no dynamic parameters beyond its own fixed intro animation.
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        // No custom state worth saving - the brief apparition is .noSave() anyway, and the permanent idol
        // variant has nothing beyond its position (handled automatically for any saved entity).
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        // See readAdditionalSaveData - nothing to write.
    }
}
