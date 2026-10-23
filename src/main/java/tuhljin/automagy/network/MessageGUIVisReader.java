package tuhljin.automagy.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import tuhljin.automagy.gui.ContainerVisReader;
import tuhljin.automagy.tiles.TileEntityVisReader;

public class MessageGUIVisReader implements IMessage, IMessageHandler<MessageGUIVisReader, IMessage> {
    private int buttonID;
    private int data;

    public MessageGUIVisReader() {
    }

    public MessageGUIVisReader(int buttonID, int data) {
        this.buttonID = buttonID;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.buttonID = buf.readByte();
        this.data = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.buttonID);
        buf.writeInt(this.data);
    }

    public IMessage onMessage(MessageGUIVisReader message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player != null) {
            Container container = player.openContainer;
            if (container != null && container instanceof ContainerVisReader) {
                TileEntityVisReader teVR = ((ContainerVisReader)container).getTileEntity();
                int configNum = message.buttonID / 4;
                int type = message.buttonID % 4;
                teVR.receiveGUIMessage(configNum, type, message.data);
            }
        }

        return null;
    }
}
