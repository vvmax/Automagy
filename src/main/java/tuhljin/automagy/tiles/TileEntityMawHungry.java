package tuhljin.automagy.tiles;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import tuhljin.automagy.lib.ThaumcraftExtension;

public class TileEntityMawHungry extends ModTileEntity {
    public static int MIN_SOUND_INTERVAL = 250;
    public long nextNoise = 0L;

    public void moveItem(EntityItem item) {
        MIN_SOUND_INTERVAL = 250;
        int orientation = this.getBlockMetadata();
        ForgeDirection dir = ForgeDirection.getOrientation(orientation);
        TileEntity te = this.worldObj.getTileEntity(this.xCoord - dir.offsetX, this.yCoord - dir.offsetY, this.zCoord - dir.offsetZ);
        if (te instanceof IInventory) {
            ItemStack leftovers = this.depositItem(item.getEntityItem(), (IInventory)te, orientation);
            if (leftovers == null || leftovers.stackSize != item.getEntityItem().stackSize) {
                long currentTime = System.currentTimeMillis();
                if (currentTime >= this.nextNoise) {
                    this.nextNoise = currentTime + MIN_SOUND_INTERVAL;
                    this.worldObj.playSoundAtEntity(item, "random.eat", 0.3F, (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F + 1.5F);
                    this.worldObj.playSoundAtEntity(item, "random.eat", 0.35F, (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F - 1.0F);
                }
            }

            if (leftovers != null) {
                item.setEntityItemStack(leftovers);
            } else {
                item.setDead();
            }
        } else {
            this.getBlockType().onNeighborBlockChange(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.blockType);
        }
    }

    protected ItemStack depositItem(ItemStack stack, IInventory inv, int side) {
        return ThaumcraftExtension.insertStackIntoInventory(stack, inv, side, true);
    }
}
