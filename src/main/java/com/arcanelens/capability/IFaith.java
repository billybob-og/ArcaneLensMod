package com.arcanelens.capability;

public interface IFaith
{
    int getFaith();

    void addFaith(int amount);

    void setFaith(int amount);

    int getSleepingGodDefeats();

    void incrementSleepingGodDefeats();

    void setSleepingGodDefeats(int amount);

    int getBrokenVesselDefeats();

    void incrementBrokenVesselDefeats();

    void setBrokenVesselDefeats(int amount);

    boolean isHungerEyesRevealed();

    void setHungerEyesRevealed(boolean revealed);

    boolean isHungerEyesHidden();

    void setHungerEyesHidden(boolean hidden);

    int getBlessingBoonLevel();

    void incrementBlessingBoonLevel();

    void setBlessingBoonLevel(int amount);

    int getBurdenResistBoonLevel();

    void incrementBurdenResistBoonLevel();

    void setBurdenResistBoonLevel(int amount);

    int getFaithSinceLastToken();

    void setFaithSinceLastToken(int amount);

    boolean hasGivenHungerTokens();

    void setHasGivenHungerTokens(boolean given);

    boolean hasReceivedHungersPact();

    void setHasReceivedHungersPact(boolean received);

    boolean hasClaimedSleepingGodRelic();

    void setHasClaimedSleepingGodRelic(boolean claimed);

    boolean hasBeenToldRelicLocation();

    void setHasBeenToldRelicLocation(boolean told);

    String getPledgedGod();

    void setPledgedGod(String godId);

    int getPledgeProgress();

    /** Plain setter, clamped at 0 - used only for sync/snapshot restoration. Gameplay code should never
     * call this directly; addFaith() bumps this automatically, and setPledgedGod() resets it to 0. */
    void setPledgeProgress(int amount);
}
