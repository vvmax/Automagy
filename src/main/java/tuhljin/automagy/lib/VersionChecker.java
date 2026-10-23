package tuhljin.automagy.lib;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import net.minecraft.util.IChatComponent.Serializer;

public class VersionChecker {
    public static boolean checkComplete = false;
    public static String latestVersion = "";
    public static ArrayList<String> latestDesc = new ArrayList<>();

    public VersionChecker() {
        new VersionCheckerThread();
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (checkComplete && event.phase == Phase.END && Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.worldObj != null) {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if (player.worldObj.provider.worldObj == null || player.worldObj.getChunkProvider() == null) {
                return;
            }

            FMLCommonHandler.instance().bus().unregister(this);
            if (latestVersion != null && !latestVersion.isEmpty() && !latestVersion.equals("0.28.2")) {
                String message = StatCollector.translateToLocalFormatted("Automagy.chat.newVersionAvailable", latestVersion, "0.28.2");
                TjUtil.sendRawChatToPlayer(player, message, null);
                if (latestDesc.size() > 0) {
                    int failsafe = 1;

                    for (String s : latestDesc) {
                        TjUtil.sendRawChatToPlayer(player, EnumChatFormatting.ITALIC + s, EnumChatFormatting.GRAY);
                        if (++failsafe >= 10) {
                            break;
                        }
                    }
                }

                message = StatCollector.translateToLocal("Automagy.chat.newVersionLink");
                IChatComponent component = Serializer.jsonToComponent(message);
                player.addChatMessage(component);
            }
        }
    }
}
