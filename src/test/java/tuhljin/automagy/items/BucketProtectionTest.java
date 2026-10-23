package tuhljin.automagy.items;

import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.Cancelable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.world.BlockEvent;
import org.junit.Test;
import sun.misc.Unsafe;
import tuhljin.automagy.TestRegistries;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BucketProtectionTest {
    @Test public void spawnProtectedTargetIsNotChanged() throws Exception {
        try (Fixture f = new Fixture()) {
            when(f.world.canMineBlock(eq(f.player), anyInt(), anyInt(), anyInt())).thenReturn(false);
            f.use();
            assertSame(f.air, f.block.get());
            verify(f.world, never()).setBlock(anyInt(), anyInt(), anyInt(), any(), anyInt(), anyInt());
        }
    }

    @Test public void canceledBucketEventKeepsWorldAndBucket() throws Exception {
        try (Fixture f = new Fixture()) {
            when(f.bus.post(isA(FillBucketEvent.class))).thenReturn(true);
            assertSame(f.stack, f.use());
            assertSame(f.air, f.block.get());
            verify(f.world, never()).setBlock(anyInt(), anyInt(), anyInt(), any(), anyInt(), anyInt());
        }
    }

    @Test public void canceledPlacementRestoresTheOriginalBlock() throws Exception {
        try (Fixture f = new Fixture()) {
            f.player.capabilities.isCreativeMode = false;
            when(f.bus.post(isA(BlockEvent.PlaceEvent.class))).thenAnswer(call -> {
                cancel(call.getArgument(0));
                return true;
            });
            assertSame(f.stack, f.use());
            assertSame(f.air, f.block.get());
            assertFalse(f.world.captureBlockSnapshots);
            assertFalse(f.world.restoringBlockSnapshots);
        }
    }

    @Test public void permittedPlacementStillWorks() throws Exception {
        try (Fixture f = new Fixture()) {
            f.player.capabilities.isCreativeMode = false;
            assertSame(net.minecraft.init.Items.bucket, f.use().getItem());
            assertSame(f.fluid, f.block.get());
            assertFalse(f.world.captureBlockSnapshots);
            verify(f.bus).post(isA(BlockEvent.PlaceEvent.class));
        }
    }

    @Test public void clientDoesNotChangeBlocksOrFireServerProtectionEvents() throws Exception {
        try (Fixture f = new Fixture()) {
            f.world.isRemote = true;
            assertSame(f.stack, f.use());
            assertSame(f.air, f.block.get());
            verifyNoInteractions(f.bus);
        }
    }

    @Test public void failingPlacementKeepsTheFilledBucket() throws Exception {
        try (Fixture f = new Fixture()) {
            f.player.capabilities.isCreativeMode = false;
            doReturn(false).when(f.world).setBlock(anyInt(), anyInt(), anyInt(), eq(f.fluid), anyInt(), anyInt());
            assertSame(f.stack, f.use());
            assertSame(f.air, f.block.get());
        }
    }

    @Test public void throwingProtectionListenerRollsBackWithoutDrops() throws Exception {
        try (Fixture f = new Fixture()) {
            when(f.bus.post(isA(BlockEvent.PlaceEvent.class))).thenThrow(new IllegalStateException("test listener"));
            try {
                f.use();
                fail("Expected listener exception");
            } catch (IllegalStateException expected) { assertEquals("test listener", expected.getMessage()); }
            assertSame(f.air, f.block.get());
            assertFalse(f.world.captureBlockSnapshots);
            assertFalse(f.world.restoringBlockSnapshots);
            verify(f.air, never()).dropBlockAsItemWithChance(any(), anyInt(), anyInt(), anyInt(), anyInt(), anyFloat(), anyInt());
        }
    }

    @Test public void deniedBreakPreventsReplacingAPlant() throws Exception {
        initializeHarvestTableBlocks();
        try (Fixture f = new Fixture()) {
            Block plant = mock(Block.class);
            when(plant.getMaterial()).thenReturn(Material.plants);
            TestRegistries.register(plant);
            f.block.set(plant);
            when(f.bus.post(isA(BlockEvent.BreakEvent.class))).thenReturn(true);
            assertSame(f.stack, f.use());
            assertSame(plant, f.block.get());
            verify(f.world, never()).setBlock(anyInt(), anyInt(), anyInt(), any(), anyInt(), anyInt());
        }
    }

    private static void initializeHarvestTableBlocks() throws Exception {
        // ForgeHooks initializes vanilla tool tables when constructing BreakEvent.
        // A headless JVM has no vanilla registry bootstrap; supply only those table entries.
        Unsafe unsafe = unsafe();
        for (Field field : net.minecraft.init.Blocks.class.getDeclaredFields()) {
            if (field.getType() == Block.class && field.get(null) == null) {
                unsafe.putObjectVolatile(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), mock(Block.class));
            }
        }
        for (Class<?> type : new Class<?>[] {net.minecraft.item.ItemPickaxe.class,
            net.minecraft.item.ItemSpade.class, net.minecraft.item.ItemAxe.class}) {
            Field effective = type.getDeclaredFields()[0];
            effective.setAccessible(true);
            ((java.util.Set<?>)effective.get(null)).remove(null);
        }
    }

    public static abstract class TestBucket extends ItemBucketCustom {
        MovingObjectPosition target;
        TestBucket() { super("unused-test-constructor", null); }
        @Override public MovingObjectPosition getMovingObjectPositionFromPlayer(World world, EntityPlayer player, boolean liquids) {
            return target;
        }
    }

    private static class Fixture implements AutoCloseable {
        final EventBus previousBus = MinecraftForge.EVENT_BUS;
        final EventBus bus = mock(EventBus.class);
        final World world = mock(World.class);
        final EntityPlayer player = mock(EntityPlayer.class);
        final Block air = mock(Block.class);
        final Block fluid = mock(Block.class);
        final AtomicReference<Block> block = new AtomicReference<>(air);
        final TestBucket bucket = mock(TestBucket.class, CALLS_REAL_METHODS);
        final ItemStack stack = new ItemStack(bucket);
        Fixture() throws Exception {
            replaceBus(bus);
            if (net.minecraft.init.Items.bucket == null) {
                net.minecraft.item.Item emptyBucket = new net.minecraft.item.Item();
                TestRegistries.register(emptyBucket);
                Field field = net.minecraft.init.Items.class.getDeclaredField("bucket");
                unsafe().putObjectVolatile(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), emptyBucket);
            }
            player.capabilities = new PlayerCapabilities();
            player.capabilities.isCreativeMode = true;
            when(air.getMaterial()).thenReturn(Material.air);
            when(fluid.getMaterial()).thenReturn(Material.water);
            TestRegistries.register(air);
            TestRegistries.register(fluid);
            TestRegistries.register(bucket);
            stack.setItem(bucket);
            when(world.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(c -> block.get());
            when(world.isAirBlock(anyInt(), anyInt(), anyInt())).thenAnswer(c -> block.get() == air);
            when(world.canMineBlock(eq(player), anyInt(), anyInt(), anyInt())).thenReturn(true);
            when(world.getHeight()).thenReturn(256);
            when(player.canPlayerEdit(anyInt(), anyInt(), anyInt(), anyInt(), any())).thenReturn(true);
            when(world.setBlock(anyInt(), anyInt(), anyInt(), any(), anyInt(), anyInt())).thenAnswer(c -> {
                block.set(c.getArgument(3));
                return true;
            });
            Field provider = World.class.getDeclaredField("provider");
            provider.setAccessible(true);
            provider.set(world, new WorldProviderSurface());
            Field captured = World.class.getDeclaredField("capturedBlockSnapshots");
            captured.setAccessible(true);
            captured.set(world, new ArrayList<>());
            bucket.fluidBlock = fluid;
            bucket.target = new MovingObjectPosition(0, 0, 0, 1, Vec3.createVectorHelper(0, 0, 0));
        }
        ItemStack use() { return bucket.onItemRightClick(stack, world, player); }
        @Override public void close() throws Exception { replaceBus(previousBus); }
    }

    private static void replaceBus(EventBus value) throws Exception {
        Unsafe unsafe = unsafe();
        Field bus = MinecraftForge.class.getDeclaredField("EVENT_BUS");
        unsafe.putObjectVolatile(unsafe.staticFieldBase(bus), unsafe.staticFieldOffset(bus), value);
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe)field.get(null);
    }

    private static void cancel(Event event) throws Exception {
        // The headless JVM does not apply FML's @Cancelable bytecode transformer.
        // Model the resulting canceled state on the real event, retaining the real event payload.
        assertNotNull(event.getClass().getAnnotation(Cancelable.class));
        Field canceled = Event.class.getDeclaredField("isCanceled");
        canceled.setAccessible(true);
        canceled.setBoolean(event, true);
    }
}
