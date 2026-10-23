package tuhljin.automagy.lib;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class NeighborNotifier {
    public static void notifyBlocksOfExtendedNeighborChange(World world, int x, int y, int z, Block block) {
        if (!world.isRemote) {
            world.notifyBlockOfNeighborChange(x - 1, y, z, block);
            world.notifyBlockOfNeighborChange(x + 1, y, z, block);
            world.notifyBlockOfNeighborChange(x, y - 1, z, block);
            world.notifyBlockOfNeighborChange(x, y + 1, z, block);
            world.notifyBlockOfNeighborChange(x, y, z - 1, block);
            world.notifyBlockOfNeighborChange(x, y, z + 1, block);
            world.notifyBlockOfNeighborChange(x - 1, y - 1, z, block);
            world.notifyBlockOfNeighborChange(x - 1, y, z - 1, block);
            world.notifyBlockOfNeighborChange(x - 1, y, z + 1, block);
            world.notifyBlockOfNeighborChange(x - 1, y + 1, z, block);
            world.notifyBlockOfNeighborChange(x, y - 1, z - 1, block);
            world.notifyBlockOfNeighborChange(x, y - 1, z + 1, block);
            world.notifyBlockOfNeighborChange(x, y + 1, z - 1, block);
            world.notifyBlockOfNeighborChange(x, y + 1, z + 1, block);
            world.notifyBlockOfNeighborChange(x + 1, y - 1, z, block);
            world.notifyBlockOfNeighborChange(x + 1, y, z - 1, block);
            world.notifyBlockOfNeighborChange(x + 1, y, z + 1, block);
            world.notifyBlockOfNeighborChange(x + 1, y + 1, z, block);
            world.notifyBlockOfNeighborChange(x - 2, y, z, block);
            world.notifyBlockOfNeighborChange(x + 2, y, z, block);
            world.notifyBlockOfNeighborChange(x, y - 2, z, block);
            world.notifyBlockOfNeighborChange(x, y + 2, z, block);
            world.notifyBlockOfNeighborChange(x, y, z - 2, block);
            world.notifyBlockOfNeighborChange(x, y, z + 2, block);
        }
    }
}
