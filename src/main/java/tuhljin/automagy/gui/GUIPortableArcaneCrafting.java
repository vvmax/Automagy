package tuhljin.automagy.gui;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import tuhljin.automagy.codechicken.lib.math.MathHelper;
import tuhljin.automagy.items.ItemFocusCrafting;

public class GUIPortableArcaneCrafting extends ModGuiContainer {
    public final ResourceLocation texture;
    private ContainerPortableArcaneCrafting container;
    private float craftAlpha = 0.138F;
    private boolean lastVisInsufficient = false;
    private boolean usingStaff = false;
    private int[][] aspectLocs = new int[][]{{72, 21}, {24, 43}, {24, 102}, {72, 124}, {120, 102}, {120, 43}};
    ArrayList<Aspect> primals = Aspect.getPrimalAspects();
    private InventoryPlayer ip;

    private GUIPortableArcaneCrafting(ContainerPortableArcaneCrafting container, EntityPlayer player) {
        super(container);
        this.container = container;
        this.ip = player.inventory;
        this.texture = new ResourceLocation("Automagy:textures/gui/focusCrafting.png");
        this.xSize = 190;
        this.ySize = 206;
        this.usingStaff = container.craftingInv.getStackInSlot(10) == null;
    }

    public GUIPortableArcaneCrafting(EntityPlayer player, int wandSlot) {
        this(new ContainerPortableArcaneCrafting(player, wandSlot), player);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        if (this.craftAlpha < 2.0F) {
            this.craftAlpha = this.craftAlpha + (this.craftAlpha < 1.0F ? 0.01F : 0.005F);
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(3042);
        int x = 135;
        int y = 135;
        int offY = -6;
        int invExtraOffY = -6;
        this.drawTexturedModalRect(this.guiLeft + x, this.guiTop + offY, x, 0, this.xSize - x, y);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop + y + offY + invExtraOffY, 0, y, this.xSize, this.ySize - y - offY - invExtraOffY + 6);
        if (this.craftAlpha < 1.0F) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, Math.min(this.craftAlpha, 1.0F));
            this.drawTexturedModalRect(this.guiLeft, this.guiTop + offY, 0, 0, x, y);
        } else {
            float r = this.interpolate(1.0F, 0.5F, this.craftAlpha - 1.0F);
            float g = this.interpolate(1.0F, 0.8F, this.craftAlpha - 1.0F);
            float b = this.interpolate(1.0F, 0.5F, this.craftAlpha - 1.0F);
            GL11.glColor4f(r, g, b, 1.0F);
            this.drawTexturedModalRect(this.guiLeft, this.guiTop + offY, 0, 0, x, y);
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(3042);
        this.drawTexturedModalRect(
            this.guiLeft + 15 + this.container.blockedSlotHotbarPos * 18,
            this.guiTop + 187,
            0,
            this.ySize + 18,
            this.container.blockedSlotHotbarPos == 8 ? 18 : 17,
            18
        );
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, offY + invExtraOffY + 1, 0.0F);
        this.drawRecipeResultsAndAspects();
        GL11.glPopMatrix();
        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
    }

    private float interpolate(float a, float b, float interpolationFactor) {
        return MathHelper.interpolate(a, b, interpolationFactor);
    }

    private void drawRecipeResultsAndAspects() {
        int var5 = (this.width - this.xSize) / 2;
        int var6 = (this.height - this.ySize) / 2;
        ItemWandCasting wand = null;
        if (this.container.craftingInv.getStackInSlot(10) != null && this.container.craftingInv.getStackInSlot(10).getItem() instanceof ItemWandCasting) {
            wand = (ItemWandCasting)this.container.craftingInv.getStackInSlot(10).getItem();
        }

        AspectList cost = null;
        if (ThaumcraftCraftingManager.findMatchingArcaneRecipe(this.container.fakeTile, this.ip.player) != null) {
            AspectList extraCost = this.getExtraVisCostForPortable(wand, this.container.craftingInv.getStackInSlot(10));
            cost = ThaumcraftCraftingManager.findMatchingArcaneRecipeAspects(this.container.fakeTile, this.ip.player);
            int count = 0;

            for (Aspect primal : this.primals) {
                float amt = cost.getAmount(primal);
                float extraAmt = extraCost.getAmount(primal);
                if (cost.getAmount(primal) > 0 || extraAmt > 0.0F) {
                    float alpha = 0.5F + (net.minecraft.util.MathHelper.sin((this.ip.player.ticksExisted + count * 10) / 2.0F) * 0.2F - 0.2F);
                    if (wand != null) {
                        amt += extraAmt / 100.0F;
                        if (amt > 0.0F) {
                            amt *= wand.getConsumptionModifier(this.container.craftingInv.getStackInSlot(10), this.ip.player, primal, true);
                        }

                        if (amt * 100.0F <= wand.getVis(this.container.craftingInv.getStackInSlot(10), primal)) {
                            alpha = 1.0F;
                        }
                    }

                    UtilsFX.drawTag(
                        var5 + this.aspectLocs[count][0] - 8, var6 + this.aspectLocs[count][1] - 8, primal, amt, 0, (double)this.zLevel, 771, alpha, false
                    );
                }

                if (++count > 5) {
                    break;
                }
            }
        }

        if (wand != null && cost != null && !wand.consumeAllVisCrafting(this.container.craftingInv.getStackInSlot(10), this.ip.player, cost, false)) {
            GL11.glPushMatrix();
            float var40 = 0.33F;
            GL11.glColor4f(var40, var40, var40, 0.66F);
            itemRender.renderWithColor = false;
            GL11.glEnable(2896);
            GL11.glEnable(2884);
            GL11.glEnable(3042);
            itemRender.renderItemAndEffectIntoGUI(
                this.mc.fontRendererObj,
                this.mc.renderEngine,
                ThaumcraftCraftingManager.findMatchingArcaneRecipe(this.container.fakeTile, this.ip.player),
                var5 + 160,
                var6 + 64
            );
            itemRender.renderItemOverlayIntoGUI(
                this.mc.fontRendererObj,
                this.mc.renderEngine,
                ThaumcraftCraftingManager.findMatchingArcaneRecipe(this.container.fakeTile, this.ip.player),
                var5 + 160,
                var6 + 64
            );
            itemRender.renderWithColor = true;
            GL11.glDisable(3042);
            GL11.glDisable(2896);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glTranslatef(var5 + 168, var6 + 46, 0.0F);
            GL11.glScalef(0.5F, 0.5F, 0.0F);
            String text = StatCollector.translateToLocal("Automagy.gui.focusCrafting.visLow");
            int ll = this.fontRendererObj.getStringWidth(text) / 2;
            this.fontRendererObj.drawString(text, -ll, 0, 15625838);
            GL11.glScalef(1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
            if (!this.lastVisInsufficient) {
                this.lastVisInsufficient = true;
                this.container.onCraftMatrixChanged(this.container.craftingInv);
            }
        } else if (this.lastVisInsufficient) {
            this.lastVisInsufficient = false;
        } else if (this.usingStaff) {
            GL11.glPushMatrix();
            GL11.glDisable(2896);
            GL11.glTranslatef(var5 + 168, var6 + 46, 0.0F);
            GL11.glScalef(0.5F, 0.5F, 0.0F);
            String text = StatCollector.translateToLocal("Automagy.gui.focusCrafting.staff.1");
            int ll = this.fontRendererObj.getStringWidth(text) / 2;
            this.fontRendererObj.drawString(text, -ll, -3, 15625838);
            text = StatCollector.translateToLocal("Automagy.gui.focusCrafting.staff.2");
            ll = this.fontRendererObj.getStringWidth(text) / 2;
            this.fontRendererObj.drawString(text, -ll, 5, 15625838);
            GL11.glScalef(1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
        }
    }

    @Override
    protected void buttonClicked(GuiButton button, boolean rightClick) {
    }

    public AspectList getExtraVisCostForPortable(ItemWandCasting wand, ItemStack wandstack) {
        ItemFocusCrafting focus = ItemFocusCrafting.getCraftingFocusOnWand(wandstack);
        return focus == null ? ItemFocusCrafting.visCost : focus.getVisCost(wand.getFocusItem(wandstack));
    }
}
