package tuhljin.automagy.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.lib.References;
import tuhljin.automagy.tiles.TileEntityTenaciousChest;

public class BlockTenaciousChest extends BlockChestBase {
    public BlockTenaciousChest(String name) {
        super(name);
    }

    @Override
    public int getRenderType() {
        return References.renderBlockTenaciousChestRI;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityTenaciousChest();
    }
}
