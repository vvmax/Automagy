package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.renderers.models.ModelBrain;
import tuhljin.automagy.blocks.ModBlocks;

public class Render3DItemCrystalBrainBlock implements IItemRenderer {
    private final ModelBrain brainModel = new ModelBrain();

    @Override
    public boolean handleRenderType(ItemStack stack, ItemRenderType type) {
        Item item = stack.getItem();
        return item == Item.getItemFromBlock(ModBlocks.essentiaLocus) || item == Item.getItemFromBlock(ModBlocks.inventariumCore);
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION || helper == ItemRendererHelper.BLOCK_3D;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        String texture = stack.getItem() == Item.getItemFromBlock(ModBlocks.essentiaLocus)
            ? "textures/models/brainWater.png"
            : "textures/models/brainOrder.png";
        GL11.glPushMatrix();
        switch (type) {
            case EQUIPPED_FIRST_PERSON:
                GL11.glTranslated(0.5, 0.05, -0.2);
                GL11.glRotatef(210.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(80.0F, 0.0F, 1.0F, 0.0F);
                break;
            case EQUIPPED:
                GL11.glScalef(1.3F, 1.3F, 1.3F);
                GL11.glTranslated(0.8, -0.2, 0.0);
                GL11.glRotatef(210.0F, 0.0F, 0.0F, 1.0F);
                break;
            case INVENTORY:
                GL11.glScaled(24.0, 24.0, 24.0);
                GL11.glTranslated(0.3, 0.9, 0.0);
                GL11.glRotated(-25.0, 0.0, 1.0, 0.0);
                GL11.glRotated(8.0, 1.0, 0.0, 0.0);
                break;
            case ENTITY:
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                GL11.glScaled(2.0, 2.0, 2.0);
                GL11.glTranslated(0.0, 0.45, 0.0);
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), texture));
        GL11.glTranslatef(0.0F, -0.9F, 0.0F);
        GL11.glScalef(0.4F, 0.4F, 0.4F);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.95F);
        this.brainModel.render();
        GL11.glPopMatrix();
    }
}
