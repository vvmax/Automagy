package tuhljin.automagy.renderers;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.models.ModelHungryMaw;
import tuhljin.automagy.tiles.TileEntityMawFinical;

public class BlockHungryMawRenderer extends TileEntitySpecialRenderer {
    private static Random rand = new Random();
    private static final ResourceLocation texture1 = new ResourceLocation("thaumcraft", "textures/misc/tunnel.png");
    private static final ResourceLocation texture2 = new ResourceLocation("thaumcraft", "textures/misc/particlefield.png");
    FloatBuffer floatBuffer = GLAllocation.createDirectFloatBuffer(16);
    private final ModelHungryMaw model = new ModelHungryMaw();
    public static final String TEXTURE_STANDARD = "textures/models/hungryMaw.png";
    public static final String TEXTURE_FILTERED = "textures/models/finicalMaw.png";

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        int orientation;
        if (te.getWorld() == null) {
            orientation = 0;
        } else {
            orientation = te.getBlockMetadata();
        }

        String texture = te instanceof TileEntityMawFinical ? "textures/models/finicalMaw.png" : "textures/models/hungryMaw.png";
        switch (orientation) {
            case 0:
                this.drawPlaneTop(x, y, z);
                break;
            case 1:
                this.drawPlaneBottom(x, y, z);
                break;
            case 2:
                this.drawPlaneSouth(x, y, z);
                break;
            case 3:
                this.drawPlaneNorth(x, y, z);
                break;
            case 4:
                this.drawPlaneEast(x, y, z);
                break;
            case 5:
                this.drawPlaneWest(x, y, z);
        }

        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), texture));
        this.translateFromOrientation((float)x, (float)y, (float)z, orientation);
        GL11.glScaled(0.75, 0.75, 0.75);
        this.model.render((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        GL11.glPopMatrix();
    }

    public void drawPlaneBottom(double x, double y, double z) {
        double size = 0.29;
        double midtrans = (1.0 - size) / 2.0;
        GL11.glPushMatrix();
        GL11.glTranslated(midtrans, 0.0, midtrans);
        float f1 = (float)this.field_147501_a.field_147560_j;
        float f2 = (float)this.field_147501_a.field_147561_k;
        float f3 = (float)this.field_147501_a.field_147558_l;
        GL11.glDisable(2896);
        rand.setSeed(31100L);
        float f4 = 0.01F;

        for (int i = 0; i < 16; i++) {
            GL11.glPushMatrix();
            float f5 = 16 - i;
            float f6 = 0.0625F;
            float f7 = 1.0F / (f5 + 1.0F);
            if (i == 0) {
                this.bindTexture(texture1);
                f7 = 0.05F;
                f5 = 65.0F;
                f6 = 0.125F;
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
            }

            if (i == 1) {
                this.bindTexture(texture2);
                GL11.glEnable(3042);
                GL11.glBlendFunc(1, 1);
                f6 = 0.5F;
            }

            float f8 = (float)(-(y + f4));
            float f9 = f8 + ActiveRenderInfo.objectY;
            float f10 = f8 + f5 + ActiveRenderInfo.objectY;
            float f11 = f9 / f10;
            f11 += (float)(y + f4);
            GL11.glTranslatef(f1, f11, f3);
            GL11.glTexGeni(8192, 9472, 9217);
            GL11.glTexGeni(8193, 9472, 9217);
            GL11.glTexGeni(8194, 9472, 9217);
            GL11.glTexGeni(8195, 9472, 9216);
            GL11.glTexGen(8192, 9473, this.populateFloatBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GL11.glTexGen(8193, 9473, this.populateFloatBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GL11.glTexGen(8194, 9473, this.populateFloatBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GL11.glTexGen(8195, 9474, this.populateFloatBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GL11.glEnable(3168);
            GL11.glEnable(3169);
            GL11.glEnable(3170);
            GL11.glEnable(3171);
            GL11.glPopMatrix();
            GL11.glMatrixMode(5890);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0F, (float)(Minecraft.getSystemTime() % 700000L) / 700000.0F, 0.0F);
            GL11.glScalef(f6, f6, f6);
            GL11.glTranslatef(0.5F, 0.5F, 0.0F);
            GL11.glRotatef((i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.5F, -0.5F, 0.0F);
            GL11.glTranslatef(-f1, -f3, -f2);
            GL11.glTranslatef(ActiveRenderInfo.objectX * f5 / f9, ActiveRenderInfo.objectZ * f5 / f9, -f2);
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            f11 = rand.nextFloat() * 0.5F + 0.1F;
            float f12 = rand.nextFloat() * 0.5F + 0.4F;
            float f13 = rand.nextFloat() * 0.5F + 0.5F;
            if (i == 0) {
                f13 = 1.0F;
                f12 = 1.0F;
                f11 = 1.0F;
            }

            tessellator.setColorRGBA_F(f11 * f7, f12 * f7, f13 * f7, 1.0F);
            tessellator.addVertex(x, y + f4, z);
            tessellator.addVertex(x, y + f4, z + size);
            tessellator.addVertex(x + size, y + f4, z + size);
            tessellator.addVertex(x + size, y + f4, z);
            tessellator.draw();
            GL11.glPopMatrix();
            GL11.glMatrixMode(5888);
        }

        GL11.glDisable(3042);
        GL11.glDisable(3168);
        GL11.glDisable(3169);
        GL11.glDisable(3170);
        GL11.glDisable(3171);
        GL11.glEnable(2896);
        GL11.glPopMatrix();
    }

    public void drawPlaneTop(double x, double y, double z) {
        double size = 0.29;
        double midtrans = (1.0 - size) / 2.0;
        GL11.glPushMatrix();
        GL11.glTranslated(midtrans, 0.89, midtrans);
        float f1 = (float)this.field_147501_a.field_147560_j;
        float f2 = (float)this.field_147501_a.field_147561_k;
        float f3 = (float)this.field_147501_a.field_147558_l;
        GL11.glDisable(2896);
        rand.setSeed(31100L);
        float offset = 0.099F;

        for (int i = 0; i < 16; i++) {
            GL11.glPushMatrix();
            float f5 = 16 - i;
            float f6 = 0.0625F;
            float f7 = 1.0F / (f5 + 1.0F);
            if (i == 0) {
                this.bindTexture(texture1);
                f7 = 0.05F;
                f5 = 65.0F;
                f6 = 0.125F;
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
            }

            if (i == 1) {
                this.bindTexture(texture2);
                GL11.glEnable(3042);
                GL11.glBlendFunc(1, 1);
                f6 = 0.5F;
            }

            float f8 = (float)(y + offset);
            float f9 = f8 - ActiveRenderInfo.objectY;
            float f10 = f8 + f5 - ActiveRenderInfo.objectY;
            float f11 = f9 / f10;
            f11 += (float)(y + offset);
            GL11.glTranslatef(f1, f11, f3);
            GL11.glTexGeni(8192, 9472, 9217);
            GL11.glTexGeni(8193, 9472, 9217);
            GL11.glTexGeni(8194, 9472, 9217);
            GL11.glTexGeni(8195, 9472, 9216);
            GL11.glTexGen(8192, 9473, this.populateFloatBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GL11.glTexGen(8193, 9473, this.populateFloatBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GL11.glTexGen(8194, 9473, this.populateFloatBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GL11.glTexGen(8195, 9474, this.populateFloatBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GL11.glEnable(3168);
            GL11.glEnable(3169);
            GL11.glEnable(3170);
            GL11.glEnable(3171);
            GL11.glPopMatrix();
            GL11.glMatrixMode(5890);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0F, (float)(Minecraft.getSystemTime() % 700000L) / 700000.0F, 0.0F);
            GL11.glScalef(f6, f6, f6);
            GL11.glTranslatef(0.5F, 0.5F, 0.0F);
            GL11.glRotatef((i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.5F, -0.5F, 0.0F);
            GL11.glTranslatef(-f1, -f3, -f2);
            GL11.glTranslatef(ActiveRenderInfo.objectX * f5 / f9, ActiveRenderInfo.objectZ * f5 / f9, -f2);
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            f11 = rand.nextFloat() * 0.5F + 0.1F;
            float f12 = rand.nextFloat() * 0.5F + 0.4F;
            float f13 = rand.nextFloat() * 0.5F + 0.5F;
            if (i == 0) {
                f13 = 1.0F;
                f12 = 1.0F;
                f11 = 1.0F;
            }

            tessellator.setColorRGBA_F(f11 * f7, f12 * f7, f13 * f7, 1.0F);
            tessellator.addVertex(x, y + offset, z + size);
            tessellator.addVertex(x, y + offset, z);
            tessellator.addVertex(x + size, y + offset, z);
            tessellator.addVertex(x + size, y + offset, z + size);
            tessellator.draw();
            GL11.glPopMatrix();
            GL11.glMatrixMode(5888);
        }

        GL11.glDisable(3042);
        GL11.glDisable(3168);
        GL11.glDisable(3169);
        GL11.glDisable(3170);
        GL11.glDisable(3171);
        GL11.glEnable(2896);
        GL11.glPopMatrix();
    }

    public void drawPlaneNorth(double x, double y, double z) {
        double size = 0.29;
        double midtrans = (1.0 - size) / 2.0;
        GL11.glPushMatrix();
        GL11.glTranslated(midtrans, midtrans, 0.0);
        float f1 = (float)this.field_147501_a.field_147560_j;
        float f2 = (float)this.field_147501_a.field_147561_k;
        float f3 = (float)this.field_147501_a.field_147558_l;
        GL11.glDisable(2896);
        rand.setSeed(31100L);
        float offset = 0.01F;

        for (int i = 0; i < 16; i++) {
            GL11.glPushMatrix();
            float f5 = 16 - i;
            float f6 = 0.0625F;
            float f7 = 1.0F / (f5 + 1.0F);
            if (i == 0) {
                this.bindTexture(texture1);
                f7 = 0.05F;
                f5 = 65.0F;
                f6 = 0.125F;
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
            }

            if (i == 1) {
                this.bindTexture(texture2);
                GL11.glEnable(3042);
                GL11.glBlendFunc(1, 1);
                f6 = 0.5F;
            }

            float f8 = (float)(-(z + offset));
            float f9 = f8 + ActiveRenderInfo.objectZ;
            float f10 = f8 + f5 + ActiveRenderInfo.objectZ;
            float f11 = f9 / f10;
            f11 += (float)(z + offset);
            GL11.glTranslatef(f1, f2, f11);
            GL11.glTexGeni(8192, 9472, 9217);
            GL11.glTexGeni(8193, 9472, 9217);
            GL11.glTexGeni(8194, 9472, 9217);
            GL11.glTexGeni(8195, 9472, 9216);
            GL11.glTexGen(8192, 9473, this.populateFloatBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GL11.glTexGen(8193, 9473, this.populateFloatBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GL11.glTexGen(8194, 9473, this.populateFloatBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GL11.glTexGen(8195, 9474, this.populateFloatBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GL11.glEnable(3168);
            GL11.glEnable(3169);
            GL11.glEnable(3170);
            GL11.glEnable(3171);
            GL11.glPopMatrix();
            GL11.glMatrixMode(5890);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0F, (float)(Minecraft.getSystemTime() % 700000L) / 700000.0F, 0.0F);
            GL11.glScalef(f6, f6, f6);
            GL11.glTranslatef(0.5F, 0.5F, 0.0F);
            GL11.glRotatef((i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.5F, -0.5F, 0.0F);
            GL11.glTranslatef(-f1, -f2, -f3);
            GL11.glTranslatef(ActiveRenderInfo.objectX * f5 / f9, ActiveRenderInfo.objectX * f5 / f9, -f3);
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            f11 = rand.nextFloat() * 0.5F + 0.1F;
            float f12 = rand.nextFloat() * 0.5F + 0.4F;
            float f13 = rand.nextFloat() * 0.5F + 0.5F;
            if (i == 0) {
                f13 = 1.0F;
                f12 = 1.0F;
                f11 = 1.0F;
            }

            tessellator.setColorRGBA_F(f11 * f7, f12 * f7, f13 * f7, 1.0F);
            tessellator.addVertex(x, y + size, z);
            tessellator.addVertex(x, y, z + offset);
            tessellator.addVertex(x + size, y, z + offset);
            tessellator.addVertex(x + size, y + size, z + offset);
            tessellator.draw();
            GL11.glPopMatrix();
            GL11.glMatrixMode(5888);
        }

        GL11.glDisable(3042);
        GL11.glDisable(3168);
        GL11.glDisable(3169);
        GL11.glDisable(3170);
        GL11.glDisable(3171);
        GL11.glEnable(2896);
        GL11.glPopMatrix();
    }

    public void drawPlaneSouth(double x, double y, double z) {
        double size = 0.29;
        double midtrans = (1.0 - size) / 2.0;
        GL11.glPushMatrix();
        GL11.glTranslated(midtrans, midtrans, 0.0);
        float f1 = (float)this.field_147501_a.field_147560_j;
        float f2 = (float)this.field_147501_a.field_147561_k;
        float f3 = (float)this.field_147501_a.field_147558_l;
        GL11.glDisable(2896);
        rand.setSeed(31100L);
        float offset = 0.99F;

        for (int i = 0; i < 16; i++) {
            GL11.glPushMatrix();
            float f5 = 16 - i;
            float f6 = 0.0625F;
            float f7 = 1.0F / (f5 + 1.0F);
            if (i == 0) {
                this.bindTexture(texture1);
                f7 = 0.05F;
                f5 = 65.0F;
                f6 = 0.125F;
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
            }

            if (i == 1) {
                this.bindTexture(texture2);
                GL11.glEnable(3042);
                GL11.glBlendFunc(1, 1);
                f6 = 0.5F;
            }

            float f8 = (float)(z + offset);
            float f9 = f8 - ActiveRenderInfo.objectZ;
            float f10 = f8 + f5 - ActiveRenderInfo.objectZ;
            float f11 = f9 / f10;
            f11 += (float)(z + offset);
            GL11.glTranslatef(f1, f2, f11);
            GL11.glTexGeni(8192, 9472, 9217);
            GL11.glTexGeni(8193, 9472, 9217);
            GL11.glTexGeni(8194, 9472, 9217);
            GL11.glTexGeni(8195, 9472, 9216);
            GL11.glTexGen(8192, 9473, this.populateFloatBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GL11.glTexGen(8193, 9473, this.populateFloatBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GL11.glTexGen(8194, 9473, this.populateFloatBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GL11.glTexGen(8195, 9474, this.populateFloatBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GL11.glEnable(3168);
            GL11.glEnable(3169);
            GL11.glEnable(3170);
            GL11.glEnable(3171);
            GL11.glPopMatrix();
            GL11.glMatrixMode(5890);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0F, (float)(Minecraft.getSystemTime() % 700000L) / 700000.0F, 0.0F);
            GL11.glScalef(f6, f6, f6);
            GL11.glTranslatef(0.5F, 0.5F, 0.0F);
            GL11.glRotatef((i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.5F, -0.5F, 0.0F);
            GL11.glTranslatef(-f1, -f2, -f3);
            GL11.glTranslatef(ActiveRenderInfo.objectX * f5 / f9, ActiveRenderInfo.objectY * f5 / f9, -f3);
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            f11 = rand.nextFloat() * 0.5F + 0.1F;
            float f12 = rand.nextFloat() * 0.5F + 0.4F;
            float f13 = rand.nextFloat() * 0.5F + 0.5F;
            if (i == 0) {
                f13 = 1.0F;
                f12 = 1.0F;
                f11 = 1.0F;
            }

            tessellator.setColorRGBA_F(f11 * f7, f12 * f7, f13 * f7, 1.0F);
            tessellator.addVertex(x, y, z + offset);
            tessellator.addVertex(x, y + size, z + offset);
            tessellator.addVertex(x + size, y + size, z + offset);
            tessellator.addVertex(x + size, y, z + offset);
            tessellator.draw();
            GL11.glPopMatrix();
            GL11.glMatrixMode(5888);
        }

        GL11.glDisable(3042);
        GL11.glDisable(3168);
        GL11.glDisable(3169);
        GL11.glDisable(3170);
        GL11.glDisable(3171);
        GL11.glEnable(2896);
        GL11.glPopMatrix();
    }

    public void drawPlaneWest(double x, double y, double z) {
        double size = 0.29;
        double midtrans = (1.0 - size) / 2.0;
        GL11.glPushMatrix();
        GL11.glTranslated(0.0, midtrans, midtrans);
        float f1 = (float)this.field_147501_a.field_147560_j;
        float f2 = (float)this.field_147501_a.field_147561_k;
        float f3 = (float)this.field_147501_a.field_147558_l;
        GL11.glDisable(2896);
        rand.setSeed(31100L);
        float offset = 0.01F;

        for (int i = 0; i < 16; i++) {
            GL11.glPushMatrix();
            float f5 = 16 - i;
            float f6 = 0.0625F;
            float f7 = 1.0F / (f5 + 1.0F);
            if (i == 0) {
                this.bindTexture(texture1);
                f7 = 0.05F;
                f5 = 65.0F;
                f6 = 0.125F;
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
            }

            if (i == 1) {
                this.bindTexture(texture2);
                GL11.glEnable(3042);
                GL11.glBlendFunc(1, 1);
                f6 = 0.5F;
            }

            float f8 = (float)(-(x + offset));
            float f9 = f8 + ActiveRenderInfo.objectX;
            float f10 = f8 + f5 + ActiveRenderInfo.objectX;
            float f11 = f9 / f10;
            f11 += (float)(x + offset);
            GL11.glTranslatef(f11, f2, f3);
            GL11.glTexGeni(8192, 9472, 9217);
            GL11.glTexGeni(8193, 9472, 9217);
            GL11.glTexGeni(8194, 9472, 9217);
            GL11.glTexGeni(8195, 9472, 9216);
            GL11.glTexGen(8193, 9473, this.populateFloatBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GL11.glTexGen(8192, 9473, this.populateFloatBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GL11.glTexGen(8194, 9473, this.populateFloatBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GL11.glTexGen(8195, 9474, this.populateFloatBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GL11.glEnable(3168);
            GL11.glEnable(3169);
            GL11.glEnable(3170);
            GL11.glEnable(3171);
            GL11.glPopMatrix();
            GL11.glMatrixMode(5890);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0F, (float)(Minecraft.getSystemTime() % 700000L) / 700000.0F, 0.0F);
            GL11.glScalef(f6, f6, f6);
            GL11.glTranslatef(0.5F, 0.5F, 0.0F);
            GL11.glRotatef((i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.5F, -0.5F, 0.0F);
            GL11.glTranslatef(-f3, -f2, -f1);
            GL11.glTranslatef(ActiveRenderInfo.objectZ * f5 / f9, ActiveRenderInfo.objectY * f5 / f9, -f1);
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            f11 = rand.nextFloat() * 0.5F + 0.1F;
            float f12 = rand.nextFloat() * 0.5F + 0.4F;
            float f13 = rand.nextFloat() * 0.5F + 0.5F;
            if (i == 0) {
                f13 = 1.0F;
                f12 = 1.0F;
                f11 = 1.0F;
            }

            tessellator.setColorRGBA_F(f11 * f7, f12 * f7, f13 * f7, 1.0F);
            tessellator.addVertex(x + offset, y + size, z);
            tessellator.addVertex(x + offset, y + size, z + size);
            tessellator.addVertex(x + offset, y, z + size);
            tessellator.addVertex(x + offset, y, z);
            tessellator.draw();
            GL11.glPopMatrix();
            GL11.glMatrixMode(5888);
        }

        GL11.glDisable(3042);
        GL11.glDisable(3168);
        GL11.glDisable(3169);
        GL11.glDisable(3170);
        GL11.glDisable(3171);
        GL11.glEnable(2896);
        GL11.glPopMatrix();
    }

    public void drawPlaneEast(double x, double y, double z) {
        double size = 0.29;
        double midtrans = (1.0 - size) / 2.0;
        GL11.glPushMatrix();
        GL11.glTranslated(0.0, midtrans, midtrans);
        float f1 = (float)this.field_147501_a.field_147560_j;
        float f2 = (float)this.field_147501_a.field_147561_k;
        float f3 = (float)this.field_147501_a.field_147558_l;
        GL11.glDisable(2896);
        rand.setSeed(31100L);
        float offset = 0.99F;

        for (int i = 0; i < 16; i++) {
            GL11.glPushMatrix();
            float f5 = 16 - i;
            float f6 = 0.0625F;
            float f7 = 1.0F / (f5 + 1.0F);
            if (i == 0) {
                this.bindTexture(texture1);
                f7 = 0.05F;
                f5 = 65.0F;
                f6 = 0.125F;
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
            }

            if (i == 1) {
                this.bindTexture(texture2);
                GL11.glEnable(3042);
                GL11.glBlendFunc(1, 1);
                f6 = 0.5F;
            }

            float f8 = (float)(x + offset);
            float f9 = f8 - ActiveRenderInfo.objectX;
            float f10 = f8 + f5 - ActiveRenderInfo.objectX;
            float f11 = f9 / f10;
            f11 += (float)(x + offset);
            GL11.glTranslatef(f11, f2, f3);
            GL11.glTexGeni(8192, 9472, 9217);
            GL11.glTexGeni(8193, 9472, 9217);
            GL11.glTexGeni(8194, 9472, 9217);
            GL11.glTexGeni(8195, 9472, 9216);
            GL11.glTexGen(8193, 9473, this.populateFloatBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GL11.glTexGen(8192, 9473, this.populateFloatBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GL11.glTexGen(8194, 9473, this.populateFloatBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GL11.glTexGen(8195, 9474, this.populateFloatBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GL11.glEnable(3168);
            GL11.glEnable(3169);
            GL11.glEnable(3170);
            GL11.glEnable(3171);
            GL11.glPopMatrix();
            GL11.glMatrixMode(5890);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0F, (float)(Minecraft.getSystemTime() % 700000L) / 700000.0F, 0.0F);
            GL11.glScalef(f6, f6, f6);
            GL11.glTranslatef(0.5F, 0.5F, 0.0F);
            GL11.glRotatef((i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.5F, -0.5F, 0.0F);
            GL11.glTranslatef(-f3, -f2, -f1);
            GL11.glTranslatef(ActiveRenderInfo.objectZ * f5 / f9, ActiveRenderInfo.objectY * f5 / f9, -f1);
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            f11 = rand.nextFloat() * 0.5F + 0.1F;
            float f12 = rand.nextFloat() * 0.5F + 0.4F;
            float f13 = rand.nextFloat() * 0.5F + 0.5F;
            if (i == 0) {
                f13 = 1.0F;
                f12 = 1.0F;
                f11 = 1.0F;
            }

            tessellator.setColorRGBA_F(f11 * f7, f12 * f7, f13 * f7, 1.0F);
            tessellator.addVertex(x + offset, y, z);
            tessellator.addVertex(x + offset, y, z + size);
            tessellator.addVertex(x + offset, y + size, z + size);
            tessellator.addVertex(x + offset, y + size, z);
            tessellator.draw();
            GL11.glPopMatrix();
            GL11.glMatrixMode(5888);
        }

        GL11.glDisable(3042);
        GL11.glDisable(3168);
        GL11.glDisable(3169);
        GL11.glDisable(3170);
        GL11.glDisable(3171);
        GL11.glEnable(2896);
        GL11.glPopMatrix();
    }

    private FloatBuffer populateFloatBuffer(float p_147525_1_, float p_147525_2_, float p_147525_3_, float p_147525_4_) {
        ((Buffer)this.floatBuffer).clear();
        this.floatBuffer.put(p_147525_1_).put(p_147525_2_).put(p_147525_3_).put(p_147525_4_);
        ((Buffer)this.floatBuffer).flip();
        return this.floatBuffer;
    }

    private void translateFromOrientation(float x, float y, float z, int orientation) {
        if (orientation == 0) {
            GL11.glTranslatef(x + 0.5F, y + 2.09F, z + 0.5F);
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        } else if (orientation == 1) {
            GL11.glTranslatef(x + 0.5F, y - 1.09F, z + 0.5F);
        } else if (orientation == 2) {
            GL11.glTranslatef(x + 0.5F, y + 0.5F, z + 2.09F);
            GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
        } else if (orientation == 3) {
            GL11.glTranslatef(x + 0.5F, y + 0.5F, z - 1.09F);
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
        } else if (orientation == 4) {
            GL11.glTranslatef(x + 2.09F, y + 0.5F, z + 0.5F);
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        } else if (orientation == 5) {
            GL11.glTranslatef(x - 1.09F, y + 0.5F, z + 0.5F);
            GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
        }
    }
}
