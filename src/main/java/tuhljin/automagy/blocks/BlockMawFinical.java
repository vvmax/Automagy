package tuhljin.automagy.blocks;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.InventoryObjectFilter;
import tuhljin.automagy.tiles.TileEntityMawFinical;

public class BlockMawFinical extends BlockMawHungry {
    public BlockMawFinical(String name) {
        super(name);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileEntityMawFinical();
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
        TileEntityMawFinical te = null;

        try {
            te = (TileEntityMawFinical)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null) {
            TjUtil.dropItemsIntoWorld(te.filterContainingInventory, world, x, y, z, this);
        }

        super.breakBlock(world, x, y, z, block, par6);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!ThaumcraftExtension.playerHasWandEquipped(player) && !InventoryObjectFilter.isItemPopulatedFilter(player.getCurrentEquippedItem())) {
            return false;
        } else {
            if (!world.isRemote) {
                FMLNetworkHandler.openGui(player, Automagy.instance, 8, world, x, y, z);
            }

            return true;
        }
    }
}
