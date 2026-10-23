package tuhljin.automagy.tiles;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.AspectList;
import tuhljin.automagy.blocks.ModBlocks;

public class TileEntityThaumostaticPylon extends TileEntityThaumostaticPylonBase {
    public int bossY = -1;

    public void activate(int controllerY, boolean goUp) {
        this.bossY = controllerY;
        this.markDirty();
        int y = this.yCoord + (goUp ? 1 : -1);
        TileEntity te = this.worldObj.getTileEntity(this.xCoord, y, this.zCoord);
        if (te instanceof TileEntityThaumostaticPylon) {
            ((TileEntityThaumostaticPylon)te).activate(controllerY, goUp);
        }
    }

    @Override
    public void assureValidity() {
        if (this.bossY != -1) {
            Block b = this.worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord);
            if (b == ModBlocks.thaumostaticPylon || b == Blocks.quartz_block && this.worldObj.getBlockMetadata(this.xCoord, this.yCoord - 1, this.zCoord) == 1) {
                b = this.worldObj.getBlock(this.xCoord, this.yCoord + 1, this.zCoord);
                if (b == ModBlocks.thaumostaticPylon || b == ModBlocks.thaumostaticSpire) {
                    return;
                }
            }

            TileEntity te = this.worldObj.getTileEntity(this.xCoord, this.bossY, this.zCoord);
            this.bossY = -1;
            this.markDirty();
            if (te instanceof TileEntityThaumostaticController) {
                ((TileEntityThaumostaticController)te).deactivate();
            }
        }
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.bossY = nbttagcompound.getInteger("bossY");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInteger("bossY", this.bossY);
    }

    @Override
    public AspectList getAspects() {
        if (this.bossY != -1) {
            TileEntity te = this.worldObj.getTileEntity(this.xCoord, this.bossY, this.zCoord);
            if (te instanceof TileEntityThaumostaticController) {
                TileEntityThaumostaticController teTC = (TileEntityThaumostaticController)te;
                if (teTC.active) {
                    return teTC.getAspects();
                }

                this.bossY = -1;
                this.markDirty(false);
            }
        }

        return null;
    }

    @Override
    public AspectList getAspectsBase() {
        if (this.bossY != -1) {
            TileEntity te = this.worldObj.getTileEntity(this.xCoord, this.bossY, this.zCoord);
            if (te instanceof TileEntityThaumostaticController) {
                TileEntityThaumostaticController teTC = (TileEntityThaumostaticController)te;
                if (teTC.active) {
                    return teTC.getAspectsBase();
                }

                this.bossY = -1;
                this.markDirty(false);
            }
        }

        return null;
    }
}
