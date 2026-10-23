package tuhljin.automagy.gui;

import net.minecraft.inventory.IInventory;
import thaumcraft.common.container.SlotGhost;

public class SlotGhostSingle extends SlotGhost {
    public SlotGhostSingle(IInventory par1iInventory, int par2, int par3, int par4, int par5) {
        super(par1iInventory, par2, par3, par4, par5);
    }

    public SlotGhostSingle(IInventory par1iInventory, int par2, int par3, int par4) {
        super(par1iInventory, par2, par3, par4);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
