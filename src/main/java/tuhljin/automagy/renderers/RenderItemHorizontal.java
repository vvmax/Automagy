package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import org.lwjgl.opengl.GL11;

public class RenderItemHorizontal implements IItemRenderer {
    private static RenderItem renderItem = new RenderItem();

    @Override
    public boolean handleRenderType(ItemStack itemStack, ItemRenderType type) {
        return type == ItemRenderType.ENTITY;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack par2ItemStack, Object... data) {
        GL11.glPushMatrix();
        TextureManager texturemanager = Minecraft.getMinecraft().renderEngine;
        IIcon iicon = par2ItemStack.getIconIndex();
        if (iicon == null) {
            GL11.glPopMatrix();
        } else {
            GL11.glScalef(0.8F, 0.8F, 0.8F);
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glTranslatef(0.75F, -0.25F, 0.0F);
            texturemanager.bindTexture(texturemanager.getResourceLocation(par2ItemStack.getItemSpriteNumber()));
            TextureUtil.func_152777_a(false, false, 1.0F);
            Tessellator tessellator = Tessellator.instance;
            float f = iicon.getMinU();
            float f1 = iicon.getMaxU();
            float f2 = iicon.getMinV();
            float f3 = iicon.getMaxV();
            float f4 = 0.0F;
            float f5 = 0.3F;
            GL11.glEnable(32826);
            GL11.glTranslatef(-f4, -f5, 0.0F);
            float f6 = 1.5F;
            GL11.glScalef(f6, f6, f6);
            GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
            ItemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, iicon.getIconWidth(), iicon.getIconHeight(), 0.0625F);
            GL11.glDisable(32826);
            texturemanager.bindTexture(texturemanager.getResourceLocation(par2ItemStack.getItemSpriteNumber()));
            TextureUtil.func_147945_b();
            GL11.glPopMatrix();
        }
    }
}
