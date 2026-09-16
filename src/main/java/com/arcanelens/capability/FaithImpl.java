package com.arcanelens.capability;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class FaithImpl implements IFaith
{
    private final Map<String, Integer> godDropsGranted = new HashMap<>();
    private final Map<String, Integer> godDropsBroken = new HashMap<>();

    private int faith;
    private int sleepingGodDefeats;
    private int brokenVesselDefeats;
    private boolean hungerEyesRevealed;
    private boolean hungerEyesHidden;
    private int blessingBoonLevel;
    private int burdenResistBoonLevel;
    private int faithSinceLastToken;
    private boolean hasGivenHungerTokens;
    private boolean hasReceivedHungersPact;
    private boolean hasClaimedSleepingGodRelic;
    private boolean hasBeenToldRelicLocation;
    private String pledgedGod = "";
    private int pledgeProgress;

    @Override
    public int getFaith()
    {
        return faith;
    }

    @Override
    public void addFaith(int amount)
    {
        faith = Math.max(0, faith + amount);
        // Every genuine Faith gain (from any source) also counts toward the current pledge's progress -
        // see setPledgedGod, which resets this to 0 on every pledge/swap. Keeps pledge-scaling perks
        // (e.g. Fertility's bonus hearts) from instantly maxing out off a large pre-existing Faith total.
        if (amount > 0)
        {
            pledgeProgress = Math.max(0, pledgeProgress + amount);
        }
    }

    @Override
    public void setFaith(int amount)
    {
        faith = Math.max(0, amount);
    }

    @Override
    public int getSleepingGodDefeats()
    {
        return sleepingGodDefeats;
    }

    @Override
    public void incrementSleepingGodDefeats()
    {
        sleepingGodDefeats++;
    }

    @Override
    public void setSleepingGodDefeats(int amount)
    {
        sleepingGodDefeats = Math.max(0, amount);
    }

    @Override
    public int getBrokenVesselDefeats()
    {
        return brokenVesselDefeats;
    }

    @Override
    public void incrementBrokenVesselDefeats()
    {
        brokenVesselDefeats++;
    }

    @Override
    public void setBrokenVesselDefeats(int amount)
    {
        brokenVesselDefeats = Math.max(0, amount);
    }

    @Override
    public boolean isHungerEyesRevealed()
    {
        return hungerEyesRevealed;
    }

    @Override
    public void setHungerEyesRevealed(boolean revealed)
    {
        // One-way ratchet - once revealed, never hide it again even if Faith later drops.
        this.hungerEyesRevealed = this.hungerEyesRevealed || revealed;
    }

    @Override
    public boolean isHungerEyesHidden()
    {
        return hungerEyesHidden;
    }

    @Override
    public void setHungerEyesHidden(boolean hidden)
    {
        // Unlike the reveal itself, this is a plain toggle (Sun and Moon Ward item) - freely reversible.
        this.hungerEyesHidden = hidden;
    }

    @Override
    public int getBlessingBoonLevel()
    {
        return blessingBoonLevel;
    }

    @Override
    public void incrementBlessingBoonLevel()
    {
        blessingBoonLevel++;
    }

    @Override
    public void setBlessingBoonLevel(int amount)
    {
        blessingBoonLevel = Math.max(0, amount);
    }

    @Override
    public int getBurdenResistBoonLevel()
    {
        return burdenResistBoonLevel;
    }

    @Override
    public void incrementBurdenResistBoonLevel()
    {
        burdenResistBoonLevel++;
    }

    @Override
    public void setBurdenResistBoonLevel(int amount)
    {
        burdenResistBoonLevel = Math.max(0, amount);
    }

    @Override
    public int getFaithSinceLastToken()
    {
        return faithSinceLastToken;
    }

    @Override
    public void setFaithSinceLastToken(int amount)
    {
        faithSinceLastToken = Math.max(0, amount);
    }

    @Override
    public boolean hasGivenHungerTokens()
    {
        return hasGivenHungerTokens;
    }

    @Override
    public void setHasGivenHungerTokens(boolean given)
    {
        // One-way ratchet, same style as hungerEyesRevealed - step one of the Hunger's Pact exchange.
        this.hasGivenHungerTokens = this.hasGivenHungerTokens || given;
    }

    @Override
    public boolean hasReceivedHungersPact()
    {
        return hasReceivedHungersPact;
    }

    @Override
    public void setHasReceivedHungersPact(boolean received)
    {
        // One-way ratchet - step two done, the exchange is complete and never repeats.
        this.hasReceivedHungersPact = this.hasReceivedHungersPact || received;
    }

    @Override
    public boolean hasClaimedSleepingGodRelic()
    {
        return hasClaimedSleepingGodRelic;
    }

    @Override
    public void setHasClaimedSleepingGodRelic(boolean claimed)
    {
        // One-way ratchet, same style as hasReceivedHungersPact - the Faith reward is one-time only.
        this.hasClaimedSleepingGodRelic = this.hasClaimedSleepingGodRelic || claimed;
    }

    @Override
    public boolean hasBeenToldRelicLocation()
    {
        return hasBeenToldRelicLocation;
    }

    @Override
    public void setHasBeenToldRelicLocation(boolean told)
    {
        // One-way ratchet - the Hunger Idol's special hint line fires once, same reasoning as
        // hasGivenHungerTokens gating the Pact exchange's own first step.
        this.hasBeenToldRelicLocation = this.hasBeenToldRelicLocation || told;
    }

    @Override
    public String getPledgedGod()
    {
        return pledgedGod;
    }

    @Override
    public void setPledgedGod(String godId)
    {
        // Deliberately NOT a one-way ratchet, unlike every other flag in this class - the whole point
        // of this field is that a player can swap allegiance later (see Pack of the Gods), so this is
        // a plain reassignment rather than the usual this.x = this.x || value idiom.
        this.pledgedGod = godId;
        // Every pledge/swap starts pledge-scaling perks over at 0, regardless of lifetime Faith - see
        // addFaith's comment. Swapping back to a god pledged before doesn't restore old progress.
        this.pledgeProgress = 0;
    }

    @Override
    public int getPledgeProgress()
    {
        return pledgeProgress;
    }

    @Override
    public void setPledgeProgress(int amount)
    {
        pledgeProgress = Math.max(0, amount);
    }

    @Override
    public int getGodDropsGranted(String godId)
    {
        return godDropsGranted.getOrDefault(godId, 0);
    }

    @Override
    public void incrementGodDropsGranted(String godId)
    {
        godDropsGranted.merge(godId, 1, Integer::sum);
    }

    @Override
    public int getGodDropsBroken(String godId)
    {
        return godDropsBroken.getOrDefault(godId, 0);
    }

    @Override
    public void incrementGodDropsBroken(String godId)
    {
        godDropsBroken.merge(godId, 1, Integer::sum);
    }

    public CompoundTag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Faith", faith);
        tag.putInt("SleepingGodDefeats", sleepingGodDefeats);
        tag.putInt("BrokenVesselDefeats", brokenVesselDefeats);
        tag.putBoolean("HungerEyesRevealed", hungerEyesRevealed);
        tag.putBoolean("HungerEyesHidden", hungerEyesHidden);
        tag.putInt("BlessingBoonLevel", blessingBoonLevel);
        tag.putInt("BurdenResistBoonLevel", burdenResistBoonLevel);
        tag.putInt("FaithSinceLastToken", faithSinceLastToken);
        tag.putBoolean("HasGivenHungerTokens", hasGivenHungerTokens);
        tag.putBoolean("HasReceivedHungersPact", hasReceivedHungersPact);
        tag.putBoolean("HasClaimedSleepingGodRelic", hasClaimedSleepingGodRelic);
        tag.putBoolean("HasBeenToldRelicLocation", hasBeenToldRelicLocation);
        tag.putString("PledgedGod", pledgedGod);
        tag.putInt("PledgeProgress", pledgeProgress);
        CompoundTag grantedTag = new CompoundTag();
        godDropsGranted.forEach(grantedTag::putInt);
        tag.put("GodDropsGranted", grantedTag);
        CompoundTag brokenTag = new CompoundTag();
        godDropsBroken.forEach(brokenTag::putInt);
        tag.put("GodDropsBroken", brokenTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag)
    {
        faith = tag.getInt("Faith");
        sleepingGodDefeats = tag.getInt("SleepingGodDefeats");
        brokenVesselDefeats = tag.getInt("BrokenVesselDefeats");
        hungerEyesRevealed = tag.getBoolean("HungerEyesRevealed");
        hungerEyesHidden = tag.getBoolean("HungerEyesHidden");
        blessingBoonLevel = tag.getInt("BlessingBoonLevel");
        burdenResistBoonLevel = tag.getInt("BurdenResistBoonLevel");
        faithSinceLastToken = tag.getInt("FaithSinceLastToken");
        hasGivenHungerTokens = tag.getBoolean("HasGivenHungerTokens");
        hasReceivedHungersPact = tag.getBoolean("HasReceivedHungersPact");
        hasClaimedSleepingGodRelic = tag.getBoolean("HasClaimedSleepingGodRelic");
        hasBeenToldRelicLocation = tag.getBoolean("HasBeenToldRelicLocation");
        pledgedGod = tag.getString("PledgedGod");
        pledgeProgress = tag.getInt("PledgeProgress");
        godDropsGranted.clear();
        CompoundTag grantedTag = tag.getCompound("GodDropsGranted");
        for (String godId : grantedTag.getAllKeys())
        {
            godDropsGranted.put(godId, grantedTag.getInt(godId));
        }
        godDropsBroken.clear();
        CompoundTag brokenTag = tag.getCompound("GodDropsBroken");
        for (String godId : brokenTag.getAllKeys())
        {
            godDropsBroken.put(godId, brokenTag.getInt(godId));
        }
    }
}
