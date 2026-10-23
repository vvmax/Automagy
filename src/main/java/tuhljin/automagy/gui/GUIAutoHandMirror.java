package tuhljin.automagy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.items.InventoryWithFilterOptions;
import tuhljin.automagy.items.ItemEnchantedPaper;

public class GUIAutoHandMirror extends ModGuiContainer {
    public final ResourceLocation texture = new ResourceLocation("Automagy:textures/gui/autoHandMirror.png");
    private ContainerAutoHandMirror container;
    private boolean[] filterExtraText = new boolean[]{false, false};
    private boolean[] filterIsBlacklist = new boolean[]{false, false};

    private GUIAutoHandMirror(InventoryPlayer inventoryPlayer, ContainerAutoHandMirror container) {
        super(container);
        this.container = container;
        container.attachToGui(this);
    }

    public GUIAutoHandMirror(InventoryPlayer inventoryPlayer, World world) {
        this(inventoryPlayer, new ContainerAutoHandMirror(inventoryPlayer, world));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.drawTexturedModalRect(
            this.guiLeft + 7 + this.container.blockedSlotID * 18, this.guiTop + 127, 0, this.ySize, this.container.blockedSlotID == 8 ? 18 : 17, 32
        );

        for (int i = 0; i < this.filterExtraText.length; i++) {
            if (this.filterExtraText[i]) {
                float old = this.scaleFactor;
                this.scaleFactor *= 0.7F;
                GL11.glPushMatrix();
                GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
                int x = this.guiLeft + (i == 0 ? 29 : 148);
                int y = this.guiTop + 49;
                if (this.filterIsBlacklist[i]) {
                    this.drawCenteredStringScaled(
                        this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.autoHandMirror.blacklist.1"), x, y, 16777215
                    );
                    this.drawCenteredStringScaled(
                        this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.autoHandMirror.blacklist.2"), x, y + 6, 16777215
                    );
                } else {
                    this.drawCenteredStringScaled(
                        this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.autoHandMirror.whitelist.1"), x, y, 16777215
                    );
                    this.drawCenteredStringScaled(
                        this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.autoHandMirror.whitelist.2"), x, y + 6, 16777215
                    );
                }

                GL11.glPopMatrix();
                this.scaleFactor = old;
            }
        }

        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
    }

    @Override
    public void onSlotChanged(SlotRestrictedWithReporting slot) {
        int i = slot.id;
        ItemStack stack = slot.getStack();
        if (stack != null) {
            this.filterIsBlacklist[i] = ItemEnchantedPaper.stackIsBlacklist(stack);
            InventoryWithFilterOptions inv = ItemEnchantedPaper.getFilterInventory(stack);
            if (inv != null) {
                this.filterExtraText[i] = inv.useItemCount;
                return;
            }
        }

        this.filterExtraText[i] = false;
    }

    @Override
    protected void buttonClicked(GuiButton button, boolean rightClick) {
    }
}
