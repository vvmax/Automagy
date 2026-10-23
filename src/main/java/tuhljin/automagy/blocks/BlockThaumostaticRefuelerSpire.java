package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.ItemApi;
import thaumcraft.client.fx.bolt.FXLightningBolt;
import tuhljin.automagy.lib.References;
import tuhljin.automagy.tiles.TileEntityThaumostaticRefuelerSpire;

public class BlockThaumostaticRefuelerSpire extends ModBlockSpecialRender {
    public BlockThaumostaticRefuelerSpire(String name) {
        super(name, Material.iron, ItemBlockRarityRare.class);
        this.setStepSound(Block.soundTypeMetal);
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
        this.setTextureName("thaumcraft:thaumiumblock");
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int metadata) {
        return metadata == 1 ? null : new TileEntityThaumostaticRefuelerSpire();
    }

    @Override
    public int getRenderType() {
        return References.renderBlockThaumostaticRefueler;
    }

    @Override
    public MapColor getMapColor(int p_149728_1_) {
        return MapColor.purpleColor;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<>();
        if (metadata == 1) {
            return drops;
        } else {
            for (int i = 0; i < 9; i++) {
                ItemStack thaumiumIgnot = ItemApi.getItem("itemResource", 2);
                drops.add(thaumiumIgnot);
            }

            return drops;
        }
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockaccess, int x, int y, int z) {
        if (blockaccess.getBlockMetadata(x, y, z) == 1) {
            this.setBlockBounds(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        } else {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        if (!world.isRemote && metadata == 1) {
            Block b = world.getBlock(x, y - 1, z);
            // A relocated upper half has no material value. Only its real base may refund ingots.
            if (b == ModBlocks.thaumostaticSpire && world.getBlockMetadata(x, y - 1, z) == 0
                && world.setBlockToAir(x, y - 1, z)) {
                b.dropBlockAsItem(world, x, y - 1, z, 0, 0);
            }
        } else if (!world.isRemote && metadata == 0 && world.getBlock(x, y + 1, z) == ModBlocks.thaumostaticSpire
            && world.getBlockMetadata(x, y + 1, z) == 1) {
            world.setBlockToAir(x, y + 1, z);
        }

        super.breakBlock(world, x, y, z, block, metadata);
    }

    @Override
    public boolean canPlaceBlockAt(World p_149742_1_, int p_149742_2_, int p_149742_3_, int p_149742_4_) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        TileEntityThaumostaticRefuelerSpire te = null;

        try {
            te = (TileEntityThaumostaticRefuelerSpire)world.getTileEntity(x, y, z);
        } catch (Exception var16) {
        }

        if (te != null && te.isPowered()) {
            if (!te.isRefueling() && rand.nextInt(5) != 0) {
                return;
            }

            float x2 = x;
            float z2 = z;
            int r = rand.nextInt(4);
            switch (r) {
                case 0:
                    x2 = (float)(x2 + rand.nextFloat() * 0.9);
                    z2 = (float)(z2 + 0.125);
                    break;
                case 1:
                    x2 = (float)(x2 + rand.nextFloat() * 0.9);
                    z2 = (float)(z2 + 0.875);
                    break;
                case 2:
                    x2 = (float)(x2 + 0.125);
                    z2 = (float)(z2 + rand.nextFloat() * 0.9);
                    break;
                case 3:
                    x2 = (float)(x2 + 0.875);
                    z2 = (float)(z2 + rand.nextFloat() * 0.9);
            }

            int btmY = y - te.numPylons();
            double yD = y;
            double y2 = y - 1;

            do {
                y2 = y2 - rand.nextDouble() - rand.nextInt(1) - 1.0;
                if (y2 < btmY) {
                    y2 = btmY;
                }

                FXLightningBolt bolt = new FXLightningBolt(world, x2, yD, z2, x2, y2, z2, rand.nextLong(), 20, 2.0F, 5);
                bolt.defaultFractal();
                bolt.setType(6);
                bolt.setWidth(0.015F);
                bolt.finalizeBolt();
                yD = y2;
            } while (y2 > btmY);
        }
    }
}
