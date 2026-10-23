package tuhljin.automagy.blocks;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.tiles.TileEntityTally;

public class BlockTally extends BlockTallyBase {
    public BlockTally(String name) {
        super(name);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileEntityTally();
    }

    @Override
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.iconReading = ir.registerIcon("Automagy:blockTallyRead");
    }
}
