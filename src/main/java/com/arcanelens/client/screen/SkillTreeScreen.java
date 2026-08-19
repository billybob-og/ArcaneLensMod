package com.arcanelens.client.screen;

import com.arcanelens.ArcaneLens;
import com.arcanelens.Config;
import com.arcanelens.capability.SkillTreeProvider;
import com.arcanelens.menu.SkillTreeMenu;
import com.arcanelens.network.NetworkHandler;
import com.arcanelens.network.packet.ServerboundPurchaseSkillPacket;
import com.arcanelens.network.packet.SkillType;
import com.arcanelens.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * A small tree rather than a flat list: Mana Boost is the root and always visible; buying it once reveals
 * Cost Reduction and Pocket Dimension side by side beneath it; buying Cost Reduction twice reveals Cooldown
 * Reduction beneath that. Node visibility doubles as the interaction gate (AbstractWidget skips both
 * rendering and clicks while invisible), and the server independently re-checks the same prerequisites in
 * ServerboundPurchaseSkillPacket so a hidden node can't be bought by spoofing the packet directly.
 */
public class SkillTreeScreen extends AbstractContainerScreen<SkillTreeMenu>
{
    private static final ResourceLocation BACKGROUND = new ResourceLocation(ArcaneLens.MODID, "textures/gui/skill_tree/background.png");
    private static final ResourceLocation MANA_BOOST_ICON = new ResourceLocation(ArcaneLens.MODID, "textures/gui/skill_tree/mana_boost.png");
    private static final ResourceLocation COOLDOWN_ICON = new ResourceLocation(ArcaneLens.MODID, "textures/gui/skill_tree/cooldown_reduction.png");
    private static final ResourceLocation POCKET_DIMENSION_ICON = new ResourceLocation(ArcaneLens.MODID, "textures/gui/skill_tree/pocket_dimension.png");
    // No dedicated Cost Reduction sprite yet - reuse Arcane Ink's own item icon.
    private static final ResourceLocation COST_REDUCTION_ICON = new ResourceLocation(ArcaneLens.MODID, "textures/item/arcane_ink.png");
    // Reuses the Living Chest's own block texture, same as STORAGE_NETWORK_EXPANSION_ICON below.
    private static final ResourceLocation STORAGE_SYSTEM_ICON = new ResourceLocation(ArcaneLens.MODID, "textures/block/storage_block.png");
    // Reuses the Living Chest's own block texture - a fitting icon for a "more chests per network" upgrade.
    private static final ResourceLocation STORAGE_NETWORK_EXPANSION_ICON = new ResourceLocation(ArcaneLens.MODID, "textures/block/storage_block.png");
    // No dedicated Arcane Assembler sprite yet - reuse its own placeholder block texture (crafting table
    // top), same "reuse the block's own texture" convention as the storage nodes above.
    private static final ResourceLocation ARCANE_ASSEMBLER_ICON = new ResourceLocation("minecraft", "textures/block/crafting_table_top.png");
    // Reuses the Warped Catalyst's own item icon - a real dedicated sprite exists for it (unlike several of
    // the reused-texture nodes above), it just doesn't have a *separate* skill-tree-specific one yet.
    private static final ResourceLocation OVERLOAD_RITUAL_ICON = new ResourceLocation(ArcaneLens.MODID, "textures/item/warped_catalyst.png");
    // Reuses the Warped Attunement Template's own item icon - a real dedicated sprite, same "reuse the
    // drop's own icon" convention as OVERLOAD_RITUAL_ICON above.
    private static final ResourceLocation WARPED_ATTUNEMENT_ICON = new ResourceLocation(ArcaneLens.MODID, "textures/item/warped_attunement_template.png");

    private static final int NODE_SIZE = 36;
    private static final int LINE_COLOR = 0xFFAAAAAA;

    private SkillNodeButton manaBoostNode;
    private SkillNodeButton costReductionNode;
    private SkillNodeButton pocketDimensionNode;
    private SkillNodeButton cooldownReductionNode;
    private SkillNodeButton pocketDimensionExpansionNode;
    private SkillNodeButton storageSystemNode;
    private SkillNodeButton storageNetworkExpansionNode;
    private SkillNodeButton arcaneAssemblerNode;
    private SkillNodeButton assemblerSpeedNode;
    private SkillNodeButton assemblerFuelEfficiencyNode;
    private SkillNodeButton overloadRitualNode;
    private SkillNodeButton warpedAttunementNode;

