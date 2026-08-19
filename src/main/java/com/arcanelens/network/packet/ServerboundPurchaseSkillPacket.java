package com.arcanelens.network.packet;

import com.arcanelens.ArcaneLens;
import com.arcanelens.Config;
import com.arcanelens.advancement.ModCriteriaTriggers;
import com.arcanelens.capability.KnownSpellsProvider;
import com.arcanelens.capability.KnownSpellsSync;
import com.arcanelens.capability.SkillTreeProvider;
import com.arcanelens.capability.SkillTreeSync;
import com.arcanelens.item.MagicLensItem;
import com.arcanelens.menu.SkillTreeMenu;
import com.arcanelens.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sent when the player clicks a buy button in SkillTreeScreen - validates level/unlock state and Arcane Ink
 * in inventory server-side before applying anything, matching ServerboundChooseHungerBoonPacket's menu-guard
 * pattern. */
public class ServerboundPurchaseSkillPacket
{
    private static final ResourceLocation POCKET_DIMENSION_SPELL_ID = new ResourceLocation(ArcaneLens.MODID, "pocket_dimension");

    private final SkillType skillType;

    public ServerboundPurchaseSkillPacket(SkillType skillType)
    {
        this.skillType = skillType;
    }

    public static void encode(ServerboundPurchaseSkillPacket packet, FriendlyByteBuf buf)
    {
        buf.writeEnum(packet.skillType);
    }

    public static ServerboundPurchaseSkillPacket decode(FriendlyByteBuf buf)
    {
        return new ServerboundPurchaseSkillPacket(buf.readEnum(SkillType.class));
    }

    public static void handle(ServerboundPurchaseSkillPacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !(player.containerMenu instanceof SkillTreeMenu))
            {
                return;
            }

