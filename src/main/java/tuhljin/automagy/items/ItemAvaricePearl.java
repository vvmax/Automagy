package tuhljin.automagy.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import tuhljin.automagy.entities.EntityAvaricePearl;

public class ItemAvaricePearl extends ModSubtypedItem {
    public static AspectList visCost = new AspectList().add(Aspect.ORDER, 10);
    public static final int DUMMY = 15;
    @SideOnly(Side.CLIENT)
    public IIcon iconMisersPearl;

    public ItemAvaricePearl(String name) {
        super(name, 2);
        this.maxStackSize = 16;
    }

    @Override
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.iconMisersPearl = ir.registerIcon("Automagy:misersPearl");
    }

    @Override
    public IIcon getIconFromDamage(int par1) {
        return par1 == 1 ? this.iconMisersPearl : super.getIconFromDamage(par1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        int metadata = stack.getMetadata();
        if (metadata == 15) {
            stack.stackSize = 0;
            return stack;
        } else {
            boolean creative = player.capabilities.isCreativeMode;
            boolean canThrow = creative || metadata == 0;
            if (!canThrow) {
                if (ThaumcraftApiHelper.consumeVisFromInventory(player, visCost)) {
                    canThrow = true;
                } else if (world.isRemote) {
                    player.playSound("random.break", 0.6F, 0.2F + itemRand.nextFloat() * 0.2F);
                }
            }

            if (canThrow) {
                if (!creative) {
                    stack.stackSize--;
                }

                world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                if (!world.isRemote) {
                    int currentItemSlot = player.inventory.currentItem;
                    world.spawnEntityInWorld(new EntityAvaricePearl(world, metadata, player, currentItemSlot, creative, player.posX, player.posY, player.posZ));
                }
            }

            return stack;
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return stack.getMetadata() == 1 ? EnumRarity.uncommon : EnumRarity.common;
    }
}
