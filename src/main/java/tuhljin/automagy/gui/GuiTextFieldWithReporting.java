package tuhljin.automagy.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;

public class GuiTextFieldWithReporting extends GuiTextField {
    public final int id;
    public ModGuiContainer parent;
    public String emptyMessage = null;
    private FontRenderer font;

    public GuiTextFieldWithReporting(int id, ModGuiContainer parent, FontRenderer fontRenderer, int xPos, int yPos, int width, int height) {
        super(fontRenderer, xPos, yPos, width, height);
        this.id = id;
        this.parent = parent;
        this.font = fontRenderer;
    }

    @Override
    public void setFocused(boolean flag) {
        boolean wasFocused = this.isFocused();
        super.setFocused(flag);
        if (!flag && wasFocused) {
            this.parent.onTextFieldLostFocus(this);
        }
    }

    @Override
    public boolean textboxKeyTyped(char c, int cI) {
        if (this.isFocused() && cI == 28) {
            this.setFocused(false);
            return true;
        } else {
            return super.textboxKeyTyped(c, cI);
        }
    }

    public boolean pointIsOver(int x, int y) {
        if (this.parent != null) {
            x = (int)(x / this.parent.scaleFactorTextField);
            y = (int)(y / this.parent.scaleFactorTextField);
        }

        return x >= this.xPosition && x < this.xPosition + this.width && y >= this.yPosition && y < this.yPosition + this.height;
    }

    public void setEmptyMessage(String str) {
        if (str != null && !str.isEmpty()) {
            this.emptyMessage = EnumChatFormatting.GRAY + "" + EnumChatFormatting.ITALIC + str;
        } else {
            this.emptyMessage = null;
        }
    }

    @Override
    public void drawTextBox() {
        super.drawTextBox();
        if (this.emptyMessage != null && this.getVisible() && this.getText().isEmpty() && !this.isFocused()) {
            int i = 14737632;
            int l = this.getEnableBackgroundDrawing() ? this.xPosition + 4 : this.xPosition;
            int i1 = this.getEnableBackgroundDrawing() ? this.yPosition + (this.height - 8) / 2 : this.yPosition;
            this.font.drawStringWithShadow(this.emptyMessage, l, i1, i);
        }
    }
}
