package tuhljin.automagy.renderers;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

public class RenderBlockGlowOverlay implements ISimpleBlockRenderingHandler {
    public static int renderID = 0;

    @Override
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

            if (color != null) {
                float f5 = (color >> 16 & 0xFF) / 255.0F;
                float f6 = (color >> 8 & 0xFF) / 255.0F;
                float f7 = (color & 0xFF) / 255.0F;
                GL11.glColor4f(f5, f6, f7, 1.0F);
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
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        renderer.renderStandardBlockWithColorMultiplier(block, x, y, z, 1.0F, 1.0F, 1.0F);
        if (block instanceof IBlockGlowOverlay) {
            IBlockGlowOverlay icn = (IBlockGlowOverlay)block;
            Tessellator.instance.setBrightness(240);
            Integer color = null;
            if (block instanceof IBlockGlowOverlayWithRecolor) {
                color = ((IBlockGlowOverlayWithRecolor)block).getOverlayColor(world, x, y, z);
            }

            if (color == null) {
                Tessellator.instance.setColorOpaque(255, 255, 255);
            } else {
                float f5 = (color >> 16 & 0xFF) / 255.0F;
                float f6 = (color >> 8 & 0xFF) / 255.0F;
                float f7 = (color & 0xFF) / 255.0F;
                Tessellator.instance.setColorRGBA_F(f5, f6, f7, 1.0F);
            }

            GL11.glPushMatrix();
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            IIcon face = icn.getOverlayTexture(world, x, y, z, 0);
            if (face != null) {
                renderer.renderFaceYNeg(block, x, y, z, face);
            }

            face = icn.getOverlayTexture(world, x, y, z, 1);
            if (face != null) {
                renderer.renderFaceYPos(block, x, y, z, face);
            }

            face = icn.getOverlayTexture(world, x, y, z, 2);
            if (face != null) {
                renderer.renderFaceZNeg(block, x, y, z, face);
            }

            face = icn.getOverlayTexture(world, x, y, z, 3);
            if (face != null) {
                renderer.renderFaceZPos(block, x, y, z, face);
            }

            face = icn.getOverlayTexture(world, x, y, z, 4);
            if (face != null) {
                renderer.renderFaceXNeg(block, x, y, z, face);
            }

            face = icn.getOverlayTexture(world, x, y, z, 5);
            if (face != null) {
                renderer.renderFaceXPos(block, x, y, z, face);
            }

            Tessellator.instance.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
            GL11.glDisable(3042);
            GL11.glPopMatrix();
        }

        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return renderID;
    }
}
