package tuhljin.automagy.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.ItemApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockCrystal;
import thaumcraft.common.blocks.BlockWoodenDevice;
import thaumcraft.common.entities.EntityAspectOrb;
import thaumcraft.common.tiles.TileEssentiaReservoir;
import thaumcraft.common.tiles.TileJarFillable;
import tuhljin.automagy.api.essentia.IAspectContainerWithMax;
import tuhljin.automagy.api.essentia.IEssentiaLocusReadable;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.items.ItemCrystalEye;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.events.AutomagyRenderEventHandler;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.network.MessageParticles;

public class TileEntityEssentiaLocus extends ModTileEntity implements IAspectContainerWithMax, IAspectContainerLarge, IContainsEyes, IAspectSource {
    public static int MAXJARS_START = 12;
    public static int MAXJARS_INC_CLUSTER = 8;
    public static int MAXDISTANCE_START = 20;
    public static int MAXDISTANCE_INC_CLUSTER = 8;
    public int maxJars;
    public int maxEyes = 1;
    public int maxDistance;
    public InventoryObjectEyes eyesContainingInventory;
    protected AspectList aspects = new AspectList();
    protected AspectList maxAspectSpaceByType = new AspectList();
    private int ticks;
    public static int JARS_UPDATE_FREQUENCY_CAP = 200;
    private int jarsUpdateFrequency = 40;
    private int ticksSinceJarsUpdate = this.jarsUpdateFrequency;
    public static int VALIDITY_CHECK_FREQUENCY = 60;
    private int ticksSinceValidityCheck;
    public int ticksToAnim = 0;
    private Object beam = null;
    protected HashSet<BlockCoord> jars = new HashSet<>();
    protected int eyeCount = 0;
    public boolean hasAggregator = false;
    protected HashMap<Aspect, BlockCoord> lastJarWithAspect = new HashMap<>();
    public static int rotationSpeedFactor = 3240;
    public static double clientRenderFloatingTop = 0.1;
    public static double clientRenderFloatingBtm = -0.3;
    public int clientRenderRotationHelper = 0;
    public double clientRenderFloatingDistance = clientRenderFloatingTop;
    public boolean clientRenderGoingUp = false;

    public TileEntityEssentiaLocus() {
        this.eyesContainingInventory = new InventoryObjectEyes(this, "Essentia Locus", 40, true);
    }

    public int getEyeCount() {
        return this.eyeCount;
    }

    public ItemStack removeEye() {
        if (this.eyeCount > 0) {
            int slot = this.eyeCount - 1;
            ItemStack stack = this.eyesContainingInventory.getStackInSlot(slot);
            this.eyesContainingInventory.setInventorySlotContents(slot, null);
            this.eyeCount--;
            this.markDirty();
            this.ticksSinceJarsUpdate = this.jarsUpdateFrequency;
            return stack;
        } else {
            return null;
        }
    }

    public boolean insertEye(ItemStack stack) {
        if (this.eyeCount < this.maxEyes) {
            this.eyesContainingInventory.setInventorySlotContents(this.eyeCount, stack.splitStack(1));
            this.eyeCount++;
            this.markDirty();
            this.ticksSinceJarsUpdate = this.jarsUpdateFrequency;
            return true;
        } else {
            return false;
        }
    }

    public boolean itemIsValidEye(ItemStack stack) {
        return this.eyesContainingInventory.isItemValidForSlot(this.eyeCount, stack);
    }

