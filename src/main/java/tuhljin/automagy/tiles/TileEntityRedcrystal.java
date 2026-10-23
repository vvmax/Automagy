package tuhljin.automagy.tiles;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import tuhljin.automagy.blocks.ModBlocks;

public class TileEntityRedcrystal extends ModTileEntity {
    public short orientation;
    public short powerStability;
    public short powerSourceSide;
    public boolean powerSourceOutsideNetwork;
    public boolean connectN;
    public boolean connectE;
    public boolean connectS;
    public boolean connectW;
    public int extraData;

    public TileEntityRedcrystal() {
        this.orientation = -1;
        this.extraData = -1;
    }

    public TileEntityRedcrystal(short orientation, boolean noConnection) {
        this.orientation = orientation;
        this.powerStability = 0;
        this.powerSourceSide = -1;
        this.powerSourceOutsideNetwork = false;
        if (noConnection) {
            this.connectN = false;
            this.connectE = false;
            this.connectS = false;
            this.connectW = false;
        } else {
            this.connectN = true;
            this.connectE = true;
            this.connectS = true;
            this.connectW = true;
        }

        this.extraData = -1;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.orientation = nbttagcompound.getShort("orientation");
        this.powerStability = nbttagcompound.getShort("powerStability");
        this.powerSourceSide = nbttagcompound.getShort("powerSourceSide");
        this.powerSourceOutsideNetwork = nbttagcompound.getBoolean("powerSourceOutsideNetwork");
        this.connectN = nbttagcompound.getBoolean("connectN");
        this.connectE = nbttagcompound.getBoolean("connectE");
        this.connectS = nbttagcompound.getBoolean("connectS");
        this.connectW = nbttagcompound.getBoolean("connectW");
        if (nbttagcompound.hasKey("extraData")) {
            try {
                this.extraData = nbttagcompound.getInteger("extraData");
            } catch (Exception var3) {
                this.extraData = -1;
            }
        } else {
            this.extraData = -1;
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setShort("orientation", this.orientation);
        nbttagcompound.setShort("powerStability", this.powerStability);
        nbttagcompound.setShort("powerSourceSide", this.powerSourceSide);
        nbttagcompound.setBoolean("powerSourceOutsideNetwork", this.powerSourceOutsideNetwork);
        nbttagcompound.setBoolean("connectN", this.connectN);
        nbttagcompound.setBoolean("connectE", this.connectE);
        nbttagcompound.setBoolean("connectS", this.connectS);
        nbttagcompound.setBoolean("connectW", this.connectW);
        if (this.extraData != -1) {
            nbttagcompound.setInteger("extraData", this.extraData);
        }
    }

    @Override
    public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z) {
        return oldBlock != newBlock
            || oldBlock != ModBlocks.redcrystal
                && oldBlock != ModBlocks.redcrystalAmp
                && oldBlock != ModBlocks.redcrystalDim
                && oldBlock != ModBlocks.redcrystalDense
                && oldBlock != ModBlocks.redcrystalRes;
    }
}
