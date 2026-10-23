package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.tiles.TileEntityBoiler;

public class BlockBoilerRenderer extends TileEntitySpecialRenderer {
    private final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation("Automagy".toLowerCase(), "textures/models/boiler.obj"));
    public static final String OBJ_MODEL = "textures/models/boiler.obj";
    public static final String TEXTURE_UNLIT_EMPTY = "textures/models/boiler.png";
    public static final String TEXTURE_LIT_EMPTY = "textures/models/boiler-lit.png";
    public static final String TEXTURE_LIT_FILLED = "textures/models/boiler-lit-filled.png";
    public static final String TEXTURE_UNLIT_FILLED = "textures/models/boiler-filled.png";

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        TileEntityBoiler ter = (TileEntityBoiler)te;
        String texture;
        if (ter.isBurning()) {
            texture = ter.hasVis() ? "textures/models/boiler-lit-filled.png" : "textures/models/boiler-lit.png";
        } else {
            texture = ter.hasVis() ? "textures/models/boiler-filled.png" : "textures/models/boiler.png";
        }

        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), texture));
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
        GL11.glScaled(0.5, 0.5, 0.5);
        GL11.glRotated(45.0, 0.0, 1.0, 0.0);
        if (te.getWorld() != null) {
            RenderHelper.disableStandardItemLighting();
        }

        this.model.renderAll();
        if (te.getWorld() != null) {
            RenderHelper.enableStandardItemLighting();
        }

        GL11.glPopMatrix();
    }
}
