package tuhljin.automagy.lib.recipe;

import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;

public abstract class CustomInfusionRecipe extends InfusionRecipe {
    public CustomInfusionRecipe(String research, Object output, int inst, AspectList aspects2, ItemStack input, ItemStack[] recipe) {
        super(research, output, inst, aspects2, input, recipe);
    }

    @Override
    public boolean matches(ArrayList<ItemStack> input, ItemStack central, World world, EntityPlayer player) {
        if (this.getRecipeInput() == null) {
            return false;
        } else if (this.research.length() > 0 && !ThaumcraftApiHelper.isResearchComplete(player.getCommandSenderName(), this.research)) {
            return false;
        } else {
            ItemStack i2 = central.copy();
            if (this.getRecipeInput().getMetadata() == 32767) {
                i2.setMetadata(32767);
            }

            if (!this.areItemStacksEqualEnough(i2, this.getRecipeInput(), true)) {
                return false;
            } else {
                ArrayList<ItemStack> ii = new ArrayList<>();

                for (ItemStack is : input) {
                    ii.add(is.copy());
                }

                for (ItemStack comp : this.getComponents()) {
                    boolean b = false;

                    for (int a = 0; a < ii.size(); a++) {
                        i2 = ii.get(a).copy();
                        if (comp.getMetadata() == 32767) {
                            i2.setMetadata(32767);
                        }

                        if (this.areItemStacksEqualEnough(i2, comp, true)) {
                            ii.remove(a);
                            b = true;
                            break;
                        }
                    }

                    if (!b) {
                        return false;
                    }
                }

                return ii.size() == 0;
            }
        }
    }

    public boolean areItemStacksEqualEnough(ItemStack stack0, ItemStack stack1, boolean fuzzy) {
        if (stack0 == null && stack1 != null) {
            return false;
        } else if (stack0 != null && stack1 == null) {
            return false;
        } else if (stack0 == null && stack1 == null) {
            return true;
        } else {
            boolean t1 = this.areItemStackTagsEqualForThisRecipe(stack0, stack1);
            if (!t1) {
                return false;
            } else {
                if (fuzzy) {
                    Integer od = OreDictionary.getOreID(stack0);
                    if (od != -1) {
                        ItemStack[] ores = OreDictionary.getOres(od).toArray(new ItemStack[0]);
                        if (ThaumcraftApiHelper.containsMatch(false, new ItemStack[]{stack1}, ores)) {
                            return true;
                        }
                    }
                }

                boolean damage = stack0.getMetadata() == stack1.getMetadata() || stack1.getMetadata() == 32767;
                return stack0.getItem() != stack1.getItem() ? false : (!damage ? false : stack0.stackSize <= stack0.getMaxStackSize());
            }
        }
    }

    public boolean areItemStackTagsEqualForThisRecipe(ItemStack slotItem, ItemStack recipeItem) {
        return ThaumcraftApiHelper.areItemStackTagsEqualForCrafting(slotItem, recipeItem);
    }
}
