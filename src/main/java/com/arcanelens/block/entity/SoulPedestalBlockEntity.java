package com.arcanelens.block.entity;

import com.arcanelens.Config;
import com.arcanelens.advancement.ModCriteriaTriggers;
import com.arcanelens.block.SoulPedestalBlock;
import com.arcanelens.registry.ModBlockEntities;
import com.arcanelens.util.PedestalLinker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SoulPedestalBlockEntity extends BlockEntity
{
    public static final int MAX_FUEL = 3;
    private static final int MANA_BEAM_DURATION_TICKS = 15;

    private ItemStack conversionItem = ItemStack.EMPTY;
    private int burnTimer;
    private int conversionTimer;
    private int rescanTimer;
    private List<BlockPos> linkedLensPedestals = Collections.emptyList();
    private final List<ManaBeam> activeManaBeams = new ArrayList<>();
    @Nullable
    private UUID owner;

    public SoulPedestalBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.SOUL_PEDESTAL.get(), pos, state);
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

    public ItemStack getConversionItem()
    {
        return conversionItem;
    }

    public void setConversionItem(ItemStack stack)
    {
        this.conversionItem = stack;
        syncToClient();
    }

    public ItemStack takeConversionItem()
    {
        ItemStack result = conversionItem;
        conversionItem = ItemStack.EMPTY;
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

    public void tick(Level level, BlockPos pos, BlockState state)
    {
        advanceManaBeams(level, pos);

        rescanTimer++;
        if (rescanTimer >= Config.linkRescanIntervalTicks)
        {
            rescanTimer = 0;
            List<BlockPos> newlyLinked = PedestalLinker.findLinkedLensPedestals(level, pos, Config.linkRadius);
            if (owner != null && linkedLensPedestals.isEmpty() && !newlyLinked.isEmpty() && level instanceof ServerLevel serverLevel)
            {
                ServerPlayer ownerPlayer = serverLevel.getServer().getPlayerList().getPlayer(owner);
                if (ownerPlayer != null)
                {
                    ModCriteriaTriggers.PEDESTALS_LINKED.trigger(ownerPlayer);
                }
            }
            linkedLensPedestals = newlyLinked;
        }

        if (!state.getValue(SoulPedestalBlock.LIT))
        {
            return;
        }

        burnTimer++;
        if (burnTimer >= Config.soulSandBurnTicks)
        {
            burnTimer = 0;
            int fuel = state.getValue(SoulPedestalBlock.FUEL) - 1;
            if (fuel <= 0)
            {
                level.setBlock(pos, state.setValue(SoulPedestalBlock.FUEL, 0).setValue(SoulPedestalBlock.LIT, false), 3);
                return;
            }
            level.setBlock(pos, state.setValue(SoulPedestalBlock.FUEL, fuel), 3);
        }

        conversionTimer++;
        if (conversionTimer >= Config.conversionIntervalTicks)
        {
            conversionTimer = 0;
            if (!conversionItem.isEmpty())
            {
                int mana = Config.getManaForRarity(conversionItem.getRarity());
                conversionItem.shrink(1);
                syncToClient();

                if (mana > 0)
                {
                    for (BlockPos linkedPos : linkedLensPedestals)
                    {
                        if (level.getBlockEntity(linkedPos) instanceof LensPedestalBlockEntity lensPedestal)
                        {
                            lensPedestal.receiveMana(mana);
                            activeManaBeams.add(new ManaBeam(linkedPos));
                        }
                    }
                }
            }
        }
    }

    private void advanceManaBeams(Level level, BlockPos pos)
    {
        if (activeManaBeams.isEmpty() || !(level instanceof ServerLevel serverLevel))
        {
            return;
        }

        Iterator<ManaBeam> iterator = activeManaBeams.iterator();
        while (iterator.hasNext())
        {
            ManaBeam beam = iterator.next();
            double progress = (double) beam.ticksElapsed / MANA_BEAM_DURATION_TICKS;

            double x = Mth.lerp(progress, pos.getX() + 0.5, beam.target.getX() + 0.5);
            double y = Mth.lerp(progress, pos.getY() + 1.3, beam.target.getY() + 1.3);
            double z = Mth.lerp(progress, pos.getZ() + 0.5, beam.target.getZ() + 0.5);
            serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);

            beam.ticksElapsed++;
            if (beam.ticksElapsed > MANA_BEAM_DURATION_TICKS)
            {
                iterator.remove();
            }
        }
    }

    private static class ManaBeam
    {
        final BlockPos target;
        int ticksElapsed;

        ManaBeam(BlockPos target)
        {
            this.target = target;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        if (!conversionItem.isEmpty())
        {
            tag.put("ConversionItem", conversionItem.save(new CompoundTag()));
        }
        tag.putInt("BurnTimer", burnTimer);
        tag.putInt("ConversionTimer", conversionTimer);
        if (owner != null)
        {
            tag.putUUID("Owner", owner);
        }
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        conversionItem = tag.contains("ConversionItem") ? ItemStack.of(tag.getCompound("ConversionItem")) : ItemStack.EMPTY;
        burnTimer = tag.getInt("BurnTimer");
        conversionTimer = tag.getInt("ConversionTimer");
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
