package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityJarXP;

public class ItemBlockJarXP extends ItemBlock {
    public ItemBlockJarXP(Block block) {
        super(block);
        this.setMaxStackSize(1);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.uncommon;
    }

    @Override
    public boolean placeBlockAt(
        ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata
    ) {
        boolean placed = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
        if (placed && stack.hasTagCompound()) {
            int xp = stack.getTagCompound().getInteger("xp");
            if (xp > 0) {
                TileEntityJarXP te = (TileEntityJarXP)world.getTileEntity(x, y, z);
                te.setXP(xp);
            }
        }

        return placed;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean p_77624_4_) {
        if (stack.hasTagCompound()) {
            int xp = stack.getTagCompound().getInteger("xp");
            if (xp > 0) {
                String s = getLevelXPInfo(xp, "Automagy.tip.jarXP");
                list.add(s);
            }
        }
    }

    public static String getLevelXPInfo(int xp, String translateKey) {
        if (xp == 0) {
            return StatCollector.translateToLocalFormatted(translateKey, "0", 0);
        } else {
            int level = TjUtil.getLevelForExperience(xp);
            int xpBaseForLevel = TjUtil.getExperienceForLevel(level);
            String sLevel;
            if (xp == xpBaseForLevel) {
                sLevel = String.valueOf(level);
            } else {
                int nextLevelXP = TjUtil.getExperienceForLevel(level + 1);
                float percentDiff = (float)(xp - xpBaseForLevel) / (nextLevelXP - xpBaseForLevel);
                DecimalFormat df = new DecimalFormat("#.00");
                sLevel = String.valueOf(level + df.format(percentDiff));
            }

            return StatCollector.translateToLocalFormatted(translateKey, sLevel, xp);
        }
    }
}
