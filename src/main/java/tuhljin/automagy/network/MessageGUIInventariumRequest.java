package tuhljin.automagy.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.gui.ContainerScribe;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.tiles.IInventariumBook;
import tuhljin.automagy.tiles.IInventariumBookLinksMirror;
import tuhljin.automagy.tiles.TileEntityInventarium;

public class MessageGUIInventariumRequest implements IMessage, IMessageHandler<MessageGUIInventariumRequest, IMessage> {
    protected int dim;
    protected int x;
    protected int y;
    protected int z;
    protected int dimR;
    protected int xR;
    protected int yR;
    protected int zR;
    protected ItemStack stack;
    protected boolean rearrange;

    public MessageGUIInventariumRequest() {
    }

    public MessageGUIInventariumRequest(int dim, int x, int y, int z, ItemStack stack, boolean rearrange, int dimR, int xR, int yR, int zR) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.stack = stack;
        this.rearrange = rearrange;
        this.dimR = dimR;
        this.xR = xR;
        this.yR = yR;
        this.zR = zR;
    }

    public MessageGUIInventariumRequest(int dim, int x, int y, int z, ItemStack stack, boolean rearrange) {
        this(dim, x, y, z, stack, rearrange, -1, -1, -1, -1);
    }

    public MessageGUIInventariumRequest(World world, int x, int y, int z, ItemStack stack, boolean rearrange) {
        this(world.provider.dimensionId, x, y, z, stack, rearrange);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.dim = buf.readInt();
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.stack = TjUtil.itemStackFromBytes(buf);
        this.rearrange = buf.readBoolean();
        this.dimR = buf.readInt();
        this.xR = buf.readInt();
        this.yR = buf.readInt();
        this.zR = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.dim);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        TjUtil.itemStackToBytes(buf, this.stack);
        buf.writeBoolean(this.rearrange);
        buf.writeInt(this.dimR);
        buf.writeInt(this.xR);
        buf.writeInt(this.yR);
        buf.writeInt(this.zR);
    }

    public IMessage onMessage(MessageGUIInventariumRequest message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player == null || !(player.openContainer instanceof ContainerScribe)
            || message.stack == null || message.stack.getItem() == null
            || message.stack.stackSize == 0 || message.stack.stackSize == Integer.MIN_VALUE) {
            return null;
        }
        ContainerScribe container = (ContainerScribe)player.openContainer;
        IInventariumBook book = container.book;
        if (book == null || !book.canRequestItems() || !container.canInteractWith(player)) {
            return null;
        }
        WorldSpecificCoordinates link = book.getLinkCoord();
        if (link == null || player.worldObj.provider.dimensionId != link.dim
            || link.dim != message.dim || link.x != message.x || link.y != message.y || link.z != message.z
            || !player.worldObj.blockExists(link.x, link.y, link.z)) {
            return null;
        }
        // The open server-side book determines the destination, never another book named by the client.
        WorldSpecificCoordinates requester = book.getRequesterCoord();
        if (requester == null ? message.yR != -1 : requester.dim != message.dimR
            || requester.x != message.xR || requester.y != message.yR || requester.z != message.zR) {
            return null;
        }
        TileEntity te = player.worldObj.getTileEntity(link.x, link.y, link.z);
        if (te instanceof TileEntityInventarium) {
            TileEntityInventarium inventarium = (TileEntityInventarium)te;
            if (message.stack.stackSize > 0 && (long)Math.max(0,
                inventarium.getRequestedItems().get(new HashableItemWithoutSize(message.stack)))
                + message.stack.stackSize > Integer.MAX_VALUE) return null;
            IInventariumBookLinksMirror destination = book instanceof IInventariumBookLinksMirror
                && ((IInventariumBookLinksMirror)book).isOutputMirrorInRange() && inventarium.isTrackingDestinations()
                ? (IInventariumBookLinksMirror)book : null;
            inventarium.receiveRequestFromClient(message.stack, message.rearrange, destination);
        }

        return null;
    }

    public static void sendToServer(IInventariumBook book, ItemStack stack, boolean rearrange) {
        WorldSpecificCoordinates link = book.getLinkCoord();
        WorldSpecificCoordinates req = book.getRequesterCoord();
        if (req == null) {
            PacketHandler.INSTANCE.sendToServer(new MessageGUIInventariumRequest(link.dim, link.x, link.y, link.z, stack, rearrange));
        } else {
            PacketHandler.INSTANCE
                .sendToServer(new MessageGUIInventariumRequest(link.dim, link.x, link.y, link.z, stack, rearrange, req.dim, req.x, req.y, req.z));
        }
    }
}
