package tuhljin.automagy.blocks;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.IRemoteComparatorOverride;
import tuhljin.automagy.lib.NeighborNotifier;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityRemoteComparator;
import tuhljin.automagy.tiles.TileEntityTallyBase;

public abstract class BlockTallyBase extends ModBlockContainer implements IRemoteComparatorOverride {
    protected IIcon iconReading;
    protected IIcon iconOutputOff;
    protected IIcon iconOutputOn;

    public BlockTallyBase(String name) {
        super(name, Material.wood);
        this.setHardness(2.5F);
        this.setResistance(10.0F);
        this.setStepSound(soundTypeWood);
    }

    public void registerIcons(IIconRegister ir) {
        this.blockIcon = ir.registerIcon("Automagy:blockTally");
        this.iconOutputOff = ir.registerIcon("Automagy:blockTallyRedOff");
        this.iconOutputOn = ir.registerIcon("Automagy:blockTallyRedOn");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockaccess, int x, int y, int z, int side) {
        int md = blockaccess.getBlockMetadata(x, y, z);
        int orientation = md > 5 ? md - 6 : md;
        if (side == orientation) {
            return md > 5 ? this.iconOutputOn : this.iconOutputOff;
        } else {
            return ForgeDirection.OPPOSITES[side] == orientation ? this.iconReading : this.blockIcon;
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == 1) {
            return this.iconReading;
        } else {
            return side == 0 ? this.iconOutputOn : this.blockIcon;
        }
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float p_149660_6_, float p_149660_7_, float p_149660_8_, int p_149660_9_) {
        return side;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote) {
            boolean rcAbove = false;
            TileEntity teRC = world.getTileEntity(x, y + 1, z);
            if (teRC != null && teRC instanceof TileEntityRemoteComparator) {
                ((TileEntityRemoteComparator)teRC).setOverride(true);
                if (world.getBlockMetadata(x, y, z) != 0) {
                    world.setBlockMetadataWithNotify(x, y, z, 0, 2);
                }

                rcAbove = true;
            }

            TileEntityTallyBase te = null;

            try {
                te = (TileEntityTallyBase)world.getTileEntity(x, y, z);
            } catch (Exception var9) {
            }

            if (te != null) {
                te.findNewTarget(rcAbove);
            }
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!world.isRemote) {
            TileEntity teRC = world.getTileEntity(x, y + 1, z);
            if (teRC != null && teRC instanceof TileEntityRemoteComparator) {
                if (block instanceof BlockRemoteComparator) {
                    ((TileEntityRemoteComparator)teRC).setOverride(true);
                }

                int md = world.getBlockMetadata(x, y, z);
                if (md != 0 && md != 6) {
                    world.setBlockMetadataWithNotify(x, y, z, 0, 3);
                }
            }

            TileEntityTallyBase te = null;

            try {
                te = (TileEntityTallyBase)world.getTileEntity(x, y, z);
            } catch (Exception var9) {
            }

            if (te != null) {
                te.findNewTarget(true);
            }
        }
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess blockaccess, int x, int y, int z, int side) {
        int orientation = getOrientation(blockaccess, x, y, z);
        return side == orientation;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess blockaccess, int x, int y, int z, int side) {
        return this.isProvidingWeakPower(blockaccess, x, y, z, side);
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess blockaccess, int x, int y, int z, int side) {
        int md = blockaccess.getBlockMetadata(x, y, z);
        return md >= 6 && ForgeDirection.OPPOSITES[side] == md - 6 ? 15 : 0;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            ItemStack held = player.getCurrentEquippedItem();
            if (held == null || held.getItem() != ModItems.tallyLens) {
                FMLNetworkHandler.openGui(player, Automagy.instance, 10, world, x, y, z);
            }
        }

        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        TileEntityTallyBase te = null;

        try {
            te = (TileEntityTallyBase)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null) {
            TjUtil.dropItemsIntoWorld(te, world, x, y, z, null);
            if (metadata > 5) {
                NeighborNotifier.notifyBlocksOfExtendedNeighborChange(world, x, y, z, this);
            }
        }

        if (!world.isRemote) {
            TileEntity teRC = world.getTileEntity(x, y + 1, z);
            if (teRC != null && teRC instanceof TileEntityRemoteComparator) {
                ((TileEntityRemoteComparator)teRC).setOverride(false);
            }
        }

        super.breakBlock(world, x, y, z, block, metadata);
    }

    @Override
    public int getRemoteComparatorParticleColor(World world, int x, int y, int z, TileEntityRemoteComparator teRC) {
        TileEntityTallyBase te = null;

        try {
            te = (TileEntityTallyBase)world.getTileEntity(x, y, z);
        } catch (Exception var8) {
        }

        return te != null && te.hasValidTarget() ? 0 : -1;
    }

    @Override
    public boolean hasActiveRedstoneSignal(World world, int x, int y, int z, TileEntityRemoteComparator teRC) {
        int md = world.getBlockMetadata(x, y, z);
        return md > 5;
    }

    public static int getOrientation(IBlockAccess blockaccess, int x, int y, int z) {
        int md = blockaccess.getBlockMetadata(x, y, z);
        return md > 5 ? md - 6 : md;
    }
}
