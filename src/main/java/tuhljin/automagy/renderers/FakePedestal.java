package tuhljin.automagy.renderers;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.common.tiles.TilePedestal;

public class FakePedestal extends TilePedestal {
    private World worldOverride;
    ItemStack floatingDisplayItem;

    public FakePedestal(World world, ItemStack stack) {
        this.worldOverride = world;
        this.floatingDisplayItem = stack;
    }

    @Override
    public ItemStack getStackInSlot(int par1) {
        return this.floatingDisplayItem;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int par1) {
        return this.floatingDisplayItem;
    }

    public World getWorld() {
        return this.worldOverride != null ? this.worldOverride : super.getWorld();
    }
}
