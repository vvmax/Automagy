package tuhljin.automagy.api.nethermind;

import net.minecraft.world.World;
import thaumcraft.api.aspects.AspectList;

public interface INetherRuneBase {
    int getRuneType(World var1, int var2, int var3, int var4);

    int ticksUntilCreated(int var1);

    AspectList essentiaCostPerCreation(int var1);

    void activateRune(World var1, int var2, int var3, int var4, int var5);

    void deactivateRune(World var1, int var2, int var3, int var4, int var5);

    RuneCategory getRuneCategory(int var1);
}
