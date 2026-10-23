package tuhljin.automagy.renderers;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.blocks.BlockHourglass;

public class Render3DItemHourglass implements IItemRenderer {
    private final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation("Automagy".toLowerCase(), "textures/models/hourglass.obj"));

    @Override
    public boolean handleRenderType(ItemStack stack, ItemRenderType type) {
        return Block.getBlockFromItem(stack.getItem()) instanceof BlockHourglass;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION || helper == ItemRendererHelper.BLOCK_3D;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        boolean isMagic = item.getUnlocalizedName().equals("tile.blockHourglassMagic");
        String texture = isMagic ? "textures/models/hourglass-gold.png" : "textures/models/hourglass.png";
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
                GL11.glScaled(0.25, 0.25, 0.25);
                GL11.glTranslated(2.5, 2.0, 0.2);
                GL11.glRotated(180.0, -0.2, 1.0, -0.1);
                break;
            case INVENTORY:
                GL11.glScaled(5.5, 5.5, 5.5);
                GL11.glTranslated(1.5, 1.5, 0.0);
                GL11.glRotated(165.0, 1.0, 0.0, 0.0);
                GL11.glRotated(90.0, 0.0, 1.0, 0.0);
                break;
            case ENTITY:
                GL11.glScaled(0.5, 0.5, 0.5);
                GL11.glTranslated(0.0, 0.7, 0.0);
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), texture));
        this.model.renderOnly("Right_Pillar_group", "group_Bottom", "group_Left_Pillar", "group_Top");
        if (!isMagic) {
            this.model.renderOnly("group_Left_Turner", "group_Right_Turner");
        } else {
            GL11.glColor4f(0.5F, 0.5F, 0.5F, 0.9F);
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/hourglass-redstone.png"));
            this.model.renderOnly("group_Redstone");
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/hourglass-sand.png"));
        this.model.renderOnly("sandbtm0");
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/hourglass-glass.png"));
        this.model.renderOnly("Group_glass");
        GL11.glPopMatrix();
    }
}
