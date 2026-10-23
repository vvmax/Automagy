package tuhljin.automagy.network;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.tiles.TileEntityHourglass;

public class MessageHourglassFlipped implements IMessage, IMessageHandler<MessageHourglassFlipped, IMessage> {
    private int dim;
    private int x;
    private int y;
    private int z;
    boolean flipState;

    public MessageHourglassFlipped() {
    }

    public MessageHourglassFlipped(int dim, int x, int y, int z, boolean flipState) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.flipState = flipState;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.dim = buf.readInt();
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.flipState = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.dim);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeBoolean(this.flipState);
    }

    @SideOnly(Side.CLIENT)
    public IMessage onMessage(MessageHourglassFlipped message, MessageContext ctx) {
        World world = FMLClientHandler.instance().getClient().theWorld;
        if (world.provider.dimensionId == message.dim) {
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityHourglass) {
                ((TileEntityHourglass)te).receiveFlipFromServer(message.flipState);
            }
        }

        return null;
    }

    public static void sendToClients(int range, World world, int x, int y, int z, boolean flipState) {
        if (!world.isRemote) {
            int dim = world.provider.dimensionId;
            TargetPoint point = new TargetPoint(dim, x, y, z, range);
            PacketHandler.INSTANCE.sendToAllAround(new MessageHourglassFlipped(dim, x, y, z, flipState), point);
        }
    }

    public static void sendToClients(World world, int x, int y, int z, boolean flipState) {
        sendToClients(20, world, x, y, z, flipState);
    }
}
