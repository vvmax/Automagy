package tuhljin.automagy.gui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import java.util.concurrent.atomic.AtomicInteger;
import thaumcraft.common.items.wands.ItemWandCasting;
import tuhljin.automagy.TestRegistries;
import tuhljin.automagy.items.InventoryForArcaneCrafting;
import tuhljin.automagy.items.InventoryForItem;
import tuhljin.automagy.items.ItemFocusCrafting;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PortableCraftingTest {
    @Test public void malformedClickCannotIndexOutsideTheContainer() throws Exception {
        Fixture f = new Fixture(4);
        assertNull(f.container.slotClick(-999, 3, 2, f.player));
        assertNull(f.container.slotClick(-1, 3, 2, f.player));
        assertNull(f.container.slotClick(47, 3, 2, f.player));
        assertNull(f.container.slotClick(-999, 1, 5, f.player));
    }

    @Test public void numberKeyCannotMoveTheBoundWand() throws Exception {
        Fixture f = new Fixture(4);
        f.player.inventory.setInventorySlotContents(9, new ItemStack(new Item(), 8));
        f.container.slotClick(20, 4, 2, f.player);
        assertSame(f.wand, f.player.inventory.getStackInSlot(4));
        assertEquals(8, f.player.inventory.getStackInSlot(9).stackSize);
    }

    @Test public void otherNumberKeysStillSwapItems() throws Exception {
        Fixture f = new Fixture(4);
        ItemStack target = new ItemStack(new Item(), 8);
        ItemStack hotbar = new ItemStack(new Item(), 3);
        f.player.inventory.setInventorySlotContents(9, target);
        f.player.inventory.setInventorySlotContents(3, hotbar);
        f.container.slotClick(20, 3, 2, f.player);
        assertSame(target.getItem(), f.player.inventory.getStackInSlot(3).getItem());
        assertSame(hotbar, f.player.inventory.getStackInSlot(9));
    }

    @Test public void resultNumberKeyKeepsRequestedHotbarIndex() throws Exception {
        Fixture f = new Fixture(4);
        ItemStack result = new ItemStack(new Item(), 2);
        f.crafting.setInventorySlotContents(0, result);
        f.container.slotClick(0, 3, 2, f.player);
        assertNotNull(f.player.inventory.getStackInSlot(3));
        assertNull(f.player.inventory.getStackInSlot(0));
    }

    @Test public void readOnlyWandCannotReceiveDragDistribution() throws Exception {
        Fixture f = new Fixture(4);
        assertFalse(f.container.canDragIntoSlot((Slot)f.container.inventorySlots.get(15)));
        assertFalse(f.container.canDragIntoSlot((Slot)f.container.inventorySlots.get(1)));
        assertTrue(f.container.canDragIntoSlot((Slot)f.container.inventorySlots.get(2)));
    }

    @Test public void fakeWorkbenchKeepsTheProvidedWorld() {
        World world = mock(World.class);
        FakeTileMagicWorkbench tile = new FakeTileMagicWorkbench(new InventoryBasic("craft", false, 11), world);
        assertSame(world, tile.getWorld());
    }

    @Test public void replacingWandClosesItsOldSession() throws Exception {
        Fixture f = new Fixture(4);
        ItemStack bound = f.bindFocus();
        assertTrue(f.container.canInteractWith(f.player));
        f.player.inventory.setInventorySlotContents(4, new ItemStack(bound.getItem()));
        assertFalse(f.container.canInteractWith(f.player));
    }

    @Test public void replacingFocusClosesItsOldStorageSession() throws Exception {
        Fixture f = new Fixture(4);
        ItemStack bound = f.bindFocus();
        assertTrue(f.container.canInteractWith(f.player));
        ItemStack replacement = new ItemStack(f.container.storagestack.getItem());
        replacement.setTagCompound(new NBTTagCompound());
        when(((ItemWandCasting)bound.getItem()).getFocusItem(any())).thenReturn(replacement);
        assertFalse(f.container.canInteractWith(f.player));
    }

    @Test public void closingTwiceReturnsMaterialsOnlyOnce() throws Exception {
        Fixture f = new Fixture(4);
        f.player.worldObj = mock(World.class);
        f.player.worldObj.rand = new java.util.Random(1);
        java.lang.reflect.Field provider = World.class.getDeclaredField("provider");
        provider.setAccessible(true);
        provider.set(f.player.worldObj, new net.minecraft.world.WorldProviderSurface());
        InventoryForArcaneCrafting inv = f.attachCraftingInventory();
        inv.setInventorySlotContentsSoftly(0, new ItemStack(new Item(), 64));
        AtomicInteger returned = new AtomicInteger();
        when(f.player.worldObj.spawnEntityInWorld(any())).thenAnswer(call -> {
            returned.addAndGet(((net.minecraft.entity.item.EntityItem)call.getArgument(0)).getEntityItem().stackSize);
            return true;
        });
        boolean previous = ContainerPortableArcaneCrafting.droppedItemsHover;
        try {
            ContainerPortableArcaneCrafting.droppedItemsHover = false;
            f.container.onContainerClosed(f.player);
            f.container.onContainerClosed(f.player);
        } finally { ContainerPortableArcaneCrafting.droppedItemsHover = previous; }
        assertEquals(64, returned.get());
        assertNull(inv.getStackInSlot(0));
    }

    @Test public void storageClosePreservesCountMetadataAndNbt() throws Exception {
        Fixture f = new Fixture(4);
        f.player.worldObj = mock(World.class);
        InventoryForArcaneCrafting inv = f.attachCraftingInventory();
        Item material = new Item();
        TestRegistries.register(material);
        ItemStack stack = new ItemStack(material, 7, 9);
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString("marker", "storage");
        inv.setInventorySlotContentsSoftly(0, stack);
        f.container.hasStorage = true;
        f.container.storagestack = new ItemStack(new Item());
        f.container.onContainerClosed(f.player);
        f.container.onContainerClosed(f.player);
        InventoryForItem restored = new InventoryForItem(f.container.storagestack, "test", 11);
        ItemStack result = restored.getStackInSlot(0);
        assertEquals(7, result.stackSize);
        assertEquals(9, result.getMetadata());
        assertEquals("storage", result.getTagCompound().getString("marker"));
        verify(f.player, never()).dropPlayerItemWithRandomChoice(any(), anyBoolean());
    }

    @Test public void focusRightClickStillOpensCraftingGui() {
        ItemFocusCrafting focus = mock(ItemFocusCrafting.class, CALLS_REAL_METHODS);
        EntityPlayer player = mock(EntityPlayer.class);
        player.inventory = new InventoryPlayer(player);
        World world = mock(World.class);
        ItemStack wand = new ItemStack(new Item());
        doNothing().when(focus).openGUI(any(), any(), anyInt());
        assertSame(wand, focus.onFocusRightClick(wand, world, player, null));
        verify(focus).openGUI(player, wand, 0);
    }

    private static class Fixture {
        final EntityPlayer player = mock(EntityPlayer.class);
        final ContainerPortableArcaneCrafting container = mock(ContainerPortableArcaneCrafting.class, CALLS_REAL_METHODS);
        final InventoryBasic crafting = new InventoryBasic("craft", false, 11);
        final ItemStack wand = new ItemStack(new Item().setMaxStackSize(1));
        Fixture(int hotbarSlot) throws Exception {
            player.inventory = new InventoryPlayer(player);
            player.inventory.setInventorySlotContents(hotbarSlot, wand);
            container.player = player;
            container.inventorySlots = new ArrayList();
            container.inventoryItemStacks = new ArrayList();
            set("blockedSlotID", 11 + hotbarSlot);
            set("blockedSlotHotbarPos", hotbarSlot);
            set("displayWandSlotID", 1);
            doNothing().when(container).onCraftMatrixChanged(any());
            // This fixture isolates vanilla click handling; session identity is tested separately.
            doReturn(true).when(container).canInteractWith(player);
            for (int i = 0; i < 11; i++) add(new Slot(crafting, i, 0, 0));
            for (int i = 0; i < 36; i++) add(new Slot(player.inventory, i, 0, 0));
        }
        void add(Slot slot) {
            slot.slotNumber = container.inventorySlots.size();
            container.inventorySlots.add(slot);
            container.inventoryItemStacks.add(null);
        }
        void set(String name, int value) throws Exception {
            Field f = ContainerPortableArcaneCrafting.class.getDeclaredField(name);
            f.setAccessible(true);
            f.setInt(container, value);
        }
        InventoryForArcaneCrafting attachCraftingInventory() {
            InventoryForArcaneCrafting inv = new InventoryForArcaneCrafting(null, "test", 11, container, player.inventory, 4);
            container.craftingInv = inv;
            return inv;
        }
        ItemStack bindFocus() throws Exception {
            ItemWandCasting wandItem = mock(ItemWandCasting.class);
            ItemFocusCrafting focusItem = mock(ItemFocusCrafting.class);
            TestRegistries.register(wandItem);
            TestRegistries.register(focusItem);
            ItemStack bound = new ItemStack(wandItem);
            ItemStack focus = new ItemStack(focusItem);
            focus.setTagCompound(new NBTTagCompound());
            when(wandItem.getFocusItem(any())).thenReturn(focus);
            player.inventory.setInventorySlotContents(4, bound);
            container.storagestack = focus;
            try {
                Field field = ContainerPortableArcaneCrafting.class.getDeclaredField("boundWand");
                field.setAccessible(true);
                field.set(container, bound);
            } catch (NoSuchFieldException baseline) { /* Original container has no session binding. */ }
            doCallRealMethod().when(container).canInteractWith(player);
            return bound;
        }
    }
}
