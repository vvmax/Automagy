package tuhljin.automagy.blocks;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import tuhljin.automagy.lib.RedstoneCalc;
import tuhljin.automagy.tiles.TileEntityRedcrystal;

public class BlockRedcrystalAmp extends BlockRedcrystalLarge {
    public static int SIGNAL_OUTPUT = 50;
    private final boolean ACCEPT_POWER_FROM_ATTACHED_DIRECTION = true;

    public BlockRedcrystalAmp(String name) {
        super(name);
    }

    @Override
    protected RedstoneCalc.PowerResult calculateRedstonePowerAt(World world, int x, int y, int z, int orientation) {
        RedstoneCalc.PowerResult result = RedstoneCalc.getRedstonePowerAt(
            world, x, y, z, orientation, true, orientation, true, 1, this.getRedstoneInputDirections(world, x, y, z)
        );
        return result != null && result.strength > 0 ? new RedstoneCalc.PowerResult(Math.max(result.strength, SIGNAL_OUTPUT), result.sourceDirection) : null;
    }

    @Override
    public ForgeDirection[] getRedstoneInputDirections(IBlockAccess blockaccess, int x, int y, int z) {
        TileEntityRedcrystal te;
        try {
            te = (TileEntityRedcrystal)blockaccess.getTileEntity(x, y, z);
        } catch (Exception var8) {
            return null;
        }

        List<ForgeDirection> list = new ArrayList<>();
        int orientation = te.orientation;
        switch (orientation) {
            case 2:
                if (te.connectN) {
                    list.add(ForgeDirection.UP);
                }

                if (te.connectS) {
                    list.add(ForgeDirection.DOWN);
                }

                if (te.connectW) {
                    list.add(ForgeDirection.EAST);
                }

                if (te.connectE) {
                    list.add(ForgeDirection.WEST);
                }
                break;
            case 3:
                if (te.connectN) {
                    list.add(ForgeDirection.DOWN);
                }

                if (te.connectS) {
                    list.add(ForgeDirection.UP);
                }

                if (te.connectW) {
                    list.add(ForgeDirection.EAST);
                }

                if (te.connectE) {
                    list.add(ForgeDirection.WEST);
                }
                break;
            case 4:
                if (te.connectN) {
                    list.add(ForgeDirection.SOUTH);
                }

                if (te.connectS) {
                    list.add(ForgeDirection.NORTH);
                }

                if (te.connectW) {
                    list.add(ForgeDirection.UP);
                }

                if (te.connectE) {
                    list.add(ForgeDirection.DOWN);
                }
                break;
            case 5:
                if (te.connectN) {
                    list.add(ForgeDirection.SOUTH);
                }

                if (te.connectS) {
                    list.add(ForgeDirection.NORTH);
                }

                if (te.connectW) {
                    list.add(ForgeDirection.DOWN);
                }

                if (te.connectE) {
                    list.add(ForgeDirection.UP);
                }
                break;
            default:
                if (te.connectN) {
                    list.add(ForgeDirection.SOUTH);
                }

                if (te.connectS) {
                    list.add(ForgeDirection.NORTH);
                }

                if (te.connectW) {
                    list.add(ForgeDirection.EAST);
                }

                if (te.connectE) {
                    list.add(ForgeDirection.WEST);
                }
        }

        list.add(ForgeDirection.getOrientation(orientation).getOpposite());
        return list.toArray(new ForgeDirection[list.size()]);
    }

    @Override
    public boolean canSendRedstoneSignalInDirection(IBlockAccess blockaccess, int x, int y, int z, int dir) {
        TileEntityRedcrystal te;
        try {
            te = (TileEntityRedcrystal)blockaccess.getTileEntity(x, y, z);
        } catch (Exception var8) {
            return false;
        }

        int orientation = te.orientation;
        switch (orientation) {
            case 0:
            case 1:
                switch (dir) {
                    case 2:
                        return te.connectN;
                    case 3:
                        return te.connectS;
                    case 4:
                        return te.connectW;
                    case 5:
                        return te.connectE;
                    default:
                        return false;
                }
            case 2:
                switch (dir) {
                    case 0:
                        return te.connectN;
                    case 1:
                        return te.connectS;
                    case 2:
                    case 3:
                    default:
                        return false;
                    case 4:
                        return te.connectW;
                    case 5:
                        return te.connectE;
                }
            case 3:
                switch (dir) {
                    case 0:
                        return te.connectS;
                    case 1:
                        return te.connectN;
                    case 2:
                    case 3:
                    default:
                        return false;
                    case 4:
                        return te.connectW;
                    case 5:
                        return te.connectE;
                }
            case 4:
                switch (dir) {
                    case 0:
                        return te.connectW;
                    case 1:
                        return te.connectE;
                    case 2:
                        return te.connectN;
                    case 3:
                        return te.connectS;
                    default:
                        return false;
                }
            case 5:
                switch (dir) {
                    case 0:
                        return te.connectE;
                    case 1:
                        return te.connectW;
                    case 2:
                        return te.connectN;
                    case 3:
                        return te.connectS;
                }
        }

        return false;
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
        if (!this.canProvidePower()) {
            return false;
        } else {
            int opposite;
            byte var7;
            switch (side) {
                case 0:
                    var7 = 2;
                    opposite = 3;
                    break;
                case 1:
                    var7 = 5;
                    opposite = 4;
                    break;
                case 2:
                    var7 = 3;
                    opposite = 2;
                    break;
                case 3:
                    var7 = 4;
                    opposite = 5;
                    break;
                default:
                    return false;
            }

            return this.canSendRedstoneSignalInDirection(world, x, y, z, var7) || this.canSendRedstoneSignalInDirection(world, x, y, z, opposite);
        }
    }

    @Override
    public boolean allowNeighborToChangeConnection(World world, int x, int y, int z) {
        return false;
    }

    @Override
    public boolean onBlockActivatedAdjustedSide(World world, int x, int y, int z, EntityPlayer player, int side) {
        TileEntityRedcrystal te = null;

        try {
            te = (TileEntityRedcrystal)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te == null) {
            return false;
        } else {
            if (side != 2 && te.connectN) {
                this.toggleConnectionToDirection(world, x, y, z, 2, false);
            }

            if (side != 3 && te.connectS) {
                this.toggleConnectionToDirection(world, x, y, z, 3, false);
            }

            if (side != 4 && te.connectW) {
                this.toggleConnectionToDirection(world, x, y, z, 4, false);
            }

            if (side != 5 && te.connectE) {
                this.toggleConnectionToDirection(world, x, y, z, 5, false);
            }

            return super.onBlockActivatedAdjustedSide(world, x, y, z, player, side);
        }
    }
}
