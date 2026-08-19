package com.arcanelens.capability;

import com.arcanelens.network.NetworkHandler;
import com.arcanelens.network.packet.ClientboundSyncSkillTreePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class SkillTreeSync
{
    public static void syncToClient(ServerPlayer player)
    {
        player.getCapability(SkillTreeProvider.CAPABILITY).ifPresent(cap ->
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new ClientboundSyncSkillTreePacket(cap.getManaBoostLevel(), cap.getCooldownReductionLevel(),
                                cap.getCostReductionLevel(), cap.isPocketDimensionUnlocked(),
                                cap.getPocketDimensionExpansionLevel(), cap.isStorageSystemUnlocked(),
                                cap.getStorageNetworkExpansionLevel(), cap.isArcaneAssemblerUnlocked(),
                                cap.getAssemblerSpeedLevel(), cap.getAssemblerFuelEfficiencyLevel(),
                                cap.isOverloadRitualUnlocked(), cap.isWarpedAttunementUnlocked())));
    }
}
