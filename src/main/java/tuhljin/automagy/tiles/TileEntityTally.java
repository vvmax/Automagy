package tuhljin.automagy.tiles;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.lib.inventory.FilteringItemList;

public class TileEntityTally extends TileEntityTallyBase {
    public TileEntityTally() {
        super("blockTally");
    }

    @Override
    protected void foundNewDirectTarget() {
        if (TileEntityInventarium.blockIsBookshelf(this.worldObj, this.targetX, this.targetY, this.targetZ)) {
            label25:
            for (int xf = this.targetX - 2; xf <= this.targetX + 2; xf++) {
                for (int zf = this.targetZ - 2; zf <= this.targetZ + 2; zf++) {
                    if (this.worldObj.getTileEntity(xf, this.targetY + 1, zf) instanceof TileEntityInventarium) {
                        this.targetX = xf;
                        this.targetY++;
                        this.targetZ = zf;
                        break label25;
                    }
                }
            }
        }

        super.foundNewDirectTarget();
    }

    @Override
    public FilteringItemList getDetectedItems() {
        BlockCoord bc = this.getUltimateTarget();
        if (bc == null) {
            return null;
        } else {
            TileEntity target = this.worldObj.getTileEntity(bc.x, bc.y, bc.z);
            if (target == null) {
                return null;
            } else if (target instanceof IInventory) {
                return new FilteringItemList().populateFromInventory((IInventory)target, false);
            } else {
                return target instanceof IHasItemList ? ((IHasItemList)target).getItemList() : null;
            }
        }
    }

    @Override
    public boolean hasValidTarget() {
        if (this.targetY != -1) {
            TileEntity target = this.worldObj.getTileEntity(this.targetX, this.targetY, this.targetZ);
            if (target instanceof IInventory || target instanceof IHasItemList) {
                return true;
            }
        }

        return false;
    }
}