            player.getCapability(SkillTreeProvider.CAPABILITY).ifPresent(cap -> {
                switch (packet.skillType)
                {
                    case MANA_BOOST -> {
                        int level = cap.getManaBoostLevel();
                        if (level >= Config.manaBoostMaxLevel)
                        {
                            return;
                        }
                        int cost = Config.getSkillCost(Config.manaBoostArcaneInkCosts, level);
                        if (!consumeArcaneInk(player, cost))
                        {
                            return;
                        }
                        cap.setManaBoostLevel(level + 1);
                        bumpAllLensesMaxMana(player, Config.manaBoostAmountPerLevel);
                    }
                    case COOLDOWN_REDUCTION -> {
                        // Gated behind Cost Reduction level 2 - the tree's third node, only revealed once
                        // its parent has been leveled up (see SkillTreeScreen's node layout).
                        int level = cap.getCooldownReductionLevel();
                        if (level >= Config.cooldownReductionMaxLevel || cap.getCostReductionLevel() < 2)
                        {
                            return;
                        }
                        int cost = Config.getSkillCost(Config.cooldownReductionArcaneInkCosts, level);
                        if (!consumeArcaneInk(player, cost))
                        {
                            return;
                        }
                        cap.setCooldownReductionLevel(level + 1);
                    }
                    case COST_REDUCTION -> {
                        // Gated behind Mana Boost level 1, alongside Pocket Dimension - the tree's root unlock.
                        int level = cap.getCostReductionLevel();
                        if (level >= Config.costReductionMaxLevel || cap.getManaBoostLevel() < 1)
                        {
                            return;
                        }
                        int cost = Config.getSkillCost(Config.costReductionArcaneInkCosts, level);
                        if (!consumeArcaneInk(player, cost))
                        {
                            return;
                        }
                        cap.setCostReductionLevel(level + 1);
                    }
                    case POCKET_DIMENSION -> {
                        if (cap.isPocketDimensionUnlocked() || cap.getManaBoostLevel() < 1)
                        {
                            return;
                        }
                        if (!consumeArcaneInk(player, Config.pocketDimensionArcaneInkCost))
                        {
                            return;
                        }
                        cap.setPocketDimensionUnlocked(true);
                        player.getCapability(KnownSpellsProvider.CAPABILITY).ifPresent(knownSpells -> knownSpells.learn(POCKET_DIMENSION_SPELL_ID));
                        KnownSpellsSync.syncToClient(player);
                        ModCriteriaTriggers.SPELL_LEARNED.trigger(player, POCKET_DIMENSION_SPELL_ID);
                    }
                    case POCKET_DIMENSION_EXPANSION -> {
                        // Gated behind Pocket Dimension being unlocked - a child of that node in the tree.
                        int level = cap.getPocketDimensionExpansionLevel();
                        if (level >= Config.pocketDimensionExpansionMaxLevel || !cap.isPocketDimensionUnlocked())
                        {
                            return;
                        }
                        int cost = Config.getSkillCost(Config.pocketDimensionExpansionArcaneInkCosts, level);
                        if (!consumeArcaneInk(player, cost))
                        {
                            return;
                        }
                        cap.setPocketDimensionExpansionLevel(level + 1);
                        // The bigger room isn't pasted until the next entry (PocketDimensionPlacer), not right
                        // now - so this is the one moment to warn before it's too late to empty anything out.
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                                "Your Pocket Dimension will be rebuilt bigger next time you enter - "
                                        + "empty any chests first, their contents won't carry over.")
                                .withStyle(net.minecraft.ChatFormatting.YELLOW), false);
                    }
                    case STORAGE_SYSTEM -> {
                        // Independent branch of the tree - gated on Mana Boost level 1 like its siblings
                        // (Cost Reduction/Pocket Dimension), deliberately NOT behind Pocket Dimension.
                        if (cap.isStorageSystemUnlocked() || cap.getManaBoostLevel() < 1)
                        {
                            return;
                        }
                        if (!consumeArcaneInk(player, Config.storageSystemArcaneInkCost))
                        {
                            return;
                        }
                        cap.setStorageSystemUnlocked(true);
                        ModCriteriaTriggers.SKILL_PURCHASED.trigger(player, packet.skillType.name());
                    }
                    case STORAGE_NETWORK_EXPANSION -> {
                        // Gated behind Storage System being unlocked - a child of that node in the tree.
                        int level = cap.getStorageNetworkExpansionLevel();
                        if (level >= Config.storageNetworkExpansionMaxLevel || !cap.isStorageSystemUnlocked())
                        {
                            return;
                        }
                        int cost = Config.getSkillCost(Config.storageNetworkExpansionArcaneInkCosts, level);
                        if (!consumeArcaneInk(player, cost))
                        {
                            return;
                        }
                        cap.setStorageNetworkExpansionLevel(level + 1);
                    }
                    case ARCANE_ASSEMBLER -> {
                        // Independent branch of the tree - gated on Mana Boost level 1 like its siblings
                        // (Cost Reduction/Pocket Dimension/Storage System), not nested under Storage
                        // System, since the storage-network integration is optional/adjacent-only.
                        if (cap.isArcaneAssemblerUnlocked() || cap.getManaBoostLevel() < 1)
                        {
                            return;
                        }
                        if (!consumeArcaneInk(player, Config.assemblerArcaneInkCost))
                        {
                            return;
                        }
                        cap.setArcaneAssemblerUnlocked(true);
                        ModCriteriaTriggers.SKILL_PURCHASED.trigger(player, packet.skillType.name());
                    }
                    case ASSEMBLER_SPEED -> {
                        // Gated behind the Arcane Assembler being unlocked - a child of that node.
                        int level = cap.getAssemblerSpeedLevel();
                        if (level >= Config.assemblerSpeedMaxLevel || !cap.isArcaneAssemblerUnlocked())
                        {
                            return;
                        }
                        int cost = Config.getSkillCost(Config.assemblerSpeedArcaneInkCosts, level);
                        if (!consumeArcaneInk(player, cost))
                        {
                            return;
                        }
                        cap.setAssemblerSpeedLevel(level + 1);
                    }
                    case ASSEMBLER_FUEL_EFFICIENCY -> {
                        // Gated behind the Arcane Assembler being unlocked - a child of that node.
                        int level = cap.getAssemblerFuelEfficiencyLevel();
                        if (level >= Config.assemblerFuelEfficiencyMaxLevel || !cap.isArcaneAssemblerUnlocked())
                        {
                            return;
                        }
                        int cost = Config.getSkillCost(Config.assemblerFuelEfficiencyArcaneInkCosts, level);
                        if (!consumeArcaneInk(player, cost))
                        {
                            return;
                        }
                        cap.setAssemblerFuelEfficiencyLevel(level + 1);
                    }
                    case OVERLOAD_RITUAL -> {
                        // Independent branch of the tree - gated on Mana Boost level 1 like its siblings,
                        // not nested under Storage System, since the ritual only ever reads the storage
                        // network and never joins it (same reasoning already used for Arcane Assembler).
                        if (cap.isOverloadRitualUnlocked() || cap.getManaBoostLevel() < 1)
                        {
                            return;
                        }
                        if (!consumeArcaneInk(player, Config.overloadRitualArcaneInkCost))
                        {
                            return;
                        }
                        cap.setOverloadRitualUnlocked(true);
                        ModCriteriaTriggers.SKILL_PURCHASED.trigger(player, packet.skillType.name());
                    }
                    case WARPED_ATTUNEMENT -> {
                        // Gated behind Overload Ritual being unlocked - a child of that node - plus having
                        // actually defeated Broken Vessel at least once, not merely purchased the ritual.
                        if (cap.isWarpedAttunementUnlocked() || !cap.isOverloadRitualUnlocked())
                        {
                            return;
                        }
                        boolean hasDefeatedVessel = player.getCapability(com.arcanelens.capability.FaithProvider.CAPABILITY)
                                .map(faith -> faith.getBrokenVesselDefeats() >= 1).orElse(false);
                        if (!hasDefeatedVessel)
                        {
                            return;
                        }
                        if (!consumeArcaneInk(player, Config.warpedAttunementArcaneInkCost))
                        {
                            return;
                        }
                        cap.setWarpedAttunementUnlocked(true);
                    }
                }

