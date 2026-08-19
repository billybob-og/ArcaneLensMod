package com.arcanelens.client.gui;

import com.arcanelens.api.spell.Spell;
import com.arcanelens.api.spell.SpellRegistry;
import com.arcanelens.item.MagicLensItem;
import com.arcanelens.util.LensSpellView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.List;

public class SpellHudOverlay implements IGuiOverlay
{
    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
        {
            return;
        }

        ItemStack lens = mc.player.getMainHandItem();
        if (!(lens.getItem() instanceof MagicLensItem))
        {
            lens = mc.player.getOffhandItem();
        }
        if (!(lens.getItem() instanceof MagicLensItem))
        {
            return;
        }

        List<LensSpellView.Entry> view = LensSpellView.build(lens);
        int selected = MagicLensItem.getSelectedIndex(lens);

        String label;
        int stackCount = 1;
        if (view.isEmpty())
        {
            label = "(no spells known)";
        }
        else if (selected < 0 || selected >= view.size())
        {
            label = "(empty)";
        }
        else
        {
            LensSpellView.Entry entry = view.get(selected);
            stackCount = entry.stackCount();
            Spell spell = SpellRegistry.REGISTRY.get().getValue(new ResourceLocation(entry.spellId()));
            label = spell != null ? spell.getDisplayName().getString() : entry.spellId();
            if (stackCount > 1)
            {
                label = label + " x" + stackCount;
            }
        }

        String text = "Spell: " + label + "  (" + (view.isEmpty() ? 0 : selected + 1) + "/" + view.size() + ")"
                + "  Mana: " + MagicLensItem.getMana(lens) + "/" + MagicLensItem.getMaxMana(lens);

        int x = width / 2 - mc.font.width(text) / 2;
        int y = height - 65;
        graphics.drawString(mc.font, text, x, y, 0xFFFFFF, true);
    }
}
