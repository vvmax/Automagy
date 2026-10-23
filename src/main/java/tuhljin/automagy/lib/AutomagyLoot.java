package tuhljin.automagy.lib;

import net.minecraft.item.Item;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import tuhljin.automagy.items.ModItems;

public class AutomagyLoot {
    public static void addLoot() {
        String[] categoriesNormal = new String[]{
            "mineshaftCorridor", "pyramidDesertyChest", "pyramidJungleChest", "strongholdCorridor", "strongholdLibrary", "strongholdCrossing", "dungeonChest"
        };
        populateChests(ModItems.ringEnderDisjunction, 1, AutomagyConfig.lootWeightRingEnderDisjunctionLesser, categoriesNormal);
        populateChests(ModItems.phialXP, 0, 1, 3, AutomagyConfig.lootWeightPhialXP, categoriesNormal);
        populateChests(ModItems.phialXP, 0, 1, 2, AutomagyConfig.lootWeightBlacksmithPhialXP, "villageBlacksmith");
    }

    public static void populateChests(Item item, int md, int min, int max, int weight, String... categories) {
        if (weight > 0) {
            for (String category : categories) {
                ChestGenHooks.addItem(category, new WeightedRandomChestContent(item, md, min, max, weight));
            }
        }
    }

    public static void populateChests(Item item, int md, int weight, String... categories) {
        populateChests(item, md, 1, 1, weight, categories);
    }
}
