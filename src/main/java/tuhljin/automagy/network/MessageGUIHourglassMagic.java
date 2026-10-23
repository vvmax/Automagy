package tuhljin.automagy.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import tuhljin.automagy.gui.ContainerHourglassMagic;
import tuhljin.automagy.tiles.TileEntityHourglassMagic;

public class MessageGUIHourglassMagic implements IMessage, IMessageHandler<MessageGUIHourglassMagic, IMessage> {
    private int type;
    private int data;

    public MessageGUIHourglassMagic() {
    }

    public MessageGUIHourglassMagic(int type, int data) {
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

    public IMessage onMessage(MessageGUIHourglassMagic message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player != null) {
            Container container = player.openContainer;
            if (container != null && container instanceof ContainerHourglassMagic) {
                TileEntityHourglassMagic teH = ((ContainerHourglassMagic)container).getTileEntity();
                teH.receiveGUIMessage(message.type, message.data);
            }
        }

        return null;
    }
}
