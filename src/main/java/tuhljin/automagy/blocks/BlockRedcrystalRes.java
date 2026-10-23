package tuhljin.automagy.blocks;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import tuhljin.automagy.lib.RedstoneCalc;
import tuhljin.automagy.lib.struct.BlockWithPos;
import tuhljin.automagy.tiles.TileEntityRedcrystal;

public class BlockRedcrystalRes extends BlockRedcrystalLarge {
    public static final int MAX_MINIMUM = 15;
    public static final int MIN_MINIMUM = 2;
    private static final int MAX_DISTANCE = 15;

    public BlockRedcrystalRes(String name) {
        super(name);
    }

    @Override
    protected RedstoneCalc.PowerResult calculateRedstonePowerAt(World world, int x, int y, int z, int orientation) {
        int receivingStrength = 0;

        TileEntityRedcrystal te;
        try {
            te = (TileEntityRedcrystal)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
            return null;
        }

        if (te != null) {
            receivingStrength = Math.max(te.extraData - 1, 0);
            if (receivingStrength >= 99) {
                return new RedstoneCalc.PowerResult(receivingStrength, ForgeDirection.UNKNOWN);
            }
        }

        RedstoneCalc.PowerResult result = super.calculateRedstonePowerAt(world, x, y, z, orientation);
        if (result != null && result.strength > 0) {
            return receivingStrength > result.strength ? new RedstoneCalc.PowerResult(receivingStrength, ForgeDirection.UNKNOWN) : result;
        } else {
            return receivingStrength > 0 ? new RedstoneCalc.PowerResult(receivingStrength, ForgeDirection.UNKNOWN) : null;
        }
    }

    public BlockWithPos findSignalConnection(World world, int x, int y, int z, boolean reverse) {
        int orientation = this.getOrientation(world, x, y, z);
        ForgeDirection dir = ForgeDirection.getOrientation(orientation);
        if (dir != ForgeDirection.UNKNOWN) {
            int opp;
            if (reverse) {
                dir = dir.getOpposite();
                opp = dir.ordinal();
            } else {
                opp = dir.getOpposite().ordinal();
            }

            for (int distance = 1; distance <= 15; distance++) {
                int x2 = x + dir.offsetX * distance;
                int y2 = y + dir.offsetY * distance;
                int z2 = z + dir.offsetZ * distance;
                Block target = world.getBlock(x2, y2, z2);
                if (target instanceof BlockRedcrystalRes) {
                    int o = ((BlockRedcrystalRes)target).getOrientation(world, x2, y2, z2);
                    if (reverse) {
                        if (orientation == o) {
                            return new BlockWithPos(target, world, x2, y2, z2);
                        }

                        if (opp == o) {
                            return null;
                        }
                    } else {
                        if (orientation == o) {
                            return null;
                        }

                        if (opp == o) {
                            return new BlockWithPos(target, world, x2, y2, z2);
                        }
                    }

                    if (opp == o) {
                        if (reverse) {
                            return null;
                        }
                    } else if (orientation == o && reverse) {
                        return new BlockWithPos(target, world, x2, y2, z2);
                    }
                }
            }
        }

        return null;
    }

    public void receiveWirelessSignal(World world, int x, int y, int z, int strength) {
        TileEntityRedcrystal te;
        try {
            te = (TileEntityRedcrystal)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
            return;
        }

        if (strength == -1) {
            strength = 0;
            BlockWithPos target = this.findSignalConnection(world, x, y, z, false);
            if (target != null) {
                BlockRedcrystalRes blockR = (BlockRedcrystalRes)target.block;
                blockR.receiveWirelessSignal(world, target.x, target.y, target.z, blockR.getRedstoneSignalStrength(world, x, y, z, true));
                strength = blockR.getRedstoneSignalStrength(world, target.x, target.y, target.z, true);
            }
        }

        if (te.extraData != strength) {
            te.extraData = strength;
            te.markDirty();
            this.updateAndPropagateChanges(world, x, y, z, true, false, false, false);
        }
    }

    @Override
    public void scheduleAreaUpdateNotification(World world, int x, int y, int z, boolean immediate) {
        super.scheduleAreaUpdateNotification(world, x, y, z, immediate);
        if (!world.isRemote) {
            BlockWithPos target = this.findSignalConnection(world, x, y, z, false);
            if (target != null) {
                BlockRedcrystalRes blockR = (BlockRedcrystalRes)target.block;
                blockR.receiveWirelessSignal(world, target.x, target.y, target.z, blockR.getRedstoneSignalStrength(world, x, y, z, true));
            }
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
        BlockWithPos target = null;
        if (!world.isRemote) {
            target = this.findSignalConnection(world, x, y, z, false);
        }

        super.breakBlock(world, x, y, z, block, p_149749_6_);
        if (target != null) {
            ((BlockRedcrystalRes)target.block).receiveWirelessSignal(world, target.x, target.y, target.z, -1);
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote) {
            TileEntityRedcrystal te;
            try {
                te = (TileEntityRedcrystal)world.getTileEntity(x, y, z);
            } catch (Exception var8) {
                return;
            }

            BlockWithPos target = this.findSignalConnection(world, x, y, z, false);
            if (target != null) {
                BlockRedcrystalRes blockR = (BlockRedcrystalRes)target.block;
                te.extraData = blockR.getRedstoneSignalStrength(world, target.x, target.y, target.z, true);
                te.markDirty();
            }

            target = this.findSignalConnection(world, x, y, z, true);
            if (target != null) {
                BlockRedcrystalRes blockR = (BlockRedcrystalRes)target.block;
                blockR.receiveWirelessSignal(world, target.x, target.y, target.z, 0);
            }
        }

        super.onBlockAdded(world, x, y, z);
    }
}
