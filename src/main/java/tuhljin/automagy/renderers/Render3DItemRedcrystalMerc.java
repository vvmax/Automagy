package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.blocks.ModBlocks;

public class Render3DItemRedcrystalMerc implements IItemRenderer {
    private final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation("Automagy".toLowerCase(), "textures/models/merccrystal.obj"));
    private static float rotationAngle;

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return Item.getItemFromBlock(ModBlocks.redcrystalMerc) == item.getItem();
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION || helper == ItemRendererHelper.BLOCK_3D;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        String texture = "textures/models/merccrystal.png";
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        switch (type) {
            case EQUIPPED_FIRST_PERSON:
                GL11.glScaled(0.33, 0.33, 0.33);
                GL11.glTranslated(2.0, 1.0, 0.0);
                GL11.glRotated(180.0, -0.2, 1.0, -0.1);
                break;
            case EQUIPPED:
                GL11.glScaled(0.33, 0.33, 0.33);
                GL11.glTranslated(2.0, 0.2, 0.0);
                GL11.glRotated(180.0, -0.2, 1.0, -0.1);
                break;
            case INVENTORY:
                GL11.glScaled(7.0, 7.0, 7.0);
                GL11.glTranslated(1.1, 1.8, 0.0);
                GL11.glRotated(180.0, 1.0, 0.0, 0.0);
                break;
            case ENTITY:
                GL11.glScaled(0.75, 0.75, 0.75);
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), texture));
        this.model.renderAllExcept("CrystalsMain");
        GL11.glTranslated(0.0, -0.4, 0.0);
        this.model.renderOnly("CrystalsMain");
        GL11.glPopMatrix();
    }
}
