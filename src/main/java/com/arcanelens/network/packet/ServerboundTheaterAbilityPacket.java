package com.arcanelens.network.packet;

import com.arcanelens.god.GodRegistry;
import com.arcanelens.item.TheaterAbilityHandler;
import com.arcanelens.item.TheaterHelmetPerkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sent by TheaterAbilityHandler.onRightClickEmpty. PlayerInteractEvent.RightClickEmpty only ever
 * fires on the client (it's posted from Minecraft.class's own input handling, never from anything a
 * dedicated or integrated server runs), so the actual ability logic - spending Faith, spawning the
 * clone, retargeting mobs - has to happen here instead, the same way every other player-initiated
 * action in this mod crosses the client/server boundary. crowdControl distinguishes which of the two
 * plain-empty-click abilities was requested (Vanishing Act has its own trigger, a held-sneak tick
 * counter that already runs server-side in TheaterAbilityHandler.onPlayerTick, so it never needs this
 * packet). Server independently re-validates wearing-the-helmet/empty-handed rather than trusting the
 * client's own gating. */
public class ServerboundTheaterAbilityPacket
{
    private final boolean crowdControl;

    public ServerboundTheaterAbilityPacket(boolean crowdControl)
    {
        this.crowdControl = crowdControl;
    }

    public static void encode(ServerboundTheaterAbilityPacket packet, FriendlyByteBuf buf)
    {
        buf.writeBoolean(packet.crowdControl);
    }

    public static ServerboundTheaterAbilityPacket decode(FriendlyByteBuf buf)
    {
        return new ServerboundTheaterAbilityPacket(buf.readBoolean());
    }

    public static void handle(ServerboundTheaterAbilityPacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !TheaterHelmetPerkHandler.isWearingTheaterHelmet(player) || !TheaterHelmetPerkHandler.isEmptyHanded(player)
                    || !GodRegistry.isPledgedTo(player, "theater"))
            {
                return;
            }

            if (packet.crowdControl)
            {
                TheaterAbilityHandler.tryCrowdControl(player);
            }
            else
            {
                TheaterAbilityHandler.tryStageClone(player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
