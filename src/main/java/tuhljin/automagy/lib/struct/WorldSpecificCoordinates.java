package tuhljin.automagy.lib.struct;

import net.minecraft.world.World;

public class WorldSpecificCoordinates {
    public final int dim;
    public final int x;
    public final int y;
    public final int z;

    public WorldSpecificCoordinates(World world, int x, int y, int z) {
        this.dim = world.provider.dimensionId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public WorldSpecificCoordinates(int dimension, int x, int y, int z) {
        this.dim = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.dim;
        result = 31 * result + this.x;
        result = 31 * result + this.y;
        return 31 * result + this.z;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            WorldSpecificCoordinates other = (WorldSpecificCoordinates)obj;
            if (this.dim != other.dim) {
                return false;
            } else if (this.x != other.x) {
                return false;
            } else {
                return this.y != other.y ? false : this.z == other.z;
            }
        }
    }
}
