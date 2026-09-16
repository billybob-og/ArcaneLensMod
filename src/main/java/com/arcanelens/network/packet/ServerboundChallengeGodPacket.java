package com.arcanelens.network.packet;

import com.arcanelens.god.GodChallengeService;
import com.arcanelens.god.GodDefinition;
import com.arcanelens.god.GodRegistry;
import com.arcanelens.menu.GodChallengeMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

/** Sent when the player clicks "Begin Challenge" in GodChallengeScreen. Carries nothing but confirmation
 * - the godId itself is read server-side from the block entity behind the open GodChallengeMenu, never
 * trusted from the client, same never-trust-the-client shape as ServerboundChoosePledgePacket. */
public class ServerboundChallengeGodPacket
{
    public static void encode(ServerboundChallengeGodPacket packet, FriendlyByteBuf buf)
    {
    }

    public static ServerboundChallengeGodPacket decode(FriendlyByteBuf buf)
    {
        return new ServerboundChallengeGodPacket();
    }

    public static void handle(ServerboundChallengeGodPacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !(player.containerMenu instanceof GodChallengeMenu menu))
            {
                return;
            }

            String godId = menu.getBlockEntity().getGodId();
            Optional<GodDefinition> god = GodRegistry.GODS.stream().filter(g -> g.id().equals(godId)).findFirst();
            if (god.isEmpty() || !god.get().hasBossContent())
            {
                player.displayClientMessage(Component.literal("This challenge isn't ready yet.")
                        .withStyle(ChatFormatting.RED), false);
                player.closeContainer();
                return;
            }
            if (GodRegistry.isPledgedTo(player, godId))
            {
                player.displayClientMessage(Component.literal("You cannot challenge a god you're pledged to.")
                        .withStyle(ChatFormatting.RED), false);
                player.closeContainer();
                return;
            }

            player.closeContainer();
            GodChallengeService.beginChallenge(player, god.get());
        });
        ctx.setPacketHandled(true);
    }
}
