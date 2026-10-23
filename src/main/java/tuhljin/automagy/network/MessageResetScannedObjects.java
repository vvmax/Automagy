package tuhljin.automagy.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.network.playerdata.PacketSyncScannedItems;

public class MessageResetScannedObjects implements IMessage, IMessageHandler<MessageResetScannedObjects, IMessage> {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @SideOnly(Side.CLIENT)
    public IMessage onMessage(MessageResetScannedObjects message, MessageContext ctx) {
        String name = Minecraft.getMinecraft().thePlayer.getCommandSenderName();
        Thaumcraft.proxy.getScannedObjects().remove(name);
        return null;
    }

    public static void sendToClient(EntityPlayerMP player) {
        PacketHandler.INSTANCE.sendTo(new MessageResetScannedObjects(), player);
    }

    public static void resyncClientScannedItems(EntityPlayerMP player) {
        sendToClient(player);
        thaumcraft.common.lib.network.PacketHandler.INSTANCE.sendTo(new PacketSyncScannedItems(player), player);
    }
}
