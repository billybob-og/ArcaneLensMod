package com.arcanelens.god;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
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
 * after actually pledging.
 *
 * <p>bossEntityType/arenaStructureId/arenaOrigin/uniqueDrop are the God Challenge Hub's boss-fight
 * content for this god - all four nullable together (never some-but-not-all; see hasBossContent())
 * since none of the 5 gods have this built yet and it lands incrementally per god as art/arenas are
 * finished. Deliberately a separate concern from `available` (which only gates pledging) - the hub's
 * challenge screen/block filters on hasBossContent(), not available. */
public record GodDefinition(String id, Component displayName, @Nullable Component lockedFlavorText,
                             boolean available, @Nullable Supplier<RegistryObject<Block>> altarBlock,
                             @Nullable Component description,
                             @Nullable Supplier<? extends RegistryObject<? extends EntityType<? extends Mob>>> bossEntityType,
                             @Nullable ResourceLocation arenaStructureId, @Nullable BlockPos arenaOrigin,
                             @Nullable Supplier<RegistryObject<Item>> uniqueDrop)
{
    public boolean hasBossContent()
    {
        return bossEntityType != null && arenaStructureId != null && arenaOrigin != null && uniqueDrop != null;
    }
}
