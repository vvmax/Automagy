package tuhljin.automagy.blocks;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.blocks.fluid.BlockFillableByBucket;
import tuhljin.automagy.lib.References;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityBoiler;

public class BlockBoiler extends BlockFillableByBucket {
    public BlockBoiler(String name) {
        super(name, Material.iron, false);
        this.setStepSound(Block.soundTypeMetal);
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setTextureName("thaumcraft:metalbase");
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityBoiler();
    }

    @Override
    public int getRenderType() {
        return References.renderBlockBoiler;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te != null && te instanceof TileEntityBoiler && ((TileEntityBoiler)te).isBurning()) {
            float f = x + 0.5F;
            float f1 = y + 0.2F + rand.nextFloat() * 5.0F / 16.0F;
            float f2 = z + 0.5F;
            float f3 = 0.52F;
            float f4 = rand.nextFloat() * 0.5F - 0.25F;
            world.spawnParticle("smoke", f - f3, f1, f2 + f4, 0.0, 0.0, 0.0);
            world.spawnParticle("flame", f - f3, f1, f2 + f4, 0.0, 0.0, 0.0);
            world.spawnParticle("smoke", f + f3, f1, f2 + f4, 0.0, 0.0, 0.0);
            world.spawnParticle("flame", f + f3, f1, f2 + f4, 0.0, 0.0, 0.0);
            world.spawnParticle("smoke", f + f4, f1, f2 - f3, 0.0, 0.0, 0.0);
            world.spawnParticle("flame", f + f4, f1, f2 - f3, 0.0, 0.0, 0.0);
            world.spawnParticle("smoke", f + f4, f1, f2 + f3, 0.0, 0.0, 0.0);
            world.spawnParticle("flame", f + f4, f1, f2 + f3, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public int getLightValue(IBlockAccess blockaccess, int x, int y, int z) {
        TileEntity te = blockaccess.getTileEntity(x, y, z);
        return te != null && te instanceof TileEntityBoiler && ((TileEntityBoiler)te).isBurning() ? 12 : super.getLightValue(blockaccess, x, y, z);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
        TileEntityBoiler te = null;

        try {
            te = (TileEntityBoiler)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null) {
            TjUtil.dropItemsIntoWorld(te, world, x, y, z, this);
        }

        super.breakBlock(world, x, y, z, block, p_149749_6_);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!super.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ) && !world.isRemote) {
            FMLNetworkHandler.openGui(player, Automagy.instance, 15, world, x, y, z);
        }

        return true;
    }
}
