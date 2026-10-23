package tuhljin.automagy.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;

public abstract class ModBlock extends Block {
    public ModBlock(String name, Material material, Class<? extends ItemBlock> itemBlockClass) {
        super(material);
        this.setUnlocalizedName(name);
        this.setTextureName("Automagy:" + name);
        if (material == ModBlocks.materialNiceStone || material == ModBlocks.materialNiceIron) {
            this.setHardness(3.0F);
            this.setResistance(10.0F);
            this.setHarvestLevel("pickaxe", 0);
        }

        if (ModBlocks.tab != null) {
            this.setCreativeTab(ModBlocks.tab);
        }

        if (itemBlockClass == null) {
            GameRegistry.registerBlock(this, name);
        } else {
            GameRegistry.registerBlock(this, itemBlockClass, name);
        }
    }

    public ModBlock(String name, Material material) {
        this(name, material, null);
    }
}
