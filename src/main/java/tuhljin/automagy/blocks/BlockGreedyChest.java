package tuhljin.automagy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.lib.References;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.InventoryObjectFilter;
import tuhljin.automagy.tiles.TileEntityGreedyChest;

public class BlockGreedyChest extends ModBlockContainer {
    public IIcon icon;

    public BlockGreedyChest(String name) {
        super(name, Material.wood);
        this.setHardness(2.5F);
        this.setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
    }

    @Override
    public TileEntity createNewTileEntity(World w, int i) {
        return null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return References.renderBlockGreedyChestRI;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityGreedyChest();
    }

    public void registerIcons(IIconRegister ir) {
        this.icon = ir.registerIcon("thaumcraft:woodplain");
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return this.icon;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te != null && te instanceof TileEntityGreedyChest) {
                if (!ThaumcraftExtension.playerHasWandEquipped(player) && !InventoryObjectFilter.isItemPopulatedFilter(player.getCurrentEquippedItem())) {
                    player.openGui(Automagy.instance, 2, world, x, y, z);
                } else {
                    player.openGui(Automagy.instance, 3, world, x, y, z);
                }
            }
        }

        return true;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        world.markBlockForUpdate(x, y, z);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int l = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 2.5) & 3;
        world.setBlockMetadataWithNotify(x, y, z, l, 2);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
        TileEntityGreedyChest te = null;

        try {
            te = (TileEntityGreedyChest)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null) {
            TjUtil.dropItemsIntoWorld(te, world, x, y, z, null);
            TjUtil.dropItemsIntoWorld(te.filterContainingInventory, world, x, y, z, this);
        }

        super.breakBlock(world, x, y, z, block, par6);
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        return Container.calcRedstoneFromInventory((TileEntityGreedyChest)world.getTileEntity(x, y, z));
    }
}
