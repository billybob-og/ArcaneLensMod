package com.arcanelens.god;

import com.arcanelens.capability.FaithProvider;
import com.arcanelens.registry.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** The sentinel value for "declined to pledge" (see ServerboundChoosePledgePacket/IFaith.pledgedGod) -
 * not a GodDefinition itself, since staying neutral has no altar/lore of its own. */
public class GodRegistry
{
    public static final String NEUTRAL_ID = "neutral";

    // Internal ids ("hunger"/"fertility"/"war"/"sun"/"theater") are deliberately NOT renamed to match
    // these proper names - they're persisted in player NBT (IFaith.pledgedGod) and compared against
    // throughout the codebase, so only the display Component changes here.
    public static final List<GodDefinition> GODS = List.of(
            new GodDefinition("hunger", Component.literal("The Hunger"), null, true, () -> ModBlocks.HUNGER_FAITH_ALTAR,
                    Component.literal("Something that's always been watching, and never quite full.")),
            new GodDefinition("fertility", Component.literal("Florian"), null, true, () -> ModBlocks.FERTILITY_FAITH_ALTAR,
                    Component.literal("What grows wild answers to him.")),
            new GodDefinition("war", Component.literal("Aurelia"), null, true, () -> ModBlocks.WAR_FAITH_ALTAR,
                    Component.literal("She remembers every fall, and rewards the ones still standing.")),
            new GodDefinition("sun", Component.literal("Quetzera"), null, true, () -> ModBlocks.SUN_FAITH_ALTAR,
                    Component.literal("Thrives in daylight, and grows sluggish once it's gone.")),
            new GodDefinition("theater", Component.literal("Janus"), null, true, () -> ModBlocks.THEATER_FAITH_ALTAR,
                    Component.literal("Three faces, one mask, and a taste for the theatrical."))
    );

    public static boolean isValidChoice(String godId)
    {
        if (NEUTRAL_ID.equals(godId))
        {
            return true;
        }
        return GODS.stream().anyMatch(god -> god.available() && god.id().equals(godId));
    }

    /** Whether player is CURRENTLY pledged to exactly this god - not "pledged when this altar was
     * placed/this item was crafted", and not neutral's weaker taste either. Dedicated altars and the
     * Theater Helmet's perks/abilities are gated on this (only the currently-chosen god's things
     * work), unlike the scaling perks (Fertility/War/Sun/Hunger), which already gated themselves
     * correctly from the start. */
    public static boolean isPledgedTo(Player player, String godId)
    {
        return player.getCapability(FaithProvider.CAPABILITY)
                .map(cap -> cap.getPledgedGod().equals(godId))
                .orElse(false);
    }
}
