package tuhljin.automagy.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;

public abstract class ModBlockSpecialRender extends ModBlockContainer {
    public ModBlockSpecialRender(String name, Material material, Class<? extends ItemBlock> itemBlockClass) {
        super(name, material, itemBlockClass);
    }

    public ModBlockSpecialRender(String name, Material material) {
        super(name, material);
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }
}
