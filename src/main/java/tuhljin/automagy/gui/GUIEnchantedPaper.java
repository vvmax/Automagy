package tuhljin.automagy.gui;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.network.MessageGUIFilterPaper;
import tuhljin.automagy.network.PacketHandler;

public class GUIEnchantedPaper extends ModGuiContainer {
    public final boolean blacklist;
    public final ResourceLocation texture;
    private ContainerEnchantedPaper container;
    private GuiTextFieldWithReporting textFieldNameFilter;
    private String prevText = "";

    private GUIEnchantedPaper(ContainerEnchantedPaper container, boolean isBlacklist, EntityPlayer player) {
        super(container);
        this.container = container;
        this.blacklist = isBlacklist;
        this.texture = new ResourceLocation(this.blacklist ? "Automagy:textures/gui/runicBlacklist.png" : "Automagy:textures/gui/runicWhitelist.png");
        this.ySize = 180;
    }

    public GUIEnchantedPaper(boolean isBlacklist, EntityPlayer player) {
        this(new ContainerEnchantedPaper(isBlacklist, player), isBlacklist, player);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList
            .add(new GuiButtonCheckbox(0, this.guiLeft + 80, this.guiTop + 18, !this.container.filterInventory.useItemCount, this.texture, this.xSize, 0));
        this.buttonList
            .add(new GuiButtonCheckbox(1, this.guiLeft + 80, this.guiTop + 28, this.container.filterInventory.ignoreMetadata, this.texture, this.xSize, 0));
        this.buttonList
            .add(new GuiButtonCheckbox(2, this.guiLeft + 80, this.guiTop + 38, this.container.filterInventory.ignoreNBT, this.texture, this.xSize, 0));
        this.prevText = this.container.filterInventory.nameFilter;
        this.textFieldNameFilter = new GuiTextFieldWithReporting(
            3,
            this,
            this.fontRendererObj,
            (int)((this.guiLeft + 81) / this.scaleFactorTextField),
            (int)((this.guiTop + 59) / this.scaleFactorTextField),
            100,
            16
        );
        this.textFieldNameFilter.setText(this.prevText);
        this.textFieldList.add(this.textFieldNameFilter);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.drawTexturedModalRect(
            this.guiLeft + 7 + this.container.blockedSlotID * 18, this.guiTop + 155, 0, this.ySize, this.container.blockedSlotID == 8 ? 18 : 17, 18
        );
        GL11.glPushMatrix();
        GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        int color = this.blacklist ? 16777215 : 0;
        this.drawStringScaledNoShadow(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.filter.ignoreQuantity"), this.guiLeft + 91, this.guiTop + 20, color
        );
        this.drawStringScaledNoShadow(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.filter.ignoreMetadata"), this.guiLeft + 91, this.guiTop + 29.5F, color
        );
        this.drawStringScaledNoShadow(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.filter.ignoreNBT"), this.guiLeft + 91, this.guiTop + 39, color
        );
        this.drawStringScaledNoShadow(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.filter.byName"), this.guiLeft + 81, this.guiTop + 50.5F, color
        );
        GL11.glPopMatrix();
        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
    }

    @Override
    public void drawScreen(int x, int y, float p_73863_3_) {
        super.drawScreen(x, y, p_73863_3_);
        GL11.glPushMatrix();
        GL11.glTranslatef(this.guiLeft, this.guiTop, 0.0F);
        if (this.textFieldNameFilter.pointIsOver(x, y)) {
            ArrayList<String> lines = TjUtil.getMultiLineLocalizedString("Automagy.gui.filter.textFilterTooltip");
            GL11.glPushMatrix();
            GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
            this.drawScaledHoveringText(lines, x - this.guiLeft - 20, y - this.guiTop + 20, this.fontRendererObj);
            GL11.glPopMatrix();
        }

        GL11.glPopMatrix();
    }

    @Override
    protected void buttonClicked(GuiButton button, boolean rightClick) {
        if (button instanceof GuiButtonCheckbox) {
            GuiButtonCheckbox checkbox = (GuiButtonCheckbox)button;
            checkbox.checked = !checkbox.checked;
            PacketHandler.INSTANCE.sendToServer(new MessageGUIFilterPaper(button.id, checkbox.checked ? "1" : "0"));
        }
    }

    @Override
    public void onTextFieldLostFocus(GuiTextFieldWithReporting textField) {
        String text = textField.getText();
        if (!text.equals(this.prevText)) {
            this.prevText = text;
            PacketHandler.INSTANCE.sendToServer(new MessageGUIFilterPaper(textField.id, text));
        }
    }
}
