package tuhljin.automagy.api.nethermind;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import thaumcraft.api.aspects.AspectList;

public interface INetherRuneCreature extends INetherRuneBase {
    Entity entityCreated(int var1, World var2);

    int getInstability(int var1);

    int getSpawnCount(int var1);

    AspectList secondaryEssentiaCost(int var1);

    int secondaryEssentiaCostUses(int var1);

    String getEntityIdentifier(int var1);

    void onEntitySpawned(int var1, Entity var2);

    int requiredLureDistance(World var1, int var2, int var3, int var4);
}
