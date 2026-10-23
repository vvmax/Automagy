package tuhljin.automagy.entities;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import thaumcraft.common.entities.golems.EntityGolemBase;

public class GolemTaskData implements IExtendedEntityProperties {
    public static final String EXT_PROP_NAME = "AutomagyGolemTask";
    public static final int WATCHER_ID = 26;
    public static final String TAG_NAME = "taskID";
    public final EntityGolemBase golem;

    public GolemTaskData(EntityGolemBase golem, int id) {
        this.golem = golem;
        golem.getDataWatcher().addObject(26, id);
    }

    public GolemTaskData(EntityGolemBase golem) {
        this(golem, 0);
    }

    @Override
    public void init(Entity entity, World world) {
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound properties = new NBTTagCompound();
        properties.setInteger("taskID", this.getTaskID());
        compound.setTag("AutomagyGolemTask", properties);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        NBTTagCompound properties = (NBTTagCompound)compound.getTag("AutomagyGolemTask");
        if (properties != null) {
            this.setTaskID(properties.getInteger("taskID"));
        }
    }

    public int getTaskID() {
        return this.golem.getDataWatcher().getWatchableObjectInt(26);
    }

    public void setTaskID(int id) {
        this.golem.getDataWatcher().updateObject(26, id);
    }

    public static GolemTaskData get(EntityGolemBase golem) {
        return (GolemTaskData)golem.getExtendedProperties("AutomagyGolemTask");
    }

    public static GolemTaskData setTaskID(EntityGolemBase golem, int id) {
        GolemTaskData taskLogic = get(golem);
        if (taskLogic == null) {
            taskLogic = new GolemTaskData(golem, id);
            golem.registerExtendedProperties("AutomagyGolemTask", taskLogic);
        } else {
            taskLogic.setTaskID(id);
        }

        return taskLogic;
    }

    public static void clearTaskID(EntityGolemBase golem) {
        setTaskID(golem, 0);
    }

    public static GolemTaskData register(EntityGolemBase golem) {
        GolemTaskData taskLogic = new GolemTaskData(golem);
        golem.registerExtendedProperties("AutomagyGolemTask", taskLogic);
        return taskLogic;
    }
}
