package com.arcanelens.util;

import com.arcanelens.Config;
import com.arcanelens.item.MagicLensItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Implements the resolved lens-combining rules (see the design plan's "Lens slot cap & re-merging rule" section). */
public class LensMerger
{
    public static boolean canMerge(ItemStack bottom, ItemStack top)
    {
        if (bottom.isEmpty() || top.isEmpty())
        {
            return false;
        }
        if (!(bottom.getItem() instanceof MagicLensItem) || !(top.getItem() instanceof MagicLensItem))
        {
            return false;
        }
        if (MagicLensItem.getMergeCount(bottom) >= MagicLensItem.MAX_MERGE_COUNT
                || MagicLensItem.getMergeCount(top) >= MagicLensItem.MAX_MERGE_COUNT)
        {
            return false;
        }
        return computeNewMergeCount(bottom, top) <= MagicLensItem.MAX_MERGE_COUNT;
    }

    public static int computeNewMergeCount(ItemStack bottom, ItemStack top)
    {
        // Additive (not max()+1): the accumulated spell payload below is additive regardless of merge-tree
        // shape, so merge count must be too, or a "tournament" merge of two already-merged lenses reaches
        // the same count as a sequential chain while consuming far more base lenses and yielding far more spells.
        return MagicLensItem.getMergeCount(bottom) + MagicLensItem.getMergeCount(top) + 1;
    }

    public static int computePrice(int newMergeCount)
    {
        return (int) Math.round(Config.lensCombinePriceBase * Math.pow(1.75, newMergeCount - 1));
    }

    /** Pure function: assumes {@link #canMerge} already returned true. Does not mutate the inputs. */
    public static ItemStack merge(ItemStack bottom, ItemStack top)
    {
        List<String> newLocked = new ArrayList<>();
        newLocked.addAll(MagicLensItem.getLockedSpells(bottom));
        for (String spellId : MagicLensItem.getConfigurableSpells(bottom))
        {
            if (spellId != null && !spellId.isEmpty())
            {
                newLocked.add(spellId);
            }
        }
        newLocked.addAll(MagicLensItem.getLockedSpells(top));

        List<String> newConfigurable = MagicLensItem.getConfigurableSpells(top);

        int newMaxMana = MagicLensItem.getMaxMana(bottom) + MagicLensItem.getMaxMana(top);
        int newMana = (int) Math.floor(0.75 * (MagicLensItem.getMana(bottom) + MagicLensItem.getMana(top)));
        int newMergeCount = computeNewMergeCount(bottom, top);

        ItemStack result = new ItemStack(bottom.getItem());
        MagicLensItem.setLockedSpells(result, newLocked);
        MagicLensItem.setConfigurableSpells(result, newConfigurable);
        MagicLensItem.setMaxMana(result, newMaxMana);
        MagicLensItem.setMana(result, newMana);
        MagicLensItem.setMergeCount(result, newMergeCount);
        MagicLensItem.setSelectedIndex(result, 0);
        return result;
    }
}
