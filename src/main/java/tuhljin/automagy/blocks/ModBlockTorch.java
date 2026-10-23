package tuhljin.automagy.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class ModBlockTorch extends BlockTorch implements ITileEntityProvider {
    public ModBlockTorch(String name, boolean displayInCreativeTab) {
        this.isBlockContainer = true;
        this.setUnlocalizedName(name);
        this.setTextureName("Automagy:" + name);
        if (!displayInCreativeTab) {
            this.setCreativeTab(null);
        } else if (ModBlocks.tab != null) {
            this.setCreativeTab(ModBlocks.tab);
        }

        GameRegistry.registerBlock(this, name);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
        super.breakBlock(world, x, y, z, block, par6);
        world.removeTileEntity(x, y, z);
    }

    @Override
    public boolean onBlockEventReceived(World world, int x, int y, int z, int eventID, int eventArg) {
        super.onBlockEventReceived(world, x, y, z, eventID, eventArg);
        TileEntity tileentity = world.getTileEntity(x, y, z);
        return tileentity != null ? tileentity.receiveClientEvent(eventID, eventArg) : false;
    }

    public int getPowerIntoAttachedBlock(World world, int x, int y, int z) {
        int l = world.getBlockMetadata(x, y, z);
        return l == 5
            ? world.getIndirectPowerLevelTo(x, y - 1, z, 0)
            : (
                l == 3
                    ? world.getIndirectPowerLevelTo(x, y, z - 1, 2)
                    : (
                        l == 4
                            ? world.getIndirectPowerLevelTo(x, y, z + 1, 3)
                            : (l == 1 ? world.getIndirectPowerLevelTo(x - 1, y, z, 4) : (l == 2 ? world.getIndirectPowerLevelTo(x + 1, y, z, 5) : 0))
                    )
            );
    }
}
