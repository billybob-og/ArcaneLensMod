package com.arcanelens.block.entity;

import com.arcanelens.menu.GodChallengeMenu;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** One instance of this block entity sits at each god's arena entrance in the God Challenge Hub -
 * which god it challenges is fixed per-instance by godId (baked into that entrance's structure NBT at
 * build time, same idea as CommandTriggerBlockEntity's baked-in command), not chosen from a menu, since
 * which entrance you're standing at already decided that. Empty godId (the default, before Phase B
 * bakes a real one into a structure) means "not configured yet" - use() below refuses to open the
 * screen rather than opening one for a god that doesn't exist. */
public class GodChallengeAltarBlockEntity extends BlockEntity implements MenuProvider
{
    private String godId = "";

    public GodChallengeAltarBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.GOD_CHALLENGE_ALTAR.get(), pos, state);
    }

    public String getGodId()
    {
        return godId;
    }

    public void setGodId(String godId)
    {
        this.godId = godId;
        setChanged();
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
        // Same reasoning as CommandTriggerBlockEntity's own override - block entity data only reaches
        // the client on initial chunk load by default, so a godId set after that (e.g. via a debug
        // command while testing) would never actually reach an already-loaded client without this.
        if (level instanceof ServerLevel serverLevel)
        {
            BlockState state = getBlockState();
            serverLevel.sendBlockUpdated(getBlockPos(), state, state, 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("God Challenge Altar");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
    {
        return new GodChallengeMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putString("GodId", godId);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        godId = tag.getString("GodId");
    }
}
