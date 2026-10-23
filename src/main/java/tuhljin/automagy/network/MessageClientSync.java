package tuhljin.automagy.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import tuhljin.automagy.lib.AutomagyConfig;

public class MessageClientSync implements IMessage, IMessageHandler<MessageClientSync, IMessage> {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @SideOnly(Side.CLIENT)
    public IMessage onMessage(MessageClientSync message, MessageContext ctx) {
        AutomagyConfig.populateBookshelvesList();
        return null;
    }

    public static void sendToClient(EntityPlayerMP player) {
        PacketHandler.INSTANCE.sendTo(new MessageClientSync(), player);
    }
}
