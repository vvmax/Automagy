package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.tiles.TileEntityAmberEncasement;

public class BlockItemInsideTileRenderer extends TileEntitySpecialRenderer {
    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        TileEntityAmberEncasement teA = (TileEntityAmberEncasement)te;
        ItemStack stack = teA.stack;
        stack = new ItemStack(Items.diamond_sword);
        if (stack != null) {
            GL11.glPushMatrix();
            char bright = 240;
            float brightX = bright % 65536;
            float brightY = bright / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightX, brightY);
            EntityItem entityitem = null;
            float ticks = 45.0F;
            ItemStack is = stack.copy();
            is.stackSize = 1;
            entityitem = new EntityItem(te.getWorld(), 0.0, 0.0, 0.0, is);
            entityitem.hoverStart = 0.0F;
            float h = -15.0F;
            GL11.glTranslatef((float)x + 0.5F, (float)y + entityitem.height, (float)z + 0.5F);
            GL11.glRotatef(ticks % 360.0F, 0.0F, 1.0F, 0.0F);
            if (stack.getItem() instanceof ItemBlock) {
                GL11.glScalef(2.0F, 2.0F, 2.0F);
            } else {
                GL11.glScalef(1.0F, 1.0F, 1.0F);
            }

            RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0, 0.0, 0.0, 0.0F, 0.0F);
            if (!Minecraft.isFancyGraphicsEnabled()) {
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0, 0.0, 0.0, 0.0F, 0.0F);
            }

            GL11.glPopMatrix();
        }
    }
}
