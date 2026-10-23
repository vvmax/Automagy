package tuhljin.automagy.tiles;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import tuhljin.automagy.blocks.BlockRedcrystalMerc;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.data.RedstoneMirrorNetwork;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

public class TileEntityRedcrystalMerc extends TileEntityRedcrystal {
    public static final int MAX_DISTANCE = 8;
    public int mirrorX;
    public int mirrorY = -1;
    public int mirrorZ;
    public int mirrorDim;
    public int mirrorLinkedX;
    public int mirrorLinkedY = -1;
    public int mirrorLinkedZ;
    public int mirrorLinkedDim;
    public int strengthWithoutMirror;
    protected boolean mirrorValid = false;
    protected boolean mirrorStartedValid = false;
    private int ticksSinceMirrorCheck = 0;
    public static int rotationSpeedFactor = 3240;
    public static double clientRenderFloatingTop_unpowered = -0.2;
    public static double clientRenderFloatingTop_powered = 0.1;
    public static double clientRenderFloatingBtm_unpowered = -0.3;
    public static double clientRenderFloatingBtm_powered = -0.1;
    public int clientRenderRotationHelper = 0;
    public double clientRenderFloatingDistance = clientRenderFloatingTop_unpowered;
    public boolean clientRenderGoingUp = false;

    public TileEntityRedcrystalMerc() {
    }

    public TileEntityRedcrystalMerc(short orientation, boolean noConnection) {
        super(orientation, noConnection);
    }

    public boolean hasValidMirrorConnection() {
        return this.mirrorValid;
    }

    public void setMirrorLink(int dimensionID, int x, int y, int z) {
        this.mirrorX = x;
        this.mirrorY = y;
        this.mirrorZ = z;
        this.mirrorDim = dimensionID;
        this.extraData = 0;
        if (this.mirrorY == -1) {
            this.mirrorValid = false;
        } else if (this.mirrorDim == this.worldObj.provider.dimensionId) {
            float distance = TjUtil.getDistanceBetweenPoints(this.xCoord, this.yCoord, this.zCoord, this.mirrorX, this.mirrorY, this.mirrorZ);
            if (distance <= 8.0F) {
                TileEntity te = this.worldObj.getTileEntity(this.mirrorX, this.mirrorY, this.mirrorZ);
                if (ThaumcraftExtension.tileIsMirror(te)) {
                    this.mirrorValid = true;
                    this.mirrorStartedValid = true;
                    WorldSpecificCoordinates coord = ThaumcraftExtension.getLinkedMirrorCoordinates(te);
                    if (coord == null) {
                        this.mirrorLinkedY = -1;
                    } else {
                        this.mirrorLinkedDim = coord.dim;
                        this.mirrorLinkedX = coord.x;
                        this.mirrorLinkedY = coord.y;
                        this.mirrorLinkedZ = coord.z;
                    }
                } else if (TjUtil.isChunkLoaded(this.worldObj, x, z)) {
                    this.mirrorY = -1;
                    this.mirrorValid = false;
                }
            } else {
                this.mirrorValid = false;
            }
        } else {
            this.mirrorValid = false;
        }

        if (!this.worldObj.isRemote) {
            this.updateMirrorNetwork();
        }

        this.markDirty();
    }

    @Override
    public void updateEntity() {
        if (this.worldObj.isRemote) {
            this.clientRenderRotationHelper += 3;
            boolean powered = this.getBlockMetadata() > 0;
            if (powered) {
                this.clientRenderRotationHelper += 7;
            }

            if (this.clientRenderRotationHelper >= rotationSpeedFactor) {
                this.clientRenderRotationHelper = this.clientRenderRotationHelper - rotationSpeedFactor;
            }

            double adj = 0.0;
            if (this.clientRenderFloatingDistance < (powered ? clientRenderFloatingBtm_powered : clientRenderFloatingBtm_unpowered)) {
                this.clientRenderGoingUp = true;
            } else if (this.clientRenderFloatingDistance > (powered ? clientRenderFloatingTop_powered : clientRenderFloatingTop_unpowered)) {
                this.clientRenderGoingUp = false;
            }

            adj += this.clientRenderGoingUp ? 5.0E-4 : -5.0E-4;
            if (powered) {
                adj *= this.clientRenderFloatingDistance < clientRenderFloatingBtm_powered ? 12 : 3;
            } else if (this.clientRenderFloatingDistance > clientRenderFloatingTop_unpowered) {
                adj *= 7.0;
            }

            this.clientRenderFloatingDistance += adj;
        } else if (this.mirrorStartedValid) {
            this.ticksSinceMirrorCheck++;
            if (this.ticksSinceMirrorCheck >= 100) {
                this.ticksSinceMirrorCheck = 0;
                TileEntity te = this.worldObj.getTileEntity(this.mirrorX, this.mirrorY, this.mirrorZ);
                if (ThaumcraftExtension.tileIsMirror(te)) {
                    WorldSpecificCoordinates coord = ThaumcraftExtension.getLinkedMirrorCoordinates(te);
                    if (coord == null) {
                        this.mirrorLinkedY = -1;
                    } else {
                        this.mirrorLinkedDim = coord.dim;
                        this.mirrorLinkedX = coord.x;
                        this.mirrorLinkedY = coord.y;
                        this.mirrorLinkedZ = coord.z;
                    }

                    if (!this.mirrorValid) {
                        this.mirrorValid = true;
                        this.markDirty();
                        return;
                    }

                    this.markDirty(false);
                } else if (this.mirrorValid && TjUtil.isChunkLoaded(this.worldObj, this.mirrorX, this.mirrorZ)) {
                    this.mirrorValid = false;
                    this.mirrorLinkedY = -1;
                    this.extraData = 0;
                    this.markDirty();
                    RedstoneMirrorNetwork closeMirrorData = RedstoneMirrorNetwork.getData(this.mirrorDim);
                    closeMirrorData.removeSignal(this.mirrorX, this.mirrorY, this.mirrorZ);
                    this.updateBlock();
                    return;
                }
            }

            if (this.mirrorValid && this.mirrorLinkedY != -1) {
                RedstoneMirrorNetwork farMirrorData = RedstoneMirrorNetwork.getData(this.mirrorLinkedDim);
                int mirrorStrength = farMirrorData.getSignalGoingIn(this.mirrorLinkedX, this.mirrorLinkedY, this.mirrorLinkedZ);
                if (this.extraData != mirrorStrength) {
                    this.extraData = mirrorStrength;
                    int strength = this.getBlockMetadata() + this.powerStability;
                    this.updateBlock();
                }
            }
        }
    }

