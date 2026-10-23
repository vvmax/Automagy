package tuhljin.automagy.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import tuhljin.automagy.gui.ContainerChestBase;

public abstract class TileEntityChestBase extends ModTileEntityWithInventory implements IChestForGui {
    private int ticksSinceSync = -1;
    public float lidAngle;
    public float prevLidAngle;
    private int numUsingPlayers;
    protected int columns;
    protected int rows;

    public TileEntityChestBase(String inventoryName, int columns, int rows) {
        super(inventoryName, columns * rows);
        this.columns = columns;
        this.rows = rows;
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (this.worldObj != null) {
            if (!this.worldObj.isRemote && this.numUsingPlayers != 0 && (this.ticksSinceSync + this.xCoord + this.yCoord + this.zCoord) % 200 == 0) {
                this.numUsingPlayers = 0;
                float f = 5.0F;

                for (EntityPlayer p : this.worldObj
                    .getEntitiesWithinAABB(
                        EntityPlayer.class,
                        AxisAlignedBB.getBoundingBox(
                            this.xCoord - f, this.yCoord - f, this.zCoord - f, this.xCoord + 1 + f, this.yCoord + 1 + f, this.zCoord + 1 + f
                        )
                    )) {
                    if (p.openContainer instanceof ContainerChestBase && ((ContainerChestBase)p.openContainer).getChest() == this) {
                        this.numUsingPlayers++;
                    }
                }
            }

            this.ticksSinceSync++;
            this.prevLidAngle = this.lidAngle;
            float f = 0.1F;
            if (this.numUsingPlayers > 0 && this.lidAngle == 0.0F) {
                double d = this.xCoord + 0.5;
                double d1 = this.zCoord + 0.5;
                this.worldObj.playSoundEffect(d, this.yCoord + 0.5, d1, "random.chestopen", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
            }

            if (this.numUsingPlayers == 0 && this.lidAngle > 0.0F || this.numUsingPlayers > 0 && this.lidAngle < 1.0F) {
                float f1 = this.lidAngle;
                if (this.numUsingPlayers > 0) {
                    this.lidAngle += f;
                } else {
                    this.lidAngle -= f;
                }

                if (this.lidAngle > 1.0F) {
                    this.lidAngle = 1.0F;
                }

                float f2 = 0.5F;
                if (this.lidAngle < f2 && f1 >= f2) {
                    double d2 = this.xCoord + 0.5;
                    double d3 = this.zCoord + 0.5;
                    this.worldObj.playSoundEffect(d2, this.yCoord + 0.5, d3, "random.chestclosed", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
                }

                if (this.lidAngle < 0.0F) {
                    this.lidAngle = 0.0F;
                }
            }
        }
    }

    @Override
    public boolean receiveClientEvent(int eventNum, int arg) {
        if (eventNum == 1) {
            this.numUsingPlayers = arg;
            return true;
        } else {
            return super.receiveClientEvent(eventNum, arg);
        }
    }

    @Override
    public void openChest() {
        if (this.worldObj != null) {
            this.numUsingPlayers++;
            this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType(), 1, this.numUsingPlayers);
        }
    }

    @Override
    public void closeChest() {
        if (this.worldObj != null) {
            this.numUsingPlayers--;
            this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType(), 1, this.numUsingPlayers);
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int par1) {
        if (this.inventorySlots[par1] != null) {
            ItemStack var2 = this.inventorySlots[par1];
            this.inventorySlots[par1] = null;
            return var2;
        } else {
            return null;
        }
    }

    @Override
    public int getFacing() {
        return this.getBlockMetadata();
    }

    @Override
    public float getLidAngle() {
        return this.lidAngle;
    }

    @Override
    public float getPrevLidAngle() {
        return this.prevLidAngle;
    }

    @Override
    public int getNumRows() {
        return this.rows;
    }

    @Override
    public int getNumColumns() {
        return this.columns;
    }
}
