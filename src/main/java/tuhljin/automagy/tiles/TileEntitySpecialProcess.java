package tuhljin.automagy.tiles;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.common.Thaumcraft;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.network.MessageParticles;

public class TileEntitySpecialProcess extends ModTileEntity {
    int completedActions = 0;
    int ticksSinceAction = 0;
    int blocksConsumed = 0;

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        this.ticksSinceAction++;
        if (this.worldObj.isRemote) {
            if (this.ticksSinceAction == 2) {
                Thaumcraft.proxy
                    .wispFX3(
                        this.worldObj,
                        this.xCoord + 0.5F + this.worldObj.rand.nextFloat() / 2.0F,
                        this.yCoord + 0.5F + this.worldObj.rand.nextFloat() / 2.0F,
                        this.zCoord + 0.5F + this.worldObj.rand.nextFloat() / 2.0F,
                        this.xCoord + 0.3F + this.worldObj.rand.nextFloat() * 0.4F,
                        this.yCoord + 0.5F,
                        this.zCoord + 0.3F + this.worldObj.rand.nextFloat() * 0.4F,
                        0.5F,
                        5,
                        true,
                        -0.025F
                    );
            } else if (this.ticksSinceAction >= 4) {
                Thaumcraft.proxy
                    .wispFX3(
                        this.worldObj,
                        this.xCoord + 0.5F - this.worldObj.rand.nextFloat() / 2.0F,
                        this.yCoord + 0.5F - this.worldObj.rand.nextFloat() / 2.0F,
                        this.zCoord + 0.5F - this.worldObj.rand.nextFloat() / 2.0F,
                        this.xCoord + 0.3F + this.worldObj.rand.nextFloat() * 0.4F,
                        this.yCoord + 0.5F,
                        this.zCoord + 0.3F + this.worldObj.rand.nextFloat() * 0.4F,
                        0.5F,
                        5,
                        true,
                        -0.025F
                    );
                this.ticksSinceAction = 0;
            }
        } else if (this.ticksSinceAction > 9) {
            if (this.completedActions < 5 && this.blocksConsumed < 4) {
                ForgeDirection dir = ForgeDirection.getOrientation(this.completedActions + 1);
                int x = this.xCoord + dir.offsetX;
                int y = this.yCoord + dir.offsetY;
                int z = this.zCoord + dir.offsetZ;
                Block b = this.worldObj.getBlock(x, y, z);
                if (b == Blocks.obsidian || ThaumcraftExtension.tileIsPortableHole(this.worldObj.getTileEntity(x, y, z), Blocks.obsidian)) {
                    MessageParticles.sendToClients(this.worldObj, x, y, z, 0, 0, 0, (short)0);
                    MessageParticles.sendToClients(this.worldObj, this.xCoord, this.yCoord, this.zCoord, x, y, z, (short)1);
                    this.worldObj.setBlockToAir(x, y, z);
                    this.blocksConsumed++;
                    this.ticksSinceAction = 0;
                }
            }

            this.completedActions++;
            if (this.completedActions > 9) {
                this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);

                for (int i = 0; i < this.blocksConsumed; i++) {
                    ItemStack stack = new ItemStack(ModBlocks.hungryMaw, 1);
                    float f = this.worldObj.rand.nextFloat() * 0.8F + 0.1F;
                    float f1 = this.worldObj.rand.nextFloat() * 0.8F + 0.1F;
                    float f2 = this.worldObj.rand.nextFloat() * 0.8F + 0.1F;
                    EntityItem entityitem = new EntityItem(this.worldObj, (double)this.xCoord + f, (double)this.yCoord + f1, (double)this.zCoord + f2, stack);
                    float f3 = 0.1F;
                    entityitem.motionX = (float)this.worldObj.rand.nextGaussian() * f3;
                    entityitem.motionY = Math.max((double)((float)this.worldObj.rand.nextGaussian() * f3 + 0.3F), 0.5);
                    entityitem.motionZ = (float)this.worldObj.rand.nextGaussian() * f3;
                    this.worldObj.spawnEntityInWorld(entityitem);
                }

                this.worldObj
                    .playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, "random.burp", 0.5F, this.worldObj.rand.nextFloat() * 0.2F + 0.7F);
                MessageParticles.sendToClients(this.worldObj, this.xCoord, this.yCoord, this.zCoord, 0, 0, 0, (short)2);
                Thaumcraft.proxy.burst(this.worldObj, this.xCoord + 0.5, this.yCoord - 0.5, this.zCoord + 0.5, 2.0F);
            }
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setInteger("completedActions", this.completedActions);
        nbttagcompound.setInteger("blocksConsumed", this.blocksConsumed);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.completedActions = nbttagcompound.getInteger("completedActions");
        this.blocksConsumed = nbttagcompound.getInteger("blocksConsumed");
    }
}
