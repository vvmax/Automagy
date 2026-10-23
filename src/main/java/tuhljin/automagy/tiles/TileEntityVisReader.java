package tuhljin.automagy.tiles;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.nodes.INode;
import thaumcraft.common.items.baubles.ItemAmuletVis;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.tiles.TileAlembic;
import thaumcraft.common.tiles.TileCentrifuge;
import thaumcraft.common.tiles.TileJarFillable;
import thaumcraft.common.tiles.TileTubeBuffer;
import thaumcraft.common.tiles.TileWandPedestal;
import tuhljin.automagy.api.essentia.IAspectContainerWithMax;
import tuhljin.automagy.lib.NeighborNotifier;
import tuhljin.automagy.lib.RedstoneCalc;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.events.AutomagyRenderEventHandler;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

public class TileEntityVisReader extends ModTileEntityWithInventory implements IAspectContainerLarge {
    protected final int COOLDOWN_TIME = 4;
    public static final int NUM_CONFIGURATIONS = 4;
    public static final int ALL_DIR = 6;
    public static final int modeDISABLED = 0;
    public static final int modeTOTAL = 1;
    public static final int modeANYHIGH = 2;
    public static final int modeANYLOW = 3;
    public static final int modeASPECTCOUNT = 4;
    public int targetX;
    public int targetY = -1;
    public int targetZ;
    public int[] modes = new int[]{1, 0, 0, 0};
    public int[] capacities = new int[4];
    public int[] outputDirs = new int[]{6, 6, 6, 6};
    public boolean[] inverseSignals = new boolean[4];
    protected int[] redstoneSignalStrength = new int[5];
    private boolean antiloopIsChecking = false;
    protected int cooldown;
    private String lastAspects = "";

    public TileEntityVisReader() {
        super("blockVisReader", 4, false);
        this.cooldown = 0;
        this.accessibleSlots = new int[0];
        this.notifyOnInventoryChanged = true;
    }

    public boolean hasActiveRedstoneSignal() {
        for (int i = 0; i < 5; i++) {
            if (this.redstoneSignalStrength[i] > 0) {
                return true;
            }
        }

        return false;
    }

    public int getRedstoneSignalStrength(int side) {
        if (side == 0) {
            return this.redstoneSignalStrength[4];
        } else {
            side = TjUtil.getSWNESide(side);
            return side == -1 ? 0 : this.redstoneSignalStrength[side];
        }
    }

    protected boolean setRedstoneSignalStrength(int side, int strength) {
        if (side == 0) {
            side = 4;
        } else {
            side = TjUtil.getSWNESide(side);
        }

        if (side != -1 && this.redstoneSignalStrength[side] != strength) {
            this.redstoneSignalStrength[side] = strength;
            return true;
        } else {
            return false;
        }
    }

    public void findNewTarget(boolean neighborMayBeRemoteComparator) {
        int x = this.xCoord;
        int y = this.yCoord + 1;
        int z = this.zCoord;
        TileEntity te = this.worldObj.getTileEntity(x, y, z);
        if (te instanceof TileEntityRemoteComparator) {
            if (!neighborMayBeRemoteComparator) {
                return;
            }

            WorldSpecificCoordinates coord = ((TileEntityRemoteComparator)te).getLinkLocation();
            if (((TileEntityRemoteComparator)te).coordinatesAreInRange(coord) && coord != null) {
                if (coord.x != this.targetX || coord.y != this.targetY || coord.z != this.targetZ) {
                    this.targetX = coord.x;
                    this.targetY = coord.y;
                    this.targetZ = coord.z;
                    this.lastAspects = "";
                    this.calculateRedstoneSignalStrength();
                    this.markDirty();
                }

                return;
            }
        } else if (TileEntityEssentiaLocus.blockIsWater(this.worldObj, x, y, z, false)) {
            ForgeDirection[] dirs = new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST};

            label60:
            for (ForgeDirection dir : dirs) {
                int x2 = x + dir.offsetX;
                int y2 = y + 2;
                int z2 = z + dir.offsetZ;
                if (this.worldObj.getTileEntity(x2, y2, z2) instanceof TileEntityEssentiaLocus) {
                    x = x2;
                    y = y2;
                    z = z2;
                    break;
                }

                for (ForgeDirection subdir : dirs) {
                    int x3 = x2 + subdir.offsetX;
                    int z3 = z2 + subdir.offsetZ;
                    if (this.worldObj.getTileEntity(x3, y2, z3) instanceof TileEntityEssentiaLocus) {
                        x = x3;
                        y = y2;
                        z = z3;
                        break label60;
                    }
                }
            }
        }

