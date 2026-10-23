package tuhljin.automagy.entities;

import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.tiles.TileEntityMobLure;

public class EntityAICharmWalk extends EntityAIBase {
    private final EntityLiving thisEntity;
    private final float maxSpeed;
    private float currentSpeed;
    public static int TARGET_IGNORABLE_RANGE = 100;
    public static int PRIORITY = -1;
    public static WeakHashMap<UUID, EntityAICharmWalk> charmedEntities = new WeakHashMap<>();
    protected WorldSpecificCoordinates coord;
    protected WorldSpecificCoordinates coordCooldown = null;
    protected int cooldown = 0;
    protected int prevX;
    protected int prevZ;
    protected int checkStuck = 0;
    public double maxLureRangeSquared;
    public int lureStrength;

    public EntityAICharmWalk(EntityLiving p_i1620_1_, float p_i1620_2_, WorldSpecificCoordinates coord, double rangeSquared, int lureStrength) {
        this.thisEntity = p_i1620_1_;
        this.maxSpeed = p_i1620_2_;
        this.coord = coord;
        this.maxLureRangeSquared = rangeSquared;
        this.lureStrength = lureStrength;
        this.setMutexBits(3);
    }

    @Override
    public void startExecuting() {
        this.currentSpeed = 0.0F;
    }

    @Override
    public void resetTask() {
        this.currentSpeed = 0.0F;
    }

    @Override
    public boolean shouldExecute() {
        return this.shouldExecuteHelper(true);
    }

    private boolean shouldExecuteHelper(boolean doCount) {
        if (this.cooldown > 0) {
            if (doCount) {
                this.cooldown--;
                if (this.cooldown < 1) {
                    this.coordCooldown = null;
                }
            }

            if (this.coord.equals(this.coordCooldown)) {
                return false;
            }
        }

        int x = (int)this.thisEntity.posX;
        int y = (int)this.thisEntity.posY;
        int z = (int)this.thisEntity.posZ;
        if (x == this.prevX && z == this.prevZ) {
            if (doCount) {
                this.checkStuck++;
            }

            if (this.checkStuck > 140) {
                if (doCount) {
                    this.cooldown = 100;
                    this.coordCooldown = this.coord;
                    this.checkStuck = 0;
                }

                return false;
            }
        } else if (doCount) {
            this.checkStuck = 0;
            this.prevX = x;
            this.prevZ = z;
        }

        return this.thisEntity.isEntityAlive()
            && this.thisEntity.worldObj.provider.dimensionId == this.coord.dim
            && ModBlocks.mobLure
                .canLureAffectEntity(this.thisEntity.worldObj, this.coord.x, this.coord.y, this.coord.z, this.thisEntity, this.maxLureRangeSquared)
            && attackTargetIgnorable(this.thisEntity);
    }

    @Override
    public void updateTask() {
        this.thisEntity.getMoveHelper().setMoveTo(this.coord.x + 0.5, this.coord.y, this.coord.z + 0.5, 0.1);
        EntityLiving entitycreature = this.thisEntity;
        if (this.currentSpeed < this.maxSpeed) {
            this.currentSpeed = this.currentSpeed + (this.maxSpeed - this.currentSpeed) * 0.01F;
        }

        if (this.currentSpeed > this.maxSpeed) {
            this.currentSpeed = this.maxSpeed;
        }

        int i = MathHelper.floor_double(this.thisEntity.posX);
        int j = MathHelper.floor_double(this.thisEntity.posY);
        int k = MathHelper.floor_double(this.thisEntity.posZ);
        float f1 = this.currentSpeed;
        float f2 = 0.91F;
        if (this.thisEntity.onGround) {
            f2 = this.thisEntity.worldObj.getBlock(MathHelper.floor_float(i), MathHelper.floor_float(j) - 1, MathHelper.floor_float(k)).slipperiness * 0.91F;
        }

        float f3 = 0.16277136F / (f2 * f2 * f2);
        float f4 = MathHelper.sin(entitycreature.rotationYaw * (float) Math.PI / 180.0F);
        float f5 = MathHelper.cos(entitycreature.rotationYaw * (float) Math.PI / 180.0F);
        float f6 = entitycreature.getAIMoveSpeed() * f3;
        float f7 = Math.max(f1, 1.0F);
        f7 = f6 / f7;
        float f8 = f1 * f7;
        float f9 = -(f8 * f4);
        float f10 = f8 * f5;
        if (MathHelper.abs(f9) > MathHelper.abs(f10)) {
            if (f9 < 0.0F) {
                f9 -= this.thisEntity.width / 2.0F;
            }

            if (f9 > 0.0F) {
                f9 += this.thisEntity.width / 2.0F;
            }

            f10 = 0.0F;
        } else {
            f9 = 0.0F;
            if (f10 < 0.0F) {
                f10 -= this.thisEntity.width / 2.0F;
            }

            if (f10 > 0.0F) {
                f10 += this.thisEntity.width / 2.0F;
            }
        }

        int l = MathHelper.floor_double(this.thisEntity.posX + f9);
        int i1 = MathHelper.floor_double(this.thisEntity.posZ + f10);
        PathPoint pathpoint = new PathPoint(
            MathHelper.floor_float(this.thisEntity.width + 1.0F),
            MathHelper.floor_float(this.thisEntity.height + 1.0F),
            MathHelper.floor_float(this.thisEntity.width + 1.0F)
        );
        if (i != l || k != i1) {
            Block block = this.thisEntity.worldObj.getBlock(i, j, k);
            boolean flag = !this.isStairOrSlab(block)
                && (block.getMaterial() != Material.air || !this.isStairOrSlab(this.thisEntity.worldObj.getBlock(i, j - 1, k)));
            if (flag
                && PathFinder.canEntityStandAt(this.thisEntity, l, j, i1, pathpoint, false, false, true) == 0
                && PathFinder.canEntityStandAt(this.thisEntity, i, j + 1, k, pathpoint, false, false, true) == 1
                && PathFinder.canEntityStandAt(this.thisEntity, l, j + 1, i1, pathpoint, false, false, true) == 1) {
                entitycreature.getJumpHelper().setJumping();
            }
        }

        this.thisEntity.moveEntityWithHeading(0.0F, f1);
        this.thisEntity.addPotionEffect(new PotionEffect(Potion.blindness.id, 20, 0));
    }

