package tuhljin.automagy.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import tuhljin.automagy.gui.ContainerEnchantedPaper;

public class MessageGUIFilterPaper implements IMessage, IMessageHandler<MessageGUIFilterPaper, IMessage> {
    private int type;
    private String data;

    public MessageGUIFilterPaper() {
    }

    public MessageGUIFilterPaper(int type, String data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.type = buf.readByte();
        this.data = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.type);
        ByteBufUtils.writeUTF8String(buf, this.data);
    }

    public IMessage onMessage(MessageGUIFilterPaper message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player != null) {
            Container container = player.openContainer;
            if (container != null && container instanceof ContainerEnchantedPaper) {
                ((ContainerEnchantedPaper)container).receiveMessageFromClient(message.type, message.data);
            }
        }

        return null;
    }
}
