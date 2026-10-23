package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.renderers.models.ModelBrain;
import tuhljin.automagy.models.ModelCrystalBrainPedestal;
import tuhljin.automagy.tiles.TileEntityGolemTaskmaster;

public class BlockGolemTaskmasterRenderer extends TileEntitySpecialRenderer {
    public static final String TEXTURE_STANDARD = "textures/models/golemTaskmaster.png";
    public static final String TEXTURE_BRAIN_ORDER = "textures/models/brainOrder.png";
    private final ModelCrystalBrainPedestal model = new ModelCrystalBrainPedestal();
    private final ModelBrain brainModel = new ModelBrain();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        TileEntityGolemTaskmaster teGT = (TileEntityGolemTaskmaster)te;
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/golemTaskmaster.png"));
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        switch (te.getBlockMetadata()) {
            case 0:
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                break;
            case 1:
                GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            case 2:
            default:
                break;
            case 3:
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        }

        this.model.render((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F, false);
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        this.model.shapeTip.render(0.0625F);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/brainOrder.png"));
        GL11.glTranslatef(0.0F, -0.9F, 0.0F);
        GL11.glScalef(0.2F, 0.2F, 0.2F);
        float rotationAngle = 1.0F + (float)teGT.clientRenderRotationHelper / TileEntityGolemTaskmaster.rotationSpeedFactor * 360.0F;
        GL11.glRotatef(rotationAngle, 0.0F, 1.0F, 0.0F);
        GL11.glTranslated(0.0, -teGT.clientRenderFloatingDistance, 0.0);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(2.0F, 2.0F, 2.0F, 0.9F);
        this.brainModel.render();
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }
}
