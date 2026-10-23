package tuhljin.automagy.tiles;

import cpw.mods.fml.common.FMLLog;
import java.util.Iterator;
import java.util.Map.Entry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import tuhljin.automagy.blocks.BlockTally;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.lib.IRestrictedInventoryAccess;
import tuhljin.automagy.lib.NeighborNotifier;
import tuhljin.automagy.lib.inventory.FilteringItemList;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

public abstract class TileEntityTallyBase extends ModTileEntityWithFilterMainInventory implements IRestrictedInventoryAccess {
    protected final int COOLDOWN_TIME = 10;
    public int targetX;
    public int targetY = -1;
    public int targetZ;
    public boolean requireAllMatches = true;
    protected int cooldown = -1;
    protected boolean antiloopIsChecking = false;
    protected boolean antiloopIsDoingCalc = false;

    protected TileEntityTallyBase(String name) {
        super(name);
    }

    public void findNewTarget(boolean neighborMayBeRemoteComparator) {
        if (!this.antiloopIsDoingCalc) {
            this.cooldown = this.cooldown == -1 ? 5 + this.worldObj.rand.nextInt(5) : 10;
            int orientation = BlockTally.getOrientation(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            if (orientation == 0) {
                TileEntity te = this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord);
                if (te instanceof TileEntityRemoteComparator) {
                    if (!neighborMayBeRemoteComparator) {
                        return;
                    }

                    this.targetY = -1;
                    WorldSpecificCoordinates coord = ((TileEntityRemoteComparator)te).getLinkLocation();
                    if (((TileEntityRemoteComparator)te).coordinatesAreInRange(coord) && coord != null) {
                        this.targetX = coord.x;
                        this.targetY = coord.y;
                        this.targetZ = coord.z;
                    }

                    this.calculateRedstoneSignalStrength();
                    this.markDirty();
                    return;
                }
            }

            ForgeDirection dir = ForgeDirection.getOrientation(orientation).getOpposite();
            this.targetX = this.xCoord + dir.offsetX;
            this.targetY = this.yCoord + dir.offsetY;
            this.targetZ = this.zCoord + dir.offsetZ;
            this.foundNewDirectTarget();
        }
    }

    protected void foundNewDirectTarget() {
        this.calculateRedstoneSignalStrength();
        this.markDirty();
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote) {
            if (this.cooldown > 0) {
                this.cooldown--;
            } else {
                this.cooldown = 10;
                this.calculateRedstoneSignalStrength();
            }
        }
    }

    protected void calculateRedstoneSignalStrength() {
        if (!this.antiloopIsDoingCalc) {
            this.antiloopIsDoingCalc = true;
            if (this.filter == null) {
                this.setPower(false);
                this.antiloopIsDoingCalc = false;
            } else {
                FilteringItemList invMap = this.getDetectedItems();
                if (invMap == null) {
                    this.setPower(false);
                    this.antiloopIsDoingCalc = false;
                } else {
                    boolean partialMatching = this.filterIsBlacklist ? !this.requireAllMatches : this.requireAllMatches;
                    boolean anyQualify = false;
                    int size = this.filter.getSizeInventory();

                    for (int i = 0; i < size; i++) {
                        ItemStack stack = this.filter.getStackInSlot(i);
                        if (stack != null) {
                            HashableItemWithoutSize item = new HashableItemWithoutSize(stack);
                            int amt = invMap.get(item, this.filter.ignoreMetadata, this.filter.ignoreNBT);
                            if (this.compare(amt, stack.stackSize)) {
                                if (!partialMatching) {
                                    this.setPower(true);
                                    this.antiloopIsDoingCalc = false;
                                    return;
                                }

                                anyQualify = true;
                            } else if (partialMatching) {
                                this.setPower(false);
                                this.antiloopIsDoingCalc = false;
                                return;
                            }
                        }
                    }

                    if (this.filterNamePattern == null) {
                        this.setPower(anyQualify);
                    } else {
                        Iterator<Entry<HashableItemWithoutSize, Integer>> iter = invMap.getIterator();

                        while (iter.hasNext()) {
                            Entry<HashableItemWithoutSize, Integer> entry = iter.next();
                            HashableItemWithoutSize key = entry.getKey();
                            if (this.nameFilterMatches(key)) {
                                this.setPower(!this.filterIsBlacklist);
                                this.antiloopIsDoingCalc = false;
                                return;
                            }
                        }

                        this.setPower(this.filterIsBlacklist);
                    }

                    this.antiloopIsDoingCalc = false;
                }
            }
        }
    }

    public abstract FilteringItemList getDetectedItems();

    protected boolean compare(int a, int b) {
        if (this.filter.useItemCount) {
            return this.filterIsBlacklist ? a < b : a >= b;
        } else {
            return this.filterIsBlacklist ? a == 0 : a > 0;
        }
    }

    protected void setPower(boolean enabled) {
        int md = this.getBlockMetadata();
        if (md > 5) {
            if (!enabled) {
                this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, md - 6, 2);
                NeighborNotifier.notifyBlocksOfExtendedNeighborChange(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
            }
        } else if (enabled) {
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, md + 6, 2);
            NeighborNotifier.notifyBlocksOfExtendedNeighborChange(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
        }
    }

    public BlockCoord getUltimateTarget() {
        if (this.antiloopIsChecking) {
            return null;
        } else {
            TileEntity tileAtTarget = this.worldObj.getTileEntity(this.targetX, this.targetY, this.targetZ);
            BlockCoord target;
            if (tileAtTarget instanceof TileEntityTallyBase) {
                this.antiloopIsChecking = true;
                target = ((TileEntityTallyBase)tileAtTarget).getUltimateTarget();
                this.antiloopIsChecking = false;
            } else {
                target = new BlockCoord(this.targetX, this.targetY, this.targetZ);
            }

            return target;
        }
    }

    public abstract boolean hasValidTarget();

    public void receiveGUIMessage(int type, boolean data) {
        switch (type) {
            case 0:
                this.requireAllMatches = !data;
                this.markDirty(false);
                return;
            default:
                FMLLog.warning("[Automagy] Tally Box tile entity received invalid packet data. Ignoring. (type=" + type + ")");
        }
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.targetX = nbttagcompound.getInteger("targetX");
        this.targetY = nbttagcompound.getInteger("targetY");
        this.targetZ = nbttagcompound.getInteger("targetZ");
        this.requireAllMatches = nbttagcompound.getBoolean("requireAllMatches");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setInteger("targetX", this.targetX);
        nbttagcompound.setInteger("targetY", this.targetY);
        nbttagcompound.setInteger("targetZ", this.targetZ);
        nbttagcompound.setBoolean("requireAllMatches", this.requireAllMatches);
    }

    @Override
    public boolean greedyChestCanSteal(TileEntity greedyChest, ForgeDirection fromSide) {
        return false;
    }

    public ItemStack silentlyExtractItem() {
        ItemStack stack = this.inventorySlots[0];
        this.inventorySlots[0] = null;
        return stack;
    }
}
