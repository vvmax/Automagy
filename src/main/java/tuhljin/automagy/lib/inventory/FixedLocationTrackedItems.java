package tuhljin.automagy.lib.inventory;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

@Deprecated
public class FixedLocationTrackedItems {
    protected static Map<WorldSpecificCoordinates, FilteringItemList> map = new HashMap<>();

    public static FilteringItemList getItemListAt(World world, int x, int y, int z, boolean checkExtract) {
        WorldSpecificCoordinates coord = new WorldSpecificCoordinates(world, x, y, z);
        FilteringItemList list = map.get(coord);
        if (list == null) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof IInventory) {
                list = new FilteringItemList().populateFromInventory((IInventory)te, checkExtract);
                map.put(coord, list);
            }
        }

        return list;
    }
}
