package tuhljin.automagy.lib.struct;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

public class BlockWithPos {
    public final Block block;
    public final IBlockAccess blockaccess;
    public final int x;
    public final int y;
    public final int z;

    public BlockWithPos(Block block, IBlockAccess blockaccess, int x, int y, int z) {
        this.block = block;
        this.blockaccess = blockaccess;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
