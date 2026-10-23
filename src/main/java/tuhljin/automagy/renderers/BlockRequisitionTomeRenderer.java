package tuhljin.automagy.renderers;

import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.codechicken.lib.math.MathHelper;
import tuhljin.automagy.models.ModelNewPedestal;
import tuhljin.automagy.tiles.TileEntityRequisitionTome;

public class BlockRequisitionTomeRenderer extends TileEntitySpecialRenderer {
    public static final ResourceLocation texture = new ResourceLocation("Automagy".toLowerCase(), "textures/models/reqTomeStand.png");
    private final ModelNewPedestal model = new ModelNewPedestal();
    private final ModelBook modelBook = new ModelBook();
    public static final ResourceLocation modelBookTexture = new ResourceLocation("Automagy".toLowerCase(), "textures/models/reqTome.png");

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        TileEntityRequisitionTome teRT = (TileEntityRequisitionTome)te;
        float interpolationFactor = teRT.openedAmt;
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        if (te.getWorld() == null) {
            GL11.glTranslatef(0.0F, 0.07F, 0.0F);
            GL11.glScalef(0.98F, 0.98F, 0.98F);
        } else {
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
        }

        this.bindTexture(texture);
        this.model.render((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        float extraY = MathHelper.interpolate(0.0F, 0.05F, Math.min(interpolationFactor * 10.0F, 1.0F));
        GL11.glScalef(1.1F, 1.1F, 1.1F);
        GL11.glTranslated(0.0, 0.39 - (teRT.bookY + extraY), 0.0);
        this.bindTexture(modelBookTexture);
        GL11.glEnable(2884);
        float rotSide = MathHelper.interpolate(90.0F, 180.0F, interpolationFactor);
        float bookOpen = MathHelper.interpolate(0.002F, 1.1F, interpolationFactor);
        float turnUp = MathHelper.interpolate(90.0F, 65.0F, interpolationFactor);
        float pageTurn1 = 0.0F;
        float pageTurn2 = teRT.page;
        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(rotSide, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(turnUp, 0.0F, 0.0F, 1.0F);
        GL11.glTranslated(0.0, extraY, 0.0);
        if (interpolationFactor < 1.0F) {
            GL11.glTranslated(MathHelper.interpolate(-0.195F, 0.0F, interpolationFactor), 0.0, 0.0);
        }

        this.modelBook.render((Entity)null, 50.0F, pageTurn1, pageTurn2, bookOpen, 0.0F, 0.0625F);
        GL11.glPopMatrix();
    }
}
