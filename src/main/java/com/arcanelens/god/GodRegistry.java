package com.arcanelens.god;

import com.arcanelens.capability.FaithProvider;
import com.arcanelens.registry.ModBlocks;
import com.arcanelens.registry.ModEntityTypes;
import com.arcanelens.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** The sentinel value for "declined to pledge" (see ServerboundChoosePledgePacket/IFaith.pledgedGod) -
 * not a GodDefinition itself, since staying neutral has no altar/lore of its own. */
public class GodRegistry
{
    public static final String NEUTRAL_ID = "neutral";

    // Aurelia's arena isn't built yet (see the God Boss Hub plan) - this origin just picks a patch of
    // the hub's flat concrete floor away from its fixed spawn point (0.5, 9, 0.5), so the full
    // challenge -> teleport -> boss -> drop flow is testable end to end before any real arena structure
    // exists. arenaStructureId points at a .nbt that doesn't exist either - harmless, since nothing
    // reads it until GodArenaPlacer is built (Phase A's arena-placement piece, still pending).
    private static final BlockPos AURELIA_ARENA_ORIGIN = new BlockPos(30, 9, 0);

    // Internal ids ("hunger"/"fertility"/"war"/"sun"/"theater") are deliberately NOT renamed to match
    // these proper names - they're persisted in player NBT (IFaith.pledgedGod) and compared against
    // throughout the codebase, so only the display Component changes here.
    public static final List<GodDefinition> GODS = List.of(
            new GodDefinition("hunger", Component.literal("The Hunger"), null, true, () -> ModBlocks.HUNGER_FAITH_ALTAR,
                    Component.literal("Something that's always been watching, and never quite full."),
                    null, null, null, null),
            new GodDefinition("fertility", Component.literal("Florian"), null, true, () -> ModBlocks.FERTILITY_FAITH_ALTAR,
                    Component.literal("What grows wild answers to him."),
                    null, null, null, null),
            new GodDefinition("war", Component.literal("Aurelia"), null, true, () -> ModBlocks.WAR_FAITH_ALTAR,
                    Component.literal("She remembers every fall, and rewards the ones still standing."),
                    () -> ModEntityTypes.AURELIA_BOSS, new ResourceLocation("arcanelens", "god_arena_war"),
                    AURELIA_ARENA_ORIGIN, () -> ModItems.SPEAR),
            new GodDefinition("sun", Component.literal("Quetzera"), null, true, () -> ModBlocks.SUN_FAITH_ALTAR,
                    Component.literal("Thrives in daylight, and grows sluggish once it's gone."),
                    null, null, null, null),
            new GodDefinition("theater", Component.literal("Janus"), null, true, () -> ModBlocks.THEATER_FAITH_ALTAR,
                    Component.literal("Three faces, one mask, and a taste for the theatrical."),
                    null, null, null, null)
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
