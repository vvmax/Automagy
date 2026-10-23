package tuhljin.automagy.lib.compat.waila;

import java.util.List;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import tuhljin.automagy.blocks.BlockRedcrystalDense;
import tuhljin.automagy.blocks.BlockRedcrystalDim;
import tuhljin.automagy.blocks.BlockRedcrystalRes;
import tuhljin.automagy.lib.IOrientableRedstoneConductor;
import tuhljin.automagy.tiles.TileEntityRedcrystal;

public class ProviderForIOrientableRedstoneConductor implements IWailaDataProvider {
    public boolean SHOW_DEBUG = false;

    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        IOrientableRedstoneConductor block;
        try {
            block = (IOrientableRedstoneConductor)accessor.getBlock();
        } catch (Exception var11) {
            return currenttip;
        }

        MovingObjectPosition pos = accessor.getPosition();
        int power = block.getRedstoneSignalStrength(accessor.getWorld(), pos.blockX, pos.blockY, pos.blockZ, true);
        String s;
        if (power > 15) {
            int amp = power - 15;
            s = StatCollector.translateToLocalFormatted("wailaTip.redcrystalPowerWithAmp", 15, amp);
        } else if (block instanceof BlockRedcrystalDim) {
            int cap = ((BlockRedcrystalDim)block).getRedstoneSignalCap(accessor.getWorld(), pos.blockX, pos.blockY, pos.blockZ);
            s = StatCollector.translateToLocalFormatted("wailaTip.redcrystalPowerWithCap", power, cap);
        } else {
            s = StatCollector.translateToLocalFormatted("wailaTip.redcrystalPower", power);
        }

        if (block instanceof BlockRedcrystalDense) {
            int min = ((BlockRedcrystalDense)block).getRedstoneSignalMin(accessor.getWorld(), pos.blockX, pos.blockY, pos.blockZ);
            s = s + "  " + StatCollector.translateToLocalFormatted("wailaTip.redcrystalPowerMin", min);
        }

        if (!s.isEmpty()) {
            currenttip.add(s);
        }

        if (this.SHOW_DEBUG) {
            if (power > 0) {
                int sourceSide = block.getRedstoneSignalSourceSide(accessor.getWorld(), pos.blockX, pos.blockY, pos.blockZ);
                if (block.isRedstoneSignalInputPoint(accessor.getWorld(), pos.blockX, pos.blockY, pos.blockZ)) {
                    currenttip.add("source=" + ForgeDirection.getOrientation(sourceSide) + " [primary]");
                } else {
                    currenttip.add("source=" + ForgeDirection.getOrientation(sourceSide));
                }
            }

            if (block instanceof BlockRedcrystalRes) {
                TileEntity te = accessor.getTileEntity();
                if (te instanceof TileEntityRedcrystal) {
                    int p = ((TileEntityRedcrystal)te).extraData;
                    if (p <= 15) {
                        currenttip.add("Remote : " + p);
                    } else {
                        currenttip.add("Remote : 15 (+" + (p - 15) + ")");
                    }
                }

                currenttip.add(pos.blockX + "," + pos.blockY + "," + pos.blockZ);
            }
        }

        return currenttip;
    }

    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }
}
