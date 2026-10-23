package tuhljin.automagy.lib;

import java.util.ArrayDeque;
import org.apache.logging.log4j.LogManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import tuhljin.automagy.entities.EntityItemDelayedFall;

/** Owns rejected drops after their original inventory or block has gone away. One queue per dimension. */
public class DeferredItemDrops extends WorldSavedData {
    public static final String KEY = "AutomagyDeferredDrops";
    private final ArrayDeque<NBTTagCompound> pending = new ArrayDeque<>();
    private boolean retrying;

    public DeferredItemDrops(String name) { super(name); }

    public static void spawnOrQueue(EntityItem entity) {
        World world = entity.worldObj;
        // Forge deliberately suppresses drops during a block rollback; queuing those would duplicate restored inventories.
        if (world.isRemote || world.restoringBlockSnapshots || world.spawnEntityInWorld(entity)) return;
        NBTTagCompound tag = new NBTTagCompound();
        entity.writeToNBT(tag);
        tag.setBoolean("AutomagyHovering", entity instanceof EntityItemDelayedFall);
        get(world).pending.addLast(tag);
        get(world).markDirty();
    }

    public static DeferredItemDrops get(World world) {
        DeferredItemDrops data = (DeferredItemDrops)world.perWorldStorage.loadData(DeferredItemDrops.class, KEY);
        if (data == null) {
            data = new DeferredItemDrops(KEY);
            world.perWorldStorage.setData(KEY, data);
        }
        return data;
    }

    public static void tick(World world) {
        if (world.isRemote || world.getTotalWorldTime() % 20 != 0) return;
        DeferredItemDrops data = (DeferredItemDrops)world.perWorldStorage.loadData(DeferredItemDrops.class, KEY);
        if (data != null) data.retry(world);
    }

    public void retry(World world) {
        if (world.isRemote || world.restoringBlockSnapshots || this.retrying) return;
        this.retrying = true;
        try {
            int attempts = Math.min(32, this.pending.size());
            while (attempts-- > 0) {
                NBTTagCompound tag = this.pending.removeFirst();
                boolean delivered = false;
                try {
                    EntityItem entity = tag.getBoolean("AutomagyHovering") ? new EntityItemDelayedFall(world) : new EntityItem(world);
                    entity.readFromNBT(tag);
                    if (world.blockExists(MathHelper.floor_double(entity.posX), Math.max(0, Math.min(255, MathHelper.floor_double(entity.posY))), MathHelper.floor_double(entity.posZ))) {
                        delivered = world.spawnEntityInWorld(entity);
                    }
                } catch (RuntimeException failure) {
                    LogManager.getLogger("Automagy").warn("Deferred item drop retained after spawn handler failed", failure);
                    break;
                } finally {
                    if (!delivered) this.pending.addLast(tag);
                    this.markDirty();
                }
            }
        } finally {
            this.retrying = false;
        }
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        this.pending.clear();
        NBTTagList list = tag.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) this.pending.addLast((NBTTagCompound)list.getCompoundTagAt(i).copy());
    }

    @Override public void writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (NBTTagCompound item : this.pending) list.appendTag(item.copy());
        tag.setTag("Items", list);
    }
}
