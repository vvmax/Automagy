package tuhljin.automagy.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.tiles.TileEntitySpecialProcess;

public class BlockSpecialProcess extends ModBlockSpecialRender {
    public BlockSpecialProcess(String name) {
        super(name, Material.circuits);
        this.setBlockUnbreakable();
        this.setResistance(6000000.0F);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.01F, 1.0F);
        this.setTextureName("obsidian");
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileEntitySpecialProcess();
    }
}
