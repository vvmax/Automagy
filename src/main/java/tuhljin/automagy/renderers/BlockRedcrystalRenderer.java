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
import tuhljin.automagy.blocks.BlockRedcrystalAmp;
import tuhljin.automagy.blocks.BlockRedcrystalDense;
import tuhljin.automagy.blocks.BlockRedcrystalDim;
import tuhljin.automagy.blocks.BlockRedcrystalRes;
import tuhljin.automagy.tiles.TileEntityRedcrystal;

public class BlockRedcrystalRenderer extends TileEntitySpecialRenderer {
    private final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation("Automagy".toLowerCase(), "textures/models/redcrystal.obj"));
    public static final String OBJ_MODEL = "textures/models/redcrystal.obj";
    public static final String TEXTURE_STANDARD = "textures/models/redcrystalStandard.png";
    public static final String TEXTURE_ACTIVE = "textures/models/redcrystalActive.png";
    public static final String TEXTURE_AMP = "textures/models/redcrystalAmp.png";
    public static final String TEXTURE_AMP_ACTIVE = "textures/models/redcrystalAmpActive.png";
    public static final String TEXTURE_DIM = "textures/models/redcrystalDim.png";
    public static final String TEXTURE_DIM_ACTIVE = "textures/models/redcrystalDimActive.png";
    public static final String TEXTURE_DENSE = "textures/models/redcrystalDense.png";
    public static final String TEXTURE_DENSE_ACTIVE = "textures/models/redcrystalDenseActive.png";
    public static final String TEXTURE_RES = "textures/models/redcrystalRes.png";
    public static final String TEXTURE_RES_ACTIVE = "textures/models/redcrystalResActive.png";

    private void adjustRotateViaMeta(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        GL11.glPushMatrix();
        GL11.glRotatef(meta * -90, 0.0F, 0.0F, 1.0F);
        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        TileEntityRedcrystal ter = (TileEntityRedcrystal)te;
        if (ter.orientation != -1) {
            Block block = te.getBlockType();
            boolean larger = false;
            String texture;
            if (block instanceof BlockRedcrystalAmp) {
                texture = te.getBlockMetadata() == 0 ? "textures/models/redcrystalAmp.png" : "textures/models/redcrystalAmpActive.png";
                larger = true;
            } else if (block instanceof BlockRedcrystalDim) {
                texture = te.getBlockMetadata() == 0 ? "textures/models/redcrystalDim.png" : "textures/models/redcrystalDimActive.png";
                larger = true;
            } else if (block instanceof BlockRedcrystalDense) {
                texture = te.getBlockMetadata() == 0 ? "textures/models/redcrystalDense.png" : "textures/models/redcrystalDenseActive.png";
                larger = true;
            } else if (block instanceof BlockRedcrystalRes) {
                texture = te.getBlockMetadata() == 0 ? "textures/models/redcrystalRes.png" : "textures/models/redcrystalResActive.png";
                larger = true;
            } else {
                texture = te.getBlockMetadata() == 0 ? "textures/models/redcrystalStandard.png" : "textures/models/redcrystalActive.png";
            }

            GL11.glPushMatrix();
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glTranslated(x + 0.5, y, z + 0.5);
            this.translateFromOrientation((float)x, (float)y, (float)z, ter.orientation);
            if (larger) {
                GL11.glScaled(0.4, 0.4, 0.4);
            } else {
                GL11.glScaled(0.25, 0.25, 0.25);
            }

            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), texture));
            this.model.renderPart("primary_Plane001");
            if (ter.orientation == 0) {
                if (ter.connectN) {
                    this.model.renderPart("adjunctC_PlaneC");
                }

                if (ter.connectS) {
                    this.model.renderPart("adjunctA_PlaneA");
                }
            } else {
                if (ter.connectN) {
                    this.model.renderPart("adjunctA_PlaneA");
                }

                if (ter.connectS) {
                    this.model.renderPart("adjunctC_PlaneC");
                }
            }

            if (ter.connectE) {
                this.model.renderPart("adjunctB_PlaneB");
            }

            if (ter.connectW) {
                this.model.renderPart("adjunctD_PlaneD");
            }

            GL11.glPopMatrix();
        }
    }

    private void translateFromOrientation(float x, float y, float z, int orientation) {
        if (orientation == 0) {
            GL11.glTranslatef(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        } else if (orientation != 1) {
            if (orientation == 2) {
                GL11.glTranslatef(0.0F, 0.5F, 0.5F);
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
            } else if (orientation == 3) {
                GL11.glTranslatef(0.0F, 0.5F, -0.5F);
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            } else if (orientation == 4) {
                GL11.glTranslatef(0.5F, 0.5F, 0.0F);
                GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            } else if (orientation == 5) {
                GL11.glTranslatef(-0.5F, 0.5F, 0.0F);
                GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
            }
        }
    }
}
