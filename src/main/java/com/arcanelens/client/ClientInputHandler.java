package com.arcanelens.client;

import com.arcanelens.ArcaneLens;
import com.arcanelens.item.MagicLensItem;
import com.arcanelens.network.NetworkHandler;
import com.arcanelens.network.packet.ServerboundCastSpellPacket;
import com.arcanelens.network.packet.ServerboundCycleSpellPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ArcaneLens.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientInputHandler
{
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
        {
            return;
        }

        boolean holdingLens = isHoldingLens(mc.player.getMainHandItem()) || isHoldingLens(mc.player.getOffhandItem());

        while (KeyBindings.CYCLE_SPELL.consumeClick())
        {
            if (holdingLens)
            {
                NetworkHandler.CHANNEL.sendToServer(new ServerboundCycleSpellPacket());
            }
        }

        while (KeyBindings.CAST_SPELL.consumeClick())
        {
            if (holdingLens)
            {
                NetworkHandler.CHANNEL.sendToServer(new ServerboundCastSpellPacket());
            }
        }
    }

    private static boolean isHoldingLens(ItemStack stack)
    {
        return stack.getItem() instanceof MagicLensItem;
    }
}