    public void removeFromMirrorNetwork() {
        if (this.mirrorStartedValid) {
            RedstoneMirrorNetwork closeMirrorData = RedstoneMirrorNetwork.getData(this.mirrorDim);
            closeMirrorData.removeContributor(this.mirrorX, this.mirrorY, this.mirrorZ, this);
        }
    }

    public void updateMirrorNetwork() {
        if (this.mirrorStartedValid) {
            RedstoneMirrorNetwork closeMirrorData = RedstoneMirrorNetwork.getData(this.mirrorDim);
            closeMirrorData.contributeToSignalInto(this.mirrorX, this.mirrorY, this.mirrorZ, this, this.strengthWithoutMirror);
        }
    }

    protected void updateBlock() {
        Block block = this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord);
        if (block instanceof BlockRedcrystalMerc) {
            ((BlockRedcrystalMerc)block).updateAndPropagateChanges(this.worldObj, this.xCoord, this.yCoord, this.zCoord, true, false, false, false);
        }
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z) {
        return oldBlock != newBlock || oldBlock != ModBlocks.redcrystalMerc;
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        if (this.mirrorY != -1) {
            nbttagcompound.setInteger("mirrorX", this.mirrorX);
            nbttagcompound.setInteger("mirrorY", this.mirrorY);
            nbttagcompound.setInteger("mirrorZ", this.mirrorZ);
            nbttagcompound.setInteger("mirrorDim", this.mirrorDim);
            nbttagcompound.setBoolean("mirrorValid", this.mirrorValid);
            nbttagcompound.setBoolean("mirrorStartedValid", this.mirrorStartedValid);
            nbttagcompound.setInteger("strengthWithoutMirror", this.strengthWithoutMirror);
            if (this.mirrorLinkedY != -1) {
                nbttagcompound.setInteger("mirrorLinkedX", this.mirrorLinkedX);
                nbttagcompound.setInteger("mirrorLinkedY", this.mirrorLinkedY);
                nbttagcompound.setInteger("mirrorLinkedZ", this.mirrorLinkedZ);
                nbttagcompound.setInteger("mirrorLinkedDim", this.mirrorLinkedDim);
            }
        }
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.mirrorX = nbttagcompound.getInteger("mirrorX");
        this.mirrorY = nbttagcompound.hasKey("mirrorY") ? nbttagcompound.getInteger("mirrorY") : -1;
        this.mirrorZ = nbttagcompound.getInteger("mirrorZ");
        this.mirrorDim = nbttagcompound.getInteger("mirrorDim");
        this.mirrorValid = nbttagcompound.getBoolean("mirrorValid");
        this.mirrorStartedValid = nbttagcompound.getBoolean("mirrorStartedValid");
        this.strengthWithoutMirror = nbttagcompound.getInteger("strengthWithoutMirror");
        this.mirrorLinkedX = nbttagcompound.getInteger("mirrorLinkedX");
        this.mirrorLinkedY = nbttagcompound.hasKey("mirrorLinkedY") ? nbttagcompound.getInteger("mirrorLinkedY") : -1;
        this.mirrorLinkedZ = nbttagcompound.getInteger("mirrorLinkedZ");
        this.mirrorLinkedDim = nbttagcompound.getInteger("mirrorLinkedDim");
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1);
    }
}
