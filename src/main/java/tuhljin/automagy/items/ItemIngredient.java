package tuhljin.automagy.items;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

public class ItemIngredient extends ModSubtypedItem {
    public ItemIngredient(String name) {
        super(name, 1);
        this.setTextureName("Automagy:confluxDisc");
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.uncommon;
    }
}
