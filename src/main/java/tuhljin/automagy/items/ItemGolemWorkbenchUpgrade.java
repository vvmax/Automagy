package tuhljin.automagy.items;

import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

public class ItemGolemWorkbenchUpgrade extends ModSubtypedItem {
    private IIcon[] icons = new IIcon[3];

    public ItemGolemWorkbenchUpgrade(String name) {
        super(name, 6);
    }

    @Override
    public void registerIcons(IIconRegister ir) {
        this.itemIcon = ir.registerIcon("Automagy:workbench-up-air");
        this.icons[0] = ir.registerIcon("Automagy:workbench-up-fire");
        this.icons[1] = ir.registerIcon("Automagy:workbench-up-earth");
        this.icons[2] = ir.registerIcon("Automagy:workbench-up-entropy");
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tabs, List list) {
        for (int damage = 0; damage < this.numSubtypes; damage++) {
            if (damage != 4 && damage != 2) {
                list.add(new ItemStack(this, 1, damage));
            }
        }
    }

    @Override
    public IIcon getIconFromDamage(int par1) {
        switch (par1) {
            case 0:
            case 2:
            case 4:
            default:
                return this.itemIcon;
            case 1:
                return this.icons[0];
            case 3:
                return this.icons[1];
            case 5:
                return this.icons[2];
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        int d = stack.getMetadata();
        list.add(StatCollector.translateToLocalFormatted("Automagy.tip.golemWorkbenchUpgrade." + d));
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.uncommon;
    }
}
