package com.arcanelens.capability;

import com.arcanelens.network.NetworkHandler;
import com.arcanelens.network.packet.ClientboundSyncFaithPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.PacketDistributor;

public class FaithSync
{
    public static void syncToClient(ServerPlayer player)
    {
        player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap ->
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new ClientboundSyncFaithPacket(cap.getFaith(), cap.getSleepingGodDefeats(), cap.getBrokenVesselDefeats(),
                                cap.isHungerEyesRevealed(), cap.isHungerEyesHidden(), cap.getBlessingBoonLevel(),
                                cap.getBurdenResistBoonLevel(), cap.hasGivenHungerTokens(), cap.hasReceivedHungersPact(),
                                cap.hasClaimedSleepingGodRelic(), cap.hasBeenToldRelicLocation(), cap.getPledgedGod(),
                                cap.getPledgeProgress())));
    }

    /** Grants Faith and gives the owner actual feedback that it happened - an actionbar message plus the
     * same "you gained something Faith-related" chime the Weird Amulet/Hunger's Boon already use. All five
     * dedicated god altars previously called addFaith()+syncToClient() silently, which made their
     * periodic/conditional grants (especially Theater's four combined, hard-to-predict conditions)
     * effectively invisible to the player. sourceLabel names what triggered the grant, e.g. "Theater's Altar". */
    public static void grantFaithWithFeedback(ServerPlayer player, int amount, String sourceLabel)
    {
        if (amount <= 0)
        {
            return;
        }
        player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> cap.addFaith(amount));
        syncToClient(player);
        player.displayClientMessage(Component.literal("+" + amount + " Faith ")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.literal("(" + sourceLabel + ")").withStyle(ChatFormatting.GRAY)), true);
        player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.0F);
    }
}
