package com.arcanelens.network.packet;

import com.arcanelens.Config;
import com.arcanelens.api.spell.Spell;
import com.arcanelens.api.spell.SpellRegistry;
import com.arcanelens.capability.SkillTreeProvider;
import com.arcanelens.item.MagicLensItem;
import com.arcanelens.spell.SpellScaling;
import com.arcanelens.util.LensSpellView;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class ServerboundCastSpellPacket
{
    public ServerboundCastSpellPacket()
    {
    }

    public static void encode(ServerboundCastSpellPacket packet, FriendlyByteBuf buf)
    {
    }

    public static ServerboundCastSpellPacket decode(FriendlyByteBuf buf)
    {
        return new ServerboundCastSpellPacket();
    }

    public static void handle(ServerboundCastSpellPacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null)
            {
                return;
            }
            ItemStack lens = ServerboundCycleSpellPacket.findHeldLens(player);
            if (lens.isEmpty())
            {
                return;
            }

            List<LensSpellView.Entry> view = LensSpellView.build(lens);
            int selected = MagicLensItem.getSelectedIndex(lens);
            if (selected < 0 || selected >= view.size())
            {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("No spell selected."), true);
                return;
            }

            LensSpellView.Entry entry = view.get(selected);
            Spell spell = SpellRegistry.REGISTRY.get().getValue(new ResourceLocation(entry.spellId()));
            if (spell == null)
            {
                return;
            }

            if (player.getCooldowns().isOnCooldown(lens.getItem()))
            {
                return;
            }

            double costReductionFactor = player.getCapability(SkillTreeProvider.CAPABILITY)
                    .map(cap -> 1.0 - Math.min(cap.getCostReductionLevel(), Config.costReductionMaxLevel) * Config.costReductionPerLevel)
                    .orElse(1.0);
            double cooldownReductionFactor = player.getCapability(SkillTreeProvider.CAPABILITY)
                    .map(cap -> 1.0 - Math.min(cap.getCooldownReductionLevel(), Config.cooldownReductionMaxLevel) * Config.cooldownReductionPerLevel)
                    .orElse(1.0);

            int stackCount = entry.stackCount();
            int manaCost = Math.round((float) (spell.getManaCost() * SpellScaling.costMultiplier(stackCount) * costReductionFactor));
            if (MagicLensItem.getMana(lens) < manaCost)
            {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("Not enough mana.").withStyle(ChatFormatting.RED), true);
                return;
            }

            int cooldownTicks = Math.round((float) (spell.getBaseCooldownTicks() * SpellScaling.costMultiplier(stackCount) * cooldownReductionFactor));

            MagicLensItem.setMana(lens, MagicLensItem.getMana(lens) - manaCost);
            spell.cast(player, lens, (ServerLevel) player.level(), stackCount);
            player.getCooldowns().addCooldown(lens.getItem(), cooldownTicks);
        });
        ctx.setPacketHandled(true);
    }
}
