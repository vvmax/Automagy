package tuhljin.automagy.gui;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.items.ItemEntityEye;
import tuhljin.automagy.network.MessageGUIEntitySignal;
import tuhljin.automagy.network.PacketHandler;
import tuhljin.automagy.tiles.TileEntityEntitySignal;

public class GUIEntitySignal extends ModGuiContainer {
    public TileEntityEntitySignal te;
    public final ResourceLocation texture = new ResourceLocation("Automagy:textures/gui/entitySignal.png");
    private GuiButtonCheckbox checkboxAdults;
    private GuiButtonCheckbox checkboxChildren;
    private GuiButtonCheckbox checkboxSpecificPlayer;
    private String mobName = "";
    private boolean showAgeOptions = false;
    private boolean isPlayer = false;

    private GUIEntitySignal(InventoryPlayer inventoryPlayer, TileEntityEntitySignal te, ContainerEntitySignal container) {
        super(container);
        this.te = te;
        container.attachToGui(this);
    }

    public GUIEntitySignal(InventoryPlayer inventoryPlayer, TileEntityEntitySignal te) {
        this(inventoryPlayer, te, new ContainerEntitySignal(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();
        this.checkboxAdults = new GuiButtonCheckbox(0, this.guiLeft + 40, this.guiTop + 55, this.te.filterAdults, this.texture, this.xSize, 0);
        this.checkboxChildren = new GuiButtonCheckbox(1, this.guiLeft + 40, this.guiTop + 65, this.te.filterChildren, this.texture, this.xSize, 0);
        this.checkboxAdults.visible = false;
        this.checkboxChildren.visible = false;
        this.buttonList.add(this.checkboxAdults);
        this.buttonList.add(this.checkboxChildren);
        this.checkboxSpecificPlayer = new GuiButtonCheckbox(2, this.guiLeft + 40, this.guiTop + 60, this.te.specificPlayer, this.texture, this.xSize, 0);
        this.checkboxSpecificPlayer.visible = false;
        this.buttonList.add(this.checkboxSpecificPlayer);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        GL11.glPushMatrix();
        GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        if (!this.mobName.isEmpty()) {
            String s = this.isPlayer ? StatCollector.translateToLocal("Automagy.gui.entitySignal.players") : this.mobName;
            this.drawStringScaled(this.fontRendererObj, EnumChatFormatting.DARK_AQUA + s, this.guiLeft + 42, this.guiTop + 36, 16777215);
        }

        if (this.showAgeOptions) {
            this.drawStringScaled(
                this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.entitySignal.includeAdults"), this.guiLeft + 53, this.guiTop + 57, 16777215
            );
            this.drawStringScaled(
                this.fontRendererObj,
                StatCollector.translateToLocal("Automagy.gui.entitySignal.includeChildren"),
                this.guiLeft + 53,
                this.guiTop + 67,
                16777215
            );
        }

        if (this.isPlayer) {
            this.drawStringScaled(
                this.fontRendererObj,
                StatCollector.translateToLocal("Automagy.gui.entitySignal.specificPlayer.1"),
                this.guiLeft + 53,
                this.guiTop + 57,
                16777215
            );
            this.drawStringScaled(
                this.fontRendererObj,
                StatCollector.translateToLocalFormatted(
                    "Automagy.gui.entitySignal.specificPlayer.2", EnumChatFormatting.AQUA + this.mobName + EnumChatFormatting.GRAY
                ),
                this.guiLeft + 53,
                this.guiTop + 67,
                16777215
            );
        }

        GL11.glPopMatrix();
        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
    }

    @Override
    public void onSlotChanged(SlotRestrictedWithReporting slot) {
        ItemStack stack = slot.getStack();
        if (stack != null) {
            this.mobName = this.te.getMobName();
            if (!this.mobName.isEmpty()) {
                this.mobName = ItemEntityEye.getLocalizedEntityName(this.mobName);
                this.showAgeOptions = stack.stackTagCompound.getBoolean("mobAges");
                this.isPlayer = stack.stackTagCompound.getBoolean("entityIsPlayer");
            } else {
                this.showAgeOptions = false;
                this.isPlayer = false;
            }
        } else {
            this.mobName = "";
            this.showAgeOptions = false;
            this.isPlayer = false;
        }

        if (this.showAgeOptions) {
            this.checkboxAdults.visible = true;
            this.checkboxChildren.visible = true;
        } else {
            this.checkboxAdults.visible = false;
            this.checkboxChildren.visible = false;
        }

        this.checkboxSpecificPlayer.visible = this.isPlayer;
    }

    @Override
    protected void buttonClicked(GuiButton button, boolean rightClick) {
        if (button instanceof GuiButtonCheckbox) {
            GuiButtonCheckbox checkbox = (GuiButtonCheckbox)button;
            checkbox.checked = !checkbox.checked;
            PacketHandler.INSTANCE.sendToServer(new MessageGUIEntitySignal(button.id, checkbox.checked ? 1 : 0));
        }
    }

    public void updateButton(int id, int value) {
        GuiButton button;
        try {
            button = (GuiButton)this.buttonList.get(id);
        } catch (Exception var5) {
            FMLLog.warning("[Automagy] GUIEntitySignal updateButton request was invalid. (id=" + id + ")");
            return;
        }

        if (button instanceof GuiButtonCheckbox) {
            ((GuiButtonCheckbox)button).checked = value == 1;
        }
    }
}
