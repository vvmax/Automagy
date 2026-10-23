package tuhljin.automagy.lib.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import thaumcraft.common.config.ConfigItems;

public class AdvGolemRecipe extends ShapelessOreRecipe {
    public AdvGolemRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting craftGrid) {
        ItemStack golem = null;

        for (int slot = 0; slot < craftGrid.getSizeInventory(); slot++) {
            ItemStack stack = craftGrid.getStackInSlot(slot);
            if (stack != null && stack.getItem() == ConfigItems.itemGolemPlacer) {
                golem = stack.copy();
                break;
            }
        }

        if (golem != null) {
            golem.setTagInfo("advanced", new NBTTagByte((byte)1));
            return golem;
        } else {
            return super.getCraftingResult(craftGrid);
        }
    }
}
