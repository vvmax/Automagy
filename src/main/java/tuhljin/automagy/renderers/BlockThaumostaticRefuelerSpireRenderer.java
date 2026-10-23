package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.armor.ItemHoverHarness;
import tuhljin.automagy.models.ModelThaumostaticSpire;
import tuhljin.automagy.tiles.TileEntityThaumostaticRefuelerSpire;

public class BlockThaumostaticRefuelerSpireRenderer extends TileEntitySpecialRenderer {
    public static final String TEXTURE_GOLEMWORKBENCH = "textures/models/thaumostaticSpire.png";
    private final ModelThaumostaticSpire model = new ModelThaumostaticSpire();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/thaumostaticSpire.png"));
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        boolean isInWorld = te.getWorld() != null;
        if (!isInWorld) {
            GL11.glScalef(0.85F, 0.85F, 0.85F);
            GL11.glTranslatef(0.0F, 0.42F, 0.0F);
        }

        this.model.render((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        if (isInWorld) {
            TileEntityThaumostaticRefuelerSpire teSpire = (TileEntityThaumostaticRefuelerSpire)te;
            if (teSpire.isRefueling()) {
                GL11.glPushMatrix();
                GL11.glTranslatef(0.0F, -0.55F, 0.0F);
                EntityPlayer player = Minecraft.getMinecraft().thePlayer;
                GL11.glRotatef(player.rotationYawHead, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-player.rotationPitch, 1.0F, 0.0F, 0.0F);
                UtilsFX.renderQuadCenteredFromIcon(
                    false, ((ItemHoverHarness)ConfigItems.itemHoverHarness).iconLightningRing, 2.5F, 1.0F, 1.0F, 1.0F, 230, 1, 1.0F
                );
                GL11.glPopMatrix();
            }
        }

        GL11.glPopMatrix();
    }
}
