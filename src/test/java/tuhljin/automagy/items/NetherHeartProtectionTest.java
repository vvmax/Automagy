package tuhljin.automagy.items;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventBus;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import org.junit.Test;
import sun.misc.Unsafe;
import tuhljin.automagy.TestRegistries;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class NetherHeartProtectionTest {
    @Test public void deniedTickDoesNotRetryHundredsOfProtectedPlacements() throws Exception {
        try (Fixture f = new Fixture()) {
            f.world.rand = mock(Random.class);
            when(f.world.canMineBlock(any(), anyInt(), anyInt(), anyInt())).thenReturn(false);
            f.player.worldObj = f.world;
            f.player.posY = 64;
            f.heart.onUsingTick(null, f.player, 350);
            verify(f.world, times(1)).canMineBlock(any(), anyInt(), anyInt(), anyInt());
            verify(f.world, never()).setBlock(anyInt(), anyInt(), anyInt(), any(), anyInt(), anyInt());
        }
    }
    @Test public void deniedSpawnAndEditPermissionsPreventIgnition() throws Exception {
        try (Fixture f = new Fixture()) {
            when(f.world.canMineBlock(eq(f.player), anyInt(), anyInt(), anyInt())).thenReturn(false);
            assertFalse(f.ignite());
            when(f.world.canMineBlock(eq(f.player), anyInt(), anyInt(), anyInt())).thenReturn(true);
            when(f.player.canPlayerEdit(anyInt(), anyInt(), anyInt(), anyInt(), any())).thenReturn(false);
            assertFalse(f.ignite());
            verify(f.world, never()).setBlock(anyInt(), anyInt(), anyInt(), any(), anyInt(), anyInt());
            verifyNoInteractions(f.bus);
        }
    }

    @Test public void canceledPlacementRestoresAirAndTransactionFlags() throws Exception {
        try (Fixture f = new Fixture()) {
            when(f.bus.post(isA(BlockEvent.PlaceEvent.class))).thenAnswer(call -> {cancel(call.getArgument(0)); return true;});
            assertFalse(f.ignite());
            assertSame(f.air, f.block(0));
            assertFalse(f.world.captureBlockSnapshots); assertFalse(f.world.restoringBlockSnapshots);
            assertTrue(f.world.capturedBlockSnapshots.isEmpty());
            verify(f.world, never()).markAndNotifyBlock(anyInt(), anyInt(), anyInt(), any(), any(), any(), anyInt());
        }
    }

    @Test public void permittedFireStillAppearsAndNotifiesTheWorld() throws Exception {
        try (Fixture f = new Fixture()) {
            assertTrue(f.ignite()); assertSame(f.fire, f.block(0));
            verify(f.fire).onBlockAdded(f.world, 0, 64, 0);
            verify(f.bus).post(isA(BlockEvent.PlaceEvent.class));
            verify(f.world).markAndNotifyBlock(0, 64, 0, null, f.air, f.fire, 3);
            assertFalse(f.world.captureBlockSnapshots); assertTrue(f.world.capturedBlockSnapshots.isEmpty());
        }
    }

    @Test public void canceledMultiPlacementRestoresEveryCapturedBlock() throws Exception {
        try (Fixture f = new Fixture()) {
            f.simulatePortal = true;
            when(f.bus.post(isA(BlockEvent.MultiPlaceEvent.class))).thenAnswer(call -> {cancel(call.getArgument(0)); return true;});
            assertFalse(f.ignite()); assertSame(f.air, f.block(0)); assertSame(f.air, f.block(1));
            verify(f.bus).post(isA(BlockEvent.MultiPlaceEvent.class));
        }
    }

    @Test public void deferredFireInitializationIsCapturedBeforeTheProtectionEvent() throws Exception {
        try (Fixture f = new Fixture()) {
            doAnswer(call -> {
                assertTrue(f.world.captureBlockSnapshots);
                f.world.setBlock(1, 64, 0, f.portal, 0, 3);
                return null;
            }).when(f.fire).onBlockAdded(f.world, 0, 64, 0);
            when(f.bus.post(isA(BlockEvent.MultiPlaceEvent.class))).thenAnswer(call -> {cancel(call.getArgument(0)); return true;});
            assertFalse(f.ignite()); assertSame(f.air, f.block(0)); assertSame(f.air, f.block(1));
            verify(f.bus).post(isA(BlockEvent.MultiPlaceEvent.class));
        }
    }

    @Test public void deniedSecondaryPositionRollsBackTheWholeChange() throws Exception {
        try (Fixture f = new Fixture()) {
            f.simulatePortal = true;
            when(f.world.canMineBlock(f.player, 1, 64, 0)).thenReturn(false);
            assertFalse(f.ignite()); assertSame(f.air, f.block(0)); assertSame(f.air, f.block(1));
            verifyNoInteractions(f.bus);
        }
    }

    @Test public void permittedMultiPlacementKeepsAllCapturedBlocks() throws Exception {
        try (Fixture f = new Fixture()) {
            f.simulatePortal = true;
            assertTrue(f.ignite()); assertSame(f.fire, f.block(0)); assertSame(f.portal, f.block(1));
            verify(f.bus).post(isA(BlockEvent.MultiPlaceEvent.class));
        }
    }

    @Test public void throwingProtectionListenerRollsBackAndRestoresFlags() throws Exception {
        try (Fixture f = new Fixture()) {
            when(f.bus.post(isA(BlockEvent.PlaceEvent.class))).thenThrow(new IllegalStateException("listener"));
            assertThrows(IllegalStateException.class, f::ignite);
            assertSame(f.air, f.block(0)); assertFalse(f.world.captureBlockSnapshots); assertFalse(f.world.restoringBlockSnapshots);
        }
    }

    @Test public void clientAndNestedTransactionsDoNotModifyTheWorld() throws Exception {
        try (Fixture f = new Fixture()) {
            f.world.isRemote = true; assertFalse(f.ignite());
            f.world.isRemote = false; f.world.captureBlockSnapshots = true; assertFalse(f.ignite());
            verify(f.world, never()).setBlock(anyInt(), anyInt(), anyInt(), any(), anyInt(), anyInt());
            assertTrue(f.world.captureBlockSnapshots);
        }
    }

    @Test public void failedPlacementDoesNotReportSuccess() throws Exception {
        try (Fixture f = new Fixture()) {
            doReturn(false).when(f.world).setBlock(0, 64, 0, f.fire, 0, 3);
            assertFalse(f.ignite()); assertSame(f.air, f.block(0)); verifyNoInteractions(f.bus);
        }
    }

    public static abstract class TestHeart extends ItemNetherHeart {
        TestHeart() {super("unused-test-constructor");}
        public boolean ignite(World world, EntityPlayer player) {return super.tryIgnite(world, player, null, 0, 64, 0);}
    }

    private static class Fixture implements AutoCloseable {
        final EventBus previousBus = MinecraftForge.EVENT_BUS;
        final BlockFire previousFire = Blocks.fire;
        final EventBus bus = mock(EventBus.class);
        final World world = mock(World.class);
        final EntityPlayer player = mock(EntityPlayer.class);
        final Block air = mock(Block.class);
        final BlockFire fire = mock(BlockFire.class);
        final Block portal = mock(Block.class);
        final TestHeart heart = mock(TestHeart.class, CALLS_REAL_METHODS);
        final Map<Integer, Block> blocks = new HashMap<>();
        boolean simulatePortal;
        Fixture() throws Exception {
            setStatic(MinecraftForge.class.getDeclaredField("EVENT_BUS"), bus);
            setStatic(Blocks.class.getDeclaredField("fire"), fire);
            when(air.getMaterial()).thenReturn(Material.air);
            when(fire.getMaterial()).thenReturn(Material.fire);
            TestRegistries.register(air); TestRegistries.register(fire); TestRegistries.register(portal);
            Field provider = World.class.getDeclaredField("provider"); provider.setAccessible(true); provider.set(world, new WorldProviderSurface());
            Field snapshots = World.class.getDeclaredField("capturedBlockSnapshots"); snapshots.setAccessible(true); snapshots.set(world, new ArrayList<BlockSnapshot>());
            when(world.getHeight()).thenReturn(256);
            when(world.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(call -> block(call.getArgument(0)));
            when(world.isAirBlock(anyInt(), anyInt(), anyInt())).thenAnswer(call -> block(call.getArgument(0)) == air);
            when(world.isSideSolid(anyInt(), anyInt(), anyInt(), any(), anyBoolean())).thenReturn(true);
            when(world.canMineBlock(eq(player), anyInt(), anyInt(), anyInt())).thenReturn(true);
            when(player.canPlayerEdit(anyInt(), anyInt(), anyInt(), anyInt(), any())).thenReturn(true);
            doAnswer(call -> {
                int x = call.getArgument(0); Block next = call.getArgument(3);
                if (world.captureBlockSnapshots) world.capturedBlockSnapshots.add(BlockSnapshot.getBlockSnapshot(world, x, 64, 0));
                blocks.put(x, next);
                // Model onBlockAdded creating a second block in the same captured transaction.
                if (simulatePortal && x == 0 && next == fire && !world.restoringBlockSnapshots) {
                    if (world.captureBlockSnapshots) world.capturedBlockSnapshots.add(BlockSnapshot.getBlockSnapshot(world, 1, 64, 0));
                    blocks.put(1, portal);
                }
                return true;
            }).when(world).setBlock(anyInt(), anyInt(), anyInt(), any(), anyInt(), anyInt());
        }
        Block block(int x) {return blocks.getOrDefault(x, air);}
        boolean ignite() {return heart.ignite(world, player);}
        public void close() throws Exception {
            setStatic(MinecraftForge.class.getDeclaredField("EVENT_BUS"), previousBus);
            setStatic(Blocks.class.getDeclaredField("fire"), previousFire);
        }
    }

    private static void setStatic(Field field, Object value) throws Exception {
        Field singleton = Unsafe.class.getDeclaredField("theUnsafe"); singleton.setAccessible(true); Unsafe unsafe = (Unsafe)singleton.get(null);
        unsafe.putObjectVolatile(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
    }
    private static void cancel(Event event) throws Exception {
        // FML normally generates cancellation support from this annotation.
        assertNotNull(event.getClass().getAnnotation(Cancelable.class));
        Field canceled = Event.class.getDeclaredField("isCanceled"); canceled.setAccessible(true); canceled.setBoolean(event, true);
    }
}
