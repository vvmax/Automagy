package tuhljin.automagy.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public abstract class ModTileEntity extends TileEntity {
    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.readCustomNBT(nbttagcompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        this.writeCustomNBT(nbttagcompound);
    }

    public void readCustomNBT(NBTTagCompound nbttagcompound) {
    }

    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
    }

    @Override
    public void markDirty() {
        this.markDirty(true);
    }

    public void markDirty(boolean updateClients) {
        super.markDirty();
        if (updateClients) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeCustomNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, -999, nbttagcompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        this.readCustomNBT(pkt.getNbtCompound());
    }

    public static int[] getIntArrayFromNbtOrDefault(NBTTagCompound nbttagcompound, String key, int defaultValue, int expectedSize) {
        int[] arr = nbttagcompound.getIntArray(key);
        if (arr.length != expectedSize) {
            int[] newArr = new int[expectedSize];

            for (int i = 0; i < expectedSize; i++) {
                if (i < arr.length) {
                    newArr[i] = arr[i];
                } else {
                    newArr[i] = defaultValue;
                }
            }

            return newArr;
        } else {
            return arr;
        }
    }

    public static boolean[] getBooleanArrayFromNbtOrDefault(NBTTagCompound nbttagcompound, String key, boolean defaultValue, int expectedSize) {
        int[] arr = getIntArrayFromNbtOrDefault(nbttagcompound, key, defaultValue ? 1 : 0, expectedSize);
        boolean[] newArr = new boolean[arr.length];

        for (int i = 0; i < arr.length; i++) {
            newArr[i] = arr[i] == 1;
        }

        return newArr;
    }

    public static void setBooleanArrayInNbt(NBTTagCompound nbttagcompound, String key, boolean[] arr) {
        int[] intArr = new int[arr.length];

        for (int i = 0; i < arr.length; i++) {
            intArr[i] = arr[i] ? 1 : 0;
        }

        nbttagcompound.setIntArray(key, intArr);
    }
}
