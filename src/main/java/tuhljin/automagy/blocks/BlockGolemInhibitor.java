package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityGolemInhibitor;

public class BlockGolemInhibitor extends ModBlockSpecialRender {
    public long nextNoise = 0L;

    public BlockGolemInhibitor(String name) {
        super(name, Material.iron);
        this.setStepSound(Block.soundTypeMetal);
        this.setBlockBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.85F, 0.8F);
        this.setHardness(5.0F);
        this.setResistance(10.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileEntityGolemInhibitor();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int l = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 2.5) & 3;
        world.setBlockMetadataWithNotify(x, y, z, l, 2);
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return ThaumcraftExtension.blockIsFetter(world, x, y - 1, z, false);
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        return ThaumcraftExtension.blockIsFetter(world, x, y - 1, z, false);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!this.canBlockStay(world, x, y, z)) {
            this.dropBlockAsItem(world, x, y, z, 0, 0);
            world.setBlockToAir(x, y, z);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        if (ThaumcraftExtension.blockIsFetter(world, x, y - 1, z, true)) {
            if (rand.nextInt(6) == 0) {
                Thaumcraft.proxy.blockRunes(world, x, y, z, 1.1F, 0.51F, 1.56F, 7, 0.002F);
            }

            if (rand.nextInt(6) == 0) {
                Thaumcraft.proxy
                    .wispFX2(
                        world,
                        x + 0.25F + rand.nextFloat() * 0.5F,
                        y + 0.7F + rand.nextFloat() * 0.2F,
                        z + 0.25F + rand.nextFloat() * 0.5F,
                        0.1F,
                        5,
                        false,
                        false,
                        0.003F
                    );
            }

            if (rand.nextBoolean()) {
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

                Thaumcraft.proxy.sparkle(x2, y + 0.1F, z2, 1.0F, 8, -0.02F);
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime >= this.nextNoise) {
                this.nextNoise = currentTime + 25000L + rand.nextInt(20000);
                world.playSound(x + 0.5, y + 0.5, z + 0.5, "thaumcraft:brain", 0.05F, world.rand.nextFloat() * 0.5F + 0.2F, false);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (ThaumcraftExtension.playerHasWandEquipped(player)) {
            if (!world.isRemote) {
                TileEntityGolemInhibitor te = null;

                try {
                    te = (TileEntityGolemInhibitor)world.getTileEntity(x, y, z);
                } catch (Exception var12) {
                }

                if (te != null) {
                    if (te.toggleMatchOmitsGolem()) {
                        TjUtil.sendChatToPlayer(player, "Automagy.chat.golemInhibitor.omitMatch");
                    } else {
                        TjUtil.sendChatToPlayer(player, "Automagy.chat.golemInhibitor.exclusiveMatch");
                    }

                    world.playSoundAtEntity(player, "thaumcraft:key", 1.0F, 1.0F);
                }
            }

            return true;
        } else {
            return false;
        }
    }
}
