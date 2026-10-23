package tuhljin.automagy.lib.compat.waila;

import java.util.List;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import tuhljin.automagy.blocks.ItemBlockJarXP;
import tuhljin.automagy.tiles.IXPContainer;

public class ProviderForXPContainer implements IWailaDataProvider {
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity te = accessor.getTileEntity();
        if (!(te instanceof IXPContainer)) {
            return currenttip;
        } else {
            int xp = ((IXPContainer)te).getXP();
            currenttip.add(ItemBlockJarXP.getLevelXPInfo(xp, "wailaTip.xp"));
            return currenttip;
        }
    }

    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }
}
