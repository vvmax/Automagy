package tuhljin.automagy.catalog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import org.junit.Test;
import thaumcraft.common.blocks.BlockWoodenDevice;
import thaumcraft.common.config.ConfigItems;
import tuhljin.automagy.TestRegistries;
import tuhljin.automagy.blocks.BlockEssentiaLocus;
import tuhljin.automagy.blocks.BlockThaumostaticRefuelerSpire;
import tuhljin.automagy.blocks.ModBlockContainer;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.tiles.TileEntityEssentiaLocus;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Real teardown/drop methods with world storage and callback ordering modeled explicitly. */
public class MultiblockLifecycleTest {
    @Test public void spireDoesNotRemoveAnotherBaseAboveIt() throws Exception {
        try (Spire f = new Spire()) {
            f.parts.put(70, 0); f.parts.put(71, 0);
            f.world.setBlockToAir(0, 70, 0);
            assertEquals(Integer.valueOf(0), f.parts.get(71));
            assertEquals(0, f.ingots);
        }
    }

    @Test public void spireDoesNotRefundWhenBaseRemovalFails() throws Exception {
        try (Spire f = new Spire()) {
            f.parts.put(70, 0); f.parts.put(71, 1); f.blockedY = 70;
            f.world.setBlockToAir(0, 71, 0);
            assertEquals(Integer.valueOf(0), f.parts.get(70));
            assertEquals(0, f.ingots);
        }
    }

    @Test public void repeatedUpperCallbackCannotRefundRemovedBaseTwice() throws Exception {
        try (Spire f = new Spire()) {
            f.parts.put(70, 0); f.parts.put(71, 1);
            f.world.setBlockToAir(0, 71, 0);
            f.block.breakBlock(f.world, 0, 71, 0, f.block, 1);
            assertTrue(f.parts.isEmpty()); assertEquals(9, f.ingots);
        }
    }

    @Test public void clientSpireCallbackDoesNotRemoveCompanion() throws Exception {
        try (Spire f = new Spire()) {
            setRemote(f.world); f.parts.put(70, 0); f.parts.put(71, 1);
            f.block.breakBlock(f.world, 0, 71, 0, f.block, 1);
            assertEquals(2, f.parts.size()); assertEquals(0, f.ingots);
            verify(f.world, never()).setBlockToAir(anyInt(), anyInt(), anyInt());
        }
    }

    @Test public void orphanIsStoppedImmediatelyAndReplacementTileIsPreserved() throws Exception {
        try (Locus f = new Locus()) {
            TileEntity replacement = new TileEntity(); f.registered = replacement;
            f.locus.updateEntity();
            assertTrue(f.locus.isInvalid()); assertSame(replacement, f.registered);
            assertSame(f.block, f.present); assertEquals(0, f.drops);
            verify(f.world, never()).setBlockToAir(anyInt(), anyInt(), anyInt());
            verify(f.world, never()).removeTileEntity(anyInt(), anyInt(), anyInt());
            verify(f.world, never()).spawnEntityInWorld(any());
        }
    }

    @Test public void registeredTileAtForeignBlockCannotRemoveIt() throws Exception {
        try (Locus f = new Locus()) {
            Block foreign = mock(Block.class); f.present = foreign;
            f.expire();
            assertTrue(f.locus.isInvalid()); assertSame(foreign, f.present);
            assertEquals(0, f.drops);
            verify(f.world, never()).setBlockToAir(anyInt(), anyInt(), anyInt());
        }
    }

    @Test public void invalidatedTileDoesNotTickEvenIfStillRegistered() throws Exception {
        try (Locus f = new Locus()) {
            f.locus.invalidate(); f.expire();
            assertSame(f.block, f.present); assertEquals(0, f.drops);
            verify(f.world, never()).setBlockToAir(anyInt(), anyInt(), anyInt());
        }
    }

    @Test public void invalidStructureReturnsBlockAndStoredEyesExactlyOnce() throws Exception {
        try (Locus f = new Locus()) {
            Item eye = new Item(); TestRegistries.register(eye);
            f.locus.eyesContainingInventory.setInventorySlotContents(0, new ItemStack(eye, 1, 7));
            f.reenterDuringRemoval = true;
            f.expire(); f.expire(); f.locus.updateEntity();
            assertEquals(1, f.drops); assertSame(f.air, f.present); assertNull(f.registered);
            assertTrue(f.locus.isInvalid());
            assertEquals(1, f.spawned.size());
            assertSame(eye, f.spawned.get(0).getEntityItem().getItem());
            assertEquals(1, f.spawned.get(0).getEntityItem().stackSize);
            assertEquals(7, f.spawned.get(0).getEntityItem().getMetadata());
        }
    }

    @Test public void failedRemovalRetainsEyesAndRetriesWithoutDropping() throws Exception {
        try (Locus f = new Locus()) {
            ItemStack eye = new ItemStack(new Item());
            f.locus.eyesContainingInventory.setInventorySlotContents(0, eye);
            f.allowRemoval = false; f.expire(); f.expire();
            assertEquals(0, f.drops); assertTrue(f.spawned.isEmpty());
            assertFalse(f.locus.isInvalid()); assertSame(eye, f.locus.eyesContainingInventory.getStackInSlot(0));
            f.allowRemoval = true; f.expire();
            assertEquals(1, f.drops); assertEquals(1, f.spawned.size());
        }
    }

    @Test public void cachedForeignBlockIsNeverUsedForLocusDrops() throws Exception {
        try (Locus f = new Locus()) {
            Block foreign = mock(Block.class); f.present = foreign;
            assertSame(foreign, f.locus.getBlockType()); f.present = f.block;
            f.expire(); assertEquals(1, f.drops);
            verify(foreign, never()).dropBlockAsItemWithChance(any(), anyInt(), anyInt(), anyInt(), anyInt(), anyFloat(), anyInt());
        }
    }

