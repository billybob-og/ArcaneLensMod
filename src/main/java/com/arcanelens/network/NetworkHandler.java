package com.arcanelens.network;

import com.arcanelens.ArcaneLens;
import com.arcanelens.network.packet.ClientboundSyncFaithPacket;
import com.arcanelens.network.packet.ClientboundSyncKnownSpellsPacket;
import com.arcanelens.network.packet.ClientboundSyncSkillTreePacket;
import com.arcanelens.network.packet.ServerboundCastSpellPacket;
import com.arcanelens.network.packet.ServerboundChooseHungerBoonPacket;
import com.arcanelens.network.packet.ServerboundChallengeGodPacket;
import com.arcanelens.network.packet.ServerboundChoosePledgePacket;
import com.arcanelens.network.packet.ServerboundCombineLensesPacket;
import com.arcanelens.network.packet.ServerboundCycleSpellPacket;
import com.arcanelens.network.packet.ServerboundPurchaseSkillPacket;
import com.arcanelens.network.packet.ServerboundScrollTerminalPacket;
import com.arcanelens.network.packet.ServerboundSetCommandTriggerConfigPacket;
import com.arcanelens.network.packet.ServerboundSetSpellSlotPacket;
import com.arcanelens.network.packet.ServerboundSetTerminalSearchPacket;
import com.arcanelens.network.packet.ServerboundSetTerminalSortPacket;
import com.arcanelens.network.packet.ServerboundTheaterAbilityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler
{
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ArcaneLens.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register()
    {
        CHANNEL.registerMessage(packetId++, ServerboundSetSpellSlotPacket.class,
                ServerboundSetSpellSlotPacket::encode,
                ServerboundSetSpellSlotPacket::decode,
                ServerboundSetSpellSlotPacket::handle);

        CHANNEL.registerMessage(packetId++, ServerboundCycleSpellPacket.class,
                ServerboundCycleSpellPacket::encode,
                ServerboundCycleSpellPacket::decode,
                ServerboundCycleSpellPacket::handle);

        CHANNEL.registerMessage(packetId++, ServerboundCastSpellPacket.class,
                ServerboundCastSpellPacket::encode,
                ServerboundCastSpellPacket::decode,
                ServerboundCastSpellPacket::handle);

        CHANNEL.registerMessage(packetId++, ClientboundSyncKnownSpellsPacket.class,
                ClientboundSyncKnownSpellsPacket::encode,
                ClientboundSyncKnownSpellsPacket::decode,
                ClientboundSyncKnownSpellsPacket::handle);

        CHANNEL.registerMessage(packetId++, ServerboundCombineLensesPacket.class,
                ServerboundCombineLensesPacket::encode,
                ServerboundCombineLensesPacket::decode,
                ServerboundCombineLensesPacket::handle);

        CHANNEL.registerMessage(packetId++, ClientboundSyncFaithPacket.class,
                ClientboundSyncFaithPacket::encode,
                ClientboundSyncFaithPacket::decode,
                ClientboundSyncFaithPacket::handle);

        CHANNEL.registerMessage(packetId++, ServerboundSetCommandTriggerConfigPacket.class,
                ServerboundSetCommandTriggerConfigPacket::encode,
                ServerboundSetCommandTriggerConfigPacket::decode,
                ServerboundSetCommandTriggerConfigPacket::handle);

        CHANNEL.registerMessage(packetId++, ServerboundChooseHungerBoonPacket.class,
                ServerboundChooseHungerBoonPacket::encode,
                ServerboundChooseHungerBoonPacket::decode,
                ServerboundChooseHungerBoonPacket::handle);

        CHANNEL.registerMessage(packetId++, ClientboundSyncSkillTreePacket.class,
                ClientboundSyncSkillTreePacket::encode,
                ClientboundSyncSkillTreePacket::decode,
                ClientboundSyncSkillTreePacket::handle);

        CHANNEL.registerMessage(packetId++, ServerboundPurchaseSkillPacket.class,
                ServerboundPurchaseSkillPacket::encode,
                ServerboundPurchaseSkillPacket::decode,
                ServerboundPurchaseSkillPacket::handle);

        CHANNEL.registerMessage(packetId++, ServerboundScrollTerminalPacket.class,
                ServerboundScrollTerminalPacket::encode,
                ServerboundScrollTerminalPacket::decode,
                ServerboundScrollTerminalPacket::handle);

        CHANNEL.registerMessage(packetId++, ServerboundSetTerminalSearchPacket.class,
                ServerboundSetTerminalSearchPacket::encode,
                ServerboundSetTerminalSearchPacket::decode,
                ServerboundSetTerminalSearchPacket::handle);

        CHANNEL.registerMessage(packetId++, ServerboundSetTerminalSortPacket.class,
                ServerboundSetTerminalSortPacket::encode,
                ServerboundSetTerminalSortPacket::decode,
                ServerboundSetTerminalSortPacket::handle);

        CHANNEL.registerMessage(packetId++, ServerboundChoosePledgePacket.class,
                ServerboundChoosePledgePacket::encode,
                ServerboundChoosePledgePacket::decode,
                ServerboundChoosePledgePacket::handle);

        CHANNEL.registerMessage(packetId++, ServerboundTheaterAbilityPacket.class,
                ServerboundTheaterAbilityPacket::encode,
                ServerboundTheaterAbilityPacket::decode,
                ServerboundTheaterAbilityPacket::handle);

        CHANNEL.registerMessage(packetId++, ServerboundChallengeGodPacket.class,
                ServerboundChallengeGodPacket::encode,
                ServerboundChallengeGodPacket::decode,
                ServerboundChallengeGodPacket::handle);
    }
}
