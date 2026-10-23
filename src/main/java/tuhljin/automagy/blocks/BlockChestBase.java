package tuhljin.automagy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.IChest;

public abstract class BlockChestBase extends ModBlockContainer {
    public final int guiID;

    public BlockChestBase(String name, int guiID) {
        super(name, Material.wood);
        this.guiID = guiID;
        this.setHardness(2.5F);
        this.setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
    }

    public BlockChestBase(String name) {
        this(name, 16);
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
    public abstract int getRenderType();

    @Override
    public abstract TileEntity createTileEntity(World var1, int var2);

    public void registerIcons(IIconRegister ir) {
        this.blockIcon = ir.registerIcon("thaumcraft:woodplain");
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te != null && te instanceof IChest) {
                this.openChest(world, x, y, z, player);
            }
        }

        return true;
    }

    public void openChest(World world, int x, int y, int z, EntityPlayer player) {
        player.openGui(Automagy.instance, this.guiID, world, x, y, z);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int l = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 2.5) & 3;
        world.setBlockMetadataWithNotify(x, y, z, l, 2);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te != null && te instanceof IChest) {
            TjUtil.dropItemsIntoWorld((IChest)te, world, x, y, z, null);
        }

        super.breakBlock(world, x, y, z, block, par6);
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        return Container.calcRedstoneFromInventory((IChest)world.getTileEntity(x, y, z));
    }
}