    public SkillTreeScreen(SkillTreeMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        // Widened from 240 to fit a 5th root-tier sibling (Overload Ritual) alongside Cost Reduction/
        // Storage System/Arcane Assembler/Pocket Dimension without cramming them together - same
        // constants-only change already used when Arcane Assembler was added (200 -> 240).
        this.imageWidth = 300;
        this.imageHeight = 280;
    }

    @Override
    protected void init()
    {
        super.init();
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        int centerX = left + this.imageWidth / 2;

        manaBoostNode = addNode(centerX - NODE_SIZE / 2, top + 30, MANA_BOOST_ICON,
                "Mana Boost", SkillType.MANA_BOOST);
        // Root-tier row: 5 siblings, all gated on Mana Boost level 1, evenly spaced (36px nodes, 20px
        // gaps) and centered under the root - centerX-130 to centerX+130, symmetric.
        costReductionNode = addNode(centerX - 130, top + 96, COST_REDUCTION_ICON,
                "Cost Reduction", SkillType.COST_REDUCTION);
        // Independent branch (deliberately not nested under Pocket Dimension).
        storageSystemNode = addNode(centerX - 74, top + 96, STORAGE_SYSTEM_ICON,
                "Storage System", SkillType.STORAGE_SYSTEM);
        // Independent branch (deliberately not nested under Storage System) - the storage-network
        // integration is optional/adjacent-only, not a hard dependency.
        arcaneAssemblerNode = addNode(centerX - 18, top + 96, ARCANE_ASSEMBLER_ICON,
                "Arcane Assembler", SkillType.ARCANE_ASSEMBLER);
        pocketDimensionNode = addNode(centerX + 38, top + 96, POCKET_DIMENSION_ICON,
                "Pocket Dimension", SkillType.POCKET_DIMENSION);
        // Independent branch (deliberately not nested under Storage System) - the ritual only ever reads
        // the storage network, never joins it.
        overloadRitualNode = addNode(centerX + 94, top + 96, OVERLOAD_RITUAL_ICON,
                "Overload Ritual", SkillType.OVERLOAD_RITUAL);

        cooldownReductionNode = addNode(costReductionNode.getX(), top + 162, COOLDOWN_ICON,
                "Cooldown Reduction", SkillType.COOLDOWN_REDUCTION);
        // No dedicated sprite for this one either - reuses the Pocket Dimension node's own Portal icon.
        pocketDimensionExpansionNode = addNode(pocketDimensionNode.getX(), top + 228, POCKET_DIMENSION_ICON,
                "Pocket Dimension Expansion", SkillType.POCKET_DIMENSION_EXPANSION);
        // Child of Storage System, straight down under its parent's new (off-center) column.
        storageNetworkExpansionNode = addNode(storageSystemNode.getX(), top + 162, STORAGE_NETWORK_EXPANSION_ICON,
                "Storage Network Expansion", SkillType.STORAGE_NETWORK_EXPANSION);
        // Two children of Arcane Assembler, stacked in its own column (empty at this row otherwise) since
        // it has 2 children rather than 1 like its siblings' single-child branches.
        assemblerSpeedNode = addNode(arcaneAssemblerNode.getX(), top + 162, ARCANE_ASSEMBLER_ICON,
                "Assembler Speed", SkillType.ASSEMBLER_SPEED);
        assemblerFuelEfficiencyNode = addNode(arcaneAssemblerNode.getX(), top + 204, ARCANE_ASSEMBLER_ICON,
                "Assembler Fuel Efficiency", SkillType.ASSEMBLER_FUEL_EFFICIENCY);
        // Child of Overload Ritual, straight down under its parent's column - same single-child shape as
        // Cooldown Reduction/Storage Network Expansion. Stays hidden (not just greyed out) until Broken
        // Vessel has actually been defeated once, on top of the parent unlock - see refreshNodes().
        warpedAttunementNode = addNode(overloadRitualNode.getX(), top + 162, WARPED_ATTUNEMENT_ICON,
                "Warped Attunement", SkillType.WARPED_ATTUNEMENT);

        refreshNodes();
    }

    private SkillNodeButton addNode(int x, int y, ResourceLocation icon, String name, SkillType type)
    {
        return this.addRenderableWidget(new SkillNodeButton(x, y, icon, name, b -> buy(type)));
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();
        refreshNodes();
    }

    private void buy(SkillType type)
    {
        NetworkHandler.CHANNEL.sendToServer(new ServerboundPurchaseSkillPacket(type));
    }

