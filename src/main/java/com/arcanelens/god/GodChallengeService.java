package com.arcanelens.god;

import com.arcanelens.worldgen.ModDimensions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.Set;

/** Teleports a validated challenger into their target god's arena (a fixed spot within the God
 * Challenge Hub - see GodDefinition.arenaOrigin) and summons that god's boss there. Arena-structure
 * placement (GodArenaPlacer/GodArenaSavedData, per the God Boss Hub plan) isn't wired in here yet -
 * every god's arenaStructureId is still null in Phase A, so there's nothing for a placer to paste yet;
 * this only needs to actually call it once Phase B gives some god a real structure to place. */
public class GodChallengeService
{
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
            boss.moveTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5, player.getYRot(), 0.0F);
            hubLevel.addFreshEntity(boss);
        }
    }
}
