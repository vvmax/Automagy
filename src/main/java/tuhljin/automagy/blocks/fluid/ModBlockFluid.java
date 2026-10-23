package tuhljin.automagy.blocks.fluid;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import tuhljin.automagy.blocks.ModBlocks;

public abstract class ModBlockFluid extends BlockFluidClassic {
    public IIcon iconFlow;

    public ModBlockFluid(String name, Fluid fluid, Material material) {
        super(fluid, Material.water);
        this.setUnlocalizedName(name);
        this.setTextureName("Automagy:" + name);
        if (ModBlocks.tab != null) {
            this.setCreativeTab(ModBlocks.tab);
        }

        GameRegistry.registerBlock(this, name);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        String t = this.getTextureName();
        this.blockIcon = ir.registerIcon(t);
        this.iconFlow = ir.registerIcon(t);
        this.getFluid().setIcons(this.blockIcon, this.iconFlow);
    }
}
