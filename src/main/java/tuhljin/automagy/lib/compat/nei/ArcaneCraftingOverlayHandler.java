package tuhljin.automagy.lib.compat.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import java.util.ArrayList;
import java.util.List;

public class ArcaneCraftingOverlayHandler extends SimpleCraftingOverlayHandler {
    public ArcaneCraftingOverlayHandler(int firstCraftingSlotNumber) {
        super(firstCraftingSlotNumber, 3, 3, 27, 27, 48, 33);
    }

    @Override
    public List<PositionedStack> getIngredientStacksFromRecipe(IRecipeHandler recipe, int recipeIndex) {
        List<PositionedStack> rawIngredients = super.getIngredientStacksFromRecipe(recipe, recipeIndex);
        if (rawIngredients == null) {
            return null;
        } else {
            List<PositionedStack> ingredients = new ArrayList<>();

            for (PositionedStack ing : rawIngredients) {
                if (ing != null && ing.item != null && ing.item.getItem() != null) {
                    int row = this.getRow(ing);
                    this.getColumn(ing);
                    if (row < this.rows) {
                        ingredients.add(ing);
                    }
                }
            }

            return ingredients;
        }
    }
}
