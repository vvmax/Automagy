package tuhljin.automagy.lib.compat.nei;

import codechicken.nei.guihook.IContainerObjectHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.gui.GuiButtonPseudoSlot;

public class NEIHandlePseudoSlots implements IContainerObjectHandler {
    public void guiTick(GuiContainer gui) {
    }

    public void refresh(GuiContainer gui) {
    }

    public void load(GuiContainer gui) {
    }

    public ItemStack getStackUnderMouse(GuiContainer gui, int mousex, int mousey) {
        if (gui instanceof INEICompatPseudoSlots) {
            GuiButtonPseudoSlot pSlot = ((INEICompatPseudoSlots)gui).getPseudoSlotAt(mousex, mousey);
            if (pSlot != null) {
                return pSlot.getItemStack();
            }
        }

        return null;
    }

    public boolean objectUnderMouse(GuiContainer gui, int mousex, int mousey) {
        return false;
    }

    public boolean shouldShowTooltip(GuiContainer gui) {
        return true;
    }
}
