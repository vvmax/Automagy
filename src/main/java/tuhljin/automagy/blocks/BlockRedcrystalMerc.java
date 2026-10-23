package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.codechicken.lib.raytracer.IndexedCuboid6;
import thaumcraft.common.Thaumcraft;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.lib.RedstoneCalc;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.tiles.TileEntityRedcrystalMerc;

public class BlockRedcrystalMerc extends BlockRedcrystalLarge {
    private WorldSpecificCoordinates lastCoord = null;

    public BlockRedcrystalMerc(String name) {
        super(name);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.32F, 1.0F);
        this.setStepSound(soundTypeStone);
        this.setHardness(2.5F);
        this.setHarvestLevel("pickaxe", 0);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        TileEntityRedcrystalMerc te = new TileEntityRedcrystalMerc((short)this.nextTEOrientation, this.nextTENoConnections);
        this.nextTEOrientation = -1;
        this.nextTENoConnections = false;
        return te;
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return !TjUtil.isAcceptableSurfaceForBlock(world, x, y - 1, z, true, true, false) ? false : world.getBlock(x, y, z).isReplaceable(world, x, y, z);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
        return TjUtil.isAcceptableSurfaceForBlock(world, x, y - 1, z, true, true, false);
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        return TjUtil.isAcceptableSurfaceForBlock(world, x, y - 1, z, true, true, false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        TileEntityRedcrystalMerc te = null;

        try {
            te = (TileEntityRedcrystalMerc)world.getTileEntity(x, y, z);
        } catch (Exception var17) {
        }

        if (te != null && te.hasValidMirrorConnection()) {
            int strength = this.getRedstoneSignalStrength(world, x, y, z, true);
            boolean power = strength > 0;
            int color = power ? 8 : 6;
            if (power) {
                boolean mirrorIsSource = te.strengthWithoutMirror != strength;
                if (rand.nextInt(4) == 0) {
                    int sx;
                    int sy;
                    int sz;
                    int dx;
                    int dy;
                    int dz;
                    if (mirrorIsSource) {
                        sx = te.mirrorX;
                        sy = te.mirrorY;
                        sz = te.mirrorZ;
                        dx = x;
                        dy = y;
                        dz = z;
                    } else {
                        sx = x;
                        sy = y;
                        sz = z;
                        dx = te.mirrorX;
                        dy = te.mirrorY;
                        dz = te.mirrorZ;
                    }

                    Automagy.proxy.ghostlyBallStreamFX(world, sx + 0.5F, sy + 0.5F, sz + 0.5F, dx + 0.5F, dy + 0.5F, dz + 0.5F, 0.05F, 8, false, 0.0F);
                }

                Thaumcraft.proxy
                    .sparkle(
                        x + 0.2F + rand.nextFloat() * 0.6F,
                        y + (mirrorIsSource ? 0.8F : 0.5F),
                        z + 0.2F + rand.nextFloat() * 0.6F,
                        1.0F,
                        color,
                        mirrorIsSource ? 0.1F : -0.1F
                    );
            } else {
                Thaumcraft.proxy.sparkle(x + 0.2F + rand.nextFloat() * 0.6F, y + 0.2F, z + 0.2F + rand.nextFloat() * 0.6F, 1.0F, color, -0.1F);
            }
        }

        super.randomDisplayTick(world, x, y, z, rand);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
        TileEntityRedcrystalMerc te = null;

        try {
            te = (TileEntityRedcrystalMerc)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        this.lastCoord = te.mirrorY == -1 ? null : new WorldSpecificCoordinates(te.mirrorDim, te.mirrorX, te.mirrorY, te.mirrorZ);
        if (!world.isRemote) {
            te.removeFromMirrorNetwork();
        }

        super.breakBlock(world, x, y, z, block, p_149749_6_);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<>();
        Item item = this.getItemDropped(metadata, world.rand, fortune);
        ItemStack stack = new ItemStack(item, 1, this.damageDropped(metadata));
        TileEntityRedcrystalMerc te = null;

        try {
            te = (TileEntityRedcrystalMerc)world.getTileEntity(x, y, z);
        } catch (Exception var12) {
        }

        if (te != null) {
            if (te.mirrorY != -1) {
                stack.setTagInfo("mirrorX", new NBTTagInt(te.mirrorX));
                stack.setTagInfo("mirrorY", new NBTTagInt(te.mirrorY));
                stack.setTagInfo("mirrorZ", new NBTTagInt(te.mirrorZ));
                stack.setTagInfo("mirrorDim", new NBTTagInt(te.mirrorDim));
                stack.setTagInfo("mirrorDimName", new NBTTagString(DimensionManager.getProvider(te.mirrorDim).getDimensionName()));
            }
        } else if (this.lastCoord != null) {
            stack.setTagInfo("mirrorX", new NBTTagInt(this.lastCoord.x));
            stack.setTagInfo("mirrorY", new NBTTagInt(this.lastCoord.y));
            stack.setTagInfo("mirrorZ", new NBTTagInt(this.lastCoord.z));
            stack.setTagInfo("mirrorDim", new NBTTagInt(this.lastCoord.dim));
            stack.setTagInfo("mirrorDimName", new NBTTagString(DimensionManager.getProvider(this.lastCoord.dim).getDimensionName()));
        }

        this.lastCoord = null;
        drops.add(stack);
        return drops;
    }

    @Override
    protected RedstoneCalc.PowerResult calculateRedstonePowerAt(World world, int x, int y, int z, int orientation) {
        int receivingStrength = 0;

        TileEntityRedcrystalMerc te;
        try {
            te = (TileEntityRedcrystalMerc)world.getTileEntity(x, y, z);
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
            te.strengthWithoutMirror = result.strength;
            te.markDirty(false);
            return receivingStrength > result.strength ? new RedstoneCalc.PowerResult(receivingStrength, ForgeDirection.UNKNOWN) : result;
        } else {
            te.strengthWithoutMirror = 0;
            te.markDirty(false);
            return receivingStrength > 0 ? new RedstoneCalc.PowerResult(receivingStrength, ForgeDirection.UNKNOWN) : null;
        }
    }

    @Override
    public void updateAndPropagateChanges(
        World world, int x, int y, int z, boolean checkStrength, boolean calledByNeighborWire, boolean forcePropagate, boolean immediateNeighborUpdates
    ) {
        super.updateAndPropagateChanges(world, x, y, z, checkStrength, calledByNeighborWire, forcePropagate, immediateNeighborUpdates);
        if (!world.isRemote) {
            TileEntityRedcrystalMerc te = null;
            int receivingStrength = 0;

            try {
                te = (TileEntityRedcrystalMerc)world.getTileEntity(x, y, z);
            } catch (Exception var12) {
            }

            if (te != null) {
                te.updateMirrorNetwork();
            }
        }
    }

    @Override
    public void addTraceableCuboids(IBlockAccess blockaccess, int x, int y, int z, List<IndexedCuboid6> cuboids) {
        double cmin = 0.36;
        double cmax = 0.64;
        double a = 0.44;
        double b = 0.9;
        double c = 0.8;
        double d = 0.2;
        double e = 0.0;
        double f = 0.56;
        double g = 1.0;
        double h = 0.3;
        this.addIndexedCuboid(x, y, z, cuboids, 6, cmin, 0.3, cmin, cmax, 0.8, cmax);
        this.addIndexedCuboid(x, y, z, cuboids, 2, a, 0.125, e, f, h, d);
        this.addIndexedCuboid(x, y, z, cuboids, 3, a, 0.125, c, f, h, g);
        this.addIndexedCuboid(x, y, z, cuboids, 5, c, 0.125, a, g, h, f);
        this.addIndexedCuboid(x, y, z, cuboids, 4, e, 0.125, a, d, h, f);
        this.addIndexedCuboid(x, y, z, cuboids, 7, 0.0, 0.0, 0.0, 1.0, 0.125, 1.0);
    }

    @Override
    protected void setNoWandBlockBounds(World world, int x, int y, int z) {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return AxisAlignedBB.getBoundingBox(x, y, z, x + 1.0, y + 0.125, z + 1.0);
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB p_149743_5_, List p_149743_6_, Entity p_149743_7_) {
        super.addCollisionBoxesToList(world, x, y, z, p_149743_5_, p_149743_6_, p_149743_7_);
        if (p_149743_7_ instanceof EntityPlayer) {
            AxisAlignedBB axisalignedbb1 = AxisAlignedBB.getBoundingBox(x + 0.36, y, z + 0.36, x + 0.64, y + 0.8, z + 0.64);
            if (axisalignedbb1 != null && p_149743_5_.intersectsWith(axisalignedbb1)) {
                p_149743_6_.add(axisalignedbb1);
            }
        }
    }
}
