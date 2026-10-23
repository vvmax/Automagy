package tuhljin.automagy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.items.InventoryWithFilterOptions;
import tuhljin.automagy.items.ItemEnchantedPaper;
import tuhljin.automagy.tiles.TileEntityMawFinical;

public class GUIFinicalMaw extends ModGuiContainer {
    public TileEntityMawFinical te;
    public final ResourceLocation texture = new ResourceLocation("Automagy:textures/gui/filterHolder.png");
    private boolean filterExtraText = false;
    private boolean filterIsBlacklist = false;

    private GUIFinicalMaw(InventoryPlayer inventoryPlayer, TileEntityMawFinical te, ContainerFinicalMaw container) {
        super(container);
        this.te = te;
        container.attachToGui(this);
    }

    public GUIFinicalMaw(InventoryPlayer inventoryPlayer, TileEntityMawFinical te) {
        this(inventoryPlayer, te, new ContainerFinicalMaw(inventoryPlayer, te));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        if (this.filterExtraText) {
            float old = this.scaleFactor;
            this.scaleFactor *= 0.7F;
            GL11.glPushMatrix();
            GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
            String s = "Automagy.gui.finicalMaw.quantityResult.";
            s = s + (this.filterIsBlacklist ? "leave" : "limit");
            int x = this.guiLeft + 88;
            int y = this.guiTop + 51;
            this.drawCenteredStringScaled(this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.finicalMaw.quantityResult.open"), x, y, 16777215);
            this.drawCenteredStringScaled(this.fontRendererObj, StatCollector.translateToLocal(s), x, y + 6, 16777215);
            GL11.glPopMatrix();
            this.scaleFactor = old;
        }

        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
    }

    @Override
    public void onSlotChanged(SlotRestrictedWithReporting slot) {
        ItemStack stack = slot.getStack();
        if (stack != null) {
            this.filterIsBlacklist = ItemEnchantedPaper.stackIsBlacklist(stack);
            InventoryWithFilterOptions inv = ItemEnchantedPaper.getFilterInventory(stack);
            if (inv != null) {
                this.filterExtraText = inv.useItemCount;
                return;
            }
        }

        this.filterExtraText = false;
    }

    @Override
    protected void buttonClicked(GuiButton button, boolean rightClick) {
    }
}
