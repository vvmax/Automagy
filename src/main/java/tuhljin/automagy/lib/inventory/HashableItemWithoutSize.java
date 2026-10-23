package tuhljin.automagy.lib.inventory;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class HashableItemWithoutSize {
    public final int damage;
    public final NBTTagCompound tagCompound;
    private final int itemID;

    public HashableItemWithoutSize(ItemStack stack) {
        this.itemID = Item.getIdFromItem(stack.getItem());
        this.damage = stack.getMetadata();
        this.tagCompound = stack.getTagCompound();
    }

    public HashableItemWithoutSize(Item item, int damage, NBTTagCompound tagCompound) {
        this.itemID = Item.getIdFromItem(item);
        this.damage = damage;
        this.tagCompound = tagCompound;
    }

    private HashableItemWithoutSize(int itemID, int damage, NBTTagCompound tagCompound) {
        this.itemID = itemID;
        this.damage = damage;
        this.tagCompound = tagCompound;
    }

    public HashableItemWithoutSize(HashableItemWithoutSize item, boolean ignoreMetadata, boolean ignoreNBT) {
        this.itemID = item.itemID;
        this.damage = ignoreMetadata ? 0 : item.damage;
        this.tagCompound = ignoreNBT ? null : item.tagCompound;
    }

    public ItemStack getItemStack(int size) {
        ItemStack stack = new ItemStack(Item.getItemById(this.itemID), size, this.damage);
        if (this.tagCompound != null) {
            stack.setTagCompound(this.tagCompound);
        }

        return stack;
    }

    public Item getItem() {
        return Item.getItemById(this.itemID);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.damage;
        result = 31 * result + this.itemID;
        return 31 * result + (this.tagCompound == null ? 0 : this.tagCompound.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof HashableItemWithoutSize) {
            HashableItemWithoutSize other = (HashableItemWithoutSize)obj;
            if (this.damage != other.damage) {
                return false;
            } else if (this.itemID != other.itemID) {
                return false;
            } else {
                if (this.tagCompound == null) {
                    if (other.tagCompound != null) {
                        return false;
                    }
                } else if (!this.tagCompound.equals(other.tagCompound)) {
                    return false;
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public boolean isSameItem(ItemStack stack) {
        return this.isSameItem(stack, false, false);
    }

    public boolean isSameItem(ItemStack stack, boolean ignoreMetadata, boolean ignoreNBT) {
        if (stack == null || this.itemID != Item.getIdFromItem(stack.getItem())) {
            return false;
        } else if (!ignoreMetadata && this.damage != stack.getMetadata()) {
            return false;
        } else {
            if (!ignoreNBT) {
                NBTTagCompound checkTagCompound = stack.getTagCompound();
                if (this.tagCompound == null) {
                    if (checkTagCompound != null) {
                        return false;
                    }
                } else if (!this.tagCompound.equals(checkTagCompound)) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean isSameItem(HashableItemWithoutSize item, boolean ignoreMetadata, boolean ignoreNBT) {
        if (item == null || this.itemID != item.itemID) {
            return false;
        } else if (!ignoreMetadata && this.damage != item.damage) {
            return false;
        } else {
            if (!ignoreNBT) {
                NBTTagCompound checkTagCompound = item.tagCompound;
                if (this.tagCompound == null) {
                    if (checkTagCompound != null) {
                        return false;
                    }
                } else if (!this.tagCompound.equals(checkTagCompound)) {
                    return false;
                }
            }

            return true;
        }
    }

    public static HashableItemWithoutSize getWithWildcardDamage(HashableItemWithoutSize item) {
        return new HashableItemWithoutSize(item.itemID, 32767, item.tagCompound);
    }
}
