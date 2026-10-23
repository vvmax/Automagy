package tuhljin.automagy.blocks;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.IAspectContainer;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.lib.IAmplifiedRedstoneOutput;
import tuhljin.automagy.lib.IRemoteComparatorOverride;
import tuhljin.automagy.lib.NeighborNotifier;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityRemoteComparator;
import tuhljin.automagy.tiles.TileEntityVisReader;

public class BlockVisReader extends ModBlockContainer implements IRemoteComparatorOverride, IAmplifiedRedstoneOutput {
    private IIcon[] iconSide;

    public BlockVisReader(String name) {
        super(name, ModBlocks.materialNiceStone);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.iconSide = new IIcon[6];
        this.iconSide[0] = ir.registerIcon(this.textureName + "_s");
        this.iconSide[1] = ir.registerIcon(this.textureName + "_w");
        this.iconSide[2] = ir.registerIcon(this.textureName + "_n");
        this.iconSide[3] = ir.registerIcon(this.textureName + "_e");
        this.iconSide[4] = ir.registerIcon(this.textureName + "_btm");
        this.iconSide[5] = ir.registerIcon(this.textureName + "_top");
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected String getTextureName() {
        return this.textureName + "_top";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        if (side < 2) {
            return this.iconSide[side + 4];
        } else {
            side = TjUtil.getSWNESide(side);
            side += meta;
            if (meta == 1 || meta == 3) {
                side += 2;
            }

            while (side > 3) {
                side -= 4;
            }

            return this.iconSide[side];
        }
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileEntityVisReader();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int l = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 2.5) & 3;
        world.setBlockMetadataWithNotify(x, y, z, l, 2);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote) {
            TileEntity teRC = world.getTileEntity(x, y + 1, z);
            if (teRC != null && teRC instanceof TileEntityRemoteComparator) {
                ((TileEntityRemoteComparator)teRC).setOverride(true);
            }

            try {
                TileEntityVisReader te = (TileEntityVisReader)world.getTileEntity(x, y, z);
                te.findNewTarget(true);
            } catch (Exception var7) {
            }
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        TileEntityVisReader te = null;

        try {
            te = (TileEntityVisReader)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null) {
            TjUtil.dropItemsIntoWorld(te, world, x, y, z, null);
        }

        NeighborNotifier.notifyBlocksOfExtendedNeighborChange(world, x, y, z, this);
        if (!world.isRemote) {
            TileEntity teRC = world.getTileEntity(x, y + 1, z);
            if (teRC != null && teRC instanceof TileEntityRemoteComparator) {
                ((TileEntityRemoteComparator)teRC).setOverride(false);
            }
        }

        super.breakBlock(world, x, y, z, block, metadata);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!world.isRemote) {
            if (block instanceof BlockRemoteComparator) {
                TileEntity teRC = world.getTileEntity(x, y + 1, z);
                if (teRC != null && teRC instanceof TileEntityRemoteComparator) {
                    ((TileEntityRemoteComparator)teRC).setOverride(true);
                }
            }

            TileEntityVisReader te = null;

            try {
                te = (TileEntityVisReader)world.getTileEntity(x, y, z);
            } catch (Exception var8) {
            }

            if (te != null) {
                te.findNewTarget(block instanceof BlockRemoteComparator);
            }
        }
    }

    @Override
    public int getRemoteComparatorParticleColor(World world, int x, int y, int z, TileEntityRemoteComparator teRC) {
        TileEntityVisReader te = null;

        try {
            te = (TileEntityVisReader)world.getTileEntity(x, y, z);
        } catch (Exception var8) {
        }

        return te != null && te.getTarget() instanceof IAspectContainer ? 7 : -1;
    }

    @Override
    public boolean hasActiveRedstoneSignal(World world, int x, int y, int z, TileEntityRemoteComparator teRC) {
        TileEntityVisReader te = null;

        try {
            te = (TileEntityVisReader)world.getTileEntity(x, y, z);
        } catch (Exception var8) {
        }

        return te != null && te.hasActiveRedstoneSignal();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (ThaumcraftExtension.playerHasWandEquipped(player)) {
            if (!world.isRemote) {
                FMLNetworkHandler.openGui(player, Automagy.instance, 0, world, x, y, z);
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess blockaccess, int x, int y, int z, int side) {
        return side != 1 ? this.isProvidingWeakPower(blockaccess, x, y, z, side) : 0;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess blockaccess, int x, int y, int z, int side) {
        return this.getRedstoneSignalStrength(blockaccess, x, y, z, TjUtil.getOppositeSide(side), false);
    }

    @Override
    public int getRedstoneSignalStrength(IBlockAccess blockaccess, int x, int y, int z, int intoDirection, boolean allowAmplifiedPower) {
        TileEntityVisReader te = null;

        try {
            te = (TileEntityVisReader)blockaccess.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te == null) {
            return 0;
        } else {
            int strength = te.getRedstoneSignalStrength(intoDirection);
            return !allowAmplifiedPower && strength > 15 ? 15 : strength;
        }
    }

    @Override
    public boolean isRedstoneWire() {
        return false;
    }
}
