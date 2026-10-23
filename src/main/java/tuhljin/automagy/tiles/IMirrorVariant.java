package tuhljin.automagy.tiles;

import net.minecraft.item.ItemStack;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

public interface IMirrorVariant {
    WorldSpecificCoordinates getDestinationMirrorCoordinates();

    boolean isLinked();

    int addStackToOutput(ItemStack var1);

    void markMirrorDirty();
}
