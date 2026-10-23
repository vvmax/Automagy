package tuhljin.automagy.entities;

import cpw.mods.fml.common.asm.transformers.SideTransformer;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ArchivedServerFixTest {
    @Test public void dispenserConstructorSurvivesActualServerSideTransformer() throws Exception {
        Field side = FMLLaunchHandler.class.getDeclaredField("side");
        side.setAccessible(true);
        Object previous = side.get(null);
        try {
            side.set(null, Side.SERVER);
            String name = "tuhljin.automagy.entities.EntityAvaricePearl";
            byte[] bytes;
            try (InputStream in = getClass().getResourceAsStream("/" + name.replace('.', '/') + ".class")) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                for (int n; (n = in.read(buffer)) != -1;) out.write(buffer, 0, n);
                bytes = out.toByteArray();
            }
            ClassNode node = new ClassNode();
            new ClassReader(new SideTransformer().transform(name, name, bytes)).accept(node, 0);
            assertTrue(node.methods.stream().anyMatch(m -> m.name.equals("<init>") && m.desc.equals("(Lnet/minecraft/world/World;DDDI)V")));
        } finally { side.set(null, previous); }
    }

    @Test public void expiredHoverItemCannotRespawnAfterItsStackWasCollected() throws Exception {
        World world = mock(World.class);
        Field provider = World.class.getDeclaredField("provider");
        provider.setAccessible(true);
        provider.set(world, new WorldProviderSurface());
        Item consumed = new Item() {
            @Override public boolean onEntityItemUpdate(EntityItem entity) {
                entity.setDead();
                return true;
            }
        };
        EntityItemDelayedFall entity = new EntityItemDelayedFall(null);
        entity.worldObj = world;
        entity.setEntityItemStack(new ItemStack(consumed, 64));
        entity.delayBeforeFall = 1;
        entity.onUpdate();
        assertTrue(entity.isDead);
        verify(world, never()).spawnEntityInWorld(any());
    }
}
