package tuhljin.automagy.gui;

import cpw.mods.fml.common.FMLLog;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.network.MessageGUIHourglassMagic;
import tuhljin.automagy.network.PacketHandler;
import tuhljin.automagy.tiles.TileEntityHourglassMagic;

public class GUIHourglassMagic extends ModGuiContainer {
    public TileEntityHourglassMagic te;
    public final ResourceLocation texture = new ResourceLocation("Automagy:textures/gui/guiCheckboxable.png");
    public static final String[] strSignal = new String[]{
        StatCollector.translateToLocal("Automagy.gui.hourglassMagic.signal.0"),
        StatCollector.translateToLocal("Automagy.gui.hourglassMagic.signal.1"),
        StatCollector.translateToLocal("Automagy.gui.hourglassMagic.signal.2"),
        StatCollector.translateToLocal("Automagy.gui.hourglassMagic.signal.3"),
        StatCollector.translateToLocal("Automagy.gui.hourglassMagic.signal.4")
    };
    public static final int[] btnOrderSignal = new int[]{0, 1, 4, 3, 2};
    public static final String[] strSandFlip = new String[]{
        StatCollector.translateToLocal("Automagy.gui.hourglassMagic.sandFlip.0"),
        StatCollector.translateToLocal("Automagy.gui.hourglassMagic.sandFlip.1"),
        StatCollector.translateToLocal("Automagy.gui.hourglassMagic.sandFlip.2")
    };
    public static final String[] strSignalReaction = new String[]{
        StatCollector.translateToLocal("Automagy.gui.hourglassMagic.signalReact.0"),
        StatCollector.translateToLocal("Automagy.gui.hourglassMagic.signalReact.1"),
        StatCollector.translateToLocal("Automagy.gui.hourglassMagic.signalReact.2"),
        StatCollector.translateToLocal("Automagy.gui.hourglassMagic.signalReact.3")
    };

    private GUIHourglassMagic(InventoryPlayer inventoryPlayer, TileEntityHourglassMagic te, ContainerHourglassMagic container) {
        super(container);
        this.te = te;
        container.attachedGUI = this;
    }

