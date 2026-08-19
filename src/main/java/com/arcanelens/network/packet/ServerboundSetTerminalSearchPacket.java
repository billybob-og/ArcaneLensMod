package com.arcanelens.network.packet;

import com.arcanelens.menu.TerminalMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent whenever the client edits a Terminal's search box, so the server's own TerminalMenu.searchQuery -
 * which gates ScrollingPoolWindow's filtered index mapping server-side too - stays in sync with what the
 * client is showing. Matches ServerboundScrollTerminalPacket's shape.
 */
public class ServerboundSetTerminalSearchPacket
{
    private final String query;

    public ServerboundSetTerminalSearchPacket(String query)
    {
        this.query = query;
    }

    public static void encode(ServerboundSetTerminalSearchPacket packet, FriendlyByteBuf buf)
    {
        buf.writeUtf(packet.query, 256);
    }

    public static ServerboundSetTerminalSearchPacket decode(FriendlyByteBuf buf)
    {
        return new ServerboundSetTerminalSearchPacket(buf.readUtf(256));
    }

    public static void handle(ServerboundSetTerminalSearchPacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.containerMenu instanceof TerminalMenu menu)
            {
                menu.setSearchQuery(packet.query);
            }
        });
        ctx.setPacketHandled(true);
    }
}
