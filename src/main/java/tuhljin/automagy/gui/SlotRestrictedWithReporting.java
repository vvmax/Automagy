package tuhljin.automagy.gui;

import net.minecraft.inventory.IInventory;

public class SlotRestrictedWithReporting extends SlotRestricted {
    public final int id;
    public IReporteeForSlot reportTo;

    public SlotRestrictedWithReporting(int id, IInventory inventory, int slotIndex, int x, int y) {
        super(inventory, slotIndex, x, y);
        this.id = id;
    }

    public void setParent(IReporteeForSlot parent) {
        this.reportTo = parent;
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        if (this.reportTo != null) {
            this.reportTo.onSlotChanged(this);
        }
    }
}
