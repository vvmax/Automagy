package tuhljin.automagy.renderers;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityJarXP;

public class BlockJarXPRenderer extends TileEntitySpecialRenderer {
    private int xpColorDisplayFallback = 15990551;

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float f) {
        TileEntityJarXP jar = (TileEntityJarXP)te;
        GL11.glPushMatrix();
        GL11.glDisable(2884);
        GL11.glTranslatef((float)x + 0.5F, (float)y + 0.01F, (float)z + 0.5F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int xp = jar.getXP();
        if (xp > 0) {
            this.renderLiquid(jar, x, y, z, f);
        }

        this.bindTexture(jar.getTexture());
        GL11.glEnable(2884);
        GL11.glPopMatrix();
    }

    public void renderLiquid(TileEntityJarXP te, double x, double y, double z, float f) {
        if (this.field_147501_a.renderEngine != null) {
            GL11.glPushMatrix();
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            RenderBlocks renderBlocks = new RenderBlocks();
            GL11.glDisable(2896);
            float level = (float)te.getXP() / TileEntityJarXP.MAX_XP * 0.625F;
            Tessellator t = Tessellator.instance;
            renderBlocks.setRenderBounds(0.25, 0.0625, 0.25, 0.75, 0.0625 + level, 0.75);
            t.startDrawingQuads();
            long xpColor = te.xpColor;
            boolean teInWorld;
            if (xpColor == -1L) {
                teInWorld = false;
                World world = FMLClientHandler.instance().getClient().theWorld;
                xpColor = world.getTotalWorldTime() % 150L;
            } else {
                teInWorld = true;
            }

            xpColor /= 15L;
            float f10 = 255.0F;
            float f11 = ((float)xpColor + 0.0F) / 2.0F;
            int l = (int)((MathHelper.sin(f11 + 0.0F) + 1.0F) * 0.5F * f10);
            int i1 = (int)f10;
            int j1 = (int)((MathHelper.sin(f11 + (float) (Math.PI * 4.0 / 3.0)) + 1.0F) * 0.1F * f10);
            int k1 = l << 16 | i1 << 8 | j1;
            if (teInWorld) {
                te.lastColorDisplayed = TjUtil.colorApproachLinear(te.lastColorDisplayed, k1, 1.0);
                t.setColorRGBA_I(te.lastColorDisplayed, 180);
            } else {
                this.xpColorDisplayFallback = TjUtil.colorApproachLinear(this.xpColorDisplayFallback, k1, 1.0);
                t.setColorRGBA_I(this.xpColorDisplayFallback, 180);
            }

            int bright = 200;
            if (te.getWorld() != null) {
                bright = Math.max(200, ModBlocks.jarExperience.getMixedBrightnessForBlock(te.getWorld(), te.xCoord, te.yCoord, te.zCoord));
            }

            t.setBrightness(bright);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
            IIcon icon = ModBlocks.jarExperience.iconLiquid;
            this.field_147501_a.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            renderBlocks.renderFaceYNeg(ModBlocks.jarExperience, -0.5, 0.0, -0.5, icon);
            renderBlocks.renderFaceYPos(ModBlocks.jarExperience, -0.5, 0.0, -0.5, icon);
            renderBlocks.renderFaceZNeg(ModBlocks.jarExperience, -0.5, 0.0, -0.5, icon);
            renderBlocks.renderFaceZPos(ModBlocks.jarExperience, -0.5, 0.0, -0.5, icon);
            renderBlocks.renderFaceXNeg(ModBlocks.jarExperience, -0.5, 0.0, -0.5, icon);
            renderBlocks.renderFaceXPos(ModBlocks.jarExperience, -0.5, 0.0, -0.5, icon);
            t.draw();
            GL11.glEnable(2896);
            GL11.glPopMatrix();
            GL11.glColor3f(1.0F, 1.0F, 1.0F);
        }
    }
}
