package com.arcanelens.client;

import com.arcanelens.Config;
import com.arcanelens.item.TheaterHelmetPerkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RenderNameTagEvent;

/** Client-side only, matches this codebase's convention of keeping pure-rendering concerns out of the
 * server-facing item/ package. Hides a Theater-Helmet wearer's nametag beyond
 * Config.theaterNametagHideRadius by blanking the rendered content directly, rather than relying on
 * RenderNameTagEvent's cancelability (simpler, and works regardless of whether the event turns out to
 * be @Cancelable). */
public class TheaterNametagHandler
{
    public static void onRenderNameTag(RenderNameTagEvent event)
    {
        if (!(event.getEntity() instanceof LivingEntity entity) || !TheaterHelmetPerkHandler.isWearingTheaterHelmet(entity))
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player != entity && mc.player.distanceTo(entity) > Config.theaterNametagHideRadius)
        {
            event.setContent(Component.empty());
        }
    }
}
