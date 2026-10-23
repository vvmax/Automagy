package tuhljin.automagy.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public abstract class ModItem extends Item {
    public ModItem(String name) {
        this.setUnlocalizedName(name);
        this.setTextureName("Automagy:" + name);
        if (ModItems.tab != null) {
            this.setCreativeTab(ModItems.tab);
        }

        GameRegistry.registerItem(this, name);
    }
}
