package tuhljin.automagy.tiles;

import java.util.ArrayList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.items.ItemCrystalEye;
import tuhljin.automagy.lib.IAutomagyLocationLink;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.InventorySimple;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

public class InventoryObjectEyes extends InventorySimple {
    protected boolean checkCoordinates;
    private IContainsEyes parent;

    public InventoryObjectEyes(IContainsEyes parent, String name, int numSlots, boolean checkCoordinates) {
        super(name, numSlots, "CrystalEyeItems");
        this.parent = parent;
        this.checkCoordinates = checkCoordinates;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (stack != null) {
            Item item = stack.getItem();
            if (item instanceof ItemCrystalEye) {
                WorldSpecificCoordinates coord = ((ItemCrystalEye)item).getLinkLocation(stack);
                if (coord != null) {
                    return !this.checkCoordinates || this.coordinatesAreInRange(coord);
                }
            }
        }

        return false;
    }

    @Override
    public void onInventoryChanged(int slot, ItemStack prevStack) {
        this.parent.onEyesInventoryChanged(slot, prevStack);
    }

    public boolean coordinatesAreInRange(WorldSpecificCoordinates coord) {
        if (coord != null && coord.dim == this.parent.getDimensionID()) {
            int range = this.parent.getMaxDistance();
            if (range >= 0) {
                BlockCoord parentPos = this.parent.getPosition();
                if (coord.x == parentPos.x && coord.y == parentPos.y && coord.z == parentPos.z) {
                    return false;
                }

                float distance = TjUtil.getDistanceBetweenPoints(parentPos.x, parentPos.y, parentPos.z, coord.x, coord.y, coord.z);
                return distance <= range;
            }
        }

        return false;
    }

    public ArrayList<ItemStack> removeInvalidEyes() {
        ArrayList<ItemStack> list = new ArrayList<>();
        int size = this.getSizeInventory();

        for (int i = 0; i < size; i++) {
            ItemStack stack = this.getStackInSlot(i);
            if (stack != null && !this.isItemValidForSlot(i, stack)) {
                list.add(stack);
                this.setInventorySlotContents(i, null);
            }
        }

        return list;
    }

    public void organizeSlots() {
        ItemStack[] newSlots = new ItemStack[this.numSlots];
        int count = 0;

        for (int i = 0; i < this.numSlots; i++) {
            if (this.inventorySlots[i] != null) {
                newSlots[count] = this.inventorySlots[i];
                count++;
            }
        }

        this.inventorySlots = newSlots;
    }

    public WorldSpecificCoordinates getCoordinates(int i) {
        ItemStack stack = this.getStackInSlot(i);
        return stack != null && stack.getItem() instanceof IAutomagyLocationLink ? ((IAutomagyLocationLink)stack.getItem()).getLinkLocation(stack) : null;
    }
}
