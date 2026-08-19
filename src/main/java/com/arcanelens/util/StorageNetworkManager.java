package com.arcanelens.util;

import com.arcanelens.block.StorageConnectorBlock;
import com.arcanelens.block.entity.StorageBlockEntity;
import com.arcanelens.block.entity.TerminalBlockEntity;
import com.arcanelens.worldgen.StorageNetworkSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cluster discovery for the storage network. Storage Blocks and Storage Connectors are "dumb" - cluster
 * membership is never cached on them, only recomputed live via BFS (same shape as
 * CommandTriggerBlockEntity.deleteConnectedPlates: Direction.values() neighbor walk, visited set, queue).
 * This means block breaks, explosions, and cluster splits all self-correct automatically - there's no
 * cache to desync. Placement validation (cap/bridging) and claim bookkeeping are layered on top of
 * findClusterFrom in later additions to this class, once StorageNetworkSavedData/TerminalBlockEntity exist.
 */
public class StorageNetworkManager
{
    /** Generous safety ceiling on BFS traversal, independent of the player-facing network cap, so a
     * pathological connector chain can't blow up traversal cost. */
    private static final int TRAVERSAL_SAFETY_LIMIT = 4096;

    public record ClusterResult(List<BlockPos> memberStoragePositions, Set<BlockPos> allTraversedPositions) {}

    /** BFS from start through orthogonally-adjacent Storage Blocks/Connectors. start itself must already
     * be one of those two block types and already present in the level (called post-placement, or from
     * an existing member block). Returns member Storage Block positions in a fixed deterministic order
     * (sorted by BlockPos) so every caller - claim registration, menu construction, self-healing - agrees
     * on the same ordering. */
    public static ClusterResult findClusterFrom(Level level, BlockPos start)
    {
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> members = new ArrayList<>();
        Deque<BlockPos> queue = new ArrayDeque<>();

        BlockPos startPos = start.immutable();
        visited.add(startPos);
        queue.add(startPos);
        addIfMember(level, startPos, members);

        while (!queue.isEmpty() && visited.size() < TRAVERSAL_SAFETY_LIMIT)
        {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values())
            {
                BlockPos neighborPos = current.relative(direction).immutable();
                if (!visited.add(neighborPos))
                {
                    continue;
                }
                if (isNetworkNode(level, neighborPos))
                {
                    queue.add(neighborPos);
                    addIfMember(level, neighborPos, members);
                }
                else
                {
                    visited.remove(neighborPos);
                }
            }
        }

