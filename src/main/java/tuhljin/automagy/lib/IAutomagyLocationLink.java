package tuhljin.automagy.lib;

import net.minecraft.item.ItemStack;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

public interface IAutomagyLocationLink {
    WorldSpecificCoordinates getLinkLocation(ItemStack var1);
}
