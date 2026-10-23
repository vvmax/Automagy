package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import tuhljin.automagy.lib.NeighborNotifier;
import tuhljin.automagy.tiles.TileEntityTorchInversion;

public class BlockTorchInversion extends ModBlockTorch {
    private boolean isLit;
    private static Map overloadDetect = new HashMap();

    public BlockTorchInversion(String name, boolean lit) {
        super(name, lit);
        this.isLit = lit;
        if (lit) {
            this.setLightLevel(0.5F);
        }
    }

    private boolean handleOverloadDetection(World world, int x, int y, int z, boolean lit) {
        if (!overloadDetect.containsKey(world)) {
            overloadDetect.put(world, new ArrayList());
        }

        List list = (List)overloadDetect.get(world);
        if (lit) {
            list.add(new BlockTorchInversion.Toggle(x, y, z, world.getTotalWorldTime()));
        }

        int l = 0;

        for (int i1 = 0; i1 < list.size(); i1++) {
            BlockTorchInversion.Toggle toggle = (BlockTorchInversion.Toggle)list.get(i1);
            if (toggle.x == x && toggle.y == y && toggle.z == z) {
                if (++l >= 30) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return this.isLit ? new TileEntityTorchInversion() : null;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess blockaccess, int x, int y, int z, int side) {
        if (this.isLit) {
            TileEntity te = blockaccess.getTileEntity(x, y, z);
            if (te instanceof TileEntityTorchInversion) {
                return ((TileEntityTorchInversion)te).getRedstoneSignalStrength();
            }
        }

        return 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess blockaccess, int x, int y, int z, int side) {
        return side == 0 ? this.isProvidingWeakPower(blockaccess, x, y, z, side) : 0;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        boolean removed = this.func_150108_b(world, x, y, z, block);
        if (!removed) {
            world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
        if (this.isLit) {
            NeighborNotifier.notifyBlocksOfExtendedNeighborChange(world, x, y, z, this);
        }

        super.breakBlock(world, x, y, z, block, p_149749_6_);
    }

    @Override
    public int tickRate(World world) {
        return 2;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        List list = (List)overloadDetect.get(world);

        while (list != null && !list.isEmpty() && world.getTotalWorldTime() - ((BlockTorchInversion.Toggle)list.get(0)).time > 60L) {
            list.remove(0);
        }

        if (this.isLit) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityTorchInversion) {
                TileEntityTorchInversion teTI = (TileEntityTorchInversion)te;
                int prev = teTI.getRedstoneSignalStrength();
                int strength = this.getPowerIntoAttachedBlock(world, x, y, z);
                strength = 15 - strength;
                if (strength != prev && this.handleOverloadDetection(world, x, y, z, true)) {
                    strength = 0;
                    world.playSoundEffect(x + 0.5F, y + 0.5F, z + 0.5F, "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

                    for (int l = 0; l < 5; l++) {
                        double d0 = x + rand.nextDouble() * 0.6 + 0.2;
                        double d1 = y + rand.nextDouble() * 0.6 + 0.2;
                        double d2 = z + rand.nextDouble() * 0.6 + 0.2;
                        world.spawnParticle("smoke", d0, d1, d2, 0.0, 0.0, 0.0);
                    }
                }

                if (strength == 0) {
                    world.setBlock(x, y, z, ModBlocks.torchInversion_off, world.getBlockMetadata(x, y, z), 3);
                } else if (strength != prev) {
                    teTI.setRedstoneSignalStrength(strength);
                    NeighborNotifier.notifyBlocksOfExtendedNeighborChange(world, x, y, z, this);
                }
            }
        } else if (this.getPowerIntoAttachedBlock(world, x, y, z) < 15 && !this.handleOverloadDetection(world, x, y, z, false)) {
            world.setBlock(x, y, z, ModBlocks.torchInversion_on, world.getBlockMetadata(x, y, z), 3);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        if (this.isLit) {
            int l = world.getBlockMetadata(x, y, z);
            double d0 = x + 0.5F + (rand.nextFloat() - 0.5F) * 0.2;
            double d1 = y + 0.7F + (rand.nextFloat() - 0.5F) * 0.2;
            double d2 = z + 0.5F + (rand.nextFloat() - 0.5F) * 0.2;
            double d3 = 0.22F;
            double d4 = 0.27F;
            if (l == 1) {
                world.spawnParticle("reddust", d0 - d4, d1 + d3, d2, 0.0, 0.0, 0.0);
            } else if (l == 2) {
                world.spawnParticle("reddust", d0 + d4, d1 + d3, d2, 0.0, 0.0, 0.0);
            } else if (l == 3) {
                world.spawnParticle("reddust", d0, d1 + d3, d2 - d4, 0.0, 0.0, 0.0);
            } else if (l == 4) {
                world.spawnParticle("reddust", d0, d1 + d3, d2 + d4, 0.0, 0.0, 0.0);
            } else {
                world.spawnParticle("reddust", d0, d1, d2, 0.0, 0.0, 0.0);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Item getItem(World world, int x, int y, int z) {
        return Item.getItemFromBlock(ModBlocks.torchInversion_on);
    }

    @Override
    public boolean isAssociatedBlock(Block block) {
        return block == ModBlocks.torchInversion_off || block == ModBlocks.torchInversion_on;
    }

    static class Toggle {
        int x;
        int y;
        int z;
        long time;
        private static final String __OBFID = "CL_00000299";

        public Toggle(int x, int y, int z, long time) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.time = time;
        }
    }
}
