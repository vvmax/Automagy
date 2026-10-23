package tuhljin.automagy.tiles;

import net.minecraft.nbt.NBTTagCompound;

public class TileEntityTorchInversion extends ModTileEntity {
    protected int redstoneSignalStrength = 0;

    public int getRedstoneSignalStrength() {
        return this.redstoneSignalStrength;
    }

    public void setRedstoneSignalStrength(int strength) {
        this.redstoneSignalStrength = strength;
        this.markDirty();
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInteger("redstoneSignalStrength", this.redstoneSignalStrength);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.redstoneSignalStrength = nbttagcompound.getInteger("redstoneSignalStrength");
    }
}
