package com.arcanelens.block.entity;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.god.GodRegistry;
import com.arcanelens.god.TheaterEventTracker;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Theater's dedicated Faith Altar - the only one of the five that combines multiple mechanics (per
 * the user's own call: "would be fun to combine all to put on a show for friends"), all proximity-
 * gated to this altar rather than owner-centric like Hunger/Fertility/War:
 *
 * <ul>
 * <li><b>Deception</b> - sneak-attacks/hitting an unaware mob nearby (see TheaterEventTracker,
 * transient combat events recorded globally and consumed here the same way WarFaithAltarBlockEntity
 * consumes WarDeathTracker).</li>
 * <li><b>Disguise</b> - a nearby player is invisible or wearing a mob head.</li>
 * <li><b>Drama</b> - a nearby player is alive with less than 3 hearts.</li>
 * <li><b>Spectacle</b> - a nearby Jukebox is playing a record (checked on the periodic scan), or the
 * altar is right-clicked with a Written Book (see TheaterFaithAltarBlock.use(), a bonus scaled by how
 * many other players are nearby as an "audience").</li>
 * </ul>
 *
 * Faith always goes to the altar's owner regardless of who actually performed the qualifying action,
 * matching Fertility/War's precedent (the altar rewards its owner for what happens around it).
 */
public class TheaterFaithAltarBlockEntity extends BlockEntity
{
    private static final Set<Item> MOB_HEADS = Set.of(
            Items.ZOMBIE_HEAD, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL,
            Items.CREEPER_HEAD, Items.PLAYER_HEAD, Items.DRAGON_HEAD, Items.PIGLIN_HEAD);

    private int scanTimer;
    private long lastScanTick;
    @Nullable
    private UUID owner;

    public TheaterFaithAltarBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.THEATER_FAITH_ALTAR.get(), pos, state);
    }

    @Nullable
    public UUID getOwner()
    {
        return owner;
    }

    public void setOwner(@Nullable UUID owner)
    {
        this.owner = owner;
        setChanged();
    }

    public void tick(Level level, BlockPos pos)
    {
        if (!(level instanceof ServerLevel serverLevel) || owner == null)
        {
            return;
        }

        // Only works while the owner is actually pledged to Theater right now - inert the moment
        // they swap to a different god. TheaterEventTracker self-prunes like WarDeathTracker does,
        // so leaving lastScanTick stale here can't build up an unbounded backlog once re-pledged.
        ServerPlayer ownerPlayer = serverLevel.getServer().getPlayerList().getPlayer(owner);
        if (ownerPlayer == null || !GodRegistry.isPledgedTo(ownerPlayer, "theater"))
        {
            return;
        }

        scanTimer++;
        if (scanTimer < Config.theaterScanIntervalTicks)
        {
            return;
        }
        scanTimer = 0;

        int faith = 0;

        int deceptions = TheaterEventTracker.countNearbyDeceptionsSince(serverLevel, pos, Config.theaterScanRadius, lastScanTick);
        lastScanTick = serverLevel.getGameTime();
        faith += deceptions * Config.theaterFaithPerDeception;

        AABB scanBox = new AABB(pos).inflate(Config.theaterScanRadius);
        List<Player> nearbyPlayers = serverLevel.getEntitiesOfClass(Player.class, scanBox);
        for (Player player : nearbyPlayers)
        {
            if (player.isInvisible() || MOB_HEADS.contains(player.getItemBySlot(EquipmentSlot.HEAD).getItem()))
            {
                faith += Config.theaterFaithPerDisguisedPlayer;
            }
            if (player.isAlive() && player.getHealth() < 6.0F)
            {
                faith += Config.theaterFaithPerDramaPlayer;
            }
        }

        if (hasNearbyPlayingJukebox(serverLevel, pos, Config.theaterScanRadius))
        {
            faith += Config.theaterFaithPerSpectacleJukebox;
        }

        if (faith <= 0)
        {
            return;
        }
        FaithSync.grantFaithWithFeedback(ownerPlayer, faith, "Janus's Altar");
    }

    /** The Written Book sub-case of Spectacle - a direct interaction rather than a scanned condition,
     * unlike everything else on this altar. Bonus scaled by how many other players are nearby (an
     * "audience"), see TheaterFaithAltarBlock.use(). */
    public void grantSpectacleBookFaith(ServerLevel level, BlockPos pos)
    {
        if (owner == null)
        {
            return;
        }

        ServerPlayer ownerPlayer = level.getServer().getPlayerList().getPlayer(owner);
        if (ownerPlayer == null || !GodRegistry.isPledgedTo(ownerPlayer, "theater"))
        {
            return;
        }

        int audience = level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(Config.theaterScanRadius)).size();
        int faith = Config.theaterFaithPerSpectacleBook + Math.max(0, audience - 1) * Config.theaterFaithPerSpectacleAudienceMember;
        FaithSync.grantFaithWithFeedback(ownerPlayer, faith, "Janus's Altar - Spectacle");
    }

    private static boolean hasNearbyPlayingJukebox(ServerLevel level, BlockPos pos, int radius)
    {
        BlockPos min = pos.offset(-radius, -radius, -radius);
        BlockPos max = pos.offset(radius, radius, radius);
        for (BlockPos check : BlockPos.betweenClosed(min, max))
        {
            BlockState state = level.getBlockState(check);
            if (state.is(Blocks.JUKEBOX) && state.getValue(JukeboxBlock.HAS_RECORD))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("ScanTimer", scanTimer);
        tag.putLong("LastScanTick", lastScanTick);
        if (owner != null)
        {
            tag.putUUID("Owner", owner);
        }
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        scanTimer = tag.getInt("ScanTimer");
        lastScanTick = tag.getLong("LastScanTick");
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
