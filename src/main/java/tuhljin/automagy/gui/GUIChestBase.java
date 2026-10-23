package tuhljin.automagy.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.tiles.IChestForGui;

public class GUIChestBase extends GuiContainer {
    public static final ResourceLocation texture = new ResourceLocation("textures/gui/container/generic_54.png");
    private int inventoryRows;
    private IChestForGui chest;
    private InventoryPlayer inventoryPlayer;

    public GUIChestBase(ContainerChestBase container, InventoryPlayer inventoryPlayer, IChestForGui te) {
        super(container);
        this.inventoryPlayer = inventoryPlayer;
        this.chest = container.getChest();
        short short1 = 222;
        int i = short1 - 108;
        this.inventoryRows = te.getNumRows();
        this.ySize = i + this.inventoryRows * 18;
    }

    public GUIChestBase(InventoryPlayer inventoryPlayer, IChestForGui te) {
        this(new ContainerChestBase(inventoryPlayer, te), inventoryPlayer, te);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        this.drawTexturedModalRect(k, l + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        this.fontRendererObj
            .drawString(this.chest.isCustomInventoryName() ? this.chest.getInventoryName() : I18n.format(this.chest.getInventoryName()), 8, 6, 4210752);
        this.fontRendererObj
            .drawString(
                this.inventoryPlayer.isCustomInventoryName() ? this.inventoryPlayer.getInventoryName() : I18n.format(this.inventoryPlayer.getInventoryName()),
                8,
                this.ySize - 96 + 2,
                4210752
            );
    }
}
