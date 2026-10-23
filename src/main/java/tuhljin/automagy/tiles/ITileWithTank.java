package tuhljin.automagy.tiles;

import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;

public interface ITileWithTank extends IFluidHandler {
    FluidTank getTank();

    boolean drainExactAmount(int var1, boolean var2);

    boolean fillExactAmount(FluidStack var1);

    boolean fillExactAmount(BlockFluidBase var1);
}
