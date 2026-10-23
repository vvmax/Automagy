package tuhljin.automagy.tiles;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.tiles.TileMirror;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.MirrorspaceItems;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

public class TileEntityMirrorInput extends TileEntityMirrorWithEye implements IWandable, IMirrorVariant {
    public static int CHECK_FREQUENCY_START = 40;
    public static int CHECK_FREQUENCY_INC = 20;
    public static int CHECK_FREQUENCY_MAX = 600;
    public boolean linked = false;
    public MirrorspaceItems outputSpace;
    private int ticksSinceCheck = 0;
    private int checkFrequency = CHECK_FREQUENCY_START;
    private int ticksSinceStabilize = 0;

    public TileEntityMirrorInput() {
        super("Melding Mirror");
        this.outputSpace = new MirrorspaceItems(this);
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote) {
            if (this.eyeCount > 0) {
                this.ticksSinceCheck++;
                if (this.ticksSinceCheck >= this.checkFrequency) {
                    this.ticksSinceCheck = 0;
                    boolean wasLinked = this.linked;
                    if (this.getDestinationMirror() != null) {
                        if (!wasLinked) {
                            this.linked = true;
                            this.markDirty();
                            this.checkFrequency = CHECK_FREQUENCY_START;
                        } else {
                            this.checkFrequency = Math.min(this.checkFrequency + CHECK_FREQUENCY_INC, CHECK_FREQUENCY_MAX);
                        }
                    } else if (!wasLinked || this.linked) {
                        this.checkFrequency = Math.min(this.checkFrequency + CHECK_FREQUENCY_INC, CHECK_FREQUENCY_MAX);
                    }
                }
            }

            if (this.outputSpace.ejectRandom()) {
                this.markDirty(false);
            }
        }
    }

    public boolean transport(EntityItem entity) {
        if (!this.linked) {
            return false;
        } else {
            ItemStack stack = entity.getEntityItem();
            TileMirror target = this.getDestinationMirror();
            if (target != null) {
                target.addStack(stack);
                entity.setDead();
                target.markDirty();
                addParticles(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public TileMirror getDestinationMirror() {
        return this.getTargetMirror(0);
    }

    @Override
    public WorldSpecificCoordinates getDestinationMirrorCoordinates() {
        return this.eyesContainingInventory.getCoordinates(0);
    }

    @Override
    public boolean isLinked() {
        return this.linked;
    }

    @Override
    public int addStackToOutput(ItemStack stack) {
        this.outputSpace.add(stack);
        this.markDirty(false);
        return 0;
    }

    @Override
    public void markMirrorDirty() {
        this.markDirty(false);
    }

    @Override
    protected void handleInvalidTarget(int slot) {
        if (this.linked) {
            this.linked = false;
            this.markDirty();
            this.ticksSinceCheck = 0;
            this.checkFrequency = CHECK_FREQUENCY_START;
        }
    }

    @Override
    public boolean insertEye(ItemStack stack) {
        if (this.isEyeLinkedToPairedMirror(stack) && super.insertEye(stack)) {
            if (this.getDestinationMirror() != null && !this.linked) {
                this.linked = true;
                this.markDirty();
            }

            this.ticksSinceCheck = 0;
            this.checkFrequency = CHECK_FREQUENCY_START;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ItemStack removeEye() {
        ItemStack stack = super.removeEye();
        if (this.eyeCount < 1) {
            this.handleInvalidTarget(0);
        }

        return stack;
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setBoolean("linked", this.linked);
        this.outputSpace.writeToNBT(nbttagcompound);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.linked = nbttagcompound.getBoolean("linked");
        this.outputSpace.readFromNBT(nbttagcompound);
    }

    @Override
    public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
        if (!world.isRemote && this.eyeCount > 0 && player.isSneaking()) {
            ItemStack stack = this.removeEye();
            if (stack != null) {
                ForgeDirection dir = ForgeDirection.getOrientation(side);
                TjUtil.dropItemIntoWorldSimple(stack, world, x + 0.5 + dir.offsetX * 0.1, y + 0.5 + dir.offsetY * 0.1, z + 0.5 + dir.offsetZ * 0.1);
                world.playSoundAtEntity(player, "thaumcraft:zap", 0.5F, 1.0F);
                return 0;
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
}
