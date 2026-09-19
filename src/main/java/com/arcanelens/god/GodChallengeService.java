package com.arcanelens.god;

import com.arcanelens.worldgen.ModDimensions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/** Teleports a validated challenger into their target god's arena (a fixed spot within the God
 * Challenge Hub - see GodDefinition.arenaOrigin) and summons that god's boss there, and lets players back
 * out again afterward. Arena placement itself is GodArenaPlacer's job. */
public class GodChallengeService
{
    /** The hub's fixed arrival point - also where players are sent back to when a boss falls, since the
     * arenas are sealed and there's otherwise no way out without flight. */
    public static final Vec3 HUB_SPAWN = new Vec3(0.5, 9.0, 0.5);

    // Roughly an arena's footprint (14x14) plus margin - who counts as "in the fight".
    private static final double ARENA_RADIUS = 24.0;

    // The boss appears this many blocks along +Z from where the challenger arrives, facing back toward
    // them - so the fight starts with a gap instead of both spawning in the same block.
    private static final double BOSS_SPAWN_OFFSET_Z = 4.0;

    public static void beginChallenge(ServerPlayer player, GodDefinition god)
    {
        MinecraftServer server = player.getServer();
        ServerLevel hubLevel = server == null ? null : server.getLevel(ModDimensions.GOD_CHALLENGE_HUB_KEY);
        if (hubLevel == null || !god.hasBossContent())
        {
            return;
        }

        var origin = god.arenaOrigin();
        player.teleportTo(hubLevel, origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5,
                Set.of(), player.getYRot(), player.getXRot());

        EntityType<? extends Mob> bossType = god.bossEntityType().get().get();
        Mob boss = bossType.create(hubLevel);
        if (boss != null)
        {
            // Yaw 180 faces -Z, i.e. toward the challenger who arrived at the -Z side of the boss.
            boss.moveTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5 + BOSS_SPAWN_OFFSET_Z, 180.0F, 0.0F);
            hubLevel.addFreshEntity(boss);
        }
    }

    /** Sends every player near the given point back to the hub's arrival point. */
    public static void returnPlayersToHub(ServerLevel level, Vec3 nearPoint)
    {
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(nearPoint, nearPoint).inflate(ARENA_RADIUS)))
        {
            player.teleportTo(level, HUB_SPAWN.x, HUB_SPAWN.y, HUB_SPAWN.z, Set.of(), player.getYRot(), player.getXRot());
        }
    }
}