    public GUIHourglassMagic(InventoryPlayer inventoryPlayer, TileEntityHourglassMagic te) {
        this(inventoryPlayer, te, new ContainerHourglassMagic(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();
        int height = (int)(20.0F * this.scaleFactor);
        this.buttonList
            .add(
                new GuiButtonScaledText(
                    0,
                    this.guiLeft + 38,
                    this.guiTop + 12,
                    40,
                    12,
                    StatCollector.translateToLocal("Automagy.gui.hourglassMagic.timer.min"),
                    this.scaleFactor * 0.6666667F
                )
            );
        this.buttonList
            .add(
                new GuiButtonScaledText(
                    1,
                    this.guiLeft + 80,
                    this.guiTop + 12,
                    40,
                    12,
                    StatCollector.translateToLocal("Automagy.gui.hourglassMagic.timer.sec"),
                    this.scaleFactor * 0.6666667F
                )
            );
        this.buttonList.add(new GuiButtonScaledText(2, this.guiLeft + 75, this.guiTop + 31, 95, height, strSignal[1], this.scaleFactor));
        this.buttonList.add(new GuiButtonScaledText(3, this.guiLeft + 75, this.guiTop + 35 + height, 95, height, strSandFlip[0], this.scaleFactor));
        this.buttonList.add(new GuiButtonCheckbox(4, this.guiLeft + 145, this.guiTop + 10, false, this.texture, this.xSize, 0));
        this.buttonList.add(new GuiButtonScaledText(5, this.guiLeft + 75, this.guiTop + 39 + height * 2, 95, height, strSignalReaction[0], this.scaleFactor));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop + 20, 0, 0, this.xSize, this.ySize);
        GL11.glPushMatrix();
        GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.hourglassMagic.timer.name"), this.guiLeft + 2, this.guiTop + 1, 16777215
        );
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.hourglassMagic.signal.name"), this.guiLeft + 2, this.guiTop + 35, 16777215
        );
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.hourglassMagic.sandFlip.name"), this.guiLeft + 2, this.guiTop + 55, 16777215
        );
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.hourglassMagic.signalReact.name"), this.guiLeft + 2, this.guiTop + 75, 16777215
        );
        this.drawCenteredStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.hourglassMagic.repeat"), this.guiLeft + 149, this.guiTop + 1, 16777215
        );
        GL11.glPopMatrix();
        int time = this.te.getTargetTimeSeconds();
        if (time >= 60) {
            int min = time / 60;
            int sec = time % 60;
            this.drawString(
                this.fontRendererObj,
                StatCollector.translateToLocalFormatted("Automagy.gui.hourglassMagic.timer.minutesAndSeconds", min, sec),
                this.guiLeft + 40,
                this.guiTop,
                16777215
            );
        } else if (time == 1) {
            this.drawString(
                this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.hourglassMagic.timer.secondsOne"), this.guiLeft + 40, this.guiTop, 16777215
            );
        } else {
            this.drawString(
                this.fontRendererObj,
                StatCollector.translateToLocalFormatted("Automagy.gui.hourglassMagic.timer.seconds", time),
                this.guiLeft + 40,
                this.guiTop,
                16777215
            );
        }
    }

    @Override
    public void drawScreen(int x, int y, float p_73863_3_) {
        super.drawScreen(x, y, p_73863_3_);
        if (((GuiButtonScaledText)this.buttonList.get(0)).isMouseHoveringOver() || ((GuiButtonScaledText)this.buttonList.get(1)).isMouseHoveringOver()) {
            ArrayList<String> lines = TjUtil.getMultiLineLocalizedString("Automagy.gui.hourglassMagic.timer.tooltip");
            GL11.glPushMatrix();
            GL11.glTranslatef(this.guiLeft, this.guiTop, 0.0F);
            GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
            this.drawScaledHoveringText(lines, x - this.guiLeft - 10, y - this.guiTop + 20, this.fontRendererObj);
            GL11.glPopMatrix();
        }
    }

    @Override
    protected void buttonClicked(GuiButton button, boolean rightClick) {
        int[] valuesOrder = null;
        int timeAdjust = 1;
        String[] strArr;
        switch (button.id) {
            case 0:
                timeAdjust = 60;
            case 1:
                int time = this.te.getTargetTimeSeconds();
                int shiftKeyAdjust = 1;
                if (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)) {
                    shiftKeyAdjust = 10;
                }

                if (rightClick) {
                    timeAdjust *= -1;
                }

                int newTime = time + timeAdjust * shiftKeyAdjust;
                if (newTime < 1) {
                    int diff = 1 - newTime;
                    newTime = 3600 - diff + 1;
                } else if (newTime > 3600) {
                    int diff = newTime - 3600;
                    newTime = 1 + diff - 1;
                }

                this.te.setTargetTimeSeconds(newTime);
                PacketHandler.INSTANCE.sendToServer(new MessageGUIHourglassMagic(0, newTime));
                return;
            case 2:
                strArr = strSignal;
                valuesOrder = btnOrderSignal;
                break;
            case 3:
                strArr = strSandFlip;
                break;
            case 4:
                if (button instanceof GuiButtonCheckbox) {
                    GuiButtonCheckbox checkbox = (GuiButtonCheckbox)button;
                    checkbox.checked = !checkbox.checked;
                    PacketHandler.INSTANCE.sendToServer(new MessageGUIHourglassMagic(button.id, checkbox.checked ? 1 : 0));
                }

                return;
            case 5:
                strArr = strSignalReaction;
                break;
            default:
                return;
        }

        int i = ArrayUtils.indexOf(strArr, button.displayString);
        if (valuesOrder == null) {
            if (rightClick) {
                if (--i < 0) {
                    i = strArr.length - 1;
                }
            } else if (++i >= strArr.length) {
                i = 0;
            }
        } else {
            int i2 = ArrayUtils.indexOf(valuesOrder, i);
            if (rightClick) {
                if (--i2 < 0) {
                    i2 = valuesOrder.length - 1;
                }
            } else if (++i2 >= valuesOrder.length) {
                i2 = 0;
            }

            i = valuesOrder[i2];
        }

        button.displayString = strArr[i];
        PacketHandler.INSTANCE.sendToServer(new MessageGUIHourglassMagic(button.id, i));
    }

    public void updateButton(int id, int stringIndex) {
        GuiButton button;
        try {
            button = (GuiButton)this.buttonList.get(id);
        } catch (Exception var5) {
            FMLLog.warning("[Automagy] GUIHourglassMagic updateButton request was invalid. (id=" + id + ")");
            return;
        }

        if (button instanceof GuiButtonCheckbox) {
            ((GuiButtonCheckbox)button).checked = stringIndex == 1;
        } else {
            String[] strArr;
            switch (button.id) {
                case 2:
                    strArr = strSignal;
                    break;
                case 3:
                    strArr = strSandFlip;
                    break;
                case 4:
                default:
                    FMLLog.warning("[Automagy] GUIHourglassMagic updateButton request was invalid. Ignoring. (id=" + id + ")");
                    return;
                case 5:
                    strArr = strSignalReaction;
            }

            if (stringIndex >= 0 && stringIndex < strArr.length) {
                button.displayString = strArr[stringIndex];
            } else {
                FMLLog.warning("[Automagy] GUIHourglassMagic updateButton request was invalid. Ignoring. (stringIndex=" + stringIndex + ")");
            }
        }
    }
}
