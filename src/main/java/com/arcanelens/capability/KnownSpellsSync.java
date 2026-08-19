package com.arcanelens.capability;

import com.arcanelens.network.NetworkHandler;
import com.arcanelens.network.packet.ClientboundSyncKnownSpellsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public class KnownSpellsSync
{
    public static void syncToClient(ServerPlayer player)
    {
        player.getCapability(KnownSpellsProvider.CAPABILITY).ifPresent(cap -> {
            List<String> ids = cap.getKnownSpells().stream().map(ResourceLocation::toString).toList();
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundSyncKnownSpellsPacket(ids));
        });
    }
}
