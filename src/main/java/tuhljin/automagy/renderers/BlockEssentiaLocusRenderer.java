package tuhljin.automagy.renderers;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.renderers.models.ModelBrain;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.tiles.TileEntityEssentiaLocus;

public class BlockEssentiaLocusRenderer extends TileEntitySpecialRenderer {
    public static final String TEXTURE_BRAIN_WATER = "textures/models/brainWater.png";
    private final ModelBrain brainModel = new ModelBrain();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        TileEntityEssentiaLocus teLocus = (TileEntityEssentiaLocus)te;
        GL11.glPushMatrix();
        if (teLocus.hasAggregator) {
            char bright = 240;
            float brightX = bright % 65536;
            float brightY = bright / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightX, brightY);
        }

        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/brainWater.png"));
        GL11.glTranslatef(0.0F, -0.9F, 0.0F);
        GL11.glScalef(0.4F, 0.4F, 0.4F);
        if (te.getWorld() != null) {
            float rotationAngle = 1.0F + (float)teLocus.clientRenderRotationHelper / TileEntityEssentiaLocus.rotationSpeedFactor * 360.0F;
            GL11.glRotatef(rotationAngle, 0.0F, 1.0F, 0.0F);
            GL11.glTranslated(0.0, teLocus.clientRenderFloatingDistance, 0.0);
        }

        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.95F);
        this.brainModel.render();
        GL11.glPopMatrix();
        int totalEyes = teLocus.getEyeCount();
        int numEyes = MathHelper.ceiling_float_int(totalEyes / 2.0F);
        if (numEyes > 0) {
            GL11.glPushMatrix();
            GL11.glTranslated(x + 0.5, y + 0.2, z + 0.5);
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            float modifier = 6.0F;
            float rotationModifier = 0.25F;
            float radiusBase = 1.8F;
            float radiusMod = 0.2F;
            float v = 0.125F;
            double ticks = Minecraft.getMinecraft().thePlayer.ticksExisted * 0.5;
            float offsetPerEye = 360 / numEyes;
            ticks *= 0.5;

            for (int i = 0; i < numEyes; i++) {
                float offset = offsetPerEye * i;
                float deg = (int)(ticks / 0.25 % 360.0 + offset);
                float rad = deg * (float) Math.PI / 180.0F;
                float radiusX = (float)(1.8F + 0.2F * Math.sin(ticks / 6.0));
                float radiusZ = (float)(1.8F + 0.2F * Math.cos(ticks / 6.0));
                float x2 = (float)(radiusX * Math.cos(rad));
                float z2 = (float)(radiusZ * Math.sin(rad));
                float y2 = (float)Math.cos((ticks + 50 * i) / 5.0) / 10.0F;
                GL11.glPushMatrix();
                GL11.glTranslatef(x2, y2, z2);
                float xRotate = (float)Math.sin(ticks * 0.25) / 2.0F;
                float yRotate = (float)Math.max(0.6F, Math.sin(ticks * 0.1F) / 2.0 + 0.5);
                float zRotate = (float)Math.cos(ticks * 0.25) / 2.0F;
                v /= 2.0F;
                GL11.glTranslatef(v, v, v);
                GL11.glRotatef(deg, xRotate * 0.1F, yRotate, zRotate * 0.1F);
                GL11.glTranslatef(-v, -v, -v);
                v *= 2.0F;
                ItemStack stack = new ItemStack(ModItems.crystalEye);
                stack.stackTagCompound = new NBTTagCompound();
                stack.stackTagCompound.setInteger("lookX", (int)x);
                Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationItemsTexture);
                IIcon icon = stack.getItem().getIcon(stack, 0);
                if (icon != null) {
                    Color color = new Color(stack.getItem().getColorFromItemStack(stack, 0));
                    GL11.glColor3ub((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue());
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.75F);
                    GL11.glScalef(0.5F, 0.5F, 0.5F);
                    float f = icon.getMinU();
                    float f1 = icon.getMaxU();
                    float f2 = icon.getMinV();
                    float f3 = icon.getMaxV();
                    ItemRenderer.renderItemIn2D(Tessellator.instance, f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
                    GL11.glColor3f(1.0F, 1.0F, 1.0F);
                }

                GL11.glPopMatrix();
            }

            GL11.glPopMatrix();
        }

        numEyes = totalEyes / 2;
        if (numEyes > 0) {
            GL11.glPushMatrix();
            GL11.glTranslated(x + 0.5, y - 0.2, z + 0.5);
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            float modifier = 6.0F;
            float rotationModifier = -0.25F;
            float radiusBase = 1.4F;
            float radiusMod = 0.1F;
            float v = 0.125F;
            double ticks = Minecraft.getMinecraft().thePlayer.ticksExisted * 0.5;
            float offsetPerEye = 360 / numEyes;
            ticks *= 0.5;

            for (int i = 0; i < numEyes; i++) {
                float offset = offsetPerEye * i;
                float deg = (int)(ticks / -0.25 % 360.0 + offset);
                float rad = deg * (float) Math.PI / 180.0F;
                float radiusX = (float)(1.4F + 0.1F * Math.sin(ticks / 6.0));
                float radiusZ = (float)(1.4F + 0.1F * Math.cos(ticks / 6.0));
                float x2 = (float)(radiusX * Math.cos(rad));
                float z2 = (float)(radiusZ * Math.sin(rad));
                float y2 = (float)Math.cos((ticks + 50 * i) / 5.0) / 10.0F;
                GL11.glPushMatrix();
                GL11.glTranslatef(x2, y2, z2);
                float xRotate = (float)Math.sin(ticks * -0.25) / 2.0F;
                float yRotate = (float)Math.max(0.6F, Math.sin(ticks * 0.1F) / 2.0 + 0.5);
                float zRotate = (float)Math.cos(ticks * -0.25) / 2.0F;
                v /= 2.0F;
                GL11.glTranslatef(v, v, v);
                GL11.glRotatef(deg, xRotate * 0.1F, yRotate, zRotate * 0.1F);
                GL11.glTranslatef(-v, -v, -v);
                v *= 2.0F;
                ItemStack stack = new ItemStack(ModItems.crystalEye);
                stack.stackTagCompound = new NBTTagCompound();
                stack.stackTagCompound.setInteger("lookX", (int)x);
                Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationItemsTexture);
                IIcon icon = stack.getItem().getIcon(stack, 0);
                if (icon != null) {
                    Color color = new Color(stack.getItem().getColorFromItemStack(stack, 0));
                    GL11.glColor3ub((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue());
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.75F);
                    GL11.glScalef(0.5F, 0.5F, 0.5F);
                    float f = icon.getMinU();
                    float f1 = icon.getMaxU();
                    float f2 = icon.getMinV();
                    float f3 = icon.getMaxV();
                    ItemRenderer.renderItemIn2D(Tessellator.instance, f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
                    GL11.glColor3f(1.0F, 1.0F, 1.0F);
                }

                GL11.glPopMatrix();
            }

            GL11.glPopMatrix();
        }
    }
}
