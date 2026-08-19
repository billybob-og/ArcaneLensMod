package com.arcanelens.util;

import com.arcanelens.item.MagicLensItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Single source of truth for a lens's actual castable spell list: locked ∪ configurable, collapsed by
 * spell id (duplicates - whether from repeated configurable assignment or accumulated through merges -
 * become one entry with a stack count) per the resolved duplicate-buffing rule.
 */
public class LensSpellView
{
    public record Entry(String spellId, int stackCount)
    {
    }

    public static List<Entry> build(ItemStack lens)
    {
        Map<String, Integer> counts = new LinkedHashMap<>();

        for (String id : MagicLensItem.getLockedSpells(lens))
        {
            if (id != null && !id.isEmpty())
            {
                counts.merge(id, 1, Integer::sum);
            }
        }
        for (String id : MagicLensItem.getConfigurableSpells(lens))
        {
            if (id != null && !id.isEmpty())
            {
                counts.merge(id, 1, Integer::sum);
            }
        }

        List<Entry> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet())
        {
            result.add(new Entry(entry.getKey(), entry.getValue()));
        }
        return result;
    }
}
