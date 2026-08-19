package com.arcanelens.capability;

import net.minecraft.nbt.CompoundTag;

public class SkillTreeImpl implements ISkillTree
{
    private int manaBoostLevel;
    private int cooldownReductionLevel;
    private int costReductionLevel;
    private boolean pocketDimensionUnlocked;
    private int pocketDimensionExpansionLevel;
    private boolean storageSystemUnlocked;
    private int storageNetworkExpansionLevel;
    private boolean arcaneAssemblerUnlocked;
    private int assemblerSpeedLevel;
    private int assemblerFuelEfficiencyLevel;
    private boolean overloadRitualUnlocked;
    private boolean warpedAttunementUnlocked;

    @Override
    public int getManaBoostLevel()
    {
        return manaBoostLevel;
    }

    @Override
    public void setManaBoostLevel(int level)
    {
        manaBoostLevel = Math.max(0, level);
    }

    @Override
    public int getCooldownReductionLevel()
    {
        return cooldownReductionLevel;
    }

    @Override
    public void setCooldownReductionLevel(int level)
    {
        cooldownReductionLevel = Math.max(0, level);
    }

    @Override
    public int getCostReductionLevel()
    {
        return costReductionLevel;
    }

    @Override
    public void setCostReductionLevel(int level)
    {
        costReductionLevel = Math.max(0, level);
    }

    @Override
    public boolean isPocketDimensionUnlocked()
    {
        return pocketDimensionUnlocked;
    }

    @Override
    public void setPocketDimensionUnlocked(boolean unlocked)
    {
        // One-way ratchet, same style as the Faith capability's other permanent unlocks.
        this.pocketDimensionUnlocked = this.pocketDimensionUnlocked || unlocked;
    }

    @Override
    public int getPocketDimensionExpansionLevel()
    {
        return pocketDimensionExpansionLevel;
    }

    @Override
    public void setPocketDimensionExpansionLevel(int level)
    {
        pocketDimensionExpansionLevel = Math.max(0, level);
    }

    @Override
    public boolean isStorageSystemUnlocked()
    {
        return storageSystemUnlocked;
    }

    @Override
    public void setStorageSystemUnlocked(boolean unlocked)
    {
        // One-way ratchet, same style as pocketDimensionUnlocked - an independent branch of the tree,
        // not gated behind Pocket Dimension.
        this.storageSystemUnlocked = this.storageSystemUnlocked || unlocked;
    }

    @Override
    public int getStorageNetworkExpansionLevel()
    {
        return storageNetworkExpansionLevel;
    }

    @Override
    public void setStorageNetworkExpansionLevel(int level)
    {
        storageNetworkExpansionLevel = Math.max(0, level);
    }

    @Override
    public boolean isArcaneAssemblerUnlocked()
    {
        return arcaneAssemblerUnlocked;
    }

    @Override
    public void setArcaneAssemblerUnlocked(boolean unlocked)
    {
        // One-way ratchet, same style as storageSystemUnlocked - an independent branch of the tree.
        this.arcaneAssemblerUnlocked = this.arcaneAssemblerUnlocked || unlocked;
    }

    @Override
    public int getAssemblerSpeedLevel()
    {
        return assemblerSpeedLevel;
    }

    @Override
    public void setAssemblerSpeedLevel(int level)
    {
        assemblerSpeedLevel = Math.max(0, level);
    }

    @Override
    public int getAssemblerFuelEfficiencyLevel()
    {
        return assemblerFuelEfficiencyLevel;
    }

    @Override
    public void setAssemblerFuelEfficiencyLevel(int level)
    {
        assemblerFuelEfficiencyLevel = Math.max(0, level);
    }

    @Override
    public boolean isOverloadRitualUnlocked()
    {
        return overloadRitualUnlocked;
    }

    @Override
    public void setOverloadRitualUnlocked(boolean unlocked)
    {
        // One-way ratchet, same style as storageSystemUnlocked/arcaneAssemblerUnlocked - an independent
        // branch of the tree, since the ritual only ever reads the storage network, never joins it.
        this.overloadRitualUnlocked = this.overloadRitualUnlocked || unlocked;
    }

    @Override
    public boolean isWarpedAttunementUnlocked()
    {
        return warpedAttunementUnlocked;
    }

    @Override
    public void setWarpedAttunementUnlocked(boolean unlocked)
    {
        // One-way ratchet - a child of overloadRitualUnlocked, additionally gated on defeating Broken
        // Vessel at least once (checked where this is purchased, not here).
        this.warpedAttunementUnlocked = this.warpedAttunementUnlocked || unlocked;
    }

    public CompoundTag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ManaBoostLevel", manaBoostLevel);
        tag.putInt("CooldownReductionLevel", cooldownReductionLevel);
        tag.putInt("CostReductionLevel", costReductionLevel);
        tag.putBoolean("PocketDimensionUnlocked", pocketDimensionUnlocked);
        tag.putInt("PocketDimensionExpansionLevel", pocketDimensionExpansionLevel);
        tag.putBoolean("StorageSystemUnlocked", storageSystemUnlocked);
        tag.putInt("StorageNetworkExpansionLevel", storageNetworkExpansionLevel);
        tag.putBoolean("ArcaneAssemblerUnlocked", arcaneAssemblerUnlocked);
        tag.putInt("AssemblerSpeedLevel", assemblerSpeedLevel);
        tag.putInt("AssemblerFuelEfficiencyLevel", assemblerFuelEfficiencyLevel);
        tag.putBoolean("OverloadRitualUnlocked", overloadRitualUnlocked);
        tag.putBoolean("WarpedAttunementUnlocked", warpedAttunementUnlocked);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag)
    {
        manaBoostLevel = tag.getInt("ManaBoostLevel");
        cooldownReductionLevel = tag.getInt("CooldownReductionLevel");
        costReductionLevel = tag.getInt("CostReductionLevel");
        pocketDimensionUnlocked = tag.getBoolean("PocketDimensionUnlocked");
        pocketDimensionExpansionLevel = tag.getInt("PocketDimensionExpansionLevel");
        storageSystemUnlocked = tag.getBoolean("StorageSystemUnlocked");
        storageNetworkExpansionLevel = tag.getInt("StorageNetworkExpansionLevel");
        arcaneAssemblerUnlocked = tag.getBoolean("ArcaneAssemblerUnlocked");
        assemblerSpeedLevel = tag.getInt("AssemblerSpeedLevel");
        assemblerFuelEfficiencyLevel = tag.getInt("AssemblerFuelEfficiencyLevel");
        overloadRitualUnlocked = tag.getBoolean("OverloadRitualUnlocked");
        warpedAttunementUnlocked = tag.getBoolean("WarpedAttunementUnlocked");
    }
}
