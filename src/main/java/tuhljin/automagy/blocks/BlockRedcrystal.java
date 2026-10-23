package tuhljin.automagy.blocks;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.codechicken.lib.raytracer.IndexedCuboid6;
import thaumcraft.codechicken.lib.raytracer.RayTracer;
import thaumcraft.codechicken.lib.vec.Cuboid6;
import thaumcraft.codechicken.lib.vec.Vector3;
import thaumcraft.common.items.wands.ItemWandCasting;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.lib.IOrientableRedstoneConductor;
import tuhljin.automagy.lib.IRedcrystalPowerConductor;
import tuhljin.automagy.lib.RedstoneCalc;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.struct.BlockWithPos;
import tuhljin.automagy.tiles.TileEntityRedcrystal;

public class BlockRedcrystal extends ModBlockSpecialRender implements IOrientableRedstoneConductor, IRedcrystalPowerConductor {
    protected final boolean CONNECT_REDSTONE_REL_TOP = false;
    protected static final int MAX_POWER = 15;
    private boolean simplePowerCheck = true;
    protected static boolean resettingPowerLevels = false;
    private HashSet<BlockCoord> newPrimaryPowerInputLocations = new HashSet<>();
    private HashMap<Integer, LinkedHashSet<BlockCoord>> queuedBlockUpdateLocations = new HashMap<>();
    private Field redstonedustAllowingPowerCheck;
    public int nextTEOrientation = -1;
    public boolean nextTENoConnections = false;
    private RayTracer rayTracer = new RayTracer();
    public static final int HITDOWN = 0;
    public static final int HITUP = 1;
    public static final int HITNORTH = 2;
    public static final int HITSOUTH = 3;
    public static final int HITWEST = 4;
    public static final int HITEAST = 5;
    public static final int HITCENTER = 6;
    public static final int HITOTHER = 7;

    public BlockRedcrystal(String name) {
        super(name, Material.circuits, ItemBlockRedcrystal.class);
        this.setHardness(0.1F);
        this.setStepSound(Block.soundTypeStone);
        this.disableStats();
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);

