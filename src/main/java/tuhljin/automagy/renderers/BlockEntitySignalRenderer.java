package tuhljin.automagy.renderers;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.renderers.block.BlockRenderer;
import thaumcraft.client.renderers.models.ModelBrain;
import tuhljin.automagy.blocks.ModBlocks;

public class BlockEntitySignalRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
    protected final Class<? extends TileEntity> tileEntityClass;
    protected final int renderID;
    public static IIcon icon;
    private final ModelBrain brainModel;

    public BlockEntitySignalRenderer(Class<? extends TileEntity> tileEntityClass, int renderID) {
        this.tileEntityClass = tileEntityClass;
        this.renderID = renderID;
        icon = ModBlocks.entitySignal.getIcon(0, 0);
        this.brainModel = new ModelBrain();
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        drawFaces(renderer, block, icon, icon, icon, icon, icon, icon, false);
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setBrightness(255);
        t.setNormal(0.0F, 1.0F, 0.0F);
        renderer.renderFaceYPos(block, 0.0, 0.0, 0.0, icon);
        t.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        renderer.setRenderBoundsFromBlock(block);
        renderer.renderStandardBlock(block, x, y, z);
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return this.renderID;
    }
}
