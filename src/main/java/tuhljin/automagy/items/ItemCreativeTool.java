package tuhljin.automagy.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import thaumcraft.common.entities.golems.EntityGolemBase;
import tuhljin.automagy.lib.AutomagyConfig;

public class ItemCreativeTool extends ModSubtypedItem {
    public ItemCreativeTool(String name) {
        super(name, 1);
        this.setTextureName("Automagy:thinkingCap");
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.epic;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("Automagy.tip.creativeModeOnly"));
        if (AutomagyConfig.usableThinkingCap) {
            list.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("Automagy.tip.creativeTool.0"));
        }
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity) {
        if (entity instanceof EntityGolemBase) {
            EntityGolemBase golem = (EntityGolemBase)entity;
            if (!player.worldObj.isRemote) {
                if (!golem.advanced) {
                    golem.advanced = true;
                    stack.stackSize--;
                    entity.playSound("thaumcraft:upgrade", 1.0F, 1.0F);
                }
            } else {
                player.swingItem();
            }
        }

        return true;
    }
}
