package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import tuhljin.automagy.renderers.IBlockGlowOverlay;
import tuhljin.automagy.renderers.RenderBlockGlowOverlay;

public class BlockMetalGlow extends ModBlock implements IBlockGlowOverlay {
    IIcon iconMemoryOverlay;

    public BlockMetalGlow(String name) {
        super(name, ModBlocks.materialNiceIron, ModSubtypedItemBlock.class);
        this.setStepSound(Block.soundTypeMetal);
        this.setResistance(5.0F);
        this.setHardness(1.5F);
        this.setTextureName("Automagy:amberMemory");
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
    }

    @Override
    public int damageDropped(int d) {
        return d == 1 ? 0 : d;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.iconMemoryOverlay = ir.registerIcon(this.textureName + "Glow");
    }

    @Override
    public int getRenderType() {
        return RenderBlockGlowOverlay.renderID;
    }

    @Override
    public IIcon getOverlayTexture(IBlockAccess world, int x, int y, int z, int side) {
        return this.getOverlayInvTexture(side, world.getBlockMetadata(x, y, z));
    }

    @Override
    public IIcon getOverlayInvTexture(int side, int meta) {
        return meta == 1 ? this.iconMemoryOverlay : null;
    }
}
