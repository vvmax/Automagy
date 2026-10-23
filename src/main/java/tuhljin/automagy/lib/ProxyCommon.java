package tuhljin.automagy.lib;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class ProxyCommon {
    public void init() {
    }

    public void registerKeyBindings() {
    }

    public void ghostlyBallStreamFX(
        World worldObj, double posX, double posY, double posZ, double posX2, double posY2, double posZ2, float size, int type, boolean shrink, float gravity
    ) {
    }

    public void oldBlockSparkle(World world, int x, int y, int z, int color, int count) {
    }

    public void xpDropletFX(World world, float i, float j, float k, float r, float g, float b) {
    }

    public String getKeyName(int key) {
        return "K";
    }

    public int getKeyIndex(String key) {
        return 0;
    }

    public World getWorldByID(int dimensionID) {
        return DimensionManager.getWorld(dimensionID);
    }
}
