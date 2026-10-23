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
import tuhljin.automagy.tiles.TileEntityThaumostaticController;
import tuhljin.automagy.tiles.TileEntityThaumostaticRefuelerSpire;

public class MessageThaumostaticSparks implements IMessage, IMessageHandler<MessageThaumostaticSparks, IMessage> {
    private int dim;
    private int x;
    private int y;
    private int z;

    public MessageThaumostaticSparks() {
    }

    public MessageThaumostaticSparks(int dim, int x, int y, int z) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.dim = buf.readInt();
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.dim);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
    }

    @SideOnly(Side.CLIENT)
    public IMessage onMessage(MessageThaumostaticSparks message, MessageContext ctx) {
        World world = FMLClientHandler.instance().getClient().theWorld;
        if (world.provider.dimensionId == message.dim) {
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityThaumostaticRefuelerSpire) {
                ((TileEntityThaumostaticRefuelerSpire)te).doSparks();
            }
        }

        return null;
    }

    public static void sendToClients(int range, World world, int x, int y, int z) {
        if (!world.isRemote) {
            int dim = world.provider.dimensionId;
            TargetPoint point = new TargetPoint(dim, x, y, z, range);
            PacketHandler.INSTANCE.sendToAllAround(new MessageThaumostaticSparks(dim, x, y, z), point);
        }
    }

    public static void sendToClients(World world, int x, int y, int z) {
        int range = TileEntityThaumostaticController.MAX_EFFECTIVE_PYLONS * TileEntityThaumostaticController.RANGE_UPGRADE_PER_PYLON + 10;
        sendToClients(range, world, x, y, z);
    }
}
