package tuhljin.automagy.gui;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.items.InventoryWithFilterOptions;
import tuhljin.automagy.items.ItemEnchantedPaper;
import tuhljin.automagy.network.MessageGUITallyBlock;
import tuhljin.automagy.network.PacketHandler;
import tuhljin.automagy.tiles.TileEntityTallyBase;

public class GUITallyBlock extends ModGuiContainer {
    public TileEntityTallyBase te;
    public final ResourceLocation texture = new ResourceLocation("Automagy:textures/gui/filterHolder.png");
    private boolean listInstalled = false;
    private boolean filterExtraText = false;
    private boolean filterIsBlacklist = false;
    private GuiButtonCheckbox checkboxMatchAll;

    private GUITallyBlock(InventoryPlayer inventoryPlayer, TileEntityTallyBase te, ContainerTallyBlock container) {
        super(container);
        this.te = te;
        container.attachToGui(this);
    }

    public GUITallyBlock(InventoryPlayer inventoryPlayer, TileEntityTallyBase te) {
        this(inventoryPlayer, te, new ContainerTallyBlock(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();
        this.checkboxMatchAll = new GuiButtonCheckbox(0, this.guiLeft + 104, this.guiTop + 32, !this.te.requireAllMatches, this.texture, this.xSize, 0);
        this.buttonList.add(this.checkboxMatchAll);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        GL11.glPushMatrix();
        GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.tallyBlock.allowPartialMatch"), this.guiLeft + 117, this.guiTop + 34, 16777215
        );
        GL11.glPopMatrix();
        if (this.listInstalled) {
            float old = this.scaleFactor;
            this.scaleFactor *= 0.7F;
            GL11.glPushMatrix();
            GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
            int x = this.guiLeft + 88;
            int y = this.guiTop + 51;
            this.drawCenteredStringScaled(
                this.fontRendererObj,
                StatCollector.translateToLocal("Automagy.gui.tallyBlock.desc." + (this.filterIsBlacklist ? "black" : "white")),
                x,
                y,
                16777215
            );
            this.drawCenteredStringScaled(
                this.fontRendererObj,
                StatCollector.translateToLocal("Automagy.gui.tallyBlock.desc." + (this.checkboxMatchAll.checked ? "any" : "all")),
                x,
                y + 6,
                16777215
            );
            if (this.filterExtraText) {
                this.drawCenteredStringScaled(
                    this.fontRendererObj,
                    StatCollector.translateToLocal("Automagy.gui.tallyBlock.quantityResult." + (this.filterIsBlacklist ? "black" : "white")),
                    x,
                    y + 15,
                    16777215
                );
            }

            GL11.glPopMatrix();
            this.scaleFactor = old;
        }

        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
    }

    @Override
    public void onSlotChanged(SlotRestrictedWithReporting slot) {
        ItemStack stack = slot.getStack();
        if (stack != null) {
            this.listInstalled = true;
            this.filterIsBlacklist = ItemEnchantedPaper.stackIsBlacklist(stack);
            InventoryWithFilterOptions inv = this.te.getFilterFromStack(stack);
            if (inv != null) {
                this.filterExtraText = inv.useItemCount;
                return;
            }
        } else {
            this.listInstalled = false;
        }

        this.filterExtraText = false;
    }

    @Override
    protected void buttonClicked(GuiButton button, boolean rightClick) {
        if (button instanceof GuiButtonCheckbox) {
            GuiButtonCheckbox checkbox = (GuiButtonCheckbox)button;
            checkbox.checked = !checkbox.checked;
            PacketHandler.INSTANCE.sendToServer(new MessageGUITallyBlock(button.id, checkbox.checked));
        }
    }

    public void updateButton(int id, boolean value) {
        GuiButton button;
        try {
            button = (GuiButton)this.buttonList.get(id);
        } catch (Exception var5) {
            FMLLog.warning("[Automagy] GUITallyBlock updateButton request was invalid. (id=" + id + ")");
            return;
        }

        if (button instanceof GuiButtonCheckbox) {
            ((GuiButtonCheckbox)button).checked = value;
        }
    }
}
