package tuhljin.automagy.gui;

import cpw.mods.fml.common.FMLLog;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.network.MessageGUIVisReader;
import tuhljin.automagy.network.PacketHandler;
import tuhljin.automagy.tiles.TileEntityVisReader;

public class GUIVisReader extends ModGuiContainer {
    public TileEntityVisReader te;
    public final ResourceLocation texture = new ResourceLocation("Automagy:textures/gui/visReader.png");
    protected GuiButtonScaledText[] btnCapacity = new GuiButtonScaledText[4];
    protected GuiButtonScaledText[] btnOutput = new GuiButtonScaledText[4];
    public static final String[] strMode = new String[]{
        StatCollector.translateToLocal("Automagy.gui.visReader.mode.0"),
        StatCollector.translateToLocal("Automagy.gui.visReader.mode.1"),
        StatCollector.translateToLocal("Automagy.gui.visReader.mode.2"),
        StatCollector.translateToLocal("Automagy.gui.visReader.mode.3"),
        StatCollector.translateToLocal("Automagy.gui.visReader.mode.4")
    };
    public static final String[] strCapacity = new String[]{
        StatCollector.translateToLocal("Automagy.gui.visReader.capacity.auto"), "1", "8", "16", "25", "32", "50", "64", "100", "125"
    };
    public static final String[] strDir = new String[]{
        StatCollector.translateToLocal("Automagy.gui.visReader.dir.all"),
        StatCollector.translateToLocal("Automagy.gui.visReader.dir.N"),
        StatCollector.translateToLocal("Automagy.gui.visReader.dir.E"),
        StatCollector.translateToLocal("Automagy.gui.visReader.dir.S"),
        StatCollector.translateToLocal("Automagy.gui.visReader.dir.W"),
        StatCollector.translateToLocal("Automagy.gui.visReader.dir.D")
    };
    public static final int[] strDirLookup = new int[]{5, -1, 1, 3, 4, 2, 0};
    public static final int[] strDirLookupReverse = new int[]{6, 2, 5, 3, 4, 0};

    private GUIVisReader(InventoryPlayer inventoryPlayer, TileEntityVisReader te, ContainerVisReader container) {
        super(container);
        this.te = te;
        this.ySize = 178;
        container.attachedGUI = this;
    }

