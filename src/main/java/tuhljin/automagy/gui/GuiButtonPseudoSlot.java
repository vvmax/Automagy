package tuhljin.automagy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.lib.TjUtil;

public class GuiButtonPseudoSlot extends GuiButton {
    protected ItemStack stack;
    private ModGuiContainer parent;

    public GuiButtonPseudoSlot(int id, ModGuiContainer parent, int x, int y) {
        super(id, x, y, 16, 16, "pseudoSlot");
        this.parent = parent;
    }

    @Override
    public void drawButton(Minecraft mc, int x, int y) {
        if (this.visible) {
            if (this.stack != null) {
                this.parent.drawItemStackAndString(this.stack, this.xPosition, this.yPosition, TjUtil.numberFormatLetter(this.stack.stackSize));
            }

            this.mouseDragged(mc, x, y);
        }
    }

    public void setItemStack(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getItemStack() {
        return this.stack;
    }

    public boolean pointIsOver(int x, int y) {
        return x >= this.xPosition && x < this.xPosition + this.width && y >= this.yPosition && y < this.yPosition + this.height;
    }

    public void playPressSound(SoundHandler p_146113_1_) {
    }
}
