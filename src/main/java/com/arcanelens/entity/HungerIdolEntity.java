package com.arcanelens.entity;

import com.arcanelens.advancement.ModCriteriaTriggers;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The permanent resident at the top of the Hunger Tower - mostly atmosphere, but also the source of The
 * Hunger's Pact (see HungersPactItem), given in exchange for a two-step trade: 3 Tokens of the Hunger on one
 * interaction, then 1 Arcane Ink on a later, separate interaction. Also, once a player's eyes have been
 * revealed, a one-time hint pointing toward the Sleeping God Relic Shrine in the Warped Hollow (see
 * tryPointToRelic) - the one line where this Idol breaks its usual crypticism and just says something
 * plainly. Outside of both of those, right-clicking just prints one of a small pool of cryptic teaser
 * lines; repeatable, no reward.
 */
public class HungerIdolEntity extends TheHungerEntity
{
    private static final int TOKENS_REQUIRED = 3;
    private static final int ARCANE_INK_REQUIRED = 1;

    private static final List<String> TEASER_LINES = List.of(
            "There is more of me than this. You are not ready to see it yet.",
            "This is only where I chose to be seen. Elsewhere, I simply... am.",
            "Not every door I've made looks like a door.");

    public HungerIdolEntity(EntityType<? extends TheHungerEntity> entityType, Level level)
    {
        super(entityType, level, false);
    }

    @Override
    public boolean isPickable()
    {
        // The brief apparition (TheHungerEntity) can't be targeted at all - but this idol needs to be
        // right-clickable, so override that back on.
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand)
    {
        if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer)
        {
            if (!tryExchange(serverPlayer) && !tryPointToRelic(serverPlayer))
            {
                String line = TEASER_LINES.get(ThreadLocalRandom.current().nextInt(TEASER_LINES.size()));
                player.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY), true);
                player.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6F, 1.0F);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /** One-time hint toward the Sleeping God Relic Shrine, gated on the player's eyes already being
     * revealed (an existing Hunger milestone) and not already told - this is the one line where the Idol
     * breaks its usual crypticism; every other interaction, including repeats after this one, falls
     * through to the normal TEASER_LINES pool. @return true if this fired (so the normal teaser line
     * should be skipped this interaction) */
    private boolean tryPointToRelic(ServerPlayer player)
    {
        boolean[] handled = {false};
        player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
            if (!cap.isHungerEyesRevealed() || cap.hasBeenToldRelicLocation())
            {
                return;
            }

            cap.setHasBeenToldRelicLocation(true);
            FaithSync.syncToClient(player);
            ModCriteriaTriggers.HUNGER_POINTS_THE_WAY.trigger(player);
            player.displayClientMessage(Component.literal(
                    "Something remembers being asleep, and I know exactly where it's kept. Look for it - I won't make you work for this one.")
                    .withStyle(ChatFormatting.DARK_GRAY), true);
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6F, 1.0F);
            handled[0] = true;
        });
        return handled[0];
    }

    /** @return true if a step of the exchange happened (so the normal teaser line should be skipped this interaction) */
    private boolean tryExchange(ServerPlayer player)
    {
        boolean[] handled = {false};
        player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.hasReceivedHungersPact())
            {
                // Exchange already complete - nothing left to trade for, always fall back to the teaser lines.
                return;
            }

            if (!cap.hasGivenHungerTokens())
            {
                if (consumeItems(player, ModItems.TOKEN_OF_THE_HUNGER.get(), TOKENS_REQUIRED))
                {
                    cap.setHasGivenHungerTokens(true);
                    FaithSync.syncToClient(player);
                    player.displayClientMessage(Component.literal(
                            "The Hunger takes the tokens without a word. ...it will want more than this before it's done.")
                            .withStyle(ChatFormatting.DARK_GRAY), true);
                    player.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6F, 0.8F);
                    handled[0] = true;
                }
                return;
            }

            if (consumeItems(player, ModItems.ARCANE_INK.get(), ARCANE_INK_REQUIRED))
            {
                cap.setHasReceivedHungersPact(true);
                FaithSync.syncToClient(player);
                ItemStack pact = new ItemStack(ModItems.HUNGERS_PACT.get());
                if (!player.getInventory().add(pact))
                {
                    player.drop(pact, false);
                }
                player.displayClientMessage(Component.literal(
                        "The ink vanishes into it. Something passes back the other way.")
                        .withStyle(ChatFormatting.DARK_GRAY), true);
                player.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6F, 1.2F);
                handled[0] = true;
            }
        });
        return handled[0];
    }

    private static boolean consumeItems(ServerPlayer player, net.minecraft.world.item.Item item, int amount)
    {
        var items = player.getInventory().items;

        int available = 0;
        for (int i = 0; i < items.size(); i++)
        {
            if (items.get(i).is(item))
            {
                available += items.get(i).getCount();
            }
        }
        if (available < amount)
        {
            return false;
        }

        int remaining = amount;
        for (int i = 0; i < items.size() && remaining > 0; i++)
        {
            ItemStack stack = items.get(i);
            if (!stack.is(item))
            {
                continue;
            }
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;
        }
        return true;
    }
}
