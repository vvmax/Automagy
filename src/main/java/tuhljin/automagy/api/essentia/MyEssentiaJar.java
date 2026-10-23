package tuhljin.automagy.api.essentia;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public abstract class MyEssentiaJar implements IAspectContainerWithMax {
    Aspect aspectContained;
    Aspect jarLabel;
    int maxAmount;
    int currentAmount;

    @Override
    public AspectList getAspectsBase() {
        Aspect a = this.aspectContained != null ? this.aspectContained : this.jarLabel;
        return a != null ? new AspectList().add(a, this.maxAmount) : null;
    }
}
