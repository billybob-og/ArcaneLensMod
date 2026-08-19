package com.arcanelens.capability;

public interface ISkillTree
{
    int getManaBoostLevel();

    void setManaBoostLevel(int level);

    int getCooldownReductionLevel();

    void setCooldownReductionLevel(int level);

    int getCostReductionLevel();

    void setCostReductionLevel(int level);

    boolean isPocketDimensionUnlocked();

    void setPocketDimensionUnlocked(boolean unlocked);

    int getPocketDimensionExpansionLevel();

    void setPocketDimensionExpansionLevel(int level);

    boolean isStorageSystemUnlocked();

    void setStorageSystemUnlocked(boolean unlocked);

    int getStorageNetworkExpansionLevel();

    void setStorageNetworkExpansionLevel(int level);

    boolean isArcaneAssemblerUnlocked();

    void setArcaneAssemblerUnlocked(boolean unlocked);

    int getAssemblerSpeedLevel();

    void setAssemblerSpeedLevel(int level);

    int getAssemblerFuelEfficiencyLevel();

    void setAssemblerFuelEfficiencyLevel(int level);

    boolean isOverloadRitualUnlocked();

    void setOverloadRitualUnlocked(boolean unlocked);

    boolean isWarpedAttunementUnlocked();

    void setWarpedAttunementUnlocked(boolean unlocked);
}
