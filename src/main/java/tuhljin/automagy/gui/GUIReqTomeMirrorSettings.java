package tuhljin.automagy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.tiles.IInventariumBookLinksMirror;
import tuhljin.automagy.tiles.IMirrorVariant;

public class GUIReqTomeMirrorSettings extends ModGuiContainer {
    public final ResourceLocation texture = new ResourceLocation("Automagy:textures/gui/reqEyes.png");
    protected IInventariumBookLinksMirror book;
    protected boolean showOutputSettings;

    public GUIReqTomeMirrorSettings(InventoryPlayer playerInventory, IInventariumBookLinksMirror book, boolean showOutputSettings) {
        super(new ContainerReqTomeMirrorSettings(playerInventory, book, showOutputSettings));
        this.book = book;
        this.showOutputSettings = showOutputSettings;
    }

    @Override
    protected void buttonClicked(GuiButton button, boolean rightClick) {
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        if (this.showOutputSettings) {
            this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        } else {
            int off = 78;
            this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize - 80, this.ySize - off);
            this.drawTexturedModalRect(this.guiLeft, this.guiTop + off, 0, off, this.xSize, this.ySize);
        }

        GL11.glPushMatrix();
        GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        int y = this.guiTop + 31;
        this.drawStringScaled(this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.scribeMirror.input"), this.guiLeft + 42, y, 16777215);
        if (this.showOutputSettings) {
            this.drawStringScaled(this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.scribeMirror.output"), this.guiLeft + 129.5F, y, 16777215);
        }

        GL11.glPopMatrix();
        float prevScaleFactor = this.scaleFactor;
        this.scaleFactor = 0.6F;
        GL11.glPushMatrix();
        GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        y = this.guiTop + 40;
        if (this.book.hasInputEye()) {
            int x = this.guiLeft + 42;
            if (this.book.isInputMirrorInRange()) {
                IMirrorVariant mirror = this.book.getInputMirror();
                if (mirror != null) {
                    if (mirror.isLinked()) {
                        this.drawStringScaled(this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.scribeMirror.valid"), x, y, 52224);
                    } else {
                        this.drawStringScaled(this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.scribeMirror.unlinked"), x, y, 16711680);
                    }
                } else {
                    this.drawStringScaled(this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.scribeMirror.noMirror"), x, y, 16711680);
                }
            } else {
                this.drawStringScaled(this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.scribeMirror.outranged"), x, y, 16711680);
            }
        }

        if (this.showOutputSettings && this.book.hasOutputEye()) {
            float x = this.guiLeft + 129.5F;
            if (this.book.isOutputMirrorInRange()) {
                IMirrorVariant mirror = this.book.getOutputMirror();
                if (mirror != null) {
                    if (mirror.isLinked()) {
                        this.drawStringScaled(this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.scribeMirror.valid"), x, y, 52224);
                    } else {
                        this.drawStringScaled(this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.scribeMirror.unlinked"), x, y, 16711680);
                    }
                } else {
                    this.drawStringScaled(this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.scribeMirror.noMirror"), x, y, 16711680);
                }
            } else {
                this.drawStringScaled(this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.scribeMirror.outranged"), x, y, 16711680);
            }
        }

        GL11.glPopMatrix();
        this.scaleFactor = prevScaleFactor;
    }
}
