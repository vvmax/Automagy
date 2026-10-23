package tuhljin.automagy.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.renderer.texture.IIconRegister;
import thaumcraft.api.wands.ItemFocusBasic;

public abstract class ModItemFocus extends ItemFocusBasic {
    public ModItemFocus(String name) {
        this.setUnlocalizedName(name);
        this.setTextureName("Automagy:" + name);
        if (ModItems.tab != null) {
            this.setCreativeTab(ModItems.tab);
        }

        GameRegistry.registerItem(this, name);
    }

    @Override
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.icon = this.itemIcon;
    }
}
