package tuhljin.automagy.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import tuhljin.automagy.gui.ContainerEntitySignal;
import tuhljin.automagy.tiles.TileEntityEntitySignal;

public class MessageGUIEntitySignal implements IMessage, IMessageHandler<MessageGUIEntitySignal, IMessage> {
    private int type;
    private int data;

    public MessageGUIEntitySignal() {
    }

    public MessageGUIEntitySignal(int type, int data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.type = buf.readByte();
        this.data = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.type);
        buf.writeInt(this.data);
    }

    public IMessage onMessage(MessageGUIEntitySignal message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player != null) {
            Container container = player.openContainer;
            if (container != null && container instanceof ContainerEntitySignal) {
                TileEntityEntitySignal teES = ((ContainerEntitySignal)container).getTileEntity();
                teES.receiveGUIMessage(message.type, message.data);
            }
        }

        return null;
    }
}
