package tuhljin.automagy.lib;

import java.util.Comparator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SortItemStack {
    public static int byName(ItemStack arg0, ItemStack arg1) {
        String n1 = arg0.getDisplayName();
        String n2 = arg1.getDisplayName();
        int r = n1.compareTo(n2);
        if (r == 0) {
            r = Integer.compare(Item.getIdFromItem(arg0.getItem()), Item.getIdFromItem(arg1.getItem()));
            if (r == 0) {
                r = Integer.compare(arg0.getMetadata(), arg1.getMetadata());
            }
        }

        return r;
    }

    public static int bySizeReverse(ItemStack arg0, ItemStack arg1) {
        int r = Integer.compare(arg0.stackSize, arg1.stackSize) * -1;
        if (r == 0) {
            r = byName(arg0, arg1);
        }

        return r;
    }

    public static class CompareByName implements Comparator<ItemStack> {
        public int compare(ItemStack arg0, ItemStack arg1) {
            return SortItemStack.byName(arg0, arg1);
        }
    }

    public static class CompareByNameReverse implements Comparator<ItemStack> {
        public int compare(ItemStack arg0, ItemStack arg1) {
            String n1 = arg0.getDisplayName();
            String n2 = arg1.getDisplayName();
            return n1.compareTo(n2) * -1;
        }
    }

    public static class CompareBySize implements Comparator<ItemStack> {
        public int compare(ItemStack arg0, ItemStack arg1) {
            return Integer.compare(arg0.stackSize, arg1.stackSize);
        }
    }

    public static class CompareBySizeReverse implements Comparator<ItemStack> {
        public int compare(ItemStack arg0, ItemStack arg1) {
            return SortItemStack.bySizeReverse(arg0, arg1);
        }
    }
}
