package tuhljin.automagy.audit;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.junit.Test;
import org.junit.BeforeClass;
import tuhljin.automagy.entities.EntityItemDelayedFall;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Regressions for the seven defects found in the 0.28.5 audit. */
public class AuditFixesTest {
    private static final java.util.Map<Field, Object> originals = new java.util.LinkedHashMap<>();
    @org.junit.AfterClass public static void restoreServices() throws Exception {
        for (java.util.Map.Entry<Field, Object> e : originals.entrySet()) putStatic(e.getKey(), e.getValue());
    }
    @Test public void controllerCannotRefundRelocatedUpperAtSavedHeight() throws Exception {
        World world = world();
        tuhljin.automagy.blocks.BlockThaumostaticRefuelerSpire spire = mock(tuhljin.automagy.blocks.BlockThaumostaticRefuelerSpire.class, CALLS_REAL_METHODS);
        tuhljin.automagy.blocks.ModBlockContainer previous = tuhljin.automagy.blocks.ModBlocks.thaumostaticSpire;
        Item previousResource = thaumcraft.common.config.ConfigItems.itemResource;
        tuhljin.automagy.blocks.ModBlocks.thaumostaticSpire = spire;
        Item resource = new Item(); tuhljin.automagy.TestRegistries.register(resource); thaumcraft.common.config.ConfigItems.itemResource = resource;
        java.util.Map<Integer, Integer> parts = new java.util.HashMap<>(); parts.put(70, 0); parts.put(71, 1);
        Block air = mock(Block.class); AtomicInteger ingots = new AtomicInteger();
        try {
            when(world.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(c -> parts.containsKey(c.getArgument(1)) ? spire : air);
            when(world.getBlockMetadata(anyInt(), anyInt(), anyInt())).thenAnswer(c -> parts.getOrDefault(c.getArgument(1), 0));
            when(world.setBlockToAir(anyInt(), anyInt(), anyInt())).thenAnswer(c -> {
                int y = c.getArgument(1); Integer metadata = parts.remove(y); if (metadata == null) return false;
                spire.breakBlock(world, 0, y, 0, spire, metadata); return true;
            });
            doAnswer(c -> {
                int y = c.getArgument(2), metadata = c.getArgument(4);
                for (ItemStack s : spire.getDrops(world, 0, y, 0, metadata, 0)) ingots.addAndGet(s.stackSize);
                return null;
            }).when(spire).dropBlockAsItemWithChance(eq(world), anyInt(), anyInt(), anyInt(), anyInt(), anyFloat(), anyInt());
            TileEntityThaumostaticController controller = new TileEntityThaumostaticController();
            controller.setWorldObj(world); controller.yCoord = 60; controller.spireY = 71; controller.active = true;
            controller.deactivate();
            assertEquals(0, ingots.get()); assertEquals(2, parts.size());
        } finally {tuhljin.automagy.blocks.ModBlocks.thaumostaticSpire = previous; thaumcraft.common.config.ConfigItems.itemResource = previousResource;}
    }

    @Test public void orphanedMawConversionCannotRepeatCompletedOutput() throws Exception {
        World world = world();
        tuhljin.automagy.blocks.ModBlockContainer previous = tuhljin.automagy.blocks.ModBlocks.hungryMaw;
        thaumcraft.common.CommonProxy previousProxy = thaumcraft.common.Thaumcraft.proxy;
        tuhljin.automagy.blocks.BlockMawHungry maw = mock(tuhljin.automagy.blocks.BlockMawHungry.class);
        tuhljin.automagy.TestRegistries.register(maw);
        java.lang.reflect.Method raw = Item.itemRegistry.getClass().getDeclaredMethod("addObjectRaw", int.class, String.class, Object.class); raw.setAccessible(true);
        Item item = new Item(); raw.invoke(Item.itemRegistry, Block.getIdFromBlock(maw), "audit:hungry_maw", item);
        tuhljin.automagy.blocks.ModBlocks.hungryMaw = maw;
        thaumcraft.common.Thaumcraft.proxy = mock(thaumcraft.common.CommonProxy.class);
        AtomicInteger emitted = new AtomicInteger();
        when(world.spawnEntityInWorld(any())).thenAnswer(c -> {emitted.addAndGet(((EntityItem)c.getArgument(0)).getEntityItem().stackSize); return true;});
        try {
            TileEntitySpecialProcess process = new TileEntitySpecialProcess(); process.setWorldObj(world); process.yCoord = 64;
            NBTTagCompound tag = new NBTTagCompound(); tag.setInteger("completedActions", 10); tag.setInteger("blocksConsumed", 2); process.readCustomNBT(tag);
            set(process, "ticksSinceAction", 10);
            for (int i = 0; i < 3; i++) process.updateEntity();
            assertEquals(0, emitted.get()); verify(world, never()).setBlockToAir(0, 64, 0); assertTrue(process.isInvalid());
        } finally {tuhljin.automagy.blocks.ModBlocks.hungryMaw = previous; thaumcraft.common.Thaumcraft.proxy = previousProxy;}
    }

    @BeforeClass public static void initializeWorldIndependentServices() throws Exception {
        replaceStatic(cpw.mods.fml.common.Loader.class.getDeclaredField("instance"), mock(cpw.mods.fml.common.Loader.class));
        Block water = mock(Block.class), lava = mock(Block.class);
        when(water.getUnlocalizedName()).thenReturn("tile.water"); when(lava.getUnlocalizedName()).thenReturn("tile.lava");
        replaceStatic(net.minecraft.init.Blocks.class.getDeclaredField("water"), water);
        replaceStatic(net.minecraft.init.Blocks.class.getDeclaredField("lava"), lava);
        replaceStatic(net.minecraft.init.Blocks.class.getDeclaredField("obsidian"), mock(Block.class));
        // Particle broadcasts are irrelevant to the operation under audit and require a running FML server.
        replaceStatic(tuhljin.automagy.network.PacketHandler.class.getDeclaredField("INSTANCE"), mock(cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper.class));
    }
    @Test public void orphanInventariumCannotDropForeignBlock() throws Exception {
        World world = world(); Block foreign = mock(Block.class), air = mock(Block.class);
        AtomicInteger present = new AtomicInteger(1), drops = new AtomicInteger();
        when(world.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(c -> (int)c.getArgument(1) == 64 && present.get() == 1 ? foreign : air);
        when(world.setBlockToAir(0, 64, 0)).thenAnswer(c -> present.getAndSet(0) == 1);
        doAnswer(c -> {drops.incrementAndGet(); return null;}).when(foreign).dropBlockAsItemWithChance(eq(world), eq(0), eq(64), eq(0), eq(0), anyFloat(), eq(0));
        TileEntityInventarium core = new TileEntityInventarium(); core.setWorldObj(world); core.yCoord = 64;
        set(core, "addedToMap", true); set(core, "constructStrikeOne", true);
        for (int i = 0; i < 3; i++) {set(core, "ticksSinceValidityCheck", TileEntityInventarium.VALIDITY_CHECK_FREQUENCY); core.updateEntity();}
        assertEquals(1, present.get()); assertEquals(0, drops.get()); assertTrue(core.isInvalid());
    }

    @Test public void cancelledHoverConversionKeepsOriginalStack() throws Exception {
        World world = world();
        Item stationary = new Item() {@Override public boolean onEntityItemUpdate(EntityItem item) {return true;}};
        EntityItemDelayedFall item = new EntityItemDelayedFall(world, 0, 64, 0, new ItemStack(stationary, 37), 0);
        item.onUpdate();
        verify(world).spawnEntityInWorld(any(EntityItem.class));
        assertFalse(item.isDead); assertEquals(37, item.getEntityItem().stackSize);
    }

    @Test public void dropHelperTransfersRejectedStackToPersistentQueue() throws Exception {
        World world = world(); Item resource = new Item(); tuhljin.automagy.TestRegistries.register(resource); ItemStack stack = new ItemStack(resource, 37);
        TjUtil.dropItemIntoWorld(stack, world, 0, 64, 0);
        assertEquals(0, stack.stackSize);
        NBTTagCompound saved = new NBTTagCompound(); tuhljin.automagy.lib.DeferredItemDrops.get(world).writeToNBT(saved);
        int pending = 0; for (int i = 0; i < saved.getTagList("Items", 10).tagCount(); i++) pending += ItemStack.loadItemStackFromNBT(saved.getTagList("Items", 10).getCompoundTagAt(i).getCompoundTag("Item")).stackSize;
        assertEquals(37, pending);
        verify(world, atLeastOnce()).spawnEntityInWorld(any(EntityItem.class));
    }

    @Test public void negativeWorkbenchRecipeIndexIsNormalized() throws Exception {
        TileEntityGolemWorkbench bench = new TileEntityGolemWorkbench();
        NBTTagCompound tag = new NBTTagCompound(); tag.setInteger("recipeIndex", -1);
        bench.readCustomNBT(tag);
        assertEquals(0, benchField(bench, "recipeIndex"));
        assertNull(bench.getCraftingResult(-1));
    }

    @Test public void workbenchReplacementSlotFromNbtIsNormalized() throws Exception {
        TileEntityGolemWorkbench bench = new TileEntityGolemWorkbench();
        NBTTagCompound tag = new NBTTagCompound(); tag.setIntArray("phReplaceIndex", new int[]{999, 1, 1});
        bench.readCustomNBT(tag); bench.progress = 1;
        java.lang.reflect.Method method = TileEntityGolemWorkbench.class.getDeclaredMethod("getFuzzyReplacement", int.class); method.setAccessible(true);
        assertNull(method.invoke(bench, 0));
    }

    @Test public void thirstTankCannotCreditFluidAfterFailedSourceRemoval() throws Exception {
        World world = world(); Block source = mock(Block.class);
        Fluid fluid = new Fluid("automagy_audit_fluid"); FluidRegistry.registerFluid(fluid);
        TileEntityThirstyTank tank = spy(new TileEntityThirstyTank()); tank.setWorldObj(world); tank.yCoord = 64;
        when(world.getBlock(anyInt(), anyInt(), anyInt())).thenReturn(source);
        doReturn(fluid).when(tank).getFluidIfValidBlock(source);
        doReturn(tank.new SourceLiquidResult(source, 0, 64, 1, 1, 1000)).when(tank)
            .findSourceLiquid(eq(source), anyInt(), anyInt(), anyInt(), any(ForgeDirection.class));
        setIn(TileEntityThirstyTank.class, tank, "cooldown", 0);
        tank.updateEntity();
        verify(world).setBlockToAir(0, 64, 1);
        assertEquals(0, tank.tank.getFluidAmount());
    }

    private static int benchField(TileEntityGolemWorkbench b, String name) throws Exception {
        Field f = TileEntityGolemWorkbench.class.getDeclaredField(name); f.setAccessible(true); return f.getInt(b);
    }
    private static void set(Object obj, String name, Object value) throws Exception {setIn(obj.getClass(), obj, name, value);}
    private static void setIn(Class<?> type, Object obj, String name, Object value) throws Exception {Field f = type.getDeclaredField(name); f.setAccessible(true); f.set(obj, value);}
    private static void replaceStatic(Field f, Object value) throws Exception {
        Class.forName(f.getDeclaringClass().getName());
        f.setAccessible(true); originals.put(f, f.get(null));
        putStatic(f, value);
    }
    private static void putStatic(Field f, Object value) throws Exception {
        Field singleton = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); singleton.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe)singleton.get(null);
        unsafe.putObject(unsafe.staticFieldBase(f), unsafe.staticFieldOffset(f), value);
    }
    private static World world() throws Exception {
        World world = mock(World.class); world.rand = new Random(1);
        Field storage = World.class.getDeclaredField("perWorldStorage"); storage.setAccessible(true); storage.set(world, new net.minecraft.world.storage.MapStorage(null));
        Field f = World.class.getDeclaredField("provider"); f.setAccessible(true); f.set(world, new WorldProviderSurface()); return world;
    }
}
