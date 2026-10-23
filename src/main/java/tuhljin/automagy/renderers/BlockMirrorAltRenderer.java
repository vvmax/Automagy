package tuhljin.automagy.renderers;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.tile.TileMirrorRenderer;
import tuhljin.automagy.blocks.ItemBlockMirrorMultiDest;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.tiles.TileEntityMirrorInput;
import tuhljin.automagy.tiles.TileEntityMirrorMultiDest;

public class BlockMirrorAltRenderer extends TileMirrorRenderer {
    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float f) {
        Block block = te.getBlockType();
        ForgeDirection dir = ForgeDirection.getOrientation(te.getBlockMetadata() % 6);
        boolean linked = false;
        float instability = 0.0F;
        TileEntityMirrorInput teInputMirror = null;
        if (block == ModBlocks.mirrorMultiDest) {
            linked = ((TileEntityMirrorMultiDest)te).linkY != -1;
        } else {
            teInputMirror = (TileEntityMirrorInput)te;
            linked = teInputMirror.linked;
        }

        label54: {
            int b = block.getMixedBrightnessForBlock(te.getWorld(), te.xCoord, te.yCoord, te.zCoord);
            if (linked) {
                double var10002 = te.xCoord + 0.5;
                double var10003 = te.yCoord + 0.5;
                double var10004 = te.zCoord;
                if (UtilsFX.isVisibleTo(1.5F, FMLClientHandler.instance().getClient().thePlayer, var10002, var10003, var10004 + 0.5)) {
                    GL11.glPushMatrix();
                    switch (dir) {
                        case DOWN:
                        default:
                            this.drawPlaneYPos(te, x, y, z, f);
                            break;
                        case UP:
                            this.drawPlaneYNeg(te, x, y, z, f);
                            break;
                        case NORTH:
                            this.drawPlaneZPos(te, x, y, z, f);
                            break;
                        case SOUTH:
                            this.drawPlaneZNeg(te, x, y, z, f);
                            break;
                        case WEST:
                            this.drawPlaneXPos(te, x, y, z, f);
                            break;
                        case EAST:
                            this.drawPlaneXNeg(te, x, y, z, f);
                    }

                    GL11.glPopMatrix();
                    GL11.glPushMatrix();
                    if (teInputMirror == null) {
                        this.translateFromOrientation((float)x, (float)y, (float)z, dir.getOpposite().ordinal(), -0.02F + instability + 1.0F);
                        UtilsFX.renderQuadFromIcon(true, ItemBlockMirrorMultiDest.paneTrans, 1.0F, 1.0F, 1.0F, 1.0F, b, 771, 1.0F);
                    } else {
                        this.translateFromOrientation((float)x, (float)y, (float)z, dir.ordinal(), 0.02F + instability);
                        UtilsFX.renderQuadFromTexture("textures/blocks/mirrorpanetrans.png", 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, b, 771, 1.0F);
                    }

                    GL11.glPopMatrix();
                    break label54;
                }
            }

            GL11.glPushMatrix();
            if (teInputMirror == null) {
                this.translateFromOrientation((float)x, (float)y, (float)z, dir.getOpposite().ordinal(), -0.02F + instability + 1.0F);
                UtilsFX.renderQuadFromIcon(true, ItemBlockMirrorMultiDest.pane, 1.0F, 1.0F, 1.0F, 1.0F, b, 771, 1.0F);
            } else {
                this.translateFromOrientation((float)x, (float)y, (float)z, dir.ordinal(), 0.02F + instability);
                UtilsFX.renderQuadFromTexture("textures/blocks/mirrorpane.png", 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, b, 771, 1.0F);
            }

            GL11.glPopMatrix();
        }

        GL11.glPushMatrix();
        this.translateFromOrientation((float)x, (float)y, (float)z, dir.ordinal(), 0.0F);
        IIcon icon;
        if (teInputMirror == null) {
            icon = ModBlocks.mirrorMultiDest.icon;
        } else {
            icon = ((TileEntityMirrorInput)te).getEyeCount() > 0 ? ModBlocks.mirrorAlt.iconSocketed : ModBlocks.mirrorAlt.icon;
        }

        float f1 = icon.getMaxU();
        float f2 = icon.getMinV();
        float f3 = icon.getMinU();
        float f4 = icon.getMaxV();
        Tessellator tessellator = Tessellator.instance;
        this.field_147501_a.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        ItemRenderer.renderItemIn2D(tessellator, f1, f2, f3, f4, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
        GL11.glPopMatrix();
    }

    private void translateFromOrientation(float x, float y, float z, int orientation, float off) {
        if (orientation == 0) {
            GL11.glTranslatef(x, y + 1.0F, z + 1.0F);
            GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
        } else if (orientation == 1) {
            GL11.glTranslatef(x, y, z);
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
        } else if (orientation == 2) {
            GL11.glTranslatef(x, y, z + 1.0F);
        } else if (orientation == 3) {
            GL11.glTranslatef(x + 1.0F, y, z);
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        } else if (orientation == 4) {
            GL11.glTranslatef(x + 1.0F, y, z + 1.0F);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        } else if (orientation == 5) {
            GL11.glTranslatef(x, y, z);
            GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        }

        GL11.glTranslatef(0.0F, 0.0F, -off);
    }
}
