package com.arcanelens.client.gui;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** Shows while wearing either full Sleeping God set - the original or the Vessel-Bound upgrade (see
 * VesselBoundFlightHandler) - since a player can only ever have one of the two complete at a time (the
 * smithing upgrade consumes the original pieces). Faith (see FaithAltarBlockEntity) permanently caps how
 * long a single flight lasts and is never spent. Mirrors each handler's own state machine client-side
 * purely off the local Abilities the server already replicates automatically, so it doesn't need its own
 * network packet - landing within the original set's grace window keeps showing the actual remaining time
 * instead of snapping back to the full amount, and the Vessel-Bound set's continuous regen is mirrored the
 * same way SleepingGodFlightHandler/VesselBoundFlightHandler diverge server-side. */
public class FlightHudOverlay implements IGuiOverlay
{
    private int lastTick = -1;
    private int usedTicks;
    private int graceTicksLeft = -1;
    private int regenAccumulator;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
        {
            return;
        }

        boolean vesselBound = hasVesselBoundFullSet(mc.player);
        if (!vesselBound && !hasFullSet(mc.player))
        {
            return;
        }

        int faith = mc.player.getCapability(FaithProvider.CAPABILITY).map(cap -> cap.getFaith()).orElse(0);
        int maxSeconds = Config.baseFlightSeconds + faith / Config.faithPerFlightSecond;
        int maxTicks = maxSeconds * 20;

        boolean flying = mc.player.getAbilities().flying;
        boolean mayfly = mc.player.getAbilities().mayfly;

        // render() runs once per rendered frame (framerate), not once per game tick - only advance state by
        // the actual number of game ticks elapsed since the last render, so this can't drain faster than the
        // server's tick-based tracking just because the framerate is high. mc.player.tickCount resets when
        // the client-side player entity is recreated (e.g. changing dimensions) - if nowTick comes back lower
        // than the last recorded value, treat it as a single tick of progress instead of clamping to 0, which
        // would otherwise freeze the whole HUD until tickCount climbed back past the stale lastTick value.
        int nowTick = mc.player.tickCount;
        int deltaTicks = (lastTick < 0 || nowTick < lastTick) ? 1 : (nowTick - lastTick);
        lastTick = nowTick;

        String text;
        int color;

        if (vesselBound)
        {
            if (!mayfly || maxTicks <= 0)
            {
                usedTicks = 0;
                regenAccumulator = 0;
            }
            else if (flying)
            {
                usedTicks = Math.min(maxTicks, usedTicks + deltaTicks);
                regenAccumulator = 0;
            }
            else if (usedTicks > 0)
            {
                regenAccumulator += deltaTicks;
                int recovered = regenAccumulator / Config.vesselBoundFlightRegenTicksPerRealTick;
                if (recovered > 0)
                {
                    usedTicks = Math.max(0, usedTicks - recovered);
                    regenAccumulator %= Config.vesselBoundFlightRegenTicksPerRealTick;
                }
            }

            int remainingSeconds = Math.max(0, (maxTicks - usedTicks) / 20);
            if (flying)
            {
                text = "Flight: " + remainingSeconds + "s remaining";
                color = 0x55FFFF;
            }
            else if (maxSeconds <= 0)
            {
                text = "Flight: no Faith available";
                color = 0xFF5555;
            }
            else if (usedTicks > 0)
            {
                text = "Flight: " + remainingSeconds + "s remaining (recovering)";
                color = 0xFFAA55;
            }
            else
            {
                text = "Flight ready: " + maxSeconds + "s available";
                color = 0x55FFFF;
            }
        }
        else
        {
            if (!mayfly || maxTicks <= 0)
            {
                usedTicks = 0;
                graceTicksLeft = -1;
            }
            else if (flying)
            {
                usedTicks = Math.min(maxTicks, usedTicks + deltaTicks);
                graceTicksLeft = -1;
            }
            else if (usedTicks > 0)
            {
                if (graceTicksLeft < 0)
                {
                    graceTicksLeft = Config.flightLandedGraceTicks;
                }
                graceTicksLeft = Math.max(0, graceTicksLeft - deltaTicks);
            }

            int remainingSeconds = Math.max(0, (maxTicks - usedTicks) / 20);
            if (flying)
            {
                text = "Flight: " + remainingSeconds + "s remaining";
                color = 0x55FFFF;
            }
            else if (maxSeconds <= 0)
            {
                text = "Flight: no Faith available";
                color = 0xFF5555;
            }
            else if (!mayfly)
            {
                // Not currently grantable despite having Faith - almost certainly on cooldown.
                text = "Flight: recharging";
                color = 0xAAAAAA;
            }
            else if (usedTicks > 0)
            {
                int graceSeconds = (graceTicksLeft + 19) / 20;
                text = "Flight: " + remainingSeconds + "s remaining (landing grace: " + graceSeconds + "s)";
                color = 0xFFAA55;
            }
            else
            {
                text = "Flight ready: " + maxSeconds + "s available";
                color = 0x55FFFF;
            }
        }

        int x = width / 2 - mc.font.width(text) / 2;
        int y = height - 78;
        graphics.drawString(mc.font, text, x, y, color, true);
    }

    private boolean hasFullSet(Player player)
    {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SLEEPING_GOD_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.SLEEPING_GOD_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.SLEEPING_GOD_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.SLEEPING_GOD_BOOTS.get());
    }

    private boolean hasVesselBoundFullSet(Player player)
    {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.VESSEL_BOUND_SLEEPING_GOD_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.VESSEL_BOUND_SLEEPING_GOD_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.VESSEL_BOUND_SLEEPING_GOD_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.VESSEL_BOUND_SLEEPING_GOD_BOOTS.get());
    }
}
