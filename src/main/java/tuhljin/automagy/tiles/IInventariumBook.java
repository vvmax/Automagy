package tuhljin.automagy.tiles;

import net.minecraft.entity.player.EntityPlayer;
import tuhljin.automagy.lib.inventory.IItemMap;
import tuhljin.automagy.lib.inventory.MappedItemsOrderedByTimeAdded;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

public interface IInventariumBook {
    WorldSpecificCoordinates getLinkCoord();

    WorldSpecificCoordinates getRequesterCoord();

    boolean hasLocalItems();

    boolean canRequestItems();

    IItemMap getLocalItemList();

    IItemMap getInventariumItemList();

    MappedItemsOrderedByTimeAdded getInventariumRequestedItemList();

    boolean isUseableByPlayer(EntityPlayer var1);

    boolean isInventariumLoaded();

    void setPlayerLocalViewPreference(EntityPlayer var1, boolean var2);

    void setPlayerSortingPreference(EntityPlayer var1, boolean var2);

    boolean doesPlayerPreferLocalView(EntityPlayer var1);

    boolean doesPlayerPreferSortByName(EntityPlayer var1);
}
