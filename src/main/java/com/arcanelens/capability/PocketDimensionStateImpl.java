package com.arcanelens.capability;

import net.minecraft.nbt.CompoundTag;

public class PocketDimensionStateImpl implements IPocketDimensionState
{
    private String returnDimension = "";
    private double returnX;
    private double returnY;
    private double returnZ;
    private float returnYaw;
    private float returnPitch;

    @Override
    public String getReturnDimension()
    {
        return returnDimension;
    }

    @Override
    public double getReturnX()
    {
        return returnX;
    }

    @Override
    public double getReturnY()
    {
        return returnY;
    }

    @Override
    public double getReturnZ()
    {
        return returnZ;
    }

    @Override
    public float getReturnYaw()
    {
        return returnYaw;
    }

    @Override
    public float getReturnPitch()
    {
        return returnPitch;
    }

    @Override
    public void setReturnPosition(String dimension, double x, double y, double z, float yaw, float pitch)
    {
        this.returnDimension = dimension;
        this.returnX = x;
        this.returnY = y;
        this.returnZ = z;
        this.returnYaw = yaw;
        this.returnPitch = pitch;
    }

    public CompoundTag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("ReturnDimension", returnDimension);
        tag.putDouble("ReturnX", returnX);
        tag.putDouble("ReturnY", returnY);
        tag.putDouble("ReturnZ", returnZ);
        tag.putFloat("ReturnYaw", returnYaw);
        tag.putFloat("ReturnPitch", returnPitch);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag)
    {
        returnDimension = tag.getString("ReturnDimension");
        returnX = tag.getDouble("ReturnX");
        returnY = tag.getDouble("ReturnY");
        returnZ = tag.getDouble("ReturnZ");
        returnYaw = tag.getFloat("ReturnYaw");
        returnPitch = tag.getFloat("ReturnPitch");
    }
}
