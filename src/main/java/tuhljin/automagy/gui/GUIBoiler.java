package tuhljin.automagy.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.gui.GuiSpa;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.compat.CompatibilityManager;
import tuhljin.automagy.tiles.TileEntityBoiler;

public class GUIBoiler extends ModGuiContainer {
    public TileEntityBoiler boiler;
    public final ResourceLocation texture = new ResourceLocation("Automagy:textures/gui/boiler.png");
    private boolean listInstalled = false;
    private boolean filterExtraText = false;
    private boolean filterIsBlacklist = false;

    public GUIBoiler(InventoryPlayer inventoryPlayer, TileEntityBoiler te) {
        super(new ContainerBoiler(inventoryPlayer, te));
        this.boiler = te;
        this.ySize = 175;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        if (this.boiler.isBurning()) {
            int i1 = this.boiler.getBurnTimeRemainingScaled(20);
            this.drawTexturedModalRect(k + 80, l + 26 + 20 - i1, 176, 20 - i1, 16, i1);
        }

        int i1 = this.boiler.getCookProgressScaled(46);
        this.drawTexturedModalRect(k + 100, l + 13 + 56 - i1, 216, 46 - i1, 9, i1);
        i1 = this.boiler.getContentsScaled(48);
        this.drawTexturedModalRect(k + 56, l + 12 + 54 - i1, 200, 48 - i1, 8, i1);
        this.drawTexturedModalRect(k + 55, l + 14, 232, 0, 10, 55);
        if (this.boiler.tank.getFluidAmount() > 0) {
            FluidStack fluid = this.boiler.tank.getFluid();
            if (fluid != null) {
                IIcon icon = fluid.getFluid().getIcon();
                if (icon != null) {
                    float bar = (float)this.boiler.tank.getFluidAmount() / this.boiler.tank.getCapacity();
                    GL11.glPushMatrix();
                    GL11.glTranslatef(this.guiLeft + 117, this.guiTop + 18, 0.0F);
                    GuiSpa.renderFluid(icon);
                    GL11.glPopMatrix();
                    Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
                    this.drawTexturedModalRect(k + 117, l + 18, 117, 18, 10, (int)(48.0F - 48.0F * bar));
                }
            }
        }

        this.drawTexturedModalRect(k + 116, l + 14, 232, 0, 10, 55);
        if (this.boiler.hasContentsCannotBoil()) {
            this.drawTexturedModalRect(k + 82, l + 14, 178, 82, 12, 12);
        }

        GL11.glDisable(3042);
        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
    }

    @Override
    protected void buttonClicked(GuiButton button, boolean rightClick) {
    }

    @Override
    public void drawScreen(int x, int y, float p_73863_3_) {
        super.drawScreen(x, y, p_73863_3_);
        int posX = x - this.guiLeft;
        int posY = y - this.guiTop;
        if (posX >= 116 && posX < 126 && posY >= 14 && posY < 70) {
            FluidStack fluid = this.boiler.tank.getFluid();
            if (fluid != null) {
                Fluid theFluid = fluid.getFluid();
                List tipsList = new ArrayList();
                tipsList.add(theFluid.getLocalizedName(fluid));
                tipsList.add(StatCollector.translateToLocalFormatted("Automagy.gui.boiler.liquidQty", fluid.amount));
                this.drawHoveringText(tipsList, x, y, this.fontRendererObj);
                Block block = theFluid.getBlock();
                if (block != null) {
                    ItemStack blockStack = new ItemStack(block);
                    EntityPlayer player = this.mc.thePlayer;
                    if (ThaumcraftExtension.hasPlayerScanned(player, blockStack)) {
                        this.renderAspects(player, blockStack, x, y);
                    } else {
                        ItemStack bucket = FluidContainerRegistry.fillFluidContainer(fluid, new ItemStack(Items.bucket));
                        if (bucket != null && ThaumcraftExtension.hasPlayerScanned(player, bucket)) {
                            this.renderAspects(player, blockStack, x, y);
                        } else if (theFluid == CompatibilityManager.mushroomSoupBlock.getFluid()) {
                            if (ThaumcraftExtension.hasPlayerScanned(player, new ItemStack(Items.mushroom_stew))) {
                                this.renderAspects(player, blockStack, x, y);
                            }
                        } else if (theFluid == ModBlocks.fluidVishroom && ThaumcraftExtension.hasPlayerScanned(player, new ItemStack(ModItems.food, 1, 0))) {
                            this.renderAspects(player, blockStack, x, y);
                        }
                    }
                }
            }
        } else if (posX >= 82 && posX < 94 && posY >= 14 && posY < 26 && this.boiler.hasContentsCannotBoil()) {
            ArrayList<String> lines = TjUtil.getMultiLineLocalizedString("Automagy.gui.boiler.cannotProcess");
            this.drawHoveringText(lines, x, y, this.fontRendererObj);
        }
    }

    public boolean renderAspects(EntityPlayer player, ItemStack stack, int x, int y) {
        AspectList tags = ThaumcraftExtension.getObjectTotalAspects(stack);
        if (tags != null) {
            ThaumcraftExtension.renderAspectsForPlayer(this, player, tags, x, y);
            return true;
        } else {
            return false;
        }
    }
}
