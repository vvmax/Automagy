package tuhljin.automagy.catalog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.block.Block;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import org.junit.Test;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.wands.ItemWandCasting;
import tuhljin.automagy.TestRegistries;
import tuhljin.automagy.blocks.BlockThaumostaticRefuelerSpire;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.entities.EntityAvaricePearl;
import tuhljin.automagy.gui.ContainerPortableArcaneCrafting;
import tuhljin.automagy.items.InventoryForItem;
import tuhljin.automagy.items.ItemFocusCrafting;
import tuhljin.automagy.tiles.TileEntityEssentiaLocus;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Regression checks for the five scenarios in the user-supplied catalog. */
public class CatalogVerificationTest {
    @Test public void catalog142_fourRealHoppersAndSixteenPearlsConserve64Items() throws Exception {
        World world = world(); List<Entity> snapshot = new ArrayList<>(); List<TileEntityHopper> hoppers = new ArrayList<>();
        Item item = new Item();
        for (int i = 0; i < 4; i++) {
            EntityItem dropped = new EntityItem(null, 0, 0, 0, new ItemStack(item, 16)); snapshot.add(dropped);
            TileEntityHopper hopper = new TileEntityHopper(); hoppers.add(hopper);
            assertTrue(TileEntityHopper.func_145898_a(hopper, dropped));
        }
        when(world.selectEntitiesWithinAABB(eq(Entity.class), any(), any())).thenReturn(snapshot);
        for (int i = 0; i < 16; i++) {Pearl pearl = new Pearl(); pearl.worldObj = world; pearl.impact(); assertEquals(0, pearl.stacksGrabbed);}
        int total = 0;
        for (TileEntityHopper hopper : hoppers) for (int s = 0; s < hopper.getSizeInventory(); s++) if (hopper.getStackInSlot(s) != null) total += hopper.getStackInSlot(s).stackSize;
        for (Entity entity : snapshot) if (!entity.isDead) total += ((EntityItem)entity).getEntityItem().stackSize;
        assertEquals(64, total);
    }

    @Test public void catalog514_movedUpperHalvesCannotCreateIngots() throws Exception {
        try (SpireFixture f = new SpireFixture()) {
            f.blocks.put(70, 1); f.blocks.put(71, 1);
            assertTrue(f.spire.getDrops(f.world, 0, 70, 0, 1, 0).isEmpty());
            assertTrue(f.spire.getDrops(f.world, 0, 71, 0, 1, 0).isEmpty());
            f.world.setBlockToAir(0, 71, 0);
            assertEquals("Two zero-drop upper halves must not refund a base", 0, f.ingots.get());
            assertEquals(Integer.valueOf(1), f.blocks.get(70));
        }
    }

    @Test public void catalog514_controlOrdinaryUpperBreakReturnsExactlyNineIngots() throws Exception {
        try (SpireFixture f = new SpireFixture()) {
            f.blocks.put(70, 0); f.blocks.put(71, 1);
            f.world.setBlockToAir(0, 71, 0);
            assertEquals(9, f.ingots.get()); assertTrue(f.blocks.isEmpty());
        }
    }

    @Test public void catalog514_controlOrdinaryLowerBreakDoesNotDoubleRefund() throws Exception {
        try (SpireFixture f = new SpireFixture()) {
            f.blocks.put(70, 0); f.blocks.put(71, 1);
            for (ItemStack stack : f.spire.getDrops(f.world, 0, 70, 0, 0, 0)) f.ingots.addAndGet(stack.stackSize);
            f.world.setBlockToAir(0, 70, 0);
            assertEquals(9, f.ingots.get()); assertTrue(f.blocks.isEmpty());
        }
    }

