package tuhljin.automagy.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.common.tiles.TileMirror;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.lib.IAutomagyLocationLink;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

public abstract class TileWithEyes extends ModTileEntity implements IContainsEyes {
    public InventoryObjectEyes eyesContainingInventory;
    protected int eyesMaxCount;
    protected int eyesMaxDistance;
    protected int eyeCount = 0;

    public TileWithEyes(String eyeInvName, int maxEyes, int maxDistance) {
        this.eyesContainingInventory = new InventoryObjectEyes(this, eyeInvName, maxEyes, maxDistance > 0);
        this.eyesMaxDistance = maxDistance;
        this.eyesMaxCount = maxEyes;
    }

    @Override
    public int getDimensionID() {
        return this.worldObj.provider.dimensionId;
    }

    @Override
    public BlockCoord getPosition() {
        return new BlockCoord(this);
    }

    @Override
    public int getMaxDistance() {
        return this.eyesMaxDistance;
    }

    @Override
    public void onEyesInventoryChanged(int slot, ItemStack prevStack) {
    }

    public ItemStack removeEye() {
        if (this.eyeCount > 0) {
            int slot = this.eyeCount - 1;
            ItemStack stack = this.eyesContainingInventory.getStackInSlot(slot);
            this.eyesContainingInventory.setInventorySlotContents(slot, null);
            this.eyeCount--;
            this.markDirty();
            return stack;
        } else {
            return null;
        }
    }

    public boolean insertEye(ItemStack stack) {
        if (this.eyeCount < this.eyesMaxCount) {
            this.eyesContainingInventory.setInventorySlotContents(this.eyeCount, stack.splitStack(1));
            this.eyeCount++;
            this.markDirty();
            return true;
        } else {
            return false;
        }
    }

    public boolean itemIsValidEye(ItemStack stack) {
        return this.eyesContainingInventory.isItemValidForSlot(this.eyeCount, stack);
    }

    public int getEyeCount() {
        return this.eyeCount;
    }

    public boolean isEyeLinkedToPairedMirror(ItemStack stack) {
        if (stack.getItem() instanceof IAutomagyLocationLink) {
            WorldSpecificCoordinates coord = ((IAutomagyLocationLink)stack.getItem()).getLinkLocation(stack);
            if (coord != null) {
                World targetWorld = DimensionManager.getWorld(coord.dim);
                if (targetWorld == null) {
                    return false;
                }

                TileEntity te = targetWorld.getTileEntity(coord.x, coord.y, coord.z);
                if (te != null && te instanceof TileMirror) {
                    TileMirror tm = (TileMirror)te;
                    if (tm.linked) {
                        return true;
                    }

                    return false;
                }

                return false;
            }
        }

        return false;
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        this.eyesContainingInventory.writeCustomNBT(nbttagcompound);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.eyesContainingInventory.readCustomNBT(nbttagcompound);
        this.eyeCount = 0;
        int size = this.eyesContainingInventory.getSizeInventory();

        for (int i = 0; i < size; i++) {
            if (this.eyesContainingInventory.getStackInSlot(i) != null) {
                this.eyeCount++;
            }
        }
    }
}
