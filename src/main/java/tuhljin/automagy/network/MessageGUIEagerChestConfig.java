package tuhljin.automagy.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import tuhljin.automagy.gui.ContainerEagerChestConfig;
import tuhljin.automagy.tiles.TileEntityEagerChest;

public class MessageGUIEagerChestConfig implements IMessage, IMessageHandler<MessageGUIEagerChestConfig, IMessage> {
    private boolean data;

    public MessageGUIEagerChestConfig() {
    }

    public MessageGUIEagerChestConfig(boolean data) {
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.data = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.data);
    }

    public IMessage onMessage(MessageGUIEagerChestConfig message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player != null) {
            Container container = player.openContainer;
            if (container != null && container instanceof ContainerEagerChestConfig) {
                TileEntityEagerChest teEC = ((ContainerEagerChestConfig)container).getTileEntity();
                teEC.setTradeWithPlayer(player, message.data);
            }
        }

        return null;
    }
}
