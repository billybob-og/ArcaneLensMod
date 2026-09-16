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

    /** How many times this god's unique boss drop has ever been granted to this player, keyed by god
     * id. Compared against getGodDropsBroken(godId) to decide whether a fresh kill grants another copy
     * (granted <= broken means no live copy is known to still exist) - see AbstractGodBossEntity. */
    int getGodDropsGranted(String godId);

    void incrementGodDropsGranted(String godId);

    /** How many times this god's drop has been confirmed lost (currently: broken via durability - see
     * GodDropLossHandler). Fire/lava/void loss are prevented outright rather than tracked here (fire
     * resistance on the item + GodDropVoidRescueHandler), so durability is the main way this climbs. */
    int getGodDropsBroken(String godId);

    void incrementGodDropsBroken(String godId);
}
