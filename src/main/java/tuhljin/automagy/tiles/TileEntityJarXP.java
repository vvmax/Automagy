package tuhljin.automagy.tiles;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.api.ItemApi;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileJar;
import thaumcraft.common.tiles.TileJarFillable;
import tuhljin.automagy.blocks.ModBlocks;

public class TileEntityJarXP extends TileJar implements IXPContainer {
    public static int MAX_XP = 10545;
    public int xpColor = -1;
    public int lastColorDisplayed = 15990551;
    protected int xp = 0;

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (this.worldObj.isRemote) {
            if (this.xpColor == -1) {
                if (this.worldObj.rand.nextBoolean()) {
                    this.lastColorDisplayed = 2096945;
                }

                this.xpColor = this.worldObj.rand.nextInt(200);
            } else if (this.worldObj.rand.nextInt(500) == 0) {
                this.xpColor = this.worldObj.rand.nextInt(200);
            }

            this.xpColor++;
        }
    }

    @Override
    public int getXP() {
        return this.xp;
    }

    @Override
    public int getMaxXP() {
        return MAX_XP;
    }

    public void setXP(int xp) {
        xp = Math.min(xp, MAX_XP);
        if (this.xp != xp) {
            this.xp = xp;
            if (this.worldObj != null) {
                if (this.xp < 1) {
                    ItemStack wardedJar = ItemApi.getBlock("blockJar", 0);
                    this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, Block.getBlockFromItem(wardedJar.getItem()));
                } else if (!this.worldObj.isRemote) {
                    this.markDirty();
                }
            }
        }
    }

    public boolean removeExactXP(int amt) {
        if (this.xp < amt) {
            return false;
        } else {
            this.setXP(this.xp - amt);
            return true;
        }
    }

    public boolean addExactXP(int amt) {
        if (MAX_XP < this.xp + amt) {
            return false;
        } else {
            this.setXP(this.xp + amt);
            return true;
        }
    }

    public int addXP(int amt) {
        int targetAmt = this.xp + amt;
        this.setXP(targetAmt);
        return targetAmt - this.xp;
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
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.xp = nbttagcompound.getInteger("xp");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInteger("xp", this.xp);
    }

    public static TileEntityJarXP transformWardedJar(TileJarFillable jar) {
        if (jar.getBlockType() == ConfigBlocks.blockJar && jar.getBlockMetadata() == 0 && jar.amount == 0 && jar.aspectFilter == null) {
            World world = jar.getWorld();
            int x = jar.xCoord;
            int y = jar.yCoord;
            int z = jar.zCoord;
            world.setBlock(x, y, z, ModBlocks.jarExperience);
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityJarXP) {
                return (TileEntityJarXP)te;
            }
        }

        return null;
    }
}
