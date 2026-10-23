package tuhljin.automagy.tiles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.TileAlembic;
import thaumcraft.common.tiles.TileBellows;
import tuhljin.automagy.api.essentia.IEssentiaDistillery;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.network.MessageBoilerVenting;

public class TileEntityBoiler extends ModTileEntityWithInventory implements ITileWithTank, IEssentiaDistillery {
    public static final int CAPACITY_IN_BUCKETS = 16;
    public static int MAX_VIS = 50;
    public int vis;
    public int furnaceBurnTime;
    public int venting;
    public int smeltTime = 100;
    public int currentItemBurnTime;
    public int furnaceCookTime;
    public AspectList aspects = new AspectList();
    protected int bellows = -1;
    protected boolean speedBoost = false;
    public FluidTank tank = new FluidTank(16000);
    private int tickCount;

    public TileEntityBoiler() {
        super("blockBoiler", 1);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return TileEntityFurnace.getItemBurnTime(stack) > 0;
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        boolean hasBurnTime = this.furnaceBurnTime > 0;
        boolean needMarKDirty = false;
        this.tickCount++;
        if (this.furnaceBurnTime > 0) {
            this.furnaceBurnTime--;
        }

        if (!this.worldObj.isRemote) {
            if (this.bellows < 0) {
                this.getBellows();
            }

            if (this.tickCount % (this.speedBoost ? 20 : 40) == 0 && this.aspects.size() > 0) {
                AspectList exclude = new AspectList();
                int alembicOffset = 0;
                TileEntity tile = null;

                while (alembicOffset < 5) {
                    tile = this.worldObj.getTileEntity(this.xCoord, this.yCoord + ++alembicOffset, this.zCoord);
                    if (!(tile instanceof TileAlembic)) {
                        break;
                    }

                    TileAlembic alembic = (TileAlembic)tile;
                    if (alembic.aspect != null && alembic.amount < alembic.maxAmount && this.aspects.getAmount(alembic.aspect) > 0) {
                        this.removeVis(alembic.aspect, 1);
                        alembic.addToContainer(alembic.aspect, 1);
                        exclude.merge(alembic.aspect, 1);
                        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
                        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord + alembicOffset, this.zCoord);
                    }

                    tile = null;
                }

                alembicOffset = 0;

                while (alembicOffset < 5) {
                    tile = this.worldObj.getTileEntity(this.xCoord, this.yCoord + ++alembicOffset, this.zCoord);
                    if (!(tile instanceof TileAlembic)) {
                        break;
                    }

                    TileAlembic alembic = (TileAlembic)tile;
                    if (alembic.aspect == null || alembic.amount == 0) {
                        Aspect as = null;
                        if (alembic.aspectFilter == null) {
                            as = this.removeRandomVis(exclude);
                        } else if (this.removeVis(alembic.aspectFilter, 1)) {
                            as = alembic.aspectFilter;
                        }

                        if (as != null) {
                            alembic.addToContainer(as, 1);
                            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
                            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord + alembicOffset, this.zCoord);
                            break;
                        }
                    }
                }
            }

            if (this.furnaceBurnTime == 0 && this.canSmelt()) {
                this.currentItemBurnTime = this.furnaceBurnTime = TileEntityFurnace.getItemBurnTime(this.inventorySlots[0]);
                if (this.furnaceBurnTime > 0) {
                    needMarKDirty = true;
                    this.speedBoost = false;
                    if (this.inventorySlots[0] != null) {
                        if (this.inventorySlots[0].isItemEqual(new ItemStack(ConfigItems.itemResource, 1, 0))) {
                            this.speedBoost = true;
                        }

                        this.inventorySlots[0].stackSize--;
                        if (this.inventorySlots[0].stackSize == 0) {
                            this.inventorySlots[0] = this.inventorySlots[0].getItem().getContainerItem(this.inventorySlots[0]);
                        }
                    }
                }
            }

            if (this.isBurning() && this.canSmelt()) {
                this.furnaceCookTime++;
                if (this.furnaceCookTime >= this.smeltTime) {
                    this.furnaceCookTime = 0;
                    this.smeltItem();
                    needMarKDirty = true;
                }
            } else {
                this.furnaceCookTime = 0;
            }

