package tuhljin.automagy.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Arrays;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.tiles.TileEntityVisReader;

public class ContainerVisReader extends Container {
    private static final int numConfigurations = 4;
    private TileEntityVisReader teVR;
    public GUIVisReader attachedGUI = null;
    private int[] oldModes = new int[4];
    private int[] oldCapacities = new int[4];
    private int[] oldOutputDirs = new int[4];
    private boolean[] oldInverseSignals = new boolean[4];

    public ContainerVisReader(InventoryPlayer inventoryPlayer, TileEntityVisReader te) {
        this.teVR = te;

        for (int i = 0; i < 4; i++) {
            this.addSlotToContainer(new SlotRestricted(this.teVR, i, 6, 10 + i * 19));
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 154));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + j + i * 9, 8 + j * 18, 96 + i * 18));
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.teVR.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum) {
        ItemStack stack = null;
        Slot slot = (Slot)this.inventorySlots.get(slotNum);
        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();
            if (slot instanceof SlotRestricted) {
                if (!this.mergeItemStack(stackInSlot, 4, 40, true)) {
                    return null;
                }
            } else {
                if (!this.teVR.isItemValidForSlot(0, stackInSlot)) {
                    return null;
                }

                if (!this.mergeSingleItemFromStack(stackInSlot, 0, 4, false)) {
                    return null;
                }
            }

            if (stackInSlot.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (stackInSlot.stackSize == stack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, stackInSlot);
        }

        return stack;
    }

    public void onCraftGuiOpened(ICrafting player) {
        super.onCraftGuiOpened(player);

        for (int i = 0; i < 4; i++) {
            player.sendProgressBarUpdate(this, i, this.teVR.modes[i]);
            player.sendProgressBarUpdate(this, i + 10, this.teVR.capacities[i]);
            player.sendProgressBarUpdate(this, i + 20, this.teVR.outputDirs[i]);
            player.sendProgressBarUpdate(this, i + 30, this.teVR.inverseSignals[i] ? 1 : 0);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int data) {
        if (id >= 30) {
            id -= 30;
            this.teVR.inverseSignals[id] = data == 1;
            if (this.attachedGUI != null) {
                this.attachedGUI.updateButton(id * 4 + 3, data);
            }
        } else if (id >= 20) {
            id -= 20;
            this.teVR.outputDirs[id] = data;
            if (this.attachedGUI != null) {
                this.attachedGUI.updateButton(id * 4 + 2, data);
            }
        } else if (id >= 10) {
            id -= 10;
            this.teVR.capacities[id] = data;
            if (this.attachedGUI != null) {
                this.attachedGUI.updateButton(id * 4 + 1, data);
            }
        } else {
            this.teVR.modes[id] = data;
            if (this.attachedGUI != null) {
                this.attachedGUI.updateButton(id * 4, data);
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (Object player : this.crafters) {
            for (int i = 0; i < 4; i++) {
                if (this.teVR.modes[i] != this.oldModes[i]) {
                    ((ICrafting)player).sendProgressBarUpdate(this, i, this.teVR.modes[i]);
                }

                if (this.teVR.capacities[i] != this.oldCapacities[i]) {
                    ((ICrafting)player).sendProgressBarUpdate(this, i + 10, this.teVR.capacities[i]);
                }

                if (this.teVR.outputDirs[i] != this.oldOutputDirs[i]) {
                    ((ICrafting)player).sendProgressBarUpdate(this, i + 20, this.teVR.outputDirs[i]);
                }

                if (this.teVR.inverseSignals[i] != this.oldInverseSignals[i]) {
                    ((ICrafting)player).sendProgressBarUpdate(this, i + 30, this.teVR.inverseSignals[i] ? 1 : 0);
                }
            }
        }

        this.oldModes = Arrays.copyOf(this.teVR.modes, this.teVR.modes.length);
        this.oldCapacities = Arrays.copyOf(this.teVR.capacities, this.teVR.capacities.length);
        this.oldOutputDirs = Arrays.copyOf(this.teVR.outputDirs, this.teVR.outputDirs.length);
        this.oldInverseSignals = Arrays.copyOf(this.teVR.inverseSignals, this.teVR.inverseSignals.length);
    }

    public TileEntityVisReader getTileEntity() {
        return this.teVR;
    }

    protected boolean mergeSingleItemFromStack(ItemStack stack, int firstSlot, int numSlots, boolean reverse) {
        boolean flag1 = false;
        if (reverse) {
            int k = numSlots - 1;
        }

        if (stack.stackSize > 0) {
            int var10;
            if (reverse) {
                var10 = numSlots - 1;
            } else {
                var10 = firstSlot;
            }

            while (!reverse && var10 < numSlots || reverse && var10 >= firstSlot) {
                Slot slot = (Slot)this.inventorySlots.get(var10);
                ItemStack itemstack1 = slot.getStack();
                if (itemstack1 == null) {
                    ItemStack newStack = stack.copy();
                    newStack.stackSize = 1;
                    slot.putStack(newStack);
                    slot.onSlotChanged();
                    stack.stackSize--;
                    flag1 = true;
                    break;
                }

                if (reverse) {
                    var10--;
                } else {
                    var10++;
                }
            }
        }

        return flag1;
    }
}
