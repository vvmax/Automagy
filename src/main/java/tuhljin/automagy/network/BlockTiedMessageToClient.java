package tuhljin.automagy.network;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;

public abstract class BlockTiedMessageToClient<T extends BlockTiedMessageToClient> implements IMessage, IMessageHandler<T, IMessage> {
    protected int dim;
    protected int x;
    protected int y;
    protected int z;

    public BlockTiedMessageToClient() {
    }

    public BlockTiedMessageToClient(int dim, int x, int y, int z) {
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
    public IMessage onMessage(T message, MessageContext ctx) {
        World world = FMLClientHandler.instance().getClient().theWorld;
        if (world.provider.dimensionId == message.dim) {
            message.onReceived(world);
        }

        return null;
    }

    public abstract void onReceived(World var1);
}
