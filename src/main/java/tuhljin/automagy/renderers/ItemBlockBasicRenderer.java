package tuhljin.automagy.renderers;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.renderers.block.BlockRenderer;

public class ItemBlockBasicRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
    protected final Class<? extends TileEntity> tileEntityClass;
    protected final int renderID;

    public ItemBlockBasicRenderer(Class<? extends TileEntity> tileEntityClass, int renderID) {
        this.tileEntityClass = tileEntityClass;
        this.renderID = renderID;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        TileEntity te = null;

        try {
            te = this.tileEntityClass.newInstance();
        } catch (Exception var7) {
        }

        if (te != null) {
            GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            te.blockMetadata = metadata;
            TileEntityRendererDispatcher.instance.renderTileEntityAt(te, 0.0, 0.0, 0.0, 0.0F);
            GL11.glEnable(32826);
        }
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        return false;
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
