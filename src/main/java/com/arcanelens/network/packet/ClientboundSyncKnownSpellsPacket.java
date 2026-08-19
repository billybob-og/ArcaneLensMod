package com.arcanelens.network.packet;

import com.arcanelens.capability.KnownSpellsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ClientboundSyncKnownSpellsPacket
{
    private final List<String> knownSpells;

    public ClientboundSyncKnownSpellsPacket(List<String> knownSpells)
    {
        this.knownSpells = knownSpells;
    }

    public static void encode(ClientboundSyncKnownSpellsPacket packet, FriendlyByteBuf buf)
    {
        buf.writeCollection(packet.knownSpells, FriendlyByteBuf::writeUtf);
    }

    public static ClientboundSyncKnownSpellsPacket decode(FriendlyByteBuf buf)
    {
        return new ClientboundSyncKnownSpellsPacket(buf.readList(FriendlyByteBuf::readUtf));
    }

    public static void handle(ClientboundSyncKnownSpellsPacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null)
            {
                return;
            }
            mc.player.getCapability(KnownSpellsProvider.CAPABILITY).ifPresent(cap -> {
                Set<ResourceLocation> known = cap.getKnownSpells();
                known.clear();
                for (String id : packet.knownSpells)
                {
                    known.add(new ResourceLocation(id));
                }
            });
        });
        ctx.setPacketHandled(true);
    }
}
