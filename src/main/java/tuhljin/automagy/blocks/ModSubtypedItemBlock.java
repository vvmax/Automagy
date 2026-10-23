package tuhljin.automagy.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ModSubtypedItemBlock extends ItemBlock {
    public ModSubtypedItemBlock(Block block) {
        super(block);
        this.setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damageValue) {
        return damageValue;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName() + "." + stack.getMetadata();
    }
}
