package tuhljin.automagy.tiles;

import cpw.mods.fml.common.FMLLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.logging.log4j.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.config.ConfigBlocks;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.network.MessageParticles;

public class TileEntityThaumostaticController extends TileEntityThaumostaticPylonBase implements IEssentiaTransport {
    public static final int ARMORSLOT_CHEST = 2;
    public static int MAX_STORED_ASPECT = 8;
    public static AspectList maxAspects = new AspectList()
        .add(Aspect.ENERGY, MAX_STORED_ASPECT)
        .add(Aspect.AIR, MAX_STORED_ASPECT)
        .add(Aspect.FLIGHT, MAX_STORED_ASPECT);
    public static List<Aspect> drawAspects = Arrays.asList(Aspect.ENERGY, Aspect.AIR, Aspect.FLIGHT);
    public static int RANGE_STANDARD_PER_PYLON = 4;
    public static int RANGE_UPGRADE_PER_PYLON = 15;
    public static int TRANSFER_RATE_STANDARD = 30;
    public static int TRANSFER_AMT_STANDARD = 1;
    public static int TRANSFER_AMT_UPGRADED = 1;
    public static int PYLON_TRANSFER_RATE_BONUS = 4;
    public static int MIN_PYLONS = 2;
    public static int MAX_EFFECTIVE_PYLONS = 6;
    public static int UPGRADING_ESSENTIA_CONSUME_RATE = 1200;
    public AspectList aspects = new AspectList();
    public boolean active = false;
    public int spireY = -1;
    public int pylonCount = 0;
    public int timeRangeUp;
    public int timeRateUp;
    protected HashMap<ForgeDirection, Aspect> dirSuction = new HashMap<>();
    protected int nextDistribution = 0;

