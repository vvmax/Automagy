package tuhljin.automagy.lib.events;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.IGoggles;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;
import tuhljin.automagy.tiles.IAspectContainerLarge;

public class AutomagyRenderEventHandler {
    public static float tagscale = 0.0F;
    public static boolean isRenderingAspects = false;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(
        priority = EventPriority.HIGH
    )
    public void blockHighlight(DrawBlockHighlightEvent event) {
        MovingObjectPosition target = event.target;
        if (this.shouldShowAspectsToPlayer(event.player)) {
            TileEntity te = event.player.worldObj.getTileEntity(target.blockX, target.blockY, target.blockZ);
            if (te != null
                && te instanceof IAspectContainerLarge
                && ((IAspectContainerLarge)te).getAspectsReal() != null
                && ((IAspectContainerLarge)te).getAspectsReal().size() > 0) {
                boolean spaceAbove = event.player.worldObj.isAirBlock(target.blockX, target.blockY + 1, target.blockZ);
                float shift = ((IAspectContainerLarge)te).getAspectVerticalShift(spaceAbove);
                if (tagscale < 0.3F) {
                    tagscale = tagscale + (0.031F - tagscale / 10.0F);
                }

                this.drawTagsOnContainer(
                    target.blockX,
                    target.blockY + shift,
                    target.blockZ,
                    ((IAspectContainerLarge)te).getAspectsReal(),
                    220,
                    spaceAbove ? ForgeDirection.UP : ForgeDirection.getOrientation(event.target.sideHit),
                    event.partialTicks
                );
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void renderLast(RenderWorldLastEvent event) {
        if (tagscale > 0.0F) {
            tagscale -= 0.005F;
        }

        if (isRenderingAspects) {
            isRenderingAspects = false;
        }
    }

    public boolean shouldShowAspectsToPlayer(EntityPlayer player) {
        ItemStack stack = player.inventory.armorItemInSlot(3);
        return stack != null && stack.getItem() instanceof IGoggles ? ((IGoggles)stack.getItem()).showIngamePopups(stack, player) : false;
    }

    public void drawTagsOnContainer(double x, double y, double z, AspectList tags, int bright, ForgeDirection dir, float partialTicks) {
        isRenderingAspects = true;
        if (Minecraft.getMinecraft().renderViewEntity instanceof EntityPlayer && tags != null && tags.size() > 0) {
            EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().renderViewEntity;
            double iPX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
            double iPY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
            double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
            int e = 0;
            int rowsize = Math.min((int)Math.ceil(tags.size() / 5.0), 11);
            if (rowsize < 5) {
                rowsize = 5;
            }

            int current = 0;
            float shifty = 0.0F;
            int left = tags.size();

            for (Aspect tag : tags.getAspects()) {
                int div = Math.min(left, rowsize);
                if (current >= rowsize) {
                    current = 0;
                    shifty -= tagscale * 1.05F;
                    left -= rowsize;
                    if (left < rowsize) {
                        div = left % rowsize;
                    }
                }

                float shift = (current - div / 2.0F + 0.5F) * tagscale * 4.0F;
                shift *= tagscale;
                Color color = new Color(tag.getColor());
                GL11.glPushMatrix();
                GL11.glDisable(2929);
                GL11.glTranslated(
                    -iPX + x + 0.5 + tagscale * 2.0F * dir.offsetX,
                    -iPY + y - shifty + 0.5 + tagscale * 2.0F * dir.offsetY,
                    -iPZ + z + 0.5 + tagscale * 2.0F * dir.offsetZ
                );
                float xd = (float)(iPX - (x + 0.5));
                float zd = (float)(iPZ - (z + 0.5));
                float rotYaw = (float)(Math.atan2(xd, zd) * 180.0 / Math.PI);
                GL11.glRotatef(rotYaw + 180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glTranslated(shift, 0.0, 0.0);
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                GL11.glScalef(tagscale, tagscale, tagscale);
                if (!Thaumcraft.proxy.playerKnowledge.hasDiscoveredAspect(player.getCommandSenderName(), tag)) {
                    UtilsFX.renderQuadCenteredFromTexture(
                        "textures/aspects/_unknown.png", 1.0F, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, bright, 771, 0.75F
                    );
                    new Color(11184810);
                } else {
                    UtilsFX.renderQuadCenteredFromTexture(
                        tag.getImage(), 1.0F, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, bright, 771, 0.75F
                    );
                }

                if (tags.getAmount(tag) >= 0) {
                    String am = "" + tags.getAmount(tag);
                    GL11.glScalef(0.04F, 0.04F, 0.04F);
                    GL11.glTranslated(0.0, 6.0, -0.1);
                    int sw = Minecraft.getMinecraft().fontRendererObj.getStringWidth(am);
                    GL11.glEnable(3042);
                    Minecraft.getMinecraft().fontRendererObj.drawString(am, 14 - sw, 1, 1118481);
                    GL11.glTranslated(0.0, 0.0, -0.1);
                    Minecraft.getMinecraft().fontRendererObj.drawString(am, 13 - sw, 0, 16777215);
                }

                GL11.glEnable(2929);
                GL11.glPopMatrix();
                current++;
            }
        }
    }
}
