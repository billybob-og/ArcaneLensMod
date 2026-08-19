package com.arcanelens.network.packet;

import com.arcanelens.menu.TerminalMenu;
import com.arcanelens.menu.TerminalSortMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent whenever the client clicks a Terminal's sort button, so the server's own TerminalMenu.sortMode -
 * which gates ScrollingPoolWindow's index mapping server-side too - stays in sync with what the client is
 * showing. Matches ServerboundSetTerminalSearchPacket's shape.
 */
public class ServerboundSetTerminalSortPacket
{
    private final TerminalSortMode sortMode;

    public ServerboundSetTerminalSortPacket(TerminalSortMode sortMode)
    {
        this.sortMode = sortMode;
    }

    public static void encode(ServerboundSetTerminalSortPacket packet, FriendlyByteBuf buf)
    {
        buf.writeEnum(packet.sortMode);
    }

    public static ServerboundSetTerminalSortPacket decode(FriendlyByteBuf buf)
    {
        return new ServerboundSetTerminalSortPacket(buf.readEnum(TerminalSortMode.class));
    }

    public static void handle(ServerboundSetTerminalSortPacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.containerMenu instanceof TerminalMenu menu)
            {
                menu.setSortMode(packet.sortMode);
            }
        });
        ctx.setPacketHandled(true);
    }
}
