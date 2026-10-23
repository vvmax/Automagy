package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.blocks.ItemBlockRedcrystal;

public class Render3DItemRedcrystal implements IItemRenderer {
    private final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation("Automagy".toLowerCase(), "textures/models/redcrystal.obj"));

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return item.getItem() instanceof ItemBlockRedcrystal;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION || helper == ItemRendererHelper.BLOCK_3D;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        String texture;
        if (item.getUnlocalizedName().equals("tile.blockRedcrystalAmp")) {
            texture = "textures/models/redcrystalAmp.png";
        } else if (item.getUnlocalizedName().equals("tile.blockRedcrystalDim")) {
            texture = "textures/models/redcrystalDim.png";
        } else if (item.getUnlocalizedName().equals("tile.blockRedcrystalDense")) {
            texture = "textures/models/redcrystalDense.png";
        } else if (item.getUnlocalizedName().equals("tile.blockRedcrystalRes")) {
            texture = "textures/models/redcrystalRes.png";
        } else {
            texture = "textures/models/redcrystalStandard.png";
        }

        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        switch (type) {
            case EQUIPPED_FIRST_PERSON:
                GL11.glScaled(0.85, 0.85, 0.85);
                GL11.glTranslated(1.0, 0.0, 0.5);
                GL11.glRotated(5.0, 0.0, 1.0, 0.0);
                break;
            case EQUIPPED:
                GL11.glScaled(0.5, 0.5, 0.5);
                GL11.glTranslated(1.5, 0.6, 0.4);
                GL11.glRotated(180.0, 0.0, 5.0, -0.5);
                break;
            case INVENTORY:
                GL11.glScaled(12.0, 12.0, 12.0);
                GL11.glTranslated(0.7, 1.25, 0.0);
                GL11.glRotated(180.0, 1.2, 0.0, 1.0);
                break;
            case ENTITY:
                GL11.glScaled(1.2, 1.2, 1.2);
                GL11.glTranslated(0.0, -0.4, 0.0);
                break;
            default:
                GL11.glScaled(0.5, 0.5, 0.5);
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), texture));
        this.model.renderOnly("primary_Plane001");
        GL11.glPopMatrix();
    }
}
