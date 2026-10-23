package tuhljin.automagy.regression;

import java.lang.reflect.Field;
import java.util.Random;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import org.junit.Test;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.wands.ItemWandCasting;
import tuhljin.automagy.TestRegistries;
import tuhljin.automagy.gui.ContainerAutoHandMirror;
import tuhljin.automagy.gui.ContainerScribe;
import tuhljin.automagy.gui.ContainerVisReader;
import tuhljin.automagy.items.ItemAutoHandMirror;
import tuhljin.automagy.items.ItemFocusCrafting;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.lib.inventory.MappedItemsOrderedByTimeAdded;
import tuhljin.automagy.lib.inventory.MappedItemsWithDestinations;
import tuhljin.automagy.lib.inventory.MirrorspaceItems;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.network.MessageGUIInventariumRequest;
import tuhljin.automagy.network.MessageGUIVisReader;
import tuhljin.automagy.network.MessageKeyPressed;
import tuhljin.automagy.tiles.IInventariumBookLinksMirror;
import tuhljin.automagy.tiles.TileEntityInventarium;
import tuhljin.automagy.tiles.TileEntityMirrorMultiDest;
import tuhljin.automagy.tiles.TileEntityVisReader;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class AuditRegressionTest {
    @Test public void inventariumRejectsRequestWithoutAnOpenBook() throws Exception {
        Fixture f = new Fixture();
        MessageGUIInventariumRequest packet = f.request(64);
        packet.onMessage(packet, f.context);
        verify(f.victim, never()).receiveRequestFromClient(any(), anyBoolean(), any());
        verify(f.world, never()).getTileEntity(anyInt(), anyInt(), anyInt());
    }

    @Test public void validBookStillRequestsFromItsRemoteCoreAndOwnDestination() throws Exception {
        Fixture f = new Fixture();
        f.openBook();
        MessageGUIInventariumRequest packet = f.request(64);
        packet.onMessage(packet, f.context);
        verify(f.victim).receiveRequestFromClient(same(packetStack(packet)), eq(false), same(f.book));
    }

    @Test public void bookCannotAddressAnotherCoreOrAnotherRequester() throws Exception {
        Fixture f = new Fixture(); f.openBook();
        MessageGUIInventariumRequest otherCore = new MessageGUIInventariumRequest(0, 10001, 64, 10000, f.stack(64), false, 0, 1, 64, 1);
        otherCore.onMessage(otherCore, f.context);
        MessageGUIInventariumRequest otherBook = new MessageGUIInventariumRequest(0, 10000, 64, 10000, f.stack(64), false, 0, 2, 64, 1);
        otherBook.onMessage(otherBook, f.context);
        verify(f.victim, never()).receiveRequestFromClient(any(), anyBoolean(), any());
        verify(f.world, never()).getTileEntity(anyInt(), anyInt(), anyInt());
    }

    @Test public void unusableOrReadOnlyBookCannotChangeRequests() throws Exception {
        Fixture f = new Fixture(); f.openBook();
        when(f.book.isUseableByPlayer(f.player)).thenReturn(false);
        MessageGUIInventariumRequest packet = f.request(64);
        packet.onMessage(packet, f.context);
        when(f.book.isUseableByPlayer(f.player)).thenReturn(true);
        when(f.book.canRequestItems()).thenReturn(false);
        packet.onMessage(packet, f.context);
        verify(f.victim, never()).receiveRequestFromClient(any(), anyBoolean(), any());
    }

    @Test public void unloadedCoreIsNotResolvedByTheRequestHandler() throws Exception {
        Fixture f = new Fixture(); f.openBook();
        when(f.world.blockExists(10000, 64, 10000)).thenReturn(false);
        MessageGUIInventariumRequest packet = f.request(64);
        packet.onMessage(packet, f.context);
        verify(f.world, never()).getTileEntity(anyInt(), anyInt(), anyInt());
    }

    @Test public void validCancellationStillReachesTheCore() throws Exception {
        Fixture f = new Fixture(); f.openBook();
        MessageGUIInventariumRequest packet = f.request(-64);
        packet.onMessage(packet, f.context);
        verify(f.victim).receiveRequestFromClient(same(packetStack(packet)), eq(false), same(f.book));
    }

    @Test public void invalidRequestCountsAndItemsAreRejected() throws Exception {
        Fixture f = new Fixture(); f.openBook();
        for (int count : new int[] {0, Integer.MIN_VALUE}) {
            MessageGUIInventariumRequest packet = f.request(count);
            packet.onMessage(packet, f.context);
        }
        MessageGUIInventariumRequest packet = new MessageGUIInventariumRequest(0, 10000, 64, 10000, new ItemStack((Item)null), false);
        packet.onMessage(packet, f.context);
        verify(f.victim, never()).receiveRequestFromClient(any(), anyBoolean(), any());
    }

    @Test public void overflowingRequestDoesNotPartiallyChangeDestinationAccounting() throws Exception {
        Fixture f = new Fixture(); f.openBook();
        ItemStack stack = f.stack(Integer.MAX_VALUE);
        f.victim.getRequestedItems().add(stack);
        stack.stackSize = 1;
        MessageGUIInventariumRequest packet = new MessageGUIInventariumRequest(0, 10000, 64, 10000, stack, false, 0, 1, 64, 1);
        packet.onMessage(packet, f.context);
        verify(f.victim, never()).receiveRequestFromClient(any(), anyBoolean(), any());
    }

    @Test public void invalidNetworkDirectionIsIgnoredAndDoesNotCrashTheTick() throws Exception {
        Fixture f = new Fixture();
        TileEntityVisReader tile = f.reader();
        ContainerVisReader container = mock(ContainerVisReader.class);
        when(container.getTileEntity()).thenReturn(tile); f.player.openContainer = container;
        MessageGUIVisReader packet = new MessageGUIVisReader(2, 123);
        packet.onMessage(packet, f.context);
        assertEquals(6, tile.outputDirs[0]);
        tile.updateEntity();
    }

    @Test public void persistedInvalidReaderSettingsAreRepaired() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setIntArray("outputDir", new int[] {123, -1, 5, 6});
        tag.setIntArray("modes", new int[] {99, -1, 4, 1});
        tag.setIntArray("capacity", new int[] {-1, 1000, 0, 64});
        TileEntityVisReader tile = new TileEntityVisReader(); tile.readCustomNBT(tag);
        assertArrayEquals(new int[] {6, 6, 5, 6}, tile.outputDirs);
        assertArrayEquals(new int[] {0, 0, 4, 1}, tile.modes);
        assertArrayEquals(new int[] {0, 1000, 0, 64}, tile.capacities);
    }

    @Test public void validReaderSettingsStillWork() throws Exception {
        Fixture f = new Fixture(); TileEntityVisReader tile = f.reader();
        tile.receiveGUIMessage(0, 2, 5);
        tile.receiveGUIMessage(1, 0, 4);
        tile.receiveGUIMessage(1, 1, 1000);
        assertEquals(5, tile.outputDirs[0]); assertEquals(4, tile.modes[1]); assertEquals(1000, tile.capacities[1]);
        tile.updateEntity();
    }

    @Test public void removingDestinationRemovesOnlyItsRequestsAcrossAllItems() throws Exception {
        ItemStack a = new ItemStack(new Item(), 64), b = new ItemStack(new Item(), 12);
        TestRegistries.register(a.getItem()); TestRegistries.register(b.getItem());
        MappedItemsWithDestinations map = new MappedItemsWithDestinations("test");
        WorldSpecificCoordinates removed = new WorldSpecificCoordinates(0, 1, 64, 1), kept = new WorldSpecificCoordinates(0, 2, 64, 1);
        map.add(removed, a); map.add(removed, b); map.add(kept, a);
        assertEquals(2, map.size());
        map.removeGroup(removed);
        assertEquals(0, map.get(removed, new HashableItemWithoutSize(a)));
        assertEquals(0, map.get(removed, new HashableItemWithoutSize(b)));
        assertEquals(64, map.get(kept, new HashableItemWithoutSize(a)));
        assertEquals(1, map.size());
    }

    @Test public void cancellingAcrossSeveralDestinationsDoesNotInvalidateIterator() {
        Item item = new Item(); ItemStack ten = new ItemStack(item, 10);
        MappedItemsWithDestinations map = new MappedItemsWithDestinations("test");
        for (int x = 0; x < 3; x++) map.add(new WorldSpecificCoordinates(0, x, 64, 1), ten);
        map.subtractFromAnyGroup(new ItemStack(item, 30));
        assertTrue(map.isEmpty());
    }

    @Test public void requestCountsSaturateAndCanStillBeCancelledAndSaved() throws Exception {
        Item item = new Item(); TestRegistries.register(item);
        ItemStack request = new ItemStack(item, Integer.MAX_VALUE);
        MappedItemsOrderedByTimeAdded map = new MappedItemsOrderedByTimeAdded("test");
        map.add(request, false); map.add(new ItemStack(item, 1), false);
        assertEquals(Integer.MAX_VALUE, map.get(new HashableItemWithoutSize(request)));
        NBTTagCompound tag = new NBTTagCompound(); map.writeToNBT(tag);
        MappedItemsOrderedByTimeAdded restored = new MappedItemsOrderedByTimeAdded("test"); restored.readFromNBT(tag);
        restored.subtract(new ItemStack(item, Integer.MAX_VALUE));
        assertTrue(restored.isEmpty());
    }

    @Test public void groupedCountsDoNotOverflow() {
        Item item = new Item(); WorldSpecificCoordinates dest = new WorldSpecificCoordinates(0, 1, 64, 1);
        MappedItemsWithDestinations map = new MappedItemsWithDestinations("test");
        map.add(dest, new ItemStack(item, Integer.MAX_VALUE)); map.add(dest, new ItemStack(item, 1));
        assertEquals(Integer.MAX_VALUE, map.get(dest, new HashableItemWithoutSize(new ItemStack(item))));
    }

    @Test public void canceledSpawnKeepsTheMirrorBufferAndItsNbt() throws Exception {
        Fixture f = new Fixture(); TileEntity parent = mock(TileEntity.class); when(parent.getWorld()).thenReturn(f.world);
        MirrorspaceItems space = new MirrorspaceItems(parent);
        ItemStack stack = f.stack(64); stack.setTagCompound(new NBTTagCompound()); stack.getTagCompound().setString("marker", "kept");
        space.add(stack);
        when(f.world.spawnEntityInWorld(any())).thenReturn(false);
        assertFalse(space.eject(0)); assertEquals(64, space.stacks.get(0).stackSize);
        NBTTagCompound tag = new NBTTagCompound(); space.writeToNBT(tag);
        MirrorspaceItems restored = new MirrorspaceItems(parent); restored.readFromNBT(tag);
        assertEquals(64, restored.stacks.get(0).stackSize); assertEquals("kept", restored.stacks.get(0).getTagCompound().getString("marker"));
    }

    @Test public void permittedSpawnIsReportedAsSuccessful() throws Exception {
        Fixture f = new Fixture(); TileEntity parent = mock(TileEntity.class); when(parent.getWorld()).thenReturn(f.world);
        ExposedSpace space = new ExposedSpace(parent); when(f.world.spawnEntityInWorld(any())).thenReturn(true);
        assertTrue(space.spawn(f.stack(1))); verify(f.world).spawnEntityInWorld(any());
    }

    @Test public void multiDestinationMirrorPersistsRejectedSpillInsteadOfDiscardingIt() throws Exception {
        Fixture f = new Fixture(); TileEntityMirrorMultiDest mirror = new TileEntityMirrorMultiDest(); mirror.setWorldObj(f.world);
        ItemStack stack = f.stack(37); when(f.world.spawnEntityInWorld(any())).thenReturn(false);
        mirror.setInventorySlotContents(0, stack);
        assertEquals(37, mirror.pendingOutput.stacks.get(0).stackSize);
        NBTTagCompound tag = new NBTTagCompound(); mirror.writeCustomNBT(tag);
        TileEntityMirrorMultiDest restored = new TileEntityMirrorMultiDest(); restored.setWorldObj(f.world); restored.readCustomNBT(tag);
        restored.updateEntity();
        assertEquals(37, restored.pendingOutput.stacks.get(0).stackSize);
    }

    @Test public void permittedMirrorSpillDoesNotCreateAPendingCopy() throws Exception {
        Fixture f = new Fixture(); TileEntityMirrorMultiDest mirror = new TileEntityMirrorMultiDest(); mirror.setWorldObj(f.world);
        when(f.world.spawnEntityInWorld(any())).thenReturn(true); mirror.setInventorySlotContents(0, f.stack(37));
        assertTrue(mirror.pendingOutput.stacks.isEmpty()); verify(f.world).spawnEntityInWorld(any());
    }

    @Test public void mirrorCannotBeMovedByItsNumberKeyOrDirectShiftTransfer() throws Exception {
        try (MirrorFixture f = new MirrorFixture()) {
            f.player.inventory.setInventorySlotContents(9, f.stack(3));
            f.container.slotClick(10, 4, 2, f.player);
            f.container.transferStackInSlot(f.player, 5);
            assertSame(f.bound, f.player.inventory.getStackInSlot(4)); assertTrue(f.container.canInteractWith(f.player));
        }
    }

    @Test public void otherMirrorNumberKeysAndUnprotectedDragSlotsStillWork() throws Exception {
        try (MirrorFixture f = new MirrorFixture()) {
            ItemStack target = f.stack(3); f.player.inventory.setInventorySlotContents(9, target);
            f.container.slotClick(10, 3, 2, f.player);
            assertSame(target.getItem(), f.player.inventory.getStackInSlot(3).getItem());
            assertFalse(f.container.canDragIntoSlot((net.minecraft.inventory.Slot)f.container.inventorySlots.get(5)));
            assertTrue(f.container.canDragIntoSlot((net.minecraft.inventory.Slot)f.container.inventorySlots.get(10)));
        }
    }

    @Test public void movingMirrorIntoItsOwnInputIsBlocked() throws Exception {
        try (MirrorFixture f = new MirrorFixture()) {
            f.player.openContainer = f.container;
            f.container.slotClick(0, 4, 2, f.player);
            assertSame(f.container, f.player.openContainer); assertSame(f.bound, f.player.inventory.getStackInSlot(4));
            assertNull(f.container.input.getStackInSlot(0));
        }
    }

    @Test public void unexpectedSelfInputIsReturnedByNormalCloseExactlyOnce() throws Exception {
        try (MirrorFixture f = new MirrorFixture()) {
            f.player.openContainer = f.container;
            f.container.input.setInventorySlotContents(0, f.bound);
            assertSame(f.container, f.player.openContainer);
            f.player.inventory.setInventorySlotContents(4, null);
            f.container.onContainerClosed(f.player); f.container.onContainerClosed(f.player);
            verify(f.player, times(1)).dropPlayerItemWithRandomChoice(f.bound, false);
            assertNull(f.container.input.getStackInSlot(0));
        }
    }

    @Test public void replacingMirrorInvalidatesItsSession() throws Exception {
        try (MirrorFixture f = new MirrorFixture()) {
            f.player.inventory.setInventorySlotContents(4, f.bound.copy());
            assertFalse(f.container.canInteractWith(f.player));
        }
    }

    @Test public void craftingHotkeyAcceptsOnlyHotbarSlotsButStillOpensValidFocus() throws Exception {
        Fixture f = new Fixture(); ItemWandCasting wand = mock(ItemWandCasting.class); ItemFocusCrafting focus = mock(ItemFocusCrafting.class);
        TestRegistries.register(wand); TestRegistries.register(focus); ItemStack stack = new ItemStack(wand);
        when(wand.getFocusItem(any())).thenReturn(new ItemStack(focus));
        f.player.inventory.setInventorySlotContents(9, stack);
        MessageKeyPressed invalid = new MessageKeyPressed(0, 9); invalid.onMessage(invalid, f.context);
        verify(focus, never()).openGUI(any(), any(), anyInt());
        f.player.inventory.setInventorySlotContents(8, stack);
        MessageKeyPressed valid = new MessageKeyPressed(0, 8); valid.onMessage(valid, f.context);
        verify(focus).openGUI(f.player, stack, 8);
    }

    private static ItemStack packetStack(MessageGUIInventariumRequest packet) throws Exception {
        Field stack = MessageGUIInventariumRequest.class.getDeclaredField("stack"); stack.setAccessible(true); return (ItemStack)stack.get(packet);
    }
    private static class ExposedSpace extends MirrorspaceItems {
        ExposedSpace(TileEntity parent) {super(parent);} boolean spawn(ItemStack stack) {return super.spawnItem(stack);}
    }
    private static class Fixture {
        final World world = mock(World.class);
        final EntityPlayerMP player = mock(EntityPlayerMP.class);
        final MessageContext context = mock(MessageContext.class);
        final TileEntityInventarium victim = mock(TileEntityInventarium.class);
        final IInventariumBookLinksMirror book = mock(IInventariumBookLinksMirror.class);
        Fixture() throws Exception {
            Field provider = World.class.getDeclaredField("provider"); provider.setAccessible(true); provider.set(world, new WorldProviderSurface());
            world.rand = new Random(1); player.worldObj = world; player.inventory = new InventoryPlayer(player);
            NetHandlerPlayServer handler = mock(NetHandlerPlayServer.class); handler.playerEntity = player;
            when(context.getServerHandler()).thenReturn(handler);
            when(world.getTileEntity(10000, 64, 10000)).thenReturn(victim);
            when(victim.getRequestedItems()).thenReturn(new MappedItemsOrderedByTimeAdded("test"));
        }
        ItemStack stack(int count) throws Exception {Item item = new Item(); TestRegistries.register(item); return new ItemStack(item, count);}
        MessageGUIInventariumRequest request(int count) throws Exception {return new MessageGUIInventariumRequest(0, 10000, 64, 10000, stack(count), false, 0, 1, 64, 1);}
        void openBook() {
            ContainerScribe container = mock(ContainerScribe.class, CALLS_REAL_METHODS); container.book = book; player.openContainer = container;
            when(book.canRequestItems()).thenReturn(true); when(book.isInventariumLoaded()).thenReturn(true); when(book.isUseableByPlayer(player)).thenReturn(true);
            when(book.getLinkCoord()).thenReturn(new WorldSpecificCoordinates(0, 10000, 64, 10000)); when(book.getRequesterCoord()).thenReturn(new WorldSpecificCoordinates(0, 1, 64, 1));
            when(book.isOutputMirrorInRange()).thenReturn(true); when(victim.isTrackingDestinations()).thenReturn(true);
            when(world.blockExists(10000, 64, 10000)).thenReturn(true);
        }
        TileEntityVisReader reader() {
            TileEntityVisReader tile = spy(new TileEntityVisReader()); tile.setWorldObj(world); tile.modes[0] = 4;
            doNothing().when(tile).markDirty(anyBoolean()); doReturn(new AspectList()).when(tile).getAspects(); return tile;
        }
    }
    private static class MirrorFixture extends Fixture implements AutoCloseable {
        final ItemAutoHandMirror previous = ModItems.autoHandMirror;
        final ItemStack bound;
        final ContainerAutoHandMirror container;
        MirrorFixture() throws Exception {
            ItemAutoHandMirror mirror = mock(ItemAutoHandMirror.class); TestRegistries.register(mirror); ModItems.autoHandMirror = mirror;
            player.inventory.currentItem = 4; bound = new ItemStack(mirror); player.inventory.setInventorySlotContents(4, bound);
            container = new ContainerAutoHandMirror(player.inventory, world);
        }
        public void close() {ModItems.autoHandMirror = previous;}
    }
}
