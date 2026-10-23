package tuhljin.automagy.tiles;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import tuhljin.automagy.lib.inventory.MirrorspaceItems;

public class TileEntityMirrorMultiDest extends ModTileEntityWithInventoryOneWay implements IInventariumMemoryUser {
    public int linkX;
    public int linkY = -1;
    public int linkZ;
    public final MirrorspaceItems pendingOutput = new MirrorspaceItems(this, "PendingOutput");

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote && this.pendingOutput.ejectRandom()) this.markDirty(false);
    }

    public TileEntityMirrorMultiDest() {
        super("blockMirrorMultiDest");
    }

    public TileEntityInventarium getLinkedInventarium() {
        if (this.linkY != -1) {
            TileEntity te = this.worldObj.getTileEntity(this.linkX, this.linkY, this.linkZ);
            if (te instanceof TileEntityInventarium) {
                return (TileEntityInventarium)te;
            }
        }

        return null;
    }

    public boolean transport(EntityItem entity) {
        TileEntityInventarium inventarium = this.getLinkedInventarium();
        if (inventarium != null) {
            ItemStack stack = entity.getEntityItem();
            int leftover = inventarium.deliverToAllRequestedDestinations(stack);
            if (leftover < stack.stackSize) {
                if (leftover <= 0) {
                    entity.setDead();
                } else {
                    ItemStack copy = stack.copy();
                    copy.stackSize = leftover;
                    entity.setEntityItemStack(copy);
                }

                TileEntityMirrorWithEye.addParticles(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
                return true;
            }
        }

        return false;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (stack == null || stack.getItem() == null || stack.stackSize <= 0) return;
        int leftover = stack.stackSize;
        TileEntityInventarium inventarium = this.getLinkedInventarium();
        if (inventarium != null) {
            leftover = inventarium.deliverToAllRequestedDestinations(stack);
            if (leftover < stack.stackSize) {
                TileEntityMirrorWithEye.addParticles(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            }
        }

        if (leftover > 0) {
            ItemStack copy = stack.copy();
            copy.stackSize = leftover;
            if (!this.spawnItem(copy)) {
                this.pendingOutput.add(copy);
                this.markDirty(false);
            }
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        TileEntityInventarium inventarium = this.getLinkedInventarium();
        return inventarium != null;
    }

    public boolean spawnItem(ItemStack stack) {
        try {
            ForgeDirection face = ForgeDirection.getOrientation(this.getBlockMetadata());
            EntityItem ie2 = new EntityItem(
                this.worldObj, this.xCoord + 0.5 - face.offsetX * 0.3, this.yCoord + 0.5 - face.offsetY * 0.3, this.zCoord + 0.5 - face.offsetZ * 0.3, stack
            );
            ie2.motionX = face.offsetX * 0.15F;
            ie2.motionY = face.offsetY * 0.15F;
            ie2.motionZ = face.offsetZ * 0.15F;
            ie2.timeUntilPortal = 20;
            return this.worldObj.spawnEntityInWorld(ie2);
        } catch (Exception var4) {
            return false;
        }
    }

    @Override
    public void breakLink() {
        this.linkY = -1;
        this.markDirty();
    }

    @Override
    public void setLink(TileEntityInventarium teInv) {
        this.linkX = teInv.xCoord;
        this.linkY = teInv.yCoord;
        this.linkZ = teInv.zCoord;
        this.markDirty();
    }

    @Override
    public boolean isLinkedTo(TileEntityInventarium teInv) {
        return teInv.xCoord == this.linkX && teInv.yCoord == this.linkY && teInv.zCoord == this.linkZ;
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInteger("linkX", this.linkX);
        nbttagcompound.setInteger("linkY", this.linkY);
        nbttagcompound.setInteger("linkZ", this.linkZ);
        this.pendingOutput.writeToNBT(nbttagcompound);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.linkX = nbttagcompound.getInteger("linkX");
        this.linkY = nbttagcompound.getInteger("linkY");
        this.linkZ = nbttagcompound.getInteger("linkZ");
        this.pendingOutput.readFromNBT(nbttagcompound);
    }
}
