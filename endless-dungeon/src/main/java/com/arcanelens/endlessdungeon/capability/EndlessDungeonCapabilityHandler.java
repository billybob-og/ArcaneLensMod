package com.arcanelens.endlessdungeon.capability;

import com.arcanelens.endlessdungeon.EndlessDungeonMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public class EndlessDungeonCapabilityHandler
{
    private static final ResourceLocation DUNGEON_RETURN_STATE_CAP_ID = new ResourceLocation(EndlessDungeonMod.MODID, "dungeon_return_state");

    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.register(IDungeonReturnState.class);
    }

    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            event.addCapability(DUNGEON_RETURN_STATE_CAP_ID, new DungeonReturnStateProvider());
        }
    }
}