                // consumeArcaneInk/bumpAllLensesMaxMana mutate ItemStacks in the player's persistent inventory
                // directly - but SkillTreeMenu (the currently open menu) has no Slots watching them, so the
                // normal per-tick slot-diff sync never notices while it's open. Forcing player.inventoryMenu
                // (which does track every inventory slot) to broadcast right now catches the diff immediately,
                // instead of leaving the client's Arcane Ink count/lens tooltip stale until the screen closes.
                player.inventoryMenu.broadcastChanges();

                SkillTreeSync.syncToClient(player);
            });
        });
        ctx.setPacketHandled(true);
    }

    private static boolean consumeArcaneInk(ServerPlayer player, int amount)
    {
        Item ink = ModItems.ARCANE_INK.get();
        var items = player.getInventory().items;

        int available = 0;
        for (int i = 0; i < items.size(); i++)
        {
            ItemStack stack = items.get(i);
            if (stack.is(ink))
            {
                available += stack.getCount();
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
            if (!stack.is(ink))
            {
                continue;
            }
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;
        }
        return true;
    }

    private static void bumpAllLensesMaxMana(ServerPlayer player, int amount)
    {
        var items = player.getInventory().items;
        for (int i = 0; i < items.size(); i++)
        {
            ItemStack stack = items.get(i);
            if (stack.is(ModItems.MAGIC_LENS.get()))
            {
                MagicLensItem.setMaxMana(stack, MagicLensItem.getMaxMana(stack) + amount);
            }
        }

        // Inventory.items (the 36 hotbar+main slots) doesn't include the offhand - a lens parked there while
        // mining with a tool in the main hand would otherwise get silently skipped on every purchase.
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(ModItems.MAGIC_LENS.get()))
        {
            MagicLensItem.setMaxMana(offhand, MagicLensItem.getMaxMana(offhand) + amount);
        }
    }
}
