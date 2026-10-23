package tuhljin.automagy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiButtonHoverAndSound extends GuiButton {
    protected ResourceLocation texture;
    protected int texX;
    protected int texY;
    protected int texXHover;
    protected int texYHover;
    public String sound;

    public GuiButtonHoverAndSound(
        int id, int x, int y, int width, int height, ResourceLocation texture, int texX, int texY, int texXHover, int texYHover, String sound
    ) {
        super(id, x, y, width, height, "turnpage");
        this.texture = texture;
        this.texX = texX;
        this.texY = texY;
        this.texXHover = texXHover;
        this.texYHover = texYHover;
        this.sound = sound;
    }

    @Override
    public void drawButton(Minecraft mc, int x, int y) {
        if (this.visible) {
            int tX;
            int tY;
            if (this.pointIsOver(x, y)) {
                tX = this.texXHover;
                tY = this.texYHover;
            } else {
                tX = this.texX;
                tY = this.texY;
            }

            Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(3042);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(770, 771);
            this.drawTexturedModalRect(this.xPosition, this.yPosition, tX, tY, this.width, this.height);
            this.mouseDragged(mc, x, y);
        }
    }

    public boolean pointIsOver(int x, int y) {
        return x >= this.xPosition && x < this.xPosition + this.width && y >= this.yPosition && y < this.yPosition + this.height;
    }

    public void playPressSound(SoundHandler soundsHandler) {
        soundsHandler.playSound(PositionedSoundRecord.createPositionedSoundRecord(new ResourceLocation(this.sound), 1.0F));
    }
}
