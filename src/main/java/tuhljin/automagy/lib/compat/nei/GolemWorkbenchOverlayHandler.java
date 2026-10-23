package tuhljin.automagy.lib.compat.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.gui.GUIGolemWorkbench;

public class GolemWorkbenchOverlayHandler implements IOverlayHandler {
    public void overlayRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, boolean shift) {
        if (firstGui instanceof GUIGolemWorkbench) {
            List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
            ItemStack[] stacks = NEIProviders.getAlignedIngredients(ingredients);
            GUIGolemWorkbench guiGW = (GUIGolemWorkbench)firstGui;
            guiGW.tile.clientSendRecipeToServer(stacks, guiGW.getRecipeIndex());
        }
    }
}
