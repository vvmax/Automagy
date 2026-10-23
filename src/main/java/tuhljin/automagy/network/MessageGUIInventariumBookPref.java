package tuhljin.automagy.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import tuhljin.automagy.gui.ContainerScribe;
import tuhljin.automagy.tiles.IInventariumBook;

public class MessageGUIInventariumBookPref implements IMessage, IMessageHandler<MessageGUIInventariumBookPref, IMessage> {
    public static final int SHOWLOCAL = 0;
    public static final int SORTBYNAME = 1;
    private int type;
    private boolean data;

    public MessageGUIInventariumBookPref() {
    }

    public MessageGUIInventariumBookPref(int type, boolean data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.type = buf.readByte();
        this.data = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.type);
        buf.writeBoolean(this.data);
    }

    public IMessage onMessage(MessageGUIInventariumBookPref message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player != null) {
            Container container = player.openContainer;
            if (container != null && container instanceof ContainerScribe) {
                IInventariumBook book = ((ContainerScribe)container).book;
                if (message.type == 0) {
                    book.setPlayerLocalViewPreference(player, message.data);
                } else if (message.type == 1) {
                    book.setPlayerSortingPreference(player, message.data);
                }
            }
        }

        return null;
    }

    public static void sendToServer(int type, boolean data) {
        PacketHandler.INSTANCE.sendToServer(new MessageGUIInventariumBookPref(type, data));
    }
}
