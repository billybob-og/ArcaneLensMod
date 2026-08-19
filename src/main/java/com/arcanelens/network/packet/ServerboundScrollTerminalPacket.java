package com.arcanelens.network.packet;

import com.arcanelens.menu.TerminalMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent whenever the client scrolls a Terminal's pool grid, so the server's own TerminalMenu.scrollOffset
 * - which gates NetworkPoolSlot.isActive() server-side too - stays in sync with what the client is
 * showing. The server never trusts the raw value: TerminalMenu.scrollTo() re-clamps against its own known
 * pool size before applying it.
 */
public class ServerboundScrollTerminalPacket
{
    private final int newScrollOffset;

    public ServerboundScrollTerminalPacket(int newScrollOffset)
    {
        this.newScrollOffset = newScrollOffset;
    }

    public static void encode(ServerboundScrollTerminalPacket packet, FriendlyByteBuf buf)
    {
        buf.writeVarInt(packet.newScrollOffset);
    }

    public static ServerboundScrollTerminalPacket decode(FriendlyByteBuf buf)
    {
        return new ServerboundScrollTerminalPacket(buf.readVarInt());
    }

    public static void handle(ServerboundScrollTerminalPacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.containerMenu instanceof TerminalMenu menu)
            {
                menu.scrollTo(packet.newScrollOffset);
            }
        });
        ctx.setPacketHandled(true);
    }
}
