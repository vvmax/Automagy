package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.blocks.BlockHourglassMagic;
import tuhljin.automagy.tiles.TileEntityHourglass;

public class BlockHourglassRenderer extends TileEntitySpecialRenderer {
    public static final String OBJ_MODEL = "textures/models/hourglass.obj";
    public static final String TEXTURE_STANDARD = "textures/models/hourglass.png";
    public static final String TEXTURE_MAGIC = "textures/models/hourglass-gold.png";
    public static final String TEXTURE_GLASS = "textures/models/hourglass-glass.png";
    public static final String TEXTURE_SAND = "textures/models/hourglass-sand.png";
    public static final String TEXTURE_REDSTONE = "textures/models/hourglass-redstone.png";
    protected final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation("Automagy".toLowerCase(), "textures/models/hourglass.obj"));

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        TileEntityHourglass teh = (TileEntityHourglass)te;
        boolean isMagic = te.getBlockType() instanceof BlockHourglassMagic;
        String texture = isMagic ? "textures/models/hourglass-gold.png" : "textures/models/hourglass.png";
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glTranslated(x + 0.5, y + 0.2, z + 0.5);
        GL11.glScaled(0.2, 0.2, 0.2);
        switch (te.getBlockMetadata()) {
            case 0:
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            case 1:
            default:
                break;
            case 2:
                GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case 3:
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), texture));
        this.model.renderOnly("Right_Pillar_group", "group_Bottom", "group_Left_Pillar", "group_Top");
        if (!isMagic) {
            this.model.renderOnly("group_Left_Turner", "group_Right_Turner");
        } else {
            if (teh.getRedstoneSignalStrength() > 0) {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.9F);
            } else {
                GL11.glColor4f(0.5F, 0.5F, 0.5F, 0.9F);
            }

            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/hourglass-redstone.png"));
            this.model.renderOnly("group_Redstone");
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        long time = System.nanoTime();
        float rotation = (float)(time - teh.timeDidFlip) / 1.0E9F / 0.25F;
        if (rotation < 1.0) {
            GL11.glRotated(180.0, 1.0, 0.0, 0.0);
            GL11.glRotated(180.0 * rotation, 0.0, 0.0, 1.0);
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/hourglass-sand.png"));
        this.renderSandState(teh, rotation < 1.0);
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("Automagy".toLowerCase(), "textures/models/hourglass-glass.png"));
        this.model.renderOnly("Group_glass");
        GL11.glPopMatrix();
    }

    private void renderSandState(TileEntityHourglass teh, boolean rotating) {
        float percent = teh.percentageComplete();
        float sectionSize = 16.666666F;
        if (percent <= sectionSize) {
            this.model.renderOnly("sandtop1");
            if (!rotating) {
                this.model.renderOnly("sandbtm6");
            }
        } else if (percent <= sectionSize * 2.0F) {
            this.model.renderOnly("sandtop2");
            this.model.renderOnly("sandbtm5");
        } else if (percent <= sectionSize * 3.0F) {
            this.model.renderOnly("sandtop3");
            this.model.renderOnly("sandbtm4");
        } else if (percent <= sectionSize * 4.0F) {
            this.model.renderOnly("sandtop4");
            this.model.renderOnly("sandbtm3");
        } else if (percent <= sectionSize * 5.0F) {
            this.model.renderOnly("sandtop5");
            this.model.renderOnly("sandbtm2");
        } else if (percent < 100.0F) {
            this.model.renderOnly("sandtop6");
            this.model.renderOnly("sandbtm1");
        } else {
            this.model.renderOnly("sandbtm0");
        }
    }
}
