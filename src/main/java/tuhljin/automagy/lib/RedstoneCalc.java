package tuhljin.automagy.lib;

import cpw.mods.fml.common.FMLLog;
import java.util.LinkedHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.logging.log4j.Level;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.lib.struct.BlockWithPos;

public class RedstoneCalc {
    public static final int MAX_POWER = 15;
    public static final int MAX_POWER_AMPLIFIED = 99;
    public static final boolean COUNT_PISTONHEAD_AS_AIR = true;

    public static RedstoneCalc.PowerResult getRedstonePowerAt(World world, int x, int y, int z) {
        return getRedstonePowerAt(world, x, y, z, 1, false, -1, false, 15, ForgeDirection.VALID_DIRECTIONS);
    }

    public static RedstoneCalc.PowerResult getRedstonePowerAt(World world, int x, int y, int z, ForgeDirection... directions) {
        return getRedstonePowerAt(world, x, y, z, 1, false, -1, false, 15, directions);
    }

    public static RedstoneCalc.PowerResult getRedstonePowerAt(
        World world, int x, int y, int z, int attachedToSide, boolean isWire, int excludeRedstoneWireDir, boolean allowAmplifiedPower
    ) {
        return getRedstonePowerAt(world, x, y, z, attachedToSide, isWire, excludeRedstoneWireDir, allowAmplifiedPower, 15, ForgeDirection.VALID_DIRECTIONS);
    }

    public static RedstoneCalc.PowerResult getRedstonePowerAt(
        World world,
        int x,
        int y,
        int z,
        int attachedToSide,
        boolean isWire,
        int excludeRedstoneWireDir,
        boolean allowAmplifiedPower,
        ForgeDirection... directions
    ) {
        return getRedstonePowerAt(world, x, y, z, attachedToSide, isWire, excludeRedstoneWireDir, allowAmplifiedPower, 15, directions);
    }

