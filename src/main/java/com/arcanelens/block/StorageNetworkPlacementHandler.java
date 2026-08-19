package com.arcanelens.block;

import com.arcanelens.Config;
import com.arcanelens.capability.ISkillTree;
import com.arcanelens.capability.SkillTreeProvider;
import com.arcanelens.util.StorageNetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.level.BlockEvent;

/**
 * Rejects a Storage Block/Storage Connector placement that would exceed the network cap, or would bridge
 * two clusters already claimed by two different Terminals together - see
 * StorageNetworkManager.checkPlacement. Cancelling here reverts the block to its pre-placement state
 * (standard Forge BlockEvent.EntityPlaceEvent behavior, the same mechanism protection mods rely on).
 * The cap itself is per-placing-player: Config.maxStorageBlocksPerNetwork plus
 * Config.storageBlocksPerExpansionLevel for each level of Storage Network Expansion that player has
 * purchased (see ServerboundPurchaseSkillPacket's STORAGE_NETWORK_EXPANSION case) - a non-player placer
 * (e.g. a dispenser) gets no expansion bonus.
 */
public class StorageNetworkPlacementHandler
{
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event)
    {
        if (!(event.getLevel() instanceof ServerLevel level))
        {
            return;
        }

        Block block = event.getPlacedBlock().getBlock();
        if (!(block instanceof StorageBlock) && !(block instanceof StorageConnectorBlock))
        {
            return;
        }

        int expansionLevel = 0;
        if (event.getEntity() instanceof ServerPlayer placer)
        {
            expansionLevel = placer.getCapability(SkillTreeProvider.CAPABILITY)
                    .map(ISkillTree::getStorageNetworkExpansionLevel).orElse(0);
        }
        int effectiveCap = Config.maxStorageBlocksPerNetwork + expansionLevel * Config.storageBlocksPerExpansionLevel;

        StorageNetworkManager.PlacementResult result = StorageNetworkManager.checkPlacement(level, event.getPos(), effectiveCap);
        if (result == StorageNetworkManager.PlacementResult.OK)
        {
            return;
        }

        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer player)
        {
            String message = result == StorageNetworkManager.PlacementResult.EXCEEDS_CAP
                    ? "That would exceed this network's storage block limit."
                    : "That would connect two separately-claimed storage networks.";
            player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.YELLOW), true);
        }
    }
}