    private int removeInvalidEyes() {
        ArrayList<ItemStack> list = this.eyesContainingInventory.removeInvalidEyes();
        int count = list.size();
        if (count > 0) {
            this.eyeCount -= count;
            this.eyesContainingInventory.organizeSlots();
            this.markDirty();
            if (!this.worldObj.isRemote) {
                TjUtil.dropItemsIntoWorld(list, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            }

            this.ticksSinceJarsUpdate = this.jarsUpdateFrequency;
        }

        return count;
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote) {
            if (this.ticksSinceValidityCheck >= VALIDITY_CHECK_FREQUENCY) {
                this.ticksSinceValidityCheck = 0;
                if (!checkValidity(this.worldObj, this.xCoord, this.yCoord, this.zCoord, false)) {
                    this.getBlockType().dropBlockAsItem(this.worldObj, this.xCoord, this.yCoord, this.zCoord, 0, 0);
                    this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
                    return;
                }

                this.checkMaxes();
            } else {
                this.ticksSinceValidityCheck++;
            }

            if (this.ticksSinceJarsUpdate >= this.jarsUpdateFrequency) {
                this.ticksSinceJarsUpdate = 0;
                if (!this.findJars()) {
                    this.jarsUpdateFrequency = Math.min(this.jarsUpdateFrequency + 20, JARS_UPDATE_FREQUENCY_CAP);
                } else {
                    this.jarsUpdateFrequency = 20;
                }
            } else if (this.eyeCount > 0) {
                this.ticksSinceJarsUpdate++;
            }

            if (this.worldObj.rand.nextInt(1000) == 0) {
                EntityAspectOrb orb = new EntityAspectOrb(this.worldObj, this.xCoord + 0.5, this.yCoord - 0.5, this.zCoord + 0.5, Aspect.WATER, 1);
                this.worldObj.spawnEntityInWorld(orb);
            }

            this.ticks++;
            if (this.ticks % 2 == 0) {
                AspectList aspectsNew = new AspectList();
                this.maxAspectSpaceByType.aspects.clear();

                for (BlockCoord bc : this.jars) {
                    TileEntity te = this.worldObj.getTileEntity(bc.x, bc.y, bc.z);
                    if (te instanceof TileJarFillable) {
                        TileJarFillable jar = (TileJarFillable)te;
                        Aspect jarContains = jar.aspect;
                        if (jarContains != null) {
                            if (jar.amount > 0) {
                                aspectsNew.add(jarContains, jar.amount);
                            }

                            this.maxAspectSpaceByType.add(jarContains, jar.maxAmount);
                        } else if (jar.aspectFilter != null) {
                            this.maxAspectSpaceByType.add(jar.aspectFilter, jar.maxAmount);
                        }
                    } else if (ThaumcraftExtension.objectIsEssentiaReservoir(te)) {
                        TileEssentiaReservoir res = (TileEssentiaReservoir)te;
                        AspectList list = res.getAspects();
                        if (list != null) {
                            for (Aspect a : list.getAspects()) {
                                if (a != null) {
                                    int amt = list.getAmount(a);
                                    if (amt > 0) {
                                        aspectsNew.add(a, amt);
                                    }
                                }
                            }
                        }
                    } else if (!(te instanceof IEssentiaLocusReadable)) {
                        if (TjUtil.isChunkLoaded(this.worldObj, bc.x, bc.z)) {
                            this.ticksSinceJarsUpdate = this.jarsUpdateFrequency;
                        }
                    } else {
                        IEssentiaLocusReadable readable = (IEssentiaLocusReadable)te;
                        AspectList list = readable.getAspects();
                        if (list != null) {
                            for (Aspect ax : list.getAspects()) {
                                if (ax != null) {
                                    int amt = list.getAmount(ax);
                                    if (amt > 0) {
                                        aspectsNew.add(ax, amt);
                                    }
                                }
                            }
                        }

                        if (readable instanceof IAspectContainerWithMax) {
                            AspectList listMax = ((IAspectContainerWithMax)readable).getAspectsBase();
                            if (listMax != null) {
                                for (Aspect axx : listMax.getAspects()) {
                                    if (axx != null) {
                                        int amt = list.getAmount(axx);
                                        if (amt > 0) {
                                            this.maxAspectSpaceByType.add(axx, amt);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!ThaumcraftExtension.areAspectListsIdentical(this.aspects, aspectsNew)) {
                    this.aspects = aspectsNew;
                    this.markDirty();
                }
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
                        13260,
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

    protected boolean findJars() {
        boolean differenceFound = false;
        HashSet<BlockCoord> newJars = new HashSet<>();

        for (int slot = 0; slot < this.eyeCount; slot++) {
            ItemStack stack = this.eyesContainingInventory.getStackInSlot(slot);
            if (stack != null) {
                WorldSpecificCoordinates coord = ((ItemCrystalEye)stack.getItem()).getLinkLocation(stack);
                TileEntity te = this.worldObj.getTileEntity(coord.x, coord.y, coord.z);
                if (tileIsValidEssentiaContainer(te)) {
                    BlockCoord bc = new BlockCoord(te);
                    if (!newJars.contains(bc)) {
                        if (!this.jars.contains(bc)) {
                            differenceFound = true;
                        }

                        newJars.add(bc);
                        if (newJars.size() >= this.maxJars) {
                            break;
                        }

                        if (this.findJarsHelper(newJars, coord.x, coord.y, coord.z)) {
                            differenceFound = true;
                        }

                        if (newJars.size() >= this.maxJars) {
                            break;
                        }
                    }
                }
            }
        }

        if (!differenceFound && this.jars.size() != newJars.size()) {
            differenceFound = true;
        }

        this.jars = newJars;
        return differenceFound;
    }

    private boolean findJarsHelper(HashSet<BlockCoord> newJars, int x, int y, int z) {
        boolean newJarFound = false;

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int x2 = x + dir.offsetX;
            int y2 = y + dir.offsetY;
            int z2 = z + dir.offsetZ;
            TileEntity te = this.worldObj.getTileEntity(x2, y2, z2);
            if (tileIsValidEssentiaContainer(te)) {
                BlockCoord bc = new BlockCoord(te);
                if (!newJars.contains(bc)) {
                    if (!this.jars.contains(bc)) {
                        newJarFound = true;
                    }

                    newJars.add(bc);
                    if (newJars.size() >= this.maxJars) {
                        break;
                    }

                    if (this.findJarsHelper(newJars, x2, y2, z2)) {
                        newJarFound = true;
                    }

                    if (newJars.size() >= this.maxJars) {
                        break;
                    }
                }
            }
        }

        return newJarFound;
    }

    public void checkMaxes() {
        int clusters = 0;
        clusters += this.numWaterClustersNear(this.xCoord - 2, this.yCoord - 2, this.zCoord - 1);
        clusters += this.numWaterClustersNear(this.xCoord - 2, this.yCoord - 2, this.zCoord);
        clusters += this.numWaterClustersNear(this.xCoord - 2, this.yCoord - 2, this.zCoord + 1);
        clusters += this.numWaterClustersNear(this.xCoord + 2, this.yCoord - 2, this.zCoord - 1);
        clusters += this.numWaterClustersNear(this.xCoord + 2, this.yCoord - 2, this.zCoord);
        clusters += this.numWaterClustersNear(this.xCoord + 2, this.yCoord - 2, this.zCoord + 1);
        clusters += this.numWaterClustersNear(this.xCoord - 1, this.yCoord - 2, this.zCoord - 2);
        clusters += this.numWaterClustersNear(this.xCoord, this.yCoord - 2, this.zCoord - 2);
        clusters += this.numWaterClustersNear(this.xCoord + 1, this.yCoord - 2, this.zCoord - 2);
        clusters += this.numWaterClustersNear(this.xCoord - 1, this.yCoord - 2, this.zCoord + 2);
        clusters += this.numWaterClustersNear(this.xCoord, this.yCoord - 2, this.zCoord + 2);
        clusters += this.numWaterClustersNear(this.xCoord + 1, this.yCoord - 2, this.zCoord + 2);
        int oldJ = this.maxJars;
        this.maxJars = MAXJARS_START + clusters * MAXJARS_INC_CLUSTER;
        if (this.maxJars != oldJ) {
            this.ticksSinceJarsUpdate = this.jarsUpdateFrequency;
        }

        int numEyesOut = 0;
        int oldD = this.maxDistance;
        this.maxDistance = MAXDISTANCE_START + clusters * MAXDISTANCE_INC_CLUSTER;
        if (this.maxDistance < oldD) {
            numEyesOut = this.removeInvalidEyes();
        }

        this.maxEyes = 1 + clusters;
        if (this.eyeCount > this.maxEyes && !this.worldObj.isRemote) {
            int size = this.eyesContainingInventory.getSizeInventory();

            for (int i = this.maxEyes; i < size; i++) {
                ItemStack stack = this.eyesContainingInventory.getStackInSlot(i);
                if (stack == null) {
                    break;
                }

                this.eyesContainingInventory.setInventorySlotContents(i, null);
                this.eyeCount--;
                if (!this.worldObj.isRemote) {
                    TjUtil.dropItemIntoWorld(stack, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
                }

                numEyesOut++;
            }

            this.markDirty();
        }

        for (int i = 0; i < numEyesOut; i++) {
            this.worldObj
                .playSoundEffect(
                    this.xCoord,
                    this.yCoord,
                    this.zCoord,
                    "random.pop",
                    0.2F,
                    ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F
                );
        }
    }

    public static boolean checkValidity(IBlockAccess blockaccess, int x, int y, int z, boolean placementCheck) {
        if (placementCheck) {
            ItemStack crystal = ItemApi.getBlock("blockCrystal", 2);
            if (blockaccess.getBlock(x, y - 1, z) != Block.getBlockFromItem(crystal.getItem()) || blockaccess.getBlockMetadata(x, y - 1, z) != 2) {
                return false;
            }
        } else if (!blockaccess.isAirBlock(x, y - 1, z) && blockaccess.getBlock(x, y - 1, z) != ModBlocks.essentiaLocusAggregator) {
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
        } else if (blockaccess.getBlock(x, y - 2, z) != Blocks.lapis_block) {
            return false;
        } else {
            return !blockIsWater(blockaccess, x - 1, y - 2, z - 1, placementCheck)
                    || !blockIsWater(blockaccess, x - 1, y - 2, z, placementCheck)
                    || !blockIsWater(blockaccess, x - 1, y - 2, z + 1, placementCheck)
                    || !blockIsWater(blockaccess, x, y - 2, z - 1, placementCheck)
                    || !blockIsWater(blockaccess, x, y - 2, z + 1, placementCheck)
                    || !blockIsWater(blockaccess, x + 1, y - 2, z - 1, placementCheck)
                    || !blockIsWater(blockaccess, x + 1, y - 2, z, placementCheck)
                    || !blockIsWater(blockaccess, x + 1, y - 2, z + 1, placementCheck)
                ? false
                : blockIsSilverwoodPlank(blockaccess, x - 2, y - 2, z - 1)
                    && blockIsSilverwoodPlank(blockaccess, x - 2, y - 2, z)
                    && blockIsSilverwoodPlank(blockaccess, x - 2, y - 2, z + 1)
                    && blockIsSilverwoodPlank(blockaccess, x + 2, y - 2, z - 1)
                    && blockIsSilverwoodPlank(blockaccess, x + 2, y - 2, z)
                    && blockIsSilverwoodPlank(blockaccess, x + 2, y - 2, z + 1)
                    && blockIsSilverwoodPlank(blockaccess, x - 1, y - 2, z - 2)
                    && blockIsSilverwoodPlank(blockaccess, x, y - 2, z - 2)
                    && blockIsSilverwoodPlank(blockaccess, x + 1, y - 2, z - 2)
                    && blockIsSilverwoodPlank(blockaccess, x - 1, y - 2, z + 2)
                    && blockIsSilverwoodPlank(blockaccess, x, y - 2, z + 2)
                    && blockIsSilverwoodPlank(blockaccess, x + 1, y - 2, z + 2);
        }
    }

    private static boolean blockIsSilverwoodPlank(IBlockAccess blockaccess, int x, int y, int z) {
        return blockaccess.getBlock(x, y, z) instanceof BlockWoodenDevice && blockaccess.getBlockMetadata(x, y, z) == 7;
    }

    protected static boolean blockIsWater(IBlockAccess blockaccess, int x, int y, int z, boolean sourceOnly) {
        Block block = blockaccess.getBlock(x, y, z);
        return block == Blocks.water ? !sourceOnly || blockaccess.getBlockMetadata(x, y, z) == 0 : !sourceOnly && block == Blocks.flowing_water;
    }

    private int numWaterClustersNear(int x, int y, int z) {
        int count = 0;

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int x2 = x + dir.offsetX;
            int y2 = y + dir.offsetY;
            int z2 = z + dir.offsetZ;
            if (this.worldObj.getBlock(x2, y2, z2) instanceof BlockCrystal && this.worldObj.getBlockMetadata(x2, y2, z2) == 2) {
                count++;
            }
        }

        return count;
    }

    public void beginPlacementProcess() {
        ItemStack crystal = ItemApi.getBlock("blockCrystal", 2);
        if (this.worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord) == Block.getBlockFromItem(crystal.getItem())
            && this.worldObj.getBlockMetadata(this.xCoord, this.yCoord - 1, this.zCoord) == 2) {
            MessageParticles.sendToClients(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.xCoord, this.yCoord - 1, this.zCoord, (short)4);
            this.worldObj.setBlockToAir(this.xCoord, this.yCoord - 1, this.zCoord);
        } else {
            this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
            TjUtil.dropItemIntoWorld(new ItemStack(ModBlocks.essentiaLocus), this.worldObj, this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    public AspectList getAspectsBase() {
        return this.maxAspectSpaceByType;
    }

    @Override
    public AspectList getAspects() {
        return AutomagyRenderEventHandler.isRenderingAspects ? null : this.getAspectsReal();
    }

    @Override
    public AspectList getAspectsReal() {
        return this.aspects;
    }

    @Override
    public float getAspectVerticalShift(boolean spaceAbove) {
        return spaceAbove ? 0.1F : 0.0F;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return false;
    }

    @Override
    public int addToContainer(Aspect tag, int amount) {
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        if (this.hasAggregator && amount == 1) {
            BlockCoord bc = this.takeAspectFromJars(tag);
            if (bc == null) {
                return false;
            } else {
                MessageParticles.sendToClients(this.worldObj, bc.x, bc.y, bc.z, 0, 0, 0, (short)8);
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean takeFromContainer(AspectList ot) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return !this.hasAggregator ? false : this.aspects.getAmount(tag) >= amount;
    }

    @Override
    public boolean doesContainerContain(AspectList ot) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return !this.hasAggregator ? 0 : this.aspects.getAmount(tag);
    }

    @Override
    public void onEyesInventoryChanged(int slot, ItemStack prevStack) {
    }

    @Override
    public int getMaxDistance() {
        return this.maxDistance;
    }

    @Override
    public BlockCoord getPosition() {
        return new BlockCoord(this);
    }

    @Override
    public int getDimensionID() {
        return this.worldObj.provider.dimensionId;
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        this.eyesContainingInventory.writeCustomNBT(nbttagcompound);
        this.aspects.writeToNBT(nbttagcompound);
        nbttagcompound.setInteger("maxEyes", this.maxEyes);
        nbttagcompound.setInteger("maxDistance", this.maxDistance);
        nbttagcompound.setBoolean("hasAggregator", this.hasAggregator);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.eyesContainingInventory.readCustomNBT(nbttagcompound);
        this.eyeCount = 0;
        int size = this.eyesContainingInventory.getSizeInventory();

        for (int i = 0; i < size; i++) {
            if (this.eyesContainingInventory.getStackInSlot(i) != null) {
                this.eyeCount++;
            }
        }

        this.aspects.readFromNBT(nbttagcompound);
        if (this.worldObj != null && this.worldObj.isRemote) {
            this.aspects = ThaumcraftExtension.getSortedAspectList(this.aspects);
        }

        this.maxEyes = nbttagcompound.getInteger("maxEyes");
        this.maxDistance = nbttagcompound.getInteger("maxDistance");
        this.hasAggregator = nbttagcompound.getBoolean("hasAggregator");
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(this.xCoord - 3, this.yCoord - 2, this.zCoord - 3, this.xCoord + 3, this.yCoord + 2, this.zCoord + 3);
    }

    public void checkAggregator() {
        boolean prev = this.hasAggregator;
        this.hasAggregator = this.worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord) == ModBlocks.essentiaLocusAggregator;
        if (prev != this.hasAggregator) {
            this.markDirty();
        }
    }

    public BlockCoord takeAspectFromJars(Aspect aspect) {
        BlockCoord lastBC = this.lastJarWithAspect.get(aspect);
        if (lastBC != null) {
            if (this.jars.contains(lastBC)) {
                TileEntity te = this.worldObj.getTileEntity(lastBC.x, lastBC.y, lastBC.z);
                if (tileIsValidEssentiaContainer(te)) {
                    IAspectSource source = (IAspectSource)te;
                    if (source.takeFromContainer(aspect, 1)) {
                        return lastBC;
                    }
                } else {
                    this.ticksSinceJarsUpdate = this.jarsUpdateFrequency;
                }
            }

            this.lastJarWithAspect.remove(aspect);
        }

        for (BlockCoord bc : this.jars) {
            TileEntity te = this.worldObj.getTileEntity(bc.x, bc.y, bc.z);
            if (tileIsValidEssentiaContainer(te)) {
                IAspectSource source = (IAspectSource)te;
                if (source.takeFromContainer(aspect, 1)) {
                    this.lastJarWithAspect.put(aspect, bc);
                    return bc;
                }
            } else {
                this.ticksSinceJarsUpdate = this.jarsUpdateFrequency;
            }
        }

        return null;
    }

    public static boolean tileIsValidEssentiaContainer(TileEntity te) {
        return te instanceof TileJarFillable || ThaumcraftExtension.objectIsEssentiaReservoir(te) || te instanceof IEssentiaLocusReadable;
    }
}
