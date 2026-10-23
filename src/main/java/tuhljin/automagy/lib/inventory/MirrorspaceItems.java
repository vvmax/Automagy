package tuhljin.automagy.lib.inventory;

import java.util.ArrayList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import tuhljin.automagy.tiles.TileEntityMirrorWithEye;

public class MirrorspaceItems {
    public ArrayList<ItemStack> stacks = new ArrayList<>();
    protected TileEntity parent;
    protected final String tagName;

    public MirrorspaceItems(TileEntity parent) {
        this(parent, "ItemsMirror");
    }

    public MirrorspaceItems(TileEntity parent, String tagName) {
        this.tagName = tagName;
        this.parent = parent;
    }

    public void add(ItemStack stack) {
        this.stacks.add(stack);
    }

    public int size() {
        return this.stacks.size();
    }

    public boolean ejectRandom() {
        if (this.stacks.size() > 0) {
            int i = this.parent.getWorld().rand.nextInt(this.stacks.size());
            return this.eject(i);
        } else {
            return false;
        }
    }

    public boolean eject(int i) {
        ItemStack stack = this.stacks.get(i);
        if (stack != null) {
            stack = stack.copy();
            stack.stackSize = 1;
            if (this.spawnItem(stack)) {
                this.stacks.get(i).stackSize--;
                TileEntityMirrorWithEye.addParticles(this.parent.getWorld(), this.parent.xCoord, this.parent.yCoord, this.parent.zCoord);
                if (this.stacks.get(i).stackSize <= 0) {
                    this.stacks.remove(i);
                }

                return true;
            }
        }

        return false;
    }

    protected boolean spawnItem(ItemStack stack) {
        try {
            ForgeDirection face = ForgeDirection.getOrientation(this.parent.getBlockMetadata());
            EntityItem ie2 = new EntityItem(
                this.parent.getWorld(),
                this.parent.xCoord + 0.5 - face.offsetX * 0.3,
                this.parent.yCoord + 0.5 - face.offsetY * 0.3,
                this.parent.zCoord + 0.5 - face.offsetZ * 0.3,
                stack
            );
            ie2.motionX = face.offsetX * 0.15F;
            ie2.motionY = face.offsetY * 0.15F;
            ie2.motionZ = face.offsetZ * 0.15F;
            ie2.timeUntilPortal = 20;
            this.parent.getWorld().spawnEntityInWorld(ie2);
            return true;
        } catch (Exception var4) {
            return false;
        }
    }

    public void readFromNBT(NBTTagCompound nbtCompound) {
        NBTTagList nbttaglist = nbtCompound.getTagList(this.tagName, 10);
        this.stacks = new ArrayList<>();

        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            this.stacks.add(ItemStack.loadItemStackFromNBT(nbttagcompound1));
        }
    }

    public void writeToNBT(NBTTagCompound nbtCompound) {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.stacks.size(); i++) {
            if (this.stacks.get(i) != null && this.stacks.get(i).stackSize > 0) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                this.stacks.get(i).writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        nbtCompound.setTag(this.tagName, nbttaglist);
    }
}
