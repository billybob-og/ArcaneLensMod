package com.arcanelens.spell.spells;

import com.arcanelens.api.spell.Spell;
import com.arcanelens.capability.ISkillTree;
import com.arcanelens.capability.PocketDimensionStateProvider;
import com.arcanelens.capability.SkillTreeProvider;
import com.arcanelens.worldgen.ModDimensions;
import com.arcanelens.worldgen.PocketDimensionPlacer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Set;

/**
 * Casting this spell from anywhere else teleports the caster into their personal Pocket Dimension room
 * (pasting/upgrading it first if needed - see PocketDimensionPlacer); casting it again while already inside
 * teleports back to the exact position/dimension they left from. Direction is decided purely by which
 * dimension the caster is currently in - the stored return position (IPocketDimensionState) only ever
 * supplies coordinates, never the branch decision, so it's safe to leave stale after an in-dimension death.
 */
public class PocketDimensionSpell implements Spell
{
    @Override
    public ItemStack getIcon()
    {
        return Items.ENDER_PEARL.getDefaultInstance();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Pocket Dimension");
    }

    @Override
    public int getManaCost()
    {
        return 100;
    }

    @Override
    public int getBaseCooldownTicks()
    {
        return 200;
    }

    @Override
    public void cast(ServerPlayer caster, ItemStack lensStack, ServerLevel level, int stackCount)
    {
        MinecraftServer server = caster.getServer();
        ServerLevel pocketLevel = server == null ? null : server.getLevel(ModDimensions.POCKET_DIMENSION_KEY);
        if (server == null || pocketLevel == null)
        {
            caster.displayClientMessage(Component.literal("The way isn't ready yet - something still needs to open first.")
                    .withStyle(ChatFormatting.DARK_PURPLE), true);
            return;
        }

        if (caster.level().dimension() == ModDimensions.POCKET_DIMENSION_KEY)
        {
            exit(caster, server);
        }
        else
        {
            enter(caster, pocketLevel);
        }
    }

    private void exit(ServerPlayer caster, MinecraftServer server)
    {
        String returnDimension = caster.getCapability(PocketDimensionStateProvider.CAPABILITY)
                .map(state -> state.getReturnDimension()).orElse("");
        if (returnDimension.isEmpty())
        {
            caster.displayClientMessage(Component.literal("There's nowhere remembered to return to.")
                    .withStyle(ChatFormatting.DARK_PURPLE), true);
            return;
        }

        ResourceKey<Level> returnKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(returnDimension));
        ServerLevel returnLevel = server.getLevel(returnKey);
        if (returnLevel == null)
        {
            returnLevel = server.overworld();
        }

        ServerLevel finalReturnLevel = returnLevel;
        caster.getCapability(PocketDimensionStateProvider.CAPABILITY).ifPresent(state ->
                caster.teleportTo(finalReturnLevel, state.getReturnX(), state.getReturnY(), state.getReturnZ(),
                        Set.of(), state.getReturnYaw(), state.getReturnPitch()));
        caster.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void enter(ServerPlayer caster, ServerLevel pocketLevel)
    {
        caster.getCapability(PocketDimensionStateProvider.CAPABILITY).ifPresent(state ->
                state.setReturnPosition(caster.level().dimension().location().toString(),
                        caster.getX(), caster.getY(), caster.getZ(), caster.getYRot(), caster.getXRot()));

        int expansionLevel = caster.getCapability(SkillTreeProvider.CAPABILITY)
                .map(ISkillTree::getPocketDimensionExpansionLevel).orElse(0);
        BlockPos entry = PocketDimensionPlacer.ensureRoomPlaced(pocketLevel, caster.getUUID(), expansionLevel);

        caster.teleportTo(pocketLevel, entry.getX() + 0.5, entry.getY(), entry.getZ() + 0.5,
                Set.of(), caster.getYRot(), caster.getXRot());
        caster.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
