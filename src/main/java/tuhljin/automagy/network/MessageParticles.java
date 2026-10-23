package tuhljin.automagy.network;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import thaumcraft.client.fx.bolt.FXLightningBolt;
import thaumcraft.common.Thaumcraft;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.struct.VectorSimple;
import tuhljin.automagy.tiles.TileEntityEssentiaLocus;
import tuhljin.automagy.tiles.TileEntityInventarium;
import tuhljin.automagy.tiles.TileEntityMirrorWithEye;

public class MessageParticles implements IMessage, IMessageHandler<MessageParticles, IMessage> {
    public static final short BREAKBLOCK = 0;
    public static final short BOLT_BLACK = 1;
    public static final short SMOKE = 2;
    public static final short ENDER_DISJUNCTION = 3;
    public static final short ABSORB_CLUSTER = 4;
    public static final short ENCHANTINGSYMBOLS = 5;
    public static final short ENCHANTINGSYMBOLS_LOW = 6;
    public static final short ENCHANTINGSYMBOLS_REVERSE = 7;
    public static final short GRABLIQUID = 8;
    public static final short NETHERPLACED = 9;
    public static final short NETHERPLACED_NOSOUND = 10;
    public static final short FLAME = 11;
    public static final short MIRROR_USED = 12;
    protected short id;
    protected int dim;
    protected int x;
    protected int y;
    protected int z;
    protected int x2;
    protected int y2;
    protected int z2;

    public MessageParticles() {
    }

    public MessageParticles(int dim, int x, int y, int z, int x2, int y2, int z2, short id) {
        this.id = id;
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.id = buf.readShort();
        this.dim = buf.readInt();
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.x2 = buf.readInt();
        this.y2 = buf.readInt();
        this.z2 = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(this.id);
        buf.writeInt(this.dim);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeInt(this.x2);
        buf.writeInt(this.y2);
        buf.writeInt(this.z2);
    }

