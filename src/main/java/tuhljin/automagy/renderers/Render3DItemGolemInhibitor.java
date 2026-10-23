package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.renderers.models.ModelBrain;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.models.ModelCrystalBrainPedestal;

public class Render3DItemGolemInhibitor implements IItemRenderer {
    private final ModelCrystalBrainPedestal model = new ModelCrystalBrainPedestal();
    private final ModelBrain brainModel = new ModelBrain();

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return Item.getItemFromBlock(ModBlocks.golemInhibitor) == item.getItem();
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION || helper == ItemRendererHelper.BLOCK_3D;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();
        switch (type) {
            case EQUIPPED_FIRST_PERSON:
                GL11.glTranslated(0.5, 0.2, 0.0);
                GL11.glRotatef(210.0F, 0.0F, 0.0F, 1.0F);
                break;
            case EQUIPPED:
                GL11.glTranslated(0.5, 0.2, 0.0);
                GL11.glRotatef(210.0F, 0.0F, 0.0F, 1.0F);
                break;
            case INVENTORY:
                GL11.glScaled(15.0, 15.0, 15.0);
                GL11.glTranslated(0.55, 0.95, 0.0);
                GL11.glRotated(100.0, 0.0, 1.0, 0.0);
                break;
            case ENTITY:
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                GL11.glScaled(1.35, 1.35, 1.35);
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/golemInhibitor.png"));
        this.model.render((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/brainEntropy.png"));
        GL11.glTranslatef(0.0F, -0.9F, 0.0F);
        GL11.glScalef(0.2F, 0.2F, 0.2F);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.95F);
        this.brainModel.render();
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }
}
