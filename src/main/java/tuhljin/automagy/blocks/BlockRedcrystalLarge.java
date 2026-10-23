package tuhljin.automagy.blocks;

import java.util.List;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.codechicken.lib.raytracer.IndexedCuboid6;

public abstract class BlockRedcrystalLarge extends BlockRedcrystal {
    public BlockRedcrystalLarge(String name) {
        super(name);
    }

    @Override
    public void addTraceableCuboids(IBlockAccess blockaccess, int x, int y, int z, List<IndexedCuboid6> cuboids) {
        double cmin = 0.36;
        double cmax = 0.64;
        double a = 0.44;
        double b = 0.9;
        double c = 0.75;
        double d = 0.25;
        double e = 0.1;
        double f = 0.56;
        double g = 0.9;
        double h = 0.15;
        switch (this.getOrientation(blockaccess, x, y, z)) {
            case 0:
                this.addIndexedCuboid(x, y, z, cuboids, 6, cmin, 0.8, cmin, cmax, 1.0, cmax);
                this.addIndexedCuboid(x, y, z, cuboids, 2, a, b, e, f, 1.0, d);
                this.addIndexedCuboid(x, y, z, cuboids, 3, a, b, c, f, 1.0, g);
                this.addIndexedCuboid(x, y, z, cuboids, 5, c, b, a, g, 1.0, f);
                this.addIndexedCuboid(x, y, z, cuboids, 4, e, b, a, d, 1.0, f);
                this.addIndexedCuboid(x, y, z, cuboids, 7, e, 0.995, e, g, 1.0, g);
                break;
            case 1:
            default:
                this.addIndexedCuboid(x, y, z, cuboids, 6, cmin, 0.0, cmin, cmax, 0.2, cmax);
                this.addIndexedCuboid(x, y, z, cuboids, 2, a, 0.0, e, f, h, d);
                this.addIndexedCuboid(x, y, z, cuboids, 3, a, 0.0, c, f, h, g);
                this.addIndexedCuboid(x, y, z, cuboids, 5, c, 0.0, a, g, h, f);
                this.addIndexedCuboid(x, y, z, cuboids, 4, e, 0.0, a, d, h, f);
                this.addIndexedCuboid(x, y, z, cuboids, 7, e, 0.0, e, g, 0.005, g);
                break;
            case 2:
                this.addIndexedCuboid(x, y, z, cuboids, 6, cmin, cmin, 0.8, cmax, cmax, 1.0);
                this.addIndexedCuboid(x, y, z, cuboids, 1, a, c, b, f, g, 1.0);
                this.addIndexedCuboid(x, y, z, cuboids, 0, a, e, b, f, d, 1.0);
                this.addIndexedCuboid(x, y, z, cuboids, 4, e, a, b, d, f, 1.0);
                this.addIndexedCuboid(x, y, z, cuboids, 5, c, a, b, g, f, 1.0);
                this.addIndexedCuboid(x, y, z, cuboids, 7, e, e, 0.995, g, g, 1.0);
                break;
            case 3:
                this.addIndexedCuboid(x, y, z, cuboids, 6, cmin, cmin, 0.0, cmax, cmax, 0.2);
                this.addIndexedCuboid(x, y, z, cuboids, 1, a, c, 0.0, f, g, h);
                this.addIndexedCuboid(x, y, z, cuboids, 0, a, e, 0.0, f, d, h);
                this.addIndexedCuboid(x, y, z, cuboids, 4, e, a, 0.0, d, f, h);
                this.addIndexedCuboid(x, y, z, cuboids, 5, c, a, 0.0, g, f, h);
                this.addIndexedCuboid(x, y, z, cuboids, 7, e, e, 0.0, g, g, 0.005);
                break;
            case 4:
                this.addIndexedCuboid(x, y, z, cuboids, 6, 0.8, cmin, cmin, 1.0, cmax, cmax);
                this.addIndexedCuboid(x, y, z, cuboids, 1, b, c, a, 1.0, g, f);
                this.addIndexedCuboid(x, y, z, cuboids, 0, b, e, a, 1.0, d, f);
                this.addIndexedCuboid(x, y, z, cuboids, 2, b, a, e, 1.0, f, d);
                this.addIndexedCuboid(x, y, z, cuboids, 3, b, a, c, 1.0, f, g);
                this.addIndexedCuboid(x, y, z, cuboids, 7, 0.995, e, e, 1.0, g, g);
                break;
            case 5:
                this.addIndexedCuboid(x, y, z, cuboids, 6, 0.0, cmin, cmin, 0.2, cmax, cmax);
                this.addIndexedCuboid(x, y, z, cuboids, 1, 0.0, c, a, h, g, f);
                this.addIndexedCuboid(x, y, z, cuboids, 0, 0.0, e, a, h, d, f);
                this.addIndexedCuboid(x, y, z, cuboids, 2, 0.0, a, e, h, f, d);
                this.addIndexedCuboid(x, y, z, cuboids, 3, 0.0, a, c, h, f, g);
                this.addIndexedCuboid(x, y, z, cuboids, 7, 0.0, e, e, 0.005, g, g);
        }
    }

    @Override
    protected void setNoWandBlockBounds(World world, int x, int y, int z) {
        int orientation = this.getOrientation(world, x, y, z);
        switch (orientation) {
            case 0:
                this.setBlockBounds(0.12F, 0.8F, 0.12F, 0.88F, 1.0F, 0.88F);
                break;
            case 1:
            default:
                this.setBlockBounds(0.12F, 0.0F, 0.12F, 0.88F, 0.2F, 0.88F);
                break;
            case 2:
                this.setBlockBounds(0.12F, 0.12F, 0.8F, 0.88F, 0.88F, 1.0F);
                break;
            case 3:
                this.setBlockBounds(0.12F, 0.12F, 0.0F, 0.88F, 0.88F, 0.2F);
                break;
            case 4:
                this.setBlockBounds(0.8F, 0.12F, 0.12F, 1.0F, 0.88F, 0.88F);
                break;
            case 5:
                this.setBlockBounds(0.0F, 0.12F, 0.12F, 0.2F, 0.88F, 0.88F);
        }
    }
}
