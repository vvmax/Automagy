package tuhljin.automagy.lib.inventory;

import net.minecraft.nbt.NBTTagCompound;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

public class MappedItemsWithDestinations extends MappedItemsWithGroupedCounts<WorldSpecificCoordinates> {
    public MappedItemsWithDestinations(String key) {
        super(key);
    }

    public MappedItemsWithDestinations(MappedItemsWithGroupedCounts copySource) {
        super(copySource);
    }

    protected WorldSpecificCoordinates getGroupFromNBT(NBTTagCompound nbt) {
        NBTTagCompound subnbt = nbt.getCompoundTag("Group");
        if (!subnbt.hasKey("dim")) {
            return null;
        } else if (!subnbt.hasKey("x")) {
            return null;
        } else if (!subnbt.hasKey("y")) {
            return null;
        } else {
            return !subnbt.hasKey("z")
                ? null
                : new WorldSpecificCoordinates(subnbt.getInteger("dim"), subnbt.getInteger("x"), subnbt.getInteger("y"), subnbt.getInteger("z"));
        }
    }

    protected void saveGroupToNBT(NBTTagCompound nbt, WorldSpecificCoordinates group) {
        NBTTagCompound subnbt = new NBTTagCompound();
        subnbt.setInteger("dim", group.dim);
        subnbt.setInteger("x", group.x);
        subnbt.setInteger("y", group.y);
        subnbt.setInteger("z", group.z);
        nbt.setTag("Group", subnbt);
    }
}
