package tuhljin.automagy.renderers;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.codechicken.lib.math.MathHelper;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.models.ModelScribePointer;
import tuhljin.automagy.tiles.TileEntityScribePointer;
import tuhljin.automagy.tiles.TileEntityUnseenScribe;

public class BlockScribePointerRenderer extends TileEntitySpecialRenderer {
    public static final String TEXTURE_SCRIBETABLE = "textures/models/scribePointer.png";
    public static int renderPass = 0;
    public static int TICKS_PER_BLOCK_VIEW = 60;
    public static float ROTATION_SPEED = 0.3F;
    private final ModelScribePointer model = new ModelScribePointer();
    private ItemStack eyeStack = new ItemStack(ModItems.crystalEye);

    public BlockScribePointerRenderer() {
        this.eyeStack.stackTagCompound = new NBTTagCompound();
        this.eyeStack.stackTagCompound.setInteger("lookX", 1);
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        TileEntityScribePointer teP = (TileEntityScribePointer)te;
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        boolean inWorld = te.getWorld() != null;
        if (!inWorld) {
            GL11.glTranslatef(0.0F, -0.5F, 0.0F);
            GL11.glScalef(1.2F, 1.2F, 1.2F);
        } else {
            switch (te.getBlockMetadata()) {
                case 0:
                    GL11.glTranslatef(-0.35F, 0.75F, -0.35F);
                    break;
                case 1:
                    GL11.glTranslatef(-0.35F, 0.75F, 0.35F);
                    break;
                case 2:
                    GL11.glTranslatef(0.35F, 0.75F, 0.35F);
                    break;
                case 3:
                    GL11.glTranslatef(0.35F, 0.75F, -0.35F);
            }

            GL11.glScalef(0.5F, 0.5F, 0.5F);
        }

        if (!inWorld) {
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/scribePointer.png"));
            this.model.renderBase((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.9F);
            this.model.renderCrystalBall();
        } else if (renderPass == 0) {
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/scribePointer.png"));
            this.model.renderBase((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
            this.renderItem(teP);
        } else {
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/scribePointer.png"));
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            float interpolationFactor = teP.ticks % 100.0F / 100.0F;
            float transp = TjUtil.dualInterpolate(0.9F, 0.7F, interpolationFactor);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, transp);
            this.model.renderCrystalBall();
        }

        GL11.glPopMatrix();
    }

    private void renderItem(TileEntityScribePointer teP) {
        int count = teP.getEyeCount();
        if (count > 0) {
            if (teP.displayChangedTick == -1 || teP.displayChangedTick != teP.ticks && teP.ticks % TICKS_PER_BLOCK_VIEW == 0) {
                teP.displayStack = this.getNextDisplayTarget(teP);
                teP.displayChangedTick = teP.ticks;
            }

            if (teP.displayStack != null) {
                GL11.glPushMatrix();
                float interpolationFactorRotate = teP.ticks * ROTATION_SPEED % 100.0F / 100.0F;
                GL11.glTranslatef(0.0F, 1.3F, 0.0F);
                GL11.glRotatef(MathHelper.interpolate(0.0F, 360.0F, interpolationFactorRotate), 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                boolean item3D = this.itemDisplaysIn3D(teP.displayStack);
                if (!item3D) {
                    GL11.glScalef(0.8F, 0.8F, 0.8F);
                }

                EntityItem entityitem = new EntityItem(teP.getWorld(), 0.0, 0.0, 0.0, teP.displayStack);
                entityitem.hoverStart = 0.0F;
                RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0, 0.0, 0.0, 0.0F, 0.0F);
                if (!item3D && !Minecraft.isFancyGraphicsEnabled()) {
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                    RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0, 0.0, 0.0, 0.0F, 0.0F);
                }

                GL11.glPopMatrix();
            } else {
                IIcon icon = this.eyeStack.getItem().getIcon(this.eyeStack, 0);
                if (icon != null) {
                    GL11.glPushMatrix();
                    float interpolationFactorRotatex = teP.ticks * ROTATION_SPEED % 100.0F / 100.0F;
                    GL11.glTranslatef(0.0F, 1.3F, 0.0F);
                    GL11.glRotatef(MathHelper.interpolate(0.0F, 360.0F, interpolationFactorRotatex), 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                    Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationItemsTexture);
                    GL11.glScalef(0.4F, 0.4F, 0.4F);
                    GL11.glTranslated(-0.5, 0.0, 0.0);
                    float f = icon.getMinU();
                    float f1 = icon.getMaxU();
                    float f2 = icon.getMinV();
                    float f3 = icon.getMaxV();
                    ItemRenderer.renderItemIn2D(Tessellator.instance, f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
                    GL11.glColor3f(1.0F, 1.0F, 1.0F);
                    GL11.glPopMatrix();
                }
            }
        }
    }

    private ItemStack getNextDisplayTarget(TileEntityScribePointer teP) {
        TileEntityUnseenScribe.SelectedContainers sc = teP.getSelectedContainers();
        if (sc != null) {
            int size = sc.size();
            if (size > 0) {
                if (teP.getEyeCount() - teP.getRangeBoosters() > size && teP.ticks % (TICKS_PER_BLOCK_VIEW * 2) == 0) {
                    return null;
                }

                teP.currentDisplayIndex++;
                if (teP.currentDisplayIndex >= size) {
                    teP.currentDisplayIndex = 0;
                }

                BlockCoord pos = sc.getBlockCoord(teP.currentDisplayIndex);
                ItemStack stack = TjUtil.getStackFromBlock(teP.getWorld(), pos.x, pos.y, pos.z);
                if (stack != null) {
                    return stack;
                }
            }
        }

        return null;
    }

    private boolean itemDisplaysIn3D(ItemStack stack) {
        return stack.getItemSpriteNumber() == 0
            && stack.getItem() instanceof ItemBlock
            && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(stack.getItem()).getRenderType());
    }
}
