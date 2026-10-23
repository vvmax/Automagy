package tuhljin.automagy.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import tuhljin.automagy.lib.RedstoneCalc;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityRedcrystal;

public class BlockRedcrystalDim extends BlockRedcrystalLarge {
    public static final int MAX_CAP = 14;
    public static final int MIN_CAP = 1;

    public BlockRedcrystalDim(String name) {
        super(name);
    }

    @Override
    protected RedstoneCalc.PowerResult calculateRedstonePowerAt(World world, int x, int y, int z, int orientation) {
        RedstoneCalc.PowerResult result = super.calculateRedstonePowerAt(world, x, y, z, orientation);
        return result != null && result.strength > 0
            ? new RedstoneCalc.PowerResult(Math.min(result.strength, this.getRedstoneSignalCap(world, x, y, z)), result.sourceDirection)
            : null;
    }

    public int getRedstoneSignalCap(IBlockAccess blockaccess, int x, int y, int z) {
        int cap = 1;

        try {
            TileEntityRedcrystal te = (TileEntityRedcrystal)blockaccess.getTileEntity(x, y, z);
            cap = te.extraData;
        } catch (Exception var7) {
        }

        if (cap > 14) {
            return 14;
        } else {
            return cap < 1 ? 1 : cap;
        }
    }

    @Override
    public boolean onBlockActivatedCenter(World world, int x, int y, int z, EntityPlayer player) {
        if (!world.isRemote) {
            int cap = this.getRedstoneSignalCap(world, x, y, z);

            try {
                TileEntityRedcrystal te = (TileEntityRedcrystal)world.getTileEntity(x, y, z);
                if (++cap > 14) {
                    cap = 1;
                }

                te.extraData = cap;
                te.markDirty();
            } catch (Exception var8) {
            }

            this.updateAndPropagateChanges(world, x, y, z, true, false, true, false);
            TjUtil.sendFormattedChatToPlayer(player, "Automagy.chat.redcrystalDim.setCap", cap);
            return true;
        } else {
            return false;
        }
    }
}
