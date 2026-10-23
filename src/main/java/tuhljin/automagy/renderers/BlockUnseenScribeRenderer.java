package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.common.blocks.BlockTable;
import thaumcraft.common.config.ConfigBlocks;
import tuhljin.automagy.models.ModelScribeTable;
import tuhljin.automagy.tiles.TileEntityUnseenScribe;

public class BlockUnseenScribeRenderer extends TileEntitySpecialRenderer {
    public static final String TEXTURE_SCRIBETABLE = "textures/models/scribeTable.png";
    private final ModelScribeTable model = new ModelScribeTable();
    private final ModelBook modelBook = new ModelBook();
    public static final ResourceLocation modelBookTexture = new ResourceLocation("textures/entity/enchanting_table_book.png");

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        TileEntityUnseenScribe teUS = (TileEntityUnseenScribe)te;
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/scribeTable.png"));
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

        this.model.render((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        this.model.renderInkwell();
        GL11.glPushMatrix();
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        double quillR = teUS.quillR;
        GL11.glRotated(quillR, 1.0, 0.0, 0.0);
        GL11.glRotated(quillR, 0.0, 0.0, 1.0);
        GL11.glRotated(quillR, 0.0, 1.0, 0.0);
        GL11.glTranslated(-0.17F + teUS.quillHoriz, -0.4F + teUS.quillY, -0.15F);
        GL11.glRotatef(15.0F, 0.0F, 1.0F, 0.0F);
        IIcon icon = ((BlockTable)ConfigBlocks.blockTable).iconQuill;
        float f1 = icon.getMaxU();
        float f2 = icon.getMinV();
        float f3 = icon.getMinU();
        float f4 = icon.getMaxV();
        Tessellator tessellator = Tessellator.instance;
        GL11.glScalef(0.5F, 0.5F, 0.5F);

        try {
            this.field_147501_a.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            ItemRenderer.renderItemIn2D(tessellator, f1, f2, f3, f4, icon.getIconWidth(), icon.getIconHeight(), 0.025F);
        } catch (Exception var19) {
        }

        GL11.glPopMatrix();
        this.bindTexture(modelBookTexture);
        GL11.glEnable(2884);
        GL11.glTranslated(0.0, 0.4 + teUS.bookY, -0.15);
        GL11.glScalef(0.8F, 0.8F, 0.8F);
        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(80.0F, 0.0F, 0.0F, 1.0F);
        this.modelBook.render((Entity)null, 50.0F, 1.0F, teUS.page, 1.1F, 0.0F, 0.0625F);
        GL11.glPopMatrix();
    }
}
