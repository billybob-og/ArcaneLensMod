package com.arcanelens.god;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/** One entry in GodRegistry.GODS - GodPledgeScreen builds one button per entry, so adding a future god
 * is purely a new entry here (plus its own altar/perk classes), never a UI/menu/packet change.
 * lockedFlavorText and description serve different UI contexts and are kept separate rather than
 * reused for both: lockedFlavorText is a tooltip shown ONLY while a god is unavailable (available =
 * false), description is a short, deliberately vague blurb shown in GodPledgeScreen's hover preview
 * for an available god - the full mechanical detail stays reserved for the guide book, unlocked only
 * after actually pledging. */
public record GodDefinition(String id, Component displayName, @Nullable Component lockedFlavorText,
                             boolean available, @Nullable Supplier<RegistryObject<Block>> altarBlock,
                             @Nullable Component description)
{
}
