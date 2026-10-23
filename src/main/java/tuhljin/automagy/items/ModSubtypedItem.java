package tuhljin.automagy.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class ModSubtypedItem extends ModItem {
    public final int numSubtypes;

    public ModSubtypedItem(String name, int numSubtypes) {
        super(name);
        this.numSubtypes = numSubtypes;
        this.setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName() + "." + stack.getMetadata();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs tabs, List list) {
        for (int damage = 0; damage < this.numSubtypes; damage++) {
            list.add(new ItemStack(this, 1, damage));
        }
    }
}
