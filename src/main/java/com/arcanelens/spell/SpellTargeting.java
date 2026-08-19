package com.arcanelens.spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Shared look-direction targeting used by the instant-effect spells. */
public class SpellTargeting
{
    @Nullable
    public static LivingEntity getTargetEntity(ServerPlayer caster, double range)
    {
        Vec3 eye = caster.getEyePosition(1.0f);
        Vec3 look = caster.getViewVector(1.0f);
        Vec3 end = eye.add(look.scale(range));
        AABB searchBox = caster.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(caster, eye, end, searchBox,
                e -> e instanceof LivingEntity && e.isAlive() && e != caster, range * range);

        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }

    public static Vec3 getImpactPoint(ServerPlayer caster, double range)
    {
        ServerLevel level = (ServerLevel) caster.level();
        Vec3 eye = caster.getEyePosition(1.0f);
        Vec3 look = caster.getViewVector(1.0f);
        Vec3 end = eye.add(look.scale(range));

        LivingEntity target = getTargetEntity(caster, range);
        if (target != null)
        {
            return target.position().add(0, target.getBbHeight() / 2.0, 0);
        }

        HitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation() : end;
    }
}
