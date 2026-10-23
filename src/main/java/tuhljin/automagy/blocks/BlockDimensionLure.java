package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import tuhljin.automagy.lib.References;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.renderers.IBlockGlowOverlayWithAlpha;
import tuhljin.automagy.tiles.TileEntityDimensionLure;

public class BlockDimensionLure extends ModBlockContainer implements IBlockGlowOverlayWithAlpha {
    private IIcon iconSide;
    private IIcon iconOverlay;

    public BlockDimensionLure(String name) {
        super(name, ModBlocks.materialNiceStone, ItemBlockRarityRare.class);
        this.setResistance(2000.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityDimensionLure();
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.iconSide = ir.registerIcon(this.textureName + "Side");
        this.iconOverlay = ir.registerIcon(this.textureName + "Overlay");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        return side == 1 ? this.blockIcon : this.iconSide;
    }

    @Override
    public MapColor getMapColor(int p_149728_1_) {
        return MapColor.obsidianColor;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return References.renderBlockDimensionLure;
    }

    @Override
    public IIcon getOverlayTexture(IBlockAccess world, int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        return meta == 1 && side == 1 ? this.iconOverlay : null;
    }

    @Override
    public IIcon getOverlayInvTexture(int side, int meta) {
        return side == 1 ? this.iconOverlay : null;
    }

    @Override
    public float getOverlayAlpha(IBlockAccess world, int x, int y, int z) {
        return this.getOverlayInvAlpha(world.getBlockMetadata(x, y, z));
    }

    @Override
    public float getOverlayInvAlpha(int meta) {
        if (meta != 1) {
            return 1.0F;
        } else {
            long currentTime = System.currentTimeMillis();
            float alphaState = (float)(currentTime / 40L % 100L);
            alphaState /= 100.0F;
            return TjUtil.dualInterpolate(1.0F, 0.5F, alphaState);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (ThaumcraftExtension.playerHasWandEquipped(player)) {
            if (!world.isRemote) {
                TileEntityDimensionLure te = null;

                try {
                    te = (TileEntityDimensionLure)world.getTileEntity(x, y, z);
                } catch (Exception var12) {
                }

                if (te != null && !te.isEnabled()) {
                    te.tryEnable();
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        TileEntityDimensionLure te = null;

        try {
            te = (TileEntityDimensionLure)world.getTileEntity(x, y, z);
        } catch (Exception var10) {
        }

        if (te != null && te.isEnabled()) {
            long currentTime = System.currentTimeMillis();
            if (te.lastNoise == 0L) {
                te.lastNoise = currentTime - 45000L;
            } else if (currentTime >= te.lastNoise + 12000L) {
                boolean shouldPlay;
                if (te.isConjuring()) {
                    shouldPlay = currentTime >= te.lastNoise + 17000L || rand.nextInt(5) == 0;
                } else {
                    shouldPlay = currentTime >= te.lastNoise + 50000L && rand.nextInt(10) == 0;
                }

                if (shouldPlay) {
                    world.playSound(x + 0.5, te.getMidPoint(), z + 0.5, "portal.portal", 1.0F, rand.nextFloat() * 0.3F + 0.3F, false);
                    te.lastNoise = currentTime;
                }
            }
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        TileEntityDimensionLure te = null;

        try {
            te = (TileEntityDimensionLure)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null && te.isEnabled()) {
            te.disable(false);
        }

        super.breakBlock(world, x, y, z, block, metadata);
    }
}
