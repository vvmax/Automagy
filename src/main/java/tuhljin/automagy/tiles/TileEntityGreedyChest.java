package tuhljin.automagy.tiles;

import cpw.mods.fml.common.FMLLog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;
import tuhljin.automagy.gui.ContainerGreedyChest;
import tuhljin.automagy.items.InventoryWithFilterOptions;
import tuhljin.automagy.items.ItemEnchantedPaper;
import tuhljin.automagy.lib.IRestrictedInventoryAccess;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.FilteringItemList;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.network.MessageGreedyChestStole;

public class TileEntityGreedyChest extends ModTileEntityWithInventory implements IChest, IContainsFilter, IRestrictedInventoryAccess {
    private static int IGNORE_OVER_SIZE = 999;
    private int ticksSinceSync = -1;
    public float lidAngle;
    public float prevLidAngle;
    private int numUsingPlayers;
    public InventoryObjectFilter filterContainingInventory;
    public int modePilfer = 0;
    public float fractionWanted = 0.5F;
    public int amountWanted = 64;
    public boolean[] directions = new boolean[]{true, true, true, true, true, true};
    public int litRunesTime = 0;
    private static final int MAX_LIT_RUNES_TICKS = 5;
    private int[] countdownPilferFromNeighbor = new int[]{0, 1, 2, 3, 4, 5};
    private FilteringItemList[] neighborInv = new FilteringItemList[6];
    private boolean[] skipNextTheft = new boolean[]{true, true, true, true, true, true};
    private static final int PACKET_COOLDOWN = 4;
    private int ticksSincePacket = 4;
    private Map<HashableItemWithoutSize, Float> partialItemTracker = new HashMap<>();
    protected boolean doingInventoryUpdate = false;
    private Map<HashableItemWithoutSize, String> itemsRemoved = null;
    private int itemsRemovedChanged = -1;
    private InventoryWithFilterOptions filter = null;
    private boolean filterIsBlacklist;
    private Pattern filterNamePattern;

