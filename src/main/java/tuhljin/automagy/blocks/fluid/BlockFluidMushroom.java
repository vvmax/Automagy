package tuhljin.automagy.blocks.fluid;

import net.minecraft.block.material.Material;
import tuhljin.automagy.blocks.ModBlocks;

public class BlockFluidMushroom extends ModBlockFluid {
    public BlockFluidMushroom(String name) {
        super(name, ModBlocks.fluidMushroom, Material.water);
        this.setLightOpacity(5);
    }
}
