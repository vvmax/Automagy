package tuhljin.automagy.tiles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.gui.ContainerEagerChest;
import tuhljin.automagy.gui.ContainerEagerChestConfig;
import tuhljin.automagy.items.InventoryWithFilterOptions;
import tuhljin.automagy.items.ItemEnchantedPaper;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.FilteringItemList;
import tuhljin.automagy.lib.inventory.FilteringItemListLinked;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;

public class TileEntityEagerChest extends ModTileEntityWithInventory implements IChest, IContainsFilter {
    private static final int NUM_FILTER_ITEMS = 4;
    private static final int ANTICIPATION_COOLDOWN_RESET = 200;
    private static final int MAX_PLAYER_SLOT = 35;
    public InventoryObjectFilter filterContainingInventory;
    public HashSet<String> validUsers = new HashSet<>();
    public boolean clientIsValidUser = false;
    private int ticksSinceSync = -1;
    public float lidAngle;
    public float prevLidAngle;
    private int numUsingPlayers;
    private int anticipationCooldown = 0;
    private EntityPlayer prevClosestPlayer = null;
    private int timeSneaking = 0;
    private InventoryWithFilterOptions[] filter = new InventoryWithFilterOptions[4];
    private boolean[] filterIsBlacklist = new boolean[4];
    private Pattern[] filterNamePattern = new Pattern[4];
    private static HashMap<HashableItemWithoutSize, Integer> defaultItemCache = null;

