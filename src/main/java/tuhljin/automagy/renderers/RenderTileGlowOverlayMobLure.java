package tuhljin.automagy.renderers;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderTileGlowOverlayMobLure extends TileEntitySpecialRenderer {
    public int renderID = -1;
    public Block block;
    public static RenderBlocks renderBlocks = new RenderBlocks();

    public RenderTileGlowOverlayMobLure(Block block, int renderID) {
        this.block = block;
        this.renderID = renderID;
    }

    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        Tessellator tessellator = Tessellator.instance;
        IBlockGlowOverlay blockOverlay = block instanceof IBlockGlowOverlay ? (IBlockGlowOverlay)block : null;
        block.setBlockBoundsForItemRender();
        renderer.setRenderBoundsFromBlock(block);
        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        renderer.renderFaceYNeg(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 0, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderer.renderFaceYPos(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 1, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderer.renderFaceZNeg(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 2, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderer.renderFaceZPos(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 3, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderer.renderFaceXNeg(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 4, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderer.renderFaceXPos(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 5, metadata));
        tessellator.draw();
        if (blockOverlay != null) {
            float lastBrightX = OpenGlHelper.lastBrightnessX;
            float lastBrightY = OpenGlHelper.lastBrightnessY;
            char bright = 240;
            float brightX = bright % 65536;
            float brightY = bright / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightX, brightY);
            Integer color = null;
            if (block instanceof IBlockGlowOverlayWithRecolor) {
                color = ((IBlockGlowOverlayWithRecolor)block).getOverlayInvColor(metadata);
            }

            float alpha = 1.0F;
            if (block instanceof IBlockGlowOverlayWithAlpha) {
                alpha = ((IBlockGlowOverlayWithAlpha)block).getOverlayInvAlpha(metadata);
            }

            if (color != null) {
                float f5 = (color >> 16 & 0xFF) / 255.0F;
                float f6 = (color >> 8 & 0xFF) / 255.0F;
                float f7 = (color & 0xFF) / 255.0F;
                GL11.glColor4f(f5, f6, f7, alpha);
            } else if (alpha != 1.0F) {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
            }

            IIcon face = blockOverlay.getOverlayInvTexture(0, metadata);
            if (face != null) {
                tessellator.startDrawingQuads();
                tessellator.setNormal(0.0F, -1.0F, 0.0F);
                renderer.renderFaceYNeg(block, 0.0, 0.0, 0.0, face);
                tessellator.draw();
            }

            face = blockOverlay.getOverlayInvTexture(1, metadata);
            if (face != null) {
                tessellator.startDrawingQuads();
                tessellator.setNormal(0.0F, 1.0F, 0.0F);
                renderer.renderFaceYPos(block, 0.0, 0.0, 0.0, face);
                tessellator.draw();
            }

            face = blockOverlay.getOverlayInvTexture(2, metadata);
            if (face != null) {
                tessellator.startDrawingQuads();
                tessellator.setNormal(0.0F, 0.0F, -1.0F);
                renderer.renderFaceZNeg(block, 0.0, 0.0, 0.0, face);
                tessellator.draw();
            }

            face = blockOverlay.getOverlayInvTexture(3, metadata);
            if (face != null) {
                tessellator.startDrawingQuads();
                tessellator.setNormal(0.0F, 0.0F, 1.0F);
                renderer.renderFaceZPos(block, 0.0, 0.0, 0.0, face);
                tessellator.draw();
            }

            face = blockOverlay.getOverlayInvTexture(4, metadata);
            if (face != null) {
                tessellator.startDrawingQuads();
                tessellator.setNormal(-1.0F, 0.0F, 0.0F);
                renderer.renderFaceXNeg(block, 0.0, 0.0, 0.0, face);
                tessellator.draw();
            }

            face = blockOverlay.getOverlayInvTexture(5, metadata);
            if (face != null) {
                tessellator.startDrawingQuads();
                tessellator.setNormal(1.0F, 0.0F, 0.0F);
                renderer.renderFaceXPos(block, 0.0, 0.0, 0.0, face);
                tessellator.draw();
            }

            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightX, lastBrightY);
        }

        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
        World world = te.getWorld();
        if (world != null) {
            this.bindTexture(TextureMap.locationBlocksTexture);
            renderCube(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5, this.block, null);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            Tessellator.instance.startDrawingQuads();
            IBlockGlowOverlay icn = (IBlockGlowOverlay)this.block;
            Integer color = null;
            if (this.block instanceof IBlockGlowOverlayWithRecolor) {
                color = ((IBlockGlowOverlayWithRecolor)this.block).getOverlayColor(world, te.xCoord, te.yCoord, te.zCoord);
            }

            float alpha = 1.0F;
            if (this.block instanceof IBlockGlowOverlayWithAlpha) {
                alpha = ((IBlockGlowOverlayWithAlpha)this.block).getOverlayAlpha(world, te.xCoord, te.yCoord, te.zCoord);
            }

            if (color == null) {
                if (alpha == 1.0F) {
                    Tessellator.instance.setColorOpaque(255, 255, 255);
                } else {
                    Tessellator.instance.setColorRGBA_F(255.0F, 255.0F, 255.0F, alpha);
                }
            } else {
                float f5 = (color >> 16 & 0xFF) / 255.0F;
                float f6 = (color >> 8 & 0xFF) / 255.0F;
                float f7 = (color & 0xFF) / 255.0F;
                Tessellator.instance.setColorRGBA_F(f5, f6, f7, alpha);
            }

            Tessellator t = Tessellator.instance;
            t.setBrightness(240);
            IIcon face = icn.getOverlayTexture(world, te.xCoord, te.yCoord, te.zCoord, 0);
            if (face != null) {
                t.setNormal(0.0F, -1.0F, 0.0F);
                renderBlocks.renderFaceYNeg(this.block, 0.0, 0.0, 0.0, face);
            }

            face = icn.getOverlayTexture(world, te.xCoord, te.yCoord, te.zCoord, 1);
            if (face != null) {
                t.setNormal(0.0F, 1.0F, 0.0F);
                renderBlocks.renderFaceYPos(this.block, 0.0, 0.0, 0.0, face);
            }

            face = icn.getOverlayTexture(world, te.xCoord, te.yCoord, te.zCoord, 2);
            if (face != null) {
                t.setNormal(0.0F, 0.0F, -1.0F);
                renderBlocks.renderFaceZNeg(this.block, 0.0, 0.0, 0.0, face);
            }

            face = icn.getOverlayTexture(world, te.xCoord, te.yCoord, te.zCoord, 3);
            if (face != null) {
                t.setNormal(0.0F, 0.0F, 1.0F);
                renderBlocks.renderFaceZPos(this.block, 0.0, 0.0, 0.0, face);
            }

            face = icn.getOverlayTexture(world, te.xCoord, te.yCoord, te.zCoord, 4);
            if (face != null) {
                t.setNormal(-1.0F, 0.0F, 0.0F);
                renderBlocks.renderFaceXNeg(this.block, 0.0, 0.0, 0.0, face);
            }

            face = icn.getOverlayTexture(world, te.xCoord, te.yCoord, te.zCoord, 5);
            if (face != null) {
                t.setNormal(1.0F, 0.0F, 0.0F);
                renderBlocks.renderFaceXPos(this.block, 0.0, 0.0, 0.0, face);
            }

            t.setBrightness(this.block.getMixedBrightnessForBlock(world, te.xCoord, te.yCoord, te.zCoord));
            t.draw();
        } else {
            this.renderInventoryBlock(this.block, te.blockMetadata, this.renderID, RenderBlocks.getInstance());
        }

        GL11.glPopMatrix();
    }

    public static void renderCube(double x1, double y1, double z1, double x2, double y2, double z2, Block block, IIcon overrideTexture) {
        renderCube(x1, y1, z1, x2, y2, z2, block, overrideTexture, 0);
    }

    public static void renderCube(double x1, double y1, double z1, double x2, double y2, double z2, Block block, IIcon overrideTexture, int meta) {
        GL11.glPushMatrix();
        Tessellator t = Tessellator.instance;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        renderBlocks.setRenderBounds(x1, y1, z1, x2, y2, z2);
        t.startDrawingQuads();
        IIcon useTexture = overrideTexture != null ? overrideTexture : block.getIcon(0, meta);
        t.setNormal(0.0F, -1.0F, 0.0F);
        renderBlocks.renderFaceYNeg(block, 0.0, 0.0, 0.0, useTexture);
        useTexture = overrideTexture != null ? overrideTexture : block.getIcon(1, meta);
        t.setNormal(0.0F, 1.0F, 0.0F);
        renderBlocks.renderFaceYPos(block, 0.0, 0.0, 0.0, useTexture);
        useTexture = overrideTexture != null ? overrideTexture : block.getIcon(2, meta);
        t.setNormal(0.0F, 0.0F, -1.0F);
        renderBlocks.renderFaceZNeg(block, 0.0, 0.0, 0.0, useTexture);
        useTexture = overrideTexture != null ? overrideTexture : block.getIcon(3, meta);
        t.setNormal(0.0F, 0.0F, 1.0F);
        renderBlocks.renderFaceZPos(block, 0.0, 0.0, 0.0, useTexture);
        useTexture = overrideTexture != null ? overrideTexture : block.getIcon(4, meta);
        t.setNormal(-1.0F, 0.0F, 0.0F);
        renderBlocks.renderFaceXNeg(block, 0.0, 0.0, 0.0, useTexture);
        useTexture = overrideTexture != null ? overrideTexture : block.getIcon(5, meta);
        t.setNormal(1.0F, 0.0F, 0.0F);
        renderBlocks.renderFaceXPos(block, 0.0, 0.0, 0.0, useTexture);
        t.draw();
        GL11.glPopMatrix();
    }
}
