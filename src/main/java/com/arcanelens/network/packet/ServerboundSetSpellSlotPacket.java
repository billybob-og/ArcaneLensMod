package com.arcanelens.network.packet;

import com.arcanelens.advancement.ModCriteriaTriggers;
import com.arcanelens.capability.KnownSpellsProvider;
import com.arcanelens.item.DiamondEngraverItem;
import com.arcanelens.item.MagicLensItem;
import com.arcanelens.menu.InscriptionWorkbenchMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundSetSpellSlotPacket
{
    private final int slotIndex;
    private final String spellId;

    public ServerboundSetSpellSlotPacket(int slotIndex, String spellId)
    {
        this.slotIndex = slotIndex;
        this.spellId = spellId;
    }

    public static void encode(ServerboundSetSpellSlotPacket packet, FriendlyByteBuf buf)
    {
        buf.writeInt(packet.slotIndex);
        buf.writeUtf(packet.spellId);
    }

    public static ServerboundSetSpellSlotPacket decode(FriendlyByteBuf buf)
    {
        return new ServerboundSetSpellSlotPacket(buf.readInt(), buf.readUtf());
    }

    public static void handle(ServerboundSetSpellSlotPacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null)
            {
                return;
            }
            if (player.containerMenu instanceof InscriptionWorkbenchMenu menu)
            {
                ItemStack lens = menu.getLensStack();
                if (!lens.isEmpty() && lens.getItem() instanceof MagicLensItem)
                {
                    boolean assigningSpell = packet.spellId != null && !packet.spellId.isEmpty();

                    if (assigningSpell)
                    {
                        ResourceLocation spellId = new ResourceLocation(packet.spellId);
                        boolean knows = player.getCapability(KnownSpellsProvider.CAPABILITY)
                                .map(cap -> cap.knows(spellId)).orElse(false);
                        if (!knows)
                        {
                            return;
                        }

                        ItemStack engraver = menu.getEngraverStack();
                        if (engraver.isEmpty() || !(engraver.getItem() instanceof DiamondEngraverItem))
                        {
                            player.displayClientMessage(Component.literal("Needs a Diamond Engraver.").withStyle(ChatFormatting.RED), true);
                            return;
                        }
                        engraver.hurtAndBreak(1, player, p -> {});
                    }

                    MagicLensItem.setConfigurableSpell(lens, packet.slotIndex, packet.spellId);

                    int filledSlots = MagicLensItem.getLockedSpells(lens).size()
                            + (int) MagicLensItem.getConfigurableSpells(lens).stream().filter(s -> s != null && !s.isEmpty()).count();
                    ModCriteriaTriggers.LENS_SLOTS_FILLED.trigger(player, filledSlots);

                    menu.broadcastChanges();
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
