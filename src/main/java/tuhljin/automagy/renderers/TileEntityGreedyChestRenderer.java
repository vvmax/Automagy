package tuhljin.automagy.renderers;

import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.tiles.TileEntityGreedyChest;

public class TileEntityGreedyChestRenderer extends TileEntitySpecialRenderer {
    public static final String TEXTURE_GREEDY = "textures/models/greedychest.png";
    public static final String TEXTURE_GREEDY_LIT = "textures/models/greedychest-litrunes.png";
    private ModelChest model = new ModelChest();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        TileEntityGreedyChest teGC = (TileEntityGreedyChest)te;
        if (teGC != null) {
            int facing = 0;
            if (teGC != null && teGC.hasWorldObj()) {
                facing = teGC.getBlockMetadata();
            }

            String texture = teGC.litRunesTime > 0 ? "textures/models/greedychest-litrunes.png" : "textures/models/greedychest.png";
            this.bindTexture(new ResourceLocation("Automagy".toLowerCase(), texture));
            GL11.glPushMatrix();
            GL11.glEnable(32826);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glTranslatef((float)x, (float)y + 1.0F, (float)z + 1.0F);
            GL11.glScalef(1.0F, -1.0F, -1.0F);
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            if (facing > 0) {
                int k = 0;
                if (facing == 1) {
                    k = 90;
                } else if (facing == 2) {
                    k = 180;
                } else if (facing == 3) {
                    k = -90;
                }

                GL11.glRotatef(k, 0.0F, 1.0F, 0.0F);
            }

            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            float lidangle = teGC.prevLidAngle + (teGC.lidAngle - teGC.prevLidAngle) * partialTick;
            lidangle = 1.0F - lidangle;
            lidangle = 1.0F - lidangle * lidangle * lidangle;
            this.model.chestLid.rotateAngleX = -(lidangle * 3.141593F / 2.0F);
            this.model.renderAll();
            GL11.glDisable(32826);
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
