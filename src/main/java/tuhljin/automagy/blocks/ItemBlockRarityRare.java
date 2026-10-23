package tuhljin.automagy.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockRarityRare extends ItemBlock {
    public ItemBlockRarityRare(Block block) {
        super(block);
    }

    @Override
    public EnumRarity getRarity(ItemStack p_77613_1_) {
        return EnumRarity.rare;
    }
}
