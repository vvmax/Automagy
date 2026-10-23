package tuhljin.automagy.blocks;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public class ItemBlockEntitySignal extends ItemBlock {
    public ItemBlockEntitySignal(Block p_i45328_1_) {
        super(p_i45328_1_);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        list.add(StatCollector.translateToLocal("Automagy.tip.blockEntitySignal.desc"));
    }
}
