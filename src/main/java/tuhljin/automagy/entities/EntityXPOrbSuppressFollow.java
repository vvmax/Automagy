package tuhljin.automagy.entities;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.ReflectionHelper;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Field;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.world.World;

public class EntityXPOrbSuppressFollow extends EntityXPOrb implements IEntityAdditionalSpawnData {
    public boolean isSuppressing = false;
    protected Field field_xpTargetColor;

    public EntityXPOrbSuppressFollow(World world) {
        super(world);
    }

    public EntityXPOrbSuppressFollow(World world, double x, double y, double z, int xpValue, int noPlayerFollowTime) {
        super(world, x, y, z, xpValue);
        if (noPlayerFollowTime > 0) {
            this.setNoFollow(noPlayerFollowTime);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.isSuppressing && this.field_70532_c == 0) {
            this.isSuppressing = false;

            try {
                this.field_xpTargetColor.setInt(this, 0);
            } catch (Exception var2) {
            }
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeBoolean(this.isSuppressing);
        buffer.writeInt(this.field_70532_c);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.isSuppressing = additionalData.readBoolean();
        int noPlayerFollowTime = additionalData.readInt();
        if (this.isSuppressing) {
            this.setNoFollow(noPlayerFollowTime);
        }
    }

    private void setNoFollow(int noPlayerFollowTime) {
        this.field_70532_c = noPlayerFollowTime;

        try {
            this.field_xpTargetColor = ReflectionHelper.findField(EntityXPOrb.class, "field_80002_g", "xpTargetColor");
            this.field_xpTargetColor.setInt(this, Integer.MAX_VALUE);
            this.isSuppressing = true;
        } catch (Exception var3) {
            FMLLog.severe("[Automagy] Failed to access xpTargetColor of class EntityXPOrb.");
        }
    }
}
