package com.arcanelens.spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Backs ArrowVolleySpell: casting it doesn't fire anything itself, it just marks the caster so the very next
 * arrow they shoot (from a bow, crossbow, whatever) gets duplicated into extra arrows arranged in a circular
 * cone around the original shot, like a one-time-use Multishot. The mark expires if they don't actually fire in time.
 */
public class ArrowVolleyHandler
{
    private static final Map<UUID, Pending> PENDING = new HashMap<>();
    private static final double CONE_ANGLE_DEGREES = 7.0;

    public static void markPending(ServerPlayer player, int extraArrows, long expiryTick)
    {
        PENDING.put(player.getUUID(), new Pending(extraArrows, expiryTick));
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event)
    {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof AbstractArrow arrow))
        {
            return;
        }
        if (!(arrow.getOwner() instanceof ServerPlayer player))
        {
            return;
        }

        Pending pending = PENDING.remove(player.getUUID());
        if (pending == null || player.level().getGameTime() > pending.expiryTick())
        {
            return;
        }

        ServerLevel level = (ServerLevel) arrow.level();
        Vec3 origin = arrow.position();
        Vec3 velocity = arrow.getDeltaMovement();
        double speed = velocity.length();
        Vec3 forward = velocity.normalize();

        // Perpendicular basis around the shot direction, so the extra arrows can be spaced evenly around
        // a circle instead of just fanned left-right in the horizontal plane.
        Vec3 arbitraryUp = Math.abs(forward.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = forward.cross(arbitraryUp).normalize();
        Vec3 up = right.cross(forward).normalize();
        double coneAngle = Math.toRadians(CONE_ANGLE_DEGREES);

        int extraArrows = pending.extraArrows();
        for (int i = 0; i < extraArrows; i++)
        {
            double theta = 2 * Math.PI * i / extraArrows;
            Vec3 radial = right.scale(Math.cos(theta)).add(up.scale(Math.sin(theta)));
            Vec3 direction = forward.scale(Math.cos(coneAngle)).add(radial.scale(Math.sin(coneAngle)));

            Arrow extra = new Arrow(level, player);
            extra.setPos(origin.x, origin.y, origin.z);
            extra.setDeltaMovement(direction.scale(speed));
            extra.pickup = arrow.pickup;
            level.addFreshEntity(extra);
        }
    }

    private record Pending(int extraArrows, long expiryTick) {}
}
