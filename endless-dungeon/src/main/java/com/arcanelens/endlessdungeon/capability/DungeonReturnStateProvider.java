package com.arcanelens.endlessdungeon.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DungeonReturnStateProvider implements ICapabilitySerializable<CompoundTag>
{
    public static final Capability<IDungeonReturnState> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final DungeonReturnStateImpl backend = new DungeonReturnStateImpl();
    private final LazyOptional<IDungeonReturnState> optional = LazyOptional.of(() -> backend);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        return cap == CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return backend.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag)
    {
        backend.deserializeNBT(tag);
    }
}
