package tuhljin.automagy.network;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXSparkle;
import thaumcraft.common.Thaumcraft;

public class MessageFloatXYZ implements IMessage, IMessageHandler<MessageFloatXYZ, IMessage> {
    public static final short EMBER_RANDOM = 0;
    public static final short GRABLIQUID = 1;
    protected short id;
    protected int dim;
    float x;
    float y;
    float z;

    public MessageFloatXYZ() {
    }

    public MessageFloatXYZ(int dim, float x, float y, float z, short id) {
        this.id = id;
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.id = buf.readShort();
        this.dim = buf.readInt();
        this.x = buf.readFloat();
        this.y = buf.readFloat();
        this.z = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(this.id);
        buf.writeInt(this.dim);
        buf.writeFloat(this.x);
        buf.writeFloat(this.y);
        buf.writeFloat(this.z);
    }

    @SideOnly(Side.CLIENT)
    public IMessage onMessage(MessageFloatXYZ message, MessageContext ctx) {
        World world = FMLClientHandler.instance().getClient().theWorld;
        if (world.provider.dimensionId == message.dim) {
            this.dim = message.dim;
            this.x = message.x;
            this.y = message.y;
            this.z = message.z;
            switch (message.id) {
                case 0:
                    int r = world.rand.nextInt(10);
                    String p;
                    if (r < 5) {
                        p = "smoke";
                    } else if (r < 8) {
                        p = "largesmoke";
                    } else {
                        p = "flame";
                    }

                    world.spawnParticle(p, this.x, this.y, this.z, 0.0, 0.0, 0.0);
                    break;
                case 1:
                    int color = 7;
                    this.sparkle(world, this.x, this.y, this.z, color, 7);
            }
        }

        return null;
    }

    @SideOnly(Side.CLIENT)
    private void sparkle(World world, float x2, float y2, float z2, int color, int count) {
        for (int a = 0; a < Thaumcraft.proxy.particleCount(count); a++) {
            FXSparkle fx = new FXSparkle(
                world,
                this.x + world.rand.nextFloat(),
                this.y + world.rand.nextFloat(),
                this.z + world.rand.nextFloat(),
                1.75F,
                color == -1 ? world.rand.nextInt(5) : color,
                3 + world.rand.nextInt(3)
            );
            fx.setGravity(0.2F);
            ParticleEngine.instance.addEffect(world, fx);
        }
    }

    public static void sendToClients(int range, World world, float x, float y, float z, short id) {
        if (!world.isRemote) {
            int dim = world.provider.dimensionId;
            TargetPoint point = new TargetPoint(dim, x, y, z, range);
            PacketHandler.INSTANCE.sendToAllAround(new MessageFloatXYZ(dim, x, y, z, id), point);
        }
    }

    public static void sendToClients(World world, double x, double y, double z, short id) {
        sendToClients(20, world, (float)x, (float)y, (float)z, id);
    }
}
