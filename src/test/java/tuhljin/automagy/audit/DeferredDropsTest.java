package tuhljin.automagy.audit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.storage.MapStorage;
import org.junit.Test;
import tuhljin.automagy.TestRegistries;
import tuhljin.automagy.entities.EntityItemDelayedFall;
import tuhljin.automagy.lib.DeferredItemDrops;
import tuhljin.automagy.lib.TjUtil;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class DeferredDropsTest {
    @Test public void acceptedDropsPreserveBalanceAndDoNotCreateQueue() throws Exception {
        Fixture f = new Fixture(); f.accept = true;
        ItemStack stack = f.stack(64); TjUtil.dropItemIntoWorld(stack, f.world, 1, 64, 2);
        assertEquals(0, stack.stackSize); assertEquals(64, f.delivered());
        assertNull(f.world.perWorldStorage.loadData(DeferredItemDrops.class, DeferredItemDrops.KEY));
    }

    @Test public void rejectedDropsSurviveNbtAndDeliverOnceWithTags() throws Exception {
        Fixture f = new Fixture(); ItemStack stack = f.stack(37);
        TjUtil.dropItemIntoWorld(stack, f.world, 1, 64, 2);
        assertEquals(0, stack.stackSize); assertEquals(37, f.pending());
        DeferredItemDrops restored = new DeferredItemDrops(DeferredItemDrops.KEY); restored.readFromNBT(f.saved());
        f.world.perWorldStorage.setData(DeferredItemDrops.KEY, restored);
        f.accept = true; restored.retry(f.world); restored.retry(f.world);
        assertEquals(0, f.pending()); assertEquals(37, f.delivered());
        for (EntityItem entity : f.spawned) {
            assertEquals(7, entity.getEntityItem().getMetadata());
            assertEquals("retained", entity.getEntityItem().getTagCompound().getString("marker"));
        }
    }

    @Test public void partialRejectionConservesAcceptedPlusPendingCounts() throws Exception {
        Fixture f = new Fixture(); AtomicInteger attempts = new AtomicInteger();
        when(f.world.spawnEntityInWorld(any())).thenAnswer(c -> {
            if (attempts.getAndIncrement() == 0) {f.spawned.add(c.getArgument(0)); return true;} return false;
        });
        ItemStack stack = f.stack(64); TjUtil.dropItemIntoWorld(stack, f.world, 1, 64, 2);
        assertTrue(f.delivered() > 0); assertTrue(f.pending() > 0);
        assertEquals(64, f.delivered() + f.pending());
    }

    @Test public void repeatedRejectionDoesNotMultiplyQueue() throws Exception {
        Fixture f = new Fixture(); TjUtil.dropItemIntoWorld(f.stack(37), f.world, 1, 64, 2);
        for (int i = 0; i < 10; i++) DeferredItemDrops.get(f.world).retry(f.world);
        assertEquals(37, f.pending()); assertEquals(0, f.delivered());
    }

    @Test public void unloadedChunkIsNotForcedAndDropRetriesAfterLoading() throws Exception {
        Fixture f = new Fixture(); TjUtil.dropItemIntoWorldSimple(f.stack(1), f.world, 1, 64, 2);
        when(f.world.blockExists(anyInt(), anyInt(), anyInt())).thenReturn(false);
        clearInvocations(f.world); DeferredItemDrops.get(f.world).retry(f.world);
        verify(f.world, never()).spawnEntityInWorld(any());
        verify(f.world, never()).getChunkFromBlockCoords(anyInt(), anyInt());
        assertEquals(1, f.pending());
        when(f.world.blockExists(anyInt(), anyInt(), anyInt())).thenReturn(true);
        f.accept = true; DeferredItemDrops.get(f.world).retry(f.world);
        assertEquals(1, f.delivered()); assertEquals(0, f.pending());
    }

    @Test public void retriesHaveFiniteBudget() throws Exception {
        Fixture f = new Fixture();
        for (int i = 0; i < 65; i++) TjUtil.dropItemIntoWorldSimple(f.stack(1), f.world, i, 64, 0);
        f.accept = true; clearInvocations(f.world);
        DeferredItemDrops.get(f.world).retry(f.world);
        verify(f.world, times(32)).spawnEntityInWorld(any());
        assertEquals(32, f.delivered()); assertEquals(33, f.pending());
    }

    @Test public void initialHoverDropSurvivesQueueRoundTrip() throws Exception {
        Fixture f = new Fixture(); EntityItemDelayedFall original = EntityItemDelayedFall.spawn(f.stack(5), f.world, 1, 64, 2);
        DeferredItemDrops restored = new DeferredItemDrops(DeferredItemDrops.KEY); restored.readFromNBT(f.saved());
        f.accept = true; restored.retry(f.world);
        assertEquals(1, f.spawned.size()); assertTrue(f.spawned.get(0) instanceof EntityItemDelayedFall);
        EntityItemDelayedFall entity = (EntityItemDelayedFall)f.spawned.get(0);
        assertEquals(original.delayBeforeFall, entity.delayBeforeFall);
        assertEquals(original.motionY, entity.motionY, 0.0); assertEquals(5, entity.getEntityItem().stackSize);
    }

    @Test public void rollbackDropsAreSuppressedWithoutBecomingDeferredDuplicates() throws Exception {
        Fixture f = new Fixture(); f.world.restoringBlockSnapshots = true;
        TjUtil.dropItemIntoWorld(f.stack(5), f.world, 1, 64, 2);
        verify(f.world, never()).spawnEntityInWorld(any());
        assertNull(f.world.perWorldStorage.loadData(DeferredItemDrops.class, DeferredItemDrops.KEY));
    }

    @Test public void exceptionDuringRetryDoesNotLosePendingEntry() throws Exception {
        Fixture f = new Fixture(); TjUtil.dropItemIntoWorldSimple(f.stack(5), f.world, 1, 64, 2);
        when(f.world.spawnEntityInWorld(any())).thenThrow(new IllegalStateException("listener"));
        DeferredItemDrops.get(f.world).retry(f.world);
        assertEquals(5, f.pending());
        doReturn(true).when(f.world).spawnEntityInWorld(any());
        DeferredItemDrops.get(f.world).retry(f.world); assertEquals(0, f.pending());
    }

    @Test public void reentrantRetryCannotEmitSameEntryAgain() throws Exception {
        Fixture f = new Fixture(); TjUtil.dropItemIntoWorldSimple(f.stack(5), f.world, 1, 64, 2);
        when(f.world.spawnEntityInWorld(any())).thenAnswer(c -> {
            DeferredItemDrops.get(f.world).retry(f.world); f.spawned.add(c.getArgument(0)); return true;
        });
        DeferredItemDrops.get(f.world).retry(f.world);
        assertEquals(5, f.delivered()); assertEquals(0, f.pending());
    }

    @Test public void worldTickRetriesOnlyAtEndAndOncePerSecond() throws Exception {
        Fixture f = new Fixture(); TjUtil.dropItemIntoWorldSimple(f.stack(5), f.world, 1, 64, 2);
        tuhljin.automagy.lib.events.AutomagyEventHandler handler = new tuhljin.automagy.lib.events.AutomagyEventHandler();
        f.accept = true;
        when(f.world.getTotalWorldTime()).thenReturn(20L);
        handler.onDeferredDropsTick(new cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent(cpw.mods.fml.relauncher.Side.SERVER, cpw.mods.fml.common.gameevent.TickEvent.Phase.START, f.world));
        assertEquals(0, f.delivered());
        when(f.world.getTotalWorldTime()).thenReturn(21L);
        handler.onDeferredDropsTick(new cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent(cpw.mods.fml.relauncher.Side.SERVER, cpw.mods.fml.common.gameevent.TickEvent.Phase.END, f.world));
        assertEquals(0, f.delivered());
        when(f.world.getTotalWorldTime()).thenReturn(40L);
        handler.onDeferredDropsTick(new cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent(cpw.mods.fml.relauncher.Side.SERVER, cpw.mods.fml.common.gameevent.TickEvent.Phase.END, f.world));
        assertEquals(5, f.delivered()); assertEquals(0, f.pending());
    }

    private static class Fixture {
        final World world = mock(World.class);
        final Item item = new Item(); final List<EntityItem> spawned = new ArrayList<>(); boolean accept;
        Fixture() throws Exception {
            Field provider = World.class.getDeclaredField("provider"); provider.setAccessible(true); provider.set(world, new WorldProviderSurface());
            Field storage = World.class.getDeclaredField("perWorldStorage"); storage.setAccessible(true); storage.set(world, new MapStorage(null));
            world.rand = new Random(1); TestRegistries.register(item);
            when(world.blockExists(anyInt(), anyInt(), anyInt())).thenReturn(true);
            when(world.spawnEntityInWorld(any())).thenAnswer(c -> {if (accept) spawned.add(c.getArgument(0)); return accept;});
        }
        ItemStack stack(int count) {ItemStack stack = new ItemStack(item, count, 7); stack.setTagCompound(new NBTTagCompound()); stack.getTagCompound().setString("marker", "retained"); return stack;}
        NBTTagCompound saved() {NBTTagCompound tag = new NBTTagCompound(); DeferredItemDrops.get(world).writeToNBT(tag); return tag;}
        int pending() {NBTTagCompound tag = saved(); int count = 0; for (int i = 0; i < tag.getTagList("Items", 10).tagCount(); i++) count += ItemStack.loadItemStackFromNBT(tag.getTagList("Items", 10).getCompoundTagAt(i).getCompoundTag("Item")).stackSize; return count;}
        int delivered() {int count = 0; for (EntityItem entity : spawned) count += entity.getEntityItem().stackSize; return count;}
    }
}
