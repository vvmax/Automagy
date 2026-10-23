package tuhljin.automagy.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

public class ItemBlockThaumostaticPylon extends ModSubtypedItemBlock {
    public ItemBlockThaumostaticPylon(Block block) {
        super(block);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return stack.getMetadata() == 1 ? EnumRarity.rare : super.getRarity(stack);
    }
}
