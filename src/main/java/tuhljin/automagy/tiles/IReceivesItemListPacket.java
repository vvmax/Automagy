package tuhljin.automagy.tiles;

import java.util.ArrayList;
import net.minecraft.item.ItemStack;

public interface IReceivesItemListPacket {
    void receiveItemListPacket(ArrayList<ItemStack> var1, boolean var2);
}