    public TileEntityThaumostaticController() {
        this.dirSuction.put(ForgeDirection.NORTH, Aspect.ENERGY);
        this.dirSuction.put(ForgeDirection.SOUTH, Aspect.ENERGY);
        this.dirSuction.put(ForgeDirection.WEST, Aspect.ENERGY);
        this.dirSuction.put(ForgeDirection.EAST, Aspect.ENERGY);
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (this.active && !this.worldObj.isRemote) {
            boolean changed = false;
            int prevEnergy = this.aspects.getAmount(Aspect.ENERGY);
            int prevAir = this.aspects.getAmount(Aspect.AIR);
            int prevFlight = this.aspects.getAmount(Aspect.FLIGHT);
            AspectList drawAspectsNow = null;

            for (Aspect a : drawAspects) {
                if (this.aspects.getAmount(a) < MAX_STORED_ASPECT) {
                    if (drawAspectsNow == null) {
                        drawAspectsNow = new AspectList();
                    }

                    drawAspectsNow.add(a, 1);
                }
            }

            if (drawAspectsNow != null) {
                ForgeDirection[] dirs = new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST};

                for (ForgeDirection dir : dirs) {
                    ThaumcraftExtension.AspectWithAmount drawn = ThaumcraftExtension.drawEssentia(
                        this.worldObj, this.xCoord, this.yCoord, this.zCoord, dir, drawAspectsNow, this.getSuctionAmount(dir)
                    );
                    if (drawn != null) {
                        this.aspects.add(drawn.aspect, drawn.amount);
                        changed = true;
                        if (this.aspects.getAmount(drawn.aspect) >= MAX_STORED_ASPECT) {
                            drawAspectsNow.remove(drawn.aspect);
                            if (drawAspectsNow.size() < 1) {
                                break;
                            }
                        }
                    }
                }
            }

            int energyLeft = this.aspects.getAmount(Aspect.ENERGY);
            TileEntityThaumostaticRefuelerSpire teSpire = this.getSpireTile();
            if (teSpire != null && teSpire.isPowered()) {
                if (this.nextDistribution > 0) {
                    this.nextDistribution--;
                    changed = true;
                }

                if (this.nextDistribution == 0) {
                    this.nextDistribution = TRANSFER_RATE_STANDARD;
                    if (this.timeRateUp > 0) {
                        this.nextDistribution = this.nextDistribution - this.pylonCount * PYLON_TRANSFER_RATE_BONUS;
                    }

                    if (this.doPotentiaDistribution()) {
                        changed = true;
                        energyLeft = this.aspects.getAmount(Aspect.ENERGY);
                        teSpire.doSparks();
                    }
                }

                if (this.timeRangeUp > 0) {
                    this.timeRangeUp--;
                    changed = true;
                }

                if (this.timeRangeUp == 0 && energyLeft > 0) {
                    int e = this.aspects.getAmount(Aspect.AIR);
                    if (e > 0) {
                        this.aspects.remove(Aspect.AIR, 1);
                        this.timeRangeUp = UPGRADING_ESSENTIA_CONSUME_RATE;
                        changed = true;
                    }
                }

                if (this.timeRateUp > 0) {
                    this.timeRateUp--;
                    changed = true;
                }

                if (this.timeRateUp == 0 && energyLeft > 0) {
                    int e = this.aspects.getAmount(Aspect.FLIGHT);
                    if (e > 0) {
                        this.aspects.remove(Aspect.FLIGHT, 1);
                        this.timeRateUp = UPGRADING_ESSENTIA_CONSUME_RATE;
                        changed = true;
                    }
                }
            }

            if (changed) {
                if (prevEnergy == energyLeft && prevAir == this.aspects.getAmount(Aspect.AIR) && prevFlight == this.aspects.getAmount(Aspect.FLIGHT)) {
                    this.markDirty(false);
                } else {
                    this.markDirty(true);
                }
            }
        }
    }

    public boolean doPotentiaDistribution() {
        int startEnergy = this.aspects.getAmount(Aspect.ENERGY);
        if (startEnergy < 1) {
            return false;
        } else {
            double range;
            if (this.timeRangeUp > 0) {
                range = this.pylonCount * RANGE_UPGRADE_PER_PYLON;
            } else {
                range = this.pylonCount * RANGE_STANDARD_PER_PYLON;
            }

            ArrayList<EntityPlayer> playerList = TjUtil.getPlayersIn2DRangeSquared(this.worldObj, this.xCoord + 0.5, this.zCoord + 0.5, range * range);
            if (playerList.size() > 0) {
                int maxAmt = this.timeRateUp > 0 ? TRANSFER_AMT_UPGRADED : TRANSFER_AMT_STANDARD;
                int energy = startEnergy;

                for (EntityPlayer player : playerList) {
                    ItemStack stackChest = player.inventory.armorItemInSlot(2);
                    if (stackChest != null) {
                        ThaumcraftExtension.EssentiaJarItemRef jar = ThaumcraftExtension.getJarFromHarness(stackChest);
                        if (jar != null) {
                            AspectList jarAspects = jar.getAspects();
                            int amt;
                            if (jarAspects == null) {
                                jarAspects = new AspectList();
                                amt = maxAmt;
                            } else {
                                amt = Math.min(maxAmt, 64 - jarAspects.getAmount(Aspect.ENERGY));
                            }

                            if (amt > 0) {
                                if (amt > energy) {
                                    amt = energy;
                                }

                                jar.setAspects(jarAspects.add(Aspect.ENERGY, amt));
                                energy -= amt;
                                player.inventoryContainer.detectAndSendChanges();
                            }

                            if (energy < 1) {
                                break;
                            }
                        }
                    }
                }

                if (energy != startEnergy) {
                    this.aspects.remove(Aspect.ENERGY, startEnergy - energy);
                    return true;
                }
            }

            return false;
        }
    }

    public Aspect switchSuction(ForgeDirection dir) {
        Aspect a = this.dirSuction.get(dir);
        int i = drawAspects.indexOf(a);
        if (++i >= drawAspects.size()) {
            i = 0;
        }

        a = drawAspects.get(i);
        this.dirSuction.put(dir, a);
        return a;
    }

    public boolean checkValidity() {
        int pylonsHere = 0;
        int y = this.yCoord - 1;

        for (TileEntity te = this.worldObj.getTileEntity(this.xCoord, y, this.zCoord);
            te instanceof TileEntityThaumostaticPylon;
            te = this.worldObj.getTileEntity(this.xCoord, --y, this.zCoord)
        ) {
            pylonsHere++;
        }

        if (this.worldObj.getBlock(this.xCoord, y, this.zCoord) == Blocks.quartz_block && this.worldObj.getBlockMetadata(this.xCoord, y, this.zCoord) == 1) {
            y = this.yCoord + 1;

            for (TileEntity var5 = this.worldObj.getTileEntity(this.xCoord, y, this.zCoord);
                var5 instanceof TileEntityThaumostaticPylon;
                var5 = this.worldObj.getTileEntity(this.xCoord, ++y, this.zCoord)
            ) {
                pylonsHere++;
            }

            if (pylonsHere < MIN_PYLONS) {
                return false;
            } else if (this.worldObj.getBlock(this.xCoord, y, this.zCoord) != ConfigBlocks.blockCosmeticSolid
                || this.worldObj.getBlockMetadata(this.xCoord, y, this.zCoord) != 4) {
                return false;
            } else if (this.worldObj.isAirBlock(this.xCoord, y + 1, this.zCoord) && this.worldObj.canBlockSeeTheSky(this.xCoord, y + 1, this.zCoord)) {
                this.pylonCount = Math.min(pylonsHere, MAX_EFFECTIVE_PYLONS);
                this.markDirty(false);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void activate() {
        TileEntity te = this.worldObj.getTileEntity(this.xCoord, this.yCoord - 1, this.zCoord);
        if (te instanceof TileEntityThaumostaticPylon) {
            ((TileEntityThaumostaticPylon)te).activate(this.yCoord, false);
        }

        te = this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord);
        if (te instanceof TileEntityThaumostaticPylon) {
            ((TileEntityThaumostaticPylon)te).activate(this.yCoord, true);
        }

        int y = this.yCoord + 1;

        while (
            this.worldObj.getBlock(this.xCoord, y, this.zCoord) != ConfigBlocks.blockCosmeticSolid
                || this.worldObj.getBlockMetadata(this.xCoord, y, this.zCoord) != 4
        ) {
            if (++y > 500) {
                FMLLog.log(Level.ERROR, "[Automagy]  Failed to place Spire block. Thaumium block unexpectedly went missing.");
                return;
            }
        }

        this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, "thaumcraft:wand", 1.0F, 1.0F);
        this.worldObj.setBlock(this.xCoord, y, this.zCoord, ModBlocks.thaumostaticSpire);
        MessageParticles.sendToClients(this.worldObj, this.xCoord, y, this.zCoord, 0, 0, 0, (short)0);
        this.worldObj.playSoundEffect(this.xCoord + 0.5, y + 0.5, this.zCoord + 0.5, "dig.stone", 1.0F, 1.0F);
        this.worldObj.setBlock(this.xCoord, y + 1, this.zCoord, ModBlocks.thaumostaticSpire, 1, 3);
        this.spireY = y;
        this.active = true;
        this.nextDistribution = TRANSFER_RATE_STANDARD;
        this.markDirty();
    }

    @Override
    public void assureValidity() {
        if (this.active) {
            Block b = this.worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord);
            int md = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord - 1, this.zCoord);
            if (b == ModBlocks.thaumostaticPylon && md == 0 || b == Blocks.quartz_block && md == 1) {
                b = this.worldObj.getBlock(this.xCoord, this.yCoord + 1, this.zCoord);
                if (b == ModBlocks.thaumostaticPylon && this.worldObj.getBlockMetadata(this.xCoord, this.yCoord + 1, this.zCoord) == 0
                    || b == ModBlocks.thaumostaticSpire) {
                    return;
                }
            }

            this.deactivate();
        }
    }

    public void deactivate() {
        Block b = this.worldObj.getBlock(this.xCoord, this.spireY, this.zCoord);
        if (b == ModBlocks.thaumostaticSpire) {
            MessageParticles.sendToClients(this.worldObj, this.xCoord, this.spireY, this.zCoord, 0, 0, 0, (short)0);
            b.dropBlockAsItem(this.worldObj, this.xCoord, this.spireY, this.zCoord, 0, 0);
            this.worldObj.setBlockToAir(this.xCoord, this.spireY, this.zCoord);
        }

        this.spireY = -1;
        this.active = false;
        this.markDirty();
    }

    private TileEntityThaumostaticRefuelerSpire getSpireTile() {
        TileEntity te = this.worldObj.getTileEntity(this.xCoord, this.spireY, this.zCoord);
        return te instanceof TileEntityThaumostaticRefuelerSpire ? (TileEntityThaumostaticRefuelerSpire)te : null;
    }

    @Override
    public AspectList getAspects() {
        return this.aspects;
    }

    @Override
    public AspectList getAspectsBase() {
        return maxAspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return drawAspects.contains(tag);
    }

    @Override
    public int addToContainer(Aspect tag, int amount) {
        if (amount == 1 && this.doesContainerAccept(tag)) {
            if (this.aspects.getAmount(tag) < MAX_STORED_ASPECT) {
                this.aspects.add(tag, amount);
                this.markDirty();
                return 0;
            } else {
                return amount;
            }
        } else {
            return amount;
        }
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        if (this.active && tag != Aspect.WEATHER) {
            return false;
        } else if (this.aspects.getAmount(tag) >= amount) {
            this.aspects.remove(tag, amount);
            this.markDirty();
            return true;
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
        return false;
    }

    @Override
    public boolean doesContainerContain(AspectList ot) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return this.aspects.getAmount(tag);
    }

    @Override
    public boolean isConnectable(ForgeDirection face) {
        return face != ForgeDirection.UP && face != ForgeDirection.DOWN;
    }

    @Override
    public boolean canInputFrom(ForgeDirection face) {
        return face != ForgeDirection.UP && face != ForgeDirection.DOWN;
    }

    @Override
    public boolean canOutputTo(ForgeDirection face) {
        return face != ForgeDirection.UP && face != ForgeDirection.DOWN;
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {
    }

    @Override
    public Aspect getSuctionType(ForgeDirection face) {
        return this.dirSuction.get(face);
    }

    @Override
    public int getSuctionAmount(ForgeDirection face) {
        return this.active && this.aspects.getAmount(this.getSuctionType(face)) < MAX_STORED_ASPECT ? 128 : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, ForgeDirection face) {
        return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, ForgeDirection face) {
        return this.canInputFrom(face) ? amount - this.addToContainer(aspect, amount) : 0;
    }

    @Override
    public Aspect getEssentiaType(ForgeDirection face) {
        if (this.active) {
            return this.aspects.getAmount(Aspect.WEATHER) > 0 ? Aspect.WEATHER : null;
        } else {
            return this.aspects.size() > 0 ? this.aspects.getAspects()[this.worldObj.rand.nextInt(this.aspects.getAspects().length)] : null;
        }
    }

    @Override
    public int getEssentiaAmount(ForgeDirection face) {
        return this.active ? this.aspects.getAmount(Aspect.WEATHER) : this.aspects.visSize();
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return true;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.aspects.readFromNBT(nbttagcompound);
        this.active = nbttagcompound.getBoolean("active");
        this.spireY = nbttagcompound.getInteger("spireY");
        this.pylonCount = nbttagcompound.getInteger("pylonCount");
        this.timeRangeUp = nbttagcompound.getInteger("timeRangeUp");
        this.timeRateUp = nbttagcompound.getInteger("timeRateUp");
        this.nextDistribution = nbttagcompound.getInteger("nextDistribution");
        String tag = nbttagcompound.getString("suctionN");
        if (!tag.isEmpty()) {
            this.dirSuction.put(ForgeDirection.NORTH, Aspect.getAspect(tag));
        }

        tag = nbttagcompound.getString("suctionS");
        if (!tag.isEmpty()) {
            this.dirSuction.put(ForgeDirection.SOUTH, Aspect.getAspect(tag));
        }

        tag = nbttagcompound.getString("suctionW");
        if (!tag.isEmpty()) {
            this.dirSuction.put(ForgeDirection.WEST, Aspect.getAspect(tag));
        }

        tag = nbttagcompound.getString("suctionE");
        if (!tag.isEmpty()) {
            this.dirSuction.put(ForgeDirection.EAST, Aspect.getAspect(tag));
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        this.aspects.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean("active", this.active);
        nbttagcompound.setInteger("spireY", this.spireY);
        nbttagcompound.setInteger("pylonCount", this.pylonCount);
        nbttagcompound.setInteger("timeRangeUp", this.timeRangeUp);
        nbttagcompound.setInteger("timeRateUp", this.timeRateUp);
        nbttagcompound.setInteger("nextDistribution", this.nextDistribution);
        nbttagcompound.setString("suctionN", this.dirSuction.get(ForgeDirection.NORTH).getTag());
        nbttagcompound.setString("suctionS", this.dirSuction.get(ForgeDirection.SOUTH).getTag());
        nbttagcompound.setString("suctionW", this.dirSuction.get(ForgeDirection.WEST).getTag());
        nbttagcompound.setString("suctionE", this.dirSuction.get(ForgeDirection.EAST).getTag());
    }
}
