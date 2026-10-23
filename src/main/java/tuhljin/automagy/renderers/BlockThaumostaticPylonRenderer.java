package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.models.ModelThaumostaticPylon;

public class BlockThaumostaticPylonRenderer extends TileEntitySpecialRenderer {
    public static final String TEXTURE_STANDARD = "textures/models/thaumostaticPylon.png";
    public static final String TEXTURE_CONTROLLER = "textures/models/thaumostaticController.png";
    private final ModelThaumostaticPylon model = new ModelThaumostaticPylon();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        String texture = te.getBlockMetadata() == 1 ? "textures/models/thaumostaticController.png" : "textures/models/thaumostaticPylon.png";
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), texture));
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        this.model.render((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        GL11.glPopMatrix();
    }
}