    @SideOnly(Side.CLIENT)
    public IMessage onMessage(MessageParticles message, MessageContext ctx) {
        World world = FMLClientHandler.instance().getClient().theWorld;
        if (world.provider.dimensionId == message.dim) {
            this.dim = message.dim;
            this.x = message.x;
            this.y = message.y;
            this.z = message.z;
            this.x2 = message.x2;
            this.y2 = message.y2;
            this.z2 = message.z2;
            switch (message.id) {
                case 0:
                    this.showBreakBlock(world);
                    break;
                case 1:
                    for (int i = 0; i < 7; i++) {
                        this.showBolt(world, 5);
                    }

                    world.playSound(this.x + 0.5, this.y + 0.5, this.z + 0.5, "thaumcraft:zap", 0.2F, world.rand.nextFloat() * 0.5F + 0.4F, false);
                    world.playSound(this.x + 0.5, this.y + 0.5, this.z + 0.5, "random.eat", 0.7F, world.rand.nextFloat() * 0.5F + 0.1F, false);
                    world.playSound(this.x + 0.5, this.y + 0.5, this.z + 0.5, "random.eat", 0.5F, world.rand.nextFloat() * 0.5F + 1.0F, false);
                    break;
                case 2:
                    int num = this.x2 > 0 ? this.x2 : 35;

                    for (int i = 0; i < num; i++) {
                        this.showSmoke(world);
                    }
                    break;
                case 3:
                    int num = this.x2 > 0 ? this.x2 : 20;

                    for (int i = 0; i < num; i++) {
                        Thaumcraft.proxy.splooshFX(world, this.x, this.y, this.z);
                        Thaumcraft.proxy.splooshFX(world, this.x, this.y + 1, this.z);
                        Thaumcraft.proxy.splooshFX(world, this.x, this.y + 2, this.z);
                    }
                    break;
                case 4: {
                    TileEntity te = world.getTileEntity(this.x, this.y, this.z);
                    int color = 0;
                    if (te instanceof TileEntityEssentiaLocus) {
                        ((TileEntityEssentiaLocus)te).ticksToAnim = 40;
                        color = 2;
                    } else if (te instanceof TileEntityInventarium) {
                        ((TileEntityInventarium)te).ticksToAnim = 40;
                        color = 0;
                    }

                    this.blockSparkle(world, this.x, this.y, this.z, color, 5);
                    this.blockSparkle(world, this.x2, this.y2, this.z2, color, 30);
                    this.blockSparkle(world, this.x2 - 1, this.y2, this.z2 - 1, color, 2);
                    this.blockSparkle(world, this.x2 - 1, this.y2, this.z2, color, 2);
                    this.blockSparkle(world, this.x2 - 1, this.y2, this.z2 + 1, color, 2);
                    this.blockSparkle(world, this.x2, this.y2, this.z2 - 1, color, 2);
                    this.blockSparkle(world, this.x2, this.y2, this.z2 + 1, color, 2);
                    this.blockSparkle(world, this.x2 + 1, this.y2, this.z2 - 1, color, 2);
                    this.blockSparkle(world, this.x2 + 1, this.y2, this.z2, color, 2);
                    this.blockSparkle(world, this.x2 + 1, this.y2, this.z2 + 1, color, 2);
                    break;
                }
                case 5:
                case 6:
                    double destinationX;
                    double destinationY;
                    double destinationZ;
                    if (TjUtil.getDistanceBetweenPoints_squared(this.x, this.y, this.z, this.x2, this.y2, this.z2) >= 120.0) {
                        VectorSimple vec = new VectorSimple(this.x2, this.y2, this.z2, this.x, this.y, this.z);
                        vec.normalize();
                        vec.multiply(10.5);
                        destinationX = MathHelper.floor_double(this.x + vec.x);
                        destinationY = MathHelper.floor_double(this.y + vec.y + 1.8);
                        destinationZ = MathHelper.floor_double(this.z + vec.z);
                    } else {
                        destinationX = this.x2;
                        destinationY = this.y2;
                        destinationZ = this.z2;
                    }

                    double xx = destinationX + 0.5;
                    double yy = destinationY + 1.8;
                    double zz = destinationZ + 0.5;

                    for (int i = 0; i < 30; i++) {
                        world.spawnParticle(
                            "enchantmenttable",
                            xx,
                            yy,
                            zz,
                            (float)(this.x - destinationX) + world.rand.nextFloat() - 0.5,
                            (float)(this.y - destinationY) - world.rand.nextFloat() - 0.2F,
                            (float)(this.z - destinationZ) + world.rand.nextFloat() - 0.5
                        );
                    }

                    if (message.id == 6) {
                        this.blockSparkle(world, this.x, this.y, this.z, 6, 2);
                        this.blockSparkle(world, this.x, this.y, this.z, 0, 2);
                    } else {
                        this.blockSparkle(world, this.x, this.y + 1, this.z, 6, 2);
                        this.blockSparkle(world, this.x, this.y + 1, this.z, 0, 2);
                    }
                    break;
                case 7:
                    double xx = this.x + 0.5;
                    double yy = this.y + 1.8;
                    double zz = this.z + 0.5;

                    for (int i = 0; i < 30; i++) {
                        world.spawnParticle(
                            "enchantmenttable",
                            xx,
                            yy,
                            zz,
                            this.x2 - this.x + world.rand.nextFloat() - 0.5,
                            this.y2 - this.y - world.rand.nextFloat() - 0.2F,
                            this.z2 - this.z + world.rand.nextFloat() - 0.5
                        );
                    }

                    this.blockSparkle(world, this.x, this.y + 1, this.z, 6, 2);
                    this.blockSparkle(world, this.x, this.y + 1, this.z, 0, 2);
                    break;
                case 8: {
                    int color = 7;
                    this.blockSparkle(world, this.x, this.y, this.z, color, 7);
                    break;
                }
                case 9:
                case 10:
                    for (int i = 0; i < 35; i++) {
                        this.showSmoke(world);
                    }

                    int times = message.id == 10 ? 4 : 2;

                    for (int i = 0; i < times; i++) {
                        Thaumcraft.proxy.furnaceLavaFx(world, this.x, this.y, this.z, -1, -1);
                        Thaumcraft.proxy.furnaceLavaFx(world, this.x, this.y, this.z, -1, 1);
                        Thaumcraft.proxy.furnaceLavaFx(world, this.x, this.y, this.z, 1, -1);
                        Thaumcraft.proxy.furnaceLavaFx(world, this.x, this.y, this.z, 1, 1);
                    }

                    if (message.id != 10) {
                        world.playSound(this.x + 0.5, this.y + 0.5, this.z + 0.5, "mob.ghast.fireball", 0.5F, world.rand.nextFloat() * 0.5F + 0.2F, false);
                    }
                    break;
                case 11:
                    int num = this.x2 > 0 ? this.x2 : 35;

                    for (int i = 0; i < num; i++) {
                        this.showFlame(world);
                    }
                    break;
                case 12:
                    TileEntityMirrorWithEye.addParticles(world, this.x, this.y, this.z);
            }
        }

        return null;
    }

