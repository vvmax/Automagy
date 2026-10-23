package tuhljin.automagy.tiles;

import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.FilteringItemList;

public class TileEntityTallyDrops extends TileEntityTallyBase {
    public TileEntityTallyDrops() {
        super("blockTallyLensDrops");
    }

    @Override
    public FilteringItemList getDetectedItems() {
        BlockCoord bc = this.getUltimateTarget();
        if (bc == null) {
            return null;
        } else {
            ArrayList<ItemStack> drops = null;
            drops = TjUtil.getDropsFromBlock(this.worldObj, bc.x, bc.y, bc.z, true);
            return drops != null && drops.size() > 0 ? new FilteringItemList(drops, false) : new FilteringItemList();
        }
    }

    @Override
    public boolean hasValidTarget() {
        return this.targetY != -1 && !this.worldObj.isAirBlock(this.targetX, this.targetY, this.targetZ);
    }
}
