package tuhljin.automagy.tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import thaumcraft.api.wands.IWandable;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.lib.RedstoneCalc;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.compat.CompatibilityManager;
import tuhljin.automagy.network.MessageFloatXYZ;
import tuhljin.automagy.network.MessageParticles;
import tuhljin.automagy.renderers.BlockThirstyTankRenderer;

public class TileEntityThirstyTank extends ModTileEntity implements ITileWithTank, IWandable {
    public static final int MAX_STEPS = 32;
    public static final int CAPACITY_IN_BUCKETS_DEFAULT = 16;
    public static int CAPACITY_IN_BUCKETS_PER_UPGRADE = 16;
    public static int DRINK_FREQUENCY_DEFAULT = 30;
    public static float DRINK_FREQUENCY_UPGRADE_MULTIPLIER = 0.65F;
    public static int DRINK_EXTRA_COOLDOWN_OVER_TICK = 600;
    public static int DRINK_FREQUENCY_LONG = 80;
    public static int CONSUME_RATE = 5;
    public static int CONSUME_AMT_PER_UPGRADE = 100;
    public static int SIPHON_RATE = 5;
    public static int SIPHON_AMT_MAX = 150;
    public static int DRINK_FREQUENCY_RAIN = 5;
    public static int DRINK_FREQUENCY_RAIN_SOUND = 80;
    public static FluidStack rainwater = new FluidStack(FluidRegistry.WATER, 10);
    public static int MILKING_RANGE = 2;
    public static String KEY_TIME_MILKED = "timeMilked";
    public static FluidStack milk;
    public static FluidStack mushroomSoup;
    public FluidTank tank;
    public boolean receivingSignal = false;
    protected int cooldown = DRINK_FREQUENCY_DEFAULT;
    protected int lastDirTry = 0;
    protected int rainTicks = 0;
    protected int cooldownConsume = 0;
    protected int cooldownSiphon = 0;
    protected int ticksSinceDrink = 0;
    public int[] glyphs = new int[6];
    public double liquidRenderHeight = -1.0;
    public boolean voids;
    public boolean redstoneControlled;
    public boolean preserves;
    public boolean siphons;
    public boolean milks;
    public int drinkFrequency = DRINK_FREQUENCY_DEFAULT;
    public int consumeAmt = 0;
    private long nextZap;

    public TileEntityThirstyTank() {
        this.tank = new FluidTank(16000);
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (this.voids) {
            return this.fillVoiding(from, resource, doFill);
        } else {
            int i = this.tank.fill(resource, doFill);
            if (doFill && i != 0) {
                this.markDirty();
            }

            return i;
        }
    }

    public int fillVoiding(ForgeDirection from, FluidStack resource, boolean doFill) {
        FluidStack fluid = this.tank.getFluid();
        int prevAmount = 0;
        if (fluid != null) {
            prevAmount = fluid.amount;
            fluid.amount = 0;
        }

        int i = this.tank.fill(resource, doFill);
        if (fluid != null) {
            fluid.amount = Math.min(fluid.amount + prevAmount, this.tank.getCapacity());
        }

        if (i == 0) {
            return 0;
        } else {
            if (doFill && (fluid == null || fluid.amount != prevAmount)) {
                this.markDirty();
            }

            return resource.amount;
        }
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return resource != null && resource.isFluidEqual(this.tank.getFluid()) ? this.drain(from, resource.amount, doDrain) : null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (this.preserves) {
            int amount = this.tank.getFluidAmount();
            if (amount > 0) {
                maxDrain = Math.min(maxDrain, amount - 1);
            }
        }

        FluidStack fluid = this.tank.drain(maxDrain, doDrain);
        if (doDrain && fluid != null) {
            this.markDirty();
        }

        return fluid;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[]{this.tank.getInfo()};
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.hasKey("Empty")) {
            this.tank.setFluid(null);
        } else {
            this.tank.readFromNBT(nbttagcompound);
        }

