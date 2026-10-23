package tuhljin.automagy.tiles;

import net.minecraft.block.Block;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.lib.IAutomagyLocationLink;
import tuhljin.automagy.lib.IRemoteComparatorOverride;
import tuhljin.automagy.lib.NeighborNotifier;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

public class TileEntityRemoteComparator extends ModTileEntityWithInventory {
    protected static final int MAX_DISTANCE = 20;
    protected static final int COOLDOWN_TIME = 4;
    protected int redstoneSignalStrength;
    protected int linkMode = 0;
    protected int linkX;
    protected int linkY;
    protected int linkZ;
    protected int cooldown = 0;

    public TileEntityRemoteComparator() {
        super("blockRemoteComparator", 1);
        this.redstoneSignalStrength = 0;
    }

    public static boolean isValidContainerForReading(Block block, World world, int x, int y, int z) {
        if (block.hasComparatorInputOverride()) {
            return true;
        } else {
            TileEntity te = world.getTileEntity(x, y, z);
            return te instanceof IInventory;
        }
    }

    public static int getComparatorReading(Block block, World world, int x, int y, int z) {
        if (block.hasComparatorInputOverride()) {
            return block.getComparatorInputOverride(world, x, y, z, 1);
        } else {
            TileEntity te = world.getTileEntity(x, y, z);
            return te instanceof IInventory ? Container.calcRedstoneFromInventory((IInventory)te) : 0;
        }
    }

    public WorldSpecificCoordinates getLinkLocation() {
        ItemStack stack = this.getStackInSlot(0);
        return stack != null && stack.getItem() instanceof IAutomagyLocationLink ? ((IAutomagyLocationLink)stack.getItem()).getLinkLocation(stack) : null;
    }

    public boolean coordinatesAreInRange(WorldSpecificCoordinates coord) {
        if (coord != null && coord.dim == this.worldObj.provider.dimensionId) {
            float distance = TjUtil.getDistanceBetweenPoints(this.xCoord, this.yCoord, this.zCoord, coord.x, coord.y, coord.z);
            return distance <= 20.0F;
        } else {
            return false;
        }
    }

    public boolean coordinatesAreInRange() {
        return this.coordinatesAreInRange(this.getLinkLocation());
    }

    public Block getBlockAtLinkedLocationIfValid() {
        WorldSpecificCoordinates coord = this.getLinkLocation();
        if (coord != null && coord.dim == this.worldObj.provider.dimensionId) {
            Block block = this.worldObj.getBlock(coord.x, coord.y, coord.z);
            if (isValidContainerForReading(block, this.worldObj, coord.x, coord.y, coord.z)) {
                return block;
            }
        }

        return null;
    }

    public ItemStack getFloatingDisplayItem() {
        return this.getStackInSlot(0);
    }

    public boolean isRedstoneSignalBeingSent() {
        if (this.linkMode == -2) {
            Block block = this.worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord);
            if (block == null) {
                return false;
            } else {
                return block instanceof IRemoteComparatorOverride
                    ? ((IRemoteComparatorOverride)block).hasActiveRedstoneSignal(this.worldObj, this.xCoord, this.yCoord - 1, this.zCoord, this)
                    : block.isProvidingWeakPower(this.worldObj, this.xCoord, this.yCoord - 1, this.zCoord, 2) > 0;
            }
        } else {
            return this.getRedstoneSignalStrength() > 0;
        }
    }

    public int getRedstoneSignalStrength() {
        return this.linkMode >= 0 ? this.redstoneSignalStrength : 0;
    }

    public void setOverride(boolean override) {
        if (override) {
            this.linkMode = -2;
            this.setRedstoneSignalStrength(0);
        } else {
            this.linkMode = 0;
        }

        this.markDirty();
    }

    public boolean isOverridden() {
        return this.linkMode == -2;
    }

    protected void setRedstoneSignalStrength(int strength) {
        if (this.redstoneSignalStrength != strength) {
            this.redstoneSignalStrength = strength;
            this.markDirty();
            NeighborNotifier.notifyBlocksOfExtendedNeighborChange(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
        }
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote && this.linkMode >= 0) {
            if (this.linkMode == 0) {
                WorldSpecificCoordinates coord = this.getLinkLocation();
                if (!this.coordinatesAreInRange(coord)) {
                    this.linkMode = -1;
                    this.setRedstoneSignalStrength(0);
                    this.markDirty();
                    return;
                }

                this.linkX = coord.x;
                this.linkY = coord.y;
                this.linkZ = coord.z;
                this.linkMode = 1;
                this.markDirty();
                this.cooldown = 0;
            } else {
                if (this.cooldown > 0) {
                    this.cooldown--;
                    return;
                }

                this.cooldown = 4;
            }

            Block block = this.getBlockAtLinkedLocationIfValid();
            if (block == null) {
                this.setRedstoneSignalStrength(0);
            } else {
                int strength = 0;

                try {
                    strength = getComparatorReading(block, this.worldObj, this.linkX, this.linkY, this.linkZ);
                } catch (Exception var4) {
                }

                this.setRedstoneSignalStrength(strength);
            }
        }
    }

    @Override
    public ItemStack decrStackSize(int slot, int num) {
        ItemStack stack = super.decrStackSize(slot, num);
        if (this.linkMode == -2) {
            this.worldObj.notifyBlockOfNeighborChange(this.xCoord, this.yCoord - 1, this.zCoord, this.getBlockType());
        } else {
            this.linkMode = 0;
            this.markDirty();
        }

        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        super.setInventorySlotContents(slot, stack);
        if (this.linkMode == -2) {
            this.worldObj.notifyBlockOfNeighborChange(this.xCoord, this.yCoord - 1, this.zCoord, this.getBlockType());
        } else {
            this.linkMode = 0;
            this.markDirty();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return stack != null && stack.getItem() instanceof IAutomagyLocationLink && ((IAutomagyLocationLink)stack.getItem()).getLinkLocation(stack) != null;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.redstoneSignalStrength = nbttagcompound.getShort("strength");
        this.linkMode = nbttagcompound.getShort("linkMode");
        this.linkX = nbttagcompound.getInteger("linkX");
        this.linkY = nbttagcompound.getInteger("linkY");
        this.linkZ = nbttagcompound.getInteger("linkZ");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setShort("strength", (short)this.redstoneSignalStrength);
        nbttagcompound.setShort("linkMode", (short)this.linkMode);
        nbttagcompound.setInteger("linkX", this.linkX);
        nbttagcompound.setInteger("linkY", this.linkY);
        nbttagcompound.setInteger("linkZ", this.linkZ);
    }
}
