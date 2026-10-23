package tuhljin.automagy.entities;

import cpw.mods.fml.common.registry.EntityRegistry;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApi.EntityTagsNBT;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.lib.compat.CompatibilityManager;

public class ModEntities {
    public static void register() {
        int id = 0;
        EntityRegistry.registerModEntity(EntityAvaricePearl.class, "AvaricePearl", id++, Automagy.instance, 80, 3, true);
        EntityRegistry.registerModEntity(EntityXPOrbSuppressFollow.class, "XPOrbSuppressed", id++, Automagy.instance, 80, 3, true);
        EntityRegistry.registerModEntity(EntityItemDelayedFall.class, "EntityItemDelayedFall", id++, Automagy.instance, 64, 3, true);
        EntityRegistry.registerModEntity(EntityWispNether.class, "WispNether", id++, Automagy.instance, 64, 3, true);
    }

    public static void registerAspects() {
        for (Aspect tag : Aspect.aspects.values()) {
            AspectList aspects = new AspectList().add(tag, 2).add(Aspect.MAGIC, 1).add(Aspect.AIR, 1).add(Aspect.FIRE, 1);
            if (CompatibilityManager.ForbiddenMagicAspect.NETHER != null) {
                aspects.add(CompatibilityManager.ForbiddenMagicAspect.NETHER, 1);
            }

            ThaumcraftApi.registerEntityTag("Automagy.WispNether", aspects, new EntityTagsNBT("Type", tag.getTag()));
        }
    }
}