        this.glyphs = getIntArrayFromNbtOrDefault(nbttagcompound, "glyphs", 0, 6);
        this.updateGlyphEffects();
        this.receivingSignal = nbttagcompound.getBoolean("receivingSignal");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        this.tank.writeToNBT(nbttagcompound);
        nbttagcompound.setIntArray("glyphs", this.glyphs);
        nbttagcompound.setBoolean("receivingSignal", this.receivingSignal);
    }

    public void updateGlyphEffects() {
        int capacity = 16;
        this.consumeAmt = 0;
        this.voids = false;
        this.redstoneControlled = false;
        this.preserves = false;
        this.siphons = false;
        this.milks = false;
        float drinkRate = DRINK_FREQUENCY_DEFAULT;

        for (int i = 0; i < this.glyphs.length; i++) {
            switch (this.glyphs[i]) {
                case 1:
                    this.voids = true;
                    break;
                case 2:
                    this.consumeAmt = this.consumeAmt + CONSUME_AMT_PER_UPGRADE;
                    break;
                case 3:
                    this.siphons = true;
                case 4:
                default:
                    break;
                case 5:
                    this.redstoneControlled = true;
                    break;
                case 6:
                    this.preserves = true;
                    break;
                case 7:
                    drinkRate *= DRINK_FREQUENCY_UPGRADE_MULTIPLIER;
                    break;
                case 8:
                    capacity += CAPACITY_IN_BUCKETS_PER_UPGRADE;
                    break;
                case 9:
                    this.milks = true;
            }
        }

        this.tank.setCapacity(1000 * capacity);
        this.drinkFrequency = Math.round(drinkRate);
    }

    public double getFractionFull() {
        return (float)this.tank.getFluidAmount() / this.tank.getCapacity();
    }

    public int getComparatorStrength() {
        return RedstoneCalc.getRedstoneSignalStrengthFromValues(this.tank.getFluidAmount(), this.tank.getCapacity());
    }

    public void setFluid(FluidStack fluidStack) {
        this.tank.setFluid(fluidStack);
        this.markDirty();
    }

    public void setGlyphs(int[] glyphs) {
        if (glyphs != null && glyphs.length == 6) {
            this.glyphs = glyphs;
            if (this.worldObj != null) {
                this.updateGlyphEffects();
                this.markDirty();
            }
        } else {
            throw new IllegalArgumentException("Argument must be an array of length 6.");
        }
    }

    public boolean installGlyph(int glyph, int side) {
        if (side < 0 || side >= this.glyphs.length) {
            throw new IllegalArgumentException("Invalid side given as argument. Expected 0-" + this.glyphs.length + ". Got " + side + ".");
        } else if (glyph != 0 && this.glyphs[side] == 0) {
            this.glyphs[side] = glyph;
            this.updateGlyphEffects();
            this.markDirty();
            return true;
        } else {
            return false;
        }
    }

    public int removeGlyph(int side) {
        if (side >= 0 && side < this.glyphs.length) {
            int drop = this.glyphs[side];
            if (drop != 0) {
                if (drop == 8) {
                    int newCapacity = this.tank.getCapacity() - 1000 * CAPACITY_IN_BUCKETS_PER_UPGRADE;
                    if (newCapacity < this.tank.getFluidAmount()) {
                        return -1;
                    }
                }

                this.glyphs[side] = 0;
                this.updateGlyphEffects();
                this.markDirty();
            }

            return drop;
        } else {
            throw new IllegalArgumentException("Invalid side given as argument. Expected 0-" + this.glyphs.length + ". Got " + side + ".");
        }
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote) {
            if (this.consumeAmt > 0) {
                this.cooldownConsume--;
                if (this.cooldownConsume < 1) {
                    this.cooldownConsume = CONSUME_RATE;
                    int amtLeft = this.tank.getFluidAmount();
                    if (amtLeft > (this.preserves ? 1 : 0)) {
                        int toConsume = this.preserves ? Math.min(this.consumeAmt, amtLeft - 1) : this.consumeAmt;
                        this.drain(ForgeDirection.UNKNOWN, toConsume, true);
                    }
                }
            }

            if (this.siphons) {
                this.cooldownSiphon--;
                if (this.cooldownSiphon < 1) {
                    this.cooldownSiphon = SIPHON_RATE;
                    int available = this.tank.getFluidAmount();
                    if (this.preserves) {
                        available--;
                    }

                    if (available > 0) {
                        Set<ForgeDirection> siphonDirs = new HashSet<>();

                        for (int i = 0; i < this.glyphs.length; i++) {
                            if (this.glyphs[i] == 3) {
                                ForgeDirection dir = ForgeDirection.getOrientation(i);
                                if (this.siphonInto(dir, 1, false) > 0) {
                                    siphonDirs.add(dir);
                                }
                            }
                        }

                        if (siphonDirs.size() > 0) {
                            int amtTaken = 0;
                            int share = Math.min(available, SIPHON_AMT_MAX) / siphonDirs.size();
                            if (share > 0) {
                                for (ForgeDirection dir : siphonDirs) {
                                    amtTaken += this.siphonInto(dir, share, true);
                                }

                                this.drain(ForgeDirection.UNKNOWN, amtTaken, true);
                            }
                        }
                    }
                }
            }

            if (this.redstoneControlled && !this.receivingSignal) {
                if (this.cooldown > 0) {
                    this.cooldown--;
                }

                return;
            }

            this.cooldown--;
            this.ticksSinceDrink++;
            if (this.cooldown < 1) {
                if (this.milks) {
                    if (this.canMilk()) {
                        int dirTry = this.lastDirTry;

                        do {
                            if (++dirTry > 5) {
                                dirTry = 0;
                            }

                            if (this.glyphs[dirTry] == 9) {
                                if (this.doMilking(dirTry)) {
                                    this.ticksSinceDrink = 0;
                                }
                                break;
                            }
                        } while (dirTry != this.lastDirTry);

                        this.lastDirTry = dirTry;
                    }
                } else if (this.voids || this.tank.getCapacity() - this.tank.getFluidAmount() >= 1000) {
                    int dirTry = this.lastDirTry;
                    Block block = null;
                    Fluid fluidFound = null;
                    ForgeDirection dir = null;
                    int x = 0;
                    int y = 0;
                    int z = 0;

                    do {
                        if (++dirTry > 5) {
                            dirTry = 0;
                        }

                        if (this.glyphs[dirTry] != 3) {
                            dir = ForgeDirection.getOrientation(dirTry);
                            x = this.xCoord + dir.offsetX;
                            y = this.yCoord + dir.offsetY;
                            z = this.zCoord + dir.offsetZ;
                            if (this.glyphs[dirTry] == 4) {
                                TileEntity te = this.worldObj.getTileEntity(x, y, z);
                                if (te instanceof IFluidHandler) {
                                    if (this.pilferFluid((IFluidHandler)te, dir)) {
                                        this.ticksSinceDrink = 0;
                                    }
                                    break;
                                }
                            } else {
                                block = this.worldObj.getBlock(x, y, z);
                                fluidFound = this.getFluidIfValidBlock(block);
                            }
                        }
                    } while (fluidFound == null && dirTry != this.lastDirTry);

                    this.lastDirTry = dirTry;
                    if (fluidFound != null) {
                        boolean ok = false;
                        int added = 1000;
                        if (!AutomagyConfig.thirstyTankPreserveInfiniteWater
                            || fluidFound.getBlock() != Blocks.water
                            || this.worldObj.getBlockMetadata(x, y, z) != 0) {
                            TileEntityThirstyTank.SourceLiquidResult result = this.findSourceLiquid(block, x, y, z, dir);
                            if (result != null) {
                                Block source = this.worldObj.getBlock(result.x, result.y, result.z);
                                if (source instanceof BlockFluidBase) {
                                    ((BlockFluidBase)source).drain(this.worldObj, result.x, result.y, result.z, true);
                                } else {
                                    this.worldObj.setBlockToAir(result.x, result.y, result.z);
                                }

                                ok = true;
                                added = result.amount;
                                this.worldObj
                                    .playSoundEffect(
                                        result.x,
                                        result.y,
                                        result.z,
                                        "random.drink",
                                        0.2F,
                                        this.worldObj.rand.nextFloat() * 0.5F + this.worldObj.rand.nextFloat() * 0.25F + 0.5F
                                    );
                                MessageParticles.sendToClients(this.worldObj, result.x, result.y, result.z, 0, 0, 0, (short)8);
                            }
                        } else if (!this.voids || this.tank.getFluidAmount() < this.tank.getCapacity()) {
                            ok = true;
                            MessageParticles.sendToClients(this.worldObj, x, y, z, 0, 0, 0, (short)8);
                        }

                        if (ok) {
                            this.fill(null, new FluidStack(fluidFound, added), true);
                            this.ticksSinceDrink = 0;
                            this.worldObj
                                .playSoundEffect(
                                    this.xCoord,
                                    this.yCoord,
                                    this.zCoord,
                                    "random.drink",
                                    0.2F,
                                    this.worldObj.rand.nextFloat() * 0.25F + this.worldObj.rand.nextFloat() * 0.25F + 0.3F
                                );
                        }
                    }
                }

                this.cooldown = this.ticksSinceDrink > DRINK_EXTRA_COOLDOWN_OVER_TICK ? DRINK_FREQUENCY_LONG : this.drinkFrequency;
            }

            if (!this.milks
                && AutomagyConfig.thirstyTankDrinksRain
                && TjUtil.isPrecipitationAt(this.worldObj, this.xCoord, this.zCoord)
                && this.worldObj.getPrecipitationHeight(this.xCoord, this.zCoord) == this.yCoord + 1) {
                this.rainTicks++;
                if (this.rainTicks % DRINK_FREQUENCY_RAIN == 0) {
                    int filled = this.fill(null, rainwater, true);
                    if (filled != 0 && this.rainTicks % DRINK_FREQUENCY_RAIN_SOUND == 0) {
                        this.worldObj
                            .playSoundEffect(
                                this.xCoord,
                                this.yCoord,
                                this.zCoord,
                                "random.drink",
                                0.2F,
                                this.worldObj.rand.nextFloat() * 0.25F + this.worldObj.rand.nextFloat() * 0.25F + 0.3F
                            );
                    }

                    if (this.drinkFrequency < DRINK_FREQUENCY_DEFAULT && this.glyphs[this.worldObj.rand.nextInt(this.glyphs.length)] == 7) {
                        this.rainTicks = (int)(this.rainTicks + DRINK_FREQUENCY_RAIN * 0.6666667F);
                    }
                }
            }
        }
    }

    public boolean canMilk() {
        if (this.voids) {
            if (this.tank.getFluidAmount() >= this.tank.getCapacity()) {
                return false;
            }
        } else if (this.tank.getFluidAmount() + 1000 > this.tank.getCapacity()) {
            return false;
        }

        FluidStack fluid = this.tank.getFluid();
        return fluid == null || fluid.getFluid() == this.getFluidStackMilk().getFluid() || fluid.getFluid() == this.getFluidStackMushroomSoup().getFluid();
    }

    public boolean doMilking(int dir) {
        double minx = this.xCoord + 0.5 - (MILKING_RANGE + 0.5);
        double miny = this.yCoord + 0.5 - (MILKING_RANGE + 0.5);
        double minz = this.zCoord + 0.5 - (MILKING_RANGE + 0.5);
        double maxx = this.xCoord + 0.5 + (MILKING_RANGE + 0.5);
        double maxy = this.yCoord + 0.5 + (MILKING_RANGE + 0.5);
        double maxz = this.zCoord + 0.5 + (MILKING_RANGE + 0.5);
        switch (dir) {
            case 0:
                maxy = this.yCoord;
                miny = this.yCoord - 1 - MILKING_RANGE * 2;
                break;
            case 1:
                miny = this.yCoord + 1;
                maxy = this.yCoord + 2 + MILKING_RANGE * 2;
                break;
            case 2:
                maxz = this.zCoord;
                minz = this.zCoord - 1 - MILKING_RANGE * 2;
                break;
            case 3:
                minz = this.zCoord + 1;
                maxz = this.zCoord + 2 + MILKING_RANGE * 2;
                break;
            case 4:
                maxx = this.xCoord;
                minx = this.xCoord - 1 - MILKING_RANGE * 2;
                break;
            case 5:
                minx = this.xCoord + 1;
                maxx = this.xCoord + 2 + MILKING_RANGE * 2;
        }

        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(minx, miny, minz, maxx, maxy, maxz);
        List<EntityCow> entities = this.worldObj.getEntitiesWithinAABB(EntityCow.class, box);
        if (entities.size() > 0) {
            Long t = this.worldObj.getTotalWorldTime();

            for (EntityCow cow : entities) {
                NBTTagCompound data = cow.getEntityData();
                if (t - data.getLong(KEY_TIME_MILKED) >= AutomagyConfig.milkingCooldownPerCow) {
                    int amt = this.fill(
                        ForgeDirection.UNKNOWN, cow instanceof EntityMooshroom ? this.getFluidStackMushroomSoup() : this.getFluidStackMilk(), true
                    );
                    if (amt > 0) {
                        data.setLong(KEY_TIME_MILKED, t);
                        cow.playSound("mob.cow.hurt", 0.4F, (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
                        MessageFloatXYZ.sendToClients(
                            this.worldObj, cow.posX - cow.width / 2.0, cow.posY + cow.height / 2.0, cow.posZ - cow.width / 2.0, (short)1
                        );
                        this.worldObj
                            .playSoundEffect(
                                this.xCoord,
                                this.yCoord,
                                this.zCoord,
                                "random.drink",
                                0.2F,
                                this.worldObj.rand.nextFloat() * 0.25F + this.worldObj.rand.nextFloat() * 0.25F + 0.3F
                            );
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean pilferFluid(IFluidHandler otherTank, ForgeDirection dir) {
        int amount = 1000;
        if (!this.voids) {
            amount = Math.min(amount, this.tank.getCapacity() - this.tank.getFluidAmount());
        }

        if (amount > 0) {
            Fluid drainable = TjUtil.canDrainTank(otherTank, dir.getOpposite(), this.tank.getFluid());
            if (drainable != null) {
                FluidStack gained = otherTank.drain(dir.getOpposite(), new FluidStack(drainable, amount), true);
                if (gained != null && gained.amount > 0) {
                    this.fill(dir, gained, true);
                    MessageParticles.sendToClients(
                        this.worldObj, this.xCoord + dir.offsetX, this.yCoord + dir.offsetY, this.zCoord + dir.offsetZ, 0, 0, 0, (short)8
                    );
                    this.worldObj
                        .playSoundEffect(
                            this.xCoord,
                            this.yCoord,
                            this.zCoord,
                            "random.drink",
                            0.2F,
                            this.worldObj.rand.nextFloat() * 0.25F + this.worldObj.rand.nextFloat() * 0.25F + 0.3F
                        );
                    return true;
                }
            }
        }

        return false;
    }

    protected int siphonInto(ForgeDirection dir, int amount, boolean doFill) {
        TileEntity te = this.worldObj.getTileEntity(this.xCoord + dir.offsetX, this.yCoord + dir.offsetY, this.zCoord + dir.offsetZ);
        if (te instanceof IFluidHandler) {
            FluidStack fluid = this.tank.getFluid();
            if (fluid == null) {
                return 0;
            } else {
                IFluidHandler otherTank = (IFluidHandler)te;
                if (!otherTank.canFill(dir.getOpposite(), fluid.getFluid())) {
                    return 0;
                } else {
                    fluid = fluid.copy();
                    fluid.amount = amount;
                    return otherTank.fill(dir.getOpposite(), fluid, doFill);
                }
            }
        } else {
            return CompatibilityManager.fillBotaniaPetalApothecary(te, doFill) ? amount : 0;
        }
    }

    public Fluid getFluidIfValidBlock(Block block) {
        Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
        if (fluid != null) {
            FluidStack tankFluid = this.tank.getFluid();
            if (tankFluid == null) {
                return fluid;
            } else {
                return tankFluid.fluidID == fluid.getID() ? fluid : null;
            }
        } else {
            return null;
        }
    }

    public TileEntityThirstyTank.SourceLiquidResult findSourceLiquid(Block fluidBlock, int x, int y, int z, ForgeDirection dir) {
        ArrayList<ForgeDirection> list = new ArrayList<>();

        for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            if (d != dir && d != ForgeDirection.UP) {
                list.add(d);
            }
        }

        Collections.shuffle(list);
        list.add(0, dir);
        if (dir != ForgeDirection.UP) {
            list.add(1, ForgeDirection.UP);
        }

        return this.findSourceLiquid(fluidBlock, x, y, z, list, dir, 1, new HashSet<>());
    }

    private TileEntityThirstyTank.SourceLiquidResult findSourceLiquid(
        Block fluidBlock, int x, int y, int z, ArrayList<ForgeDirection> dirs, ForgeDirection nextDir, int steps, HashSet<BlockCoord> seen
    ) {
        BlockCoord coord = new BlockCoord(x, y, z);
        if (seen.contains(coord)) {
            return null;
        } else {
            seen.add(coord);
            Block block = this.worldObj.getBlock(x, y, z);
            if (block == fluidBlock) {
                double distFurthest = TjUtil.getDistanceBetweenPoints_squared(this.xCoord, this.yCoord, this.zCoord, x, y, z);
                if (steps < 32) {
                    TileEntityThirstyTank.SourceLiquidResult bestResult = null;
                    TileEntityThirstyTank.SourceLiquidResult result = this.findSourceLiquid(
                        fluidBlock, x + nextDir.offsetX, y + nextDir.offsetY, z + nextDir.offsetZ, dirs, nextDir, steps + 1, seen
                    );
                    if (result != null && (bestResult == null || result.distance > distFurthest)) {
                        distFurthest = result.distance;
                        bestResult = result;
                    }

                    for (ForgeDirection dir : dirs) {
                        if (dir != nextDir) {
                            result = this.findSourceLiquid(fluidBlock, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dirs, dir, steps + 1, seen);
                            if (result != null && (bestResult == null || result.distance > distFurthest)) {
                                distFurthest = result.distance;
                                bestResult = result;
                            }
                        }
                    }

                    if (bestResult != null) {
                        return bestResult;
                    }
                }

                if (this.worldObj.getBlockMetadata(x, y, z) == 0) {
                    int added = 1000;
                    if (block instanceof BlockFluidBase) {
                        if (!((BlockFluidBase)block).canDrain(this.worldObj, x, y, z)) {
                            return null;
                        }

                        FluidStack amt = ((BlockFluidBase)block).drain(this.worldObj, x, y, z, false);
                        added = amt.amount;
                        if (added < 1) {
                            return null;
                        }
                    }

                    return new TileEntityThirstyTank.SourceLiquidResult(block, x, y, z, distFurthest, added);
                }
            }

            return null;
        }
    }

    @Override
    public boolean drainExactAmount(int amount, boolean doDrain) {
        FluidStack drained = this.drain(ForgeDirection.UNKNOWN, amount, false);
        if (drained.amount == amount) {
            if (doDrain) {
                this.drain(ForgeDirection.UNKNOWN, amount, true);
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean fillExactAmount(FluidStack resource) {
        int amount = this.fill(ForgeDirection.UNKNOWN, resource, false);
        if (amount == resource.amount) {
            this.fill(ForgeDirection.UNKNOWN, resource, true);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean fillExactAmount(BlockFluidBase fluidBlock) {
        return this.fillExactAmount(new FluidStack(fluidBlock.getFluid(), 1000));
    }

    @Override
    public FluidTank getTank() {
        return this.tank;
    }

    @Override
    public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
        if (!world.isRemote && player.isSneaking()) {
            int glyph = this.removeGlyph(side);
            if (glyph > 0) {
                ForgeDirection dir = ForgeDirection.getOrientation(side);
                TjUtil.dropItemIntoWorldSimple(
                    new ItemStack(ModItems.tankGlyph, 1, glyph), world, x + 0.5 + dir.offsetX * 0.6, y + 0.5 + dir.offsetY * 0.6, z + 0.5 + dir.offsetZ * 0.6
                );
                world.playSoundAtEntity(player, "thaumcraft:zap", 0.5F, 1.0F);
            } else if (glyph == -1) {
                long currentTime = System.currentTimeMillis();
                if (currentTime >= this.nextZap) {
                    this.nextZap = currentTime + 1000L;
                    ForgeDirection dir = ForgeDirection.getOrientation(side);
                    player.attackEntityFrom(DamageSource.magic, 1.0F);
                    ThaumcraftExtension.zapLocation(
                        this.worldObj,
                        x + 0.5F + dir.offsetX * 0.1F,
                        y + 0.5F + dir.offsetY * 0.1F,
                        z + 0.5F + dir.offsetZ * 0.1F,
                        (float)player.posX,
                        (float)player.boundingBox.minY + player.eyeHeight,
                        (float)player.posZ
                    );
                }
            }
        }

        return 0;
    }

    @Override
    public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
        return null;
    }

    @Override
    public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {
    }

    @Override
    public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {
    }

    protected FluidStack getFluidStackMilk() {
        if (milk == null) {
            milk = new FluidStack(CompatibilityManager.milkBlock.getFluid(), 1000);
        }

        return milk;
    }

    protected FluidStack getFluidStackMushroomSoup() {
        if (mushroomSoup == null) {
            mushroomSoup = new FluidStack(CompatibilityManager.mushroomSoupBlock.getFluid(), 1000);
        }

        return mushroomSoup;
    }

    public void updateRedstoneInput(boolean newSignal) {
        if (this.receivingSignal) {
            if (!newSignal) {
                this.receivingSignal = false;
                this.markDirty(false);
            }
        } else if (newSignal) {
            this.receivingSignal = true;
            this.markDirty(false);
        }
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        BlockThirstyTankRenderer.renderPass = pass;
        return true;
    }

    public class SourceLiquidResult {
        Block block;
        int x;
        int y;
        int z;
        double distance;
        int amount;

        public SourceLiquidResult(Block block, int x, int y, int z, double distance, int amount) {
            this.block = block;
            this.x = x;
            this.y = y;
            this.z = z;
            this.distance = distance;
            this.amount = amount;
        }
    }
}
