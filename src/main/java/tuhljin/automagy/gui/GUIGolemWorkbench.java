package tuhljin.automagy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.tiles.TileEntityGolemWorkbench;

public class GUIGolemWorkbench extends ModGuiContainer {
    public final ResourceLocation texture = new ResourceLocation("Automagy:textures/gui/golemWorkbench.png");
    public TileEntityGolemWorkbench tile;
    protected GuiButtonHoverAndSound pagePrev;
    protected GuiButtonHoverAndSound pageNext;

    public GUIGolemWorkbench(InventoryPlayer inventoryPlayer, TileEntityGolemWorkbench te) {
        super(new ContainerGolemWorkbench(inventoryPlayer, te));
        this.ySize = 205;
        this.tile = te;
        this.scaleFactor = 0.5F;
    }

    @Override
    public void initGui() {
        super.initGui();
        if (this.tile.getNumRecipes() > 1) {
            this.pagePrev = new GuiButtonHoverAndSound(0, this.guiLeft + 64, this.guiTop + 68, 24, 8, this.texture, 0, 236, 0, 218, "thaumcraft:cameraclack");
            this.buttonList.add(this.pagePrev);
            this.pageNext = new GuiButtonHoverAndSound(1, this.guiLeft + 88, this.guiTop + 68, 24, 8, this.texture, 24, 236, 24, 218, "thaumcraft:cameraclack");
            this.buttonList.add(this.pageNext);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        if (this.tile.usingPlaceholders()) {
            this.drawTexturedModalRect(this.guiLeft + 7, this.guiTop + 85, 184, 85, 17, 17);
        }

        int index = this.getRecipeIndex();
        if (index > 0 || this.tile.getNumRecipes() > 1) {
            GL11.glPushMatrix();
            GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
            this.drawCenteredStringScaled(this.fontRendererObj, index + 1 + "/" + this.tile.getNumRecipes(), this.guiLeft + 120, this.guiTop + 70, 16777215);
            GL11.glPopMatrix();
        }
    }

    @Override
    protected void buttonClicked(GuiButton button, boolean rightClick) {
        int index = this.getRecipeIndex() + (button.id == 0 ? -1 : 1);
        if (index > this.tile.getNumRecipes() - 1) {
            index = 0;
        } else if (index < 0) {
            index = this.tile.getNumRecipes() - 1;
        }

        this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, index);
        ((ContainerGolemWorkbench)this.inventorySlots).recipeIndex = index;
        ((ContainerGolemWorkbench)this.inventorySlots).refreshInventory();
    }

    public int getRecipeIndex() {
        return ((ContainerGolemWorkbench)this.inventorySlots).getRecipeIndex();
    }
}
