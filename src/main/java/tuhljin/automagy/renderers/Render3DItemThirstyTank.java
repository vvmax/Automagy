package tuhljin.automagy.renderers;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.tiles.ModTileEntity;
import tuhljin.automagy.tiles.TileEntityThirstyTank;

public class Render3DItemThirstyTank implements IItemRenderer {
    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        RenderBlocks renderblocks = (RenderBlocks)data[0];
        Block block = Block.getBlockFromItem(item.getItem());
        int meta = item.getMetadata();
        GL11.glPushMatrix();
        switch (type) {
            case EQUIPPED_FIRST_PERSON:
                GL11.glScalef(1.5F, 1.5F, 1.5F);
                GL11.glTranslated(1.2, 0.0, 0.28);
                break;
            case EQUIPPED:
                GL11.glTranslated(0.3, 0.5, 0.5);
        }

        TileEntityThirstyTank te = null;

        try {
            te = TileEntityThirstyTank.class.newInstance();
        } catch (Exception var12) {
        }

        if (te != null) {
            FluidStack fluidStack = null;
            if (item.hasTagCompound()) {
                fluidStack = FluidStack.loadFluidStackFromNBT(item.stackTagCompound);
                if (item.stackTagCompound.hasKey("Glyphs")) {
                    int[] glyphs = ModTileEntity.getIntArrayFromNbtOrDefault(item.stackTagCompound, "Glyphs", 0, 6);
                    te.setGlyphs(glyphs);
                    int capacity = 16;

                    for (int i = 0; i < glyphs.length; i++) {
                        if (glyphs[i] == 8) {
                            capacity += TileEntityThirstyTank.CAPACITY_IN_BUCKETS_PER_UPGRADE;
                        }
                    }

                    te.tank.setCapacity(1000 * capacity);
                }
            }

            te.tank.setFluid(fluidStack);
            GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            TileEntityRendererDispatcher.instance.renderTileEntityAt(te, 0.0, 0.0, 0.0, 0.0F);
            GL11.glEnable(32826);
        }

        GL11.glPopMatrix();
    }
}
