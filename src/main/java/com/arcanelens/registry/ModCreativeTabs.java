package com.arcanelens.registry;

import com.arcanelens.ArcaneLens;
import com.arcanelens.api.spell.SpellRegistry;
import com.arcanelens.item.AncientSpellBookItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArcaneLens.MODID);

    public static final RegistryObject<CreativeModeTab> ARCANE_LENS_TAB = CREATIVE_MODE_TABS.register("arcane_lens_tab",
            () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.MAGIC_LENS.get().getDefaultInstance())
                    .title(Component.translatable("itemGroup.arcanelens.arcane_lens_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.MAGIC_LENS.get());
                        output.accept(ModItems.ARCANE_GUIDE_BOOK.get());
                        output.accept(ModItems.INSCRIPTION_WORKBENCH.get());
                        output.accept(ModItems.SOUL_PEDESTAL.get());
                        output.accept(ModItems.LENS_PEDESTAL.get());
                        output.accept(ModItems.SOUL_FEEDER.get());
                        output.accept(ModItems.DIAMOND_ENGRAVER.get());
                        for (var entry : SpellRegistry.REGISTRY.get().getEntries())
                        {
                            ItemStack book = new ItemStack(ModItems.ANCIENT_SPELL_BOOK.get());
                            AncientSpellBookItem.setSpellId(book, entry.getKey().location());
                            output.accept(book);
                        }
                        output.accept(ModItems.ARCANE_ORE.get());
                        output.accept(ModItems.RAW_ARCANE_MATERIAL.get());
                        output.accept(ModItems.LENS_COMBINER.get());
                        output.accept(ModItems.SLEEPING_GOD_HELMET.get());
                        output.accept(ModItems.SLEEPING_GOD_CHESTPLATE.get());
                        output.accept(ModItems.SLEEPING_GOD_LEGGINGS.get());
                        output.accept(ModItems.SLEEPING_GOD_BOOTS.get());
                        output.accept(ModItems.FAITH_ALTAR.get());
                        output.accept(ModItems.WEIRD_AMULET.get());
                        output.accept(ModItems.TOKEN_OF_THE_HUNGER.get());
                        output.accept(ModItems.HUNGERS_BOON.get());
                        output.accept(ModItems.HUNGERS_BINDINGS.get());
                        output.accept(ModItems.SUN_AND_MOON_WARD.get());
                        output.accept(ModItems.MAGNET_CHARM.get());
                        output.accept(ModItems.FAITH_CHARM.get());
                        output.accept(ModItems.MANA_ORB_CHARM.get());
                        output.accept(ModItems.BURNING_CHARCOAL_CHARM.get());
                        output.accept(ModItems.ANTIVENOM_CHARM.get());
                        output.accept(ModItems.PRESERVED_WITHER_ROSE_CHARM.get());
                        output.accept(ModItems.IRON_BRACELET.get());
                        output.accept(ModItems.GOLD_BRACELET.get());
                        output.accept(ModItems.NETHERITE_BRACELET.get());
                        output.accept(ModItems.ARCANE_INK.get());
                        output.accept(ModItems.HUNGERS_PACT.get());
                        output.accept(ModItems.STORAGE_BLOCK.get());
                        output.accept(ModItems.STORAGE_CONNECTOR.get());
                        output.accept(ModItems.TERMINAL.get());
                        output.accept(ModItems.STACK_UPGRADE.get());
                        output.accept(ModItems.SLOT_UPGRADE.get());
                        output.accept(ModItems.ARCANE_ASSEMBLER.get());
                        output.accept(ModItems.OVERLOAD_CORE.get());
                        output.accept(ModItems.WARPED_CATALYST.get());
                        output.accept(ModItems.WARPED_VESSEL_SHARD.get());
                        output.accept(ModItems.WARPED_ATTUNEMENT_TEMPLATE.get());
                        output.accept(ModItems.VESSEL_BOUND_SLEEPING_GOD_HELMET.get());
                        output.accept(ModItems.VESSEL_BOUND_SLEEPING_GOD_CHESTPLATE.get());
                        output.accept(ModItems.VESSEL_BOUND_SLEEPING_GOD_LEGGINGS.get());
                        output.accept(ModItems.VESSEL_BOUND_SLEEPING_GOD_BOOTS.get());
                        output.accept(ModItems.WARPED_ANCHOR.get());
                        output.accept(ModItems.VESSEL_SUMMONING_CHARM.get());
                        output.accept(ModItems.HUNGER_FAITH_ALTAR.get());
                        output.accept(ModItems.FERTILITY_FAITH_ALTAR.get());
                        output.accept(ModItems.WAR_FAITH_ALTAR.get());
                        output.accept(ModItems.SUN_FAITH_ALTAR.get());
                        output.accept(ModItems.THEATER_FAITH_ALTAR.get());
                        output.accept(ModItems.THEATER_HELMET.get());
                        output.accept(ModItems.PACK_OF_THE_GODS.get());
                        // ARENA_TRIGGER is deliberately NOT listed here - it's a worldgen-internal block that
                        // expects jigsaw-provided rotation/offset context, not something a player should be
                        // able to grab and place freely. Still obtainable via /give for building purposes.
                    })
                    .build());
}
