package com.arcanelens.api.spell;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface Spell
{
    ItemStack getIcon();

    Component getDisplayName();

    int getManaCost();

    int getBaseCooldownTicks();

    /**
     * @param stackCount how many duplicate copies of this spell the casting lens has inscribed (locked + configurable,
     *                    deduplicated by spell id) - implementations scale their effect magnitude from this.
     */
    void cast(ServerPlayer caster, ItemStack lensStack, ServerLevel level, int stackCount);

    default boolean canCast(ServerPlayer caster, ItemStack lensStack)
    {
        return true;
    }
}
