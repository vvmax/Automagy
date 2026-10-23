package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityAmnesiacStone;

public class BlockAmnesiac extends ModBlockContainer {
    public static int XP_ORB_SUPPRESS_FOLLOW_TIME = 80;
    public static int DRAIN_CLICK_NORMAL = 1;
    public static int DRAIN_CLICK_SNEAK = 5;
    public static int DRAIN_WALK = 30;
    public static int XP_PER_BOTTLE_DEFAULT = 8;

    public BlockAmnesiac(String name) {
        super(name, ModBlocks.materialNiceStone);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityAmnesiacStone();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            this.drainExperience(world, x, y, z, player, player.isSneaking() ? DRAIN_CLICK_SNEAK : DRAIN_CLICK_NORMAL, false);
            if (player.isSneaking()) {
                player.attackEntityFrom(DamageSource.magic, 0.01F);
            }
        }

        return true;
    }

    public boolean drainExperience(World world, int x, int y, int z, EntityPlayer player, int levels, boolean toTop) {
        int prevXP = TjUtil.getPlayerXP(player);
        player.addExperienceLevel(levels * -1);
        int diff = prevXP - TjUtil.getPlayerXP(player);
        if (diff > 0) {
            double x2;
            double y2;
            double z2;
            if (toTop) {
                x2 = x + 0.5;
                y2 = y + 1.25;
                z2 = z + 0.5;
            } else {
                x2 = (x + player.posX + 0.5) / 2.0;
                y2 = (y + player.posY + player.height / 2.0) / 2.0;
                z2 = (z + player.posZ + 0.5) / 2.0;
                if (player.posY > y2 && !world.isAirBlock((int)x2, (int)y2, (int)z2) && world.isAirBlock((int)x2, (int)y2 + 1, (int)z2)) {
                    y2 = (int)y2 + 1.25;
                }
            }

            TjUtil.spawnSuppressedExperienceIntoWorld(diff, XP_ORB_SUPPRESS_FOLLOW_TIME, world, x2, y2, z2);
            world.playSoundAtEntity(player, "random.fizz", 1.0F, 2.0F + world.rand.nextFloat() * 0.4F);
            return true;
        } else {
            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        if (rand.nextInt(8) == 0) {
            Thaumcraft.proxy.blockRunes(world, x, y, z, 1.1F, 0.51F, 1.56F, 50, 5.0E-4F);
        }
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        return ((TileEntityAmnesiacStone)world.getTileEntity(x, y, z)).getComparatorStrength();
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            ((TileEntityAmnesiacStone)te).isSharingStorage = null;
        }
    }
}
