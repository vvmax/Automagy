package tuhljin.automagy.lib;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import tuhljin.automagy.blocks.ModBlocks;

public class CreativeTabAutomagy extends CreativeTabs {
    public CreativeTabAutomagy(int par1, String par2Str) {
        super(par1, par2Str);
    }

    @Override
    public Item getTabIconItem() {
        return Item.getItemFromBlock(ModBlocks.redcrystal);
    }
}
