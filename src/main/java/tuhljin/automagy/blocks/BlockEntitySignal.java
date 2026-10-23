package tuhljin.automagy.blocks;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.lib.IAmplifiedRedstoneOutput;
import tuhljin.automagy.lib.NeighborNotifier;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityEntitySignal;

public class BlockEntitySignal extends ModBlockContainer implements IAmplifiedRedstoneOutput {
    protected static int glassOreID = -1;
    protected static int glassPaneOreID;
    IIcon icon2;
    IIcon icondark;
    IIcon icondark2;

    public BlockEntitySignal(String name) {
        super(name, ModBlocks.materialNiceStone, ItemBlockEntitySignal.class);
        if (glassOreID == -1) {
            glassOreID = OreDictionary.getOreID("blockGlass");
            glassPaneOreID = OreDictionary.getOreID("paneGlass");
        }
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileEntityEntitySignal();
    }

    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        String t = this.getTextureName();
        this.icon2 = ir.registerIcon(t + "2");
        this.icondark = ir.registerIcon(t + "-dark");
        this.icondark2 = ir.registerIcon(t + "2-dark");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockaccess, int x, int y, int z, int side) {
        int md = blockaccess.getBlockMetadata(x, y, z);
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        if (isScanBlockedBy(blockaccess, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir)) {
            return md > 0 ? this.icon2 : this.icondark2;
        } else {
            return md > 0 ? this.blockIcon : this.icondark;
        }
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        int md = world.getBlockMetadata(x, y, z);
        return md > 0 ? 9 : 0;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess blockaccess, int x, int y, int z, int side) {
        return this.isProvidingWeakPower(blockaccess, x, y, z, side);
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess blockaccess, int x, int y, int z, int side) {
        return blockaccess.getBlockMetadata(x, y, z);
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        TileEntityEntitySignal te = null;

        try {
            te = (TileEntityEntitySignal)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null) {
            TjUtil.dropItemsIntoWorld(te, world, x, y, z, null);
        }

        if (metadata > 0) {
            NeighborNotifier.notifyBlocksOfExtendedNeighborChange(world, x, y, z, this);
        }

        super.breakBlock(world, x, y, z, block, metadata);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            FMLNetworkHandler.openGui(player, Automagy.instance, 9, world, x, y, z);
        }

        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!world.isRemote) {
            TileEntityEntitySignal te = null;

            try {
                te = (TileEntityEntitySignal)world.getTileEntity(x, y, z);
            } catch (Exception var8) {
            }

            if (te != null) {
                te.flagRangeForRecalculation();
            }
        }
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        return this.isProvidingWeakPower(world, x, y, z, 0);
    }

    public static boolean isScanBlockedBy(IBlockAccess blockaccess, int x, int y, int z, ForgeDirection dir) {
        Block block = blockaccess.getBlock(x, y, z);
        if (block == null || block.isAir(blockaccess, x, y, z)) {
            return false;
        } else if (block.isReplaceable(blockaccess, x, y, z)) {
            return false;
        } else if (block instanceof BlockRedstoneWire) {
            return dir == ForgeDirection.UP;
        } else if (block instanceof BlockRedcrystal) {
            return ((BlockRedcrystal)block).getOrientation(blockaccess, x, y, z) == dir.ordinal();
        } else {
            Item item = Item.getItemFromBlock(block);
            if (item != null) {
                int[] oids = OreDictionary.getOreIDs(new ItemStack(item));

                for (int oid : oids) {
                    if (oid == glassOreID || oid == glassPaneOreID) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    @Override
    public int getRedstoneSignalStrength(IBlockAccess blockaccess, int x, int y, int z, int intoDirection, boolean allowAmplifiedPower) {
        int strength = this.isProvidingWeakPower(blockaccess, x, y, z, intoDirection);
        if (allowAmplifiedPower && strength == 15) {
            TileEntityEntitySignal te = (TileEntityEntitySignal)blockaccess.getTileEntity(x, y, z);
            strength += te.powerStability;
        }

        return strength;
    }

    @Override
    public boolean isRedstoneWire() {
        return false;
    }
}
