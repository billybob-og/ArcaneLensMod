package com.arcanelens.network.packet;

import com.arcanelens.item.MagicLensItem;
import com.arcanelens.util.LensSpellView;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class ServerboundCycleSpellPacket
{
    public ServerboundCycleSpellPacket()
    {
    }

    public static void encode(ServerboundCycleSpellPacket packet, FriendlyByteBuf buf)
    {
    }

    public static ServerboundCycleSpellPacket decode(FriendlyByteBuf buf)
    {
        return new ServerboundCycleSpellPacket();
    }

    public static void handle(ServerboundCycleSpellPacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null)
            {
                return;
            }
            ItemStack lens = findHeldLens(player);
            if (lens.isEmpty())
            {
                return;
            }

            List<LensSpellView.Entry> view = LensSpellView.build(lens);
            if (view.isEmpty())
            {
                return;
            }

            int current = MagicLensItem.getSelectedIndex(lens);
            int next = ((current % view.size()) + view.size() + 1) % view.size();
            MagicLensItem.setSelectedIndex(lens, next);
        });
        ctx.setPacketHandled(true);
    }

    public static ItemStack findHeldLens(ServerPlayer player)
    {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHand.getItem() instanceof MagicLensItem)
        {
            return mainHand;
        }
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offHand.getItem() instanceof MagicLensItem)
        {
            return offHand;
        }
        return ItemStack.EMPTY;
    }
}
