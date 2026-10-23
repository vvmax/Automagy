package tuhljin.automagy.renderers;

import cpw.mods.fml.common.FMLLog;
import java.lang.reflect.Field;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import tuhljin.automagy.entities.IItemMetadata;

public class RenderItemProjectileWithEntityMetadata extends RenderSnowball {
    private static boolean didReflectionAttempt = false;
    private Field metadata;
    private Entity cachedEntity = null;

    public RenderItemProjectileWithEntityMetadata(Item item) {
        super(item);
        if (!didReflectionAttempt) {
            try {
                this.metadata = this.getClass().getSuperclass().getDeclaredField("field_94150_f");
                this.metadata.setAccessible(true);
            } catch (Exception var3) {
                FMLLog.severe("[Automagy] Failed to access RenderSnowball's metadata field. Improper icon rendering of projectile items may result.");
            }

            didReflectionAttempt = true;
        }
    }

    @Override
    public void doRender(Entity entity, double par2, double par4, double par6, float par8, float par9) {
        if (this.cachedEntity != entity && entity instanceof IItemMetadata) {
            this.cachedEntity = entity;

            try {
                this.metadata.setInt(this, ((IItemMetadata)entity).getItemMetadata());
            } catch (Exception var11) {
            }
        }

        super.doRender(entity, par2, par4, par6, par8, par9);
    }
}
