package tuhljin.automagy.gui;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.container.InventoryHandMirror;
import thaumcraft.common.items.relics.ItemHandMirror;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.items.ItemEnchantedPaper;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.lib.inventory.IItemMap;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.network.MessageItemStacksList;
import tuhljin.automagy.network.MessageItemStacksRequested;
import tuhljin.automagy.tiles.IInventariumBook;
import tuhljin.automagy.tiles.IInventariumBookLinksMirror;
import tuhljin.automagy.tiles.IMirrorVariant;

public class ContainerScribe extends Container {
    public IInventariumBook book;
    protected TileEntity te;
    protected IItemMap prevListLocal;
    protected IItemMap prevListInventarium;
    protected IItemMap prevListRequested;
    public static int XOFFSET_DEMAND = 30;
    protected final boolean hasLocalItems;
    protected final boolean hasDemand;
    private WorldSpecificCoordinates invCoord;
    private EntityPlayer player;
    private World worldObj;
    public IInventory mirror;
    private ItemStack fakeHandMirror;

    public ContainerScribe(InventoryPlayer inventoryPlayer, World world, IInventariumBook book, boolean hasLocalItems, boolean hasDemand) {
        this.book = book;
        this.hasLocalItems = hasLocalItems;
        this.hasDemand = hasDemand;
        if (book instanceof IInventariumBookLinksMirror) {
            IMirrorVariant mirror = ((IInventariumBookLinksMirror)book).getInputMirror();
            if (mirror != null) {
                WorldSpecificCoordinates coord = mirror.getDestinationMirrorCoordinates();
                if (coord != null) {
                    this.setMirrorOutputLocation(coord);
                }
            }
        }

        this.player = inventoryPlayer.player;
        this.worldObj = world;
        if (book instanceof TileEntity) {
            this.te = (TileEntity)book;
        }

        if (book.isInventariumLoaded()) {
            this.invCoord = book.getLinkCoord();
            if (hasLocalItems) {
                this.prevListLocal = book.getLocalItemList();
            }

            this.prevListInventarium = book.getInventariumItemList();
            if (hasDemand) {
                this.prevListRequested = book.getInventariumRequestedItemList().copy();
            }
        }

        if (this.fakeHandMirror != null) {
            this.mirror = new InventoryHandMirror(this);
            this.addSlotToContainer(new Slot(this.mirror, 0, 22, 169));
            this.onCraftMatrixChanged(this.mirror);
        }

        int xOffset = hasDemand ? XOFFSET_DEMAND : 0;

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18 + 16 + xOffset, 198));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + j + i * 9, 8 + j * 18 + 16 + xOffset, 84 + i * 18 + 56));
            }
        }

        if (this.fakeHandMirror != null) {
            this.onCraftMatrixChanged(this.mirror);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.book.isInventariumLoaded() && this.book.isUseableByPlayer(player);
    }

    @Override
    public void onCraftMatrixChanged(IInventory inv) {
        if (this.fakeHandMirror == null) {
            super.onCraftMatrixChanged(inv);
        } else {
            if (!this.worldObj.isRemote && this.mirror.getStackInSlot(0) != null && this.player != null && this.transport(this.mirror.getStackInSlot(0))) {
                this.mirror.setInventorySlotContents(0, null);

                for (int var4 = 0; var4 < this.crafters.size(); var4++) {
                    ((ICrafting)this.crafters.get(var4)).sendSlotContents(this, 0, null);
                }
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum) {
        if (this.fakeHandMirror == null) {
            return null;
        } else {
            ItemStack stack = null;
            Slot slot = (Slot)this.inventorySlots.get(slotNum);
            if (slot != null) {
                if (slot instanceof SlotRestrictedWithReporting) {
                    return super.transferStackInSlot(player, slotNum);
                }

                if (slot.getHasStack()) {
                    ItemStack stackInSlot = slot.getStack();
                    if (ItemEnchantedPaper.stackIsFilter(stackInSlot)) {
                        return super.transferStackInSlot(player, slotNum);
                    }

                    if (slotNum == 0) {
                        if (!this.mergeItemStack(stackInSlot, 1, this.inventorySlots.size(), true)) {
                            return null;
                        }
                    } else if (!this.mergeItemStack(stackInSlot, 0, 1, false)) {
                        return null;
                    }

                    if (stackInSlot.stackSize == 0) {
                        slot.putStack(null);
                    } else {
                        slot.onSlotChanged();
                    }
                }
            }

            return stack;
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.hasLocalItems) {
            IItemMap itemList = this.book.getLocalItemList();
            ArrayList<ItemStack> list = getUpdatedItems(itemList, this.prevListLocal);
            if (list.size() > 0) {
                this.prevListLocal = itemList;

                for (Object player : this.crafters) {
                    if (player instanceof EntityPlayer) {
                        MessageItemStacksList.sendToClient(
                            (EntityPlayer)player, this.te.getWorld(), this.te.xCoord, this.te.yCoord, this.te.zCoord, list, true, true
                        );
                    }
                }
            }
        }

        IItemMap itemList = this.book.getInventariumItemList();
        ArrayList<ItemStack> list = getUpdatedItems(itemList, this.prevListInventarium);
        if (list.size() > 0) {
            this.prevListInventarium = itemList;

            for (Object playerx : this.crafters) {
                if (playerx instanceof EntityPlayer) {
                    MessageItemStacksList.sendToClient(
                        (EntityPlayer)playerx, this.invCoord.dim, this.invCoord.x, this.invCoord.y, this.invCoord.z, list, true, false
                    );
                }
            }
        }

        if (this.hasDemand) {
            IItemMap var6 = this.book.getInventariumRequestedItemList();
            list = getUpdatedItems(var6, this.prevListRequested);
            if (list.size() > 0) {
                this.prevListRequested = var6.copy();

                for (Object playerxx : this.crafters) {
                    if (playerxx instanceof EntityPlayer) {
                        MessageItemStacksRequested.sendToClient(
                            (EntityPlayer)playerxx, this.invCoord.dim, this.invCoord.x, this.invCoord.y, this.invCoord.z, list, true
                        );
                    }
                }
            }
        }
    }

    public TileEntity getTile() {
        return this.te;
    }

    public boolean transport(ItemStack items) {
        return this.fakeHandMirror == null ? false : ItemHandMirror.transport(this.fakeHandMirror, items, this.player, this.worldObj);
    }

    public void setMirrorOutputLocation(WorldSpecificCoordinates coord) {
        World world = Automagy.proxy.getWorldByID(coord.dim);
        if (world == null) {
            this.fakeHandMirror = null;
        } else {
            this.fakeHandMirror = new ItemStack(ConfigItems.itemHandMirror);
            this.fakeHandMirror.setTagInfo("linkX", new NBTTagInt(coord.x));
            this.fakeHandMirror.setTagInfo("linkY", new NBTTagInt(coord.y));
            this.fakeHandMirror.setTagInfo("linkZ", new NBTTagInt(coord.z));
            this.fakeHandMirror.setTagInfo("linkDim", new NBTTagInt(coord.dim));
            this.fakeHandMirror.setTagInfo("dimname", new NBTTagString(world.provider.getDimensionName()));
        }
    }

    public void setMirrorOutputLocation(World world, int x, int y, int z) {
        this.fakeHandMirror = new ItemStack(ConfigItems.itemHandMirror);
        this.fakeHandMirror.setTagInfo("linkX", new NBTTagInt(x));
        this.fakeHandMirror.setTagInfo("linkY", new NBTTagInt(y));
        this.fakeHandMirror.setTagInfo("linkZ", new NBTTagInt(z));
        this.fakeHandMirror.setTagInfo("linkDim", new NBTTagInt(world.provider.dimensionId));
        this.fakeHandMirror.setTagInfo("dimname", new NBTTagString(DimensionManager.getProvider(world.provider.dimensionId).getDimensionName()));
    }

    public boolean hasMirror() {
        return this.fakeHandMirror != null;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!this.worldObj.isRemote && this.mirror != null) {
            for (int i = 0; i < 1; i++) {
                ItemStack stack = this.mirror.getStackInSlotOnClosing(i);
                if (stack != null) {
                    player.dropPlayerItemWithRandomChoice(stack, false);
                }
            }
        }
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int button) {
        if (button == 0 && this.te != null) {
            int gui = 18;
            if (this.hasDemand && this.book instanceof IInventariumBookLinksMirror && ((IInventariumBookLinksMirror)this.book).isTrackingDestinations()) {
                gui = 19;
            }

            FMLNetworkHandler.openGui(player, Automagy.instance, gui, this.te.getWorld(), this.te.xCoord, this.te.yCoord, this.te.zCoord);
            return true;
        } else {
            return false;
        }
    }

    public static ArrayList<ItemStack> getUpdatedItems(IItemMap newList, IItemMap oldList) {
        ArrayList<ItemStack> changed = new ArrayList<>();
        if (newList != null) {
            Iterator<Entry<HashableItemWithoutSize, Integer>> iter = newList.getIterator();

            while (iter.hasNext()) {
                Entry<HashableItemWithoutSize, Integer> entry = iter.next();
                HashableItemWithoutSize key = entry.getKey();
                int count = entry.getValue();
                if (oldList == null || oldList.get(key) != count) {
                    changed.add(key.getItemStack(count));
                }
            }
        }

        if (oldList != null) {
            Iterator<Entry<HashableItemWithoutSize, Integer>> iter = oldList.getIterator();

            while (iter.hasNext()) {
                Entry<HashableItemWithoutSize, Integer> entry = iter.next();
                HashableItemWithoutSize key = entry.getKey();
                if (newList == null || !newList.containsKey(key)) {
                    changed.add(key.getItemStack(0));
                }
            }
        }

        return changed;
    }
}
