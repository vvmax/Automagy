package tuhljin.automagy.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import tuhljin.automagy.lib.RedstoneCalc;
import tuhljin.automagy.network.MessageHourglassFlipped;

public class TileEntityHourglass extends ModTileEntity {
    public final float SECONDS_COOLDOWN_BETWEEN_FLIPS = 0.25F;
    public final int REDSTONE_PULSE_LENGTH = 5;
    protected int ticksTarget = 100;
    protected int ticksCounted = this.ticksTarget;
    protected int ticksSendingPulse = 0;
    public boolean receivingSignal = false;
    public long timeDidFlip = 0L;
    public boolean flipped = false;
    public boolean modeRepeat = false;
    protected int comparatorSignalStrength = 0;

    public int getRedstoneSignalStrength() {
        return this.ticksSendingPulse > 0 ? 15 : 0;
    }

    public int getComparatorSignalStrength() {
        return this.comparatorSignalStrength;
    }

    public int getTargetTimeSeconds() {
        return this.ticksTarget / 20;
    }

    public void setTargetTimeSeconds(int target) {
        if (target < 1) {
            target = 1;
        } else if (target > 32767) {
            target = 32767;
        }

        target *= 20;
        if (target != this.ticksTarget) {
            if (this.ticksCounted >= this.ticksTarget) {
                this.ticksCounted = target;
            } else if (this.ticksCounted >= target) {
                this.ticksCounted = target - 1;
            }

            this.ticksTarget = target;
            if (!this.worldObj.isRemote) {
                this.markDirty();
            }
        }
    }

    public int getRemainingSeconds() {
        return MathHelper.ceiling_float_int((this.ticksTarget - this.ticksCounted) / 20.0F);
    }

    public float percentageComplete() {
        return (float)this.ticksCounted / this.ticksTarget * 100.0F;
    }

    public void updateRedstoneInput(boolean newSignal) {
        if (this.receivingSignal) {
            if (!newSignal) {
                this.receivingSignal = false;
                this.markDirty(false);
            }
        } else if (newSignal) {
            this.receivingSignal = true;
            this.markDirty(false);
            this.startFlipOnServer(false);
        }
    }

    public boolean startFlipOnServer(boolean timeCheck) {
        float secondsCooldown = timeCheck ? 0.25F : 0.125F;
        long time = System.nanoTime();
        if ((float)(time - this.timeDidFlip) > secondsCooldown * 1.0E9F) {
            this.flipped = !this.flipped;
            this.timeDidFlip = time;
            this.ticksCounted = this.onFlipTickCount();
            if (this.ticksCounted > this.ticksTarget) {
                this.ticksCounted = this.ticksTarget;
                this.notifyNeighbors();
            }

            MessageHourglassFlipped.sendToClients(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.flipped);
            this.markDirty();
            return true;
        } else {
            return false;
        }
    }

    public void receiveFlipFromServer(boolean flipState) {
        this.flipped = flipState;
        this.timeDidFlip = System.nanoTime();
        this.ticksCounted = this.onFlipTickCount();
        if (this.ticksCounted > this.ticksTarget) {
            this.ticksCounted = this.ticksTarget;
        }
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        boolean neighborsNeedNotification = false;
        if (this.ticksSendingPulse > 0) {
            this.ticksSendingPulse--;
            if (this.ticksSendingPulse == 0) {
                neighborsNeedNotification = true;
            }
        }

        if (this.ticksCounted < this.ticksTarget) {
            this.ticksCounted++;
            if (!this.worldObj.isRemote && this.ticksCounted == this.ticksTarget) {
                this.ticksSendingPulse = 5;
                this.comparatorSignalStrength = 0;
                neighborsNeedNotification = true;
                this.markDirty();
                if (this.modeRepeat) {
                    this.startFlipOnServer(false);
                }
            } else {
                int css = RedstoneCalc.getRedstoneSignalStrengthFromValues(this.ticksCounted, this.ticksTarget);
                if (this.comparatorSignalStrength != css) {
                    this.comparatorSignalStrength = css;
                    neighborsNeedNotification = true;
                }
            }
        }

        if (neighborsNeedNotification) {
            this.notifyNeighbors();
        }
    }

    protected int onFlipTickCount() {
        return this.ticksTarget - this.ticksCounted;
    }

    protected void notifyNeighbors() {
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInteger("ticksTarget", this.ticksTarget);
        nbttagcompound.setInteger("ticksCounted", this.ticksCounted);
        nbttagcompound.setShort("ticksSendingSignal", (short)this.ticksSendingPulse);
        nbttagcompound.setBoolean("receivingSignal", this.receivingSignal);
        nbttagcompound.setShort("comparatorSignalStrength", (short)this.comparatorSignalStrength);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.ticksTarget = nbttagcompound.getInteger("ticksTarget");
        this.ticksCounted = nbttagcompound.getInteger("ticksCounted");
        this.ticksSendingPulse = nbttagcompound.getShort("ticksSendingSignal");
        this.receivingSignal = nbttagcompound.getBoolean("receivingSignal");
        this.comparatorSignalStrength = nbttagcompound.getShort("comparatorSignalStrength");
    }
}