        if (x != this.targetX || y != this.targetY || z != this.targetZ) {
            this.targetX = x;
            this.targetY = y;
            this.targetZ = z;
            this.lastAspects = "";
            this.calculateRedstoneSignalStrength();
            this.markDirty();
        }
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
                this.cooldown = 4;
                this.calculateRedstoneSignalStrength();
            }
        }
    }

    private void calculateRedstoneSignalStrength() {
        AspectList aspects = this.getAspects();
        boolean changed = false;
        if (aspects != null) {
            int total = 0;
            int highestAmt = 0;
            int lowestAmt = Integer.MAX_VALUE;
            StringBuilder strAspects = new StringBuilder(",");

            for (Aspect a : aspects.getAspects()) {
                if (a != null) {
                    int amt = aspects.getAmount(a);
                    total += amt;
                    highestAmt = Math.max(amt, highestAmt);
                    lowestAmt = Math.min(amt, lowestAmt);
                    strAspects.append(a.getTag() + ":" + amt + ",");
                }
            }

            String newAspects = strAspects.toString();
            if (this.lastAspects.equals(newAspects)) {
                return;
            }

            this.lastAspects = newAspects;
            if (lowestAmt > highestAmt) {
                lowestAmt = highestAmt;
            }

            int detectedCapacity = -1;
            int detectedIndividualCapacity = 0;
            Aspect detectedIndividualAspectHighest = null;
            Aspect detectedIndividualAspectLowest = null;
            AspectList aspectsKnownCapacity = null;
            int[] strengths = new int[]{0, 0, 0, 0, 0, 0};

            for (int i = 0; i < 4; i++) {
                if (this.modes[i] != 0) {
                    int strength;
                    if (this.modes[i] == 4) {
                        strength = aspects.size();
                        if (strength > 15) {
                            if (this.inverseSignals[i]) {
                                strength = 15;
                            } else if (strength > 99) {
                                strength = 99;
                            }
                        }
                    } else {
                        int capacity = this.capacities[i];
                        int individualCapacity = -1;
                        if (capacity < 1) {
                            if (detectedCapacity == -1) {
                                IAspectContainer target = this.getUltimateTarget();
                                if (target != null) {
                                    detectedCapacity = 64;
                                    if (target instanceof TileWandPedestal) {
                                        detectedCapacity = 25;
                                        ItemStack stack = ThaumcraftExtension.getStackInRechargePedestal((TileWandPedestal)target);
                                        if (stack != null) {
                                            Item item = stack.getItem();
                                            if (item instanceof ItemWandCasting) {
                                                ItemWandCasting wand = (ItemWandCasting)item;
                                                detectedIndividualCapacity = wand.getMaxVis(stack) / 100;
                                                detectedCapacity = detectedIndividualCapacity * 6;
                                            } else if (item instanceof ItemAmuletVis) {
                                                ItemAmuletVis amulet = (ItemAmuletVis)item;
                                                detectedIndividualCapacity = amulet.getMaxVis(stack) / 100;
                                                detectedCapacity = detectedIndividualCapacity * 6;
                                            }
                                        }
                                    } else if (!(target instanceof INode) && !(target instanceof IAspectContainerWithMax)) {
                                        if (target instanceof TileJarFillable) {
                                            detectedCapacity = ((TileJarFillable)target).maxAmount;
                                        } else if (target instanceof TileAlembic) {
                                            detectedCapacity = 32;
                                        } else if (target instanceof TileTubeBuffer) {
                                            detectedCapacity = 8;
                                        } else if (target instanceof TileCentrifuge) {
                                            detectedCapacity = 1;
                                        } else if (ThaumcraftExtension.objectIsEssentiaReservoir(target)) {
                                            detectedCapacity = 256;
                                        }

                                        detectedIndividualCapacity = detectedCapacity;
                                    } else {
                                        if (target instanceof INode) {
                                            aspectsKnownCapacity = ((INode)target).getAspectsBase();
                                        } else {
                                            aspectsKnownCapacity = ((IAspectContainerWithMax)target).getAspectsBase();
                                        }

                                        if (aspectsKnownCapacity != null) {
                                            int count = 0;
                                            float highest = 0.0F;
                                            float lowest = Float.MAX_VALUE;

                                            for (Aspect ax : aspectsKnownCapacity.getAspects()) {
                                                if (ax != null && aspects.aspects.containsKey(ax)) {
                                                    int amt = aspectsKnownCapacity.getAmount(ax);
                                                    if (amt > 0) {
                                                        count += amt;
                                                        float f = (float)aspects.getAmount(ax) / amt;
                                                        if (f > highest) {
                                                            highest = f;
                                                            detectedIndividualAspectHighest = ax;
                                                        }

                                                        if (f < lowest) {
                                                            lowest = f;
                                                            detectedIndividualAspectLowest = ax;
                                                        }
                                                    }
                                                }
                                            }

                                            detectedCapacity = count;
                                        }
                                    }
                                } else {
                                    detectedCapacity = 0;
                                }
                            }

                            capacity = detectedCapacity;
                            individualCapacity = detectedIndividualCapacity;
                        }

                        if (individualCapacity < 1) {
                            individualCapacity = capacity;
                        }

                        if (this.inventorySlots[i] != null) {
                            AspectList stackAspects = ThaumcraftExtension.getAspectsFromItem(this.inventorySlots[i]);
                            if (stackAspects == null) {
                                strength = 0;
                            } else {
                                Aspect axx = stackAspects.getAspects()[0];
                                int aspectCapacity;
                                if (this.capacities[i] < 1 && aspectsKnownCapacity != null) {
                                    aspectCapacity = aspectsKnownCapacity.getAmount(axx);
                                } else {
                                    aspectCapacity = this.modes[i] == 1 ? capacity : individualCapacity;
                                }

                                strength = RedstoneCalc.getRedstoneSignalStrengthFromValues(aspects.getAmount(axx), aspectCapacity);
                            }
                        } else if (this.modes[i] == 1) {
                            strength = RedstoneCalc.getRedstoneSignalStrengthFromValues(total, capacity);
                        } else if (this.modes[i] == 2) {
                            if (detectedIndividualAspectHighest == null) {
                                strength = RedstoneCalc.getRedstoneSignalStrengthFromValues(highestAmt, individualCapacity);
                            } else {
                                strength = RedstoneCalc.getRedstoneSignalStrengthFromValues(
                                    aspects.getAmount(detectedIndividualAspectHighest), aspectsKnownCapacity.getAmount(detectedIndividualAspectHighest)
                                );
                            }
                        } else if (this.modes[i] == 3) {
                            if (detectedIndividualAspectLowest == null) {
                                strength = RedstoneCalc.getRedstoneSignalStrengthFromValues(lowestAmt, individualCapacity);
                            } else {
                                strength = RedstoneCalc.getRedstoneSignalStrengthFromValues(
                                    aspects.getAmount(detectedIndividualAspectLowest), aspectsKnownCapacity.getAmount(detectedIndividualAspectLowest)
                                );
                            }
                        } else {
                            strength = 0;
                        }
                    }

                    if (this.inverseSignals[i]) {
                        strength = 15 - strength;
                    }

                    int side = this.outputDirs[i];
                    if (side == 6) {
                        strengths[0] = Math.max(strengths[0], strength);
                        strengths[2] = Math.max(strengths[2], strength);
                        strengths[3] = Math.max(strengths[3], strength);
                        strengths[4] = Math.max(strengths[4], strength);
                        strengths[5] = Math.max(strengths[5], strength);
                    } else {
                        strengths[side] = Math.max(strengths[side], strength);
                    }
                }
            }

            for (int side = 0; side < strengths.length; side++) {
                if (this.setRedstoneSignalStrength(side, strengths[side])) {
                    changed = true;
                }
            }
        } else {
            if (this.setRedstoneSignalStrength(0, 0)) {
                changed = true;
            }

            if (this.setRedstoneSignalStrength(2, 0)) {
                changed = true;
            }

            if (this.setRedstoneSignalStrength(3, 0)) {
                changed = true;
            }

            if (this.setRedstoneSignalStrength(4, 0)) {
                changed = true;
            }

            if (this.setRedstoneSignalStrength(5, 0)) {
                changed = true;
            }

            this.lastAspects = "";
        }

        if (changed) {
            this.markDirty();
            NeighborNotifier.notifyBlocksOfExtendedNeighborChange(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
        }
    }

    public IAspectContainer getUltimateTarget() {
        if (this.antiloopIsChecking) {
            return null;
        } else {
            IAspectContainer target = this.getTarget();
            if (target instanceof TileEntityVisReader) {
                this.antiloopIsChecking = true;
                target = ((TileEntityVisReader)target).getUltimateTarget();
                this.antiloopIsChecking = false;
            }

            return target;
        }
    }

    public IAspectContainer getTarget() {
        TileEntity target = this.worldObj.getTileEntity(this.targetX, this.targetY, this.targetZ);
        return target instanceof IAspectContainer ? (IAspectContainer)target : null;
    }

    public void receiveGUIMessage(int configNum, int type, int data) {
        if (configNum >= 0 && configNum < 4) {
            switch (type) {
                case 0:
                    this.modes[configNum] = data;
                    break;
                case 1:
                    this.capacities[configNum] = data;
                    break;
                case 2:
                    this.outputDirs[configNum] = data;
                    break;
                case 3:
                    this.inverseSignals[configNum] = data == 1;
                    break;
                default:
                    FMLLog.warning("[Automagy] TileEntityVisReader received invalid packet data. Ignoring. (type=" + type + ")");
                    return;
            }

            this.lastAspects = "";
            this.markDirty(false);
        } else {
            FMLLog.warning("[Automagy] TileEntityVisReader received invalid packet data. Ignoring. (configNum=" + configNum + ")");
        }
    }

    @Override
    public void onInventoryChanged(int slot, ItemStack prevStack) {
        this.lastAspects = "";
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return ThaumcraftExtension.itemIsJarLabel(stack) ? ThaumcraftExtension.getAspectsFromItem(stack) != null : false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public AspectList getAspects() {
        return AutomagyRenderEventHandler.isRenderingAspects ? null : this.getAspectsReal();
    }

    @Override
    public AspectList getAspectsReal() {
        if (this.antiloopIsChecking) {
            return null;
        } else {
            IAspectContainer target = this.getTarget();
            if (target != null) {
                this.antiloopIsChecking = true;
                AspectList aspects = target.getAspects();
                this.antiloopIsChecking = false;
                return aspects;
            } else {
                return null;
            }
        }
    }

    @Override
    public float getAspectVerticalShift(boolean spaceAbove) {
        return spaceAbove ? 0.4F : 0.0F;
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
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList ot) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContain(AspectList ot) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return 0;
    }

    @Override
    public int[] getSlotsForFace(int side) {
        return side > 1 && side < 6 ? new int[]{side - 2} : this.accessibleSlots;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.targetX = nbttagcompound.getInteger("targetX");
        this.targetY = nbttagcompound.getInteger("targetY");
        this.targetZ = nbttagcompound.getInteger("targetZ");
        this.redstoneSignalStrength = getIntArrayFromNbtOrDefault(nbttagcompound, "strength", 0, 5);
        this.modes = getIntArrayFromNbtOrDefault(nbttagcompound, "modes", 1, 4);
        this.capacities = getIntArrayFromNbtOrDefault(nbttagcompound, "capacity", 0, 4);
        this.outputDirs = getIntArrayFromNbtOrDefault(nbttagcompound, "outputDir", 6, 4);
        this.inverseSignals = getBooleanArrayFromNbtOrDefault(nbttagcompound, "inverse", false, 4);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setInteger("targetX", this.targetX);
        nbttagcompound.setInteger("targetY", this.targetY);
        nbttagcompound.setInteger("targetZ", this.targetZ);
        nbttagcompound.setIntArray("strength", this.redstoneSignalStrength);
        nbttagcompound.setIntArray("modes", this.modes);
        nbttagcompound.setIntArray("capacity", this.capacities);
        nbttagcompound.setIntArray("outputDir", this.outputDirs);
        setBooleanArrayInNbt(nbttagcompound, "inverse", this.inverseSignals);
    }
}