    public TileEntityGreedyChest() {
        super("blockGreedyChest", 27);
        this.notifyOnInventoryChanged = true;
        this.filterContainingInventory = new InventoryObjectFilter(this, "Greedy Chest Filter", 1);
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
                    if (p.openContainer instanceof ContainerGreedyChest && ((ContainerGreedyChest)p.openContainer).getTileEntity() == this) {
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

            if (!this.worldObj.isRemote) {
                if (this.ticksSincePacket < 4) {
                    this.ticksSincePacket++;
                }

                this.doingInventoryUpdate = true;

                for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                    int d = dir.ordinal();
                    if (this.countdownPilferFromNeighbor[d] < 1) {
                        this.countdownPilferFromNeighbor[d] = 5;
                        if (this.directions[d]) {
                            this.checkThenPilferFromNeighbor(dir);
                        }
                    } else {
                        this.countdownPilferFromNeighbor[d]--;
                    }
                }

                this.doingInventoryUpdate = false;
                if (this.itemsRemovedChanged != -1 && Math.abs(this.ticksSinceSync - this.itemsRemovedChanged) > 100) {
                    this.itemsRemoved = null;
                    this.itemsRemovedChanged = -1;
                }
            }

            if (this.litRunesTime > 0) {
                this.litRunesTime--;
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

    protected void checkThenPilferFromNeighbor(ForgeDirection dir) {
        int d = dir.ordinal();
        int x = this.xCoord + dir.offsetX;
        int y = this.yCoord + dir.offsetY;
        int z = this.zCoord + dir.offsetZ;
        TileEntity te = this.worldObj.getTileEntity(x, y, z);
        if (te != null && this.worldObj.blockExists(x, y, z)) {
            if (this.modePilfer == 0) {
                if (!this.isPercentageShareBoss(x, y, z, te)) {
                    return;
                }
            } else if (!this.canTakeFromNeighbor(te, dir)) {
                return;
            }

            int extractionSide = dir.getOpposite().ordinal();
            FilteringItemList newContents = null;
            if (te instanceof IInventory) {
                newContents = new FilteringItemList().populateFromInventory((IInventory)te, extractionSide, true);
            }

            if (newContents == null || newContents.size() == 0) {
                this.neighborInv[d] = null;
                this.skipNextTheft[d] = false;
            } else if (this.skipNextTheft[d]) {
                this.neighborInv[d] = newContents;
                this.skipNextTheft[d] = false;
            } else {
                FilteringItemList oldContents = this.neighborInv[d];
                FilteringItemList additions;
                if (oldContents == null) {
                    additions = new FilteringItemList(newContents);
                } else {
                    additions = newContents.getDifferences(oldContents);
                }

                if (this.itemsRemoved != null) {
                    boolean copiedKeys = false;
                    Iterator<Entry<HashableItemWithoutSize, String>> iter = this.itemsRemoved.entrySet().iterator();

                    while (iter.hasNext()) {
                        Entry<HashableItemWithoutSize, String> entry = iter.next();
                        String s = entry.getValue();
                        String ds = String.valueOf(d);
                        if (s.indexOf(ds) == -1) {
                            HashableItemWithoutSize key = entry.getKey();
                            if (!copiedKeys) {
                                copiedKeys = true;
                                Iterator<Entry<HashableItemWithoutSize, Integer>> subIter = newContents.getIterator();

                                while (subIter.hasNext()) {
                                    Entry<HashableItemWithoutSize, Integer> subEntry = subIter.next();
                                    HashableItemWithoutSize item = subEntry.getKey();
                                    if (!additions.containsKey(item)) {
                                        additions.setZero(item);
                                    }
                                }
                            }

                            this.itemsRemoved.put(key, s + ds);
                        } else {
                            iter.remove();
                        }
                    }

                    if (this.itemsRemoved.size() == 0) {
                        this.itemsRemoved = null;
                        this.itemsRemovedChanged = -1;
                    }
                }

                if (additions.size() > 0) {
                    boolean didTeleport = this.pilferFromNeighbor((IInventory)te, x, y, z, dir, extractionSide, additions, newContents, -1);
                    if (this.modePilfer == 0) {
                        return;
                    }

                    if (didTeleport && this.ticksSincePacket >= 4) {
                        this.ticksSincePacket = 0;
                        MessageGreedyChestStole.sendToClients(this.worldObj, this.xCoord, this.yCoord, this.zCoord, d);
                    }
                }

                this.neighborInv[d] = newContents.size() == 0 ? null : newContents;
            }
        }
    }

    private boolean pilferFromNeighbor(
        IInventory inv,
        int x,
        int y,
        int z,
        ForgeDirection dir,
        int extractionSide,
        FilteringItemList additions,
        FilteringItemList newContents,
        int overrideTakeAmt
    ) {
        ForgeDirection oppDir = dir.getOpposite();
        ISidedInventory sidedInv = null;
        if (inv instanceof ISidedInventory) {
            sidedInv = (ISidedInventory)inv;
        }

        boolean didTeleport = false;
        boolean ignoreMetadata = this.filter != null && this.filter.ignoreMetadata;
        boolean ignoreNBT = this.filter != null && this.filter.ignoreNBT;
        Iterator<Entry<HashableItemWithoutSize, Integer>> iter = additions.getIterator();

        while (iter.hasNext()) {
            Entry<HashableItemWithoutSize, Integer> entry = iter.next();
            HashableItemWithoutSize key = entry.getKey();
            int toTake;
            if (overrideTakeAmt == -1) {
                int qtyAdded = entry.getValue();
                int qtyTotal = newContents.get(key);
                int qtyNbrFilterAdjusted = newContents.get(key, ignoreMetadata, ignoreNBT);
                toTake = this.getPilferAmount(key, qtyAdded, qtyTotal, qtyNbrFilterAdjusted, inv, newContents, x, y, z);
            } else {
                toTake = overrideTakeAmt;
            }

            if (toTake > 0) {
                int took = 0;

                for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
                    ItemStack stack = inv.getStackInSlot(slot);
                    if (stack != null
                        && stack.stackSize > 0
                        && this.filterSensitiveIsSameItem(key, stack)
                        && (sidedInv == null || sidedInv.canExtractItem(slot, stack, extractionSide))) {
                        stack = inv.decrStackSize(slot, Math.min(toTake - took, stack.stackSize));
                        if (stack != null && stack.stackSize > 0) {
                            took += stack.stackSize;
                            this.addToInventory(stack);
                            didTeleport = true;
                        }
                    }

                    if (toTake - took < 1) {
                        break;
                    }
                }

                if (took != 0 && overrideTakeAmt == -1) {
                    int val = newContents.get(key) - took;
                    newContents.set(key, val);
                }
            }
        }

        return didTeleport;
    }

    protected boolean filterSensitiveIsSameItem(HashableItemWithoutSize item, ItemStack stack) {
        boolean ignoreMetadata = this.filter != null && this.filter.ignoreMetadata;
        boolean ignoreNBT = this.filter != null && this.filter.ignoreNBT;
        return item.isSameItem(stack, ignoreMetadata, ignoreNBT);
    }

    protected HashableItemWithoutSize getFilterSensitiveItem(HashableItemWithoutSize item) {
        boolean ignoreMetadata = this.filter != null && this.filter.ignoreMetadata;
        boolean ignoreNBT = this.filter != null && this.filter.ignoreNBT;
        return new HashableItemWithoutSize(item, ignoreMetadata, ignoreNBT);
    }

    public void addToInventory(ItemStack stack) {
        int stacksTo = Math.min(stack.getMaxStackSize(), this.getInventoryStackLimit());
        if (stacksTo > 1 && stack.isStackable()) {
            for (int slot = 0; slot < this.getSizeInventory(); slot++) {
                ItemStack slottedStack = this.getStackInSlot(slot);
                if (slottedStack != null && TjUtil.canItemsStack(stack, slottedStack)) {
                    int newSize = stack.stackSize + slottedStack.stackSize;
                    if (newSize <= stacksTo) {
                        slottedStack.stackSize = newSize;
                        this.setInventorySlotContents(slot, slottedStack);
                        return;
                    }

                    if (slottedStack.stackSize < stacksTo) {
                        stack.stackSize = newSize - stacksTo;
                        slottedStack.stackSize = stacksTo;
                        this.setInventorySlotContents(slot, slottedStack);
                    }
                }
            }
        }

        for (int slotx = 0; slotx < this.getSizeInventory(); slotx++) {
            ItemStack slottedStack = this.getStackInSlot(slotx);
            if (slottedStack == null) {
                if (stack.stackSize <= stacksTo) {
                    this.setInventorySlotContents(slotx, stack);
                    return;
                }

                ItemStack copy = stack.copy();
                copy.stackSize = stacksTo;
                stack.stackSize -= stacksTo;
                this.setInventorySlotContents(slotx, copy);
            }
        }

        if (stack.stackSize > 0) {
            TjUtil.dropItemIntoWorld(stack, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
        }
    }

    private int getPilferAmount(
        HashableItemWithoutSize item,
        int qtyAdded,
        int qtyNeighborTotal,
        int qtyNbrFilterAdjusted,
        IInventory inv,
        FilteringItemList source,
        int x,
        int y,
        int z
    ) {
        int qtyHere = -1;
        int filteredMax = Integer.MAX_VALUE;
        int filterOverridesMode = -1;
        int wanted = -1;
        if (this.modePilfer == 0) {
            this.bossHandlePercentageShare(x, y, z, inv, source, item, qtyAdded, qtyNeighborTotal);
            return 0;
        } else {
            if (wanted == -1) {
                if (this.filter != null) {
                    int[] result = this.getMaxAllowedAfterFilter(item, source);
                    if (result != null) {
                        filteredMax = result[0];
                        if (filteredMax < 1) {
                            return 0;
                        }

                        filterOverridesMode = result[1];
                        if (result.length >= 3) {
                            qtyHere = result[2];
                        }
                    }
                }

                if (this.modePilfer == filterOverridesMode) {
                    wanted = filteredMax;
                } else if (this.modePilfer == 2) {
                    wanted = qtyNbrFilterAdjusted - this.amountWanted;
                } else {
                    if (qtyHere == -1) {
                        FilteringItemList dest = new FilteringItemList().populateFromInventory(this, false);
                        qtyHere = dest.get(item, this.filter == null ? false : this.filter.ignoreMetadata, this.filter == null ? false : this.filter.ignoreNBT);
                    }

                    if (this.modePilfer == 1) {
                        int total = qtyNbrFilterAdjusted + qtyHere;
                        int needed = MathHelper.ceiling_float_int(total * this.fractionWanted);
                        wanted = needed - qtyHere;
                    } else {
                        if (this.modePilfer != 3) {
                            return 0;
                        }

                        wanted = this.amountWanted - qtyHere;
                    }
                }

                wanted = Math.min(wanted, filteredMax);
                if (wanted > 0) {
                    int available = this.getAvailableSpaceForItem(item, wanted);
                    wanted = Math.min(wanted, available);
                }
            }

            wanted = Math.min(wanted, qtyNeighborTotal);
            return wanted < 1 ? 0 : wanted;
        }
    }

    private int getAvailableSpaceForItem(HashableItemWithoutSize item, int wanted) {
        ItemStack stack = item.getItemStack(1);
        int maxStack = stack.isStackable() ? Math.min(stack.getMaxStackSize(), this.getInventoryStackLimit()) : 1;
        int available = 0;

        for (int slot = 0; slot < this.getSizeInventory(); slot++) {
            ItemStack slottedStack = this.getStackInSlot(slot);
            if (slottedStack == null) {
                available += maxStack;
            } else if (maxStack > 1 && TjUtil.canItemsStack(stack, slottedStack) && slottedStack.stackSize < maxStack) {
                available += maxStack - slottedStack.stackSize;
            }

            if (wanted != -1 && available >= wanted) {
                break;
            }
        }

        return available;
    }

    private void bossHandlePercentageShare(
        int x, int y, int z, IInventory inv, FilteringItemList source, HashableItemWithoutSize item, int qtyAdded, int qtyNeighborTotal
    ) {
        TileEntityGreedyChest[] chests = new TileEntityGreedyChest[6];
        float totalFraction = 0.0F;
        int numChests = 0;
        int[] available = new int[6];
        Map<TileEntityGreedyChest, Integer> otherGreedyNeighbors = new HashMap<>();

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int x2 = x + dir.offsetX;
            int y2 = y + dir.offsetY;
            int z2 = z + dir.offsetZ;
            TileEntity te = this.worldObj.getTileEntity(x2, y2, z2);
            if (te instanceof TileEntityGreedyChest) {
                TileEntityGreedyChest chest = (TileEntityGreedyChest)te;
                ForgeDirection opp = dir.getOpposite();
                if (chest == this || chest.modePilfer == 0 && chest.directions[opp.ordinal()] && chest.canTakeFromNeighbor(inv, opp)) {
                    int a = chest.getAvailableSpaceForItem(item, -1);
                    if (a > 0 && chest.filter != null) {
                        int[] result = chest.getMaxAllowedAfterFilter(item, source);
                        if (result != null) {
                            a = Math.min(a, result[0]);
                        }
                    }

                    if (a > 0) {
                        int d = dir.ordinal();
                        chests[d] = chest;
                        available[d] = a;
                        numChests++;
                        totalFraction += chest.fractionWanted;
                    } else {
                        otherGreedyNeighbors.put(chest, opp.ordinal());
                    }
                }
            }
        }

        if (numChests >= 1) {
            boolean fractionsOK = totalFraction < 1.0F || TjUtil.areFloatsAlmostEqual_lowprec(1.0F, totalFraction);
            float[] priority = new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F};
            int[] wantedArr = new int[6];
            HashableItemWithoutSize filterSensitiveItem = this.getFilterSensitiveItem(item);

            for (int d = 0; d < 6; d++) {
                TileEntityGreedyChest chest = chests[d];
                if (chest != null) {
                    float frac = fractionsOK ? chest.fractionWanted : chest.fractionWanted / totalFraction;
                    int wanted;
                    if (frac >= 1.0F) {
                        wanted = qtyAdded;
                    } else {
                        float f = qtyAdded * frac;
                        if (available[d] >= f) {
                            wanted = MathHelper.floor_float(f);
                            if (f > wanted) {
                                f -= wanted;
                                if (chest.partialItemTracker.containsKey(filterSensitiveItem)) {
                                    float val = chest.partialItemTracker.get(filterSensitiveItem);
                                    f += val;
                                    if (f >= 1.0F) {
                                        f--;
                                        wanted++;
                                        if (f <= 0.0F) {
                                            chest.partialItemTracker.remove(filterSensitiveItem);
                                        }

                                        priority[d]++;
                                    }
                                }

                                if (f > 0.0F) {
                                    chest.partialItemTracker.put(filterSensitiveItem, f);
                                    priority[d] += f;
                                }
                            }
                        } else {
                            wanted = available[d];
                        }
                    }

                    wantedArr[d] = Math.min(wanted, available[d]);
                }
            }

            float[] sortedPriority = (float[])priority.clone();
            Arrays.sort(sortedPriority);
            int amtShouldBeLeft = qtyNeighborTotal;

            for (int dx = 0; dx < 6; dx++) {
                if (chests[dx] != null) {
                    amtShouldBeLeft -= wantedArr[dx];
                }
            }

            if (amtShouldBeLeft < 0) {
                amtShouldBeLeft = 0;
            }

            int[] chestIndex = this.getShuffledArray(new int[]{0, 1, 2, 3, 4, 5});

            for (int num = 5; num >= 0; num--) {
                float currentPriority = sortedPriority[num];

                for (int i = 0; i < 6; i++) {
                    int dxx = chestIndex[i];
                    if (chests[dxx] != null && wantedArr[dxx] > 0 && TjUtil.areFloatsAlmostEqual_lowprec(priority[dxx], currentPriority)) {
                        chests[dxx].triggerTakeAmount(inv, ForgeDirection.getOrientation(dxx).getOpposite(), item, wantedArr[dxx], amtShouldBeLeft);
                        wantedArr[dxx] = 0;
                        break;
                    }
                }
            }

            int amtReallyLeft = this.countItemsOfTypeInInv(item, inv);

            for (int dxx = 0; dxx < 6; dxx++) {
                if (chests[dxx] != null) {
                    int opp = ForgeDirection.getOrientation(dxx).getOpposite().ordinal();
                    FilteringItemList map = chests[dxx].neighborInv[opp];
                    if (amtReallyLeft > 0) {
                        if (map == null) {
                            map = new FilteringItemList();
                            chests[dxx].neighborInv[opp] = map;
                        }

                        map.set(item, amtReallyLeft);
                    } else if (map != null) {
                        map.set(item, 0);
                        if (map.size() < 1) {
                            chests[dxx].neighborInv[opp] = null;
                        }
                    }
                }
            }

            for (Entry<TileEntityGreedyChest, Integer> entry : otherGreedyNeighbors.entrySet()) {
                TileEntityGreedyChest gc = entry.getKey();
                int opp = entry.getValue();
                FilteringItemList map = gc.neighborInv[opp];
                if (amtReallyLeft > 0) {
                    if (map == null) {
                        map = new FilteringItemList();
                        gc.neighborInv[opp] = map;
                    }

                    map.set(item, amtReallyLeft);
                } else if (map != null) {
                    map.set(item, 0);
                    if (map.size() < 1) {
                        gc.neighborInv[opp] = null;
                    }
                }
            }
        }
    }

