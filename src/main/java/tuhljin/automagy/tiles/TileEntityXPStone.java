package tuhljin.automagy.tiles;

import java.awt.Color;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.common.tiles.TileJar;
import thaumcraft.common.tiles.TileJarFillable;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.blocks.BlockXPStone;
import tuhljin.automagy.lib.RedstoneCalc;
import tuhljin.automagy.lib.ThaumcraftExtension;

public class TileEntityXPStone extends ModTileEntity implements IXPContainer {
    public static int MAX_XP = 2000;
    private static final ForgeDirection[] DIRS = new ForgeDirection[]{
        ForgeDirection.UP, ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST
    };
    private static int CHECK_FREQUENCY = 10;
    private static int MAX_TRANSFER_AMT = 10;
    private static int MAX_QUICK_DRAIN_AMT = MAX_XP;
    private static int DRIP_FREQUENCY = 75;
    private static int DRIP_AMOUNT = 10;
    protected int xp = 0;
    private int nextCheck = CHECK_FREQUENCY;
    private int nextDrip = DRIP_FREQUENCY;

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote) {
            this.nextCheck--;
            if (this.nextCheck < 1) {
                this.nextCheck = CHECK_FREQUENCY;

                for (ForgeDirection dir : DIRS) {
                    TileEntity te = this.worldObj.getTileEntity(this.xCoord + dir.offsetX, this.yCoord + dir.offsetY, this.zCoord + dir.offsetZ);
                    if (te != null && te instanceof TileEntityAmnesiacStone) {
                        ((TileEntityAmnesiacStone)te).doXPTransfer(this, this.xp);
                    }
                }
            }

            this.nextDrip--;
            if (this.nextDrip < 1) {
                this.nextDrip = DRIP_FREQUENCY;
                if (this.xp > 0 && this.yCoord > 1) {
                    boolean needDrip = true;

                    for (int yOffset = 1; yOffset < 3; yOffset++) {
                        TileEntity te = this.worldObj.getTileEntity(this.xCoord, this.yCoord - yOffset, this.zCoord);
                        if (te != null) {
                            if (te instanceof TileEntityJarXP) {
                                this.dripXP((TileEntityJarXP)te);
                                needDrip = false;
                                break;
                            }

                            if (te instanceof TileJarFillable) {
                                TileEntityJarXP jar = TileEntityJarXP.transformWardedJar((TileJarFillable)te);
                                this.dripXP(jar);
                                needDrip = false;
                                break;
                            }
                        }

                        if (!this.worldObj.isAirBlock(this.xCoord, this.yCoord - yOffset, this.zCoord)
                            && (yOffset != 1 || !ThaumcraftExtension.blockIsItemGrate(this.worldObj, this.xCoord, this.yCoord - 1, this.zCoord, true))) {
                            if (yOffset > 1 || this.worldObj.getTileEntity(this.xCoord, this.yCoord - yOffset, this.zCoord) instanceof TileJar) {
                                this.dripXP(null);
                            }

                            needDrip = false;
                            break;
                        }

                        if (this.yCoord - yOffset < 1) {
                            break;
                        }
                    }

                    if (needDrip) {
                        this.dripXP(null);
                    }
                }
            }
        } else {
            this.nextDrip--;
            if (this.nextDrip < 1) {
                this.nextDrip = DRIP_FREQUENCY;
                if (this.xp > 0 && this.yCoord > 1) {
                    float y = 0.0F;
                    if (ThaumcraftExtension.blockIsItemGrate(this.worldObj, this.xCoord, this.yCoord - 1, this.zCoord, true)) {
                        y = this.yCoord - 0.2F;
                    } else if (this.worldObj.isAirBlock(this.xCoord, this.yCoord - 1, this.zCoord)
                        || this.worldObj.getTileEntity(this.xCoord, this.yCoord - 1, this.zCoord) instanceof TileJar) {
                        y = this.yCoord - 0.02F;
                    }

                    if (y > 0.0F) {
                        Integer i = ((BlockXPStone)this.getBlockType()).getOverlayInvColor(this.getBlockMetadata());
                        if (i != null) {
                            Color color = new Color(i);
                            float r = color.getRed() / 255.0F;
                            float g = color.getGreen() / 255.0F;
                            float b = color.getBlue() / 255.0F;
                            Automagy.proxy
                                .xpDropletFX(
                                    this.worldObj,
                                    this.xCoord + 0.5F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.15F,
                                    y,
                                    this.zCoord + 0.5F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.15F,
                                    r,
                                    g,
                                    b
                                );
                        }
                    }
                }
            }
        }
    }

    public void dripXP(TileEntityJarXP jar) {
        int amt = Math.min(DRIP_AMOUNT, this.xp);
        this.xp -= amt;
        this.markDirty();
        if (jar != null) {
            jar.addXP(amt);
        }
    }

    public int drainXP(int amt) {
        amt = Math.min(Math.min(amt, MAX_TRANSFER_AMT), this.xp);
        if (amt < 1) {
            return 0;
        } else {
            this.xp -= amt;
            this.markDirty();
            return amt;
        }
    }

    public int addXP(int amt) {
        amt = Math.min(Math.min(amt, MAX_TRANSFER_AMT), MAX_XP - this.xp);
        if (amt < 1) {
            return 0;
        } else {
            this.xp += amt;
            this.markDirty();
            return amt;
        }
    }

    public int quickDrain() {
        if (this.xp == 0) {
            return 0;
        } else {
            int amt = Math.min(this.xp, MAX_QUICK_DRAIN_AMT);
            this.xp -= amt;
            this.markDirty();
            return amt;
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

    public float getXPFraction() {
        return this.xp == 0 ? 0.0F : (float)this.xp / MAX_XP;
    }

    public int getComparatorStrength() {
        return RedstoneCalc.getRedstoneSignalStrengthFromValues(this.xp, MAX_XP);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.xp = nbttagcompound.getInteger("xp");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInteger("xp", this.xp);
    }
}
