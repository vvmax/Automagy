package tuhljin.automagy.lib.events;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import tuhljin.automagy.config.ModResearchItems;

public class ClientEventHandler {
    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (event.phase == Phase.END) {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if (player != null && player.worldObj != null) {
                if (player.worldObj.provider.worldObj == null || player.worldObj.getChunkProvider() == null) {
                    return;
                }

                FMLCommonHandler.instance().bus().unregister(this);
                ModResearchItems.makeClientAdjustments();
            }
        }
    }
}
