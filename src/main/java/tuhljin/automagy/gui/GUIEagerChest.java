package tuhljin.automagy.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.tiles.TileEntityEagerChest;

public class GUIEagerChest extends GuiContainer {
    public TileEntityEagerChest te;
    private InventoryPlayer inventoryPlayer;
    public final ResourceLocation texture = new ResourceLocation("textures/gui/container/generic_54.png");
    private int inventoryRows;

    public GUIEagerChest(InventoryPlayer inventoryPlayer, TileEntityEagerChest te) {
        super(new ContainerEagerChest(inventoryPlayer, te));
        this.te = te;
        this.inventoryPlayer = inventoryPlayer;
        short short1 = 222;
        int i = short1 - 108;
        this.inventoryRows = te.getSizeInventory() / 9;
        this.ySize = i + this.inventoryRows * 18;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(this.texture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        this.drawTexturedModalRect(k, l + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        this.fontRendererObj.drawString(this.te.isCustomInventoryName() ? this.te.getInventoryName() : I18n.format(this.te.getInventoryName()), 8, 6, 4210752);
        this.fontRendererObj
            .drawString(
                this.inventoryPlayer.isCustomInventoryName() ? this.inventoryPlayer.getInventoryName() : I18n.format(this.inventoryPlayer.getInventoryName()),
                8,
                this.ySize - 96 + 2,
                4210752
            );
    }
}
