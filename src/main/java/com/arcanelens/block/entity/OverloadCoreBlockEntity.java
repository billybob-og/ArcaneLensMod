package com.arcanelens.block.entity;

import com.arcanelens.Config;
import com.arcanelens.capability.ISkillTree;
import com.arcanelens.capability.SkillTreeProvider;
import com.arcanelens.block.OverloadPortalBlock;
import com.arcanelens.registry.ModBlockEntities;
import com.arcanelens.registry.ModBlocks;
import com.arcanelens.util.StorageNetworkManager;
import com.arcanelens.worldgen.ModDimensions;
import com.arcanelens.worldgen.OverloadPortalSavedData;
import com.arcanelens.worldgen.VesselLairPlacer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * Anchored to a Living Chest cluster (see StorageNetworkManager.findAnchor) - never itself a BFS network
 * node, only ever reads the cluster's live member count. Right-clicking with a Warped Catalyst while that
 * cluster sits at its effective cap starts a one-shot charge-up; completing it pastes a small portal frame
 * here (replacing this block) and a matching one in the Warped Hollow, linked together via
 * OverloadPortalSavedData.
 */
public class OverloadCoreBlockEntity extends BlockEntity
{
    // -1 = idle. Counts up (not down) while charging, purely a style choice matching how this codebase's
    // other "elapsed" counters (e.g. SoulPedestalBlockEntity.burnTimer) are usually framed.
    private int chargeTicksElapsed = -1;

    // Whoever started the current (or most recently completed) charge - remembered so completeRitual can
    // hand them a Lodestone Compass bound to the lair once it finishes, since by then the ritual is no
    // longer running through their own click and has no other reference to which player to reward.
    // Persisted rather than a plain instance field: the charge only lasts Config.overloadRitualChargeTicks
    // (a few seconds), but a server restart mid-charge shouldn't silently drop who gets the compass.
    private UUID startingPlayerId;

