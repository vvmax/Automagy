package tuhljin.automagy.lib.compat.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.guihook.GuiContainerManager;
import java.util.List;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.gui.GUIGolemWorkbench;
import tuhljin.automagy.gui.GUIPortableArcaneCrafting;

public class NEIProviders {
    public static void register() {
        GuiContainerManager.addObjectHandler(new NEIHandlePseudoSlots());
        API.registerGuiOverlayHandler(GUIGolemWorkbench.class, new GolemWorkbenchOverlayHandler(), "crafting");
        API.registerGuiOverlay(GUIPortableArcaneCrafting.class, "crafting", new SimpleStackPositioner(40, 29, 24, 24));
        API.registerGuiOverlayHandler(GUIPortableArcaneCrafting.class, new SimpleCraftingOverlayHandler(2), "crafting");
        API.registerGuiOverlayHandler(GUIPortableArcaneCrafting.class, new ArcaneCraftingOverlayHandler(2), "arcaneshapedrecipes");
        API.registerGuiOverlayHandler(GUIPortableArcaneCrafting.class, new ArcaneCraftingOverlayHandler(2), "arcaneshapelessrecipes");
    }

    public static ItemStack[] getAlignedIngredients(List<PositionedStack> ingredients) {
        ItemStack[] alignedIngredients = new ItemStack[9];

        for (PositionedStack current : ingredients) {
            if (current != null) {
                int row = (current.relx - 25) / 18;
                int column = (current.rely - 6) / 18;
                alignedIngredients[column * 3 + row] = current.items[0];
            }
        }

        return alignedIngredients;
    }
}
