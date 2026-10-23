package tuhljin.automagy.items;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class ItemBaubleEnderDisjunction extends ModSubtypedItem implements IBauble, IRunicArmor {
    public static double TELEPORT_RIFT_FEEDBACK_SIZE = 8.5;
    public static int TELEPORT_RIFT_FEEDBACK_DAMAGE = 15;
    public static int TELEPORT_RIFT_FEEDBACK_DAMAGE_LESSER = 8;
    public static float TELEPORT_RIFT_FEEDBACK_DIMINISH = 0.5F;
    public static AspectList disjunctionVisCost = new AspectList().add(Aspect.ENTROPY, 100);
    public IIcon lesser;

    public ItemBaubleEnderDisjunction(String name) {
        super(name, 2);
        this.maxStackSize = 1;
    }

    @Override
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.lesser = ir.registerIcon(this.getIconString() + "Lesser");
    }

    @Override
    public IIcon getIconFromDamage(int par1) {
        return par1 == 1 ? this.lesser : super.getIconFromDamage(par1);
    }

    public boolean canEquip(ItemStack arg0, EntityLivingBase player) {
        return true;
    }

    public boolean canUnequip(ItemStack arg0, EntityLivingBase player) {
        return true;
    }

    public BaubleType getBaubleType(ItemStack arg0) {
        return BaubleType.RING;
    }

    public void onEquipped(ItemStack arg0, EntityLivingBase arg1) {
    }

    public void onUnequipped(ItemStack arg0, EntityLivingBase arg1) {
    }

    public void onWornTick(ItemStack arg0, EntityLivingBase arg1) {
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.uncommon;
    }

    @Override
    public int getRunicCharge(ItemStack itemstack) {
        return 0;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        if (stack.getMetadata() == 1) {
            list.add(StatCollector.translateToLocalFormatted("Automagy.tip.enderDisjunction"));
        }
    }

    public int getDisjunctionDamageAndConsumeVis(ItemStack stack, EntityPlayer player) {
        if (stack.getMetadata() == 1) {
            return TELEPORT_RIFT_FEEDBACK_DAMAGE_LESSER;
        } else {
            return ThaumcraftApiHelper.consumeVisFromInventory(player, disjunctionVisCost) ? TELEPORT_RIFT_FEEDBACK_DAMAGE : 0;
        }
    }
}
