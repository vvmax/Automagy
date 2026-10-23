package tuhljin.automagy.blocks;

import java.util.ArrayList;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.tiles.TileEntityRemoteComparator;
import tuhljin.automagy.tiles.TileEntityTallyWorld;

public class BlockTallyWorld extends BlockTallyBase {
    public BlockTallyWorld(String name) {
        super(name);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityTallyWorld();
    }

    @Override
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.iconReading = ir.registerIcon("Automagy:blockTallyLensWorld");
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(ModBlocks.tallyBlock));
        drops.add(new ItemStack(ModItems.tallyLens, 1, 1));
        return drops;
    }

    @Override
    public boolean canSilkHarvest() {
        return true;
    }

    @Override
    public int getRemoteComparatorParticleColor(World world, int x, int y, int z, TileEntityRemoteComparator teRC) {
        int ret = super.getRemoteComparatorParticleColor(world, x, y, z, teRC);
        if (ret != -1 && world.rand.nextInt(3) == 0) {
            ret = 1;
        }

        return ret;
    }
}
