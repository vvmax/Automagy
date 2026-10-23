package tuhljin.automagy.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.block.Block;
import tuhljin.automagy.api.inventarium.IInventariumContentsProvider;

public class AutomagyAPI {
    public static ArrayList<Block> invContainerBlacklist = new ArrayList<>();
    public static ArrayList<Block> invOtherBookshelves = new ArrayList<>();
    public static Map<Integer, List<IInventariumContentsProvider>> invContentsProviders = new TreeMap<>();

    public static void registerInventariumContentsProvider(IInventariumContentsProvider provider, int priority) {
        if (!invContentsProviders.containsKey(priority)) {
            invContentsProviders.put(priority, new ArrayList<>());
        }

        invContentsProviders.get(priority).add(provider);
    }
}
