package tuhljin.automagy.tiles;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.AxisAlignedBB;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.lib.inventory.FilteringItemList;

public class TileEntityTallyWorld extends TileEntityTallyBase {
    public static double EXTRARANGE = 0.1;

    public TileEntityTallyWorld() {
        super("blockTallyLensWorld");
    }

    @Override
    public FilteringItemList getDetectedItems() {
        BlockCoord bc = this.getUltimateTarget();
        if (bc == null) {
            return null;
        } else {
            FilteringItemList list = new FilteringItemList();
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                this.targetX - EXTRARANGE,
                this.targetY - EXTRARANGE,
                this.targetZ - EXTRARANGE,
                this.targetX + EXTRARANGE + 1.0,
                this.targetY + EXTRARANGE + 1.0,
                this.targetZ + EXTRARANGE + 1.0
            );

            for (EntityItem item : this.worldObj.getEntitiesWithinAABB(EntityItem.class, box)) {
                list.add(item.getEntityItem());
            }

            return list;
        }
    }

    @Override
    public boolean hasValidTarget() {
        return true;
    }
}
