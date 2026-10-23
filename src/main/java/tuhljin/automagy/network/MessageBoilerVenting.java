package tuhljin.automagy.network;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.tiles.TileEntityBoiler;

public class MessageBoilerVenting extends BlockTiedMessageToClient<MessageBoilerVenting> {
    public MessageBoilerVenting() {
    }

    public MessageBoilerVenting(int dim, int x, int y, int z) {
        super(dim, x, y, z);
    }

    @Override
    public void onReceived(World world) {
        TileEntity te = world.getTileEntity(this.x, this.y, this.z);
        if (te instanceof TileEntityBoiler) {
            ((TileEntityBoiler)te).startVenting();
        }
    }

    public static void sendToClients(int range, World world, int x, int y, int z) {
        if (!world.isRemote) {
            int dim = world.provider.dimensionId;
            TargetPoint point = new TargetPoint(dim, x, y, z, range);
            PacketHandler.INSTANCE.sendToAllAround(new MessageBoilerVenting(dim, x, y, z), point);
        }
    }

    public static void sendToClients(World world, int x, int y, int z) {
        sendToClients(20, world, x, y, z);
    }
}