    public GUIVisReader(InventoryPlayer inventoryPlayer, TileEntityVisReader te) {
        this(inventoryPlayer, te, new ContainerVisReader(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();
        int height = (int)(20.0F * this.scaleFactor);
        int leftOffset = 29;

        for (int i = 0; i < 4; i++) {
            this.buttonList
                .add(
                    new GuiButtonScaledText(
                        i * 4, this.guiLeft + leftOffset, this.guiTop + 11 + (height + 4) * i, 56, height, strMode[this.te.modes[i]], this.scaleFactor
                    )
                );
            int iC = this.te.capacities[i];
            if (iC != 0) {
                int index = Arrays.asList(strCapacity).indexOf(String.valueOf(iC));
                iC = Math.max(index, 0);
            }

            this.btnCapacity[i] = new GuiButtonScaledText(
                i * 4 + 1, this.guiLeft + leftOffset + 57, this.guiTop + 11 + (height + 4) * i, 36, height, strCapacity[iC], this.scaleFactor
            );
            this.buttonList.add(this.btnCapacity[i]);
            String s = strDirLookup[this.te.outputDirs[i]] == -1 ? "invalid" : strDir[strDirLookup[this.te.outputDirs[i]]];
            this.btnOutput[i] = new GuiButtonScaledText(
                i * 4 + 2, this.guiLeft + leftOffset + 94, this.guiTop + 11 + (height + 4) * i, 27, height, s, this.scaleFactor
            );
            this.buttonList.add(this.btnOutput[i]);
            this.buttonList
                .add(
                    new GuiButtonCheckbox(
                        i * 4 + 3, this.guiLeft + leftOffset + 126, this.guiTop + 14 + (height + 4) * i, this.te.inverseSignals[i], this.texture, this.xSize, 0
                    )
                );
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        int leftOffset = 29;
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        GL11.glPushMatrix();
        GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.visReader.mode.col"), this.guiLeft + leftOffset + 2, this.guiTop, 16777215
        );
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.visReader.capacity.col"), this.guiLeft + leftOffset + 59, this.guiTop, 16777215
        );
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.visReader.dir.col"), this.guiLeft + leftOffset + 96, this.guiTop, 16777215
        );
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.visReader.invert"), this.guiLeft + leftOffset + 124, this.guiTop, 16777215
        );
        GL11.glPopMatrix();
    }

    @Override
    protected void buttonClicked(GuiButton button, boolean rightClick) {
        int type = button.id % 4;
        String[] strArr;
        switch (type) {
            case 0:
                strArr = strMode;
                break;
            case 1:
                strArr = strCapacity;
                break;
            case 2:
                strArr = strDir;
                break;
            case 3:
                if (button instanceof GuiButtonCheckbox) {
                    GuiButtonCheckbox checkbox = (GuiButtonCheckbox)button;
                    checkbox.checked = !checkbox.checked;
                    PacketHandler.INSTANCE.sendToServer(new MessageGUIVisReader(button.id, checkbox.checked ? 1 : 0));
                }

                return;
            default:
                return;
        }

        int i = Arrays.asList(strArr).indexOf(button.displayString);
        if (rightClick) {
            if (--i < 0) {
                i = strArr.length - 1;
            }
        } else if (++i >= strArr.length) {
            i = 0;
        }

        button.displayString = strArr[i];
        if (type == 0) {
            int index = button.id / 4;
            this.btnCapacity[index].enabled = i != 0 && i != 4;
            this.btnOutput[index].enabled = i != 0;
        } else if (type == 1) {
            if (i != 0) {
                int num;
                try {
                    num = Integer.valueOf(button.displayString);
                } catch (Exception var9) {
                    FMLLog.warning("[Automagy] GUIVisReader failed to translate button info into usable packet data. (capacity)");
                    return;
                }

                i = num < 1 ? 1 : (num > 1000 ? 1000 : num);
            }
        } else if (type == 2) {
            try {
                i = strDirLookupReverse[i];
            } catch (Exception var8) {
                FMLLog.warning("[Automagy] GUIVisReader failed to translate button info into usable packet data. (output dir)");
                return;
            }
        }

        PacketHandler.INSTANCE.sendToServer(new MessageGUIVisReader(button.id, i));
    }

    public void updateButton(int id, int stringIndex) {
        GuiButton button;
        try {
            button = (GuiButton)this.buttonList.get(id);
        } catch (Exception var7) {
            FMLLog.warning("[Automagy] GUIVisReader updateButton request was invalid. (id=" + id + ")");
            return;
        }

        if (button instanceof GuiButtonCheckbox) {
            ((GuiButtonCheckbox)button).checked = stringIndex == 1;
        } else {
            int type = button.id % 4;
            String[] strArr;
            switch (type) {
                case 0:
                    strArr = strMode;
                    int index = id / 4;
                    this.btnCapacity[index].enabled = this.te.modes[index] != 0 && this.te.modes[index] != 4;
                    this.btnOutput[index].enabled = this.te.modes[index] != 0;
                    break;
                case 1:
                    if (stringIndex != 0) {
                        int i = Arrays.asList(strCapacity).indexOf(String.valueOf(stringIndex));
                        stringIndex = Math.max(i, 0);
                    }

                    strArr = strCapacity;
                    break;
                case 2:
                    if (stringIndex < 0 || stringIndex >= strDirLookup.length || strDirLookup[stringIndex] == -1) {
                        FMLLog.warning("[Automagy] GUIVisReader updateButton request was invalid. Ignoring. (id=" + id + ", stringIndex=" + stringIndex + ")");
                        return;
                    }

                    stringIndex = strDirLookup[stringIndex];
                    strArr = strDir;
                    break;
                default:
                    FMLLog.warning("[Automagy] GUIVisReader updateButton request was invalid. Ignoring. (id=" + id + ")");
                    return;
            }

            if (stringIndex >= 0 && stringIndex < strArr.length) {
                button.displayString = strArr[stringIndex];
            } else {
                FMLLog.warning("[Automagy] GUIVisReader updateButton request was invalid. Ignoring. (stringIndex=" + stringIndex + ")");
            }
        }
    }
}
