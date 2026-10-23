package tuhljin.automagy.tiles;

import cpw.mods.fml.common.FMLLog;
import java.util.List;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import tuhljin.automagy.blocks.BlockEntitySignal;
import tuhljin.automagy.items.ItemEntityEye;
import tuhljin.automagy.lib.NeighborNotifier;

public class TileEntityEntitySignal extends ModTileEntityWithInventory {
    public static final int TICK_CHECK_RATE = 4;
    public static final int DETECT_RADIUS = 5;
    public boolean filterAdults = true;
    public boolean filterChildren = true;
    public boolean specificPlayer = false;
    public int powerStability;
    private String mobName = "";
    private boolean isPlayer = false;
    private int ticksSinceCheck = 0;
    private double minX;
    private double minY = -1.0;
    private double minZ;
    private double maxX;
    private double maxY;
    private double maxZ;

    public TileEntityEntitySignal() {
        super("blockEntitySignal", 1);
        this.notifyOnInventoryChanged = true;
    }

    public String getMobName() {
        return this.mobName;
    }

    private void updateMobName() {
        ItemStack stack = this.getStackInSlot(0);
        if (stack != null && stack.stackTagCompound != null && stack.stackTagCompound.hasKey("mobName")) {
            this.mobName = stack.stackTagCompound.getString("mobName");
        } else {
            this.mobName = "";
        }

        if (stack != null && stack.stackTagCompound != null && stack.stackTagCompound.getBoolean("entityIsPlayer")) {
            this.isPlayer = true;
        } else {
            this.isPlayer = false;
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return super.getInventoryStackLimit();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return ItemEntityEye.hasLinkedMob(stack);
    }

    @Override
    public void onInventoryChanged(int slot, ItemStack prevStack) {
        this.updateMobName();
    }

    public void receiveGUIMessage(int type, int data) {
        switch (type) {
            case 0:
                this.filterAdults = data == 1;
                break;
            case 1:
                this.filterChildren = data == 1;
                break;
            case 2:
                this.specificPlayer = data == 1;
                break;
            default:
                FMLLog.warning("[Automagy] TileEntityEntitySignal received invalid packet data. Ignoring. (type=" + type + ")");
                return;
        }

        this.markDirty(false);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setBoolean("filterAdults", this.filterAdults);
        nbttagcompound.setBoolean("filterChildren", this.filterChildren);
        nbttagcompound.setBoolean("specificPlayer", this.specificPlayer);
        nbttagcompound.setShort("powerStability", (short)this.powerStability);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.filterAdults = nbttagcompound.getBoolean("filterAdults");
        this.filterChildren = nbttagcompound.getBoolean("filterChildren");
        this.specificPlayer = nbttagcompound.getBoolean("specificPlayer");
        this.powerStability = nbttagcompound.getShort("powerStability");
        this.updateMobName();
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote) {
            if (this.ticksSinceCheck < 4) {
                this.ticksSinceCheck++;
            } else {
                this.ticksSinceCheck = 0;
                if (!this.mobName.isEmpty()) {
                    if (this.minY == -1.0) {
                        this.calculateRange();
                    }

                    AxisAlignedBB box = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
                    List<Entity> entities = this.worldObj
                        .selectEntitiesWithinAABB(EntityLivingBase.class, box, new TileEntityEntitySignal.EntitySelectorByName());
                    this.setPowerLevel(entities.size());
                } else {
                    this.setPowerLevel(0);
                }
            }
        }
    }

    private void setPowerLevel(int power) {
        int md = this.getBlockMetadata();
        int oldStability = this.powerStability;
        this.powerStability = Math.max(0, power - 15);
        power = Math.min(power, 15);
        if (this.powerStability != oldStability) {
            this.markDirty();
        }

        if (power != md) {
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, power, 3);
        } else if (this.powerStability != oldStability) {
            NeighborNotifier.notifyBlocksOfExtendedNeighborChange(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
        }
    }

    private void calculateRange() {
        boolean edgeXn = BlockEntitySignal.isScanBlockedBy(this.worldObj, this.xCoord - 1, this.yCoord, this.zCoord, ForgeDirection.WEST);
        boolean edgeXp = BlockEntitySignal.isScanBlockedBy(this.worldObj, this.xCoord + 1, this.yCoord, this.zCoord, ForgeDirection.EAST);
        boolean edgeYn = BlockEntitySignal.isScanBlockedBy(this.worldObj, this.xCoord, this.yCoord - 1, this.zCoord, ForgeDirection.DOWN);
        boolean edgeYp = BlockEntitySignal.isScanBlockedBy(this.worldObj, this.xCoord, this.yCoord + 1, this.zCoord, ForgeDirection.UP);
        boolean edgeZn = BlockEntitySignal.isScanBlockedBy(this.worldObj, this.xCoord, this.yCoord, this.zCoord - 1, ForgeDirection.NORTH);
        boolean edgeZp = BlockEntitySignal.isScanBlockedBy(this.worldObj, this.xCoord, this.yCoord, this.zCoord + 1, ForgeDirection.SOUTH);
        this.minX = edgeXn ? this.xCoord : this.xCoord - 5;
        this.minY = edgeYn ? this.yCoord : this.yCoord - 5;
        this.minZ = edgeZn ? this.zCoord : this.zCoord - 5;
        if (edgeXp) {
            this.maxX = this.xCoord + 1;
            if (!edgeXn) {
                this.minX -= 5.0;
            }
        } else {
            this.maxX = this.xCoord + 1 + 5;
            if (edgeXn) {
                this.maxX += 5.0;
            }
        }

        if (edgeYp) {
            this.maxY = this.yCoord + 1;
            if (!edgeYn) {
                this.minY -= 5.0;
            }
        } else {
            this.maxY = this.yCoord + 1 + 5;
            if (edgeYn) {
                this.maxY += 5.0;
            }
        }

        if (edgeZp) {
            this.maxZ = this.zCoord + 1;
            if (!edgeZn) {
                this.minZ -= 5.0;
            }
        } else {
            this.maxZ = this.zCoord + 1 + 5;
            if (edgeZn) {
                this.maxZ += 5.0;
            }
        }

        if (this.minY < 0.0) {
            this.minY = 0.0;
        }
    }

    public void flagRangeForRecalculation() {
        this.minY = -1.0;
    }

    public class EntitySelectorByName implements IEntitySelector {
        @Override
        public boolean isEntityApplicable(Entity entity) {
            if (entity instanceof EntityLivingBase) {
                if (entity instanceof EntityPlayer) {
                    if (TileEntityEntitySignal.this.isPlayer) {
                        if (!TileEntityEntitySignal.this.specificPlayer) {
                            return true;
                        }

                        String name = entity.getCommandSenderName();
                        if (TileEntityEntitySignal.this.mobName.equals(name)) {
                            return true;
                        }
                    }
                } else {
                    String name = EntityList.getEntityString(entity);
                    if (TileEntityEntitySignal.this.mobName.equals(name)) {
                        if (entity instanceof EntityAgeable) {
                            if (((EntityAgeable)entity).isChild()) {
                                return TileEntityEntitySignal.this.filterChildren;
                            }

                            return TileEntityEntitySignal.this.filterAdults;
                        }

                        return true;
                    }
                }
            }

            return false;
        }
    }
}