    private int[] getShuffledArray(int[] arr) {
        Random rand = new Random();

        for (int i = arr.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            int a = arr[index];
            arr[index] = arr[i];
            arr[i] = a;
        }

        return arr;
    }

    protected void triggerTakeAmount(IInventory inv, ForgeDirection dir, HashableItemWithoutSize item, int amount, int amtShouldBeLeft) {
        int d = dir.ordinal();
        if (this.filter != null && this.filter.ignoreMetadata) {
            boolean var11 = true;
        } else {
            boolean var10000 = false;
        }

        if (this.filter != null && this.filter.ignoreNBT) {
            boolean var13 = true;
        } else {
            boolean var12 = false;
        }

        FilteringItemList additions = new FilteringItemList();
        additions.set(item, amount);
        boolean didTeleport = this.pilferFromNeighbor(inv, 0, -1, 0, dir, d, additions, null, amount);
        if (didTeleport && this.ticksSincePacket >= 4) {
            this.ticksSincePacket = 0;
            MessageGreedyChestStole.sendToClients(this.worldObj, this.xCoord, this.yCoord, this.zCoord, d);
        }
    }

    private int[] getMaxAllowedAfterFilter(HashableItemWithoutSize item, FilteringItemList source) {
        ItemStack keyStack = null;
        ItemStack stack = null;
        boolean found = false;

        for (int i = 0; i < this.filter.getSizeInventory(); i++) {
            stack = this.filter.getStackInSlot(i);
            if (stack != null && stack.getItem() == item.getItem() && (this.filter.ignoreMetadata || stack.getMetadata() == item.damage)) {
                if (this.filter.ignoreNBT) {
                    found = true;
                    break;
                }

                if (keyStack == null) {
                    keyStack = item.getItemStack(1);
                }

                if (ItemStack.areItemStackTagsEqual(stack, keyStack)) {
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            if (this.filterNamePattern != null) {
                if (keyStack == null) {
                    keyStack = item.getItemStack(1);
                }

                String name = keyStack.getDisplayName().toLowerCase();
                Matcher m = this.filterNamePattern.matcher(name);
                if (m.matches()) {
                    return new int[]{this.filterIsBlacklist ? 0 : Integer.MAX_VALUE, -1};
                }
            }

            return new int[]{this.filterIsBlacklist ? Integer.MAX_VALUE : 0, -1};
        } else if (!this.filter.useItemCount) {
            return new int[]{this.filterIsBlacklist ? 0 : Integer.MAX_VALUE, -1};
        } else if (this.filterIsBlacklist) {
            int qtyNeighborTotal = source.get(item, this.filter.ignoreMetadata, this.filter.ignoreNBT);
            return new int[]{qtyNeighborTotal - stack.stackSize, 2};
        } else {
            FilteringItemList dest = new FilteringItemList().populateFromInventory(this, false);
            int qtyHere = dest.get(item, this.filter.ignoreMetadata, this.filter.ignoreNBT);
            return new int[]{stack.stackSize - qtyHere, 3, qtyHere};
        }
    }

    private boolean isPercentageShareBoss(int x, int y, int z, TileEntity teInv) {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int x2 = x + dir.offsetX;
            int y2 = y + dir.offsetY;
            int z2 = z + dir.offsetZ;
            TileEntity te = this.worldObj.getTileEntity(x2, y2, z2);
            if (te instanceof TileEntityGreedyChest) {
                TileEntityGreedyChest tegc = (TileEntityGreedyChest)te;
                ForgeDirection opp = dir.getOpposite();
                if (tegc.directions[opp.ordinal()] && tegc.canTakeFromNeighbor(teInv, opp)) {
                    return tegc == this;
                }

                if (tegc == this) {
                    return false;
                }
            }
        }

        return false;
    }

    protected int countItemsOfType(HashableItemWithoutSize item) {
        return this.countItemsOfTypeInInv(item, this);
    }

    protected int countItemsOfTypeInInv(HashableItemWithoutSize item, IInventory inv) {
        int count = 0;

        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            ItemStack stack = inv.getStackInSlot(slot);
            if (stack != null && item.isSameItem(stack)) {
                count += stack.stackSize;
            }
        }

        return count;
    }

