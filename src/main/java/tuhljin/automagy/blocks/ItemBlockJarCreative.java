package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

public class ItemBlockJarCreative extends ModSubtypedItemBlock {
    public ItemBlockJarCreative(Block block) {
        super(block);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.epic;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("Automagy.tip.creativeModeOnly"));
    }
}
