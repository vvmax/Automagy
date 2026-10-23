package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.models.ModelGolemWorkbench;
import tuhljin.automagy.tiles.TileEntityGolemWorkbench;

public class BlockGolemWorkbenchRenderer extends TileEntitySpecialRenderer {
    public static final String TEXTURE_GOLEMWORKBENCH = "textures/models/golemWorkbench.png";
    private final ModelGolemWorkbench model = new ModelGolemWorkbench();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/golemWorkbench.png"));
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        this.model.render((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        if (te.hasWorldObj()) {
            TileEntityGolemWorkbench teGW = (TileEntityGolemWorkbench)te;
            ItemStack[] upgradeStacks = teGW.getUpgradeStacks();
            if (upgradeStacks != null) {
                for (int i = 0; i < upgradeStacks.length; i++) {
                    ItemStack stack = upgradeStacks[i];
                    if (stack != null) {
                        this.renderUpgrade(teGW, stack, i);
                    }
                }
            }
        }

        GL11.glPopMatrix();
    }

    protected void renderUpgrade(TileEntityGolemWorkbench teGW, ItemStack stack, int side) {
        IIcon icon = stack.getItem().getIcon(stack, 0);
        if (icon != null) {
            GL11.glPushMatrix();
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationItemsTexture);
            switch (side) {
                case 0:
                    GL11.glTranslated(-0.19, 0.93, -0.5);
                    break;
                case 1:
                    GL11.glRotated(90.0, 0.0, 1.0, 0.0);
                    GL11.glTranslated(-0.19, 0.93, -0.5);
                    break;
                case 2:
                    GL11.glTranslated(-0.19, 0.93, 0.5225);
                    break;
                case 3:
                    GL11.glRotated(90.0, 0.0, 1.0, 0.0);
                    GL11.glTranslated(-0.19, 0.93, 0.5225);
            }

            GL11.glScalef(0.375F, 0.375F, 0.375F);
            float f = icon.getMinU();
            float f1 = icon.getMaxU();
            float f2 = icon.getMinV();
            float f3 = icon.getMaxV();
            ItemRenderer.renderItemIn2D(Tessellator.instance, f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
            GL11.glPopMatrix();
        }
    }
}
