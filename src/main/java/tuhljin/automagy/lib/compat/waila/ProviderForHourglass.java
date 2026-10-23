package tuhljin.automagy.lib.compat.waila;

import java.util.List;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import tuhljin.automagy.tiles.TileEntityHourglass;

public class ProviderForHourglass implements IWailaDataProvider {
    public boolean SHOW_DEBUG = false;

    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntityHourglass te;
        try {
            te = (TileEntityHourglass)accessor.getTileEntity();
        } catch (Exception var11) {
            return currenttip;
        }

        int time = te.getTargetTimeSeconds();
        if (time == 1) {
            currenttip.add(StatCollector.translateToLocal("wailaTip.hourglassTimerSecOne"));
        } else if (time <= 60) {
            currenttip.add(StatCollector.translateToLocalFormatted("wailaTip.hourglassTimerSec", time));
        } else {
            int min = time / 60;
            int sec = time % 60;
            currenttip.add(StatCollector.translateToLocalFormatted("wailaTip.hourglassTimerMinSec", min, sec));
        }

        int timeLeft = te.getRemainingSeconds();
        if (timeLeft > 0) {
            int min = timeLeft / 60;
            int sec = timeLeft % 60;
            String sSec = sec < 10 ? "0" + sec : String.valueOf(sec);
            currenttip.add(StatCollector.translateToLocalFormatted("wailaTip.hourglassTimerCountdown", min, sSec));
        }

        return currenttip;
    }

    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }
}
