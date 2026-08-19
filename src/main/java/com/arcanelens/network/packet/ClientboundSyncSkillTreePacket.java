package com.arcanelens.network.packet;

import com.arcanelens.capability.SkillTreeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundSyncSkillTreePacket
{
    private final int manaBoostLevel;
    private final int cooldownReductionLevel;
    private final int costReductionLevel;
    private final boolean pocketDimensionUnlocked;
    private final int pocketDimensionExpansionLevel;
    private final boolean storageSystemUnlocked;
    private final int storageNetworkExpansionLevel;
    private final boolean arcaneAssemblerUnlocked;
    private final int assemblerSpeedLevel;
    private final int assemblerFuelEfficiencyLevel;
    private final boolean overloadRitualUnlocked;
    private final boolean warpedAttunementUnlocked;

    public ClientboundSyncSkillTreePacket(int manaBoostLevel, int cooldownReductionLevel, int costReductionLevel,
                                           boolean pocketDimensionUnlocked, int pocketDimensionExpansionLevel,
                                           boolean storageSystemUnlocked, int storageNetworkExpansionLevel,
                                           boolean arcaneAssemblerUnlocked, int assemblerSpeedLevel,
                                           int assemblerFuelEfficiencyLevel, boolean overloadRitualUnlocked,
                                           boolean warpedAttunementUnlocked)
    {
        this.manaBoostLevel = manaBoostLevel;
        this.cooldownReductionLevel = cooldownReductionLevel;
        this.costReductionLevel = costReductionLevel;
        this.pocketDimensionUnlocked = pocketDimensionUnlocked;
        this.pocketDimensionExpansionLevel = pocketDimensionExpansionLevel;
        this.storageSystemUnlocked = storageSystemUnlocked;
        this.storageNetworkExpansionLevel = storageNetworkExpansionLevel;
        this.arcaneAssemblerUnlocked = arcaneAssemblerUnlocked;
        this.assemblerSpeedLevel = assemblerSpeedLevel;
        this.assemblerFuelEfficiencyLevel = assemblerFuelEfficiencyLevel;
        this.overloadRitualUnlocked = overloadRitualUnlocked;
        this.warpedAttunementUnlocked = warpedAttunementUnlocked;
    }

    public static void encode(ClientboundSyncSkillTreePacket packet, FriendlyByteBuf buf)
    {
        buf.writeVarInt(packet.manaBoostLevel);
        buf.writeVarInt(packet.cooldownReductionLevel);
        buf.writeVarInt(packet.costReductionLevel);
        buf.writeBoolean(packet.pocketDimensionUnlocked);
        buf.writeVarInt(packet.pocketDimensionExpansionLevel);
        buf.writeBoolean(packet.storageSystemUnlocked);
        buf.writeVarInt(packet.storageNetworkExpansionLevel);
        buf.writeBoolean(packet.arcaneAssemblerUnlocked);
        buf.writeVarInt(packet.assemblerSpeedLevel);
        buf.writeVarInt(packet.assemblerFuelEfficiencyLevel);
        buf.writeBoolean(packet.overloadRitualUnlocked);
        buf.writeBoolean(packet.warpedAttunementUnlocked);
    }

    public static ClientboundSyncSkillTreePacket decode(FriendlyByteBuf buf)
    {
        return new ClientboundSyncSkillTreePacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readBoolean(), buf.readVarInt(), buf.readBoolean(), buf.readVarInt(), buf.readBoolean(),
                buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(ClientboundSyncSkillTreePacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null)
            {
                return;
            }
            mc.player.getCapability(SkillTreeProvider.CAPABILITY).ifPresent(cap -> {
                cap.setManaBoostLevel(packet.manaBoostLevel);
                cap.setCooldownReductionLevel(packet.cooldownReductionLevel);
                cap.setCostReductionLevel(packet.costReductionLevel);
                cap.setPocketDimensionUnlocked(packet.pocketDimensionUnlocked);
                cap.setPocketDimensionExpansionLevel(packet.pocketDimensionExpansionLevel);
                cap.setStorageSystemUnlocked(packet.storageSystemUnlocked);
                cap.setStorageNetworkExpansionLevel(packet.storageNetworkExpansionLevel);
                cap.setArcaneAssemblerUnlocked(packet.arcaneAssemblerUnlocked);
                cap.setAssemblerSpeedLevel(packet.assemblerSpeedLevel);
                cap.setAssemblerFuelEfficiencyLevel(packet.assemblerFuelEfficiencyLevel);
                cap.setOverloadRitualUnlocked(packet.overloadRitualUnlocked);
                cap.setWarpedAttunementUnlocked(packet.warpedAttunementUnlocked);
            });
        });
        ctx.setPacketHandled(true);
    }
}
