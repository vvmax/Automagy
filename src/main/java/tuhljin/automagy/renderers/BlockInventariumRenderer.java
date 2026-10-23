package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.renderers.models.ModelBrain;
import tuhljin.automagy.tiles.TileEntityInventarium;

public class BlockInventariumRenderer extends TileEntitySpecialRenderer {
    public static final String TEXTURE_BRAIN_ORDER = "textures/models/brainOrder.png";
    private final ModelBrain brainModel = new ModelBrain();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        TileEntityInventarium teI = (TileEntityInventarium)te;
        GL11.glPushMatrix();
        if (teI.maxMemoryUsers > 0) {
            char bright = 240;
            float brightX = bright % 65536;
            float brightY = bright / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightX, brightY);
        }

        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/brainOrder.png"));
        GL11.glTranslatef(0.0F, -0.9F, 0.0F);
        GL11.glScalef(0.4F, 0.4F, 0.4F);
        if (te.getWorld() != null) {
            float rotationAngle = 1.0F + (float)teI.clientRenderRotationHelper / TileEntityInventarium.rotationSpeedFactor * 360.0F;
            GL11.glRotatef(rotationAngle, 0.0F, 1.0F, 0.0F);
            GL11.glTranslated(0.0, teI.clientRenderFloatingDistance, 0.0);
        }

        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.95F);
        this.brainModel.render();
        GL11.glPopMatrix();
    }
}