    @Override
    public void onInventoryChanged(int slot, ItemStack prevStack) {
        if (!this.doingInventoryUpdate && prevStack != null && (this.modePilfer == 1 || this.modePilfer == 3)) {
            ItemStack stack = this.inventorySlots[slot];
            if (stack == null || !TjUtil.areItemsEqualIgnoringSize(stack, prevStack) || stack.stackSize < prevStack.stackSize) {
                boolean foundValidDir = false;

                for (int i = 0; i < this.directions.length; i++) {
                    if (this.directions[i]) {
                        foundValidDir = true;
                        break;
                    }
                }

                if (foundValidDir) {
                    if (this.itemsRemoved == null) {
                        this.itemsRemoved = new HashMap<>();
                    }

                    this.itemsRemoved.put(new HashableItemWithoutSize(prevStack), "");
                    this.itemsRemovedChanged = this.ticksSinceSync;
                }
            }
        }
    }

    @Override
    public void onFilterInventoryChanged(int slot, ItemStack prevStack) {
        ItemStack stack = this.filterContainingInventory.getStackInSlot(slot);
        boolean changed = false;
        if (stack == null) {
            if (this.filter != null) {
                this.filter = null;
                changed = true;
            }
        } else {
            boolean hadFilter = this.filter != null;
            this.filter = ItemEnchantedPaper.getFilterInventory(stack);
            if (this.filter != null) {
                this.filterIsBlacklist = ItemEnchantedPaper.stackIsBlacklist(stack);
                if (this.filter.nameFilter.isEmpty()) {
                    this.filterNamePattern = null;
                } else {
                    this.filterNamePattern = TjUtil.getSafePatternUsingAsteriskForWildcard(this.filter.nameFilter.toLowerCase());
                }

                changed = true;
            } else if (hadFilter) {
                changed = true;
            }
        }

        if (changed) {
            for (int d = 0; d < 6; d++) {
                this.skipNextTheft[d] = true;
                this.neighborInv[d] = null;
            }

            this.partialItemTracker.clear();
        }
    }

