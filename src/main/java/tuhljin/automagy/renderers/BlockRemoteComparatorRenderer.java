package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.renderers.tile.TilePedestalRenderer;
import thaumcraft.common.tiles.TilePedestal;
import tuhljin.automagy.tiles.TileEntityRemoteComparator;

public class BlockRemoteComparatorRenderer extends TileEntitySpecialRenderer {
    private final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation("Automagy".toLowerCase(), "textures/models/comparator.obj"));
    public static final String OBJ_MODEL = "textures/models/comparator.obj";
    public static final String TEXTURE_STANDARD = "textures/models/comparator.png";
    public static final String TEXTURE_READING = "textures/models/comparatorReading.png";
    public static final String TEXTURE_SIGNALING = "textures/models/comparatorSignaling.png";
    private TilePedestalRenderer floatingItemDisplayRenderer = new TilePedestalRenderer();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
        TileEntityRemoteComparator ter = (TileEntityRemoteComparator)te;
        ItemStack stack = ter.getFloatingDisplayItem();
        String texture;
        if (stack == null) {
            texture = "textures/models/comparator.png";
        } else if (ter.isRedstoneSignalBeingSent()) {
            texture = "textures/models/comparatorSignaling.png";
        } else {
            texture = "textures/models/comparatorReading.png";
        }

        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glScaled(0.5, 0.5, 0.5);
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
        this.model.renderAll();
        GL11.glPopMatrix();
        if (stack != null) {
            this.renderFloatingItem(te.getWorld(), stack, x, y, z, scale);
        }
    }

    private void renderFloatingItem(World world, ItemStack stack, double x, double y, double z, float scale) {
        FakePedestal fakePedestal = new FakePedestal(world, stack);
        this.floatingItemDisplayRenderer.renderTileEntityAt((TilePedestal)fakePedestal, x, y - 0.8, z, scale);
    }
}
