package tuhljin.automagy.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectSourceHelper;
import thaumcraft.common.entities.golems.EntityGolemBase;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.entities.EntityAICharmWalk;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.network.MessageParticles;

public class TileEntityMobLure extends ModTileEntity {
    public static int VERTICAL_CALC_FREQUENCY = 60;
    public static int CHARM_FREQUENCY = 2;
    public static double RANGE_HORIZONTAL = 8.5;
    public static double RANGE_HORIZONTAL_BOOSTED = 32.5;
    public static int RANGE_BOOST_TIME = 400;
    public static int MIN_TOTEM_BLOCKS = 2;
    public static int MAX_TOTEM_BLOCKS_EACHDIR = 4;
    public static WeakHashMap<UUID, Integer> secondaryCharmedEntitiesStrength = new WeakHashMap<>();
    public static WeakHashMap<UUID, Long> secondaryCharmedEntitiesTime = new WeakHashMap<>();
    public static WeakHashMap<UUID, WorldSpecificCoordinates> secondaryCharmedEntitiesCoord = new WeakHashMap<>();
    public static HashMap<Integer, HashSet<BlockCoord>> lureLocations = new HashMap<>();
    private boolean addedToMap = false;
    public int numTotemBlocks;
    public int yTop;
    public int yBottom;
    public int nextVerticalCalc;
    public int nextCharm = 0;
    public int timeRangeUp = 0;

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote && this.isEnabled()) {
            if (!this.addedToMap) {
                int dimension = this.worldObj.provider.dimensionId;
                HashSet<BlockCoord> bcs;
                if (lureLocations.containsKey(dimension)) {
                    bcs = lureLocations.get(dimension);
                } else {
                    bcs = new HashSet<>();
                    lureLocations.put(dimension, bcs);
                }

                bcs.add(new BlockCoord(this));
                this.addedToMap = true;
                this.nextCharm = this.worldObj.rand.nextInt(CHARM_FREQUENCY + 5) + 1;
            }

            int prevTimeRangeUp = this.timeRangeUp;
            if (this.timeRangeUp > 0) {
                this.timeRangeUp--;
            }

            this.nextVerticalCalc--;
            if (this.nextVerticalCalc < 1 && !this.calcVerticalLimits()) {
                this.tryDisable();
                return;
            }

            this.nextCharm--;
            if (this.nextCharm < 1) {
                if (this.timeRangeUp == 0 && AspectSourceHelper.drainEssentia(this, Aspect.HUNGER, ForgeDirection.UNKNOWN, 8)) {
                    this.timeRangeUp = RANGE_BOOST_TIME;
                }

                this.charmNearbyEntities();
                this.nextCharm = CHARM_FREQUENCY;
                if (this.worldObj.rand.nextBoolean()) {
                    this.nextCharm++;
                }
            }

            if (prevTimeRangeUp < 1) {
                if (this.timeRangeUp > 0) {
                    this.markDirty();
                } else if (prevTimeRangeUp != this.timeRangeUp) {
                    this.markDirty(false);
                }
            } else if (this.timeRangeUp < 1) {
                this.markDirty();
            } else if (prevTimeRangeUp != this.timeRangeUp) {
                this.markDirty(false);
            }
        }
    }

    @Override
    public void invalidate() {
        if (this.worldObj != null && !this.worldObj.isRemote) {
            int dimension = this.worldObj.provider.dimensionId;
            if (lureLocations.containsKey(dimension)) {
                HashSet<BlockCoord> bcs = lureLocations.get(dimension);
                bcs.remove(new BlockCoord(this));
            }
        }

        super.invalidate();
    }

    public boolean isEnabled() {
        return this.getBlockMetadata() == 1;
    }

    public boolean tryEnable() {
        if (!this.isEnabled() && this.calcVerticalLimits()) {
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 1, 3);
            this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "thaumcraft:brain", 0.8F, this.worldObj.rand.nextFloat() * 0.3F + 0.2F);
            MessageParticles.sendToClients(this.worldObj, this.xCoord, this.yCoord, this.zCoord, 0, 0, 0, (short)10);
            return true;
        } else {
            return false;
        }
    }

    public boolean tryDisable() {
        if (this.isEnabled()) {
            this.timeRangeUp = 0;
            this.markDirty();
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 0, 3);
            this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "thaumcraft:wandfail", 0.5F, 0.4F);
            return true;
        } else {
            return false;
        }
    }

    public static TileEntityMobLure getLureNear(World world, double x, double y, double z) {
        int dimension = world.provider.dimensionId;
        if (!lureLocations.containsKey(dimension)) {
            return null;
        } else {
            HashSet<BlockCoord> bcs = lureLocations.get(dimension);
            TileEntityMobLure closest = null;
            double closestDistance = Double.MAX_VALUE;

            for (BlockCoord bc : bcs) {
                if (TjUtil.isChunkLoaded(world, bc.x, bc.z)) {
                    TileEntity te = world.getTileEntity(bc.x, bc.y, bc.z);
                    if (te instanceof TileEntityMobLure) {
                        TileEntityMobLure teML = (TileEntityMobLure)te;
                        if (teML.isEnabled()) {
                            double d = teML.getDistanceIfInRange(x, y, z);
                            if (d != -1.0 && d < closestDistance) {
                                closest = teML;
                                closestDistance = d;
                            }
                        }
                    }
                }
            }

            return closest;
        }
    }

    public double getDistanceIfInRange(double x, double y, double z) {
        double range = this.getRange();
        double d = TjUtil.getDistanceBetweenPoints_squared(this.xCoord + 0.5, this.yCoord, this.zCoord + 0.5, x, y, z);
        return d <= range * range ? d : -1.0;
    }

    public double getRange() {
        return this.timeRangeUp > 0 ? RANGE_HORIZONTAL_BOOSTED : RANGE_HORIZONTAL;
    }

    public boolean calcVerticalLimits() {
        this.nextVerticalCalc = VERTICAL_CALC_FREQUENCY;
        int prevTop = this.yTop;
        int prevBottom = this.yBottom;
        int numAbove = 0;
        int numBelow = 0;
        this.yTop = this.yCoord;

        while (ThaumcraftExtension.blockIsObsidianTotem(this.worldObj, this.xCoord, this.yTop + 1, this.zCoord)) {
            this.yTop++;
            if (++numAbove >= MAX_TOTEM_BLOCKS_EACHDIR) {
                break;
            }
        }

        this.yBottom = this.yCoord;

        while (ThaumcraftExtension.blockIsObsidianTotem(this.worldObj, this.xCoord, this.yBottom - 1, this.zCoord)) {
            this.yBottom--;
            if (++numBelow >= MAX_TOTEM_BLOCKS_EACHDIR) {
                break;
            }
        }

        this.numTotemBlocks = numAbove + numBelow;
        if (prevBottom != this.yBottom || prevTop != this.yTop) {
            this.markDirty();
        }

        return this.numTotemBlocks >= MIN_TOTEM_BLOCKS;
    }

    public void charmNearbyEntities() {
        double range = this.getRange();
        List<EntityLiving> targets = this.getTargetEntities(range);
        int strength = this.getLureStrength();
        WorldSpecificCoordinates coord = new WorldSpecificCoordinates(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
        long time = System.currentTimeMillis();

        for (EntityLiving e : targets) {
            if (!EntityAICharmWalk.addAIToEntity(e, this.worldObj, this.xCoord, this.yCoord, this.zCoord, range + 1.0, strength, this)
                && e instanceof EntityCreature) {
                UUID id = e.getUniqueID();
                if (secondaryCharmedEntitiesStrength.containsKey(id) && !secondaryCharmedEntitiesCoord.get(id).equals(coord)) {
                    int luredStr = secondaryCharmedEntitiesStrength.get(id);
                    if (luredStr >= strength) {
                        long t = secondaryCharmedEntitiesTime.get(id);
                        if (time - t < 1000L) {
                            continue;
                        }
                    }
                }

                secondaryCharmedEntitiesStrength.put(id, strength);
                secondaryCharmedEntitiesTime.put(id, time);
                secondaryCharmedEntitiesCoord.put(id, coord);
                PathNavigate nav = e.getNavigator();
                PathEntity path = nav.getPathToXYZ(this.xCoord + 0.5, this.yCoord, this.zCoord + 0.5);
                ((EntityCreature)e).setPathToEntity(path);
                double x = e.posX + e.motionX;
                double z = e.posZ + e.motionZ;
                if (Math.abs(this.xCoord - x) < Math.abs(this.xCoord - e.posX) || Math.abs(this.zCoord - z) < Math.abs(this.zCoord - e.posZ)) {
                    e.moveEntityWithHeading(0.0F, 0.8F);
                } else if (this.worldObj.rand.nextInt(5) == 0) {
                    e.moveEntityWithHeading(0.0F, 0.5F);
                }

                e.addPotionEffect(new PotionEffect(Potion.blindness.id, CHARM_FREQUENCY + 1, 0));
            }
        }
    }

    private int getLureStrength() {
        return this.numTotemBlocks;
    }

    public List<EntityLiving> getTargetEntities(double rangeHorizontal) {
        double x = this.xCoord + 0.5;
        double z = this.zCoord + 0.5;
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
            x - rangeHorizontal, this.yBottom, z - rangeHorizontal, x + rangeHorizontal, this.yTop + 1.0, z + rangeHorizontal
        );
        List<EntityLiving> entities = this.worldObj.getEntitiesWithinAABB(EntityLiving.class, box);
        List<EntityLiving> targets = new ArrayList<>();

        for (EntityLiving e : entities) {
            if ((e instanceof IMob || e instanceof IAnimals)
                && !(e instanceof EntityGolemBase)
                && !(e instanceof IBossDisplayData)
                && !(e instanceof IEntityMultiPart)) {
                targets.add(e);
            }
        }

        return targets;
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setInteger("timeRangeUp", this.timeRangeUp);
        nbttagcompound.setInteger("yTop", this.yTop);
        nbttagcompound.setInteger("yBottom", this.yBottom);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.timeRangeUp = nbttagcompound.getInteger("timeRangeUp");
        this.yTop = nbttagcompound.getInteger("yTop");
        this.yBottom = nbttagcompound.getInteger("yBottom");
    }
}
