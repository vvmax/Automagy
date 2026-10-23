package tuhljin.automagy.entities;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.monster.EntityWisp;
import tuhljin.automagy.lib.compat.CompatibilityManager;

public class EntityWispNether extends EntityWisp {
    public EntityWispNether(World world) {
        super(world);
        this.experienceValue++;
        this.isImmuneToFire = true;
    }

    @Override
    protected void updateEntityActionState() {
        if (!this.worldObj.isRemote && Aspect.getAspect(this.getType()) == null) {
            Aspect aspect = Aspect.FIRE;
            if (CompatibilityManager.ForbiddenMagicAspect.NETHER != null && this.getRNG().nextInt(4) == 0) {
                aspect = CompatibilityManager.ForbiddenMagicAspect.NETHER;
            }

            this.setType(aspect.getTag());
        }

        if (this.ticksExisted % 60 == 0 && this.getTargetedEntity() == null) {
            Entity e = this.worldObj.getClosestVulnerablePlayerToEntity(this, 16.0);
            if (e != null) {
                this.setTargetedEntity(e);
            }
        }

        super.updateEntityActionState();
    }

    @Override
    protected boolean isValidLightLevel() {
        return true;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30.0);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(5.0);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        if (!this.worldObj.isRemote) {
            this.worldObj.newExplosion(this, this.posX, this.posY, this.posZ, 1.5F, true, false);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.worldObj.isRemote) {
            Thaumcraft.proxy
                .drawGenericParticles(
                    this.worldObj,
                    this.prevPosX + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F,
                    this.prevPosY + this.height / 2.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F,
                    this.prevPosZ + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F,
                    0.0,
                    0.0,
                    0.0,
                    1.0F,
                    1.0F,
                    1.0F,
                    0.8F,
                    false,
                    151,
                    9,
                    1,
                    7 + this.rand.nextInt(5),
                    0,
                    1.0F + this.rand.nextFloat() * 0.5F
                );
            if (this.ticksExisted % 5 == 0) {
                this.worldObj
                    .spawnParticle(
                        "smoke",
                        this.prevPosX + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F,
                        this.prevPosY + this.height / 2.0F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F,
                        this.prevPosZ + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F,
                        0.0,
                        0.0,
                        0.0
                    );
                this.worldObj
                    .spawnParticle(
                        "flame",
                        this.prevPosX + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F,
                        this.prevPosY + this.height / 2.0F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F,
                        this.prevPosZ + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F,
                        0.0,
                        0.0,
                        0.0
                    );
            }
        }
    }

    @Override
    protected Item getDropItem() {
        return super.getDropItem();
    }

    protected Entity getTargetedEntity() {
        return ReflectionHelper.getPrivateValue(EntityWisp.class, this, "targetedEntity");
    }

    protected void setTargetedEntity(Entity e) {
        ReflectionHelper.setPrivateValue(EntityWisp.class, this, e, "targetedEntity");
    }
}
