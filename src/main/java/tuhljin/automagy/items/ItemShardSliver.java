package tuhljin.automagy.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import thaumcraft.common.blocks.BlockCustomOreItem;

public class ItemShardSliver extends ModSubtypedItem {
    public ItemShardSliver(String name) {
        super(name, 6);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getColorFromItemStack(ItemStack stack, int par2) {
        return BlockCustomOreItem.colors[stack.getMetadata() + 1];
    }
}
