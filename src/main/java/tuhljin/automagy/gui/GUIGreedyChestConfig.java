package tuhljin.automagy.gui;

import cpw.mods.fml.common.FMLLog;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.items.InventoryWithFilterOptions;
import tuhljin.automagy.items.ItemEnchantedPaper;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.network.MessageGUIGreedyChestConfig;
import tuhljin.automagy.network.PacketHandler;
import tuhljin.automagy.tiles.TileEntityGreedyChest;

public class GUIGreedyChestConfig extends ModGuiContainer {
    public TileEntityGreedyChest te;
    public final ResourceLocation texture = new ResourceLocation("Automagy:textures/gui/greedyChest.png");
    public GuiTextFieldWithReporting percentTextField;
    public GuiTextFieldWithReporting amountTextField;
    public static final String[] strMode = new String[]{
        StatCollector.translateToLocal("Automagy.gui.greedyChest.mode.0"),
        StatCollector.translateToLocal("Automagy.gui.greedyChest.mode.1"),
        StatCollector.translateToLocal("Automagy.gui.greedyChest.mode.2"),
        StatCollector.translateToLocal("Automagy.gui.greedyChest.mode.3")
    };
    private String strPercent = "50" + TjUtil.decimalSeparator + "00";
    private String strAmount = "64";
    private boolean showingPercentField = true;
    private GuiButtonScaledText buttonMode;
    private boolean filterExtraText = false;
    private boolean filterIsBlacklist = false;

    private GUIGreedyChestConfig(InventoryPlayer inventoryPlayer, TileEntityGreedyChest te, ContainerGreedyChestConfig container) {
        super(container);
        this.te = te;
        container.attachToGui(this);
    }