    public void receiveActionStateFromServer(int side) {
        this.litRunesTime = 5;
        if (this.lidAngle < 0.01F) {
            this.lidAngle = 0.01F;
        }

        ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[side];
        int x = this.xCoord + dir.offsetX;
        int y = this.yCoord + dir.offsetY;
        int z = this.zCoord + dir.offsetZ;

        for (int i = 0; i < 2; i++) {
            this.worldObj
                .spawnParticle(
                    "portal",
                    x + 0.5,
                    y + this.worldObj.rand.nextDouble() * 2.0 - 1.0,
                    z + 0.5,
                    this.worldObj.rand.nextGaussian(),
                    0.0,
                    this.worldObj.rand.nextGaussian()
                );
            this.worldObj
                .spawnParticle(
                    "portal",
                    this.xCoord + 0.5,
                    this.yCoord + this.worldObj.rand.nextDouble() * 2.0 - 1.0,
                    this.zCoord + 0.5,
                    this.worldObj.rand.nextGaussian(),
                    0.0,
                    this.worldObj.rand.nextGaussian()
                );
        }

        this.worldObj.playSound(x + 0.5, y + 0.5, z + 0.5, "mob.endermen.portal", 0.15F, 1.1F + this.worldObj.rand.nextFloat() / 4.0F, false);
    }

