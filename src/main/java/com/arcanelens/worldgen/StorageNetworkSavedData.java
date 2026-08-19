package com.arcanelens.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks storage-network ownership claims: which Terminal, anchored to which adjacent Storage Block,
 * first claimed a connected cluster. This is the ONLY thing persisted for the storage system - cluster
 * membership itself is always recomputed live via StorageNetworkManager.findClusterFrom, never cached,
 * so a claim going stale (anchor broken, terminal broken) just needs lazy self-healing, not active upkeep.
 */
public class StorageNetworkSavedData extends SavedData
{
    private static final String DATA_NAME = "arcanelens_storage_networks";

    public record Claim(BlockPos terminalPos, BlockPos anchorStoragePos) {}

    private final List<Claim> claims = new ArrayList<>();

    public static StorageNetworkSavedData load(CompoundTag tag)
    {
        StorageNetworkSavedData data = new StorageNetworkSavedData();
        for (Tag entry : tag.getList("Claims", Tag.TAG_COMPOUND))
        {
            CompoundTag claimTag = (CompoundTag) entry;
            BlockPos terminalPos = new BlockPos(claimTag.getInt("TerminalX"), claimTag.getInt("TerminalY"), claimTag.getInt("TerminalZ"));
            BlockPos anchorPos = new BlockPos(claimTag.getInt("AnchorX"), claimTag.getInt("AnchorY"), claimTag.getInt("AnchorZ"));
            data.claims.add(new Claim(terminalPos, anchorPos));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        ListTag claimList = new ListTag();
        for (Claim claim : claims)
        {
            CompoundTag claimTag = new CompoundTag();
            claimTag.putInt("TerminalX", claim.terminalPos().getX());
            claimTag.putInt("TerminalY", claim.terminalPos().getY());
            claimTag.putInt("TerminalZ", claim.terminalPos().getZ());
            claimTag.putInt("AnchorX", claim.anchorStoragePos().getX());
            claimTag.putInt("AnchorY", claim.anchorStoragePos().getY());
            claimTag.putInt("AnchorZ", claim.anchorStoragePos().getZ());
            claimList.add(claimTag);
        }
        tag.put("Claims", claimList);
        return tag;
    }

    public static StorageNetworkSavedData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(StorageNetworkSavedData::load, StorageNetworkSavedData::new, DATA_NAME);
    }

    public List<Claim> getClaims()
    {
        return claims;
    }

    public Claim findClaimByTerminal(BlockPos terminalPos)
    {
        for (Claim claim : claims)
        {
            if (claim.terminalPos().equals(terminalPos))
            {
                return claim;
            }
        }
        return null;
    }

    public void addClaim(Claim claim)
    {
        claims.add(claim);
        setDirty();
    }

    public void removeClaim(Claim claim)
    {
        if (claims.remove(claim))
        {
            setDirty();
        }
    }
}
