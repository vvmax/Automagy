package tuhljin.automagy.network;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.tiles.TileEntityEagerChest;

public class MessageGUIEagerChestClientUpdate implements IMessage, IMessageHandler<MessageGUIEagerChestClientUpdate, IMessage> {
    private int dim;
    private int x;
    private int y;
    private int z;
    boolean clientIsValidUser;

    public MessageGUIEagerChestClientUpdate() {
    }

    public MessageGUIEagerChestClientUpdate(int dim, int x, int y, int z, boolean clientIsValidUser) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.clientIsValidUser = clientIsValidUser;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.dim = buf.readInt();
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.clientIsValidUser = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.dim);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeBoolean(this.clientIsValidUser);
    }

    @SideOnly(Side.CLIENT)
    public IMessage onMessage(MessageGUIEagerChestClientUpdate message, MessageContext ctx) {
        World world = FMLClientHandler.instance().getClient().theWorld;
        if (world.provider.dimensionId == message.dim) {
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityEagerChest) {
                ((TileEntityEagerChest)te).receiveUserValidityFromServer(message.clientIsValidUser);
            }
        }

        return null;
    }

    public static void sendToClient(EntityPlayerMP player, World world, int x, int y, int z, boolean clientIsValidUser) {
        if (!world.isRemote) {
            int dim = world.provider.dimensionId;
            PacketHandler.INSTANCE.sendTo(new MessageGUIEagerChestClientUpdate(dim, x, y, z, clientIsValidUser), player);
        }
    }
}
