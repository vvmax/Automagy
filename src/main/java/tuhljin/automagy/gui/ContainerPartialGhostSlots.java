package tuhljin.automagy.gui;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Method;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Level;
import thaumcraft.common.container.ContainerGhostSlots;

public abstract class ContainerPartialGhostSlots extends Container {
    public final int ghostSlotStart;
    public final int ghostSlotEnd;
    public boolean ghostLimitStackSize;
    protected final ContainerGhostSlots ghostContainer = new ContainerGhostSlots();
    protected Method addGhostSlotMethod;

    public ContainerPartialGhostSlots(int ghostSlotStart, int ghostSlotEnd) {
        this.ghostSlotStart = ghostSlotStart;
        this.ghostSlotEnd = ghostSlotEnd;
        this.addGhostSlotMethod = ReflectionHelper.findMethod(
            Container.class, this.ghostContainer, new String[]{"addSlotToContainer", "func_75146_a"}, Slot.class
        );
    }

    @Override
    protected Slot addSlotToContainer(Slot slot) {
        try {
            this.addGhostSlotMethod.invoke(this.ghostContainer, slot);
        } catch (Exception var3) {
            FMLLog.log(Level.ERROR, "[Automagy] Failed to update mirrored ghost slot.");
            var3.printStackTrace();
        }

        return super.addSlotToContainer(slot);
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer player) {
        return slotId >= this.ghostSlotStart && slotId <= this.ghostSlotEnd
            ? this.slotClickAsGhost(slotId, clickedButton, mode, player)
            : super.slotClick(slotId, clickedButton, mode, player);
    }

    public ItemStack slotClickAsGhost(int slotClicked, int button, int mode, EntityPlayer player) {
        return this.ghostContainer.slotClick(slotClicked, button, mode, player);
    }
}
