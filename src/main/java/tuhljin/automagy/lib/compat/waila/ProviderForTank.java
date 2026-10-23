package tuhljin.automagy.lib.compat.waila;

import java.util.List;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidTank;
import tuhljin.automagy.tiles.ITileWithTank;

public class ProviderForTank implements IWailaDataProvider {
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity te = accessor.getTileEntity();
        if (!(te instanceof ITileWithTank)) {
            return currenttip;
        } else {
            FluidTank tank = ((ITileWithTank)te).getTank();
            int amt = tank.getFluidAmount();
            int max = tank.getCapacity();
            if (amt == 0) {
                currenttip.add(StatCollector.translateToLocalFormatted("wailaTip.tankFluidDataEmpty", max));
            } else {
                currenttip.add(StatCollector.translateToLocalFormatted("wailaTip.tankFluidData", amt, max, tank.getFluid().getLocalizedName()));
            }

            return currenttip;
        }
    }

    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }
}