    private void refreshNodes()
    {
        if (this.minecraft == null || this.minecraft.player == null)
        {
            return;
        }

        int ink = this.minecraft.player.getInventory().countItem(ModItems.ARCANE_INK.get());
        boolean brokenVesselDefeated = this.minecraft.player.getCapability(com.arcanelens.capability.FaithProvider.CAPABILITY)
                .map(faith -> faith.getBrokenVesselDefeats() >= 1).orElse(false);
        this.minecraft.player.getCapability(SkillTreeProvider.CAPABILITY).ifPresent(cap -> {
            int manaLevel = cap.getManaBoostLevel();
            int costLevel = cap.getCostReductionLevel();

            manaBoostNode.visible = true;
            costReductionNode.visible = manaLevel >= 1;
            pocketDimensionNode.visible = manaLevel >= 1;
            cooldownReductionNode.visible = costLevel >= 2;
            pocketDimensionExpansionNode.visible = cap.isPocketDimensionUnlocked();
            storageSystemNode.visible = manaLevel >= 1;
            storageNetworkExpansionNode.visible = cap.isStorageSystemUnlocked();
            arcaneAssemblerNode.visible = manaLevel >= 1;
            assemblerSpeedNode.visible = cap.isArcaneAssemblerUnlocked();
            assemblerFuelEfficiencyNode.visible = cap.isArcaneAssemblerUnlocked();
            overloadRitualNode.visible = manaLevel >= 1;
            warpedAttunementNode.visible = cap.isOverloadRitualUnlocked() && brokenVesselDefeated;

            boolean manaMaxed = manaLevel >= Config.manaBoostMaxLevel;
            int manaCost = Config.getSkillCost(Config.manaBoostArcaneInkCosts, manaLevel);
            manaBoostNode.update(manaLevel + "/" + Config.manaBoostMaxLevel, !manaMaxed && ink >= manaCost,
                    manaMaxed ? "Maxed" : "Cost: " + manaCost + " Arcane Ink");

            boolean costMaxed = costLevel >= Config.costReductionMaxLevel;
            int costCost = Config.getSkillCost(Config.costReductionArcaneInkCosts, costLevel);
            costReductionNode.update(costLevel + "/" + Config.costReductionMaxLevel, !costMaxed && ink >= costCost,
                    costMaxed ? "Maxed" : "Cost: " + costCost + " Arcane Ink");

            boolean pocketUnlocked = cap.isPocketDimensionUnlocked();
            pocketDimensionNode.update(pocketUnlocked ? "1/1" : "0/1", !pocketUnlocked && ink >= Config.pocketDimensionArcaneInkCost,
                    pocketUnlocked ? "Unlocked" : "Cost: " + Config.pocketDimensionArcaneInkCost + " Arcane Ink");

            int cooldownLevel = cap.getCooldownReductionLevel();
            boolean cooldownMaxed = cooldownLevel >= Config.cooldownReductionMaxLevel;
            int cooldownCost = Config.getSkillCost(Config.cooldownReductionArcaneInkCosts, cooldownLevel);
            cooldownReductionNode.update(cooldownLevel + "/" + Config.cooldownReductionMaxLevel, !cooldownMaxed && ink >= cooldownCost,
                    cooldownMaxed ? "Maxed" : "Cost: " + cooldownCost + " Arcane Ink");

            int expansionLevel = cap.getPocketDimensionExpansionLevel();
            boolean expansionMaxed = expansionLevel >= Config.pocketDimensionExpansionMaxLevel;
            int expansionCost = Config.getSkillCost(Config.pocketDimensionExpansionArcaneInkCosts, expansionLevel);
            // The new (bigger) room structure gets pasted fresh over the old one - anything placed or stored
            // inside, including chest contents, is overwritten. Warn before the purchase, since there's no
            // undo once it's bought.
            pocketDimensionExpansionNode.update(expansionLevel + "/" + Config.pocketDimensionExpansionMaxLevel,
                    !expansionMaxed && ink >= expansionCost,
                    (expansionMaxed ? "Maxed" : "Cost: " + expansionCost + " Arcane Ink")
                            + "\nWarning: resets the room - empty any chests first!");

            boolean storageUnlocked = cap.isStorageSystemUnlocked();
            storageSystemNode.update(storageUnlocked ? "1/1" : "0/1", !storageUnlocked && ink >= Config.storageSystemArcaneInkCost,
                    storageUnlocked ? "Unlocked" : "Cost: " + Config.storageSystemArcaneInkCost + " Arcane Ink");

            int storageExpansionLevel = cap.getStorageNetworkExpansionLevel();
            boolean storageExpansionMaxed = storageExpansionLevel >= Config.storageNetworkExpansionMaxLevel;
            int storageExpansionCost = Config.getSkillCost(Config.storageNetworkExpansionArcaneInkCosts, storageExpansionLevel);
            storageNetworkExpansionNode.update(storageExpansionLevel + "/" + Config.storageNetworkExpansionMaxLevel,
                    !storageExpansionMaxed && ink >= storageExpansionCost,
                    storageExpansionMaxed ? "Maxed" : "Cost: " + storageExpansionCost + " Arcane Ink");

            boolean assemblerUnlocked = cap.isArcaneAssemblerUnlocked();
            arcaneAssemblerNode.update(assemblerUnlocked ? "1/1" : "0/1", !assemblerUnlocked && ink >= Config.assemblerArcaneInkCost,
                    assemblerUnlocked ? "Unlocked" : "Cost: " + Config.assemblerArcaneInkCost + " Arcane Ink");

            int assemblerSpeedLevel = cap.getAssemblerSpeedLevel();
            boolean assemblerSpeedMaxed = assemblerSpeedLevel >= Config.assemblerSpeedMaxLevel;
            int assemblerSpeedCost = Config.getSkillCost(Config.assemblerSpeedArcaneInkCosts, assemblerSpeedLevel);
            assemblerSpeedNode.update(assemblerSpeedLevel + "/" + Config.assemblerSpeedMaxLevel,
                    !assemblerSpeedMaxed && ink >= assemblerSpeedCost,
                    assemblerSpeedMaxed ? "Maxed" : "Cost: " + assemblerSpeedCost + " Arcane Ink");

            int fuelEfficiencyLevel = cap.getAssemblerFuelEfficiencyLevel();
            boolean fuelEfficiencyMaxed = fuelEfficiencyLevel >= Config.assemblerFuelEfficiencyMaxLevel;
            int fuelEfficiencyCost = Config.getSkillCost(Config.assemblerFuelEfficiencyArcaneInkCosts, fuelEfficiencyLevel);
            assemblerFuelEfficiencyNode.update(fuelEfficiencyLevel + "/" + Config.assemblerFuelEfficiencyMaxLevel,
                    !fuelEfficiencyMaxed && ink >= fuelEfficiencyCost,
                    fuelEfficiencyMaxed ? "Maxed" : "Cost: " + fuelEfficiencyCost + " Arcane Ink");

            boolean overloadRitualUnlocked = cap.isOverloadRitualUnlocked();
            overloadRitualNode.update(overloadRitualUnlocked ? "1/1" : "0/1",
                    !overloadRitualUnlocked && ink >= Config.overloadRitualArcaneInkCost,
                    overloadRitualUnlocked ? "Unlocked" : "Cost: " + Config.overloadRitualArcaneInkCost + " Arcane Ink");

            boolean warpedAttunementUnlocked = cap.isWarpedAttunementUnlocked();
            warpedAttunementNode.update(warpedAttunementUnlocked ? "1/1" : "0/1",
                    !warpedAttunementUnlocked && ink >= Config.warpedAttunementArcaneInkCost,
                    warpedAttunementUnlocked ? "Unlocked" : "Cost: " + Config.warpedAttunementArcaneInkCost + " Arcane Ink");
        });
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        // No slots on this screen - the default "Inventory" label has nowhere sensible to sit.
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        for (int x = 0; x < this.imageWidth; x += 16)
        {
            for (int y = 0; y < this.imageHeight; y += 16)
            {
                int w = Math.min(16, this.imageWidth - x);
                int h = Math.min(16, this.imageHeight - y);
                graphics.blit(BACKGROUND, left + x, top + y, 0, 0, w, h, 16, 16);
            }
        }

        drawConnections(graphics);
    }

