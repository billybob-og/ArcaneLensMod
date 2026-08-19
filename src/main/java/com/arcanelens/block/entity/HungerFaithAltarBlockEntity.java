package com.arcanelens.block.entity;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.god.GodRegistry;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** The Hunger's dedicated Faith Altar - a slow, passive devouring mechanic (mirrors
 * SoulPedestalBlockEntity's conversionItem/conversionTimer shape, producing Faith instead of mana)
 * rather than the base FaithAltarBlock's burn-in-soul-fire mechanic, since a god of appetite is a
 * better fit for "feed it and it slowly eats" than for a sacrificial-burning flavor. */
public class HungerFaithAltarBlockEntity extends BlockEntity
{
    private ItemStack faithItem = ItemStack.EMPTY;
    private int consumeTimer;
    @Nullable
    private UUID owner;

    public HungerFaithAltarBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.HUNGER_FAITH_ALTAR.get(), pos, state);
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

    public ItemStack getFaithItem()
    {
        return faithItem;
    }

    public void setFaithItem(ItemStack stack)
    {
        this.faithItem = stack;
        syncToClient();
    }

    public ItemStack takeFaithItem()
    {
        ItemStack result = faithItem;
        faithItem = ItemStack.EMPTY;
        syncToClient();
        return result;
    }

    private void syncToClient()
    {
        setChanged();
        if (level != null && !level.isClientSide)
        {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void tick(Level level, BlockPos pos)
    {
        if (!(level instanceof ServerLevel serverLevel) || faithItem.isEmpty() || owner == null)
        {
            return;
        }

        // Only works while the owner is actually pledged to Hunger right now - a later swap away
        // leaves the altar (and the item sitting in it) completely inert, not silently devouring
        // items for nothing.
        ServerPlayer ownerPlayer = serverLevel.getServer().getPlayerList().getPlayer(owner);
        if (ownerPlayer == null || !GodRegistry.isPledgedTo(ownerPlayer, "hunger"))
        {
            return;
        }

        consumeTimer++;
        if (consumeTimer < Config.hungerAltarConsumeIntervalTicks)
        {
            return;
        }
        consumeTimer = 0;

        int faith = Config.getFaithForRarity(faithItem.getRarity());
        faithItem.shrink(1);
        syncToClient();

        if (faith <= 0)
        {
            return;
        }
        FaithSync.grantFaithWithFeedback(ownerPlayer, faith, "Hunger's Altar");
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        if (!faithItem.isEmpty())
        {
            tag.put("FaithItem", faithItem.save(new CompoundTag()));
        }
        tag.putInt("ConsumeTimer", consumeTimer);
        if (owner != null)
        {
            tag.putUUID("Owner", owner);
        }
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        faithItem = tag.contains("FaithItem") ? ItemStack.of(tag.getCompound("FaithItem")) : ItemStack.EMPTY;
        consumeTimer = tag.getInt("ConsumeTimer");
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
