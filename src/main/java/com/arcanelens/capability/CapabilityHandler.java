package com.arcanelens.capability;

import com.arcanelens.ArcaneLens;
import com.arcanelens.worldgen.ModDimensions;
import com.arcanelens.worldgen.PocketDimensionPlacer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class CapabilityHandler
{
    private static final ResourceLocation KNOWN_SPELLS_CAP_ID = new ResourceLocation(ArcaneLens.MODID, "known_spells");
    private static final ResourceLocation FAITH_CAP_ID = new ResourceLocation(ArcaneLens.MODID, "faith");
    private static final ResourceLocation SKILL_TREE_CAP_ID = new ResourceLocation(ArcaneLens.MODID, "skill_tree");
    private static final ResourceLocation POCKET_DIMENSION_STATE_CAP_ID = new ResourceLocation(ArcaneLens.MODID, "pocket_dimension_state");
    private static final String DEATH_SNAPSHOT_KEY = "arcanelensKnownSpellsSnapshot";
    private static final String FAITH_SNAPSHOT_KEY = "arcanelensFaithSnapshot";
    private static final String SKILL_TREE_SNAPSHOT_KEY = "arcanelensSkillTreeSnapshot";

    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.register(IKnownSpells.class);
        event.register(IFaith.class);
        event.register(ISkillTree.class);
        event.register(IPocketDimensionState.class);
    }

    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            event.addCapability(KNOWN_SPELLS_CAP_ID, new KnownSpellsProvider());
            event.addCapability(FAITH_CAP_ID, new FaithProvider());
            event.addCapability(SKILL_TREE_CAP_ID, new SkillTreeProvider());
            event.addCapability(POCKET_DIMENSION_STATE_CAP_ID, new PocketDimensionStateProvider());
        }
    }

    // Capabilities are invalidated during entity removal, which happens before PlayerEvent.Clone fires -
    // so by the time Clone runs, event.getOriginal()'s capability is already gone. Snapshot into the
    // (capability-independent) persistent data at the moment of death instead, while it's still valid.
    public static void onPlayerDeath(LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
        {
            return;
        }
        player.getCapability(KnownSpellsProvider.CAPABILITY).ifPresent(cap -> {
            ListTag list = new ListTag();
            for (ResourceLocation id : cap.getKnownSpells())
            {
                list.add(StringTag.valueOf(id.toString()));
            }
            CompoundTag snapshot = new CompoundTag();
            snapshot.put("KnownSpells", list);
            player.getPersistentData().put(DEATH_SNAPSHOT_KEY, snapshot);
        });
        player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> {
            CompoundTag snapshot = new CompoundTag();
            snapshot.putInt("Faith", cap.getFaith());
            snapshot.putInt("SleepingGodDefeats", cap.getSleepingGodDefeats());
            snapshot.putInt("BrokenVesselDefeats", cap.getBrokenVesselDefeats());
            snapshot.putBoolean("HungerEyesRevealed", cap.isHungerEyesRevealed());
            snapshot.putBoolean("HungerEyesHidden", cap.isHungerEyesHidden());
            snapshot.putInt("BlessingBoonLevel", cap.getBlessingBoonLevel());
            snapshot.putInt("BurdenResistBoonLevel", cap.getBurdenResistBoonLevel());
            snapshot.putInt("FaithSinceLastToken", cap.getFaithSinceLastToken());
            snapshot.putBoolean("HasGivenHungerTokens", cap.hasGivenHungerTokens());
            snapshot.putBoolean("HasReceivedHungersPact", cap.hasReceivedHungersPact());
            snapshot.putBoolean("HasClaimedSleepingGodRelic", cap.hasClaimedSleepingGodRelic());
            snapshot.putBoolean("HasBeenToldRelicLocation", cap.hasBeenToldRelicLocation());
            snapshot.putString("PledgedGod", cap.getPledgedGod());
            snapshot.putInt("PledgeProgress", cap.getPledgeProgress());
            player.getPersistentData().put(FAITH_SNAPSHOT_KEY, snapshot);
        });
        player.getCapability(SkillTreeProvider.CAPABILITY).ifPresent(cap -> {
            CompoundTag snapshot = new CompoundTag();
            snapshot.putInt("ManaBoostLevel", cap.getManaBoostLevel());
            snapshot.putInt("CooldownReductionLevel", cap.getCooldownReductionLevel());
            snapshot.putInt("CostReductionLevel", cap.getCostReductionLevel());
            snapshot.putBoolean("PocketDimensionUnlocked", cap.isPocketDimensionUnlocked());
            snapshot.putInt("PocketDimensionExpansionLevel", cap.getPocketDimensionExpansionLevel());
            snapshot.putBoolean("StorageSystemUnlocked", cap.isStorageSystemUnlocked());
            snapshot.putInt("StorageNetworkExpansionLevel", cap.getStorageNetworkExpansionLevel());
            snapshot.putBoolean("ArcaneAssemblerUnlocked", cap.isArcaneAssemblerUnlocked());
            snapshot.putInt("AssemblerSpeedLevel", cap.getAssemblerSpeedLevel());
            snapshot.putInt("AssemblerFuelEfficiencyLevel", cap.getAssemblerFuelEfficiencyLevel());
            snapshot.putBoolean("OverloadRitualUnlocked", cap.isOverloadRitualUnlocked());
            snapshot.putBoolean("WarpedAttunementUnlocked", cap.isWarpedAttunementUnlocked());
            player.getPersistentData().put(SKILL_TREE_SNAPSHOT_KEY, snapshot);
        });
    }

    public static void onPlayerClone(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath())
        {
            return;
        }

        CompoundTag snapshot = event.getOriginal().getPersistentData().getCompound(DEATH_SNAPSHOT_KEY);
        ListTag list = snapshot.getList("KnownSpells", Tag.TAG_STRING);

        event.getEntity().getCapability(KnownSpellsProvider.CAPABILITY).ifPresent(newCap -> {
            for (int i = 0; i < list.size(); i++)
            {
                newCap.learn(new ResourceLocation(list.getString(i)));
            }
        });

        CompoundTag faithSnapshot = event.getOriginal().getPersistentData().getCompound(FAITH_SNAPSHOT_KEY);
        event.getEntity().getCapability(FaithProvider.CAPABILITY).ifPresent(newCap -> {
            newCap.setFaith(faithSnapshot.getInt("Faith"));
            newCap.setSleepingGodDefeats(faithSnapshot.getInt("SleepingGodDefeats"));
            newCap.setBrokenVesselDefeats(faithSnapshot.getInt("BrokenVesselDefeats"));
            newCap.setHungerEyesRevealed(faithSnapshot.getBoolean("HungerEyesRevealed"));
            newCap.setHungerEyesHidden(faithSnapshot.getBoolean("HungerEyesHidden"));
            newCap.setBlessingBoonLevel(faithSnapshot.getInt("BlessingBoonLevel"));
            newCap.setBurdenResistBoonLevel(faithSnapshot.getInt("BurdenResistBoonLevel"));
            newCap.setFaithSinceLastToken(faithSnapshot.getInt("FaithSinceLastToken"));
            newCap.setHasGivenHungerTokens(faithSnapshot.getBoolean("HasGivenHungerTokens"));
            newCap.setHasReceivedHungersPact(faithSnapshot.getBoolean("HasReceivedHungersPact"));
            newCap.setHasClaimedSleepingGodRelic(faithSnapshot.getBoolean("HasClaimedSleepingGodRelic"));
            newCap.setHasBeenToldRelicLocation(faithSnapshot.getBoolean("HasBeenToldRelicLocation"));
            // setPledgedGod resets pledgeProgress to 0 - setPledgeProgress below MUST come after it so
            // the real snapshot value wins (same ordering requirement as ClientboundSyncFaithPacket).
            newCap.setPledgedGod(faithSnapshot.getString("PledgedGod"));
            newCap.setPledgeProgress(faithSnapshot.getInt("PledgeProgress"));
        });

        CompoundTag skillTreeSnapshot = event.getOriginal().getPersistentData().getCompound(SKILL_TREE_SNAPSHOT_KEY);
        event.getEntity().getCapability(SkillTreeProvider.CAPABILITY).ifPresent(newCap -> {
            newCap.setManaBoostLevel(skillTreeSnapshot.getInt("ManaBoostLevel"));
            newCap.setCooldownReductionLevel(skillTreeSnapshot.getInt("CooldownReductionLevel"));
            newCap.setCostReductionLevel(skillTreeSnapshot.getInt("CostReductionLevel"));
            newCap.setPocketDimensionUnlocked(skillTreeSnapshot.getBoolean("PocketDimensionUnlocked"));
            newCap.setPocketDimensionExpansionLevel(skillTreeSnapshot.getInt("PocketDimensionExpansionLevel"));
            newCap.setStorageSystemUnlocked(skillTreeSnapshot.getBoolean("StorageSystemUnlocked"));
            newCap.setStorageNetworkExpansionLevel(skillTreeSnapshot.getInt("StorageNetworkExpansionLevel"));
            newCap.setArcaneAssemblerUnlocked(skillTreeSnapshot.getBoolean("ArcaneAssemblerUnlocked"));
            newCap.setAssemblerSpeedLevel(skillTreeSnapshot.getInt("AssemblerSpeedLevel"));
            newCap.setAssemblerFuelEfficiencyLevel(skillTreeSnapshot.getInt("AssemblerFuelEfficiencyLevel"));
            newCap.setOverloadRitualUnlocked(skillTreeSnapshot.getBoolean("OverloadRitualUnlocked"));
            newCap.setWarpedAttunementUnlocked(skillTreeSnapshot.getBoolean("WarpedAttunementUnlocked"));
        });
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            KnownSpellsSync.syncToClient(serverPlayer);
            FaithSync.syncToClient(serverPlayer);
            SkillTreeSync.syncToClient(serverPlayer);

            // Guards against an interrupted first visit (room never got pasted) or a manual /execute in
            // teleport landing before the room exists - cheap no-op if the room is already correctly placed.
            if (serverPlayer.level().dimension() == ModDimensions.POCKET_DIMENSION_KEY
                    && serverPlayer.level() instanceof ServerLevel pocketLevel)
            {
                int expansionLevel = serverPlayer.getCapability(SkillTreeProvider.CAPABILITY)
                        .map(ISkillTree::getPocketDimensionExpansionLevel).orElse(0);
                PocketDimensionPlacer.ensureRoomPlaced(pocketLevel, serverPlayer.getUUID(), expansionLevel);
            }
        }
    }

    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            KnownSpellsSync.syncToClient(serverPlayer);
            FaithSync.syncToClient(serverPlayer);
            SkillTreeSync.syncToClient(serverPlayer);
        }
    }

    // An ordinary portal trip never fires PlayerRespawnEvent (that's death/End-exit only), so without this
    // the client's synced capability copy just goes stale after any dimension change - the server-side data
    // itself is untouched (this doesn't recreate the ServerPlayer the way death does), but nothing tells the
    // client to re-read it, so FlightHudOverlay keeps rendering off whatever it last had (e.g. falling back
    // to 0 Faith / baseFlightSeconds if that resync never happened at all yet this session).
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            KnownSpellsSync.syncToClient(serverPlayer);
            FaithSync.syncToClient(serverPlayer);
            SkillTreeSync.syncToClient(serverPlayer);
        }
    }
}