            if (hasBurnTime != this.furnaceBurnTime > 0) {
                needMarKDirty = true;
                this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            }

            if (needMarKDirty) {
                this.markDirty();
            }
        } else if (this.venting > 0) {
            this.venting--;
            this.vent();
        }
    }

    public boolean isBurning() {
        return this.furnaceBurnTime > 0;
    }

    public boolean hasVis() {
        return this.vis > 0;
    }

    public void startVenting() {
        this.venting = 10;
        this.worldObj
            .playSound(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, "random.fizz", 0.1F, 1.0F + this.worldObj.rand.nextFloat() * 0.1F, false);
    }

    public void vent() {
        float fx = 0.35F;
        float fz = 0.1F;
        float fy = 0.05F;
        float fx2 = 0.1F - this.worldObj.rand.nextFloat() * 0.2F;
        float fz2 = 0.1F - this.worldObj.rand.nextFloat() * 0.2F;
        float fy2 = 2.0F + this.worldObj.rand.nextFloat() * 5.0F;
        int color = 16777215;
        Thaumcraft.proxy
            .drawVentParticles(this.worldObj, this.xCoord + 0.5F + fx, this.yCoord + 0.8F + fy, this.zCoord + fz, 2.0F + fx2, fy2, -2.0F + fz2, color);
        Thaumcraft.proxy
            .drawVentParticles(this.worldObj, this.xCoord + 0.5F - fx, this.yCoord + 0.8F + fy, this.zCoord + fz, -2.0F + fx2, fy2, -2.0F + fz2, color);
        float compensate = 2.0F;
        Thaumcraft.proxy
            .drawVentParticles(
                this.worldObj,
                this.xCoord + 0.5F + fx,
                this.yCoord + 0.8F + fy,
                this.zCoord - fz + 1.0F,
                2.0F + fx2 + compensate,
                fy2,
                2.0F + fz2 + compensate,
                color
            );
        Thaumcraft.proxy
            .drawVentParticles(this.worldObj, this.xCoord + 0.5F - fx, this.yCoord + 0.8F + fy, this.zCoord - fz + 1.0F, -2.0F + fx2, fy2, 2.0F + fz2, color);
    }

    public ItemStack getStackToBoil() {
        if (this.tank.getFluidAmount() > 0) {
            FluidStack fluid = this.tank.getFluid();
            if (fluid != null) {
                Block block = fluid.getFluid().getBlock();
                if (block != null) {
                    return new ItemStack(block);
                }
            }
        }

        return null;
    }

    public boolean hasContentsCannotBoil() {
        if (this.tank.getFluidAmount() < 1) {
            return false;
        } else {
            ItemStack stack = this.getStackToBoil();
            if (stack == null) {
                return true;
            } else {
                AspectList tags = ThaumcraftExtension.getObjectTotalAspects(stack);
                return tags == null || tags.size() == 0;
            }
        }
    }

    protected void getBellows() {
        this.bellows = TileBellows.getBellows(this.worldObj, this.xCoord, this.yCoord, this.zCoord, ForgeDirection.VALID_DIRECTIONS);
    }

    private boolean canSmelt() {
        if (this.tank.getFluidAmount() < 1000) {
            return false;
        } else {
            ItemStack stack = this.getStackToBoil();
            if (stack == null) {
                return false;
            } else {
                AspectList al = ThaumcraftExtension.getObjectTotalAspects(stack);
                if (al != null && al.size() != 0) {
                    int vs = al.visSize();
                    if (vs > MAX_VIS - this.vis) {
                        return false;
                    } else {
                        this.smeltTime = (int)(vs * 10 * (1.0F - 0.125F * this.bellows));
                        return true;
                    }
                } else {
                    return false;
                }
            }
        }
    }

    public void smeltItem() {
        if (this.canSmelt() && this.drainExactAmount(1000, false)) {
            ItemStack stack = this.getStackToBoil();
            AspectList al = ThaumcraftExtension.getObjectTotalAspects(stack);

            for (Aspect a : al.getAspects()) {
                this.aspects.add(a, al.getAmount(a));
            }

            this.vis = this.aspects.visSize();
            this.drain(ForgeDirection.UNKNOWN, 1000, true);
            MessageBoilerVenting.sendToClients(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
        }
    }

    public boolean removeVis(Aspect tag, int amount) {
        if (this.aspects != null && this.aspects.getAmount(tag) >= amount) {
            this.aspects.remove(tag, amount);
            this.vis -= amount;
            return true;
        } else {
            return false;
        }
    }

    public Aspect removeRandomVis(AspectList exclude) {
        if (this.aspects.size() > 0) {
            AspectList temp = this.aspects.copy();
            if (exclude.size() > 0) {
                for (Aspect a : exclude.getAspects()) {
                    temp.remove(a);
                }
            }

            if (temp.size() > 0) {
                Aspect tag = temp.getAspects()[this.worldObj.rand.nextInt(temp.getAspects().length)];
                this.aspects.remove(tag, 1);
                this.vis--;
                return tag;
            }
        }

        return null;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.speedBoost = nbttagcompound.getBoolean("speedBoost");
        this.furnaceCookTime = nbttagcompound.getShort("furnaceCookTime");
        this.currentItemBurnTime = TileEntityFurnace.getItemBurnTime(this.inventorySlots[0]);
        this.aspects.readFromNBT(nbttagcompound);
        this.vis = this.aspects.visSize();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean("speedBoost", this.speedBoost);
        nbttagcompound.setShort("furnaceCookTime", (short)this.furnaceCookTime);
        this.aspects.writeToNBT(nbttagcompound);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.vis = nbttagcompound.getShort("vis");
        this.furnaceBurnTime = nbttagcompound.getShort("furnaceBurnTime");
        if (nbttagcompound.hasKey("Empty")) {
            this.tank.setFluid(null);
        } else {
            this.tank.readFromNBT(nbttagcompound);
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setShort("vis", (short)this.vis);
        nbttagcompound.setShort("furnaceBurnTime", (short)this.furnaceBurnTime);
        this.tank.writeToNBT(nbttagcompound);
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        int i = this.tank.fill(resource, doFill);
        if (doFill && i != 0) {
            this.markDirty();
        }

        return i;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource != null && resource.isFluidEqual(this.tank.getFluid())) {
            FluidStack fluid = this.tank.drain(resource.amount, doDrain);
            if (doDrain && fluid != null) {
                this.markDirty();
            }

            return fluid;
        } else {
            return null;
        }
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        FluidStack fluid = this.tank.drain(maxDrain, doDrain);
        if (doDrain && fluid != null) {
            this.markDirty();
        }

        return fluid;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return from != ForgeDirection.UP;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return from != ForgeDirection.UP;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[]{this.tank.getInfo()};
    }

    @Override
    public FluidTank getTank() {
        return this.tank;
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
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        if (this.worldObj != null) {
            this.worldObj.updateLightByType(EnumSkyBlock.Block, this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @SideOnly(Side.CLIENT)
    public int getBurnTimeRemainingScaled(int i) {
        if (this.currentItemBurnTime == 0) {
            this.currentItemBurnTime = 200;
        }

        return this.furnaceBurnTime * i / this.currentItemBurnTime;
    }

    @SideOnly(Side.CLIENT)
    public int getCookProgressScaled(int i) {
        if (this.smeltTime <= 0) {
            this.smeltTime = 1;
        }

        return this.furnaceCookTime * i / this.smeltTime;
    }

    @SideOnly(Side.CLIENT)
    public int getContentsScaled(int i) {
        return this.vis * i / MAX_VIS;
    }

    @Override
    public int getFurnaceBurnTime() {
        return this.furnaceBurnTime;
    }

    @Override
    public void setFurnaceBurnTime(int i) {
        this.furnaceBurnTime = i;
        this.markDirty(false);
    }

    @Override
    public boolean isSpeedBoosted() {
        return this.speedBoost;
    }

    @Override
    public void setSpeedBoosted(boolean b) {
        this.speedBoost = b;
        this.markDirty(false);
    }

    @Override
    public int getStoredVis() {
        return this.vis;
    }

    @Override
    public int getStoredVisMax() {
        return MAX_VIS;
    }
}