    public TileEntityEagerChest() {
        super("blockEagerChest", 27);
        this.filterContainingInventory = new InventoryObjectFilter(this, "Eager Chest Filter", 4);
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
                    if (p.openContainer instanceof ContainerEagerChest && ((ContainerEagerChest)p.openContainer).getTileEntity() == this) {
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

            if (this.worldObj.isRemote) {
                if (this.anticipationCooldown > 0) {
                    this.anticipationCooldown--;
                } else if (this.numUsingPlayers == 0 && this.lidAngle < 0.01F) {
                    EntityPlayer closestPlayer = this.worldObj.getClosestPlayer(this.xCoord, this.yCoord, this.zCoord, 7.0);
                    if (closestPlayer != this.prevClosestPlayer) {
                        this.prevClosestPlayer = closestPlayer;
                        this.timeSneaking = 0;
                    }

                    if (closestPlayer != null
                        && (
                            !(closestPlayer.openContainer instanceof ContainerEagerChestConfig)
                                || ((ContainerEagerChestConfig)closestPlayer.openContainer).getTileEntity() != this
                        )) {
                        boolean sneak = closestPlayer.isSneaking();
                        double d = closestPlayer.getDistanceSq(this.xCoord, this.yCoord, this.zCoord);
                        d = Math.max(50.0 - d, 0.0);
                        float angle;
                        if (!sneak && this.isPlayerLookingAtThis(closestPlayer)) {
                            angle = (float)d / 800.0F;
                        } else {
                            angle = (float)d / 1200.0F;
                        }

                        if (sneak) {
                            this.timeSneaking++;
                            if (this.timeSneaking > 40) {
                                float f2x = 1.0F - (this.timeSneaking - 40) / 30.0F;
                                angle = Math.max(angle * f2x, 0.0F);
                            }

                            this.lidAngle = Math.min(this.prevLidAngle, angle);
                        } else {
                            this.timeSneaking = 0;
                            this.lidAngle = angle;
                        }
                    }
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
            if (this.worldObj.isRemote) {
                this.anticipationCooldown = 200;
            }
        }
    }

    @Override
    public void closeChest() {
        if (this.worldObj != null) {
            this.numUsingPlayers--;
            this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType(), 1, this.numUsingPlayers);
            if (this.worldObj.isRemote && this.numUsingPlayers < 1) {
                this.anticipationCooldown = 200;
                this.prevClosestPlayer = null;
            }
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

    public boolean isPlayerLookingAtThis(EntityPlayer player) {
        BlockCoord coord = TjUtil.getBlockCoordPlayerIsLookingAt(player, this.worldObj);
        return coord != null && coord.x == this.xCoord && coord.y == this.yCoord && coord.z == this.zCoord;
    }

    public boolean willTradeWithPlayer(EntityPlayer player) {
        String name = player.getCommandSenderName();
        return this.validUsers.contains(name);
    }

    public void setTradeWithPlayer(EntityPlayer player, boolean allow) {
        String name = player.getCommandSenderName();
        if (allow) {
            if (name != "Rcon") {
                this.validUsers.add(name);
            }
        } else if (this.validUsers.contains(name)) {
            this.validUsers.remove(name);
        }

        this.markDirty(false);
    }

    public void attemptTradeWithPlayer(EntityPlayer player) {
        if (!player.isSneaking() && this.willTradeWithPlayer(player)) {
            InventoryPlayer inv = player.inventory;
            FilteringItemListLinked playerInvMap = new FilteringItemListLinked().populateFromInventory(inv, false);
            FilteringItemListLinked myInvMap = new FilteringItemListLinked().populateFromInventory(this, true);
            boolean giveFilter = this.filter[0] != null || this.filter[1] != null;
            boolean takeFilter = this.filter[2] != null || this.filter[3] != null;
            boolean didTeleport = false;
            if (takeFilter || !giveFilter) {
                didTeleport = this.doInventoryTransfer(inv, 35, this, -1, playerInvMap, myInvMap, 2, 3, !takeFilter);
            }

            if (giveFilter || !takeFilter) {
                didTeleport = this.doInventoryTransfer(this, -1, inv, 35, myInvMap, playerInvMap, 0, 1, !giveFilter) || didTeleport;
            }

            if (takeFilter || !giveFilter) {
                didTeleport = this.doInventoryTransfer(inv, 35, this, -1, playerInvMap, myInvMap, 2, 3, !takeFilter) || didTeleport;
            }

            inv.markDirty();
            this.markDirty();
            if (didTeleport) {
                this.worldObj
                    .playSoundEffect(
                        this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, "mob.endermen.portal", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F
                    );
            }
        }
    }

    private boolean doInventoryTransfer(
        IInventory source,
        int sourceMaxSlot,
        IInventory dest,
        int destMaxSlot,
        FilteringItemListLinked sMap,
        FilteringItemListLinked dMap,
        int minFilterSlot,
        int maxFilterSlot,
        boolean defaultFilter
    ) {
        boolean reverse;
        if (sourceMaxSlot == -1) {
            reverse = false;
            sourceMaxSlot = source.getSizeInventory() - 1;
        } else {
            reverse = true;
        }

        boolean didTeleport = false;
        Iterator<Entry<HashableItemWithoutSize, Integer>> iter = sMap.getIterator();

        while (iter.hasNext()) {
            Entry<HashableItemWithoutSize, Integer> entry = iter.next();
            HashableItemWithoutSize key = entry.getKey();
            int qtySource = entry.getValue();
            int qtyDestination = dMap.get(key);
            int toTake;
            if (defaultFilter) {
                toTake = this.getMaxToMoveAfterDefaultFilter(key, source == this);
            } else {
                toTake = this.getMaxToMoveAfterFilter(minFilterSlot, maxFilterSlot, key, sMap, dMap);
            }

            if (toTake > 0) {
                int took = 0;

                for (int i = 0; i <= sourceMaxSlot; i++) {
                    int slot = reverse ? sourceMaxSlot - i : i;
                    ItemStack stack = source.getStackInSlot(slot);
                    if (stack != null && stack.stackSize > 0 && this.filterSensitiveIsSameItem(key, stack, minFilterSlot, maxFilterSlot)) {
                        stack = source.decrStackSize(slot, Math.min(toTake - took, stack.stackSize));
                        if (stack != null && stack.stackSize > 0) {
                            took += stack.stackSize;
                            this.addToInventory(stack, dest, destMaxSlot);
                            didTeleport = true;
                        }
                    }

                    if (toTake - took < 1) {
                        break;
                    }
                }

                if (took != 0) {
                    qtySource -= took;
                    qtyDestination += took;
                    sMap.set(key, qtySource, true);
                    dMap.set(key, qtyDestination, true);
                }
            }
        }

        return didTeleport;
    }

    protected boolean filterSensitiveIsSameItem(HashableItemWithoutSize item, ItemStack stack, int minFilterSlot, int maxFilterSlot) {
        for (int fs = minFilterSlot; fs <= maxFilterSlot; fs++) {
            boolean ignoreMetadata = this.filter[fs] != null && this.filter[fs].ignoreMetadata;
            boolean ignoreNBT = this.filter[fs] != null && this.filter[fs].ignoreNBT;
            if (item.isSameItem(stack, ignoreMetadata, ignoreNBT)) {
                return true;
            }
        }

        return false;
    }

    public void addToInventory(ItemStack stack, IInventory inv, int maxSlot) {
        boolean reverse;
        if (maxSlot == -1) {
            reverse = false;
            maxSlot = inv.getSizeInventory() - 1;
        } else {
            reverse = true;
        }

        if (stack.isStackable() && stack.getMaxStackSize() > 1) {
            for (int i = 0; i <= maxSlot; i++) {
                int slot = reverse ? maxSlot - i : i;
                ItemStack slottedStack = inv.getStackInSlot(slot);
                if (slottedStack != null && TjUtil.canItemsStack(stack, slottedStack)) {
                    int stacksTo = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
                    int newSize = stack.stackSize + slottedStack.stackSize;
                    if (newSize <= stacksTo) {
                        slottedStack.stackSize = newSize;
                        inv.setInventorySlotContents(slot, slottedStack);
                        return;
                    }

                    if (slottedStack.stackSize < stacksTo) {
                        stack.stackSize = newSize - stacksTo;
                        slottedStack.stackSize = stacksTo;
                        inv.setInventorySlotContents(slot, slottedStack);
                    }
                }
            }
        }

        for (int ix = 0; ix <= maxSlot; ix++) {
            int slot = reverse ? maxSlot - ix : ix;
            ItemStack slottedStack = inv.getStackInSlot(slot);
            if (slottedStack == null) {
                inv.setInventorySlotContents(slot, stack);
                return;
            }
        }

        TjUtil.dropItemIntoWorld(stack, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
    }

    public void receiveUserValidityFromServer(boolean clientIsValidUser) {
        this.clientIsValidUser = clientIsValidUser;
    }

    private int getMaxToMoveAfterDefaultFilter(HashableItemWithoutSize item, boolean giving) {
        if (defaultItemCache == null) {
            defaultItemCache = new HashMap<>();
            defaultItemCache.put(new HashableItemWithoutSize(new ItemStack(Blocks.dirt)), 1);
            defaultItemCache.put(new HashableItemWithoutSize(new ItemStack(Blocks.stone)), 1);
            defaultItemCache.put(new HashableItemWithoutSize(new ItemStack(Blocks.cobblestone)), 1);
            defaultItemCache.put(new HashableItemWithoutSize(new ItemStack(Blocks.gravel)), 1);
        }

        int val;
        if (!defaultItemCache.containsKey(item)) {
            AspectList aspects = new AspectList(item.getItemStack(1));
            val = aspects.getAmount(Aspect.GREED) > 0 ? 2 : 0;
            defaultItemCache.put(item, val);
        } else {
            val = defaultItemCache.get(item);
        }

        if (giving) {
            return val == 1 ? Integer.MAX_VALUE : 0;
        } else {
            return val == 2 ? Integer.MAX_VALUE : 0;
        }
    }

    private int getMaxToMoveAfterFilter(int minFilterSlot, int maxFilterSlot, HashableItemWithoutSize item, FilteringItemList source, FilteringItemList dest) {
        ItemStack keyStack = null;
        ItemStack stack = null;
        int found = -1;

        label102:
        for (int fs = minFilterSlot; fs <= maxFilterSlot; fs++) {
            if (this.filter[fs] != null) {
                for (int i = 0; i < this.filter[fs].getSizeInventory(); i++) {
                    stack = this.filter[fs].getStackInSlot(i);
                    if (stack != null && stack.getItem() == item.getItem() && (this.filter[fs].ignoreMetadata || stack.getMetadata() == item.damage)) {
                        if (this.filter[fs].ignoreNBT) {
                            found = fs;
                            break label102;
                        }

                        if (keyStack == null) {
                            keyStack = item.getItemStack(1);
                        }

                        if (ItemStack.areItemStackTagsEqual(stack, keyStack)) {
                            found = fs;
                            break label102;
                        }
                    }
                }
            }
        }

        if (found == -1) {
            for (int fsx = minFilterSlot; fsx <= maxFilterSlot; fsx++) {
                if (this.filter[fsx] != null && this.filterNamePattern[fsx] != null) {
                    if (keyStack == null) {
                        keyStack = item.getItemStack(1);
                    }

                    String name = keyStack.getDisplayName().toLowerCase();
                    Matcher m = this.filterNamePattern[fsx].matcher(name);
                    if (m.matches()) {
                        return this.filterIsBlacklist[fsx] ? 0 : Integer.MAX_VALUE;
                    }
                }
            }

            for (int fsxx = minFilterSlot; fsxx <= maxFilterSlot; fsxx++) {
                if (this.filter[fsxx] != null && !this.filterIsBlacklist[fsxx]) {
                    return 0;
                }
            }

            return Integer.MAX_VALUE;
        } else if (!this.filter[found].useItemCount) {
            return this.filterIsBlacklist[found] ? 0 : Integer.MAX_VALUE;
        } else if (this.filterIsBlacklist[found]) {
            int qtySource = source.get(item, this.filter[found].ignoreMetadata, this.filter[found].ignoreNBT);
            return qtySource - stack.stackSize;
        } else {
            int qtyDestination = dest.get(item, this.filter[found].ignoreMetadata, this.filter[found].ignoreNBT);
            return stack.stackSize - qtyDestination;
        }
    }

    protected LinkedHashMap<HashableItemWithoutSize, Integer> getMappedInventory(IInventory inv, boolean ignoreMeta, boolean ignoreNBT) {
        int size = inv.getSizeInventory();
        LinkedHashMap<HashableItemWithoutSize, Integer> map = new LinkedHashMap<>();

        for (int i1 = 0; i1 < size; i1++) {
            ItemStack stack = inv.getStackInSlot(i1);
            if (stack != null) {
                if (ignoreMeta) {
                    ItemStack newStack = new ItemStack(stack.getItem(), 1, 0);
                    if (!ignoreNBT && stack.hasTagCompound()) {
                        newStack.setTagCompound(stack.getTagCompound());
                    }

                    stack = newStack;
                } else if (ignoreNBT && stack.hasTagCompound()) {
                    stack.setTagCompound(null);
                }

                HashableItemWithoutSize item = new HashableItemWithoutSize(stack);
                if (map.containsKey(item)) {
                    map.put(item, map.get(item) + stack.stackSize);
                } else {
                    map.put(item, stack.stackSize);
                }
            }
        }

        return map;
    }

    @Override
    public void onFilterInventoryChanged(int slot, ItemStack prevStack) {
        ItemStack stack = this.filterContainingInventory.getStackInSlot(slot);
        if (stack == null) {
            this.filter[slot] = null;
        } else {
            this.filter[slot] = ItemEnchantedPaper.getFilterInventory(stack);
            if (this.filter[slot] != null) {
                this.filterIsBlacklist[slot] = ItemEnchantedPaper.stackIsBlacklist(stack);
                if (this.filter[slot].nameFilter.isEmpty()) {
                    this.filterNamePattern[slot] = null;
                } else {
                    this.filterNamePattern[slot] = TjUtil.getSafePatternUsingAsteriskForWildcard(this.filter[slot].nameFilter.toLowerCase());
                }
            }
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        this.filterContainingInventory.writeCustomNBT(nbttagcompound);
        NBTTagList nbttaglist = new NBTTagList();

        for (String name : this.validUsers) {
            if (name != null && !name.isEmpty()) {
                NBTTagCompound tagList = new NBTTagCompound();
                tagList.setString("User", name);
                nbttaglist.appendTag(tagList);
            }
        }

        nbttagcompound.setTag("ValidUsers", nbttaglist);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.filterContainingInventory.readCustomNBT(nbttagcompound);

        for (int slot = 0; slot < 4; slot++) {
            ItemStack filterStack = this.filterContainingInventory.getStackInSlot(slot);
            if (filterStack == null) {
                this.filter[slot] = null;
            } else {
                this.filter[slot] = ItemEnchantedPaper.getFilterInventory(filterStack);
                if (this.filter[slot] != null) {
                    this.filterIsBlacklist[slot] = ItemEnchantedPaper.stackIsBlacklist(filterStack);
                    if (this.filter[slot].nameFilter.isEmpty()) {
                        this.filterNamePattern[slot] = null;
                    } else {
                        this.filterNamePattern[slot] = TjUtil.getSafePatternUsingAsteriskForWildcard(this.filter[slot].nameFilter.toLowerCase());
                    }
                }
            }
        }

        NBTTagList nbttaglist = nbttagcompound.getTagList("ValidUsers", 10);
        this.validUsers.clear();
        int size = nbttaglist.tagCount();

        for (int i = 0; i < size; i++) {
            NBTTagCompound tagList = nbttaglist.getCompoundTagAt(i);
            String name = tagList.getString("User");
            if (name != null && !name.isEmpty()) {
                this.validUsers.add(name);
            }
        }
    }
}