        members.sort(Comparator.<BlockPos>comparingInt(BlockPos::getX)
                .thenComparingInt(BlockPos::getY)
                .thenComparingInt(BlockPos::getZ));
        return new ClusterResult(members, visited);
    }

    private static boolean isNetworkNode(Level level, BlockPos pos)
    {
        return level.getBlockEntity(pos) instanceof StorageBlockEntity
                || level.getBlockState(pos).getBlock() instanceof StorageConnectorBlock;
    }

    private static void addIfMember(Level level, BlockPos pos, List<BlockPos> members)
    {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof StorageBlockEntity)
        {
            members.add(pos.immutable());
        }
    }

    /** First adjacent Storage Block to a position (a Terminal's placement), checked in a fixed Direction
     * order so every code path that needs to pick "the" anchor for a position agrees on the same one. */
    public static BlockPos findAnchor(Level level, BlockPos anchorFrom)
    {
        for (Direction direction : Direction.values())
        {
            BlockPos neighborPos = anchorFrom.relative(direction);
            if (level.getBlockEntity(neighborPos) instanceof StorageBlockEntity)
            {
                return neighborPos.immutable();
            }
        }
        return null;
    }

    public enum PlacementResult { OK, EXCEEDS_CAP, BRIDGES_OWNED_NETWORKS }

    /** Called from the block-place event handler for a just-placed Storage Block/Connector - the block is
     * already present in the level by the time BlockEvent.EntityPlaceEvent fires, so the cluster it would
     * form can be discovered directly by BFS from placedPos. Rejects (without mutating any claim) if the
     * resulting cluster would exceed effectiveCap (the placing player's Config.maxStorageBlocksPerNetwork
     * plus whatever their Storage Network Expansion skill level adds - see
     * StorageNetworkPlacementHandler, which computes this per-player before calling in), or would bridge
     * two clusters already claimed by two different Terminals together. */
    public static PlacementResult checkPlacement(ServerLevel level, BlockPos placedPos, int effectiveCap)
    {
        ClusterResult cluster = findClusterFrom(level, placedPos);

        if (cluster.memberStoragePositions().size() > effectiveCap)
        {
            return PlacementResult.EXCEEDS_CAP;
        }

        StorageNetworkSavedData data = StorageNetworkSavedData.get(level);
        Set<BlockPos> owningTerminals = new HashSet<>();
        for (StorageNetworkSavedData.Claim claim : data.getClaims())
        {
            if (cluster.allTraversedPositions().contains(claim.anchorStoragePos()))
            {
                owningTerminals.add(claim.terminalPos());
            }
        }

        if (owningTerminals.size() > 1)
        {
            return PlacementResult.BRIDGES_OWNED_NETWORKS;
        }

        return PlacementResult.OK;
    }

    /** Registers a claim for a newly-placed Terminal if its anchor's cluster isn't already claimed by a
     * different Terminal. No-ops (silent secondary viewer) if the cluster is already claimed, or if the
     * Terminal has no adjacent Storage Block yet. */
    public static void tryClaim(ServerLevel level, BlockPos terminalPos)
    {
        BlockPos anchor = findAnchor(level, terminalPos);
        if (anchor == null)
        {
            return;
        }

        StorageNetworkSavedData data = StorageNetworkSavedData.get(level);
        if (data.findClaimByTerminal(terminalPos) != null)
        {
            return;
        }

        ClusterResult cluster = findClusterFrom(level, anchor);
        for (StorageNetworkSavedData.Claim claim : data.getClaims())
        {
            if (cluster.allTraversedPositions().contains(claim.anchorStoragePos()))
            {
                return;
            }
        }

        data.addClaim(new StorageNetworkSavedData.Claim(terminalPos.immutable(), anchor));
    }

    /** Verifies a claim's Terminal + anchor still exist and are still adjacent; re-anchors to another
     * currently-adjacent Storage Block if the original anchor is gone, or releases the claim if none
     * remain. Called lazily wherever claim validity actually matters (menu construction), not on a hot
     * path - membership itself is never cached, so a stale claim only matters for ownership bookkeeping. */
    public static void resolveClaim(ServerLevel level, StorageNetworkSavedData.Claim claim)
    {
        StorageNetworkSavedData data = StorageNetworkSavedData.get(level);
        if (!(level.getBlockEntity(claim.terminalPos()) instanceof TerminalBlockEntity))
        {
            data.removeClaim(claim);
            return;
        }

        if (level.getBlockEntity(claim.anchorStoragePos()) instanceof StorageBlockEntity)
        {
            return;
        }

        BlockPos newAnchor = findAnchor(level, claim.terminalPos());
        data.removeClaim(claim);
        if (newAnchor != null)
        {
            data.addClaim(new StorageNetworkSavedData.Claim(claim.terminalPos(), newAnchor));
        }
    }

    /** Finds the Terminal (if any) currently claiming the cluster storagePos belongs to - used by
     * StorageBlockEntity to look up its owning Terminal's Stack Upgrade bonus for direct (e.g. hopper)
     * inserts that bypass the Terminal's own pool logic entirely. One BFS from storagePos itself (not one
     * per claim), then a lookup through the (typically few) claims for one whose anchor falls within that
     * cluster - same shape as checkPlacement's bridging check. */
    @Nullable
    public static TerminalBlockEntity findOwningTerminal(ServerLevel level, BlockPos storagePos)
    {
        ClusterResult cluster = findClusterFrom(level, storagePos);
        StorageNetworkSavedData data = StorageNetworkSavedData.get(level);
        for (StorageNetworkSavedData.Claim claim : data.getClaims())
        {
            if (cluster.allTraversedPositions().contains(claim.anchorStoragePos()))
            {
                return level.getBlockEntity(claim.terminalPos()) instanceof TerminalBlockEntity terminal ? terminal : null;
            }
        }
        return null;
    }

    public static void releaseClaim(ServerLevel level, BlockPos terminalPos)
    {
        StorageNetworkSavedData data = StorageNetworkSavedData.get(level);
        StorageNetworkSavedData.Claim claim = data.findClaimByTerminal(terminalPos);
        if (claim != null)
        {
            data.removeClaim(claim);
        }
    }
}
