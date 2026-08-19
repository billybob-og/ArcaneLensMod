package com.arcanelens.capability;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public interface IKnownSpells
{
    boolean knows(ResourceLocation spellId);

    void learn(ResourceLocation spellId);

    Set<ResourceLocation> getKnownSpells();
}