    private void drawConnections(GuiGraphics graphics)
    {
        int rootCenterX = manaBoostNode.getX() + NODE_SIZE / 2;
        int rootBottom = manaBoostNode.getY() + NODE_SIZE;
        int costCenterX = costReductionNode.getX() + NODE_SIZE / 2;
        int storageCenterX = storageSystemNode.getX() + NODE_SIZE / 2;
        int assemblerCenterX = arcaneAssemblerNode.getX() + NODE_SIZE / 2;
        int pocketCenterX = pocketDimensionNode.getX() + NODE_SIZE / 2;
        int overloadRitualCenterX = overloadRitualNode.getX() + NODE_SIZE / 2;
        int rowTop = costReductionNode.getY();
        int midY = rootBottom + (rowTop - rootBottom) / 2;

        // All 5 root-tier siblings share the identical Mana Boost level 1 gate, so they're always
        // visible/invisible together - one representative check covers the shared spine.
        if (costReductionNode.visible)
        {
            verticalLine(graphics, rootCenterX, rootBottom, midY);
            horizontalLine(graphics, costCenterX, overloadRitualCenterX, midY);
            verticalLine(graphics, costCenterX, midY, rowTop);
            verticalLine(graphics, storageCenterX, midY, rowTop);
            verticalLine(graphics, assemblerCenterX, midY, rowTop);
            verticalLine(graphics, pocketCenterX, midY, rowTop);
            verticalLine(graphics, overloadRitualCenterX, midY, rowTop);
        }

        if (cooldownReductionNode.visible)
        {
            int childCenterX = cooldownReductionNode.getX() + NODE_SIZE / 2;
            verticalLine(graphics, childCenterX, costReductionNode.getY() + NODE_SIZE, cooldownReductionNode.getY());
        }

        if (pocketDimensionExpansionNode.visible)
        {
            int childCenterX = pocketDimensionExpansionNode.getX() + NODE_SIZE / 2;
            verticalLine(graphics, childCenterX, pocketDimensionNode.getY() + NODE_SIZE, pocketDimensionExpansionNode.getY());
        }

        if (storageNetworkExpansionNode.visible)
        {
            int childCenterX = storageNetworkExpansionNode.getX() + NODE_SIZE / 2;
            verticalLine(graphics, childCenterX, storageSystemNode.getY() + NODE_SIZE, storageNetworkExpansionNode.getY());
        }

        if (assemblerSpeedNode.visible)
        {
            verticalLine(graphics, assemblerCenterX, arcaneAssemblerNode.getY() + NODE_SIZE, assemblerSpeedNode.getY());
        }

        if (assemblerFuelEfficiencyNode.visible)
        {
            verticalLine(graphics, assemblerCenterX, assemblerSpeedNode.getY() + NODE_SIZE, assemblerFuelEfficiencyNode.getY());
        }

        if (warpedAttunementNode.visible)
        {
            verticalLine(graphics, overloadRitualCenterX, overloadRitualNode.getY() + NODE_SIZE, warpedAttunementNode.getY());
        }
    }

