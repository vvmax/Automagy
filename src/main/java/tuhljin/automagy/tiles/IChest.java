package tuhljin.automagy.tiles;

import net.minecraft.inventory.ISidedInventory;

public interface IChest extends ISidedInventory {
    int getFacing();

    float getLidAngle();

    float getPrevLidAngle();
}