    private boolean isStairOrSlab(Block p_151498_1_) {
        return p_151498_1_.getRenderType() == 10 || p_151498_1_ instanceof BlockSlab;
    }

    public static boolean attackTargetIgnorable(EntityLiving entity) {
        EntityLivingBase target = entity.getAttackTarget();
        if (target != null) {
            int revengeTimer = entity.getRevengeTimer();
            if (revengeTimer > 0 && entity.ticksExisted - revengeTimer < 100) {
                return entity.getDistanceSqToEntity(target) >= TARGET_IGNORABLE_RANGE;
            }
        }

        return true;
    }

    public static boolean addAIToEntity(EntityLiving entity, World world, int x, int y, int z, double range, int lureStrength, TileEntityMobLure teML) {
        UUID id = entity.getUniqueID();
        if (!charmedEntities.containsKey(id)) {
            if (!DataFromEntity.isAIEnabled(entity)) {
                return false;
            }

            WorldSpecificCoordinates coord = new WorldSpecificCoordinates(world, x, y, z);
            EntityAICharmWalk aiCharmedWalk = new EntityAICharmWalk(entity, 5.0F, coord, range * range, lureStrength);
            entity.tasks.addTask(PRIORITY, aiCharmedWalk);
            charmedEntities.put(id, aiCharmedWalk);
            if (entity instanceof EntityAgeable) {
                for (EntityAITaskEntry entityaitaskentry : entity.tasks.taskEntries) {
                    EntityAIBase entityaibase1 = entityaitaskentry.action;
                    if (entityaibase1 instanceof EntityAIFollowParent) {
                        entity.tasks.removeTask(entityaibase1);
                        break;
                    }
                }
            }
        } else {
            EntityAICharmWalk aiCharmedWalk = charmedEntities.get(id);
            WorldSpecificCoordinates coord = new WorldSpecificCoordinates(world, x, y, z);
            double rangeSquared = range * range;
            if (aiCharmedWalk.coord.equals(coord)) {
                if (aiCharmedWalk.maxLureRangeSquared != rangeSquared || aiCharmedWalk.lureStrength != lureStrength) {
                    aiCharmedWalk.maxLureRangeSquared = rangeSquared;
                    aiCharmedWalk.lureStrength = lureStrength;
                }
            } else if ((aiCharmedWalk.lureStrength < lureStrength || !aiCharmedWalk.shouldExecuteHelper(false))
                && !coord.equals(aiCharmedWalk.coordCooldown)
                && ModBlocks.mobLure.canActiveLureAffectEntity(teML, aiCharmedWalk.thisEntity, rangeSquared)) {
                aiCharmedWalk.coord = coord;
                aiCharmedWalk.cooldown = 200;
                aiCharmedWalk.maxLureRangeSquared = rangeSquared;
                int prev = aiCharmedWalk.lureStrength;
                aiCharmedWalk.lureStrength = lureStrength;
            }
        }

        return true;
    }
}
