package com.arcanelens.network.packet;

import com.arcanelens.capability.FaithProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundSyncFaithPacket
{
    private final int faith;
    private final int sleepingGodDefeats;
    private final int brokenVesselDefeats;
    private final boolean hungerEyesRevealed;
    private final boolean hungerEyesHidden;
    private final int blessingBoonLevel;
    private final int burdenResistBoonLevel;
    private final boolean hasGivenHungerTokens;
    private final boolean hasReceivedHungersPact;
    private final boolean hasClaimedSleepingGodRelic;
    private final boolean hasBeenToldRelicLocation;
    private final String pledgedGod;
    private final int pledgeProgress;

    public ClientboundSyncFaithPacket(int faith, int sleepingGodDefeats, int brokenVesselDefeats, boolean hungerEyesRevealed,
                                       boolean hungerEyesHidden, int blessingBoonLevel, int burdenResistBoonLevel,
                                       boolean hasGivenHungerTokens, boolean hasReceivedHungersPact, boolean hasClaimedSleepingGodRelic,
                                       boolean hasBeenToldRelicLocation, String pledgedGod, int pledgeProgress)
    {
        this.faith = faith;
        this.sleepingGodDefeats = sleepingGodDefeats;
        this.brokenVesselDefeats = brokenVesselDefeats;
        this.hungerEyesRevealed = hungerEyesRevealed;
        this.hungerEyesHidden = hungerEyesHidden;
        this.blessingBoonLevel = blessingBoonLevel;
        this.burdenResistBoonLevel = burdenResistBoonLevel;
        this.hasGivenHungerTokens = hasGivenHungerTokens;
        this.hasReceivedHungersPact = hasReceivedHungersPact;
        this.hasClaimedSleepingGodRelic = hasClaimedSleepingGodRelic;
        this.hasBeenToldRelicLocation = hasBeenToldRelicLocation;
        this.pledgedGod = pledgedGod;
        this.pledgeProgress = pledgeProgress;
    }

    public static void encode(ClientboundSyncFaithPacket packet, FriendlyByteBuf buf)
    {
        buf.writeVarInt(packet.faith);
        buf.writeVarInt(packet.sleepingGodDefeats);
        buf.writeVarInt(packet.brokenVesselDefeats);
        buf.writeBoolean(packet.hungerEyesRevealed);
        buf.writeBoolean(packet.hungerEyesHidden);
        buf.writeVarInt(packet.blessingBoonLevel);
        buf.writeVarInt(packet.burdenResistBoonLevel);
        buf.writeBoolean(packet.hasGivenHungerTokens);
        buf.writeBoolean(packet.hasReceivedHungersPact);
        buf.writeBoolean(packet.hasClaimedSleepingGodRelic);
        buf.writeBoolean(packet.hasBeenToldRelicLocation);
        buf.writeUtf(packet.pledgedGod);
        buf.writeVarInt(packet.pledgeProgress);
    }

    public static ClientboundSyncFaithPacket decode(FriendlyByteBuf buf)
    {
        return new ClientboundSyncFaithPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(),
                buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readUtf(), buf.readVarInt());
    }

    public static void handle(ClientboundSyncFaithPacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null)
            {
                return;
            }
            mc.player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
                cap.setFaith(packet.faith);
                cap.setSleepingGodDefeats(packet.sleepingGodDefeats);
                cap.setBrokenVesselDefeats(packet.brokenVesselDefeats);
                cap.setHungerEyesRevealed(packet.hungerEyesRevealed);
                cap.setHungerEyesHidden(packet.hungerEyesHidden);
                cap.setBlessingBoonLevel(packet.blessingBoonLevel);
                cap.setBurdenResistBoonLevel(packet.burdenResistBoonLevel);
                cap.setHasGivenHungerTokens(packet.hasGivenHungerTokens);
                cap.setHasReceivedHungersPact(packet.hasReceivedHungersPact);
                cap.setHasClaimedSleepingGodRelic(packet.hasClaimedSleepingGodRelic);
                cap.setHasBeenToldRelicLocation(packet.hasBeenToldRelicLocation);
                // setPledgedGod resets pledgeProgress to 0 as a side effect (see FaithImpl) - the
                // explicit setPledgeProgress call below MUST come after it, so the synced value wins
                // over that reset rather than the client mirror silently losing its real progress on
                // every ordinary sync (which fires far more often than the pledge itself changes).
                cap.setPledgedGod(packet.pledgedGod);
                cap.setPledgeProgress(packet.pledgeProgress);
            });
        });
        ctx.setPacketHandled(true);
    }
}
