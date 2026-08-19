package com.arcanelens.item;

import com.arcanelens.advancement.ModCriteriaTriggers;
import com.arcanelens.api.spell.Spell;
import com.arcanelens.api.spell.SpellRegistry;
import com.arcanelens.capability.KnownSpellsSync;
import com.arcanelens.capability.KnownSpellsProvider;
import com.arcanelens.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class AncientSpellBookItem extends Item
{
    private static final int RECYCLE_YIELD = 2;

    public AncientSpellBookItem(Properties properties)
    {
        super(properties);
    }

    @Nullable
    public static ResourceLocation getSpellId(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("SpellId"))
        {
            return new ResourceLocation(tag.getString("SpellId"));
        }
        return null;
    }

    public static void setSpellId(ItemStack stack, ResourceLocation spellId)
    {
        stack.getOrCreateTag().putString("SpellId", spellId.toString());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide)
        {
            return InteractionResultHolder.success(stack);
        }

        ResourceLocation spellId = getSpellId(stack);
        Spell spell = spellId != null ? SpellRegistry.REGISTRY.get().getValue(spellId) : null;
        if (spellId == null || spell == null || !(player instanceof ServerPlayer serverPlayer))
        {
            return InteractionResultHolder.fail(stack);
        }

        player.getCapability(KnownSpellsProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.knows(spellId))
            {
                ItemStack reward = new ItemStack(ModItems.RAW_ARCANE_MATERIAL.get(), RECYCLE_YIELD);
                if (!player.getInventory().add(reward))
                {
                    player.drop(reward, false);
                }
                serverPlayer.displayClientMessage(
                        Component.literal("Recycled duplicate spell book into raw arcane material.").withStyle(ChatFormatting.GRAY), true);
                stack.shrink(1);
                return;
            }

            cap.learn(spellId);
            ModCriteriaTriggers.SPELL_LEARNED.trigger(serverPlayer, spellId);
            KnownSpellsSync.syncToClient(serverPlayer);
            serverPlayer.displayClientMessage(
                    Component.literal("Learned: ").append(spell.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
            stack.shrink(1);
        });

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag)
    {
        ResourceLocation spellId = getSpellId(stack);
        if (spellId == null)
        {
            return;
        }
        Spell spell = SpellRegistry.REGISTRY.get().getValue(spellId);
        Component name = spell != null ? spell.getDisplayName() : Component.literal(spellId.toString());
        tooltip.add(Component.literal("Teaches: ").append(name).withStyle(ChatFormatting.GRAY));
    }
}
