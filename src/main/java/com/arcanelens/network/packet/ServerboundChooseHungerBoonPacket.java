package com.arcanelens.network.packet;

import com.arcanelens.menu.TokenOfTheHungerMenu;
import com.arcanelens.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sent when the player clicks a button in TokenOfTheHungerScreen - consumes one Token of the Hunger from
 * their inventory and grants the chosen item. Does not touch IFaith directly (see TheHungersBoonItem /
 * FaithAltarBlockEntity for what each granted item actually does). */
public class ServerboundChooseHungerBoonPacket
{
    private final boolean chooseBindings;

    public ServerboundChooseHungerBoonPacket(boolean chooseBindings)
    {
        this.chooseBindings = chooseBindings;
    }

    public static void encode(ServerboundChooseHungerBoonPacket packet, FriendlyByteBuf buf)
    {
        buf.writeBoolean(packet.chooseBindings);
    }

    public static ServerboundChooseHungerBoonPacket decode(FriendlyByteBuf buf)
    {
        return new ServerboundChooseHungerBoonPacket(buf.readBoolean());
    }

    public static void handle(ServerboundChooseHungerBoonPacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !(player.containerMenu instanceof TokenOfTheHungerMenu))
            {
                return;
            }

            if (!consumeOneToken(player))
            {
                return;
            }

            ItemStack granted = new ItemStack(packet.chooseBindings ? ModItems.HUNGERS_BINDINGS.get() : ModItems.HUNGERS_BOON.get());
            if (!player.getInventory().add(granted))
            {
                player.drop(granted, false);
            }
            player.closeContainer();
        });
        ctx.setPacketHandled(true);
    }

    private static boolean consumeOneToken(ServerPlayer player)
    {
        Item token = ModItems.TOKEN_OF_THE_HUNGER.get();
        var items = player.getInventory().items;
        for (int i = 0; i < items.size(); i++)
        {
            ItemStack stack = items.get(i);
            if (stack.is(token))
            {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}
