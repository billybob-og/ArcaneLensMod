package com.arcanelens.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class KnownSpellsImpl implements IKnownSpells
{
    private final Set<ResourceLocation> known = new HashSet<>();

    @Override
    public boolean knows(ResourceLocation spellId)
    {
        return known.contains(spellId);
    }

    @Override
    public void learn(ResourceLocation spellId)
    {
        known.add(spellId);
    }

    @Override
    public Set<ResourceLocation> getKnownSpells()
    {
        return known;
    }

    public CompoundTag serializeNBT()
    {
        ListTag list = new ListTag();
        for (ResourceLocation id : known)
        {
            list.add(StringTag.valueOf(id.toString()));
        }
        CompoundTag tag = new CompoundTag();
        tag.put("KnownSpells", list);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag)
    {
        known.clear();
        ListTag list = tag.getList("KnownSpells", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++)
        {
            known.add(new ResourceLocation(list.getString(i)));
        }
    }
}
