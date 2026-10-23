package tuhljin.automagy.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.VisNetHandler;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.network.MessageThaumostaticSparks;

public class TileEntityThaumostaticRefuelerSpire extends ModTileEntity {
    public static int VALIDITYCHECK_RATE = 60;
    public int poweredTime = 0;
    public int timeSparks = 0;
    private long nextPacketAllowed = 0L;
    private boolean validLocation = false;
    private int nextCheck = 1;
    private int pylonCount = -1;
    private int bossY = -1;

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (this.timeSparks > 0) {
            this.timeSparks--;
        }

        if (!this.worldObj.isRemote) {
            this.nextCheck--;
            if (this.nextCheck < 1) {
                this.nextCheck = VALIDITYCHECK_RATE;
                this.validLocation = this.worldObj.canBlockSeeTheSky(this.xCoord, this.yCoord + 2, this.zCoord);
            }

            int prevPower = this.poweredTime;
            if (this.poweredTime > 0) {
                this.poweredTime--;
            }

            if (this.poweredTime < 1) {
                int amt = this.validLocation ? this.drainVis() : 0;
                if (amt > 0) {
                    this.poweredTime = amt;
                    if (prevPower < 1) {
                        this.markDirty(true);
                    } else {
                        this.markDirty(false);
                    }
                } else if (prevPower > 0) {
                    this.markDirty(true);
                }
            }
        }
    }

    public void doSparks() {
        if (!this.worldObj.isRemote) {
            long currentTime = System.currentTimeMillis();
            if (currentTime >= this.nextPacketAllowed) {
                this.nextPacketAllowed = currentTime + 2500L;
                MessageThaumostaticSparks.sendToClients(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            }
        } else {
            this.timeSparks = 160;
        }
    }

    public boolean isPowered() {
        return this.poweredTime > 0;
    }

    public boolean isRefueling() {
        return this.timeSparks > 0 && this.isPowered();
    }

    public int numPylons() {
        if (this.pylonCount == -1) {
            this.pylonCount = 0;

            for (int y = this.yCoord - 1; this.worldObj.getBlock(this.xCoord, y, this.zCoord) == ModBlocks.thaumostaticPylon; y--) {
                this.pylonCount++;
            }
        }

        return this.pylonCount;
    }

    private int drainVis() {
        int amt = VisNetHandler.drainVis(this.worldObj, this.xCoord, this.yCoord + 1, this.zCoord, Aspect.AIR, 10);
        if (amt < 1) {
            if (this.bossY == -1) {
                this.bossY = -2;

                for (int y = this.yCoord - 1; y >= 0; y--) {
                    TileEntity te = this.worldObj.getTileEntity(this.xCoord, y, this.zCoord);
                    if (te instanceof TileEntityThaumostaticController) {
                        this.bossY = y;
                        break;
                    }

                    if (!(te instanceof TileEntityThaumostaticPylonBase)) {
                        break;
                    }
                }
            }

            if (this.bossY > -1) {
                amt = VisNetHandler.drainVis(this.worldObj, this.xCoord, this.bossY, this.zCoord, Aspect.AIR, 10);
            }
        }

        return amt;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.poweredTime = nbttagcompound.getInteger("poweredTime");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInteger("poweredTime", this.poweredTime);
    }
}
