package com.arcanelens.client.screen;

import com.arcanelens.api.spell.Spell;
import com.arcanelens.api.spell.SpellRegistry;
import com.arcanelens.capability.KnownSpellsProvider;
import com.arcanelens.item.MagicLensItem;
import com.arcanelens.menu.InscriptionWorkbenchMenu;
import com.arcanelens.network.NetworkHandler;
import com.arcanelens.network.packet.ServerboundSetSpellSlotPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InscriptionWorkbenchScreen extends AbstractContainerScreen<InscriptionWorkbenchMenu>
{
    private static final int LENS_SLOT_X = 70;
    private static final int LENS_SLOT_Y = 20;
    private static final int ENGRAVER_SLOT_X = 98;
    private static final int ENGRAVER_SLOT_Y = 20;
    private static final int SLOT_BUTTONS_Y = 44;
    private static final int SPELL_BUTTONS_Y = 66;
    private static final int SPELL_BUTTON_SPACING = 20;
    private static final int SPELL_BUTTON_WIDTH = 150;
    private static final int VISIBLE_SPELL_ROWS = 5;
    private static final int SCROLLBAR_WIDTH = 6;

    private int selectedSlot = 0;
    private int scrollOffset = 0;
    private List<Map.Entry<ResourceLocation, Spell>> knownSpellEntries = List.of();

    public InscriptionWorkbenchScreen(InscriptionWorkbenchMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 268;
        this.inventoryLabelY = 174;
    }

    @Override
    protected void init()
    {
        super.init();
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        for (int i = 0; i < MagicLensItem.MAX_CONFIGURABLE_SLOTS; i++)
        {
            int index = i;
            this.addRenderableWidget(Button.builder(Component.literal("Slot " + (i + 1)), b -> selectedSlot = index)
                    .bounds(left + 8 + i * 33, top + SLOT_BUTTONS_Y, 32, 18).build());
        }

        Player clientPlayer = Minecraft.getInstance().player;

        knownSpellEntries = new ArrayList<>();
        for (var entry : SpellRegistry.REGISTRY.get().getEntries())
        {
            ResourceLocation id = entry.getKey().location();
            boolean known = clientPlayer != null && clientPlayer.getCapability(KnownSpellsProvider.CAPABILITY)
                    .map(cap -> cap.knows(id)).orElse(false);
            if (known)
            {
                knownSpellEntries.add(Map.entry(id, entry.getValue()));
            }
        }

        int maxScroll = Math.max(0, knownSpellEntries.size() - VISIBLE_SPELL_ROWS);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

        for (int row = 0; row < VISIBLE_SPELL_ROWS; row++)
        {
            int index = scrollOffset + row;
            if (index >= knownSpellEntries.size())
            {
                break;
            }
            ResourceLocation id = knownSpellEntries.get(index).getKey();
            Spell spell = knownSpellEntries.get(index).getValue();
            int rowY = top + SPELL_BUTTONS_Y + row * SPELL_BUTTON_SPACING;
            this.addRenderableWidget(Button.builder(spell.getDisplayName(), b -> assignSpell(id))
                    .bounds(left + 8, rowY, SPELL_BUTTON_WIDTH, 18).build());
        }
    }

    private void assignSpell(ResourceLocation spellId)
    {
        NetworkHandler.CHANNEL.sendToServer(new ServerboundSetSpellSlotPacket(selectedSlot, spellId.toString()));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        int maxScroll = Math.max(0, knownSpellEntries.size() - VISIBLE_SPELL_ROWS);
        int newOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta), 0, maxScroll);
        if (newOffset != scrollOffset)
        {
            scrollOffset = newOffset;
            this.rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF8B7355);

        drawSlotFrame(graphics, left, top, LENS_SLOT_X, LENS_SLOT_Y);
        drawSlotFrame(graphics, left, top, ENGRAVER_SLOT_X, ENGRAVER_SLOT_Y);

        // Player inventory (3 rows of 9) + hotbar - positions must match InscriptionWorkbenchMenu's slot coordinates
        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                drawSlotFrame(graphics, left, top, 8 + col * 18, 184 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++)
        {
            drawSlotFrame(graphics, left, top, 8 + col * 18, 242);
        }

        if (knownSpellEntries.size() > VISIBLE_SPELL_ROWS)
        {
            drawScrollbar(graphics, left, top);
        }
    }

    private void drawScrollbar(GuiGraphics graphics, int left, int top)
    {
        int trackX = left + 8 + SPELL_BUTTON_WIDTH + 4;
        int trackY = top + SPELL_BUTTONS_Y;
        int trackHeight = VISIBLE_SPELL_ROWS * SPELL_BUTTON_SPACING - 2;
        graphics.fill(trackX, trackY, trackX + SCROLLBAR_WIDTH, trackY + trackHeight, 0xFF373737);

        int maxScroll = Math.max(0, knownSpellEntries.size() - VISIBLE_SPELL_ROWS);
        int thumbHeight = Math.max(8, trackHeight * VISIBLE_SPELL_ROWS / knownSpellEntries.size());
        int thumbY = trackY + (maxScroll == 0 ? 0 : (trackHeight - thumbHeight) * scrollOffset / maxScroll);
        graphics.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFBBBBBB);
    }

    private void drawSlotFrame(GuiGraphics graphics, int left, int top, int slotX, int slotY)
    {
        graphics.fill(left + slotX - 1, top + slotY - 1, left + slotX + 17, top + slotY + 17, 0xFF373737);
        graphics.fill(left + slotX, top + slotY, left + slotX + 16, top + slotY + 16, 0xFF8B8B8B);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        ItemStack lens = this.menu.getLensStack();
        if (!lens.isEmpty())
        {
            List<String> spells = MagicLensItem.getConfigurableSpells(lens);
            for (int i = 0; i < spells.size(); i++)
            {
                String id = spells.get(i);
                String label = id.isEmpty() ? "-" : shortName(id);
                drawFittedLabel(graphics, label, left + 8 + i * 33, top + SLOT_BUTTONS_Y - 10, 32);
            }
        }

        ItemStack engraver = this.menu.getEngraverStack();
        if (!engraver.isEmpty())
        {
            int usesLeft = engraver.getMaxDamage() - engraver.getDamageValue();
            drawFittedLabel(graphics, usesLeft + "/" + engraver.getMaxDamage(), left + ENGRAVER_SLOT_X - 4, top + ENGRAVER_SLOT_Y - 10, 24);
        }
    }

    private String shortName(String id)
    {
        int idx = id.indexOf(':');
        return idx >= 0 ? id.substring(idx + 1) : id;
    }

    private void drawFittedLabel(GuiGraphics graphics, String label, int x, int y, int maxWidth)
    {
        int textWidth = this.font.width(label);
        float scale = textWidth > maxWidth ? (float) maxWidth / textWidth : 1.0f;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(this.font, label, 0, 0, 0xFFFFFF, true);
        graphics.pose().popPose();
    }
}
