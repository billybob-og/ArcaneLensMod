package com.arcanelens.network.packet;

import com.arcanelens.advancement.ModCriteriaTriggers;
import com.arcanelens.block.entity.LensCombinerBlockEntity;
import com.arcanelens.item.MagicLensItem;
import com.arcanelens.menu.LensCombinerMenu;
import com.arcanelens.util.LensMerger;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundCombineLensesPacket
{
    public ServerboundCombineLensesPacket()
    {
    }

    public static void encode(ServerboundCombineLensesPacket packet, FriendlyByteBuf buf)
    {
    }

    public static ServerboundCombineLensesPacket decode(FriendlyByteBuf buf)
    {
        return new ServerboundCombineLensesPacket();
    }

    public static void handle(ServerboundCombineLensesPacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (!(player.containerMenu instanceof LensCombinerMenu menu))
            {
                return;
            }

            LensCombinerBlockEntity blockEntity = menu.getBlockEntity();
            ItemStackHandler handler = blockEntity.getItemHandler();

            ItemStack bottom = handler.getStackInSlot(LensCombinerBlockEntity.SLOT_BOTTOM);
            ItemStack top = handler.getStackInSlot(LensCombinerBlockEntity.SLOT_TOP);
            ItemStack ore = handler.getStackInSlot(LensCombinerBlockEntity.SLOT_ORE);
            ItemStack output = handler.getStackInSlot(LensCombinerBlockEntity.SLOT_OUTPUT);

            if (!output.isEmpty())
            {
                player.displayClientMessage(Component.literal("Take the previous result first.").withStyle(ChatFormatting.RED), true);
                return;
            }

            if (!LensMerger.canMerge(bottom, top))
            {
                player.displayClientMessage(Component.literal("These lenses can't be combined (merge cap reached).").withStyle(ChatFormatting.RED), true);
                return;
            }

            int newMergeCount = LensMerger.computeNewMergeCount(bottom, top);
            int price = LensMerger.computePrice(newMergeCount);
            if (ore.getCount() < price)
            {
                player.displayClientMessage(Component.literal("Need " + price + " raw arcane material.").withStyle(ChatFormatting.RED), true);
                return;
            }

            ItemStack result = LensMerger.merge(bottom, top);

            ore.shrink(price);
            handler.setStackInSlot(LensCombinerBlockEntity.SLOT_BOTTOM, ItemStack.EMPTY);
            handler.setStackInSlot(LensCombinerBlockEntity.SLOT_TOP, ItemStack.EMPTY);
            handler.setStackInSlot(LensCombinerBlockEntity.SLOT_OUTPUT, result);

            ModCriteriaTriggers.LENSES_COMBINED.trigger(player);
            int filledSlots = MagicLensItem.getLockedSpells(result).size()
                    + (int) MagicLensItem.getConfigurableSpells(result).stream().filter(s -> s != null && !s.isEmpty()).count();
            ModCriteriaTriggers.LENS_SLOTS_FILLED.trigger(player, filledSlots);

            menu.broadcastChanges();
        });
        ctx.setPacketHandled(true);
    }
}
