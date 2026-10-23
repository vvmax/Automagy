package tuhljin.automagy.lib.recipe;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import thaumcraft.api.aspects.AspectList;

@Deprecated
public class InfusionWithEnchantedComponentRecipe extends CustomInfusionRecipe {
    public InfusionWithEnchantedComponentRecipe(String research, Object output, int inst, AspectList aspects2, ItemStack input, ItemStack[] recipe) {
        super(research, output, inst, aspects2, input, recipe);
    }

    @Override
    public boolean areItemStackTagsEqualForThisRecipe(ItemStack slotItem, ItemStack recipeItem) {
        if (recipeItem == null || slotItem == null) {
            return false;
        } else if (recipeItem.stackTagCompound != null && slotItem.stackTagCompound == null) {
            return false;
        } else if (recipeItem.stackTagCompound == null) {
            return true;
        } else {
            for (String s : recipeItem.stackTagCompound.getKeySet()) {
                if (!slotItem.stackTagCompound.hasKey(s)) {
                    return false;
                }

                if (!slotItem.stackTagCompound.getTag(s).toString().equals(recipeItem.stackTagCompound.getTag(s).toString())
                    && (
                        s.equals("StoredEnchantments")
                            ? !this.areEnchantmentRequirementsMet(Items.enchanted_book.func_92110_g(slotItem), Items.enchanted_book.func_92110_g(recipeItem))
                            : !s.equals("ench") || !this.areEnchantmentRequirementsMet(slotItem.getEnchantmentTagList(), recipeItem.getEnchantmentTagList())
                    )) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean areEnchantmentRequirementsMet(NBTTagList slotEnchantments, NBTTagList recipeEnchantments) {
        for (int i = 0; i < recipeEnchantments.tagCount(); i++) {
            short idRecipe = recipeEnchantments.getCompoundTagAt(i).getShort("id");
            short lvlRecipe = recipeEnchantments.getCompoundTagAt(i).getShort("lvl");
            boolean found = false;

            for (int j = 0; j < slotEnchantments.tagCount(); j++) {
                short idSlot = slotEnchantments.getCompoundTagAt(j).getShort("id");
                if (idSlot == idRecipe) {
                    short lvlSlot = slotEnchantments.getCompoundTagAt(j).getShort("lvl");
                    if (lvlSlot >= lvlRecipe) {
                        found = true;
                    }
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }
}
