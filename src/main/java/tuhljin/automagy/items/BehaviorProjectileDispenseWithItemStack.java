package tuhljin.automagy.items;

import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;

public abstract class BehaviorProjectileDispenseWithItemStack extends BehaviorProjectileDispense {
    protected ItemStack theItemStack;

    @Override
    public ItemStack dispenseStack(IBlockSource par1IBlockSource, ItemStack stack) {
        this.theItemStack = stack;
        return super.dispenseStack(par1IBlockSource, stack);
    }
}
