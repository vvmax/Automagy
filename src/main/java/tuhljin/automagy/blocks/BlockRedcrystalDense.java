package tuhljin.automagy.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import tuhljin.automagy.lib.RedstoneCalc;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityRedcrystal;

public class BlockRedcrystalDense extends BlockRedcrystalLarge {
    public static final int MAX_MINIMUM = 15;
    public static final int MIN_MINIMUM = 2;

    public BlockRedcrystalDense(String name) {
        super(name);
    }

    @Override
    protected RedstoneCalc.PowerResult calculateRedstonePowerAt(World world, int x, int y, int z, int orientation) {
        RedstoneCalc.PowerResult result = super.calculateRedstonePowerAt(world, x, y, z, orientation);
        return result != null && result.strength >= this.getRedstoneSignalMin(world, x, y, z) ? result : null;
    }

    public int getRedstoneSignalMin(IBlockAccess blockaccess, int x, int y, int z) {
        int min = 2;

        try {
            TileEntityRedcrystal te = (TileEntityRedcrystal)blockaccess.getTileEntity(x, y, z);
            min = te.extraData;
        } catch (Exception var7) {
        }

        if (min > 15) {
            return 15;
        } else {
            return min < 2 ? 2 : min;
        }
    }

    @Override
    public boolean onBlockActivatedCenter(World world, int x, int y, int z, EntityPlayer player) {
        if (!world.isRemote) {
            int min = this.getRedstoneSignalMin(world, x, y, z);

            try {
                TileEntityRedcrystal te = (TileEntityRedcrystal)world.getTileEntity(x, y, z);
                if (++min > 15) {
                    min = 2;
                }

                te.extraData = min;
                te.markDirty();
            } catch (Exception var8) {
            }

            this.updateAndPropagateChanges(world, x, y, z, true, false, true, false);
            TjUtil.sendFormattedChatToPlayer(player, "Automagy.chat.redcrystalDense.setMin", min);
            return true;
        } else {
            return false;
        }
    }
}
