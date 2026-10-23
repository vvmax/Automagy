package tuhljin.automagy.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import tuhljin.automagy.gui.ContainerGreedyChestConfig;
import tuhljin.automagy.tiles.TileEntityGreedyChest;

public class MessageGUIGreedyChestConfig implements IMessage, IMessageHandler<MessageGUIGreedyChestConfig, IMessage> {
    private int type;
    private float data;

    public MessageGUIGreedyChestConfig() {
    }

    public MessageGUIGreedyChestConfig(int type, float data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.type = buf.readByte();
        this.data = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.type);
        buf.writeFloat(this.data);
    }

    public IMessage onMessage(MessageGUIGreedyChestConfig message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player != null) {
            Container container = player.openContainer;
            if (container != null && container instanceof ContainerGreedyChestConfig) {
                TileEntityGreedyChest teGC = ((ContainerGreedyChestConfig)container).getTileEntity();
                teGC.receiveGUIMessage(message.type, message.data);
            }
        }

        return null;
    }
}
