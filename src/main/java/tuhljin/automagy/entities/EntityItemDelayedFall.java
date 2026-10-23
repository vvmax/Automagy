package tuhljin.automagy.entities;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.util.Random;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import thaumcraft.common.entities.EntitySpecialItem;

public class EntityItemDelayedFall extends EntitySpecialItem implements IEntityAdditionalSpawnData {
    public static int DELAY_AFTER_PLAYER_TOUCHED = 200;
    public int delayBeforeFall = -1;

    public EntityItemDelayedFall(World par1World) {
        super(par1World);
    }

    public EntityItemDelayedFall(World par1World, double par2, double par4, double par6, ItemStack par8ItemStack, int delay) {
        super(par1World, par2, par4, par6, par8ItemStack);
        this.delayBeforeFall = delay;
        NBTTagCompound nbt = this.getEntityData();
        nbt.setInteger("delayBeforeFall", this.delayBeforeFall);
    }

    @Override
    public void onUpdate() {
        if (this.delayBeforeCanPickup > 0) {
            if (this.motionY > 0.0) {
                this.motionY -= 0.04F;
            }
        } else {
            this.motionX *= 0.8;
            this.motionZ *= 0.8;
        }

        super.onUpdate();
        if (this.delayBeforeFall > 0) {
            this.delayBeforeFall--;
        }

        if (this.delayBeforeFall == 0 && !this.worldObj.isRemote) {
            EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, this.getEntityItem().copy());
            float f3 = 0.05F;
            entityitem.motionX = this.motionX;
            entityitem.motionY = this.motionY;
            entityitem.motionZ = this.motionZ;
            this.worldObj.spawnEntityInWorld(entityitem);
            this.setDead();
        }
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer player) {
        this.delayBeforeFall = Math.max(this.delayBeforeFall, DELAY_AFTER_PLAYER_TOUCHED);
        super.onCollideWithPlayer(player);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeInt(this.delayBeforeFall);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.delayBeforeFall = additionalData.readInt();
    }

    @Override
    public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
        NBTTagCompound nbt = this.getEntityData();
        nbt.setInteger("delayBeforeFall", this.delayBeforeFall);
        super.writeToNBT(par1nbtTagCompound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound) {
        super.readEntityFromNBT(par1nbtTagCompound);
        NBTTagCompound nbt = this.getEntityData();
        this.delayBeforeFall = nbt.getInteger("delayBeforeFall");
    }

    public static EntityItemDelayedFall spawn(ItemStack stack, World world, double x, double y, double z) {
        EntityItemDelayedFall entityitem = new EntityItemDelayedFall(world, x, y, z, stack, DELAY_AFTER_PLAYER_TOUCHED);
        entityitem.delayBeforeCanPickup = 1;
        entityitem.motionY = 0.1000000014901161;
        entityitem.motionX = 0.0;
        entityitem.motionZ = 0.0;
        world.spawnEntityInWorld(entityitem);
        return entityitem;
    }

    public static EntityItemDelayedFall toss(ItemStack stack, EntityPlayer player) {
        EntityItemDelayedFall entityitem = spawn(stack, player.worldObj, player.posX, player.posY + Math.max(player.getEyeHeight() * 0.5, 0.25), player.posZ);
        Random rand = player.worldObj.rand;
        float f = 0.3F;
        entityitem.motionX = -MathHelper.sin(player.rotationYaw / 180.0F * (float) Math.PI)
            * MathHelper.cos(player.rotationPitch / 180.0F * (float) Math.PI)
            * f;
        entityitem.motionZ = MathHelper.cos(player.rotationYaw / 180.0F * (float) Math.PI)
            * MathHelper.cos(player.rotationPitch / 180.0F * (float) Math.PI)
            * f;
        entityitem.motionY = -MathHelper.sin(player.rotationPitch / 180.0F * (float) Math.PI) * f + 0.1F;
        f = 0.02F;
        float f1 = rand.nextFloat() * (float) Math.PI * 2.0F;
        f *= rand.nextFloat();
        entityitem.motionX = entityitem.motionX + Math.cos(f1) * f;
        entityitem.motionY = entityitem.motionY + (rand.nextFloat() - rand.nextFloat()) * 0.1F;
        entityitem.motionZ = entityitem.motionZ + Math.sin(f1) * f;
        return entityitem;
    }
}
