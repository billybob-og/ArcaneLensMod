package com.arcanelens.item;

import com.arcanelens.capability.FaithProvider;
import com.arcanelens.god.GodDefinition;
import com.arcanelens.god.GodRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Right-click to check current Faith - a private flavor-text readout in the same style as The Hunger's own
 * messages (see TheHungerHandler), rather than a plain number in a tooltip. Faith itself stays a single
 * shared number regardless of pledge (see PledgeChoiceHandler's design notes) - this only contextualizes
 * the existing readout with who it's currently sworn to, it doesn't track anything new. */
public class WeirdAmuletItem extends Item
{
    private static final List<String> FLAVOR_LINES = List.of(
            "The amulet grows warm against your skin: %d Faith stirs within.",
            "You feel the weight of %d Faith settle in your chest.",
            "The amulet hums quietly - %d Faith recognized.",
            "Something ancient counts for you: %d Faith.");

    public WeirdAmuletItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide)
        {
            player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
                int faith = cap.getFaith();
                String line = FLAVOR_LINES.get(ThreadLocalRandom.current().nextInt(FLAVOR_LINES.size()));
                String message = (String.format(line, faith) + " " + pledgeClause(cap.getPledgedGod(), cap.getPledgeProgress())).trim();
                player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.LIGHT_PURPLE), true);
            });
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.0F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** pledgeProgress is a separate counter from lifetime Faith (see FaithImpl's design notes) - it's what
     * Fertility/War/Sun's scaling perks actually key off of, and resets to 0 on every pledge/swap, so it's
     * the number that answers "how close am I" rather than the lifetime total. */
    private static String pledgeClause(String godId, int pledgeProgress)
    {
        if (godId.isEmpty())
        {
            return "";
        }
        if (godId.equals(GodRegistry.NEUTRAL_ID))
        {
            return "Owed to no god in particular.";
        }
        return GodRegistry.GODS.stream()
                .filter(god -> god.id().equals(godId))
                .findFirst()
                .map(GodDefinition::displayName)
                .map(name -> "Sworn to " + name.getString() + " - " + pledgeProgress + " Faith earned toward that pledge.")
                .orElse("");
    }
}
