package tuhljin.automagy.tiles;

import java.util.HashSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.common.tiles.TileMirror;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.codechicken.lib.math.MathHelper;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.FilteringItemList;
import tuhljin.automagy.lib.inventory.IItemMap;
import tuhljin.automagy.lib.inventory.MappedItemsOrderedByTimeAdded;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.network.MessageGUIInventariumBookPref;

public class TileEntityRequisitionTome extends TileWithEyes implements IInventariumMemoryUser, IInventariumBookLinksMirror, IHasItemList {
    public static int MIRROR_RANGE = 5;
    public static double BOOK_MAXY = 0.1;
    public int linkX;
    public int linkY = -1;
    public int linkZ;
    protected int poweredTime = 0;
    public float openedAmt;
    public double bookY;
    public float page;
    private boolean bookGoingUp = true;
    private FilteringItemList requestsFilterable = null;
    public HashSet<String> sortByNameUsers = new HashSet<>();

    public TileEntityRequisitionTome() {
        super("Mirror Settings", 2, 0);
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote) {
            this.requestsFilterable = null;
            int prevPower = this.poweredTime;
            if (this.poweredTime > 0) {
                this.poweredTime--;
            }

            if (this.poweredTime < 1) {
                int amt = this.linkY != -1 ? VisNetHandler.drainVis(this.worldObj, this.xCoord, this.yCoord, this.zCoord, Aspect.ORDER, 10) : 0;
                if (amt > 0) {
                    this.poweredTime = amt * 2;
                    if (prevPower < 1) {
                        this.markDirty(true);
                    } else {
                        this.markDirty(false);
                    }
                } else if (prevPower > 0) {
                    this.markDirty(true);
                }
            }
        } else {
            if (this.poweredTime > 0) {
                if (this.openedAmt < 1.0F) {
                    this.openedAmt += 0.02F;
                    if (this.openedAmt > 1.0F) {
                        this.openedAmt = 1.0F;
                    }

                    if (this.bookY < BOOK_MAXY) {
                        this.bookY = MathHelper.retreatExp(this.bookY, BOOK_MAXY, 0.0, 0.18, 0.025);
                        this.bookGoingUp = true;
                    }
                } else if (this.bookGoingUp) {
                    if (this.bookY >= BOOK_MAXY) {
                        this.bookGoingUp = false;
                    } else {
                        this.bookY = MathHelper.retreatExp(this.bookY, BOOK_MAXY, 0.0, 0.007, 0.025);
                    }
                } else if (this.bookY <= 0.0) {
                    this.bookGoingUp = true;
                } else {
                    this.bookY = MathHelper.retreatExp(this.bookY, 0.0, BOOK_MAXY, 0.007, 0.025);
                }
            } else if (this.openedAmt > 0.0F) {
                this.openedAmt -= 0.02F;
                if (this.openedAmt < 0.0F) {
                    this.openedAmt = 0.0F;
                }
            }

            if (this.page > 0.0F) {
                this.page += 0.1F;
                if (this.page > 1.0F) {
                    if (this.poweredTime > 0 && this.openedAmt >= 1.0F && this.worldObj.rand.nextInt(4) == 0) {
                        this.page = 0.1F;
                    } else {
                        this.page = 0.0F;
                    }
                }
            } else if (!(this.openedAmt < 1.0F) && this.worldObj.rand.nextInt(200) == 0) {
                this.page = 0.1F;
                if (this.worldObj.rand.nextInt(16) == 0) {
                    this.worldObj
                        .playSound(
                            this.xCoord + 0.5,
                            this.yCoord + 1.0,
                            this.zCoord + 0.5,
                            "thaumcraft:page",
                            0.1F,
                            this.worldObj.rand.nextFloat() * 0.1F + 0.9F,
                            false
                        );
                }
            }
        }
    }

    @Override
    public void breakLink() {
        this.linkY = -1;
        this.markDirty();
    }

    @Override
    public void setLink(TileEntityInventarium teInv) {
        this.linkX = teInv.xCoord;
        this.linkY = teInv.yCoord;
        this.linkZ = teInv.zCoord;
        this.markDirty();
    }

    @Override
    public boolean isLinkedTo(TileEntityInventarium teInv) {
        return teInv.xCoord == this.linkX && teInv.yCoord == this.linkY && teInv.zCoord == this.linkZ;
    }

    @Override
    public boolean isInventariumLoaded() {
        return this.linkY != -1 && TjUtil.isChunkLoaded(this.worldObj, this.linkX, this.linkZ);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.linkY != -1 && this.poweredTime >= 1
            ? this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this
                && player.getDistanceSq(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5) <= 64.0
            : false;
    }

    @Override
    public WorldSpecificCoordinates getLinkCoord() {
        return new WorldSpecificCoordinates(this.worldObj, this.linkX, this.linkY, this.linkZ);
    }

    @Override
    public WorldSpecificCoordinates getRequesterCoord() {
        return new WorldSpecificCoordinates(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public boolean hasLocalItems() {
        return false;
    }

    @Override
    public boolean canRequestItems() {
        return true;
    }

    private WorldSpecificCoordinates getMirrorCoordIfInRange(int index) {
        WorldSpecificCoordinates coord = this.eyesContainingInventory.getCoordinates(index);
        if (coord != null && coord.dim == this.worldObj.provider.dimensionId) {
            double d = TjUtil.getDistanceBetweenPoints_squared(this.xCoord, this.yCoord, this.zCoord, coord.x, coord.y, coord.z);
            if (d <= MIRROR_RANGE * MIRROR_RANGE) {
                return coord;
            }
        }

        return null;
    }

    private IMirrorVariant getMirror(int index) {
        WorldSpecificCoordinates coord = this.getMirrorCoordIfInRange(index);
        if (coord != null) {
            World w = Automagy.proxy.getWorldByID(coord.dim);
            if (w == null) {
                return null;
            }

            TileEntity te = w.getTileEntity(coord.x, coord.y, coord.z);
            if (te instanceof TileMirror) {
                return new TileMirrorWrapper((TileMirror)te);
            }

            if (te instanceof IMirrorVariant) {
                return (IMirrorVariant)te;
            }
        }

        return null;
    }

    @Override
    public boolean isInputMirrorInRange() {
        return this.getMirrorCoordIfInRange(0) != null;
    }

    @Override
    public boolean isOutputMirrorInRange() {
        return this.getMirrorCoordIfInRange(1) != null;
    }

    @Override
    public boolean hasInputEye() {
        return this.eyesContainingInventory.getStackInSlot(0) != null;
    }

    @Override
    public boolean hasOutputEye() {
        return this.eyesContainingInventory.getStackInSlot(1) != null;
    }

    @Override
    public IMirrorVariant getInputMirror() {
        return this.getMirror(0);
    }

    @Override
    public IMirrorVariant getOutputMirror() {
        return this.getMirror(1);
    }

    @Override
    public IInventory getMirrorSettingsInventory() {
        return this.eyesContainingInventory;
    }

    @Override
    public boolean isTrackingDestinations() {
        TileEntity te = this.worldObj.getTileEntity(this.linkX, this.linkY, this.linkZ);
        return te instanceof TileEntityInventarium ? ((TileEntityInventarium)te).isTrackingDestinations() : false;
    }

    @Override
    public IItemMap getLocalItemList() {
        return null;
    }

    public FilteringItemList getInventariumItemList() {
        TileEntity te = this.worldObj.getTileEntity(this.linkX, this.linkY, this.linkZ);
        return te instanceof TileEntityInventarium ? ((TileEntityInventarium)te).getItemList() : null;
    }

    @Override
    public MappedItemsOrderedByTimeAdded getInventariumRequestedItemList() {
        TileEntity te = this.worldObj.getTileEntity(this.linkX, this.linkY, this.linkZ);
        return te instanceof TileEntityInventarium ? ((TileEntityInventarium)te).getRequestedItems() : null;
    }

    @Override
    public FilteringItemList getItemList() {
        if (this.requestsFilterable == null) {
            MappedItemsOrderedByTimeAdded list = this.getInventariumRequestedItemList();
            if (list != null) {
                this.requestsFilterable = new FilteringItemList(list);
            }
        }

        return this.requestsFilterable;
    }

    @Override
    public void setPlayerLocalViewPreference(EntityPlayer player, boolean local) {
    }

    @Override
    public void setPlayerSortingPreference(EntityPlayer player, boolean byName) {
        if (this.worldObj.isRemote) {
            MessageGUIInventariumBookPref.sendToServer(1, byName);
        } else {
            String name = player.getCommandSenderName();
            if (byName) {
                if (name != "Rcon") {
                    this.sortByNameUsers.add(name);
                }
            } else if (this.sortByNameUsers.contains(name)) {
                this.sortByNameUsers.remove(name);
            }

            this.markDirty();
        }
    }

    @Override
    public boolean doesPlayerPreferLocalView(EntityPlayer player) {
        return false;
    }

    @Override
    public boolean doesPlayerPreferSortByName(EntityPlayer player) {
        return this.sortByNameUsers.contains(player.getCommandSenderName());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 2, this.zCoord + 1);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setInteger("poweredTime", this.poweredTime);
        nbttagcompound.setInteger("linkX", this.linkX);
        nbttagcompound.setInteger("linkY", this.linkY);
        nbttagcompound.setInteger("linkZ", this.linkZ);
        NBTTagList nbttaglist = new NBTTagList();

        for (String name : this.sortByNameUsers) {
            if (name != null && !name.isEmpty()) {
                NBTTagCompound tagList = new NBTTagCompound();
                tagList.setString("User", name);
                nbttaglist.appendTag(tagList);
            }
        }

        nbttagcompound.setTag("sortByNameUsers", nbttaglist);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.poweredTime = nbttagcompound.getInteger("poweredTime");
        this.linkX = nbttagcompound.getInteger("linkX");
        this.linkY = nbttagcompound.getInteger("linkY");
        this.linkZ = nbttagcompound.getInteger("linkZ");
        NBTTagList nbttaglist = nbttagcompound.getTagList("sortByNameUsers", 10);
        this.sortByNameUsers.clear();
        int size = nbttaglist.tagCount();

        for (int i = 0; i < size; i++) {
            NBTTagCompound tagList = nbttaglist.getCompoundTagAt(i);
            String name = tagList.getString("User");
            if (name != null && !name.isEmpty()) {
                this.sortByNameUsers.add(name);
            }
        }
    }

    @Override
    public void onEyesInventoryChanged(int slot, ItemStack prevStack) {
        super.onEyesInventoryChanged(slot, prevStack);
    }
}