    public void receiveGUIMessage(int type, float data) {
        switch (type) {
            case 6:
                this.modePilfer = Math.round(data);
                break;
            case 7:
                if (!TjUtil.areFloatsAlmostEqual_lowprec(this.fractionWanted, data)) {
                    if (TjUtil.areFloatsAlmostEqual_lowprec(data, 0.3333F)) {
                        this.fractionWanted = 0.33333334F;
                    } else if (TjUtil.areFloatsAlmostEqual_lowprec(data, 0.166F)) {
                        this.fractionWanted = 0.16666667F;
                    } else {
                        this.fractionWanted = data;
                    }

                    this.partialItemTracker.clear();
                }
                break;
            case 8:
                this.amountWanted = Math.round(data);
                break;
            default:
                if (type < 0 || type > 5) {
                    FMLLog.warning("[Automagy] TileEntityGreedyChest received invalid packet data. Ignoring. (type=" + type + ")");
                    return;
                }

                boolean b = Math.round(data) == 1;
                boolean oldB = this.directions[type];
                this.directions[type] = b;
                if (b) {
                    if (!oldB) {
                        this.skipNextTheft[type] = true;
                    }
                } else {
                    this.neighborInv[type] = null;
                }
        }

        this.markDirty(false);
    }

    public boolean canTakeFromNeighbor(Object inv, ForgeDirection dir) {
        if (inv instanceof TileEntityHopper && dir == ForgeDirection.DOWN) {
            return false;
        } else {
            return inv instanceof IRestrictedInventoryAccess ? ((IRestrictedInventoryAccess)inv).greedyChestCanSteal(this, dir.getOpposite()) : true;
        }
    }

