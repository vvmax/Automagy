package tuhljin.automagy.tiles;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityHourglassMagic extends TileEntityHourglass {
    public int modeSignal = 1;
    public int modeSandFlip = 0;
    public int modeSignalReact = 1;

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setShort("modeSignal", (short)this.modeSignal);
        nbttagcompound.setShort("modeSandFlip", (short)this.modeSandFlip);
        nbttagcompound.setShort("modeSignalReact", (short)this.modeSignalReact);
        nbttagcompound.setBoolean("modeRepeat", this.modeRepeat);
        nbttagcompound.setBoolean("flipped", this.flipped);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.modeSignal = nbttagcompound.getShort("modeSignal");
        this.modeSandFlip = nbttagcompound.getShort("modeSandFlip");
        if (nbttagcompound.hasKey("modeSignalReact")) {
            this.modeSignalReact = nbttagcompound.getShort("modeSignalReact");
        }

        this.modeRepeat = nbttagcompound.getBoolean("modeRepeat");
        this.flipped = nbttagcompound.getBoolean("flipped");
    }

    @Override
    public int getRedstoneSignalStrength() {
        switch (this.modeSignal) {
            case 1:
                return super.getRedstoneSignalStrength();
            case 2:
                return this.flipped && this.ticksCounted < this.ticksTarget ? 15 : 0;
            case 4:
                if (this.modeRepeat && super.getRedstoneSignalStrength() > 0) {
                    return 0;
                }
            case 3:
                return this.ticksCounted < this.ticksTarget ? 15 : 0;
            default:
                return 0;
        }
    }

    @Override
    protected int onFlipTickCount() {
        if ((this.modeSignalReact != 2 || !this.receivingSignal) && (this.modeSignalReact != 3 || this.receivingSignal)) {
            if (this.modeSandFlip == 1) {
                return 0;
            } else if (this.modeSandFlip != 2) {
                return super.onFlipTickCount();
            } else if (this.ticksCounted >= this.ticksTarget) {
                return 0;
            } else if (this.comparatorSignalStrength == 0 && this.getRedstoneSignalStrength() == 0) {
                return this.ticksTarget;
            } else {
                this.comparatorSignalStrength = 0;
                return this.ticksTarget + 1;
            }
        } else {
            this.markDirty();
            if (this.ticksCounted < this.ticksTarget) {
                if (this.comparatorSignalStrength == 0 && this.getRedstoneSignalStrength() == 0) {
                    return this.ticksTarget;
                } else {
                    this.comparatorSignalStrength = 0;
                    return this.ticksTarget + 1;
                }
            } else {
                return 0;
            }
        }
    }

    @Override
    public void updateRedstoneInput(boolean newSignal) {
        if (this.modeSignalReact == 1) {
            super.updateRedstoneInput(newSignal);
        } else {
            if (this.receivingSignal) {
                if (!newSignal) {
                    this.receivingSignal = false;
                    this.markDirty(false);
                    if (this.modeSignalReact == 0) {
                        return;
                    }

                    if (this.modeSignalReact == 2) {
                        this.startFlipOnServer(false);
                    } else if (this.ticksCounted < this.ticksTarget) {
                        this.startFlipOnServer(false);
                    }
                }
            } else if (newSignal) {
                this.receivingSignal = true;
                this.markDirty(false);
                if (this.modeSignalReact == 0) {
                    return;
                }

                if (this.modeSignalReact == 3) {
                    this.startFlipOnServer(false);
                } else if (this.ticksCounted < this.ticksTarget) {
                    this.startFlipOnServer(false);
                }
            }
        }
    }

    public void receiveGUIMessage(int type, int data) {
        switch (type) {
            case 0:
                this.setTargetTimeSeconds(data);
                return;
            case 1:
            default:
                FMLLog.warning("[Automagy] TileEntityHourglassMagic received invalid packet data. Ignoring. (type=" + type + ")");
                return;
            case 2:
                this.modeSignal = data;
                break;
            case 3:
                this.modeSandFlip = data;
                break;
            case 4:
                this.modeRepeat = data == 1;
                break;
            case 5:
                this.modeSignalReact = data;
        }

        this.markDirty(false);
    }

    @Override
    public void updateEntity() {
        if (this.ticksCounted < this.ticksTarget) {
            if (this.receivingSignal) {
                if (this.modeSignalReact == 2) {
                    this.startFlipOnServer(false);
                    return;
                }
            } else if (this.modeSignalReact == 3) {
                this.startFlipOnServer(false);
                return;
            }
        }

        super.updateEntity();
    }
}