    public GUIGreedyChestConfig(InventoryPlayer inventoryPlayer, TileEntityGreedyChest te) {
        this(inventoryPlayer, te, new ContainerGreedyChestConfig(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();
        int height = (int)(20.0F * this.scaleFactor);
        this.buttonList.add(new GuiButtonCheckbox(0, this.guiLeft + 50, this.guiTop + 65, this.te.directions[1], this.texture, this.xSize, 0));
        this.buttonList.add(new GuiButtonCheckbox(1, this.guiLeft + 5, this.guiTop + 65, this.te.directions[0], this.texture, this.xSize, 0));
        this.buttonList.add(new GuiButtonCheckbox(2, this.guiLeft + 5, this.guiTop + 44, this.te.directions[2], this.texture, this.xSize, 0));
        this.buttonList.add(new GuiButtonCheckbox(3, this.guiLeft + 50, this.guiTop + 45, this.te.directions[3], this.texture, this.xSize, 0));
        this.buttonList.add(new GuiButtonCheckbox(4, this.guiLeft + 5, this.guiTop + 55, this.te.directions[4], this.texture, this.xSize, 0));
        this.buttonList.add(new GuiButtonCheckbox(5, this.guiLeft + 50, this.guiTop + 55, this.te.directions[5], this.texture, this.xSize, 0));
        this.buttonMode = new GuiButtonScaledText(6, this.guiLeft, this.guiTop + 10, 95, height, strMode[0], this.scaleFactor);
        this.buttonList.add(this.buttonMode);
        this.percentTextField = new GuiTextFieldWithReporting(
            7,
            this,
            this.fontRendererObj,
            (int)((this.guiLeft + 102) / this.scaleFactorTextField),
            (int)((this.guiTop + 11) / this.scaleFactorTextField),
            38,
            16
        );
        this.percentTextField.setText(this.strPercent);
        this.percentTextField.setMaxStringLength(5);
        this.textFieldList.add(this.percentTextField);
        this.amountTextField = new GuiTextFieldWithReporting(
            8,
            this,
            this.fontRendererObj,
            (int)((this.guiLeft + 102) / this.scaleFactorTextField),
            (int)((this.guiTop + 11) / this.scaleFactorTextField),
            38,
            16
        );
        this.amountTextField.setText(this.strAmount);
        this.amountTextField.setMaxStringLength(5);
        this.textFieldList.add(this.amountTextField);
        this.updateTextFieldVisibility();
    }

    private void updateTextFieldVisibility() {
        String s = this.buttonMode.displayString;
        if (!s.equals(strMode[0]) && !s.equals(strMode[1])) {
            this.showingPercentField = false;
            this.percentTextField.setVisible(false);
            this.percentTextField.setEnabled(false);
            this.amountTextField.setVisible(true);
            this.amountTextField.setEnabled(true);
        } else {
            this.showingPercentField = true;
            this.percentTextField.setVisible(true);
            this.percentTextField.setEnabled(true);
            this.amountTextField.setVisible(false);
            this.amountTextField.setEnabled(false);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        GL11.glPushMatrix();
        GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.greedyChest.mode.name"), this.guiLeft + 2, this.guiTop + 1, 16777215
        );
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.greedyChest.dir.name"), this.guiLeft + 2, this.guiTop + 32, 16777215
        );
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.greedyChest.dir.N"), this.guiLeft + 17, this.guiTop + 46, 16777215
        );
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.greedyChest.dir.W"), this.guiLeft + 17, this.guiTop + 56, 16777215
        );
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.greedyChest.dir.U"), this.guiLeft + 17, this.guiTop + 66, 16777215
        );
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.greedyChest.dir.S"), this.guiLeft + 62, this.guiTop + 46, 16777215
        );
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.greedyChest.dir.E"), this.guiLeft + 62, this.guiTop + 56, 16777215
        );
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.greedyChest.dir.D"), this.guiLeft + 62, this.guiTop + 66, 16777215
        );
        if (this.showingPercentField) {
            this.drawStringScaled(this.fontRendererObj, "%", this.guiLeft + 133, this.guiTop + 14, 16777215);
        }

        GL11.glPopMatrix();
        if (this.filterExtraText) {
            float old = this.scaleFactor;
            this.scaleFactor *= 0.7F;
            GL11.glPushMatrix();
            GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
            this.drawStringScaled(
                this.fontRendererObj,
                StatCollector.translateToLocal("Automagy.gui.greedyChest.quantityResult.open"),
                this.guiLeft + 120,
                this.guiTop + 54,
                16777215
            );
            String s = "Automagy.gui.greedyChest.quantityResult.";
            switch (this.te.modePilfer) {
                case 0:
                case 1:
                    s = s + (this.filterIsBlacklist ? "leave" : "limit");
                    break;
                case 2:
                    s = s + (this.filterIsBlacklist ? "override" : "limit");
                    break;
                case 3:
                    s = s + (this.filterIsBlacklist ? "leave" : "override");
            }

            this.drawStringScaled(this.fontRendererObj, StatCollector.translateToLocal(s), this.guiLeft + 120, this.guiTop + 60, 16777215);
            GL11.glPopMatrix();
            this.scaleFactor = old;
        }

        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
    }

    @Override
    public void drawScreen(int x, int y, float p_73863_3_) {
        super.drawScreen(x, y, p_73863_3_);

        for (int i = 0; i < 6; i++) {
            if (((GuiButtonCheckbox)this.buttonList.get(i)).mousePressed(this.mc, x, y)) {
                ForgeDirection dir = ForgeDirection.getOrientation(i);
                World world = this.te.getWorld();
                String name = TjUtil.getBlockNameAt(world, this.te.xCoord + dir.offsetX, this.te.yCoord + dir.offsetY, this.te.zCoord + dir.offsetZ);
                if (name != null) {
                    boolean isAir = world.isAirBlock(this.te.xCoord + dir.offsetX, this.te.yCoord + dir.offsetY, this.te.zCoord + dir.offsetZ);
                    name = " " + name + " ";
                    ArrayList<String> lines = new ArrayList<>();
                    lines.add((isAir ? EnumChatFormatting.GRAY : EnumChatFormatting.AQUA) + name);
                    GL11.glPushMatrix();
                    GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
                    this.drawScaledHoveringText(
                        lines, x - (int)(this.fontRendererObj.getStringWidth(name) * this.scaleFactor) - 15, y + 7, this.fontRendererObj
                    );
                    GL11.glPopMatrix();
                }
                break;
            }
        }
    }

    @Override
    protected void buttonClicked(GuiButton button, boolean rightClick) {
        if (button.id == 6) {
            int i = Arrays.asList(strMode).indexOf(button.displayString);
            if (rightClick) {
                if (--i < 0) {
                    i = strMode.length - 1;
                }
            } else if (++i >= strMode.length) {
                i = 0;
            }

            button.displayString = strMode[i];
            this.updateTextFieldVisibility();
            PacketHandler.INSTANCE.sendToServer(new MessageGUIGreedyChestConfig(button.id, i));
        } else {
            if (button instanceof GuiButtonCheckbox) {
                GuiButtonCheckbox checkbox = (GuiButtonCheckbox)button;
                checkbox.checked = !checkbox.checked;
                PacketHandler.INSTANCE.sendToServer(new MessageGUIGreedyChestConfig(button.id, checkbox.checked ? 1.0F : 0.0F));
            }
        }
    }

    public void updatePercentage(String s) {
        this.percentTextField.setText(s);
        this.makeTextFieldValid(this.percentTextField);
    }

    public void updateButton(int id, int stringIndex) {
        if (id == 8) {
            this.amountTextField.setText(String.valueOf(stringIndex));
            this.makeTextFieldValid(this.amountTextField);
        } else {
            GuiButton button;
            try {
                button = (GuiButton)this.buttonList.get(id);
            } catch (Exception var5) {
                FMLLog.warning("[Automagy] GUIGreedyChestConfig updateButton request was invalid. (id=" + id + ")");
                return;
            }

            if (button instanceof GuiButtonCheckbox) {
                ((GuiButtonCheckbox)button).checked = stringIndex == 1;
            } else if (id != 6) {
                FMLLog.warning("[Automagy] Unexpected GUIGreedyChestConfig updateButton request. Ignoring. (id=" + id + ")");
            } else if (stringIndex >= 0 && stringIndex < strMode.length) {
                button.displayString = strMode[stringIndex];
                this.updateTextFieldVisibility();
            } else {
                FMLLog.warning("[Automagy] GUIGreedyChestConfig updateButton request was invalid. Ignoring. (stringIndex=" + stringIndex + ")");
            }
        }
    }

    @Override
    protected boolean textFieldAllowsCharacter(GuiTextField textField, char c, String currentText) {
        return c != TjUtil.decimalSeparator
            ? Character.isDigit(c)
            : textField == this.percentTextField && !currentText.contains(String.valueOf(TjUtil.decimalSeparator));
    }

    @Override
    public void onTextFieldLostFocus(GuiTextFieldWithReporting textField) {
        if (this.makeTextFieldValid(textField)) {
            if (textField.id == 7) {
                Float f = null;

                try {
                    f = Float.valueOf(textField.getText()) / 100.0F;
                } catch (Exception var5) {
                    FMLLog.severe("[Automagy] GUIGreedyChestConfig failed to prepare packet. Invalid value for fraction.");
                    return;
                }

                if (f < 0.001F) {
                    f = 0.0F;
                }

                if (!TjUtil.areFloatsAlmostEqual_lowprec(this.te.fractionWanted, f)) {
                    PacketHandler.INSTANCE.sendToServer(new MessageGUIGreedyChestConfig(textField.id, f));
                }
            } else if (textField.id == 8) {
                Integer i = null;

                try {
                    i = Integer.valueOf(textField.getText());
                } catch (Exception var4) {
                    FMLLog.severe("[Automagy] GUIGreedyChestConfig failed to prepare packet. Invalid value for amount.");
                    return;
                }

                if (this.te.amountWanted != i) {
                    PacketHandler.INSTANCE.sendToServer(new MessageGUIGreedyChestConfig(textField.id, i.intValue()));
                }
            }
        }
    }

    private boolean makeTextFieldValid(GuiTextFieldWithReporting textField) {
        boolean percentMode = textField.id == 7;
        boolean usedPrev = false;
        String text = textField.getText();
        Float f = null;

        try {
            f = Float.valueOf(text);
        } catch (Exception var8) {
        }

        if (f == null) {
            try {
                f = Float.valueOf(percentMode ? this.strPercent : this.strAmount);
                usedPrev = true;
            } catch (Exception var7) {
            }

            if (f == null) {
                f = 0.0F;
            }
        }

        if (f < 0.1F) {
            f = 0.0F;
        } else if (percentMode && f > 100.0F) {
            f = 100.0F;
        }

        if (percentMode) {
            if (f < 0.001F) {
                f = 0.0F;
            }

            this.strPercent = String.valueOf(f);
            if (!text.equals(this.strPercent)) {
                textField.setText(this.strPercent);
            }
        } else {
            this.strAmount = String.valueOf(MathHelper.floor_float(f));
            if (!text.equals(this.strAmount)) {
                textField.setText(this.strAmount);
            }
        }

        return !usedPrev;
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
}
