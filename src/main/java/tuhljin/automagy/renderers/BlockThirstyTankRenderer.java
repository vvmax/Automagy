package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.codechicken.lib.math.MathHelper;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.models.ModelThirstyTank;
import tuhljin.automagy.tiles.TileEntityThirstyTank;

public class BlockThirstyTankRenderer extends TileEntitySpecialRenderer {
    public static final String TEXTURE = "textures/models/thirstyTank.png";
    public static int renderPass = 0;
    private final ModelThirstyTank model = new ModelThirstyTank();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        TileEntityThirstyTank teTT = te instanceof TileEntityThirstyTank ? (TileEntityThirstyTank)te : null;
        World world = te.getWorld();
        if ((renderPass == 0 || world == null) && teTT != null) {
            FluidStack fluidStack = teTT.tank.getFluid();
            if (fluidStack != null) {
                GL11.glPushMatrix();
                GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
                GL11.glScaled(0.7, 0.8, 0.7);
                GL11.glTranslated(0.0, 0.02, 0.0);
                GL11.glDisable(2896);
                Fluid fluid = fluidStack.getFluid();
                IIcon texture = fluid.getStillIcon();
                int color;
                if (texture != null) {
                    this.bindTexture(getFluidSheet(fluid));
                    color = fluid.getColor(fluidStack);
                } else {
                    this.bindTexture(TextureMap.locationBlocksTexture);
                    texture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("missingno");
                    color = -1;
                }

                Tessellator t = Tessellator.instance;
                double height = teTT.getFractionFull();
                if (teTT.liquidRenderHeight != height) {
                    if (teTT.liquidRenderHeight != -1.0 && !TjUtil.areDoublesAlmostEqual_lowprec(height, teTT.liquidRenderHeight)) {
                        height = MathHelper.approachLinear(teTT.liquidRenderHeight, height, 0.015);
                        teTT.liquidRenderHeight = height;
                    } else {
                        teTT.liquidRenderHeight = height;
                    }
                }

                double uMin = texture.getInterpolatedU(0.0);
                double uMax = texture.getInterpolatedU(16.0);
                double vMin = texture.getInterpolatedV(0.0);
                double vMax = texture.getInterpolatedV(16.0);
                double vHeight = vMax - vMin;
                float r = (color >> 16 & 0xFF) / 255.0F;
                float g = (color >> 8 & 0xFF) / 255.0F;
                float b = (color & 0xFF) / 255.0F;
                t.startDrawingQuads();
                t.setColorOpaque_F(r, g, b);
                t.addVertexWithUV(0.5, -0.5, -0.5, uMax, vMin);
                t.addVertexWithUV(-0.5, -0.5, -0.5, uMin, vMin);
                t.addVertexWithUV(-0.5, -0.5 + height, -0.5, uMin, vMin + vHeight * height);
                t.addVertexWithUV(0.5, -0.5 + height, -0.5, uMax, vMin + vHeight * height);
                t.addVertexWithUV(0.5, -0.5, 0.5, uMin, vMin);
                t.addVertexWithUV(0.5, -0.5 + height, 0.5, uMin, vMin + vHeight * height);
                t.addVertexWithUV(-0.5, -0.5 + height, 0.5, uMax, vMin + vHeight * height);
                t.addVertexWithUV(-0.5, -0.5, 0.5, uMax, vMin);
                t.addVertexWithUV(0.5, -0.5, -0.5, uMin, vMin);
                t.addVertexWithUV(0.5, -0.5 + height, -0.5, uMin, vMin + vHeight * height);
                t.addVertexWithUV(0.5, -0.5 + height, 0.5, uMax, vMin + vHeight * height);
                t.addVertexWithUV(0.5, -0.5, 0.5, uMax, vMin);
                t.addVertexWithUV(-0.5, -0.5, 0.5, uMin, vMin);
                t.addVertexWithUV(-0.5, -0.5 + height, 0.5, uMin, vMin + vHeight * height);
                t.addVertexWithUV(-0.5, -0.5 + height, -0.5, uMax, vMin + vHeight * height);
                t.addVertexWithUV(-0.5, -0.5, -0.5, uMax, vMin);
                t.addVertexWithUV(0.5, -0.5 + height, 0.5, uMax, vMin);
                t.addVertexWithUV(0.5, -0.5 + height, -0.5, uMin, vMin);
                t.addVertexWithUV(-0.5, -0.5 + height, -0.5, uMin, vMax);
                t.addVertexWithUV(-0.5, -0.5 + height, 0.5, uMax, vMax);
                t.addVertexWithUV(0.5, -0.5, -0.5, uMax, vMin);
                t.addVertexWithUV(0.5, -0.5, 0.5, uMin, vMin);
                t.addVertexWithUV(-0.5, -0.5, 0.5, uMin, vMax);
                t.addVertexWithUV(-0.5, -0.5, -0.5, uMax, vMax);
                t.draw();
                GL11.glEnable(2896);
                GL11.glPopMatrix();
            }
        }

        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/thirstyTank.png"));
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glScalef(0.94F, 0.94F, 0.94F);
        GL11.glTranslatef(0.0F, 0.098F, 0.0F);
        if (world == null) {
            this.model.render((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        } else if (renderPass == 0) {
            this.model.renderStone((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        } else {
            this.model.renderGlass((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        }

        GL11.glPopMatrix();
        if (teTT != null && (renderPass == 0 || world == null)) {
            GL11.glPushMatrix();
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
            if (world != null) {
                RenderHelper.disableStandardItemLighting();
            }

            int brightness = 230;
            if (world != null) {
                brightness = ModBlocks.thirstyTank.getMixedBrightnessForBlock(world, te.xCoord, te.yCoord, te.zCoord);
            }

            float size = 0.22F;
            int glyph = teTT.glyphs[0];
            if (glyph > 0) {
                GL11.glPushMatrix();
                GL11.glTranslated(0.0, -0.503, 0.0);
                GL11.glRotated(-90.0, 1.0, 0.0, 0.0);
                UtilsFX.renderQuadCenteredFromIcon(false, ModItems.tankGlyph.getInstalledIcon(glyph), size, 1.0F, 1.0F, 1.0F, brightness, 771, 1.0F);
                GL11.glPopMatrix();
            }

            glyph = teTT.glyphs[1];
            if (glyph > 0) {
                GL11.glPushMatrix();
                GL11.glTranslated(0.0, 0.438, 0.0);
                GL11.glRotated(90.0, 1.0, 0.0, 0.0);
                UtilsFX.renderQuadCenteredFromIcon(false, ModItems.tankGlyph.getInstalledIcon(glyph), size, 1.0F, 1.0F, 1.0F, brightness, 771, 1.0F);
                GL11.glPopMatrix();
            }

            glyph = teTT.glyphs[2];
            if (glyph > 0) {
                GL11.glPushMatrix();
                GL11.glTranslated(0.0, 0.0, -0.36);
                GL11.glRotated(180.0, 0.0, 0.0, 1.0);
                UtilsFX.renderQuadCenteredFromIcon(false, ModItems.tankGlyph.getInstalledIcon(glyph), size, 1.0F, 1.0F, 1.0F, brightness, 771, 1.0F);
                GL11.glPopMatrix();
            }

            glyph = teTT.glyphs[3];
            if (glyph > 0) {
                GL11.glPushMatrix();
                GL11.glTranslated(0.0, 0.0, 0.36);
                GL11.glRotated(180.0, 1.0, 0.0, 0.0);
                UtilsFX.renderQuadCenteredFromIcon(false, ModItems.tankGlyph.getInstalledIcon(glyph), size, 1.0F, 1.0F, 1.0F, brightness, 771, 1.0F);
                GL11.glPopMatrix();
            }

            glyph = teTT.glyphs[4];
            if (glyph > 0) {
                GL11.glPushMatrix();
                GL11.glTranslated(-0.36, 0.0, 0.0);
                GL11.glRotated(90.0, 0.0, 1.0, 0.0);
                GL11.glRotated(180.0, 0.0, 0.0, 1.0);
                UtilsFX.renderQuadCenteredFromIcon(false, ModItems.tankGlyph.getInstalledIcon(glyph), size, 1.0F, 1.0F, 1.0F, brightness, 771, 1.0F);
                GL11.glPopMatrix();
            }

            glyph = teTT.glyphs[5];
            if (glyph > 0) {
                GL11.glPushMatrix();
                GL11.glTranslated(0.36, 0.0, 0.0);
                GL11.glRotated(-90.0, 0.0, 1.0, 0.0);
                GL11.glRotated(180.0, 0.0, 0.0, 1.0);
                UtilsFX.renderQuadCenteredFromIcon(false, ModItems.tankGlyph.getInstalledIcon(glyph), size, 1.0F, 1.0F, 1.0F, brightness, 771, 1.0F);
                GL11.glPopMatrix();
            }

            if (world != null) {
                RenderHelper.enableStandardItemLighting();
            }

            GL11.glPopMatrix();
        }
    }

    public static ResourceLocation getFluidSheet(FluidStack liquid) {
        return liquid == null ? TextureMap.locationBlocksTexture : getFluidSheet(liquid.getFluid());
    }

    public static ResourceLocation getFluidSheet(Fluid liquid) {
        return TextureMap.locationBlocksTexture;
    }
}