    @Test public void validStructureKeepsUpdatingWithoutDismantling() throws Exception {
        try (Locus f = new Locus()) {
            BlockWoodenDevice wood = mock(BlockWoodenDevice.class);
            when(f.world.isAirBlock(anyInt(), anyInt(), anyInt())).thenReturn(true);
            when(f.world.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(call -> {
                int x = call.getArgument(0), y = call.getArgument(1), z = call.getArgument(2);
                if (x == 0 && y == 64 && z == 0) return f.present;
                if (y != 62) return f.air;
                if (x == 0 && z == 0) return Blocks.lapis_block;
                return Math.abs(x) <= 1 && Math.abs(z) <= 1 ? Blocks.water : wood;
            });
            when(f.world.getBlockMetadata(anyInt(), anyInt(), anyInt())).thenReturn(7);
            assertTrue(TileEntityEssentiaLocus.checkValidity(f.world, 0, 64, 0, false));
            f.expire(); f.locus.updateEntity();
            assertFalse(f.locus.isInvalid()); assertEquals(0, f.drops); assertSame(f.block, f.present);
            verify(f.world, never()).setBlockToAir(anyInt(), anyInt(), anyInt());
        }
    }

    private static class Locus implements AutoCloseable {
        final World world = world();
        final BlockEssentiaLocus block = mock(BlockEssentiaLocus.class, CALLS_REAL_METHODS);
        final ModBlockContainer previous = ModBlocks.essentiaLocus;
        final Block air = mock(Block.class);
        final TileEntityEssentiaLocus locus = new TileEntityEssentiaLocus();
        final List<EntityItem> spawned = new ArrayList<>();
        Block present = block;
        TileEntity registered = locus;
        boolean allowRemoval = true, reenterDuringRemoval;
        int drops;
        Locus() throws Exception {
            ModBlocks.essentiaLocus = block; locus.setWorldObj(world); locus.yCoord = 64;
            when(world.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(call -> (int)call.getArgument(1) == 64 ? present : air);
            when(world.getTileEntity(0, 64, 0)).thenAnswer(call -> registered);
            when(world.spawnEntityInWorld(any())).thenAnswer(call -> {spawned.add(call.getArgument(0)); return true;});
            doAnswer(call -> {
                if (registered != null) registered.invalidate();
                registered = null; return null;
            }).when(world).removeTileEntity(0, 64, 0);
            when(world.setBlockToAir(0, 64, 0)).thenAnswer(call -> {
                if (reenterDuringRemoval) locus.updateEntity();
                if (!allowRemoval || present == air) return false;
                present = air;
                // Chunk replaces the block first, then invokes breakBlock while its old tile is still available.
                block.breakBlock(world, 0, 64, 0, block, 0); return true;
            });
            doAnswer(call -> {drops++; return null;}).when(block)
                .dropBlockAsItemWithChance(eq(world), eq(0), eq(64), eq(0), anyInt(), anyFloat(), eq(0));
        }
        void expire() throws Exception {
            Field timer = TileEntityEssentiaLocus.class.getDeclaredField("ticksSinceValidityCheck"); timer.setAccessible(true);
            timer.setInt(locus, TileEntityEssentiaLocus.VALIDITY_CHECK_FREQUENCY); locus.updateEntity();
        }
        public void close() {ModBlocks.essentiaLocus = previous;}
    }

    private static class Spire implements AutoCloseable {
        final World world = world();
        final BlockThaumostaticRefuelerSpire block = mock(BlockThaumostaticRefuelerSpire.class, CALLS_REAL_METHODS);
        final ModBlockContainer previous = ModBlocks.thaumostaticSpire;
        final Item previousResource = ConfigItems.itemResource;
        final Block air = mock(Block.class);
        final Map<Integer, Integer> parts = new HashMap<>();
        int ingots, blockedY = -1;
        Spire() throws Exception {
            ModBlocks.thaumostaticSpire = block;
            ConfigItems.itemResource = new Item(); TestRegistries.register(ConfigItems.itemResource);
            when(world.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(call -> parts.containsKey(call.getArgument(1)) ? block : air);
            when(world.getBlockMetadata(anyInt(), anyInt(), anyInt())).thenAnswer(call -> parts.getOrDefault(call.getArgument(1), 0));
            when(world.setBlockToAir(anyInt(), anyInt(), anyInt())).thenAnswer(call -> {
                int y = call.getArgument(1); if (y == blockedY) return false;
                Integer metadata = parts.remove(y); if (metadata == null) return false;
                block.breakBlock(world, 0, y, 0, block, metadata); return true;
            });
            doAnswer(call -> {
                int y = call.getArgument(2), metadata = call.getArgument(4);
                for (ItemStack stack : block.getDrops(world, 0, y, 0, metadata, 0)) ingots += stack.stackSize;
                return null;
            }).when(block).dropBlockAsItemWithChance(eq(world), anyInt(), anyInt(), anyInt(), anyInt(), anyFloat(), anyInt());
        }
        public void close() {ModBlocks.thaumostaticSpire = previous; ConfigItems.itemResource = previousResource;}
    }

    private static World world() throws Exception {
        World world = mock(World.class);
        Field provider = World.class.getDeclaredField("provider"); provider.setAccessible(true); provider.set(world, new WorldProviderSurface());
        world.rand = new Random(1); return world;
    }
    private static void setRemote(World world) throws Exception {
        Field remote = World.class.getDeclaredField("isRemote"); remote.setAccessible(true); remote.setBoolean(world, true);
    }
}
