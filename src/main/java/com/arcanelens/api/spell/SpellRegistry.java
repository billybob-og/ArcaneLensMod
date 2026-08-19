package com.arcanelens.api.spell;

import com.arcanelens.ArcaneLens;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class SpellRegistry
{
    public static final ResourceKey<Registry<Spell>> SPELL_REGISTRY_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(ArcaneLens.MODID, "spells"));

    public static final DeferredRegister<Spell> SPELLS = DeferredRegister.create(SPELL_REGISTRY_KEY, ArcaneLens.MODID);

    public static final Supplier<IForgeRegistry<Spell>> REGISTRY = SPELLS.makeRegistry(RegistryBuilder::new);
}
