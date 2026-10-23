package tuhljin.automagy;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.RegistryNamespaced;
import java.lang.reflect.Field;
import cpw.mods.fml.common.registry.RegistryDelegate;

/** Minimal real registry entries for headless tests; no Minecraft/FML bootstrap. */
public final class TestRegistries {
    private static final AtomicInteger NEXT_BLOCK = new AtomicInteger(3000);
    private static final AtomicInteger NEXT_ITEM = new AtomicInteger(20000);
    public static void register(Block block) throws Exception { add(Block.blockRegistry, block, NEXT_BLOCK.getAndIncrement()); }
    public static void register(Item item) throws Exception {
        if (item.delegate == null) {
            Field delegate = Item.class.getDeclaredField("delegate");
            delegate.setAccessible(true);
            delegate.set(item, new RegistryDelegate.Delegate<Item>(item, Item.class));
        }
        add(Item.itemRegistry, item, NEXT_ITEM.getAndIncrement());
    }
    private static void add(RegistryNamespaced registry, Object value, int id) throws Exception {
        Method raw = registry.getClass().getDeclaredMethod("addObjectRaw", int.class, String.class, Object.class);
        raw.setAccessible(true);
        raw.invoke(registry, id, "automagy_test:entry_" + id, value);
    }
    private TestRegistries() { }
}
