package com.arcanelens.network.packet;

import com.arcanelens.advancement.ModCriteriaTriggers;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.god.GodRegistry;
import com.arcanelens.menu.GodPledgeMenu;
import com.arcanelens.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sent when the player clicks a button in GodPledgeScreen. Server independently re-validates godId
 * against GodRegistry rather than trusting the client's button state (a placeholder god's button is
 * disabled client-side, but a modified/spoofed client could still send its id). Handles both the free
 * first-time choice and a later paid swap (Pack of the Gods) with the same branch, keyed only on
 * whether IFaith.pledgedGod is already set. */
public class ServerboundChoosePledgePacket
{
    private final String godId;

    public ServerboundChoosePledgePacket(String godId)
    {
        this.godId = godId;
    }

    public static void encode(ServerboundChoosePledgePacket packet, FriendlyByteBuf buf)
    {
        buf.writeUtf(packet.godId);
    }

    public static ServerboundChoosePledgePacket decode(FriendlyByteBuf buf)
    {
        return new ServerboundChoosePledgePacket(buf.readUtf());
    }

    public static void handle(ServerboundChoosePledgePacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !(player.containerMenu instanceof GodPledgeMenu) || !GodRegistry.isValidChoice(packet.godId))
            {
                return;
            }

            player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
                boolean firstChoice = cap.getPledgedGod().isEmpty();
                if (!firstChoice && !consumeOnePack(player))
                {
                    player.displayClientMessage(Component.literal(
                                    "You need a Pack of the Gods to change your pledge.")
                            .withStyle(ChatFormatting.RED), false);
                    return;
                }

                cap.setPledgedGod(packet.godId);
                FaithSync.syncToClient(player);
                // Fired on every pledge, not just the first - the per-god variants (pledge_made_hunger/
                // fertility/war/sun/theater) gate each god's detailed guide book entry and need to catch
                // "ever pledged to this god", including via a later swap, not just a first-time choice.
                // The base pledge_made advancement (no god condition) still only toasts once regardless,
                // since re-firing an already-granted vanilla advancement criterion is a no-op.
                ModCriteriaTriggers.PLEDGE_MADE.trigger(player, packet.godId);
                player.closeContainer();
            });
        });
        ctx.setPacketHandled(true);
    }

    private static boolean consumeOnePack(ServerPlayer player)
    {
        Item pack = ModItems.PACK_OF_THE_GODS.get();
        var items = player.getInventory().items;
        for (int i = 0; i < items.size(); i++)
        {
            ItemStack stack = items.get(i);
            if (stack.is(pack))
            {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}
