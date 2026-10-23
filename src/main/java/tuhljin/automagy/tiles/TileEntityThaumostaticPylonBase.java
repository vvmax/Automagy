package tuhljin.automagy.tiles;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import tuhljin.automagy.api.essentia.IAspectContainerWithMax;

public abstract class TileEntityThaumostaticPylonBase extends ModTileEntity implements IAspectContainerWithMax {
    public abstract void assureValidity();

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return false;
    }

    @Override
    public int addToContainer(Aspect tag, int amount) {
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList ot) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContain(AspectList ot) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return 0;
    }
}
