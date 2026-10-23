package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.blocks.ItemBlockJarXP;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.tiles.TileEntityJarXP;

public class Render3DItemJarXP implements IItemRenderer {
    RenderBlocks rb = new RenderBlocks();

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return item != null && item.getItem() instanceof ItemBlockJarXP;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return helper != ItemRendererHelper.EQUIPPED_BLOCK;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        if (item.getItem() instanceof ItemBlockJarXP) {
            if (type == ItemRenderType.ENTITY) {
                GL11.glTranslatef(-0.5F, -0.25F, -0.5F);
            } else if (type == ItemRenderType.EQUIPPED && data[1] instanceof EntityPlayer) {
                GL11.glTranslatef(0.0F, 0.0F, -0.5F);
            }

            TileEntityJarXP jar = new TileEntityJarXP();
            if (item.hasTagCompound()) {
                int xp = item.getTagCompound().getInteger("xp");
                if (xp > 0) {
                    jar.setXP(xp);
                }
            }

            jar.blockType = ModBlocks.jarExperience;
            jar.blockMetadata = 0;
            TileEntityRendererDispatcher.instance.renderTileEntityAt(jar, 0.0, 0.0, 0.0, 0.0F);
            GL11.glPushMatrix();
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            this.rb.useInventoryTint = true;
            this.rb.renderBlockAsItem(ModBlocks.jarExperience, item.getMetadata(), 1.0F);
            GL11.glPopMatrix();
            GL11.glEnable(32826);
        }
    }
}