    private void verticalLine(GuiGraphics graphics, int x, int yFrom, int yTo)
    {
        graphics.fill(x - 1, yFrom, x + 1, yTo, LINE_COLOR);
    }

    private void horizontalLine(GuiGraphics graphics, int xFrom, int xTo, int y)
    {
        graphics.fill(xFrom, y - 1, xTo, y + 1, LINE_COLOR);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        int ink = this.minecraft.player == null ? 0 : this.minecraft.player.getInventory().countItem(ModItems.ARCANE_INK.get());
        graphics.drawString(this.font, "Arcane Ink: " + ink, left + 10, top + 8, 0xFFFFFF, true);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    /** A square node button - icon centered, a small level counter along the bottom edge, no button label. */
    private static class SkillNodeButton extends Button
    {
        private final ResourceLocation icon;

        SkillNodeButton(int x, int y, ResourceLocation icon, String name, OnPress onPress)
        {
            super(x, y, NODE_SIZE, NODE_SIZE, Component.literal(name), onPress, Button.DEFAULT_NARRATION);
            this.icon = icon;
            this.counterText = "";
        }

        private String counterText;

        void update(String counterText, boolean active, String statusLine)
        {
            this.counterText = counterText;
            this.active = active;
            this.setTooltip(Tooltip.create(Component.literal(this.getMessage().getString() + "\n" + statusLine)));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
        {
            int x = this.getX();
            int y = this.getY();
            int w = this.getWidth();
            int h = this.getHeight();
            boolean hovered = this.isHoveredOrFocused();

            int borderColor = !this.active ? 0xFF444444 : (hovered ? 0xFFFFFFFF : 0xFFAAAAAA);
            int bgColor = !this.active ? 0xFF1A1A2E : 0xFF2A2A4E;

            graphics.fill(x, y, x + w, y + h, borderColor);
            graphics.fill(x + 2, y + 2, x + w - 2, y + h - 2, bgColor);

            int iconSize = 16;
            graphics.blit(icon, x + (w - iconSize) / 2, y + 4, 0, 0, iconSize, iconSize, iconSize, iconSize);

            if (!counterText.isEmpty())
            {
                var font = Minecraft.getInstance().font;
                int tx = x + w / 2 - font.width(counterText) / 2;
                int ty = y + h - 11;
                graphics.drawString(font, counterText, tx, ty, this.active ? 0xFFFFFF : 0x888888, true);
            }
        }
    }
}
