package tuhljin.automagy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiButtonScaledText extends GuiButton {
    protected static final ResourceLocation buttonTexturesRecolor = new ResourceLocation("Automagy:textures/gui/widgetsRecolor.png");
    public boolean texturesRecolored = false;
    private float scaleFactor;
    private boolean mouseIsHoveringOver = false;

    public GuiButtonScaledText(int id, int x, int y, int width, int height, String text, float scaleFactor, boolean recolorTextures) {
        super(id, x, y, width, height, text);
        this.scaleFactor = scaleFactor;
        this.texturesRecolored = recolorTextures;
    }

    public GuiButtonScaledText(int id, int x, int y, int width, int height, String text, float scaleFactor) {
        this(id, x, y, width, height, text, scaleFactor, false);
    }

    public GuiButtonScaledText(int id, int x, int y, String text, float scaleFactor, boolean recolorTextures) {
        this(id, x, y, 200, 20, text, scaleFactor, recolorTextures);
    }

    public GuiButtonScaledText(int id, int x, int y, String text, float scaleFactor) {
        this(id, x, y, 200, 20, text, scaleFactor, false);
    }

    @Override
    public void drawCenteredString(FontRenderer par1FontRenderer, String par2Str, int par3, int par4, int par5) {
        GL11.glPushMatrix();
        GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        super.drawCenteredString(par1FontRenderer, par2Str, (int)(par3 / this.scaleFactor), (int)(par4 / this.scaleFactor), par5);
        GL11.glPopMatrix();
    }

    @Override
    public void drawButton(Minecraft mc, int x, int y) {
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRendererObj;
            mc.getTextureManager().bindTexture(this.texturesRecolored ? buttonTexturesRecolor : buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;
            int k = this.getHoverState(this.hovered);
            GL11.glEnable(3042);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(770, 771);
            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + k * 20, this.width / 2, this.height / 2);
            this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height / 2);
            this.drawTexturedModalRect(this.xPosition, this.yPosition + this.height / 2, 0, 66 + k * 20 - this.height / 2, this.width / 2, this.height / 2);
            this.drawTexturedModalRect(
                this.xPosition + this.width / 2,
                this.yPosition + this.height / 2,
                200 - this.width / 2,
                66 + k * 20 - this.height / 2,
                this.width / 2,
                this.height / 2
            );
            this.mouseDragged(mc, x, y);
            int l = 14737632;
            if (this.packedFGColour != 0) {
                l = this.packedFGColour;
            } else if (!this.enabled) {
                l = 10526880;
            } else if (this.hovered) {
                l = 16777120;
            }

            this.drawCenteredString(
                fontrenderer, this.displayString, this.xPosition + this.width / 2, (int)(this.yPosition + (this.height - 8.0F * this.scaleFactor) / 2.0F), l
            );
            this.mouseIsHoveringOver = k == 2;
        }
    }

    public boolean isMouseHoveringOver() {
        return this.mouseIsHoveringOver;
    }
}