    @Test public void catalog664_orphanLocusCannotDropOrRemoveForeignBlocks() throws Exception {
        World world = world(); Block target = mock(Block.class); Block air = mock(Block.class);
        AtomicInteger present = new AtomicInteger(1); AtomicInteger drops = new AtomicInteger();
        when(world.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(call -> {
            int y = call.getArgument(1); return y == 64 && present.get() > 0 ? target : air;
        });
        // A moved/orphaned TileEntity is not the tile registered at these coordinates.
        assertNull(world.getTileEntity(0, 64, 0));
        when(world.isAirBlock(anyInt(), anyInt(), anyInt())).thenReturn(false);
        doAnswer(call -> {drops.incrementAndGet(); return null;}).when(target)
            .dropBlockAsItemWithChance(eq(world), eq(0), eq(64), eq(0), eq(0), anyFloat(), eq(0));
        when(world.setBlockToAir(0, 64, 0)).thenAnswer(call -> present.getAndSet(0) > 0);
        TileEntityEssentiaLocus locus = new TileEntityEssentiaLocus(); locus.setWorldObj(world); locus.yCoord = 64;
        Field timer = TileEntityEssentiaLocus.class.getDeclaredField("ticksSinceValidityCheck"); timer.setAccessible(true);
        for (int i = 0; i < 3; i++) {
            timer.setInt(locus, TileEntityEssentiaLocus.VALIDITY_CHECK_FREQUENCY);
            locus.updateEntity();
        }
        assertEquals(1, present.get()); assertEquals(0, drops.get());
        assertTrue(locus.isInvalid());
        verify(world, never()).setBlockToAir(anyInt(), anyInt(), anyInt());
    }

    @Test public void catalog563_dispenserHopperAndPearlsConserve64TaggedItems() throws Exception {
        dispenserBalance(false);
    }

    @Test public void catalog563_hopperConsumptionDuringPearlQueryStillConserves64Items() throws Exception {
        dispenserBalance(true);
    }

    @Test public void catalog710_switchBeforeClickCannotDuplicateStoredStack() throws Exception {
        FocusFixture f = new FocusFixture(); FocusContainer old = f.open();
        assertEquals(64, old.craftingInv.getStackInSlot(0).stackSize);
        f.swap();
        old.slotClick(2, 0, 0, f.player);
        assertNull(f.player.inventory.getItemStack());
        assertFalse(old.canInteractWith(f.player));
        old.onContainerClosed(f.player);
        assertEquals(64, f.total());
        f.roundTrip(); assertEquals(64, f.total());
    }

    @Test public void catalog710_extractBeforeSwitchAlsoConservesBothStoragesAndCursor() throws Exception {
        FocusFixture f = new FocusFixture(); FocusContainer old = f.open();
        old.slotClick(2, 0, 0, f.player);
        assertEquals(64, f.player.inventory.getItemStack().stackSize);
        assertEquals(7, f.player.inventory.getItemStack().getMetadata());
        assertEquals("catalog710", f.player.inventory.getItemStack().getTagCompound().getString("marker"));
        f.swap(); old.onContainerClosed(f.player);
        assertEquals(64, f.total()); f.roundTrip(); assertEquals(64, f.total());
    }

    @Test public void catalog710_halfStackOnCursorSurvivesSwitchAndTransferToTheOtherFocus() throws Exception {
        FocusFixture f = new FocusFixture(); FocusContainer old = f.open();
        old.slotClick(2, 1, 0, f.player); assertEquals(32, f.player.inventory.getItemStack().stackSize);
        f.swap(); old.slotClick(2, 0, 0, f.player);
        assertEquals(64, f.total());
        FocusContainer next = f.open(); next.slotClick(2, 0, 0, f.player);
        old.onContainerClosed(f.player); next.onContainerClosed(f.player);
        assertNull(f.player.inventory.getItemStack()); assertEquals(64, f.total());
        f.roundTrip(); assertEquals(64, f.total());
    }

    @Test public void catalog710_openingNextContainerBeforeClosingPreviousDoesNotCopyContents() throws Exception {
        FocusFixture f = new FocusFixture(); FocusContainer old = f.open();
        f.swap(); FocusContainer next = f.open();
        old.slotClick(2, 0, 0, f.player);
        old.onContainerClosed(f.player);
        assertTrue(next.canInteractWith(f.player)); assertNull(next.craftingInv.getStackInSlot(0));
        next.onContainerClosed(f.player);
        f.swap(); FocusContainer restored = f.open();
        assertEquals(64, restored.craftingInv.getStackInSlot(0).stackSize);
        restored.slotClick(2, 0, 0, f.player); restored.onContainerClosed(f.player);
        assertEquals(64, f.total());
    }

    @Test public void catalog710_twentyBidirectionalSwitchesKeepAClosedBalance() throws Exception {
        FocusFixture f = new FocusFixture();
        for (int i = 0; i < 20; i++) {
            FocusContainer old = f.open(); f.swap();
            old.slotClick(2, 0, 0, f.player); old.onContainerClosed(f.player);
            assertEquals(64, f.total());
            f.roundTrip(); assertEquals(64, f.total());
        }
    }

    private void dispenserBalance(boolean consumeDuringQuery) throws Exception {
        World world = world(); List<EntityItem> spawned = new ArrayList<>(); InventoryBasic hopper = new InventoryBasic("hopper", false, 5);
        ItemStack sourceStack = new ItemStack(new Item(), 64, 7); sourceStack.setTagCompound(new NBTTagCompound()); sourceStack.getTagCompound().setString("marker", "catalog563");
        when(world.spawnEntityInWorld(any())).thenAnswer(call -> {spawned.add(call.getArgument(0)); return true;});
        IBlockSource source = mock(IBlockSource.class); when(source.getWorld()).thenReturn(world); when(source.getBlockMetadata()).thenReturn(3);
        when(source.getX()).thenReturn(0.5); when(source.getY()).thenReturn(64.5); when(source.getZ()).thenReturn(0.5);
        ExposedDispenser dispenser = new ExposedDispenser();
        when(world.selectEntitiesWithinAABB(eq(Entity.class), any(), any())).thenAnswer(call -> {
            net.minecraft.command.IEntitySelector selector = call.getArgument(2); List<Entity> snapshot = new ArrayList<>();
            for (EntityItem item : spawned) if (selector.isEntityApplicable(item)) snapshot.add(item);
            if (consumeDuringQuery) for (EntityItem item : spawned) if (!item.isDead) TileEntityHopper.func_145898_a(hopper, item);
            return snapshot;
        });
        for (int i = 0; i < 64; i++) {
            dispenser.emit(source, sourceStack);
            if (!consumeDuringQuery) assertTrue(TileEntityHopper.func_145898_a(hopper, spawned.get(i)));
            for (int p = 0; p < 3; p++) {Pearl pearl = new Pearl(); pearl.worldObj = world; pearl.impact(); assertEquals(0, pearl.stacksGrabbed);}
            int total = sourceStack.stackSize;
            for (int s = 0; s < hopper.getSizeInventory(); s++) if (hopper.getStackInSlot(s) != null) total += hopper.getStackInSlot(s).stackSize;
            for (EntityItem item : spawned) if (!item.isDead) total += item.getEntityItem().stackSize;
            assertEquals(64, total);
        }
        assertEquals(64, hopper.getStackInSlot(0).stackSize);
        assertEquals(7, hopper.getStackInSlot(0).getMetadata());
        assertEquals("catalog563", hopper.getStackInSlot(0).getTagCompound().getString("marker"));
    }

    public static class FocusContainer extends ContainerPortableArcaneCrafting {
        FocusContainer(EntityPlayer player) {super(player, 4);}
        // This scenario stores inputs without crafting a result; recipe lookup is outside the check.
        @Override public void onCraftMatrixChanged(IInventory inv) { }
    }
    private static class FocusFixture {
        final EntityPlayer player = mock(EntityPlayer.class);
        final ItemWandCasting wandItem = mock(ItemWandCasting.class, CALLS_REAL_METHODS);
        final ItemFocusCrafting focusItem = mock(ItemFocusCrafting.class, CALLS_REAL_METHODS);
        final ItemStack wand;
        final List<ItemStack> dropped = new ArrayList<>();
        ItemStack detached;
        FocusFixture() throws Exception {
            TestRegistries.register(wandItem); TestRegistries.register(focusItem);
            player.worldObj = world(); player.inventory = new InventoryPlayer(player);
            when(player.dropPlayerItemWithRandomChoice(any(), anyBoolean())).thenAnswer(call -> {dropped.add(((ItemStack)call.getArgument(0)).copy()); return null;});
            wand = new ItemStack(wandItem); player.inventory.setInventorySlotContents(4, wand);
            ItemStack a = new ItemStack(focusItem), b = new ItemStack(focusItem);
            assertTrue(focusItem.applyUpgrade(a, ItemFocusCrafting.storage, 3)); assertTrue(focusItem.applyUpgrade(b, ItemFocusCrafting.storage, 3));
            a.getTagCompound().setString("catalogFocus", "A"); b.getTagCompound().setString("catalogFocus", "B");
            Item material = new Item(); TestRegistries.register(material); ItemStack input = new ItemStack(material, 64, 7);
            input.setTagCompound(new NBTTagCompound()); input.getTagCompound().setString("marker", "catalog710");
            InventoryForItem storage = new InventoryForItem(a, "test", 11); storage.setInventorySlotContents(0, input); storage.saveToItemStack(a);
            wandItem.setFocus(wand, a); detached = b;
        }
        FocusContainer open() {FocusContainer container = new FocusContainer(player); assertTrue(container.hasStorage); player.openContainer = container; return container;}
        void swap() {ItemStack old = wandItem.getFocusItem(wand).copy(); wandItem.setFocus(wand, detached); detached = old;}
        int stored(ItemStack focus) {InventoryForItem inv = new InventoryForItem(focus, "test", 11); int total = 0; for (int i = 0; i < 9; i++) if (inv.getStackInSlot(i) != null) total += inv.getStackInSlot(i).stackSize; return total;}
        int total() {ItemStack cursor = player.inventory.getItemStack(); int onGround = 0; for (ItemStack stack : dropped) onGround += stack.stackSize; return stored(wandItem.getFocusItem(wand)) + stored(detached) + (cursor == null ? 0 : cursor.stackSize) + onGround;}
        void roundTrip() {
            ItemStack restored = ItemStack.loadItemStackFromNBT(wand.writeToNBT(new NBTTagCompound())); wand.setTagCompound(restored.getTagCompound());
            detached = ItemStack.loadItemStackFromNBT(detached.writeToNBT(new NBTTagCompound()));
            ItemStack cursor = player.inventory.getItemStack(); if (cursor != null) player.inventory.setItemStack(ItemStack.loadItemStackFromNBT(cursor.writeToNBT(new NBTTagCompound())));
            for (int i = 0; i < dropped.size(); i++) dropped.set(i, ItemStack.loadItemStackFromNBT(dropped.get(i).writeToNBT(new NBTTagCompound())));
        }
    }
    private static class SpireFixture implements AutoCloseable {
        final World world = world();
        final BlockThaumostaticRefuelerSpire spire = mock(BlockThaumostaticRefuelerSpire.class, CALLS_REAL_METHODS);
        final tuhljin.automagy.blocks.ModBlockContainer previousSpire = ModBlocks.thaumostaticSpire;
        final Item previousResource = ConfigItems.itemResource;
        final Block air = mock(Block.class);
        final Map<Integer, Integer> blocks = new HashMap<>();
        final AtomicInteger ingots = new AtomicInteger();
        SpireFixture() throws Exception {
            ModBlocks.thaumostaticSpire = spire;
            Item resource = new Item(); TestRegistries.register(resource); ConfigItems.itemResource = resource;
            when(world.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(call -> blocks.containsKey(call.getArgument(1)) ? spire : air);
            when(world.getBlockMetadata(anyInt(), anyInt(), anyInt())).thenAnswer(call -> blocks.getOrDefault(call.getArgument(1), 0));
            doAnswer(call -> {
                int x = call.getArgument(1), y = call.getArgument(2), z = call.getArgument(3), metadata = call.getArgument(4);
                for (ItemStack drop : spire.getDrops(world, x, y, z, metadata, 0)) ingots.addAndGet(drop.stackSize);
                return null;
            }).when(spire).dropBlockAsItemWithChance(eq(world), anyInt(), anyInt(), anyInt(), anyInt(), anyFloat(), anyInt());
            when(world.setBlockToAir(anyInt(), anyInt(), anyInt())).thenAnswer(call -> {
                int y = call.getArgument(1); Integer metadata = blocks.remove(y);
                if (metadata == null) return false;
                spire.breakBlock(world, 0, y, 0, spire, metadata); return true;
            });
        }
        public void close() {ModBlocks.thaumostaticSpire = previousSpire; ConfigItems.itemResource = previousResource;}
    }
    private static class ExposedDispenser extends BehaviorDefaultDispenseItem {
        ItemStack emit(IBlockSource source, ItemStack stack) {return super.dispenseStack(source, stack);}
    }
    private static class Pearl extends EntityAvaricePearl {
        Pearl() {super(null); metadata = 0; noPearlOnImpact = true;}
        @Override public void onEntityUpdate() { }
        void impact() {onImpact(null);}
    }
    private static World world() throws Exception {
        World world = mock(World.class); Field provider = World.class.getDeclaredField("provider"); provider.setAccessible(true); provider.set(world, new WorldProviderSurface());
        world.rand = new Random(1); return world;
    }
}
