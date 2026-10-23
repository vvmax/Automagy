package tuhljin.automagy.tiles;

import cpw.mods.fml.common.FMLLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.ItemApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockCrystal;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.EntityAspectOrb;
import thaumcraft.common.entities.golems.EntityGolemBase;
import tuhljin.automagy.api.AutomagyAPI;
import tuhljin.automagy.api.inventarium.IInventariumContentsProvider;
import tuhljin.automagy.blocks.IInventariumSlotUpgrade;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.entities.GolemTaskData;
import tuhljin.automagy.gui.GUIScribe;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.FilteringItemList;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.lib.inventory.MappedItemsOrderedByTimeAdded;
import tuhljin.automagy.lib.inventory.MappedItemsWithDestinations;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.network.MessageParticles;

public class TileEntityInventarium extends ModTileEntity implements IHasItemList, IReceivesItemListPacket {
    public static int MAX_INVNODES_START = 1;
    public static int MAX_INVNODES_INC_CLUSTER = 1;
    public static int MAXDISTANCE_START = 10;
    public static int MAXDISTANCE_INC_CLUSTER = 8;
    public static int STANDARD_SLOTS_PER_SHELF = 81;
    public static int MEMORYUSERS_PER_PROVIDER = 1;
    public static int RELATIVEMAX_Y_FOR_MEMORYBLOCK = -2;
    public static final String TAG_ITEMS_REQUESTED = "ItemsRequested";
    public static final String TAG_ITEMS_ENROUTE = "ItemsEnRoute";
    public static final String TAG_ITEMS_DESTINATIONS = "ItemsDestinations";
    public static int REFRESH_RATE = 400;
    public static int MAX_PREFERRED_TIME = 12000;
    public static HashMap<Integer, HashSet<BlockCoord>> coreLocations = new HashMap<>();
    public int refreshCountdown = REFRESH_RATE;
    public int maxInvNodes;
    public int maxMemoryUsers;
    public int maxDistance;
    public int maxSlotsRead;
    public int usedSlots = 0;
    public int activeNodes = 0;
    protected Boolean hasMultiDestMirror = null;
    private FilteringItemList itemList = null;
    private HashSet<BlockCoord> nodes = new HashSet<>();
    private HashSet<BlockCoord> memoryUsers = new HashSet<>();
    public static int VALIDITY_CHECK_FREQUENCY = 80;
    private int ticksSinceValidityCheck;
    private boolean addedToMap = false;
    protected static int getContentsIdentifier = 0;
    protected MappedItemsOrderedByTimeAdded requestedItems = new MappedItemsOrderedByTimeAdded("ItemsRequested");
    protected MappedItemsOrderedByTimeAdded requestedItemsEnRoute = new MappedItemsOrderedByTimeAdded("ItemsEnRoute");
    protected MappedItemsWithDestinations requestedItemsDestinations = new MappedItemsWithDestinations("ItemsDestinations");
    private int ticksSinceRequestDestinationsEmptied = 0;
    protected int lastTaskID = 0;
    protected Map<Integer, ItemStack> stacksEnRouteByTaskID = new HashMap<>();
    protected Map<Integer, Boolean> taskReturning = new HashMap<>();
    private Map<HashableItemWithoutSize, Integer> requestsClaimedThisTick = new HashMap<>();
    private boolean constructStrikeOne = false;
    private long timeMarkedAsPreferredLink = 0L;
    public int ticksToAnim = 0;
    private Object beam = null;
    public static int rotationSpeedFactor = 3240;
    public static double clientRenderFloatingTop = 0.1;
    public static double clientRenderFloatingBtm = -0.3;
    public int clientRenderRotationHelper = 0;
    public double clientRenderFloatingDistance = clientRenderFloatingTop;
    public boolean clientRenderGoingUp = false;

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote) {
            if (!this.addedToMap) {
                int dimension = this.worldObj.provider.dimensionId;
                HashSet<BlockCoord> bcs;
                if (coreLocations.containsKey(dimension)) {
                    bcs = coreLocations.get(dimension);
                } else {
                    bcs = new HashSet<>();
                    coreLocations.put(dimension, bcs);
                }

                bcs.add(new BlockCoord(this));
                this.addedToMap = true;
                this.checkMaxes();
            }

            this.itemList = null;
            this.requestsClaimedThisTick.clear();
            if (!this.requestedItemsDestinations.isEmpty() && this.requestedItems.isEmpty()) {
                this.ticksSinceRequestDestinationsEmptied++;
                if (this.ticksSinceRequestDestinationsEmptied > 1200) {
                    this.requestedItemsDestinations.clear();
                    this.ticksSinceRequestDestinationsEmptied = 0;
                    this.markDirty(false);
                }
            } else {
                this.ticksSinceRequestDestinationsEmptied = 0;
            }

            if (this.ticksSinceValidityCheck >= VALIDITY_CHECK_FREQUENCY) {
                this.ticksSinceValidityCheck = 0;
                if (!checkValidity(this.worldObj, this.xCoord, this.yCoord, this.zCoord, false)) {
                    if (this.constructStrikeOne) {
                        this.getBlockType().dropBlockAsItem(this.worldObj, this.xCoord, this.yCoord, this.zCoord, 0, 0);
                        this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
                    } else {
                        this.constructStrikeOne = true;
                        this.ticksSinceValidityCheck = VALIDITY_CHECK_FREQUENCY / 2;
                    }

                    return;
                }

                this.checkMaxes();
                this.constructStrikeOne = false;
            } else {
                this.ticksSinceValidityCheck++;
            }

            if (this.worldObj.rand.nextInt(1500) == 0) {
                EntityAspectOrb orb = new EntityAspectOrb(this.worldObj, this.xCoord + 0.5, this.yCoord - 0.5, this.zCoord + 0.5, Aspect.ORDER, 1);
                this.worldObj.spawnEntityInWorld(orb);
            }

            this.refreshCountdown--;
            if (this.refreshCountdown < 1 && this.refreshCountdown == 0) {
                this.getItemList();
                this.refreshCountdown = REFRESH_RATE;
            }
        } else {
            if (this.ticksToAnim > 0) {
                this.ticksToAnim--;
                this.beam = Thaumcraft.proxy
                    .beamBore(
                        this.worldObj,
                        this.xCoord + 0.5,
                        this.yCoord + 0.7,
                        this.zCoord + 0.5,
                        this.xCoord + 0.5,
                        this.yCoord - 2,
                        this.zCoord + 0.5,
                        10,
                        10053375,
                        true,
                        10.0F,
                        this.beam,
                        0
                    );
            }

            this.clientRenderRotationHelper += 3;
            if (this.clientRenderRotationHelper >= rotationSpeedFactor) {
                this.clientRenderRotationHelper = this.clientRenderRotationHelper - rotationSpeedFactor;
            }

            double adj = 0.0;
            if (this.clientRenderFloatingDistance < clientRenderFloatingBtm) {
                this.clientRenderGoingUp = true;
            } else if (this.clientRenderFloatingDistance > clientRenderFloatingTop) {
                this.clientRenderGoingUp = false;
            }

            adj += this.clientRenderGoingUp ? 0.002 : -0.002;
            this.clientRenderFloatingDistance += adj;
        }
    }

    public void checkMaxes() {
        int clusters = 0;
        int memoryProviders = 0;
        this.maxSlotsRead = 0;

        for (int x = -3; x < 4; x++) {
            for (int y = -3; y < 4; y++) {
                for (int z = -3; z < 4; z++) {
                    Block block = this.worldObj.getBlock(this.xCoord + x, this.yCoord + y, this.zCoord + z);
                    int slots = getBookshelfSlotsProvidedAt(block, this.worldObj, this.xCoord + x, this.yCoord + y, this.zCoord + z);
                    if (slots > 0) {
                        this.maxSlotsRead += slots;
                    } else if (block instanceof BlockCrystal && this.worldObj.getBlockMetadata(this.xCoord + x, this.yCoord + y, this.zCoord + z) == 4) {
                        clusters++;
                    } else if (y <= RELATIVEMAX_Y_FOR_MEMORYBLOCK && block == ModBlocks.metalGlow) {
                        int md = this.worldObj.getBlockMetadata(this.xCoord + x, this.yCoord + y, this.zCoord + z);
                        if (md == 0) {
                            if (neighborIsAmber(this.worldObj, this.xCoord + x, this.yCoord + y, this.zCoord + z)) {
                                memoryProviders++;
                                this.worldObj.setBlockMetadataWithNotify(this.xCoord + x, this.yCoord + y, this.zCoord + z, 1, 3);
                            }
                        } else if (md == 1) {
                            if (neighborIsAmber(this.worldObj, this.xCoord + x, this.yCoord + y, this.zCoord + z)) {
                                memoryProviders++;
                            } else {
                                this.worldObj.setBlockMetadataWithNotify(this.xCoord + x, this.yCoord + y, this.zCoord + z, 0, 3);
                            }
                        }
                    }
                }
            }
        }

        int oldDistance = this.maxDistance;
        this.maxDistance = MAXDISTANCE_START + MAXDISTANCE_INC_CLUSTER * clusters;
        int oldInvNodes = this.maxInvNodes;
        this.maxInvNodes = clusters + 1;
        if (this.maxInvNodes != oldInvNodes) {
            Iterator<BlockCoord> iter = this.nodes.iterator();

            while (iter.hasNext()) {
                BlockCoord entry = iter.next();
                if (this.getDistanceIfInRange(entry.x, entry.y, entry.z) == -1.0) {
                    iter.remove();
                    TileEntity te = this.worldObj.getTileEntity(entry.x, entry.y, entry.z);
                    if (te instanceof TileEntityUnseenScribe) {
                        ((TileEntityUnseenScribe)te).breakLink();
                    }
                }
            }

            if (this.nodes.size() > this.maxInvNodes) {
                int c = 0;
                HashSet<BlockCoord> newNodes = new HashSet<>();

                for (BlockCoord bc : this.nodes) {
                    if (++c <= this.maxInvNodes) {
                        newNodes.add(bc);
                    } else {
                        TileEntity te = this.worldObj.getTileEntity(bc.x, bc.y, bc.z);
                        if (te instanceof TileEntityUnseenScribe) {
                            ((TileEntityUnseenScribe)te).breakLink();
                        }
                    }
                }

                this.nodes = newNodes;
            }
        }

        int oldMemoryUsers = this.maxMemoryUsers;
        this.maxMemoryUsers = memoryProviders * MEMORYUSERS_PER_PROVIDER;
        if (this.maxMemoryUsers != oldMemoryUsers || this.maxDistance != oldDistance) {
            Iterator<BlockCoord> iter = this.memoryUsers.iterator();

            while (iter.hasNext()) {
                BlockCoord entry = iter.next();
                if (this.getDistanceIfInRange(entry.x, entry.y, entry.z) == -1.0) {
                    iter.remove();
                    TileEntity te = this.worldObj.getTileEntity(entry.x, entry.y, entry.z);
                    if (te instanceof IInventariumMemoryUser) {
                        ((IInventariumMemoryUser)te).breakLink();
                    }
                }
            }

            if (this.memoryUsers.size() > this.maxMemoryUsers) {
                int c = 0;
                HashSet<BlockCoord> newNodes = new HashSet<>();

                for (BlockCoord bcx : this.memoryUsers) {
                    if (++c <= this.maxMemoryUsers) {
                        newNodes.add(bcx);
                    } else {
                        TileEntity te = this.worldObj.getTileEntity(bcx.x, bcx.y, bcx.z);
                        if (te instanceof IInventariumMemoryUser) {
                            ((IInventariumMemoryUser)te).breakLink();
                        }
                    }
                }

                this.memoryUsers = newNodes;
            }

            this.hasMultiDestMirror = null;
            this.markDirty();
        }
    }

    public void removeMultiDestMirror() {
        this.hasMultiDestMirror = null;
    }

    public static boolean checkValidity(IBlockAccess blockaccess, int x, int y, int z, boolean placementCheck) {
        if (placementCheck) {
            ItemStack crystal = ItemApi.getBlock("blockCrystal", 4);
            if (blockaccess.getBlock(x, y - 1, z) != Block.getBlockFromItem(crystal.getItem()) || blockaccess.getBlockMetadata(x, y - 1, z) != 4) {
                return false;
            }
        } else if (!blockaccess.isAirBlock(x, y - 1, z)) {
            return false;
        }

        if (!blockaccess.isAirBlock(x - 1, y - 1, z - 1)
            || !blockaccess.isAirBlock(x - 1, y - 1, z)
            || !blockaccess.isAirBlock(x - 1, y - 1, z + 1)
            || !blockaccess.isAirBlock(x, y - 1, z - 1)
            || !blockaccess.isAirBlock(x, y - 1, z + 1)
            || !blockaccess.isAirBlock(x + 1, y - 1, z - 1)
            || !blockaccess.isAirBlock(x + 1, y - 1, z)
            || !blockaccess.isAirBlock(x + 1, y - 1, z + 1)
            || !blockaccess.isAirBlock(x - 1, y, z - 1)
            || !blockaccess.isAirBlock(x - 1, y, z)
            || !blockaccess.isAirBlock(x - 1, y, z + 1)
            || !blockaccess.isAirBlock(x, y, z - 1)
            || !blockaccess.isAirBlock(x, y, z + 1)
            || !blockaccess.isAirBlock(x + 1, y, z - 1)
            || !blockaccess.isAirBlock(x + 1, y, z)
            || !blockaccess.isAirBlock(x + 1, y, z + 1)) {
            return false;
        } else {
            return !blockIsAmber(blockaccess, x, y - 2, z)
                ? false
                : blockIsBookshelf(blockaccess, x - 2, y - 1, z - 1)
                    && blockIsBookshelf(blockaccess, x - 2, y - 1, z + 1)
                    && blockIsBookshelf(blockaccess, x + 2, y - 1, z - 1)
                    && blockIsBookshelf(blockaccess, x + 2, y - 1, z + 1)
                    && blockIsBookshelf(blockaccess, x - 1, y - 1, z - 2)
                    && blockIsBookshelf(blockaccess, x + 1, y - 1, z - 2)
                    && blockIsBookshelf(blockaccess, x - 1, y - 1, z + 2)
                    && blockIsBookshelf(blockaccess, x + 1, y - 1, z + 2);
        }
    }

    public static boolean blockIsBookshelf(IBlockAccess blockaccess, int x, int y, int z) {
        return getBookshelfSlotsProvidedAt(blockaccess, x, y, z) > 0;
    }

    public static int getBookshelfSlotsProvidedAt(IBlockAccess blockaccess, int x, int y, int z) {
        return getBookshelfSlotsProvidedAt(blockaccess.getBlock(x, y, z), blockaccess, x, y, z);
    }

    private static int getBookshelfSlotsProvidedAt(Block block, IBlockAccess blockaccess, int x, int y, int z) {
        if (block != null) {
            if (block == Blocks.bookshelf) {
                return STANDARD_SLOTS_PER_SHELF;
            }

            if (block instanceof IInventariumSlotUpgrade) {
                return ((IInventariumSlotUpgrade)block).getInventariumSlotAllowance(blockaccess, x, y, z);
            }

            if (AutomagyAPI.invOtherBookshelves.contains(block)) {
                return STANDARD_SLOTS_PER_SHELF;
            }
        }

        return 0;
    }

    public static boolean blockIsAmber(IBlockAccess blockaccess, int x, int y, int z) {
        Block block = blockaccess.getBlock(x, y, z);
        if (block == ConfigBlocks.blockCosmeticOpaque) {
            int md = blockaccess.getBlockMetadata(x, y, z);
            if (md == 0 || md == 1) {
                return true;
            }
        } else if (block == ModBlocks.translucent) {
            return true;
        }

        return false;
    }

    public static boolean neighborIsAmber(IBlockAccess blockaccess, int x, int y, int z) {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (blockIsAmber(blockaccess, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)) {
                return true;
            }
        }

        return false;
    }

    public void beginPlacementProcess() {
        ItemStack crystal = ItemApi.getBlock("blockCrystal", 4);
        if (this.worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord) == Block.getBlockFromItem(crystal.getItem())
            && this.worldObj.getBlockMetadata(this.xCoord, this.yCoord - 1, this.zCoord) == 4) {
            MessageParticles.sendToClients(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.xCoord, this.yCoord - 1, this.zCoord, (short)4);
            this.worldObj.setBlockToAir(this.xCoord, this.yCoord - 1, this.zCoord);
        } else {
            this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
            TjUtil.dropItemIntoWorld(new ItemStack(ModBlocks.inventariumCore), this.worldObj, this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    public FilteringItemList getItemList() {
        this.usedSlots = 0;
        if (this.worldObj.isRemote) {
            return this.itemList == null ? new FilteringItemList() : this.itemList;
        } else {
            if (this.itemList == null) {
                getContentsIdentifier++;
                this.activeNodes = 0;
                int slotCount = 0;
                ArrayList<TileEntityInventarium.PairedContentsProvider> invs = new ArrayList<>();
                HashSet<BlockCoord> invPos = new HashSet<>();
                Iterator<BlockCoord> iter = this.nodes.iterator();

                while (iter.hasNext()) {
                    BlockCoord entry = iter.next();
                    if (TjUtil.isChunkLoaded(this.worldObj, entry.x, entry.z)) {
                        TileEntity te = this.worldObj.getTileEntity(entry.x, entry.y, entry.z);
                        if (!(te instanceof TileEntityUnseenScribe)) {
                            iter.remove();
                        } else {
                            TileEntityUnseenScribe scribe = (TileEntityUnseenScribe)te;
                            if (scribe.linkX != this.xCoord && scribe.linkY != this.yCoord && scribe.linkZ != this.zCoord) {
                                iter.remove();
                            } else {
                                ArrayList<BlockCoord> bcs = scribe.getInventoryCoords();
                                if (bcs != null) {
                                    this.activeNodes++;
                                    boolean anyOmitted = false;

                                    for (BlockCoord bc : bcs) {
                                        if (!invPos.contains(bc)) {
                                            invPos.add(bc);
                                            TileEntity te2 = this.worldObj.getTileEntity(bc.x, bc.y, bc.z);
                                            if (te2 != null) {
                                                IInventariumContentsProvider provider = getContentsProvider(te2);
                                                if (provider != null) {
                                                    int slots = provider.getSlotCount(te2, getContentsIdentifier);
                                                    if (slots > 0) {
                                                        if (slotCount + slots <= this.maxSlotsRead) {
                                                            invs.add(new TileEntityInventarium.PairedContentsProvider(provider, te2));
                                                            slotCount += slots;
                                                        } else {
                                                            anyOmitted = true;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (anyOmitted) {
                                        if (!scribe.someSlotsOmitted) {
                                            scribe.flagSlotsOmitted(true);
                                        }
                                    } else if (scribe.someSlotsOmitted) {
                                        scribe.flagSlotsOmitted(false);
                                    }

                                    if (this.activeNodes >= this.maxInvNodes) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                this.itemList = new FilteringItemList().populateFromInventariumContentsProviders(invs, getContentsIdentifier);
                this.usedSlots = slotCount;
            }

            return this.activeNodes > 0 ? this.itemList : null;
        }
    }

    public void verifyMemoryUsers() {
        Iterator<BlockCoord> iter = this.memoryUsers.iterator();

        while (iter.hasNext()) {
            BlockCoord entry = iter.next();
            if (TjUtil.isChunkLoaded(this.worldObj, entry.x, entry.z)) {
                TileEntity te = this.worldObj.getTileEntity(entry.x, entry.y, entry.z);
                if (!(te instanceof IInventariumMemoryUser)) {
                    iter.remove();
                    this.hasMultiDestMirror = null;
                } else if (!((IInventariumMemoryUser)te).isLinkedTo(this)) {
                    iter.remove();
                    this.hasMultiDestMirror = null;
                }
            }
        }
    }

    @Override
    public void receiveItemListPacket(ArrayList<ItemStack> list, boolean partial) {
        if (partial && this.itemList != null) {
            for (ItemStack stack : list) {
                this.itemList.set(new HashableItemWithoutSize(stack), stack.stackSize > 0 ? stack.stackSize : 0);
            }

            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            if (gui instanceof GUIScribe) {
                ((GUIScribe)gui).setStacks(this.getItemList(), false);
            }
        } else {
            this.itemList = new FilteringItemList(list, false);
        }
    }

    public void receiveItemsRequestedListPacket(ArrayList<ItemStack> list, boolean partial) {
        if (!partial) {
            this.requestedItems.setAll(list);
        } else {
            boolean needUpdate = false;
            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            GUIScribe guiScribe = gui instanceof GUIScribe ? (GUIScribe)gui : null;

            for (ItemStack stack : list) {
                HashableItemWithoutSize item = new HashableItemWithoutSize(stack);
                this.requestedItems.set(item, stack.stackSize);
                if (guiScribe != null) {
                    int amt = guiScribe.getDisplayedRequestAmount(item);
                    if (amt == 0 || amt != stack.stackSize) {
                        needUpdate = true;
                    }
                }
            }

            if (needUpdate) {
                guiScribe.updateRequestedItems(this.getRequestedItems());
            }
        }
    }

    public void receiveRequestFromClient(ItemStack stack, boolean rearrange, IInventariumBookLinksMirror book) {
        if (stack != null) {
            if (stack.stackSize > 0) {
                this.requestedItems.add(stack, rearrange);
                if (book != null && this.isTrackingDestinations()) {
                    this.addRequestForDestination(book.getRequesterCoord(), stack);
                }

                this.markDirty(false);
            } else if (stack.stackSize < 0) {
                stack.stackSize *= -1;
                this.requestedItems.subtract(stack);
                if (!this.requestedItemsDestinations.isEmpty()) {
                    this.subtractRequestForDestination(book == null ? null : book.getRequesterCoord(), stack, true);
                }

                this.markDirty(false);
            }
        }
    }

    public MappedItemsOrderedByTimeAdded getRequestedItems() {
        return this.requestedItems;
    }

    public int getRequestedAmountEnRoute(HashableItemWithoutSize item) {
        return this.requestedItemsEnRoute.get(item);
    }

    public void markRequestEnRoute(EntityGolemBase golem, ItemStack stack) {
        this.lastTaskID++;

        while (this.lastTaskID == 0 || this.stacksEnRouteByTaskID.containsKey(this.lastTaskID)) {
            this.lastTaskID++;
        }

        this.requestedItemsEnRoute.add(stack, false);
        this.stacksEnRouteByTaskID.put(this.lastTaskID, stack);
        GolemTaskData.setTaskID(golem, this.lastTaskID);
        this.markDirty(false);
    }

    public boolean markRequestPhaseTwo(EntityGolemBase golem) {
        int id = GolemTaskData.get(golem).getTaskID();
        ItemStack claimStack = this.stacksEnRouteByTaskID.get(id);
        if (claimStack != null) {
            ItemStack carriedStack = golem.getCarried();
            if (TjUtil.areItemsEqualIgnoringSize(claimStack, carriedStack) && carriedStack.stackSize > 0) {
                if (claimStack.stackSize > carriedStack.stackSize) {
                    ItemStack sub = claimStack.copy();
                    sub.stackSize = claimStack.stackSize - carriedStack.stackSize;
                    this.requestedItemsEnRoute.subtract(sub);
                    this.stacksEnRouteByTaskID.put(id, carriedStack.copy());
                } else if (claimStack.stackSize < carriedStack.stackSize) {
                    ThaumcraftExtension.limitGolemCarrying(golem, claimStack.stackSize);
                }

                this.taskReturning.put(id, true);
                return true;
            }

            this.requestFailedByGolem(golem);
            ThaumcraftExtension.limitGolemCarrying(golem, 0);
        }

        return false;
    }

    public void requestFulfilledByGolem(EntityGolemBase golem) {
        int id = GolemTaskData.get(golem).getTaskID();
        ItemStack stack = this.stacksEnRouteByTaskID.get(id);
        if (stack != null) {
            this.requestedItems.subtract(stack);
            this.requestedItemsEnRoute.subtract(stack);
            this.stacksEnRouteByTaskID.remove(id);
            this.taskReturning.remove(id);
            GolemTaskData.clearTaskID(golem);
            ThaumcraftExtension.clearGolemItemFilter(golem);
            this.markDirty(false);
        }
    }

    public void requestFailedByGolem(EntityGolemBase golem) {
        int id = GolemTaskData.get(golem).getTaskID();
        ItemStack stack = this.stacksEnRouteByTaskID.get(id);
        if (stack != null) {
            this.requestedItemsEnRoute.subtract(stack);
            this.stacksEnRouteByTaskID.remove(id);
            this.taskReturning.remove(id);
            GolemTaskData.clearTaskID(golem);
            ThaumcraftExtension.clearGolemItemFilter(golem);
            this.markDirty(false);
        }
    }

    public void golemContinueClaim(EntityGolemBase golem, int taskID) {
        ItemStack claimStack = this.stacksEnRouteByTaskID.get(taskID);
        if (claimStack == null) {
            GolemTaskData.clearTaskID(golem);
            ThaumcraftExtension.clearGolemItemFilter(golem);
            ThaumcraftExtension.limitGolemCarrying(golem, 0);
        } else {
            HashableItemWithoutSize item = new HashableItemWithoutSize(claimStack);
            Integer claimedThisTick = this.requestsClaimedThisTick.get(item);
            if (claimedThisTick == null) {
                claimedThisTick = 0;
            }

            int available = this.requestedItems.get(item) - claimedThisTick;
            if (available < 1) {
                this.requestFailedByGolem(golem);
                ThaumcraftExtension.limitGolemCarrying(golem, 0);
            } else {
                if (available < claimStack.stackSize) {
                    ItemStack sub = claimStack.copy();
                    sub.stackSize = claimStack.stackSize - available;
                    this.requestedItemsEnRoute.subtract(sub);
                    claimStack.stackSize = available;
                    this.stacksEnRouteByTaskID.put(taskID, claimStack);
                    ThaumcraftExtension.setGolemItemFilter(golem, claimStack);
                    ThaumcraftExtension.limitGolemCarrying(golem, available);
                } else if (available > claimStack.stackSize && !this.isTaskInPhaseTwo(taskID)) {
                    int maxCarry = ThaumcraftExtension.getMaxGolemCanCarry(golem, claimStack);
                    if (maxCarry > claimStack.stackSize) {
                        int neverClaimed = this.requestedItems.get(item) - this.requestedItemsEnRoute.get(item);
                        int amt = Math.min(Math.min(available, claimStack.stackSize + neverClaimed), maxCarry);
                        if (amt > claimStack.stackSize) {
                            ItemStack add = claimStack.copy();
                            add.stackSize = amt - claimStack.stackSize;
                            this.requestedItemsEnRoute.add(add);
                            claimStack.stackSize = amt;
                            this.stacksEnRouteByTaskID.put(taskID, claimStack);
                            ThaumcraftExtension.setGolemItemFilter(golem, claimStack);
                            if (golem.itemWatched != null) {
                                golem.itemWatched.stackSize = claimStack.stackSize;
                            }
                        }
                    }
                }

                this.requestsClaimedThisTick.put(item, claimedThisTick + claimStack.stackSize);
            }
        }
    }

    public boolean isTaskInPhaseTwo(int taskID) {
        Boolean returning = this.taskReturning.get(taskID);
        return returning == null ? false : returning;
    }

    public static boolean isGolemEnRoute(EntityGolemBase golem) {
        return GolemTaskData.get(golem).getTaskID() != 0;
    }

    @Override
    public void invalidate() {
        if (this.worldObj != null && !this.worldObj.isRemote) {
            int dimension = this.worldObj.provider.dimensionId;
            if (coreLocations.containsKey(dimension)) {
                HashSet<BlockCoord> bcs = coreLocations.get(dimension);
                bcs.remove(new BlockCoord(this));
            }
        }

        super.invalidate();
    }

    public static TileEntityInventarium getCoreNear(World world, int x, int y, int z) {
        int dimension = world.provider.dimensionId;
        if (!coreLocations.containsKey(dimension)) {
            return null;
        } else {
            HashSet<BlockCoord> bcs = coreLocations.get(dimension);
            TileEntityInventarium closest = null;
            double closestDistance = Double.MAX_VALUE;
            long currentWorldTime = world.getTotalWorldTime();
            long mostPreferredLinkTime = 0L;

            for (BlockCoord bc : bcs) {
                if (TjUtil.isChunkLoaded(world, bc.x, bc.z)) {
                    TileEntity te = world.getTileEntity(bc.x, bc.y, bc.z);
                    if (te instanceof TileEntityInventarium) {
                        TileEntityInventarium teI = (TileEntityInventarium)te;
                        double d = teI.getDistanceIfInRange(x, y, z);
                        if (teI.timeMarkedAsPreferredLink != 0L && teI.timeMarkedAsPreferredLink < currentWorldTime - MAX_PREFERRED_TIME) {
                            teI.timeMarkedAsPreferredLink = 0L;
                        }

                        if (d != -1.0
                            && (
                                mostPreferredLinkTime < teI.timeMarkedAsPreferredLink
                                    || mostPreferredLinkTime == teI.timeMarkedAsPreferredLink && d < closestDistance
                            )) {
                            closest = teI;
                            closestDistance = d;
                            mostPreferredLinkTime = teI.timeMarkedAsPreferredLink;
                        }
                    }
                }
            }

            return closest;
        }
    }

    public double getDistanceIfInRange(int x, int y, int z) {
        double d = TjUtil.getDistanceBetweenPoints_squared(this.xCoord, this.yCoord, this.zCoord, x, y, z);
        return d <= this.maxDistance * this.maxDistance ? d : -1.0;
    }

    public boolean addNode(int x, int y, int z) {
        if (this.nodes.size() >= this.maxInvNodes) {
            return false;
        } else {
            this.nodes.add(new BlockCoord(x, y, z));
            this.markDirty(false);
            if (this.refreshCountdown == 0 || this.refreshCountdown > 5) {
                this.refreshCountdown = 5;
            }

            return true;
        }
    }

    public boolean addMemoryUser(int x, int y, int z) {
        this.checkMaxes();
        this.verifyMemoryUsers();
        if (this.memoryUsers.size() >= this.maxMemoryUsers) {
            return false;
        } else {
            this.memoryUsers.add(new BlockCoord(x, y, z));
            this.markDirty(false);
            return true;
        }
    }

    public boolean addMultiDestMirror(int x, int y, int z) {
        if (this.addMemoryUser(x, y, z)) {
            this.hasMultiDestMirror = true;
            return true;
        } else {
            return false;
        }
    }

    public void breakAllLinks() {
        for (BlockCoord bc : this.nodes) {
            TileEntity te = this.worldObj.getTileEntity(bc.x, bc.y, bc.z);
            if (te instanceof TileEntityUnseenScribe) {
                ((TileEntityUnseenScribe)te).breakLink();
            }
        }

        this.nodes.clear();

        for (BlockCoord bcx : this.memoryUsers) {
            TileEntity te = this.worldObj.getTileEntity(bcx.x, bcx.y, bcx.z);
            if (te instanceof IInventariumMemoryUser) {
                ((IInventariumMemoryUser)te).breakLink();
            }
        }

        this.memoryUsers.clear();
        this.hasMultiDestMirror = false;

        for (int x = -3; x < 4; x++) {
            for (int y = -3; y <= RELATIVEMAX_Y_FOR_MEMORYBLOCK; y++) {
                for (int z = -3; z < 4; z++) {
                    Block block = this.worldObj.getBlock(this.xCoord + x, this.yCoord + y, this.zCoord + z);
                    if (block == ModBlocks.metalGlow && this.worldObj.getBlockMetadata(this.xCoord + x, this.yCoord + y, this.zCoord + z) == 1) {
                        this.worldObj.setBlockMetadataWithNotify(this.xCoord + x, this.yCoord + y, this.zCoord + z, 0, 3);
                    }
                }
            }
        }
    }

    public int getNumScribes() {
        return this.nodes.size();
    }

    public int getNumMemoryUsers() {
        return this.memoryUsers.size();
    }

    public void markAsPreferredLink() {
        this.timeMarkedAsPreferredLink = this.worldObj.getTotalWorldTime();
    }

    public void addRequestForDestination(WorldSpecificCoordinates coord, ItemStack stack) {
        if (coord != null) {
            this.requestedItemsDestinations.add(coord, stack);
            this.markDirty(false);
        }
    }

    public void subtractRequestForDestination(WorldSpecificCoordinates coord, ItemStack stack, boolean checkOtherDest) {
        if (coord != null) {
            int numRemoved = this.requestedItemsDestinations.subtract(coord, stack);
            if (checkOtherDest && numRemoved < stack.stackSize) {
                ItemStack copy = stack.copy();
                copy.stackSize = stack.stackSize - numRemoved;
                this.requestedItemsDestinations.subtractFromAnyGroup(copy);
            }

            this.markDirty(false);
        } else {
            this.requestedItemsDestinations.subtractFromAnyGroup(stack);
            this.markDirty(false);
        }
    }

    public int deliverToAllRequestedDestinations(ItemStack stack) {
        int leftover = stack.stackSize;
        ItemStack remaining = stack.copy();

        do {
            int last = leftover;
            remaining.stackSize = leftover;
            leftover = this.deliverToRequestedDestination(remaining);
            if (leftover == last) {
                return leftover;
            }
        } while (leftover > 0);

        return 0;
    }

    public int deliverToRequestedDestination(ItemStack stack) {
        int leftover = stack.stackSize;
        if (leftover < 1) {
            return 0;
        } else if (!this.isTrackingDestinations()) {
            return leftover;
        } else {
            HashableItemWithoutSize item = new HashableItemWithoutSize(stack);
            Map<WorldSpecificCoordinates, Integer> groups = this.requestedItemsDestinations.getGroupsForItem(item);
            if (groups != null && groups.size() > 0) {
                Entry<WorldSpecificCoordinates, Integer> entry = groups.entrySet().iterator().next();
                WorldSpecificCoordinates dest = entry.getKey();
                IMirrorVariant mirror = this.getOutputForReqTome(dest);
                if (mirror != null && mirror.isLinked()) {
                    int wanted = entry.getValue();
                    int toSend = Math.min(wanted, stack.stackSize);
                    if (toSend > 0) {
                        ItemStack copy = stack.copy();
                        copy.stackSize = toSend;
                        leftover = mirror.addStackToOutput(copy);
                        int sent = toSend - leftover;
                        leftover = stack.stackSize - sent;
                        mirror.markMirrorDirty();
                        this.requestedItemsDestinations.subtract(dest, item, sent);
                        this.markDirty(false);
                    }
                } else {
                    this.requestedItemsDestinations.removeGroup(dest);
                }
            }

            return leftover;
        }
    }

    public boolean isTrackingDestinations() {
        if (this.hasMultiDestMirror == null) {
            this.hasMultiDestMirror = false;

            for (BlockCoord bc : this.memoryUsers) {
                TileEntity te = this.worldObj.getTileEntity(bc.x, bc.y, bc.z);
                if (te instanceof TileEntityMirrorMultiDest) {
                    this.hasMultiDestMirror = true;
                    break;
                }
            }
        }

        return this.hasMultiDestMirror;
    }

    private IMirrorVariant getOutputForReqTome(WorldSpecificCoordinates coord) {
        if (coord != null) {
            World w = DimensionManager.getWorld(coord.dim);
            if (w == null) {
                return null;
            }

            TileEntity te = w.getTileEntity(coord.x, coord.y, coord.z);
            if (te instanceof TileEntityRequisitionTome) {
                return ((TileEntityRequisitionTome)te).getOutputMirror();
            }
        }

        return null;
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setInteger("maxInvNodes", this.maxInvNodes);
        nbttagcompound.setInteger("maxDistance", this.maxDistance);
        nbttagcompound.setInteger("maxSlotsRead", this.maxSlotsRead);
        NBTTagList nbttaglistNodes = new NBTTagList();

        for (BlockCoord pos : this.nodes) {
            NBTTagCompound tagList = new NBTTagCompound();
            tagList.setIntArray("pos", new int[]{pos.x, pos.y, pos.z});
            nbttaglistNodes.appendTag(tagList);
        }

        nbttagcompound.setTag("Nodes", nbttaglistNodes);
        NBTTagList nbttaglistTM = new NBTTagList();

        for (BlockCoord pos : this.memoryUsers) {
            NBTTagCompound tagList = new NBTTagCompound();
            tagList.setIntArray("pos", new int[]{pos.x, pos.y, pos.z});
            nbttaglistTM.appendTag(tagList);
        }

        nbttagcompound.setTag("MemoryUsers", nbttaglistTM);
        nbttagcompound.setInteger("maxMemoryUsers", this.maxMemoryUsers);
        this.requestedItems.writeToNBT(nbttagcompound);
        this.requestedItemsEnRoute.writeToNBT(nbttagcompound);
        NBTTagList stacksTagList = new NBTTagList();

        for (Entry<Integer, ItemStack> entry : this.stacksEnRouteByTaskID.entrySet()) {
            NBTTagCompound nbtcomp = new NBTTagCompound();
            int id = entry.getKey();
            ItemStack stack = entry.getValue();
            boolean returning = this.isTaskInPhaseTwo(id);
            nbtcomp.setInteger("TaskID", id);
            stack.writeToNBT(nbtcomp);
            nbtcomp.setBoolean("TaskReturn", returning);
            stacksTagList.appendTag(nbtcomp);
        }

        nbttagcompound.setTag("StackTasks", stacksTagList);
        nbttagcompound.setInteger("lastTaskID", this.lastTaskID);
        this.requestedItemsDestinations.writeToNBT(nbttagcompound);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.maxInvNodes = nbttagcompound.getInteger("maxInvNodes");
        this.maxDistance = nbttagcompound.getInteger("maxDistance");
        this.maxSlotsRead = nbttagcompound.getInteger("maxSlotsRead");
        this.nodes.clear();
        int errors = 0;
        NBTTagList nodesList = nbttagcompound.getTagList("Nodes", 10);
        int count = nodesList.tagCount();

        for (int i = 0; i < count; i++) {
            NBTTagCompound compound = nodesList.getCompoundTagAt(i);
            int[] arr = compound.getIntArray("pos");
            if (arr.length != 3) {
                errors++;
            } else {
                this.nodes.add(new BlockCoord(arr));
            }
        }

        if (errors > 0) {
            FMLLog.warning("[Automagy] Inventarium: Malformed scribe data detected. Failed to load " + errors + " positions.");
        }

        this.memoryUsers.clear();
        this.hasMultiDestMirror = null;
        errors = 0;
        NBTTagList memoryUsersList = nbttagcompound.getTagList("MemoryUsers", 10);
        count = memoryUsersList.tagCount();

        for (int ix = 0; ix < count; ix++) {
            NBTTagCompound compound = memoryUsersList.getCompoundTagAt(ix);
            int[] arr = compound.getIntArray("pos");
            if (arr.length != 3) {
                errors++;
            } else {
                this.memoryUsers.add(new BlockCoord(arr));
            }
        }

        if (errors > 0) {
            FMLLog.warning("[Automagy] Inventarium: Malformed MemoryUser data detected. Failed to load " + errors + " positions.");
        }

        this.maxMemoryUsers = nbttagcompound.getInteger("maxMemoryUsers");
        this.requestedItems.readFromNBT(nbttagcompound);
        if (this.requestedItems.size() > 0) {
            this.requestedItemsEnRoute.readFromNBT(nbttagcompound);
            NBTTagList stacksTagList = nbttagcompound.getTagList("StackTasks", 10);
            this.stacksEnRouteByTaskID.clear();
            this.taskReturning.clear();

            for (int ixx = 0; ixx < stacksTagList.tagCount(); ixx++) {
                NBTTagCompound nbtcomp = stacksTagList.getCompoundTagAt(ixx);
                int id = nbtcomp.getInteger("TaskID");
                if (id != 0) {
                    ItemStack stack = TjUtil.readLargeItemStackFromNBT(nbtcomp);
                    if (stack != null && stack.stackSize > 0) {
                        this.stacksEnRouteByTaskID.put(id, stack);
                        boolean returning = nbtcomp.getBoolean("TaskReturn");
                        if (returning) {
                            this.taskReturning.put(id, true);
                        }
                    }
                }
            }

            this.lastTaskID = nbttagcompound.getInteger("lastTaskID");
        } else {
            this.requestedItemsEnRoute.clear();
            this.stacksEnRouteByTaskID.clear();
            this.taskReturning.clear();
            this.lastTaskID = 0;
        }

        this.requestedItemsDestinations.readFromNBT(nbttagcompound);
    }

    public static IInventariumContentsProvider getContentsProvider(TileEntity te) {
        for (List<IInventariumContentsProvider> list : AutomagyAPI.invContentsProviders.values()) {
            for (IInventariumContentsProvider provider : list) {
                if (provider.canHandleTile(te)) {
                    return provider;
                }
            }
        }

        return null;
    }

    public static class PairedContentsProvider {
        public IInventariumContentsProvider provider;
        public TileEntity te;

        public PairedContentsProvider(IInventariumContentsProvider provider, TileEntity te) {
            this.provider = provider;
            this.te = te;
        }
    }
}
