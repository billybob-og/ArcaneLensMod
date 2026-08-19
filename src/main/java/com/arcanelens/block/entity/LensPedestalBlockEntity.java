package com.arcanelens.block.entity;

import com.arcanelens.advancement.ModCriteriaTriggers;
import com.arcanelens.item.MagicLensItem;
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

public class LensPedestalBlockEntity extends BlockEntity
{
    private ItemStack lens = ItemStack.EMPTY;
    private int bufferedMana;
    @Nullable
    private UUID owner;

    public LensPedestalBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.LENS_PEDESTAL.get(), pos, state);
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

    public ItemStack getLens()
    {
        return lens;
    }

    public void setLens(ItemStack stack)
    {
        this.lens = stack;
        syncToClient();
    }

    public ItemStack takeLens()
    {
        ItemStack result = lens;
        lens = ItemStack.EMPTY;
        syncToClient();
        return result;
    }

    public void receiveMana(int amount)
    {
        bufferedMana += amount;
        setChanged();
    }

    public void tick(Level level, BlockPos pos, BlockState state)
    {
        if (lens.isEmpty())
        {
            return;
        }

        if (bufferedMana > 0)
        {
            int current = MagicLensItem.getMana(lens);
            int max = MagicLensItem.getMaxMana(lens);
            int room = max - current;
            if (room > 0)
            {
                MagicLensItem.setMana(lens, current + Math.min(bufferedMana, room));
            }
            bufferedMana = 0;
            syncToClient();
        }

        if (owner != null && MagicLensItem.getMana(lens) >= MagicLensItem.getMaxMana(lens) && level instanceof ServerLevel serverLevel)
        {
            ServerPlayer ownerPlayer = serverLevel.getServer().getPlayerList().getPlayer(owner);
            if (ownerPlayer != null)
            {
                ModCriteriaTriggers.LENS_MANA_MAXED.trigger(ownerPlayer);
            }
        }
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

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        if (!lens.isEmpty())
        {
            tag.put("Lens", lens.save(new CompoundTag()));
        }
        tag.putInt("BufferedMana", bufferedMana);
        if (owner != null)
        {
            tag.putUUID("Owner", owner);
        }
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        lens = tag.contains("Lens") ? ItemStack.of(tag.getCompound("Lens")) : ItemStack.EMPTY;
        bufferedMana = tag.getInt("BufferedMana");
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