    public static RedstoneCalc.PowerResult getRedstonePowerAt(
        World world,
        int x,
        int y,
        int z,
        int attachedToSide,
        boolean isWire,
        int excludeRedstoneWireDir,
        boolean allowAmplifiedPower,
        int maxPower,
        ForgeDirection... directions
    ) {
        int strength = 0;
        ForgeDirection directionToPower = ForgeDirection.UNKNOWN;

        for (ForgeDirection dir : directions) {
            int x2 = x + dir.offsetX;
            int y2 = y + dir.offsetY;
            int z2 = z + dir.offsetZ;
            Block block = world.getBlock(x2, y2, z2);
            if (block instanceof BlockRedstoneWire) {
                if (excludeRedstoneWireDir != dir.ordinal()) {
                    int metadata = world.getBlockMetadata(x2, y2, z2);
                    if (isWire) {
                        metadata--;
                    }

                    if (metadata > strength) {
                        strength = metadata;
                        directionToPower = dir;
                    }
                }
            } else if (block instanceof IAmplifiedRedstoneOutput) {
                IAmplifiedRedstoneOutput amp = (IAmplifiedRedstoneOutput)block;
                int data = amp.getRedstoneSignalStrength(world, x2, y2, z2, dir.getOpposite().ordinal(), allowAmplifiedPower);
                if (isWire && amp.isRedstoneWire()) {
                    data--;
                }

                if (data > strength) {
                    strength = data;
                    directionToPower = dir;
                }
            } else if (isAirBlock(block, world, x2, y2, z2)) {
                if (isWire) {
                    BlockWithPos bwp = getORCAroundCorner(world, x2, y2, z2, attachedToSide, dir);
                    if (bwp != null) {
                        int datax = ((IOrientableRedstoneConductor)bwp.block)
                            .getRedstoneSignalStrength(world, bwp.x, bwp.y, bwp.z, attachedToSide, allowAmplifiedPower);
                        if (--datax > strength) {
                            strength = datax;
                            directionToPower = dir;
                        }
                    }
                }
            } else {
                int s = world.getIndirectPowerLevelTo(x2, y2, z2, dir.ordinal());
                if (s > strength) {
                    if (isWire && !block.canProvidePower()) {
                        for (ForgeDirection subdir : ForgeDirection.VALID_DIRECTIONS) {
                            if (subdir.getOpposite() != dir) {
                                int x3 = x2 + subdir.offsetX;
                                int y3 = y2 + subdir.offsetY;
                                int z3 = z2 + subdir.offsetZ;
                                Block block2 = world.getBlock(x3, y3, z3);
                                if (!(block2 instanceof BlockRedstoneWire) && !(block2 instanceof IOrientableRedstoneConductor) && block2.canProvidePower()) {
                                    int s2 = block2.isProvidingStrongPower(world, x3, y3, z3, subdir.ordinal());
                                    if (s2 == s) {
                                        strength = s;
                                        directionToPower = dir;
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        strength = s;
                        directionToPower = dir;
                    }
                }
            }

            if (strength >= (allowAmplifiedPower ? 99 : maxPower)) {
                break;
            }
        }

        return strength > 0 ? new RedstoneCalc.PowerResult(strength, directionToPower) : null;
    }

    public static boolean isAirBlock(Block block, IBlockAccess blockaccess, int x, int y, int z) {
        return blockaccess.isAirBlock(x, y, z) || block == Blocks.piston_head;
    }

    public static BlockWithPos getORCAroundCorner(IBlockAccess blockaccess, int x2, int y2, int z2, int attachedToSide, ForgeDirection dir) {
        ForgeDirection side = ForgeDirection.getOrientation(attachedToSide);
        if (side == ForgeDirection.UNKNOWN) {
            FMLLog.log(Level.ERROR, "[Automagy] RedstoneCalc was passed UNKNOWN attachedToSide. Will calculate as if given DOWN.");
            side = ForgeDirection.DOWN;
        }

        ForgeDirection subdir = side.getOpposite();
        if (dir.ordinal() != attachedToSide && subdir.ordinal() != attachedToSide) {
            int x3 = x2 + subdir.offsetX;
            int y3 = y2 + subdir.offsetY;
            int z3 = z2 + subdir.offsetZ;
            Block block2 = blockaccess.getBlock(x3, y3, z3);
            if (block2 instanceof IOrientableRedstoneConductor) {
                int otherside = ((IOrientableRedstoneConductor)block2).getOrientation(blockaccess, x3, y3, z3);
                if (TjUtil.getNextSideOnBlockFromDir(attachedToSide, dir.ordinal()) == otherside) {
                    return new BlockWithPos(block2, blockaccess, x3, y3, z3);
                }
            }
        }

        return null;
    }

    public static LinkedHashMap<BlockCoord, ForgeDirection> getConnectedWireAwayFromSource(IBlockAccess blockaccess, int x, int y, int z) {
        LinkedHashMap<BlockCoord, ForgeDirection> wiring = new LinkedHashMap<>();
        buildWiringSet(wiring, blockaccess, x, y, z, -1, 2);
        wiring.remove(new BlockCoord(x, y, z));
        return wiring;
    }

    public static LinkedHashMap<BlockCoord, ForgeDirection> getConnectedWireAwayFromSourceWithNoInputPoint(IBlockAccess blockaccess, int x, int y, int z) {
        LinkedHashMap<BlockCoord, ForgeDirection> wiring = new LinkedHashMap<>();
        boolean ret = buildWiringSet(wiring, blockaccess, x, y, z, -1, -1);
        if (ret) {
            return null;
        } else {
            wiring.remove(new BlockCoord(x, y, z));
            return wiring;
        }
    }

    private static boolean buildWiringSet(
        LinkedHashMap<BlockCoord, ForgeDirection> wiring, IBlockAccess blockaccess, int x, int y, int z, int backDir, int distance
    ) {
        BlockCoord bc = new BlockCoord(x, y, z);
        if (!wiring.containsKey(bc)) {
            Block block = blockaccess.getBlock(x, y, z);
            if (backDir == -1 || block instanceof IOrientableRedstoneConductor) {
                IOrientableRedstoneConductor orc = block instanceof IOrientableRedstoneConductor ? (IOrientableRedstoneConductor)block : null;
                if (backDir != -1
                    && !orc.canReceiveRedstoneSignalFromDirection(blockaccess, x, y, z, backDir)
                    && !orc.canSendRedstoneSignalInDirection(blockaccess, x, y, z, backDir)) {
                    return false;
                }

                wiring.put(bc, ForgeDirection.getOrientation(backDir));
                if (distance == -1) {
                    if (orc != null && orc.isRedstoneSignalInputPoint(blockaccess, x, y, z)) {
                        return true;
                    }
                } else if (--distance == 0) {
                    return false;
                }

                for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                    if (dir.ordinal() != backDir
                        && (
                            orc == null
                                || orc.canSendRedstoneSignalInDirection(blockaccess, x, y, z, dir.ordinal())
                                || orc.canReceiveRedstoneSignalFromDirection(blockaccess, x, y, z, dir.ordinal())
                        )) {
                        int x2 = x + dir.offsetX;
                        int y2 = y + dir.offsetY;
                        int z2 = z + dir.offsetZ;
                        if (isAirBlock(block, blockaccess, x2, y2, z2)) {
                            if (orc == null) {
                                for (int attachedToSide = 0; attachedToSide < 6; attachedToSide++) {
                                    BlockWithPos bwp = getORCAroundCorner(blockaccess, x2, y2, z2, attachedToSide, dir);
                                    if (bwp != null) {
                                        boolean ret = buildWiringSet(wiring, blockaccess, bwp.x, bwp.y, bwp.z, attachedToSide, distance);
                                        if (ret) {
                                            return true;
                                        }
                                    }
                                }
                            } else {
                                int attachedToSidex = orc.getOrientation(blockaccess, x, y, z);
                                BlockWithPos bwp = getORCAroundCorner(blockaccess, x2, y2, z2, attachedToSidex, dir);
                                if (bwp != null) {
                                    boolean ret = buildWiringSet(wiring, blockaccess, bwp.x, bwp.y, bwp.z, attachedToSidex, distance);
                                    if (ret) {
                                        return true;
                                    }
                                }
                            }
                        } else {
                            boolean ret = buildWiringSet(wiring, blockaccess, x2, y2, z2, dir.getOpposite().ordinal(), distance);
                            if (ret) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public static int getRedstoneSignalStrengthFromValues(int amt, int capacity) {
        if (amt < 1 || capacity < 1) {
            return 0;
        } else {
            return amt >= capacity ? 15 : MathHelper.floor_float(1.0F + (float)amt / capacity * 14.0F);
        }
    }

    public static class PowerResult {
        public final int strength;
        public final ForgeDirection sourceDirection;

        public PowerResult(int strength, ForgeDirection sourceDirection) {
            this.strength = strength;
            this.sourceDirection = sourceDirection;
        }
    }
}
