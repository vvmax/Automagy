package tuhljin.automagy.blocks.fluid;

import net.minecraft.block.material.Material;
import tuhljin.automagy.blocks.ModBlocks;

public class BlockFluidMilk extends ModBlockFluid {
    public BlockFluidMilk(String name) {
        super(name, ModBlocks.fluidMilk, Material.water);
        this.setLightOpacity(3);
    }
}