    @Override
    public boolean greedyChestCanSteal(TileEntity greedyChest, ForgeDirection fromSide) {
        int d = fromSide.ordinal();
        return !this.directions[d];
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setShort("modePilfer", (short)this.modePilfer);
        nbttagcompound.setFloat("fractionWanted", this.fractionWanted);
        nbttagcompound.setInteger("amountWanted", this.amountWanted);
        setBooleanArrayInNbt(nbttagcompound, "directions", this.directions);
        this.filterContainingInventory.writeCustomNBT(nbttagcompound);
        if (this.worldObj == null || !this.worldObj.isRemote) {
            NBTTagList nbttaglist = new NBTTagList();

            for (Entry<HashableItemWithoutSize, Float> entry : this.partialItemTracker.entrySet()) {
                ItemStack stack = entry.getKey().getItemStack(1);
                if (stack != null) {
                    NBTTagCompound tagList = new NBTTagCompound();
                    stack.writeToNBT(tagList);
                    tagList.setFloat("Value", entry.getValue());
                    nbttaglist.appendTag(tagList);
                }
            }

            nbttagcompound.setTag("GreedPartialItems", nbttaglist);
        }
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.modePilfer = nbttagcompound.getShort("modePilfer");
        this.fractionWanted = nbttagcompound.getFloat("fractionWanted");
        this.amountWanted = nbttagcompound.getInteger("amountWanted");
        this.directions = getBooleanArrayFromNbtOrDefault(nbttagcompound, "directions", true, 6);
        this.filterContainingInventory.readCustomNBT(nbttagcompound);
        ItemStack filterStack = this.filterContainingInventory.getStackInSlot(0);
        if (filterStack == null) {
            this.filter = null;
        } else {
            this.filter = ItemEnchantedPaper.getFilterInventory(filterStack);
            if (this.filter != null) {
                this.filterIsBlacklist = ItemEnchantedPaper.stackIsBlacklist(filterStack);
                if (this.filter.nameFilter.isEmpty()) {
                    this.filterNamePattern = null;
                } else {
                    this.filterNamePattern = TjUtil.getSafePatternUsingAsteriskForWildcard(this.filter.nameFilter.toLowerCase());
                }
            }
        }

        if ((this.worldObj == null || !this.worldObj.isRemote) && this.modePilfer == 0) {
            NBTTagList nbttaglist = nbttagcompound.getTagList("GreedPartialItems", 10);
            this.partialItemTracker = new HashMap<>();

            for (int i = 0; i < nbttaglist.tagCount(); i++) {
                NBTTagCompound tagList = nbttaglist.getCompoundTagAt(i);
                float val = tagList.getFloat("Value");
                if (val > 0.0F) {
                    ItemStack stack = ItemStack.loadItemStackFromNBT(tagList);
                    if (stack != null) {
                        this.partialItemTracker.put(new HashableItemWithoutSize(stack), val);
                    }
                }
            }
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
}
