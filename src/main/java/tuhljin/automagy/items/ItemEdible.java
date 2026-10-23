package tuhljin.automagy.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import tuhljin.automagy.lib.TjUtil;

public class ItemEdible extends ModSubtypedItem {
    public static final int PECH_VALUE = 7;

    public ItemEdible(String name) {
        super(name, 1);
        this.setTextureName("Automagy:tradeEssence");
    }

    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
        if (!player.capabilities.isCreativeMode) {
            stack.stackSize--;
        }

        if (!world.isRemote) {
            player.addPotionEffect(new PotionEffect(Potion.poison.id, 100, 0));
            player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 150, 1));
        }

        if (!player.capabilities.isCreativeMode) {
            if (stack.stackSize <= 0) {
                return new ItemStack(Items.glass_bottle);
            }

            boolean added = player.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));
            if (!added && !world.isRemote) {
                TjUtil.dropItemIntoWorld(new ItemStack(Items.glass_bottle), world, Math.round(player.posX), player.posY, player.posZ);
            }
        }

        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.drink;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
        return par1ItemStack;
    }
}
