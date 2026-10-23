package tuhljin.automagy.entities;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Method;
import net.minecraft.entity.EntityLiving;
import org.apache.logging.log4j.Level;

public class DataFromEntity {
    protected static Method methodIsAIEnabled = ReflectionHelper.findMethod(EntityLiving.class, null, new String[]{"isAIEnabled", "func_70650_aV"});

    public static boolean isAIEnabled(EntityLiving entity) {
        try {
            return (Boolean)methodIsAIEnabled.invoke(entity);
        } catch (Exception var2) {
            FMLLog.log(Level.ERROR, "[Automagy] Failed to determine entity's AI type.");
            var2.printStackTrace();
            return false;
        }
    }
}
