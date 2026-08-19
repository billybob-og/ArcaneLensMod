package com.arcanelens.item;

import com.arcanelens.Config;
import com.arcanelens.api.spell.Spell;
import com.arcanelens.api.spell.SpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class MagicLensItem extends Item
{
    public static final int MAX_CONFIGURABLE_SLOTS = 5;
    public static final int MAX_MERGE_COUNT = 5;

    public MagicLensItem(Properties properties)
    {
        super(properties);
    }

    public static int getMana(ItemStack stack)
    {
        return stack.getOrCreateTag().getInt("Mana");
    }

    public static void setMana(ItemStack stack, int mana)
    {
        stack.getOrCreateTag().putInt("Mana", Math.max(0, Math.min(mana, getMaxMana(stack))));
    }

    public static int getMaxMana(ItemStack stack)
    {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("MaxMana"))
        {
            tag.putInt("MaxMana", Config.baseLensMaxMana);
        }
        return tag.getInt("MaxMana");
    }

    public static void setMaxMana(ItemStack stack, int maxMana)
    {
        stack.getOrCreateTag().putInt("MaxMana", maxMana);
    }

    public static int getMergeCount(ItemStack stack)
    {
        return stack.getOrCreateTag().getInt("MergeCount");
    }

    public static void setMergeCount(ItemStack stack, int count)
    {
        stack.getOrCreateTag().putInt("MergeCount", count);
    }

    public static boolean isMerged(ItemStack stack)
    {
        return getMergeCount(stack) > 0;
    }

    public static List<String> getConfigurableSpells(ItemStack stack)
    {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.getList("Spells", Tag.TAG_STRING);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < MAX_CONFIGURABLE_SLOTS; i++)
        {
            result.add(i < list.size() ? list.getString(i) : "");
        }
        return result;
    }

    public static void setConfigurableSpells(ItemStack stack, List<String> spells)
    {
        ListTag list = new ListTag();
        for (int i = 0; i < MAX_CONFIGURABLE_SLOTS; i++)
        {
            list.add(StringTag.valueOf(i < spells.size() ? spells.get(i) : ""));
        }
        stack.getOrCreateTag().put("Spells", list);
    }

    public static void setConfigurableSpell(ItemStack stack, int slot, String spellId)
    {
        if (slot < 0 || slot >= MAX_CONFIGURABLE_SLOTS)
        {
            return;
        }
        List<String> spells = getConfigurableSpells(stack);
        spells.set(slot, spellId == null ? "" : spellId);
        setConfigurableSpells(stack, spells);
    }

    public static List<String> getLockedSpells(ItemStack stack)
    {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.getList("LockedSpells", Tag.TAG_STRING);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++)
        {
            result.add(list.getString(i));
        }
        return result;
    }

    public static void setLockedSpells(ItemStack stack, List<String> spells)
    {
        ListTag list = new ListTag();
        for (String s : spells)
        {
            list.add(StringTag.valueOf(s));
        }
        stack.getOrCreateTag().put("LockedSpells", list);
    }

    public static int getSelectedIndex(ItemStack stack)
    {
        return stack.getOrCreateTag().getInt("SelectedIndex");
    }

    public static void setSelectedIndex(ItemStack stack, int index)
    {
        stack.getOrCreateTag().putInt("SelectedIndex", index);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.literal("Mana: " + getMana(stack) + " / " + getMaxMana(stack)).withStyle(ChatFormatting.AQUA));

        int mergeCount = getMergeCount(stack);
        if (mergeCount > 0)
        {
            tooltip.add(Component.literal("Merge tier: " + mergeCount + "/" + MAX_MERGE_COUNT).withStyle(ChatFormatting.GOLD));
        }

        List<String> locked = getLockedSpells(stack);
        if (!locked.isEmpty())
        {
            tooltip.add(Component.literal("Locked spells:").withStyle(ChatFormatting.GRAY));
            for (String id : locked)
            {
                appendSpellLine(tooltip, id, ChatFormatting.DARK_PURPLE);
            }
        }

        tooltip.add(Component.literal("Spells:").withStyle(ChatFormatting.GRAY));
        for (String id : getConfigurableSpells(stack))
        {
            appendSpellLine(tooltip, id, ChatFormatting.LIGHT_PURPLE);
        }
    }

    private static void appendSpellLine(List<Component> tooltip, String id, ChatFormatting color)
    {
        if (id == null || id.isEmpty())
        {
            tooltip.add(Component.literal(" - (empty)").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        Spell spell = SpellRegistry.REGISTRY.get().getValue(new ResourceLocation(id));
        Component name = spell != null ? spell.getDisplayName() : Component.literal(id);
        tooltip.add(Component.literal(" - ").append(name).withStyle(color));
    }
}
