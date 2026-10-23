package tuhljin.automagy.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import tuhljin.automagy.gui.ContainerTallyBlock;
import tuhljin.automagy.tiles.TileEntityTallyBase;

public class MessageGUITallyBlock implements IMessage, IMessageHandler<MessageGUITallyBlock, IMessage> {
    private int type;
    private boolean data;

    public MessageGUITallyBlock() {
    }

    public MessageGUITallyBlock(int type, boolean data) {
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

    public IMessage onMessage(MessageGUITallyBlock message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player != null) {
            Container container = player.openContainer;
            if (container != null && container instanceof ContainerTallyBlock) {
                TileEntityTallyBase teT = ((ContainerTallyBlock)container).getTileEntity();
                teT.receiveGUIMessage(message.type, message.data);
            }
        }

        return null;
    }
}
