package tuhljin.automagy.tiles;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.common.tiles.TileMirror;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.network.MessageParticles;

public abstract class TileEntityMirrorWithEye extends TileWithEyes implements IInventory {
    protected static TileMirror staticMirror = new TileMirror();

    public TileEntityMirrorWithEye(String eyeInvName) {
        super(eyeInvName, 1, 0);
    }

    protected TileMirror getTargetMirror(int i) {
        WorldSpecificCoordinates coord = this.eyesContainingInventory.getCoordinates(i);
        if (coord != null) {
            World targetWorld = DimensionManager.getWorld(coord.dim);
            if (targetWorld == null) {
                return null;
            }

            TileEntity te = targetWorld.getTileEntity(coord.x, coord.y, coord.z);
            if (te == null || !(te instanceof TileMirror)) {
                this.handleInvalidTarget(i);
                return null;
            }

            TileMirror tm = (TileMirror)te;
            if (tm.linked) {
                return tm;
            }

            this.handleInvalidTarget(i);
        }

        return null;
    }

    public abstract TileMirror getDestinationMirror();

    protected abstract void handleInvalidTarget(int var1);

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int p_70301_1_) {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) {
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        TileMirror target = this.getDestinationMirror();
        if (target != null) {
            target.addStack(stack.copy());
            addParticles(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
        } else {
            this.spawnItem(stack.copy());
        }
    }

    @Override
    public String getInventoryName() {
        return "Automagy Mirror";
    }

    public boolean isCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return false;
    }

    public void openChest() {
    }

    public void closeChest() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return this.getDestinationMirror() != null;
    }

    public boolean spawnItem(ItemStack stack) {
        try {
            ForgeDirection face = ForgeDirection.getOrientation(this.getBlockMetadata());
            EntityItem ie2 = new EntityItem(
                this.worldObj, this.xCoord + 0.5 - face.offsetX * 0.3, this.yCoord + 0.5 - face.offsetY * 0.3, this.zCoord + 0.5 - face.offsetZ * 0.3, stack
            );
            ie2.motionX = face.offsetX * 0.15F;
            ie2.motionY = face.offsetY * 0.15F;
            ie2.motionZ = face.offsetZ * 0.15F;
            ie2.timeUntilPortal = 20;
            this.worldObj.spawnEntityInWorld(ie2);
            return true;
        } catch (Exception var4) {
            return false;
        }
    }

    public static void addParticles(World world, int x, int y, int z) {
        if (world.isRemote) {
            staticMirror.setWorldObj(world);
            staticMirror.xCoord = x;
            staticMirror.yCoord = y;
            staticMirror.zCoord = z;
            staticMirror.blockMetadata = world.getBlockMetadata(x, y, z);
            staticMirror.receiveClientEvent(1, 0);
        } else {
            MessageParticles.sendToClients(world, x, y, z, 0, 0, 0, (short)12);
        }
    }

    public static boolean spawnItem(ItemStack stack, World world, int x, int y, int z) {
        staticMirror.setWorldObj(world);
        staticMirror.xCoord = x;
        staticMirror.yCoord = y;
        staticMirror.zCoord = z;
        staticMirror.blockMetadata = world.getBlockMetadata(x, y, z);
        return staticMirror.spawnItem(stack);
    }
}
