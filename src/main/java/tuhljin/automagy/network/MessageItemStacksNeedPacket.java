package tuhljin.automagy.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import tuhljin.automagy.gui.AutomagyGUIHandler;
import tuhljin.automagy.gui.ContainerScribe;
import tuhljin.automagy.tiles.IInventariumBook;

public class MessageItemStacksNeedPacket implements IMessage, IMessageHandler<MessageItemStacksNeedPacket, IMessage> {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public IMessage onMessage(MessageItemStacksNeedPacket message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player != null) {
            Container container = player.openContainer;
            if (container != null && container instanceof ContainerScribe) {
                TileEntity te = ((ContainerScribe)container).getTile();
                if (te != null) {
                    IInventariumBook book = ((ContainerScribe)container).book;
                    AutomagyGUIHandler.sendInventariumContents(book, player, player.worldObj, te.xCoord, te.yCoord, te.zCoord);
                }
            }
        }

        return null;
    }

    public static void sendToServer() {
        PacketHandler.INSTANCE.sendToServer(new MessageItemStacksNeedPacket());
    }
}
