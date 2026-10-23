package tuhljin.automagy.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.common.Thaumcraft;
import tuhljin.automagy.api.AutomagyAPI;
import tuhljin.automagy.api.inventarium.IInventariumContentsProvider;
import tuhljin.automagy.codechicken.lib.math.MathHelper;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.gui.GUIScribe;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.FilteringItemList;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.lib.inventory.MappedItemsOrderedByTimeAdded;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.network.MessageGUIInventariumBookPref;

public class TileEntityUnseenScribe extends ModTileEntity implements IReceivesItemListPacket, IInventariumBook {
    public static int RANGE_DEFAULT = 3;
    public int linkX;
    public int linkY = -1;
    public int linkZ;
    public static double QUILL_IDLE_MAXY = 0.1;
    public static double QUILL_IDLE_MINY = 0.05;
    public static double QUILL_POWERED_MAXY = 0.2;
    public static double QUILL_POWERED_MINY = 0.15;
    public static double QUILL_POWERED_HORIZ = -0.5;
    public static double BOOK_MAXY = 0.01;
    public double quillY;
    public double quillHoriz;
    public double quillR;
    private boolean quillGoingUp = false;
    public boolean someSlotsOmitted = false;
    public double bookY = BOOK_MAXY / 2.0;
    private boolean bookGoingUp = true;
    public float page;
    protected int poweredTime = 0;
    private FilteringItemList itemList = null;
    public HashSet<String> localViewUsers = new HashSet<>();
    public HashSet<String> sortByNameUsers = new HashSet<>();

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (this.worldObj != null) {
            if (!this.worldObj.isRemote) {
                this.itemList = null;
                int prevPower = this.poweredTime;
                if (this.poweredTime > 0) {
                    this.poweredTime--;
                }

                if (this.poweredTime < 1) {
                    int amt = this.linkY != -1 ? VisNetHandler.drainVis(this.worldObj, this.xCoord, this.yCoord + 1, this.zCoord, Aspect.ORDER, 10) : 0;
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
                if (this.poweredTime < 1) {
                    if (this.quillHoriz < 0.0) {
                        this.quillHoriz = MathHelper.approachLinear(this.quillHoriz, 0.0, 0.01);
                        if (this.quillY >= QUILL_IDLE_MAXY) {
                            this.quillY = MathHelper.approachExp(this.quillY, QUILL_IDLE_MAXY, 0.1);
                        }
                    } else if (this.quillGoingUp) {
                        if (this.quillY >= QUILL_IDLE_MAXY) {
                            this.quillGoingUp = false;
                        } else {
                            this.quillY = MathHelper.retreatExp(this.quillY, QUILL_IDLE_MAXY, QUILL_IDLE_MINY, 0.01, 0.025);
                        }
                    } else if (this.quillY <= QUILL_IDLE_MINY) {
                        if (this.poweredTime > 0) {
                            this.quillGoingUp = true;
                        } else {
                            this.quillY = MathHelper.retreatExp(this.quillY, 0.0, QUILL_IDLE_MINY, 0.02, 0.025);
                        }
                    } else {
                        this.quillY = MathHelper.retreatExp(this.quillY, QUILL_IDLE_MINY, QUILL_IDLE_MAXY, 0.01, 0.025);
                    }

                    if (this.quillR < 0.0) {
                        this.quillR = MathHelper.approachLinear(this.quillR, 0.0, 1.0);
                    }
                } else if (this.quillHoriz > QUILL_POWERED_HORIZ) {
                    if (this.quillY < QUILL_POWERED_MINY) {
                        this.quillGoingUp = true;
                        this.quillY = MathHelper.approachLinear(this.quillY, QUILL_POWERED_MINY, 0.01);
                    }

                    this.quillHoriz = MathHelper.approachLinear(this.quillHoriz, QUILL_POWERED_HORIZ, 0.01);
                    if (this.quillY >= QUILL_IDLE_MINY * 0.5 && this.quillR > -20.0) {
                        this.quillR = MathHelper.approachLinear(this.quillR, -20.0, 0.3);
                    }
                } else if (this.quillGoingUp) {
                    if (this.quillY >= QUILL_POWERED_MAXY) {
                        this.quillGoingUp = false;
                    } else {
                        this.quillY = MathHelper.retreatExp(this.quillY, QUILL_POWERED_MAXY, QUILL_POWERED_MINY, 0.01, 0.025);
                    }
                } else if (this.quillY <= QUILL_POWERED_MINY) {
                    if (this.poweredTime > 0) {
                        this.quillGoingUp = true;
                    } else {
                        this.quillY = MathHelper.retreatExp(this.quillY, 0.0, QUILL_POWERED_MINY, 0.02, 0.025);
                    }
                } else {
                    this.quillY = MathHelper.retreatExp(this.quillY, QUILL_POWERED_MINY, QUILL_POWERED_MAXY, 0.01, 0.025);
                }

                if (this.bookGoingUp) {
                    if (this.bookY >= BOOK_MAXY) {
                        this.bookGoingUp = false;
                    } else {
                        this.bookY = MathHelper.retreatExp(this.bookY, BOOK_MAXY, 0.0, 0.005, 0.025);
                    }
                } else if (this.bookY <= 0.0) {
                    this.bookGoingUp = true;
                } else {
                    this.bookY = MathHelper.retreatExp(this.bookY, 0.0, BOOK_MAXY, 0.005, 0.025);
                }

                if (this.page > 0.0F) {
                    this.page += 0.1F;
                    if (this.page > 1.0F) {
                        if (this.poweredTime > 0 && this.worldObj.rand.nextInt(4) == 0) {
                            this.page = 0.1F;
                        } else {
                            this.page = 0.0F;
                        }
                    }
                } else if (this.poweredTime >= 1) {
                    if (this.worldObj.rand.nextInt(100) == 0) {
                        this.page = 0.1F;
                        if (this.worldObj.rand.nextInt(8) == 0) {
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
                    } else if (this.worldObj.rand.nextInt(600) == 0) {
                        this.worldObj
                            .playSound(
                                this.xCoord + 0.5,
                                this.yCoord + 1.0,
                                this.zCoord + 0.5,
                                "thaumcraft:write",
                                0.1F,
                                this.worldObj.rand.nextFloat() * 0.1F + 0.9F,
                                false
                            );
                    }
                }

                if (this.someSlotsOmitted && this.poweredTime > 0 && this.worldObj.rand.nextInt(4) == 0) {
                    int color = 5;
                    Thaumcraft.proxy
                        .sparkle(
                            this.xCoord + 0.2F + this.worldObj.rand.nextFloat() * 0.6F,
                            this.yCoord + 1.2F,
                            this.zCoord + 0.2F + this.worldObj.rand.nextFloat() * 0.6F,
                            1.0F,
                            color,
                            -0.1F
                        );
                }
            }
        }
    }

    public TileEntityUnseenScribe.SelectedContainers getSelectedContainers() {
        TileEntity teAbove = this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord);
        if (teAbove instanceof TileEntityScribePointer) {
            return ((TileEntityScribePointer)teAbove).getSelectedContainers();
        } else {
            ForgeDirection dir;
            switch (this.getBlockMetadata()) {
                case 0:
                    dir = ForgeDirection.NORTH;
                    break;
                case 1:
                    dir = ForgeDirection.EAST;
                    break;
                case 2:
                    dir = ForgeDirection.SOUTH;
                    break;
                case 3:
                    dir = ForgeDirection.WEST;
                    break;
                default:
                    return null;
            }

            int x = this.xCoord;
            int y = this.yCoord;
            int z = this.zCoord;
            int d = this.getRange();
            TileEntityUnseenScribe.SelectedContainers sc = new TileEntityUnseenScribe.SelectedContainers();

            do {
                x += dir.offsetX;
                y += dir.offsetY;
                z += dir.offsetZ;
                if (sc.add(this.worldObj, x, y, z)) {
                    return sc;
                }
            } while (--d > 0);

            return null;
        }
    }

