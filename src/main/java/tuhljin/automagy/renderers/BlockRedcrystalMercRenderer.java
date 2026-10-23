package tuhljin.automagy.renderers;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.tiles.TileEntityRedcrystalMerc;

public class BlockRedcrystalMercRenderer extends TileEntitySpecialRenderer {
    private final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation("Automagy".toLowerCase(), "textures/models/merccrystal.obj"));
    public static final String OBJ_MODEL_MERC = "textures/models/merccrystal.obj";
    public static final String TEXTURE_MERC = "textures/models/merccrystal.png";
    public static final String TEXTURE_MERC_ACTIVE = "textures/models/merccrystalActive.png";

    private void adjustRotateViaMeta(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        GL11.glPushMatrix();
        GL11.glRotatef(meta * -90, 0.0F, 0.0F, 1.0F);
        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        TileEntityRedcrystalMerc ter = (TileEntityRedcrystalMerc)te;
        if (ter.orientation != -1) {
            Block block = te.getBlockType();
            boolean active = te.getBlockMetadata() != 0;
            String texture = active ? "textures/models/merccrystalActive.png" : "textures/models/merccrystal.png";
            GL11.glPushMatrix();
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glTranslated(x + 0.5, y, z + 0.5);
            GL11.glScaled(0.5, 0.5, 0.5);
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), texture));
            if (ter.connectN) {
                this.model.renderPart("adjunctA_PlaneA");
            }

            if (ter.connectS) {
                this.model.renderPart("adjunctC_PlaneC");
            }

            if (ter.connectE) {
                this.model.renderPart("adjunctB_PlaneB");
            }

            if (ter.connectW) {
                this.model.renderPart("adjunctD_PlaneD");
            }

            if (te.getWorld() == null) {
                this.model.renderOnly("CrystalsMain", "Slab");
            } else {
                this.model.renderPart("Slab");
                float rotationAngle = 1.0F + (float)ter.clientRenderRotationHelper / TileEntityRedcrystalMerc.rotationSpeedFactor * 360.0F;
                GL11.glRotatef(rotationAngle, 0.0F, 1.0F, 0.0F);
                GL11.glTranslated(0.0, ter.clientRenderFloatingDistance, 0.0);
                this.model.renderPart("CrystalsMain");
            }

            GL11.glPopMatrix();
        }
    }
}
