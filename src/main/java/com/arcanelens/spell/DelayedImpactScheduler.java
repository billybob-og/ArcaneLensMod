package com.arcanelens.spell;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.List;

/** Lets a spell (or a boss attack) schedule a fireball-style impact to land some ticks in the future, for attacks that play out as a barrage over time rather than resolving all at once. */
public class DelayedImpactScheduler
{
    private static final List<PendingImpact> PENDING = new ArrayList<>();

    public static void schedule(ServerLevel level, Vec3 pos, LivingEntity caster, float damage, double radius, long executeAtTick)
    {
        PENDING.add(new PendingImpact(level, pos, caster, damage, radius, executeAtTick));
    }

    public static void onServerTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty())
        {
            return;
        }

        List<PendingImpact> ready = new ArrayList<>();
        for (PendingImpact impact : PENDING)
        {
            if (impact.level().getGameTime() >= impact.executeAtTick())
            {
                ready.add(impact);
            }
        }

        PENDING.removeAll(ready);
        for (PendingImpact impact : ready)
        {
            detonate(impact);
        }
    }

    private static void detonate(PendingImpact impact)
    {
        ServerLevel level = impact.level();
        Vec3 pos = impact.pos();
        LivingEntity caster = impact.caster();

        level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 30, 0.5, 0.5, 0.5, 0.02);
        level.sendParticles(ParticleTypes.LAVA, pos.x, pos.y, pos.z, 8, 0.4, 0.4, 0.4, 0.0);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.8f);

        AABB area = new AABB(pos, pos).inflate(impact.radius());
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != caster))
        {
            target.hurt(level.damageSources().indirectMagic(caster, caster), impact.damage());
            target.setSecondsOnFire(3);
        }
    }

    private record PendingImpact(ServerLevel level, Vec3 pos, LivingEntity caster, float damage, double radius, long executeAtTick) {}
}
