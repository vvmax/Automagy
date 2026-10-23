package tuhljin.automagy.entities;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EntityAvaricePearlTest {
    private final Item item = new Item().setMaxStackSize(64);
    private final EntityAvaricePearl.EntitySelectorItem selector = new EntityAvaricePearl.EntitySelectorItem();

    private EntityItem drop(int count) {
        ItemStack stack = new ItemStack(item, count, 7);
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString("marker", "preserved");
        return new EntityItem(null, 0, 0, 0, stack);
    }

    @Test
    public void hopperConsumedStackIsNotAvailableToPearl() {
        EntityItem dropped = drop(64);
        InventoryBasic hopper = new InventoryBasic("hopper", false, 5);
        // Actual Forge 1.7.10 hopper code: copies the stack, then kills the entity.
        assertTrue(TileEntityHopper.func_145898_a(hopper, dropped));
        assertTrue(dropped.isDead);
        assertEquals(64, dropped.getEntityItem().stackSize);
        assertFalse(selector.isEntityApplicable(dropped));
        assertEquals(64, count(hopper) + collectibleCount(dropped));
    }

    @Test
    public void partialHopperPickupKeepsOnlyLiveRemainderAndNbt() {
        EntityItem dropped = drop(64);
        InventoryBasic hopper = new InventoryBasic("hopper", false, 1);
        ItemStack nearlyFull = dropped.getEntityItem().copy();
        nearlyFull.stackSize = 60;
        hopper.setInventorySlotContents(0, nearlyFull);
        assertFalse(TileEntityHopper.func_145898_a(hopper, dropped));
        assertFalse(dropped.isDead);
        assertTrue(selector.isEntityApplicable(dropped));
        assertEquals(60, dropped.getEntityItem().stackSize);
        assertEquals(124, count(hopper) + collectibleCount(dropped));
        assertEquals(7, dropped.getEntityItem().getMetadata());
        assertEquals("preserved", dropped.getEntityItem().getTagCompound().getString("marker"));
    }

    @Test
    public void emptyStacksAndNonItemsAreRejected() {
        assertFalse(selector.isEntityApplicable(drop(0)));
        assertFalse(selector.isEntityApplicable(drop(-1)));
        assertFalse(selector.isEntityApplicable(new Pearl()));
        assertFalse(selector.isEntityApplicable(null));
        assertTrue(selector.isEntityApplicable(drop(1)));
    }

    @Test
    public void impactRechecksItemsConsumedAfterQuery() {
        EntityItem dropped = drop(64);
        InventoryBasic hopper = new InventoryBasic("hopper", false, 5);
        Pearl pearl = new Pearl();
        pearl.worldObj = queryWorld(dropped, () -> assertTrue(TileEntityHopper.func_145898_a(hopper, dropped)));
        pearl.impact();
        assertEquals(0, pearl.stacksGrabbed);
        assertEquals(64, count(hopper) + collectibleCount(dropped));
    }

    @Test
    public void impactStillTransportsLiveItemsWithoutChangingTheirStack() {
        EntityItem dropped = drop(64);
        ItemStack stack = dropped.getEntityItem();
        Pearl pearl = new Pearl();
        pearl.setPosition(8, 10, 12);
        pearl.worldObj = queryWorld(dropped, () -> { });
        pearl.impact();
        assertEquals(1, pearl.stacksGrabbed);
        assertFalse(dropped.isDead);
        assertSame(stack, dropped.getEntityItem());
        assertEquals(64, stack.stackSize);
        assertEquals(8, dropped.posX, 0);
        assertEquals(10.5, dropped.posY, 0);
        assertEquals(12, dropped.posZ, 0);
    }

    @Test
    public void multiplePearlsCannotReuseHopperConsumedEntity() {
        EntityItem dropped = drop(64);
        InventoryBasic hopper = new InventoryBasic("hopper", false, 5);
        assertTrue(TileEntityHopper.func_145898_a(hopper, dropped));
        World world = queryWorld(dropped, () -> { });
        for (int i = 0; i < 16; i++) {
            Pearl pearl = new Pearl();
            pearl.worldObj = world;
            pearl.impact();
            assertEquals(0, pearl.stacksGrabbed);
        }
        assertEquals(64, count(hopper) + collectibleCount(dropped));
    }

    @Test
    public void ongoingMiserPearlRechecksConsumedSnapshot() {
        EntityItem dropped = drop(64);
        InventoryBasic hopper = new InventoryBasic("hopper", false, 5);
        Pearl pearl = new Pearl();
        pearl.worldObj = queryWorld(dropped, () -> assertTrue(TileEntityHopper.func_145898_a(hopper, dropped)));
        pearl.onUpdate();
        assertEquals(0, pearl.stacksGrabbed);
        assertEquals(64, count(hopper) + collectibleCount(dropped));
    }

    @Test
    public void ongoingMiserPearlStillStopsAtTenStacks() {
        List<Entity> dropped = new ArrayList<>();
        for (int i = 0; i < 14; i++) dropped.add(drop(1));
        World world = mock(World.class);
        when(world.selectEntitiesWithinAABB(eq(Entity.class), any(AxisAlignedBB.class), any(IEntitySelector.class)))
            .thenReturn(dropped);
        Pearl pearl = new Pearl();
        pearl.worldObj = world;
        pearl.onUpdate();
        assertEquals(10, pearl.stacksGrabbed);
        for (Entity entity : dropped) {
            assertTrue(selector.isEntityApplicable(entity));
            assertEquals(1, ((EntityItem)entity).getEntityItem().stackSize);
        }
    }

    private World queryWorld(EntityItem dropped, Runnable afterQuery) {
        World world = mock(World.class);
        when(world.selectEntitiesWithinAABB(eq(Entity.class), any(AxisAlignedBB.class), any(IEntitySelector.class)))
            .thenAnswer(invocation -> {
                IEntitySelector filter = invocation.getArgument(2);
                List<Entity> snapshot = new ArrayList<>();
                if (filter.isEntityApplicable(dropped)) snapshot.add(dropped);
                afterQuery.run();
                return snapshot;
            });
        return world;
    }

    private int count(InventoryBasic inventory) {
        int total = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null) total += stack.stackSize;
        }
        return total;
    }

    private int collectibleCount(EntityItem dropped) {
        return selector.isEntityApplicable(dropped) ? dropped.getEntityItem().stackSize : 0;
    }

    private static final class Pearl extends EntityAvaricePearl {
        Pearl() {
            super(null);
            metadata = 1;
            noPearlOnImpact = true;
        }
        // Keep projectile movement and pearl logic real; skip unrelated world/base tick bookkeeping.
        @Override public void onEntityUpdate() { }
        void impact() { onImpact(null); }
    }
}
