package tuhljin.automagy.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.lib.IAutomagyLocationLink;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.renderers.BlockScribePointerRenderer;

public class TileEntityScribePointer extends ModTileEntity implements IContainsEyes {
    public static int RANGE = 10;
    public static int MAX_EYES = 5;
    public InventoryObjectEyes eyesContainingInventory;
    protected int eyeCount = 0;
    public int currentDisplayIndex = 0;
    public int ticks;
    public int displayChangedTick = -1;
    public ItemStack displayStack;
    public int rangeBoosters = -1;

    public TileEntityScribePointer() {
        this.eyesContainingInventory = new InventoryObjectEyes(this, "Scribe Pointer", MAX_EYES, true);
    }

    public TileEntityScribePointer(World world) {
        this();
        this.ticks = world.rand.nextInt(500);
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (this.worldObj.isRemote) {
            this.ticks++;
            if (this.eyeCount > 0 && this.displayStack == null && this.ticks % 8 == 0) {
                float x = this.xCoord + 0.5F;
                float y = this.yCoord;
                float z = this.zCoord + 0.5F;
                switch (this.getBlockMetadata()) {
                    case 0:
                        x += 0.35F;
                        z += -0.35F;
                        break;
                    case 1:
                        x += 0.35F;
                        z += 0.35F;
                        break;
                    case 2:
                        x += -0.35F;
                        z += 0.35F;
                        break;
                    case 3:
                        x += -0.35F;
                        z += -0.35F;
                }

                int color = 5;
                Thaumcraft.proxy
                    .sparkle(
                        x + this.worldObj.rand.nextFloat() * 0.2F - this.worldObj.rand.nextFloat() * 0.2F,
                        y + 0.1F,
                        z + this.worldObj.rand.nextFloat() * 0.2F - this.worldObj.rand.nextFloat() * 0.2F,
                        1.0F,
                        color,
                        -0.1F
                    );
            }
        }
    }

    @Override
    public void onEyesInventoryChanged(int slot, ItemStack prevStack) {
    }

    @Override
    public int getDimensionID() {
        return this.worldObj.provider.dimensionId;
    }

    @Override
    public BlockCoord getPosition() {
        return new BlockCoord(this);
    }

    @Override
    public int getMaxDistance() {
        return RANGE;
    }

    public ItemStack removeEye() {
        if (this.eyeCount > 0) {
            int slot = this.eyeCount - 1;
            ItemStack stack = this.eyesContainingInventory.getStackInSlot(slot);
            this.eyesContainingInventory.setInventorySlotContents(slot, null);
            this.eyeCount--;
            this.markDirty();
            this.rangeBoosters = -1;
            return stack;
        } else {
            return null;
        }
    }

    public boolean insertEye(ItemStack stack) {
        if (this.eyeCount < MAX_EYES) {
            this.eyesContainingInventory.setInventorySlotContents(this.eyeCount, stack.splitStack(1));
            this.eyeCount++;
            this.markDirty();
            this.rangeBoosters = -1;
            return true;
        } else {
            return false;
        }
    }

    public boolean itemIsValidEye(ItemStack stack) {
        return this.eyesContainingInventory.isItemValidForSlot(this.eyeCount, stack);
    }

    public int getEyeCount() {
        return this.eyeCount;
    }

    public TileEntityUnseenScribe.SelectedContainers getSelectedContainers() {
        if (this.eyeCount <= 0) {
            return null;
        } else {
            TileEntityUnseenScribe.SelectedContainers sc = new TileEntityUnseenScribe.SelectedContainers();

            for (int slot = 0; slot < this.eyeCount; slot++) {
                ItemStack stack = this.eyesContainingInventory.getStackInSlot(slot);
                if (stack != null && stack.getItem() instanceof IAutomagyLocationLink) {
                    WorldSpecificCoordinates coord = ((IAutomagyLocationLink)stack.getItem()).getLinkLocation(stack);
                    if (coord != null && (coord.x != this.xCoord || coord.y != this.yCoord - 1 || coord.z != this.zCoord)) {
                        sc.add(this.worldObj, coord.x, coord.y, coord.z);
                    }
                }
            }

            return sc;
        }
    }

    public int getRangeBoosters() {
        if (this.rangeBoosters == -1 && this.eyeCount > 0) {
            this.rangeBoosters = 0;

            for (int slot = 0; slot < this.eyeCount; slot++) {
                ItemStack stack = this.eyesContainingInventory.getStackInSlot(slot);
                if (stack != null && stack.getItem() instanceof IAutomagyLocationLink) {
                    WorldSpecificCoordinates coord = ((IAutomagyLocationLink)stack.getItem()).getLinkLocation(stack);
                    if (coord != null && coord.x == this.xCoord && coord.y == this.yCoord - 1 && coord.z == this.zCoord) {
                        this.rangeBoosters++;
                    }
                }
            }
        }

        return this.rangeBoosters;
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        this.eyesContainingInventory.writeCustomNBT(nbttagcompound);
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

        this.displayChangedTick = -1;
        this.rangeBoosters = -1;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        BlockScribePointerRenderer.renderPass = pass;
        return true;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 0.5, this.zCoord + 1);
    }
}
