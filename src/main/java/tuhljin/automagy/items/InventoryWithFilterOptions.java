package tuhljin.automagy.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class InventoryWithFilterOptions extends InventoryForItemLargeStacks {
    public boolean useItemCount;
    public boolean ignoreNBT;
    public boolean ignoreMetadata;
    public String nameFilter;
    private final int limit;

    public InventoryWithFilterOptions(ItemStack containerStack, String inventoryName, int numSlots, int limit) {
        super(containerStack, inventoryName, numSlots);
        if (this.nameFilter == null) {
            this.nameFilter = "";
        }

        this.limit = limit;
    }

    @Override
    public int getInventoryStackLimit() {
        return this.limit;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.useItemCount = false;
        this.ignoreNBT = false;
        this.ignoreMetadata = false;
        this.nameFilter = "";
        NBTBase base = nbttagcompound.getTag("FilterOptions");
        if (base instanceof NBTTagCompound) {
            NBTTagCompound nbtsub = (NBTTagCompound)base;
            if (nbtsub != null && !nbtsub.hasNoTags()) {
                this.useItemCount = nbtsub.getBoolean("useItemCount");
                this.ignoreNBT = nbtsub.getBoolean("ignoreNBT");
                this.ignoreMetadata = nbtsub.getBoolean("ignoreMetadata");
                this.nameFilter = nbtsub.getString("nameFilter");
            }
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        NBTTagCompound nbtsub = new NBTTagCompound();
        if (this.useItemCount) {
            nbtsub.setBoolean("useItemCount", true);
        }

        if (this.ignoreNBT) {
            nbtsub.setBoolean("ignoreNBT", true);
        }

        if (this.ignoreMetadata) {
            nbtsub.setBoolean("ignoreMetadata", true);
        }

        this.nameFilter = this.nameFilter.trim();
        if (!this.nameFilter.isEmpty()) {
            nbtsub.setString("nameFilter", this.nameFilter);
        }

        if (!nbtsub.hasNoTags()) {
            nbttagcompound.setTag("FilterOptions", nbtsub);
        } else if (nbttagcompound.hasKey("FilterOptions")) {
            nbttagcompound.removeTag("FilterOptions");
        }
    }
}
