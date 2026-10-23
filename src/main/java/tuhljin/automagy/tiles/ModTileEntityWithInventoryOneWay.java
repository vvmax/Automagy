package tuhljin.automagy.tiles;

import net.minecraft.item.ItemStack;

public abstract class ModTileEntityWithInventoryOneWay extends ModTileEntityWithInventory {
    public ModTileEntityWithInventoryOneWay(String inventoryName) {
        this(inventoryName, true);
    }

    public ModTileEntityWithInventoryOneWay(String inventoryName, boolean createDefaultAccessibleSlots) {
        super(inventoryName, 1, createDefaultAccessibleSlots);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int num) {
        return null;
    }

    @Override
    public boolean canExtractItem(int var1, ItemStack var2, int var3) {
        return false;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }
}
