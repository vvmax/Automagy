package tuhljin.automagy.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ModBlockSlab extends BlockSlab {
    private ModBlockSlab(String name, boolean doubleSlab, Material material, Class<? extends ItemBlock> itemBlockClass) {
        super(doubleSlab, material);
        this.useNeighborBrightness = true;
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

    public ModBlockSlab(String name, Material material, Class<? extends ItemBlock> itemBlockClass) {
        this(name, false, material, itemBlockClass);
    }

    public String getFullSlabName(int damage) {
        return StatCollector.translateToLocal(this.getUnlocalizedName() + ".name");
    }

    @Override
    public Item getItem(World p_149694_1_, int p_149694_2_, int p_149694_3_, int p_149694_4_) {
        return Item.getItemFromBlock(this);
    }
}