        try {
            this.redstonedustAllowingPowerCheck = Blocks.redstone_wire.getClass().getDeclaredField("field_150181_a");
            this.redstonedustAllowingPowerCheck.setAccessible(true);
        } catch (Exception var3) {
            FMLLog.severe(
                "[Automagy] Failed to access redstone dust's signal-checking field. Improper signal strengths may result when redstone wire is placed next to redcrystal."
            );
        }
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        TileEntityRedcrystal te = new TileEntityRedcrystal((short)this.nextTEOrientation, this.nextTENoConnections);
        this.nextTEOrientation = -1;
        this.nextTENoConnections = false;
        return te;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        this.updateAndPropagateChanges(world, x, y, z, true, false, false, false);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
        super.breakBlock(world, x, y, z, block, p_149749_6_);
        this.updateAndPropagateChanges(world, x, y, z, false, false, true, true);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_) {
        return null;
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        int side = this.getOrientation(world, x, y, z);
        return this.canPlaceBlockOnSide(world, x, y, z, side);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        boolean ok = dir == ForgeDirection.DOWN && world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN)
            || dir == ForgeDirection.UP && world.isSideSolid(x, y - 1, z, ForgeDirection.UP)
            || dir == ForgeDirection.NORTH && world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH)
            || dir == ForgeDirection.SOUTH && world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH)
            || dir == ForgeDirection.WEST && world.isSideSolid(x + 1, y, z, ForgeDirection.WEST)
            || dir == ForgeDirection.EAST && world.isSideSolid(x - 1, y, z, ForgeDirection.EAST);
        return !ok && world.getBlock(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ) == Blocks.glowstone ? true : ok;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        int strength = world.getBlockMetadata(x, y, z);
        if (strength > 0) {
            double d0 = x + 0.5 + (rand.nextFloat() - 0.5) * 0.2;
            double d1 = y + 0.1625F;
            double d2 = z + 0.5 + (rand.nextFloat() - 0.5) * 0.2;
            float f = strength / 15.0F;
            float f1 = f * 0.6F + 0.4F;
            if (strength == 0) {
                f1 = 0.0F;
            }

            float f2 = f * f * 0.7F - 0.5F;
            float f3 = f * f * 0.6F - 0.7F;
            if (f2 < 0.0F) {
                f2 = 0.0F;
            }

            if (f3 < 0.0F) {
                f3 = 0.0F;
            }

            int orientation = this.getOrientation(world, x, y, z);
            switch (orientation) {
                case 0:
                    d1 += 0.6;
                case 1:
                default:
                    break;
                case 2:
                    d1 += 0.28;
                    d2 += 0.3;
                    break;
                case 3:
                    d1 += 0.28;
                    d2 -= 0.3;
                    break;
                case 4:
                    d0 += 0.3;
                    d1 += 0.28;
                    break;
                case 5:
                    d0 -= 0.3;
                    d1 += 0.28;
            }

            world.spawnParticle("reddust", d0, d1, d2, f1, f2, f3);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!world.isRemote) {
            int side = this.getOrientation(world, x, y, z);
            if (side != -1) {
                if (this.canPlaceBlockOnSide(world, x, y, z, side)) {
                    if (!(block instanceof IRedcrystalPowerConductor)) {
                        this.updateAndPropagateChanges(world, x, y, z, true, false, false, false);
                    }
                } else {
                    this.dropBlockAsItem(world, x, y, z, 0, 0);
                    world.setBlockToAir(x, y, z);
                }
            }

            super.onNeighborBlockChange(world, x, y, z, block);
        }
    }

    public void updateAndPropagateChanges(
        World world, int x, int y, int z, boolean checkStrength, boolean calledByNeighborWire, boolean forcePropagate, boolean immediateNeighborUpdates
    ) {
        if (!world.isRemote) {
            boolean notifyChain = false;
            boolean needClientUpdate = false;
            boolean needNotifyNeighbors = false;
            int strength = -1;
            TileEntityRedcrystal te = null;
            if (checkStrength) {
                te = (TileEntityRedcrystal)world.getTileEntity(x, y, z);
                if (te == null) {
                    return;
                }

                int orientation = te.orientation;
                if (orientation != -1) {
                    this.simplePowerCheck = false;
                    RedstoneCalc.PowerResult result = this.calculateRedstonePowerAt(world, x, y, z, orientation);
                    this.simplePowerCheck = true;
                    strength = result == null ? 0 : result.strength;
                    int sourceSide = result == null ? -1 : result.sourceDirection.ordinal();
                    if (sourceSide != te.powerSourceSide) {
                        te.powerSourceSide = (short)sourceSide;
                        needClientUpdate = true;
                        notifyChain = true;
                    }

                    if (sourceSide != -1 && strength >= 1) {
                        ForgeDirection sourceSideDir = ForgeDirection.getOrientation(sourceSide);
                        Block sourceBlock = world.getBlock(x + sourceSideDir.offsetX, y + sourceSideDir.offsetY, z + sourceSideDir.offsetZ);
                        if (!(sourceBlock instanceof IRedcrystalPowerConductor)
                            && !RedstoneCalc.isAirBlock(sourceBlock, world, x + sourceSideDir.offsetX, y + sourceSideDir.offsetY, z + sourceSideDir.offsetZ)) {
                            if (!te.powerSourceOutsideNetwork) {
                                te.powerSourceOutsideNetwork = true;
                                needClientUpdate = true;
                                notifyChain = true;
                            }

                            if (resettingPowerLevels) {
                                this.newPrimaryPowerInputLocations.add(new BlockCoord(x, y, z));
                            }
                        } else if (te.powerSourceOutsideNetwork) {
                            te.powerSourceOutsideNetwork = false;
                            needClientUpdate = true;
                            notifyChain = true;
                        }
                    } else if (te.powerSourceOutsideNetwork) {
                        te.powerSourceOutsideNetwork = false;
                        needClientUpdate = true;
                        notifyChain = true;
                    }

                    int metadata = world.getBlockMetadata(x, y, z);
                    int amp = 0;
                    if (metadata == 15) {
                        amp = te.powerStability;
                    }

                    if (strength != metadata + amp) {
                        if (strength > 15) {
                            metadata = 15;
                            te.powerStability = (short)(strength - metadata);
                        } else {
                            metadata = strength;
                            if (amp > 0) {
                                te.powerStability = 0;
                            }
                        }

                        world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
                        needNotifyNeighbors = true;
                        needClientUpdate = true;
                        notifyChain = true;
                    }
                }
            } else {
                strength = 0;
                notifyChain = true;
            }

            if (notifyChain || forcePropagate) {
                if (strength == -1) {
                    strength = this.getRedstoneSignalStrength(world, x, y, z, true);
                }

                if (!calledByNeighborWire && (needClientUpdate || !checkStrength)) {
                    LinkedHashMap<BlockCoord, ForgeDirection> wireTrace = RedstoneCalc.getConnectedWireAwayFromSourceWithNoInputPoint(world, x, y, z);
                    if (wireTrace != null) {
                        for (Entry<BlockCoord, ForgeDirection> entry : wireTrace.entrySet()) {
                            BlockCoord bc = entry.getKey();
                            IOrientableRedstoneConductor orc = (IOrientableRedstoneConductor)world.getBlock(bc.x, bc.y, bc.z);
                            if (orc.getRedstoneSignalStrength(world, bc.x, bc.y, bc.z, true) > 0) {
                                resettingPowerLevels = true;
                                orc.onNeighborRedstoneConductorUpdate(world, bc.x, bc.y, bc.z, entry.getValue(), 100);
                                resettingPowerLevels = false;
                            }
                        }

                        for (BlockCoord bc : this.newPrimaryPowerInputLocations) {
                            Block block = world.getBlock(bc.x, bc.y, bc.z);
                            if (block instanceof BlockRedcrystal) {
                                ((BlockRedcrystal)block).updateAndPropagateChanges(world, bc.x, bc.y, bc.z, true, false, true, false);
                            }
                        }

                        this.newPrimaryPowerInputLocations.clear();
                    }
                }

                LinkedHashMap<BlockCoord, ForgeDirection> wiring = RedstoneCalc.getConnectedWireAwayFromSource(world, x, y, z);

                for (Entry<BlockCoord, ForgeDirection> entryx : wiring.entrySet()) {
                    BlockCoord bcx = entryx.getKey();
                    IOrientableRedstoneConductor orc = (IOrientableRedstoneConductor)world.getBlock(bcx.x, bcx.y, bcx.z);
                    orc.onNeighborRedstoneConductorUpdate(world, bcx.x, bcx.y, bcx.z, entryx.getValue(), strength);
                }
            }

            if (needClientUpdate) {
                if (te == null) {
                    te = (TileEntityRedcrystal)world.getTileEntity(x, y, z);
                }

                te.markDirty();
            }

            if (needNotifyNeighbors || forcePropagate) {
                this.scheduleAreaUpdateNotification(world, x, y, z, immediateNeighborUpdates);
            }
        }
    }

    public void scheduleAreaUpdateNotification(World world, int x, int y, int z, boolean immediate) {
        if (!world.isRemote) {
            int dimension = world.provider.dimensionId;
            LinkedHashSet<BlockCoord> bcs;
            if (this.queuedBlockUpdateLocations.containsKey(dimension)) {
                bcs = this.queuedBlockUpdateLocations.get(dimension);
            } else {
                bcs = new LinkedHashSet<>();
                this.queuedBlockUpdateLocations.put(dimension, bcs);
            }

            bcs.add(new BlockCoord(x - 1, y, z));
            bcs.add(new BlockCoord(x + 1, y, z));
            bcs.add(new BlockCoord(x, y - 1, z));
            bcs.add(new BlockCoord(x, y + 1, z));
            bcs.add(new BlockCoord(x, y, z - 1));
            bcs.add(new BlockCoord(x, y, z + 1));
            bcs.add(new BlockCoord(x - 1, y - 1, z));
            bcs.add(new BlockCoord(x - 1, y, z - 1));
            bcs.add(new BlockCoord(x - 1, y, z + 1));
            bcs.add(new BlockCoord(x - 1, y + 1, z));
            bcs.add(new BlockCoord(x, y - 1, z - 1));
            bcs.add(new BlockCoord(x, y - 1, z + 1));
            bcs.add(new BlockCoord(x, y + 1, z - 1));
            bcs.add(new BlockCoord(x, y + 1, z + 1));
            bcs.add(new BlockCoord(x + 1, y - 1, z));
            bcs.add(new BlockCoord(x + 1, y, z - 1));
            bcs.add(new BlockCoord(x + 1, y, z + 1));
            bcs.add(new BlockCoord(x + 1, y + 1, z));
            bcs.add(new BlockCoord(x - 2, y, z));
            bcs.add(new BlockCoord(x + 2, y, z));
            bcs.add(new BlockCoord(x, y - 2, z));
            bcs.add(new BlockCoord(x, y + 2, z));
            bcs.add(new BlockCoord(x, y, z - 2));
            bcs.add(new BlockCoord(x, y, z + 2));
            if (immediate) {
                this.handleQueuedBlockUpdates(world);
            } else {
                world.scheduleBlockUpdate(x, y, z, this, 1);
            }
        }
    }

    private void handleQueuedBlockUpdates(World world) {
        if (!world.isRemote) {
            int dimension = world.provider.dimensionId;
            LinkedHashSet<BlockCoord> bcs = this.queuedBlockUpdateLocations.get(dimension);
            if (bcs == null) {
                return;
            }

            BlockCoord[] queue = bcs.toArray(new BlockCoord[bcs.size()]);
            bcs.clear();

            for (BlockCoord bc : queue) {
                world.notifyBlockOfNeighborChange(bc.x, bc.y, bc.z, this);
            }
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        this.handleQueuedBlockUpdates(world);
    }

    @Override
    public void onNeighborRedstoneConductorUpdate(World world, int x, int y, int z, ForgeDirection neighborDir, int neighborStrength) {
        int strength = this.getRedstoneSignalStrength(world, x, y, z, true);
        if (neighborStrength > strength || this.getRedstoneSignalSourceSide(world, x, y, z) == neighborDir.ordinal()) {
            this.updateAndPropagateChanges(world, x, y, z, true, true, false, false);
        }
    }

    public void onNeighborRedstoneConductorUpdate(World world, int x, int y, int z) {
        this.updateAndPropagateChanges(world, x, y, z, true, true, false, false);
    }

    @Override
    public boolean isRedstoneSignalInputPoint(IBlockAccess blockaccess, int x, int y, int z) {
        try {
            TileEntityRedcrystal te = (TileEntityRedcrystal)blockaccess.getTileEntity(x, y, z);
            return te.powerSourceOutsideNetwork;
        } catch (Exception var6) {
            return false;
        }
    }

    protected RedstoneCalc.PowerResult calculateRedstonePowerAt(World world, int x, int y, int z, int orientation) {
        return RedstoneCalc.getRedstonePowerAt(world, x, y, z, orientation, true, orientation, true, this.getRedstoneInputDirections(world, x, y, z));
    }

    @Override
    public int getLightValue(IBlockAccess blockaccess, int x, int y, int z) {
        if (!AutomagyConfig.redcrystalEmitsLight) {
            return super.getLightValue(blockaccess, x, y, z);
        } else {
            double strength = blockaccess.getBlockMetadata(x, y, z);
            strength = Math.floor(strength * 0.6666666666666666);
            return (int)strength;
        }
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
        byte var6;
        switch (side) {
            case 0:
                var6 = 2;
                break;
            case 1:
                var6 = 5;
                break;
            case 2:
                var6 = 3;
                break;
            case 3:
                var6 = 4;
                break;
            default:
                return false;
        }

        return var6 == this.getOrientation(world, x, y, z) ? false : this.canSendRedstoneSignalInDirection(world, x, y, z, var6);
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess blockaccess, int x, int y, int z, int side) {
        if (!this.simplePowerCheck) {
            return 0;
        } else {
            try {
                if (!this.redstonedustAllowingPowerCheck.getBoolean(Blocks.redstone_wire)) {
                    return 0;
                }
            } catch (Exception var7) {
            }

            return this.isProvidingWeakPower(blockaccess, x, y, z, side);
        }
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess blockaccess, int x, int y, int z, int side) {
        if (!this.simplePowerCheck) {
            return 0;
        } else if (side == TjUtil.getOppositeSide(this.getOrientation(blockaccess, x, y, z))) {
            return 0;
        } else {
            int strength = blockaccess.getBlockMetadata(x, y, z);
            if (strength > 0) {
                if (!this.canSendRedstoneSignalInDirection(blockaccess, x, y, z, ForgeDirection.getOrientation(side).getOpposite().ordinal())) {
                    return 0;
                }

                try {
                    if (!this.redstonedustAllowingPowerCheck.getBoolean(Blocks.redstone_wire)) {
                        strength--;
                    }
                } catch (Exception var8) {
                }
            }

            return strength;
        }
    }

    @Override
    public int getOrientation(IBlockAccess blockaccess, int x, int y, int z) {
        int orientation = -1;

        try {
            TileEntityRedcrystal te = (TileEntityRedcrystal)blockaccess.getTileEntity(x, y, z);
            orientation = te.orientation;
        } catch (Exception var7) {
        }

        return orientation;
    }

    @Override
    public int getRedstoneSignalSourceSide(IBlockAccess blockaccess, int x, int y, int z) {
        int side = -1;

        try {
            TileEntityRedcrystal te = (TileEntityRedcrystal)blockaccess.getTileEntity(x, y, z);
            side = te.powerSourceSide;
        } catch (Exception var7) {
        }

        return side;
    }

    @Override
    public int getRedstoneSignalStrength(IBlockAccess blockaccess, int x, int y, int z, boolean allowAmplifiedPower) {
        if (resettingPowerLevels) {
            return 0;
        } else {
            int strength = blockaccess.getBlockMetadata(x, y, z);
            if (strength == 15 && allowAmplifiedPower) {
                TileEntityRedcrystal te = (TileEntityRedcrystal)blockaccess.getTileEntity(x, y, z);
                return strength + te.powerStability;
            } else {
                return strength;
            }
        }
    }

    @Override
    public boolean isRedstoneWire() {
        return true;
    }

    @Override
    public int getRedstoneSignalStrength(IBlockAccess blockaccess, int x, int y, int z, int intoDirection, boolean allowAmplifiedPower) {
        int strength = this.getRedstoneSignalStrength(blockaccess, x, y, z, allowAmplifiedPower);
        return strength > 0 && !this.canSendRedstoneSignalInDirection(blockaccess, x, y, z, intoDirection) ? 0 : strength;
    }

    public void setConnection(IBlockAccess blockaccess, int x, int y, int z, boolean connects, int... sides) {
        TileEntityRedcrystal te;
        try {
            te = (TileEntityRedcrystal)blockaccess.getTileEntity(x, y, z);
        } catch (Exception var13) {
            return;
        }

        boolean madeChanges = false;

        for (int side : sides) {
            if (side == 0) {
                side = te.orientation;
            } else if (side == 1) {
                side = TjUtil.getOppositeSide(te.orientation);
            }

            switch (side) {
                case 2:
                    te.connectN = connects;
                    madeChanges = true;
                    break;
                case 3:
                    te.connectS = connects;
                    madeChanges = true;
                    break;
                case 4:
                    te.connectW = connects;
                    madeChanges = true;
                    break;
                case 5:
                    te.connectE = connects;
                    madeChanges = true;
            }
        }

        if (madeChanges) {
            te.markDirty();
        }

        this.updateAndPropagateChanges(te.getWorld(), x, y, z, true, false, true, false);
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
                    case 0:
                    case 1:
                        return true;
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
                        return true;
                    case 4:
                        return te.connectW;
                    case 5:
                        return te.connectE;
                    default:
                        return false;
                }
            case 3:
                switch (dir) {
                    case 0:
                        return te.connectS;
                    case 1:
                        return te.connectN;
                    case 2:
                    case 3:
                        return true;
                    case 4:
                        return te.connectW;
                    case 5:
                        return te.connectE;
                    default:
                        return false;
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
                    case 4:
                    case 5:
                        return true;
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
                    case 4:
                    case 5:
                        return true;
                }
        }

        return false;
    }

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
                    list.add(ForgeDirection.DOWN);
                }

                if (te.connectS) {
                    list.add(ForgeDirection.UP);
                }

                if (te.connectW) {
                    list.add(ForgeDirection.WEST);
                }

                if (te.connectE) {
                    list.add(ForgeDirection.EAST);
                }

                list.add(ForgeDirection.NORTH);
                list.add(ForgeDirection.SOUTH);
                break;
            case 3:
                if (te.connectN) {
                    list.add(ForgeDirection.UP);
                }

                if (te.connectS) {
                    list.add(ForgeDirection.DOWN);
                }

                if (te.connectW) {
                    list.add(ForgeDirection.WEST);
                }

                if (te.connectE) {
                    list.add(ForgeDirection.EAST);
                }

                list.add(ForgeDirection.NORTH);
                list.add(ForgeDirection.SOUTH);
                break;
            case 4:
                if (te.connectN) {
                    list.add(ForgeDirection.NORTH);
                }

                if (te.connectS) {
                    list.add(ForgeDirection.SOUTH);
                }

                if (te.connectW) {
                    list.add(ForgeDirection.DOWN);
                }

                if (te.connectE) {
                    list.add(ForgeDirection.UP);
                }

                list.add(ForgeDirection.EAST);
                list.add(ForgeDirection.WEST);
                break;
            case 5:
                if (te.connectN) {
                    list.add(ForgeDirection.NORTH);
                }

                if (te.connectS) {
                    list.add(ForgeDirection.SOUTH);
                }

                if (te.connectW) {
                    list.add(ForgeDirection.UP);
                }

                if (te.connectE) {
                    list.add(ForgeDirection.DOWN);
                }

                list.add(ForgeDirection.EAST);
                list.add(ForgeDirection.WEST);
                break;
            default:
                if (te.connectN) {
                    list.add(ForgeDirection.NORTH);
                }

                if (te.connectS) {
                    list.add(ForgeDirection.SOUTH);
                }

                if (te.connectW) {
                    list.add(ForgeDirection.WEST);
                }

                if (te.connectE) {
                    list.add(ForgeDirection.EAST);
                }

                list.add(ForgeDirection.UP);
                list.add(ForgeDirection.DOWN);
        }

        return list.toArray(new ForgeDirection[list.size()]);
    }

    @Override
    public boolean canReceiveRedstoneSignalFromDirection(IBlockAccess blockaccess, int x, int y, int z, int dir) {
        ForgeDirection[] dirs = this.getRedstoneInputDirections(blockaccess, x, y, z);

        for (ForgeDirection d : dirs) {
            if (d.ordinal() == dir) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && ThaumcraftExtension.playerHasWandEquipped(player)) {
            MovingObjectPosition hit = RayTracer.retraceBlock(world, player, x, y, z);
            if (hit != null) {
                int calcSide = hit.subHit;
                if (calcSide == 6) {
                    return this.onBlockActivatedCenter(world, x, y, z, player);
                }

                if (calcSide == 1) {
                    calcSide = TjUtil.getOppositeSide(this.getOrientation(world, x, y, z));
                } else if (calcSide == 0) {
                    calcSide = this.getOrientation(world, x, y, z);
                } else if (calcSide == 7) {
                    return false;
                }

                return this.onBlockActivatedAdjustedSide(world, x, y, z, player, calcSide);
            }
        }

        return ThaumcraftExtension.playerHasWandEquipped(player);
    }

    public boolean onBlockActivatedCenter(World world, int x, int y, int z, EntityPlayer player) {
        return false;
    }

    public boolean onBlockActivatedAdjustedSide(World world, int x, int y, int z, EntityPlayer player, int side) {
        if (this.toggleConnectionToDirection(world, x, y, z, side, true)) {
            world.playSoundAtEntity(player, "thaumcraft:crystal", 1.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }

    public boolean toggleConnectionToDirection(World world, int x, int y, int z, int side, boolean alterOtherConnection) {
        TileEntityRedcrystal te = null;

        try {
            te = (TileEntityRedcrystal)world.getTileEntity(x, y, z);
        } catch (Exception var20) {
        }

        if (te == null) {
            return false;
        } else {
            boolean connects;
            switch (side) {
                case 2:
                    connects = te.connectN;
                    break;
                case 3:
                    connects = te.connectS;
                    break;
                case 4:
                    connects = te.connectW;
                    break;
                case 5:
                    connects = te.connectE;
                    break;
                default:
                    return false;
            }

            connects = !connects;
            BlockCoord connectedPos = null;
            boolean connectionAroundCorner = false;
            boolean overheadConnection = false;
            ForgeDirection orientationDir = ForgeDirection.getOrientation(side);
            ForgeDirection dir = orientationDir;
            int orientation = this.getOrientation(world, x, y, z);
            if (orientation == orientationDir.ordinal()) {
                dir = ForgeDirection.DOWN;
            } else if (orientation == orientationDir.getOpposite().ordinal()) {
                dir = ForgeDirection.UP;
            }

            int x2 = x + dir.offsetX;
            int y2 = y + dir.offsetY;
            int z2 = z + dir.offsetZ;
            Block block = world.getBlock(x2, y2, z2);
            if (block instanceof BlockRedcrystal) {
                connectedPos = new BlockCoord(x2, y2, z2);
                if (this.getOrientation(world, x2, y2, z2) == dir.getOpposite().ordinal()) {
                    overheadConnection = true;
                }
            } else if (block.isAir(world, x2, y2, z2)) {
                BlockWithPos bwp = RedstoneCalc.getORCAroundCorner(world, x2, y2, z2, orientation, dir);
                if (bwp != null && bwp.block instanceof BlockRedcrystal) {
                    block = bwp.block;
                    connectedPos = new BlockCoord(bwp.x, bwp.y, bwp.z);
                    connectionAroundCorner = true;
                }
            } else if (dir != orientationDir) {
                x2 = x + orientationDir.offsetX;
                y2 = y + orientationDir.offsetY;
                z2 = z + orientationDir.offsetZ;
                block = world.getBlock(x2, y2, z2);
                if (block instanceof BlockRedcrystal) {
                    connectedPos = new BlockCoord(x2, y2, z2);
                }
            }

            this.setConnection(world, x, y, z, connects, side);
            if (connectedPos != null) {
                if (alterOtherConnection
                    && !overheadConnection
                    && block instanceof BlockRedcrystal
                    && ((BlockRedcrystal)block).allowNeighborToChangeConnection(world, x2, y2, z2)) {
                    int otherSide = TjUtil.getOppositeSide(side);
                    if (connectionAroundCorner) {
                        otherSide = orientation;
                    }

                    ((BlockRedcrystal)block).setConnection(world, connectedPos.x, connectedPos.y, connectedPos.z, connects, otherSide);
                    this.updateAndPropagateChanges(world, x, y, z, true, false, true, false);
                } else if (!connects) {
                    ((BlockRedcrystal)block).updateAndPropagateChanges(world, connectedPos.x, connectedPos.y, connectedPos.z, true, false, true, false);
                }
            }

            return true;
        }
    }

    public boolean allowNeighborToChangeConnection(World world, int x, int y, int z) {
        return true;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 start, Vec3 end) {
        Block block = world.getBlock(x, y, z);
        if (block != this) {
            return super.collisionRayTrace(world, x, y, z, start, end);
        } else {
            List<IndexedCuboid6> cuboids = new LinkedList<>();
            ((BlockRedcrystal)block).addTraceableCuboids(world, x, y, z, cuboids);
            thaumcraft.codechicken.lib.vec.BlockCoord pos = new thaumcraft.codechicken.lib.vec.BlockCoord(x, y, z);
            return this.rayTracer.rayTraceCuboids(new Vector3(start), new Vector3(end), cuboids, pos, this);
        }
    }

    public void addTraceableCuboids(IBlockAccess blockaccess, int x, int y, int z, List<IndexedCuboid6> cuboids) {
        double cmin = 0.4;
        double cmax = 0.6;
        switch (this.getOrientation(blockaccess, x, y, z)) {
            case 0:
                this.addIndexedCuboid(x, y, z, cuboids, 6, cmin, 0.8, cmin, cmax, 1.0, cmax);
                this.addIndexedCuboid(x, y, z, cuboids, 2, 0.45, 0.9, 0.25, 0.55, 1.0, 0.35);
                this.addIndexedCuboid(x, y, z, cuboids, 3, 0.45, 0.9, 0.65, 0.55, 1.0, 0.75);
                this.addIndexedCuboid(x, y, z, cuboids, 5, 0.65, 0.9, 0.45, 0.75, 1.0, 0.55);
                this.addIndexedCuboid(x, y, z, cuboids, 4, 0.25, 0.9, 0.45, 0.35, 1.0, 0.55);
                this.addIndexedCuboid(x, y, z, cuboids, 7, 0.25, 0.995, 0.25, 0.75, 1.0, 0.75);
                break;
            case 1:
            default:
                this.addIndexedCuboid(x, y, z, cuboids, 6, cmin, 0.0, cmin, cmax, 0.2, cmax);
                this.addIndexedCuboid(x, y, z, cuboids, 2, 0.45, 0.0, 0.25, 0.55, 0.1, 0.35);
                this.addIndexedCuboid(x, y, z, cuboids, 3, 0.45, 0.0, 0.65, 0.55, 0.1, 0.75);
                this.addIndexedCuboid(x, y, z, cuboids, 5, 0.65, 0.0, 0.45, 0.75, 0.1, 0.55);
                this.addIndexedCuboid(x, y, z, cuboids, 4, 0.25, 0.0, 0.45, 0.35, 0.1, 0.55);
                this.addIndexedCuboid(x, y, z, cuboids, 7, 0.25, 0.0, 0.25, 0.75, 0.005, 0.75);
                break;
            case 2:
                this.addIndexedCuboid(x, y, z, cuboids, 6, cmin, cmin, 0.8, cmax, cmax, 1.0);
                this.addIndexedCuboid(x, y, z, cuboids, 1, 0.45, 0.65, 0.9, 0.55, 0.75, 1.0);
                this.addIndexedCuboid(x, y, z, cuboids, 0, 0.45, 0.25, 0.9, 0.55, 0.35, 1.0);
                this.addIndexedCuboid(x, y, z, cuboids, 4, 0.25, 0.45, 0.9, 0.35, 0.55, 1.0);
                this.addIndexedCuboid(x, y, z, cuboids, 5, 0.65, 0.45, 0.9, 0.75, 0.55, 1.0);
                this.addIndexedCuboid(x, y, z, cuboids, 7, 0.25, 0.25, 0.995, 0.75, 0.75, 1.0);
                break;
            case 3:
                this.addIndexedCuboid(x, y, z, cuboids, 6, cmin, cmin, 0.0, cmax, cmax, 0.2);
                this.addIndexedCuboid(x, y, z, cuboids, 1, 0.45, 0.65, 0.0, 0.55, 0.75, 0.1);
                this.addIndexedCuboid(x, y, z, cuboids, 0, 0.45, 0.25, 0.0, 0.55, 0.35, 0.1);
                this.addIndexedCuboid(x, y, z, cuboids, 4, 0.25, 0.45, 0.0, 0.35, 0.55, 0.1);
                this.addIndexedCuboid(x, y, z, cuboids, 5, 0.65, 0.45, 0.0, 0.75, 0.55, 0.1);
                this.addIndexedCuboid(x, y, z, cuboids, 7, 0.25, 0.25, 0.0, 0.75, 0.75, 0.005);
                break;
            case 4:
                this.addIndexedCuboid(x, y, z, cuboids, 6, 0.8, cmin, cmin, 1.0, cmax, cmax);
                this.addIndexedCuboid(x, y, z, cuboids, 1, 0.9, 0.65, 0.45, 1.0, 0.75, 0.55);
                this.addIndexedCuboid(x, y, z, cuboids, 0, 0.9, 0.25, 0.45, 1.0, 0.35, 0.55);
                this.addIndexedCuboid(x, y, z, cuboids, 2, 0.9, 0.45, 0.25, 1.0, 0.55, 0.35);
                this.addIndexedCuboid(x, y, z, cuboids, 3, 0.9, 0.45, 0.65, 1.0, 0.55, 0.75);
                this.addIndexedCuboid(x, y, z, cuboids, 7, 0.995, 0.25, 0.25, 1.0, 0.75, 0.75);
                break;
            case 5:
                this.addIndexedCuboid(x, y, z, cuboids, 6, 0.0, cmin, cmin, 0.2, cmax, cmax);
                this.addIndexedCuboid(x, y, z, cuboids, 1, 0.0, 0.65, 0.45, 0.1, 0.75, 0.55);
                this.addIndexedCuboid(x, y, z, cuboids, 0, 0.0, 0.25, 0.45, 0.1, 0.35, 0.55);
                this.addIndexedCuboid(x, y, z, cuboids, 2, 0.0, 0.45, 0.25, 0.1, 0.55, 0.35);
                this.addIndexedCuboid(x, y, z, cuboids, 3, 0.0, 0.45, 0.65, 0.1, 0.55, 0.75);
                this.addIndexedCuboid(x, y, z, cuboids, 7, 0.0, 0.25, 0.25, 0.005, 0.75, 0.75);
        }
    }

    protected void addIndexedCuboid(
        int x, int y, int z, List<IndexedCuboid6> cuboids, int i, double minX, double minY, double minZ, double maxX, double maxY, double maxZ
    ) {
        cuboids.add(new IndexedCuboid6(i, new Cuboid6(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ)));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null || player.getCurrentEquippedItem() == null || !(player.getCurrentEquippedItem().getItem() instanceof ItemWandCasting)) {
            this.setNoWandBlockBounds(world, x, y, z);
        }

        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    protected void setNoWandBlockBounds(World world, int x, int y, int z) {
        int orientation = this.getOrientation(world, x, y, z);
        switch (orientation) {
            case 0:
                this.setBlockBounds(0.25F, 0.8F, 0.25F, 0.75F, 1.0F, 0.75F);
                break;
            case 1:
            default:
                this.setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 0.2F, 0.75F);
                break;
            case 2:
                this.setBlockBounds(0.25F, 0.25F, 0.8F, 0.75F, 0.75F, 1.0F);
                break;
            case 3:
                this.setBlockBounds(0.25F, 0.25F, 0.0F, 0.75F, 0.75F, 0.2F);
                break;
            case 4:
                this.setBlockBounds(0.8F, 0.25F, 0.25F, 1.0F, 0.75F, 0.75F);
                break;
            case 5:
                this.setBlockBounds(0.0F, 0.25F, 0.25F, 0.2F, 0.75F, 0.75F);
        }
    }
}
