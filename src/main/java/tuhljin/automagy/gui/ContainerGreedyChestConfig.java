package tuhljin.automagy.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Arrays;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityGreedyChest;

public class ContainerGreedyChestConfig extends FilterContainer {
    private TileEntityGreedyChest teGC;
    private GUIGreedyChestConfig attachedGUI = null;
    private static final int PERCENTAGE_ADJUSTMENT = 10000;
    private SlotRestrictedWithReporting filterSlot;
    private int oldModePilfer = -1;
    private float oldFractionWanted = -1.0F;
    private int oldAmountWanted = -1;
    private boolean[] oldDirections = new boolean[]{true, true, true, true, true, true};

    public ContainerGreedyChestConfig(InventoryPlayer inventoryPlayer, TileEntityGreedyChest te) {
        this.teGC = te;

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + j + i * 9, 8 + j * 18, 84 + i * 18));
            }
        }

        this.filterSlot = new SlotRestrictedWithReporting(0, te.filterContainingInventory, 0, 98, 51);
        this.addSlotToContainer(this.filterSlot);
    }

    @SideOnly(Side.CLIENT)
    public void attachToGui(GUIGreedyChestConfig gui) {
        this.attachedGUI = gui;
        this.filterSlot.setParent(gui);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.teGC.isUseableByPlayer(player);
    }

    public void onCraftGuiOpened(ICrafting player) {
        super.onCraftGuiOpened(player);

        for (int i = 0; i < 6; i++) {
            player.sendProgressBarUpdate(this, i, this.teGC.directions[i] ? 1 : 0);
        }

        player.sendProgressBarUpdate(this, 6, this.teGC.modePilfer);
        player.sendProgressBarUpdate(this, 7, Math.round(this.teGC.fractionWanted * 10000.0F));
        player.sendProgressBarUpdate(this, 8, this.teGC.amountWanted);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 6:
                this.teGC.modePilfer = data;
                break;
            case 7:
                this.teGC.fractionWanted = data / 10000.0F;
                if (this.attachedGUI != null) {
                    String s = Integer.toString(data);
                    if (data < 10) {
                        s = "0.0" + s;
                    } else {
                        s = new StringBuilder(s).insert(s.length() - 2, TjUtil.decimalSeparator).toString();
                    }

                    this.attachedGUI.updatePercentage(s);
                }

                return;
            case 8:
                this.teGC.amountWanted = data;
                break;
            default:
                if (id >= 0 && id < 6) {
                    this.teGC.directions[id] = data == 1;
                }
        }

        if (this.attachedGUI != null) {
            this.attachedGUI.updateButton(id, data);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (Object player : this.crafters) {
            if (this.teGC.modePilfer != this.oldModePilfer) {
                ((ICrafting)player).sendProgressBarUpdate(this, 6, this.teGC.modePilfer);
            }

            if (!TjUtil.areFloatsAlmostEqual_lowprec(this.teGC.fractionWanted, this.oldFractionWanted)) {
                ((ICrafting)player).sendProgressBarUpdate(this, 7, Math.round(this.teGC.fractionWanted * 10000.0F));
            }

            if (this.teGC.amountWanted != this.oldAmountWanted) {
                ((ICrafting)player).sendProgressBarUpdate(this, 8, this.teGC.amountWanted);
            }

            for (int side = 0; side < 6; side++) {
                if (this.teGC.directions[side] != this.oldDirections[side]) {
                    ((ICrafting)player).sendProgressBarUpdate(this, side, this.teGC.directions[side] ? 1 : 0);
                }
            }
        }

        this.oldModePilfer = this.teGC.modePilfer;
        this.oldFractionWanted = this.teGC.fractionWanted;
        this.oldAmountWanted = this.teGC.amountWanted;
        this.oldDirections = Arrays.copyOf(this.teGC.directions, this.teGC.directions.length);
    }

    public TileEntityGreedyChest getTileEntity() {
        return this.teGC;
    }
}
