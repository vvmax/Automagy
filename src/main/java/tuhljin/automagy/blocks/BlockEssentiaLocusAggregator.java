package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import tuhljin.automagy.renderers.IBlockGlowOverlay;
import tuhljin.automagy.renderers.RenderBlockGlowOverlay;

public class BlockEssentiaLocusAggregator extends ModBlockSlab implements IBlockGlowOverlay {
    IIcon iconTop;
    IIcon iconSide;
    IIcon iconTopOverlay;
    IIcon iconSideOverlay;

    public BlockEssentiaLocusAggregator(String name) {
        super(name, ModBlocks.materialNiceStone, null);
        this.setTextureName("lapis_block");
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.iconTop = ir.registerIcon("Automagy:locus_aggregator_top");
        this.iconSide = ir.registerIcon("Automagy:locus_aggregator_side");
        this.iconTopOverlay = ir.registerIcon("Automagy:locus_aggregator_top_overlay");
        this.iconSideOverlay = ir.registerIcon("Automagy:locus_aggregator_side_overlay");
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        switch (side) {
            case 0:
                return this.blockIcon;
            case 1:
                return this.iconTop;
            default:
                return this.iconSide;
        }
    }

    @Override
    public int getRenderType() {
        return RenderBlockGlowOverlay.renderID;
    }

    @Override
    public IIcon getOverlayTexture(IBlockAccess world, int x, int y, int z, int side) {
        return this.getOverlayInvTexture(side, 0);
    }

    @Override
    public IIcon getOverlayInvTexture(int side, int meta) {
        switch (side) {
            case 0:
                return null;
            case 1:
                return this.iconTopOverlay;
            default:
                return this.iconSideOverlay;
        }
    }
}
