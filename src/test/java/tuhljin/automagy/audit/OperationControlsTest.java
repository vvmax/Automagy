package tuhljin.automagy.audit;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import tuhljin.automagy.TestRegistries;
import tuhljin.automagy.blocks.*;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.lib.DeferredItemDrops;
import tuhljin.automagy.tiles.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class OperationControlsTest {
    @BeforeClass public static void services() throws Exception {AuditFixesTest.initializeWorldIndependentServices();}
    @AfterClass public static void restore() throws Exception {AuditFixesTest.restoreServices();}

    @Test public void unsupportedInventariumDropsItsOwnBlockOnlyAfterRemovalOnce() throws Exception {
        try (Core f = new Core()) {
            f.expire(); f.expire();
            assertEquals(1, f.drops); assertTrue(f.tile.isInvalid()); assertFalse(f.present);
        }
    }

    @Test public void failedCoreRemovalKeepsCoreAndDoesNotDrop() throws Exception {
        try (Core f = new Core()) {
            f.allowRemoval = false; f.expire(); f.expire();
            assertTrue(f.present); assertFalse(f.tile.isInvalid()); assertEquals(0, f.drops);
            f.allowRemoval = true; f.expire(); assertEquals(1, f.drops);
        }
    }

    @Test public void staleCoreCannotUnregisterReplacementCoreLocation() throws Exception {
        try (Core f = new Core()) {
            TileEntityInventarium replacement = new TileEntityInventarium();
            f.registered = replacement;
            HashSet<BlockCoord> previous = TileEntityInventarium.coreLocations.get(0);
            HashSet<BlockCoord> coords = new HashSet<>(); coords.add(new BlockCoord(0, 64, 0));
            TileEntityInventarium.coreLocations.put(0, coords);
            try {
                f.tile.updateEntity();
                assertTrue(f.tile.isInvalid()); assertSame(replacement, f.registered);
                assertTrue(coords.contains(new BlockCoord(0, 64, 0))); assertEquals(0, f.drops);
            } finally {if (previous == null) TileEntityInventarium.coreLocations.remove(0); else TileEntityInventarium.coreLocations.put(0, previous);}
        }
    }

    @Test public void ordinaryControllerDeactivationRefundsNineExactlyOnce() throws Exception {
        try (Controller f = new Controller()) {
            f.tile.deactivate(); f.tile.deactivate();
            assertEquals(9, f.ingots); assertFalse(f.present); assertFalse(f.tile.active);
        }
    }

    @Test public void failedSpireRemovalDoesNotRefundAndRetainsControllerForRetry() throws Exception {
        try (Controller f = new Controller()) {
            f.allowRemoval = false; f.tile.deactivate();
            assertTrue(f.present); assertTrue(f.tile.active); assertEquals(0, f.ingots); assertEquals(70, f.tile.spireY);
            f.allowRemoval = true; f.tile.deactivate(); assertEquals(9, f.ingots);
        }
    }

    @Test public void ordinaryProcessReturnsEachPaidMawOnceDespiteReentrantTick() throws Exception {
        try (Process f = new Process()) {
            f.counts(10, 2); f.reenter = true; f.tick(); f.tick();
            assertEquals(2, f.emitted.get()); assertTrue(f.tile.isInvalid()); assertFalse(f.present);
        }
    }

    @Test public void failedProcessRemovalCanRetryWithoutRepeatingRewards() throws Exception {
        try (Process f = new Process()) {
            f.counts(10, 2); f.allowRemoval = false; f.tick(); f.tick();
            assertEquals(0, f.emitted.get()); assertFalse(f.tile.isInvalid());
            f.allowRemoval = true; f.tick(); f.tick(); assertEquals(2, f.emitted.get());
        }
    }

    @Test public void failedObsidianRemovalDoesNotCountAsConsumed() throws Exception {
        try (Process f = new Process()) {
            f.counts(0, 0);
            Block obsidian = net.minecraft.init.Blocks.obsidian;
            when(f.world.getBlock(0, 65, 0)).thenReturn(obsidian);
            f.tick(); assertEquals(0, f.saved().getInteger("blocksConsumed"));
            verify(f.world).setBlockToAir(0, 65, 0);
        }
    }

    @Test public void successfulObsidianRemovalAndProgressSurviveSaving() throws Exception {
        try (Process f = new Process()) {
            f.counts(0, 0);
            when(f.world.getBlock(0, 65, 0)).thenReturn(net.minecraft.init.Blocks.obsidian);
            when(f.world.setBlockToAir(0, 65, 0)).thenReturn(true);
            f.tick(); NBTTagCompound saved = f.saved();
            assertEquals(1, saved.getInteger("blocksConsumed")); assertEquals(1, saved.getInteger("completedActions"));
            TileEntitySpecialProcess restored = new TileEntitySpecialProcess(); restored.readCustomNBT(saved);
            NBTTagCompound roundTrip = new NBTTagCompound(); restored.writeCustomNBT(roundTrip);
            assertEquals(1, roundTrip.getInteger("blocksConsumed"));
        }
    }

    @Test public void rejectedProcessRewardsAreSavedForLaterDelivery() throws Exception {
        try (Process f = new Process()) {
            f.counts(10, 2); doReturn(false).when(f.world).spawnEntityInWorld(any());
            f.tick(); NBTTagCompound saved = new NBTTagCompound(); DeferredItemDrops.get(f.world).writeToNBT(saved);
            assertEquals(2, saved.getTagList("Items", 10).tagCount()); assertTrue(f.tile.isInvalid());
        }
    }

    @Test public void tankCreditsSuccessfulVanillaSourceOnce() throws Exception {
        Tank f = new Tank(false); when(f.world.setBlockToAir(0, 64, 1)).thenReturn(true);
        f.tick(); assertEquals(1000, f.tile.tank.getFluidAmount());
    }

    @Test public void tankCreditsActualDrainAmountInsteadOfSimulatedAmount() throws Exception {
        Tank f = new Tank(true);
        when(((BlockFluidBase)f.source).drain(f.world, 0, 64, 1, true)).thenAnswer(c -> {
            when(f.world.getBlock(0, 64, 1)).thenReturn(mock(Block.class)); return new FluidStack(f.fluid, 250);
        });
        f.tick(); assertEquals(250, f.tile.tank.getFluidAmount());
    }

    @Test public void unchangedSourceCannotBeCreditedEvenWhenDrainReturnsAFluidStack() throws Exception {
        Tank f = new Tank(true);
        when(((BlockFluidBase)f.source).drain(f.world, 0, 64, 1, true)).thenReturn(new FluidStack(f.fluid, 1000));
        f.tick(); assertEquals(0, f.tile.tank.getFluidAmount());
    }

    @Test public void realForgeClassicDrainCannotCreateFluidWhenItsWorldWriteFails() throws Exception {
        Tank f = new Tank(true);
        net.minecraftforge.fluids.BlockFluidClassic classic = mock(net.minecraftforge.fluids.BlockFluidClassic.class, CALLS_REAL_METHODS);
        set(net.minecraftforge.fluids.BlockFluidClassic.class, classic, "stack", new FluidStack(f.fluid, 1000));
        when(f.world.getBlock(anyInt(), anyInt(), anyInt())).thenReturn(classic);
        doReturn(f.fluid).when(f.tile).getFluidIfValidBlock(classic);
        doReturn(f.tile.new SourceLiquidResult(classic, 0, 64, 1, 1, 1000)).when(f.tile).findSourceLiquid(eq(classic), anyInt(), anyInt(), anyInt(), any());
        f.tick(); assertEquals(0, f.tile.tank.getFluidAmount());
        verify(f.world).setBlock(eq(0), eq(64), eq(1), any());
    }

    @Test public void failedFluidBlockDrainDoesNotCreateFluid() throws Exception {
        Tank f = new Tank(true); f.tick(); assertEquals(0, f.tile.tank.getFluidAmount());
    }

    @Test public void validWorkbenchIndicesArePreserved() throws Exception {
        TileEntityGolemWorkbench tile = new TileEntityGolemWorkbench(); NBTTagCompound tag = new NBTTagCompound();
        tag.setIntArray("phReplaceIndex", new int[]{1, 8, 4}); tile.readCustomNBT(tag);
        NBTTagCompound saved = new NBTTagCompound(); tile.writeCustomNBT(saved);
        assertArrayEquals(new int[]{1, 8, 4}, saved.getIntArray("phReplaceIndex"));
        tag.setIntArray("phReplaceIndex", new int[]{-1, 0, 9}); tile.readCustomNBT(tag); tile.writeCustomNBT(saved);
        assertArrayEquals(new int[]{1, 1, 1}, saved.getIntArray("phReplaceIndex"));
    }

    private static class Core implements AutoCloseable {
        final World world = world(); final TileEntityInventarium tile = new TileEntityInventarium();
        final ModBlockContainer block = mock(ModBlockContainer.class), previous = ModBlocks.inventariumCore;
        final Block air = mock(Block.class); TileEntity registered = tile; boolean present = true, allowRemoval = true; int drops;
        Core() throws Exception {
            ModBlocks.inventariumCore = block; tile.setWorldObj(world); tile.yCoord = 64;
            set(TileEntityInventarium.class, tile, "addedToMap", true); set(TileEntityInventarium.class, tile, "constructStrikeOne", true);
            when(world.getTileEntity(0, 64, 0)).thenAnswer(c -> registered);
            when(world.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(c -> (int)c.getArgument(1) == 64 && present ? block : air);
            when(world.setBlockToAir(0, 64, 0)).thenAnswer(c -> {tile.updateEntity(); if (!allowRemoval || !present) return false; present = false; registered = null; tile.invalidate(); return true;});
            doAnswer(c -> {assertFalse(present); drops++; return null;}).when(block).dropBlockAsItemWithChance(eq(world), anyInt(), anyInt(), anyInt(), anyInt(), anyFloat(), anyInt());
        }
        void expire() throws Exception {set(TileEntityInventarium.class, tile, "ticksSinceValidityCheck", 80); tile.updateEntity();}
        public void close() {ModBlocks.inventariumCore = previous;}
    }

    private static class Controller implements AutoCloseable {
        final World world = world(); final TileEntityThaumostaticController tile = new TileEntityThaumostaticController();
        final ModBlockContainer block = mock(ModBlockContainer.class), previous = ModBlocks.thaumostaticSpire;
        final Block air = mock(Block.class); boolean present = true, allowRemoval = true; int ingots;
        Controller() throws Exception {
            ModBlocks.thaumostaticSpire = block; tile.setWorldObj(world); tile.yCoord = 60; tile.spireY = 70; tile.active = true;
            when(world.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(c -> (int)c.getArgument(1) == 70 && present ? block : air);
            when(world.setBlockToAir(0, 70, 0)).thenAnswer(c -> {if (!allowRemoval || !present) return false; present = false; return true;});
            doAnswer(c -> {assertFalse(present); ingots += 9; tile.deactivate(); return null;}).when(block).dropBlockAsItemWithChance(eq(world), anyInt(), anyInt(), anyInt(), anyInt(), anyFloat(), anyInt());
        }
        public void close() {ModBlocks.thaumostaticSpire = previous;}
    }

    private static class Process implements AutoCloseable {
        final World world = world(); final TileEntitySpecialProcess tile = new TileEntitySpecialProcess();
        final ModBlockContainer previous = ModBlocks.specialProcessBlock, previousMaw = ModBlocks.hungryMaw;
        final ModBlockContainer block = mock(ModBlockContainer.class); final Block air = mock(Block.class);
        final thaumcraft.common.CommonProxy previousProxy = thaumcraft.common.Thaumcraft.proxy;
        boolean present = true, allowRemoval = true, reenter; final AtomicInteger emitted = new AtomicInteger();
        Process() throws Exception {
            ModBlocks.specialProcessBlock = block; ModBlocks.hungryMaw = mock(BlockMawHungry.class);
            TestRegistries.register(ModBlocks.hungryMaw);
            java.lang.reflect.Method raw = Item.itemRegistry.getClass().getDeclaredMethod("addObjectRaw", int.class, String.class, Object.class); raw.setAccessible(true);
            raw.invoke(Item.itemRegistry, Block.getIdFromBlock(ModBlocks.hungryMaw), "controls:maw", new Item());
            thaumcraft.common.Thaumcraft.proxy = mock(thaumcraft.common.CommonProxy.class);
            tile.setWorldObj(world); tile.yCoord = 64;
            when(world.getTileEntity(0, 64, 0)).thenReturn(tile);
            when(world.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(c -> (int)c.getArgument(1) == 64 && present ? block : air);
            when(world.setBlockToAir(0, 64, 0)).thenAnswer(c -> {if (reenter) tile.updateEntity(); if (!present || !allowRemoval) return false; present = false; return true;});
            when(world.spawnEntityInWorld(any())).thenAnswer(c -> {emitted.addAndGet(((EntityItem)c.getArgument(0)).getEntityItem().stackSize); return true;});
        }
        void counts(int steps, int consumed) {NBTTagCompound tag = new NBTTagCompound(); tag.setInteger("completedActions", steps); tag.setInteger("blocksConsumed", consumed); tile.readCustomNBT(tag);}
        void tick() throws Exception {set(TileEntitySpecialProcess.class, tile, "ticksSinceAction", 10); tile.updateEntity();}
        NBTTagCompound saved() {NBTTagCompound tag = new NBTTagCompound(); tile.writeCustomNBT(tag); return tag;}
        public void close() {ModBlocks.specialProcessBlock = previous; ModBlocks.hungryMaw = previousMaw; thaumcraft.common.Thaumcraft.proxy = previousProxy;}
    }

    private static class Tank {
        final World world = world(); final TileEntityThirstyTank tile = spy(new TileEntityThirstyTank()); final Block source;
        final Fluid fluid;
        Tank(boolean forgeFluid) throws Exception {
            if (FluidRegistry.getFluid("automagy_controls") == null) FluidRegistry.registerFluid(new Fluid("automagy_controls"));
            fluid = FluidRegistry.getFluid("automagy_controls");
            source = forgeFluid ? mock(BlockFluidBase.class) : mock(Block.class); tile.setWorldObj(world); tile.yCoord = 64;
            when(world.getBlock(anyInt(), anyInt(), anyInt())).thenReturn(source);
            doReturn(fluid).when(tile).getFluidIfValidBlock(source);
            doReturn(tile.new SourceLiquidResult(source, 0, 64, 1, 1, 1000)).when(tile).findSourceLiquid(eq(source), anyInt(), anyInt(), anyInt(), any());
        }
        void tick() throws Exception {set(TileEntityThirstyTank.class, tile, "cooldown", 0); tile.updateEntity();}
    }

    private static void set(Class<?> type, Object object, String name, Object value) throws Exception {Field f = type.getDeclaredField(name); f.setAccessible(true); f.set(object, value);}
    private static World world() throws Exception {
        World world = mock(World.class); world.rand = new Random(1);
        Field provider = World.class.getDeclaredField("provider"); provider.setAccessible(true); provider.set(world, new WorldProviderSurface());
        Field storage = World.class.getDeclaredField("perWorldStorage"); storage.setAccessible(true); storage.set(world, new MapStorage(null)); return world;
    }
}