    public ArrayList<BlockCoord> getInventoryCoords() {
        if (this.poweredTime < 1) {
            return null;
        } else {
            TileEntityUnseenScribe.SelectedContainers sc = this.getSelectedContainers();
            if (sc != null && sc.size() >= 1) {
                ArrayList<BlockCoord> bcs = new ArrayList<>();
                int range = this.getRange();

                for (int ox = -range; ox <= range; ox++) {
                    for (int oy = -range; oy <= range; oy++) {
                        for (int oz = -range; oz <= range; oz++) {
                            int x = this.xCoord + ox;
                            int y = this.yCoord + oy;
                            int z = this.zCoord + oz;
                            if (sc.containsBlockAt(this.worldObj, x, y, z)) {
                                bcs.add(new BlockCoord(x, y, z));
                            }
                        }
                    }
                }

                return bcs.size() > 0 ? bcs : null;
            } else {
                return null;
            }
        }
    }

    public void breakLink() {
        this.linkY = -1;
        this.quillGoingUp = false;
        this.flagSlotsOmitted(false);
    }

    public int getRange() {
        TileEntity teAbove = this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord);
        return teAbove instanceof TileEntityScribePointer ? RANGE_DEFAULT + ((TileEntityScribePointer)teAbove).getRangeBoosters() : RANGE_DEFAULT;
    }

    public FilteringItemList getLocalItemList() {
        if (this.itemList == null) {
            TileEntityInventarium.getContentsIdentifier++;
            if (this.worldObj.isRemote) {
                return new FilteringItemList();
            }

            ArrayList<TileEntityInventarium.PairedContentsProvider> invs = new ArrayList<>();
            ArrayList<BlockCoord> bcs = this.getInventoryCoords();
            if (bcs != null) {
                for (BlockCoord bc : bcs) {
                    TileEntity te2 = this.worldObj.getTileEntity(bc.x, bc.y, bc.z);
                    IInventariumContentsProvider provider = TileEntityInventarium.getContentsProvider(te2);
                    if (provider != null && provider.getSlotCount(te2, TileEntityInventarium.getContentsIdentifier) > 0) {
                        invs.add(new TileEntityInventarium.PairedContentsProvider(provider, te2));
                    }
                }

                this.itemList = new FilteringItemList().populateFromInventariumContentsProviders(invs, TileEntityInventarium.getContentsIdentifier);
            }
        }

        return this.itemList;
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
    public WorldSpecificCoordinates getLinkCoord() {
        return new WorldSpecificCoordinates(this.worldObj, this.linkX, this.linkY, this.linkZ);
    }

    @Override
    public WorldSpecificCoordinates getRequesterCoord() {
        return null;
    }

    @Override
    public boolean hasLocalItems() {
        return true;
    }

    @Override
    public boolean canRequestItems() {
        return false;
    }

    @Override
    public void receiveItemListPacket(ArrayList<ItemStack> list, boolean partial) {
        if (partial && this.itemList != null) {
            for (ItemStack stack : list) {
                this.itemList.set(new HashableItemWithoutSize(stack), stack.stackSize > 0 ? stack.stackSize : 0);
            }

            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            if (gui instanceof GUIScribe) {
                ((GUIScribe)gui).setStacks(this.getLocalItemList(), true);
            }
        } else {
            this.itemList = new FilteringItemList(list, false);
        }
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

    public void flagSlotsOmitted(boolean b) {
        this.someSlotsOmitted = b;
        this.markDirty();
    }

    @Override
    public void setPlayerLocalViewPreference(EntityPlayer player, boolean local) {
        if (this.worldObj.isRemote) {
            MessageGUIInventariumBookPref.sendToServer(0, local);
        } else {
            String name = player.getCommandSenderName();
            if (local) {
                if (name != "Rcon") {
                    this.localViewUsers.add(name);
                }
            } else if (this.localViewUsers.contains(name)) {
                this.localViewUsers.remove(name);
            }

            this.markDirty();
        }
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
        return this.localViewUsers.contains(player.getCommandSenderName());
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
        nbttagcompound.setBoolean("someSlotsOmitted", this.someSlotsOmitted);
        NBTTagList nbttaglist = new NBTTagList();

        for (String name : this.localViewUsers) {
            if (name != null && !name.isEmpty()) {
                NBTTagCompound tagList = new NBTTagCompound();
                tagList.setString("User", name);
                nbttaglist.appendTag(tagList);
            }
        }

        nbttagcompound.setTag("localViewUsers", nbttaglist);
        nbttaglist = new NBTTagList();

        for (String namex : this.sortByNameUsers) {
            if (namex != null && !namex.isEmpty()) {
                NBTTagCompound tagList = new NBTTagCompound();
                tagList.setString("User", namex);
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
        this.someSlotsOmitted = nbttagcompound.getBoolean("someSlotsOmitted");
        NBTTagList nbttaglist = nbttagcompound.getTagList("localViewUsers", 10);
        this.localViewUsers.clear();
        int size = nbttaglist.tagCount();

        for (int i = 0; i < size; i++) {
            NBTTagCompound tagList = nbttaglist.getCompoundTagAt(i);
            String name = tagList.getString("User");
            if (name != null && !name.isEmpty()) {
                this.localViewUsers.add(name);
            }
        }

        nbttaglist = nbttagcompound.getTagList("sortByNameUsers", 10);
        this.sortByNameUsers.clear();
        size = nbttaglist.tagCount();

        for (int ix = 0; ix < size; ix++) {
            NBTTagCompound tagList = nbttaglist.getCompoundTagAt(ix);
            String name = tagList.getString("User");
            if (name != null && !name.isEmpty()) {
                this.sortByNameUsers.add(name);
            }
        }
    }

    public static class SelectedContainers {
        private List<Block> blocks = new ArrayList<>();
        private List<Integer> mds = new ArrayList<>();
        private List<BlockCoord> coords = new ArrayList<>();
        private Map<Integer, String> names = new HashMap<>();

        private int getIndex(Block block, int metadata) {
            for (int i = 0; i < this.blocks.size(); i++) {
                if (this.blocks.get(i) == block && this.mds.get(i) == metadata) {
                    return i;
                }
            }

            return -1;
        }

        public boolean contains(Block block, int metadata) {
            return this.getIndex(block, metadata) != -1;
        }

        public boolean containsBlockAt(World world, int x, int y, int z) {
            Block block = world.getBlock(x, y, z);
            int md = -1;

            for (int i = 0; i < this.blocks.size(); i++) {
                if (this.blocks.get(i) == block) {
                    if (md == -1) {
                        md = world.getBlockMetadata(x, y, z);
                    }

                    int mdStored = this.mds.get(i);
                    if (mdStored == md) {
                        return true;
                    }

                    String name = TjUtil.getBlockUnlocalizedName(block, world, x, y, z);
                    String nameStored = this.getName(block, mdStored, world);
                    if (name.equals(nameStored)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean add(World world, int x, int y, int z) {
            Block block = world.getBlock(x, y, z);
            if (block != null && !AutomagyAPI.invContainerBlacklist.contains(block)) {
                TileEntity te = world.getTileEntity(x, y, z);
                if (TileEntityInventarium.getContentsProvider(te) != null) {
                    int md = world.getBlockMetadata(x, y, z);
                    if (!this.contains(block, md)) {
                        this.blocks.add(block);
                        this.mds.add(md);
                        this.coords.add(new BlockCoord(x, y, z));
                        return true;
                    }
                }
            }

            return false;
        }

        public String getName(Block block, int metadata, World world) {
            int i = this.getIndex(block, metadata);
            if (i == -1) {
                return null;
            } else {
                String name = this.names.get(i);
                if (name == null) {
                    BlockCoord pos = this.coords.get(i);
                    name = TjUtil.getBlockUnlocalizedName(block, world, pos.x, pos.y, pos.z);
                    this.names.put(i, name);
                }

                return name;
            }
        }

        public int size() {
            return this.blocks.size();
        }

        public BlockCoord getBlockCoord(int i) {
            return this.coords.get(i);
        }
    }
}
