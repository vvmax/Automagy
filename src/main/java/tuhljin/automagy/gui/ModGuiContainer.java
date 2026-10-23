package tuhljin.automagy.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public abstract class ModGuiContainer extends GuiContainer implements IReporteeForSlot {
    protected float scaleFactor = 0.75F;
    protected float scaleFactorTextField = 0.75F;
    protected List<GuiTextField> textFieldList;

    public ModGuiContainer(Container container) {
        super(container);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.textFieldList = new ArrayList<>();
        AutomagyGUIHandler.hideCrosshairs = true;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        AutomagyGUIHandler.hideCrosshairs = false;
    }

    public void drawStringScaled(FontRenderer par1FontRenderer, String par2Str, float par3, float par4, int par5) {
        this.drawString(par1FontRenderer, par2Str, (int)(par3 / this.scaleFactor), (int)(par4 / this.scaleFactor), par5);
    }

    public void drawStringScaledNoShadow(FontRenderer par1FontRenderer, String par2Str, float par3, float par4, int par5) {
        par1FontRenderer.drawString(par2Str, (int)(par3 / this.scaleFactor), (int)(par4 / this.scaleFactor), par5);
    }

    public void drawCenteredStringScaled(FontRenderer par1FontRenderer, String par2Str, int par3, int par4, int par5) {
        this.drawCenteredString(par1FontRenderer, par2Str, (int)(par3 / this.scaleFactor), (int)(par4 / this.scaleFactor), par5);
    }

    public void drawCenteredStringScaledNoShadow(FontRenderer par1FontRenderer, String par2Str, int par3, int par4, int par5) {
        par3 = (int)(par3 / this.scaleFactor);
        par4 = (int)(par4 / this.scaleFactor);
        par1FontRenderer.drawString(par2Str, par3 - par1FontRenderer.getStringWidth(par2Str) / 2, par4, par5);
    }

    protected void drawScaledHoveringText(List list, int x, int y, FontRenderer font) {
        this.drawHoveringText(list, (int)(x / this.scaleFactor), (int)(y / this.scaleFactor), font);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        this.buttonClicked(button, false);
    }

    protected abstract void buttonClicked(GuiButton var1, boolean var2);

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        if (par3 == 1) {
            for (int l = 0; l < this.buttonList.size(); l++) {
                GuiButton guibutton = (GuiButton)this.buttonList.get(l);
                if (guibutton.mousePressed(this.mc, par1, par2)) {
                    guibutton.playPressSound(this.mc.getSoundHandler());
                    this.buttonClicked(guibutton, true);
                }
            }
        }

        super.mouseClicked(par1, par2, par3);
        if (this.textFieldList.size() > 0) {
            int scaleAdjusted1 = (int)(par1 / this.scaleFactorTextField);
            int scaleAdjusted2 = (int)(par2 / this.scaleFactorTextField);

            for (GuiTextField input : this.textFieldList) {
                if (input.getVisible()) {
                    input.mouseClicked(scaleAdjusted1, scaleAdjusted2, par3);
                    if (par3 == 1 && input.isFocused()) {
                        input.setText("");
                    }
                }
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        for (GuiTextField input : this.textFieldList) {
            input.updateCursorCounter();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        if (this.textFieldList.size() > 0) {
            GL11.glPushMatrix();
            GL11.glScalef(this.scaleFactorTextField, this.scaleFactorTextField, this.scaleFactorTextField);
            GL11.glDisable(2896);
            GL11.glDisable(3042);

            for (GuiTextField input : this.textFieldList) {
                input.drawTextBox();
            }

            GL11.glPopMatrix();
        }
    }

    @Override
    protected void keyTyped(char c, int cI) {
        boolean actionKey;
        boolean controlKey;
        if (isCtrlKeyDown()) {
            actionKey = true;
            controlKey = true;
        } else {
            actionKey = cI == 14 || cI == 211 || cI == 203 || cI == 205 || cI == 199 || cI == 207 || cI == 28;
            controlKey = false;
        }

        for (GuiTextField input : this.textFieldList) {
            if (actionKey || this.textFieldAllowsCharacter(input, c, input.getText())) {
                String text = input.getText();
                if (input.textboxKeyTyped(c, cI)) {
                    String newText;
                    if (controlKey) {
                        String testText = input.getText();
                        if (!testText.equals(text)) {
                            StringBuilder sb = new StringBuilder();

                            for (int i = 0; i < testText.length(); i++) {
                                char c2 = testText.charAt(i);
                                if (this.textFieldAllowsCharacter(input, c2, sb.toString())) {
                                    sb.append(c2);
                                }
                            }

                            newText = sb.toString();
                            if (!testText.equals(newText)) {
                                input.setText(newText);
                            }
                        } else {
                            newText = testText;
                        }
                    } else {
                        newText = input.getText();
                    }

                    if (!text.equals(newText)) {
                        this.onTextFieldChanged(input, newText);
                    }

                    return;
                }
            }
        }

        super.keyTyped(c, cI);
    }

    protected void onTextFieldChanged(GuiTextField textField, String newText) {
    }

    protected boolean textFieldAllowsCharacter(GuiTextField textField, char c, String currentText) {
        return true;
    }

    public void onTextFieldLostFocus(GuiTextFieldWithReporting textField) {
    }

    @Override
    public void onSlotChanged(SlotRestrictedWithReporting slot) {
    }

    public void drawItemStackAndString(ItemStack stack, int p_146982_2_, int p_146982_3_, String str) {
        GL11.glPushMatrix();
        GL11.glEnable(2929);
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glTranslatef(0.0F, 0.0F, 32.0F);
        this.zLevel = 200.0F;
        itemRender.zLevel = 200.0F;
        FontRenderer font = null;
        if (stack != null) {
            font = stack.getItem().getFontRenderer(stack);
        }

        if (font == null) {
            font = this.fontRendererObj;
        }

        itemRender.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), stack, p_146982_2_, p_146982_3_);
        itemRender.renderItemOverlayIntoGUI(font, this.mc.getTextureManager(), stack, p_146982_2_, p_146982_3_, "");
        GL11.glPushMatrix();
        GL11.glDisable(2929);
        GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        this.drawStringScaled(font, str, p_146982_2_ + 15 - font.getStringWidth(str) * this.scaleFactor, p_146982_3_ + 11, 16777215);
        GL11.glPopMatrix();
        this.zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
        GL11.glPopMatrix();
    }
}
