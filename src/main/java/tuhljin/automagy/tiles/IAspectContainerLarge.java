package tuhljin.automagy.tiles;

import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;

public interface IAspectContainerLarge extends IAspectContainer {
    AspectList getAspectsReal();

    float getAspectVerticalShift(boolean var1);
}
