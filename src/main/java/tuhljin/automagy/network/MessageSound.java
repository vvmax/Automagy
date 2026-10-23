package tuhljin.automagy.network;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

public class MessageSound implements IMessage, IMessageHandler<MessageSound, IMessage> {
    public static final short WHISPERS = 0;
    protected short id;

    public MessageSound() {
    }

    public MessageSound(short id) {
        this.id = id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.id = buf.readShort();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(this.id);
    }

    @SideOnly(Side.CLIENT)
    public IMessage onMessage(MessageSound message, MessageContext ctx) {
        EntityClientPlayerMP player = FMLClientHandler.instance().getClientPlayerEntity();
        switch (message.id) {
            case 0:
                player.playSound("thaumcraft:whispers", 0.5F, 1.0F);
            default:
                return null;
        }
    }

    public static void sendToClient(EntityPlayerMP player, short id) {
        PacketHandler.INSTANCE.sendTo(new MessageSound(id), player);
    }

    public static void sendToClient(EntityPlayer player, short id) {
        if (player instanceof EntityPlayerMP) {
            sendToClient((EntityPlayerMP)player, id);
        } else {
            FMLLog.warning("[Automagy] Could not send sound packet to client. Player object was the wrong type!");
        }
    }

    public static void sendToClients(int range, World world, int x, int y, int z, short id) {
        if (!world.isRemote) {
            int dim = world.provider.dimensionId;
            TargetPoint point = new TargetPoint(dim, x, y, z, range);
            PacketHandler.INSTANCE.sendToAllAround(new MessageSound(id), point);
        }
    }

    public static void sendToClients(World world, int x, int y, int z, short id) {
        sendToClients(20, world, x, y, z, id);
    }
}