    public OverloadCoreBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.OVERLOAD_CORE.get(), pos, state);
    }

    public boolean isCharging()
    {
        return chargeTicksElapsed >= 0;
    }

    /** Validates capacity/adjacency, consumes the Catalyst, and starts the charge - or messages the player
     * and does nothing if any precondition fails. The at-capacity check only gates *starting* the ritual;
     * once charging begins it always runs to completion even if the network later drops below capacity
     * (no partial-refund path exists anywhere else in this codebase's Skill Tree veto handlers either, so
     * this is consistent rather than a special case). */
    public void tryStartRitual(ServerPlayer player, ItemStack catalyst)
    {
        if (isCharging())
        {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel))
        {
            return;
        }

        BlockPos anchor = StorageNetworkManager.findAnchor(serverLevel, worldPosition);
        if (anchor == null)
        {
            message(player, "The Overload Core must be placed adjacent to a Living Chest.");
            return;
        }

        StorageNetworkManager.ClusterResult cluster = StorageNetworkManager.findClusterFrom(serverLevel, anchor);
        boolean warpedAttunement = player.getCapability(SkillTreeProvider.CAPABILITY)
                .map(ISkillTree::isWarpedAttunementUnlocked).orElse(false);
        int expansionLevel = player.getCapability(SkillTreeProvider.CAPABILITY)
                .map(ISkillTree::getStorageNetworkExpansionLevel).orElse(0);
        int effectiveCap = Config.maxStorageBlocksPerNetwork + expansionLevel * Config.storageBlocksPerExpansionLevel;
        // Warped Attunement (purchasable after Broken Vessel's first defeat) lowers how full the network
        // needs to be to start a *further* ritual - a low-risk numeric reward for repeat rituals, reusing
        // the existing cap/skill plumbing rather than a new reward type.
        int requiredMembers = warpedAttunement
                ? (int) Math.ceil(effectiveCap * Config.overloadRitualCapFractionAfterVesselDefeat)
                : effectiveCap;

        if (cluster.memberStoragePositions().size() < requiredMembers)
        {
            message(player, "The anchored network isn't at its storage capacity yet ("
                    + cluster.memberStoragePositions().size() + "/" + requiredMembers + ").");
            return;
        }

        catalyst.shrink(1);
        chargeTicksElapsed = 0;
        startingPlayerId = player.getUUID();
        setChanged();
        message(player, ChatFormatting.LIGHT_PURPLE, "The network strains under its own weight. Something begins to give.");
    }

    private void message(ServerPlayer player, String text)
    {
        message(player, ChatFormatting.YELLOW, text);
    }

    private void message(ServerPlayer player, ChatFormatting color, String text)
    {
        player.displayClientMessage(Component.literal(text).withStyle(color), true);
    }

    public void tick(Level level, BlockPos pos, BlockState state)
    {
        if (chargeTicksElapsed < 0)
        {
            return;
        }

        chargeTicksElapsed++;

        // Escalating particle cue every half-second, so the charge reads as one deliberate, telegraphed
        // event rather than an instant flip - matches the plan's "escalating particle/sound cues" note.
        if (level instanceof ServerLevel serverLevel && chargeTicksElapsed % 10 == 0)
        {
            float progress = (float) chargeTicksElapsed / Config.overloadRitualChargeTicks;
            int count = 2 + (int) (progress * 6);
            serverLevel.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    count, 0.35, 0.35, 0.35, 0.01);
        }

        if (chargeTicksElapsed >= Config.overloadRitualChargeTicks)
        {
            completeRitual(level, pos, state);
        }
    }

    /** Pastes a small portal frame at this exact position (replacing the Core, so the ritual can't be
     * re-triggered) and a matching one at a freshly-assigned cell in the shared Warped Hollow, then links
     * every tile of each frame to the other frame's anchor point (see OverloadPortalSavedData). Requires
     * the Broken Vessel dimension to actually be registered - if server.getLevel(...) returns null (a
     * malformed/missing dimension JSON), this fails silently rather than crashing, same defensive shape
     * PocketDimensionSpell already uses. */
    private void completeRitual(Level level, BlockPos pos, BlockState state)
    {
        chargeTicksElapsed = -1;
        setChanged();

        if (!(level instanceof ServerLevel originLevel))
        {
            return;
        }

        ServerLevel brokenVesselLevel = originLevel.getServer().getLevel(ModDimensions.BROKEN_VESSEL_KEY);
        if (brokenVesselLevel == null)
        {
            return;
        }

        VesselLairPlacer.ensureLairPlaced(brokenVesselLevel);

        OverloadPortalSavedData data = OverloadPortalSavedData.get(brokenVesselLevel);
        GlobalPos originAnchor = GlobalPos.of(originLevel.dimension(), pos);
        int cellIndex = data.getOrAssignCell(originAnchor);
        BlockPos destBottom = data.cellOrigin(cellIndex);

        // Force the destination chunk to actually exist before pasting into it - same idiom
        // PocketDimensionPlacer/VesselLairPlacer both already use.
        brokenVesselLevel.getChunk(destBottom.getX() >> 4, destBottom.getZ() >> 4);

        buildPortalFrame(originLevel, pos);
        buildPortalFrame(brokenVesselLevel, destBottom);

        GlobalPos destAnchor = GlobalPos.of(ModDimensions.BROKEN_VESSEL_KEY, destBottom);
        GlobalPos originTop = GlobalPos.of(originLevel.dimension(), pos.above());
        GlobalPos destTop = GlobalPos.of(ModDimensions.BROKEN_VESSEL_KEY, destBottom.above());

        // Both tiles on each side resolve to the other side's single anchor point (the bottom tile) -
        // players don't need tile-precise pairing, just to arrive at essentially the same spot regardless
        // of which of the 2 walkable tiles they stepped into.
        data.linkTile(originAnchor, destAnchor);
        data.linkTile(originTop, destAnchor);
        data.linkTile(destAnchor, originAnchor);
        data.linkTile(destTop, originAnchor);

        originLevel.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                1, 0.0, 0.0, 0.0, 0.0);

        grantLairCompass(originLevel);
    }

    /** The ritual's arrival cell (see OverloadPortalSavedData.getOrAssignCell) can land arbitrarily far from
     * VesselLairPlacer's one fixed tower - without this, a player stepping through has no in-game way to
     * find it. A Lodestone Compass pre-bound to the Lodestone VesselLairPlacer places next to the tower
     * solves that with nothing but vanilla mechanics - no custom waypoint UI needed. */
    private void grantLairCompass(ServerLevel originLevel)
    {
        if (startingPlayerId == null)
        {
            return;
        }
        ServerPlayer player = originLevel.getServer().getPlayerList().getPlayer(startingPlayerId);
        startingPlayerId = null;
        if (player == null)
        {
            return;
        }

        ItemStack compass = new ItemStack(Items.COMPASS);
        CompoundTag tag = compass.getOrCreateTag();
        tag.put(CompassItem.TAG_LODESTONE_POS, NbtUtils.writeBlockPos(VesselLairPlacer.LODESTONE_POS));
        Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, ModDimensions.BROKEN_VESSEL_KEY)
                .resultOrPartial(err -> {})
                .ifPresent(encoded -> tag.put(CompassItem.TAG_LODESTONE_DIMENSION, encoded));
        tag.putBoolean(CompassItem.TAG_LODESTONE_TRACKED, true);

        if (!player.getInventory().add(compass))
        {
            player.drop(compass, false);
        }
        message(player, ChatFormatting.LIGHT_PURPLE, "A compass in your pack now pulls toward what waits beyond.");
    }

    /** Just the two walkable portal tiles themselves (interiorBottom + the tile above it) - no surrounding
     * frame. OverloadPortalBlock's own animated texture already reads as a distinct, deliberate rift on its
     * own (see OverloadPortalBlock.UPPER + the top/bottom filmstrip textures), so an obsidian ring around it
     * was redundant rather than load-bearing; OVERLOAD_PORTAL is unbreakable by normal means regardless
     * (see ModBlockTagsProvider), so nothing here relied on the ring for protection either. */
    private static void buildPortalFrame(ServerLevel level, BlockPos interiorBottom)
    {
        level.setBlockAndUpdate(interiorBottom,
                ModBlocks.OVERLOAD_PORTAL.get().defaultBlockState().setValue(OverloadPortalBlock.UPPER, false));
        level.setBlockAndUpdate(interiorBottom.above(),
                ModBlocks.OVERLOAD_PORTAL.get().defaultBlockState().setValue(OverloadPortalBlock.UPPER, true));
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("ChargeTicksElapsed", chargeTicksElapsed);
        if (startingPlayerId != null)
        {
            tag.putUUID("StartingPlayerId", startingPlayerId);
        }
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        chargeTicksElapsed = tag.getInt("ChargeTicksElapsed");
        startingPlayerId = tag.hasUUID("StartingPlayerId") ? tag.getUUID("StartingPlayerId") : null;
    }
}