    @SideOnly(Side.CLIENT)
    private void showBreakBlock(World world) {
        try {
            Block block = world.getBlock(this.x, this.y, this.z);
            int md = world.getBlockMetadata(this.x, this.y, this.z);
            Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(this.x, this.y, this.z, block, md);
        } catch (Exception var4) {
        }
    }

    @SideOnly(Side.CLIENT)
    private void showBolt(World world, int color) {
        FXLightningBolt bolt = new FXLightningBolt(
            world,
            this.x + 0.5,
            this.y + 0.5,
            this.z + 0.5,
            this.x2 + 0.25 + world.rand.nextFloat() / 2.0F,
            this.y2 + 0.25 + world.rand.nextFloat() / 2.0F,
            this.z2 + 0.25 + world.rand.nextFloat() / 2.0F,
            world.rand.nextLong(),
            20,
            2.0F,
            1
        );
        bolt.defaultFractal();
        bolt.setType(color);
        bolt.setWidth(0.015F);
        bolt.finalizeBolt();
    }

    @SideOnly(Side.CLIENT)
    private void showSmoke(World world) {
        world.spawnParticle(
            "smoke",
            this.x + 0.5,
            this.y + 0.25 + world.rand.nextDouble(),
            this.z + 0.5,
            world.rand.nextGaussian() * 0.05,
            0.005,
            world.rand.nextGaussian() * 0.05
        );
    }

    @SideOnly(Side.CLIENT)
    private void showFlame(World world) {
        world.spawnParticle(
            "flame",
            this.x + 0.5 + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.5F,
            this.y + 0.25 + world.rand.nextDouble(),
            this.z + 0.5 + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.5F,
            world.rand.nextGaussian() * 0.05,
            0.005 + world.rand.nextDouble() * 0.1,
            world.rand.nextGaussian() * 0.05
        );
    }

    public static void sendToClients(int range, World world, int x, int y, int z, int x2, int y2, int z2, short id) {
        if (!world.isRemote) {
            int dim = world.provider.dimensionId;
            TargetPoint point = new TargetPoint(dim, x, y, z, range);
            PacketHandler.INSTANCE.sendToAllAround(new MessageParticles(dim, x, y, z, x2, y2, z2, id), point);
        }
    }

    public static void sendToClients(World world, int x, int y, int z, int x2, int y2, int z2, short id) {
        sendToClients(20, world, x, y, z, x2, y2, z2, id);
    }

    private void blockSparkle(World world, int x, int y, int z, int color, int count) {
        Automagy.proxy.oldBlockSparkle(world, x, y, z, color, count);
    }
}
