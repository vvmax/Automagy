package tuhljin.automagy.lib.compat.waila;

import java.util.List;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import tuhljin.automagy.tiles.TileEntityGolemInhibitor;

public class ProviderForGolemInhibitor implements IWailaDataProvider {
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntityGolemInhibitor te;
        try {
            te = (TileEntityGolemInhibitor)accessor.getTileEntity();
        } catch (Exception var7) {
            return currenttip;
        }

        if (te.matchOmitsGolem) {
            currenttip.add(StatCollector.translateToLocal("wailaTip.golemInhibitorMatchOmit"));
        } else {
            currenttip.add(StatCollector.translateToLocal("wailaTip.golemInhibitorMatchExclusive"));
        }

        return currenttip;
    }

    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }
}
