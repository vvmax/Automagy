package tuhljin.automagy.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.lib.WarpEvents;
import thaumcraft.common.lib.potions.PotionBlurredVision;
import tuhljin.automagy.network.MessageSound;

public class ItemFoodstuff extends ItemFood {
    IIcon iconVishroomStew;

    public ItemFoodstuff(String name) {
        super(6, false);
        this.setUnlocalizedName(name);
        this.setTextureName("Automagy:vishroomStew");
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
        this.setAlwaysEdible();
        if (ModItems.tab != null) {
            this.setCreativeTab(ModItems.tab);
        }

        GameRegistry.registerItem(this, name);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName() + "." + stack.getMetadata();
    }

    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
        super.onItemUseFinish(stack, world, player);
        return new ItemStack(Items.bowl);
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && player != null) {
            if (!Config.wuss) {
                Thaumcraft.addWarpToPlayer(player, world.rand.nextInt(10) + 8, true);
                MessageSound.sendToClient(player, (short)0);
                WarpEvents.checkWarpEvent(player);
            }

            player.addPotionEffect(new PotionEffect(Potion.weakness.id, 800, 1));
            player.addPotionEffect(new PotionEffect(PotionBlurredVision.instance.id, 800, 1));
            player.addPotionEffect(new PotionEffect(Potion.confusion.id, 400, 1));
            player.addPotionEffect(new PotionEffect(Potion.blindness.id, 160, 1));
            player.addPotionEffect(new PotionEffect(Potion.nightVision.id, 60, 1));
            player.addPotionEffect(new PotionEffect(Potion.poison.id, 60, 2));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(StatCollector.translateToLocal("Automagy.tip.foodstuff.0"));
    }
}
