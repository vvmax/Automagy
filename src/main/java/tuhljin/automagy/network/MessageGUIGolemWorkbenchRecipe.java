package tuhljin.automagy.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.tiles.TileEntityGolemWorkbench;

public class MessageGUIGolemWorkbenchRecipe implements IMessage, IMessageHandler<MessageGUIGolemWorkbenchRecipe, IMessage> {
    protected int dim;
    protected int x;
    protected int y;
    protected int z;
    protected ItemStack[] stacks;
    protected int recipeIndex;

    public MessageGUIGolemWorkbenchRecipe() {
    }

    public MessageGUIGolemWorkbenchRecipe(World world, int x, int y, int z, ItemStack[] stacks, int recipeIndex) {
        this.dim = world.provider.dimensionId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.stacks = stacks;
        this.recipeIndex = recipeIndex;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.dim = buf.readInt();
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.stacks = new ItemStack[9];

        for (int i = 0; i < 9; i++) {
            this.stacks[i] = ByteBufUtils.readItemStack(buf);
        }

        this.recipeIndex = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.dim);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);

        for (int i = 0; i < 9; i++) {
            ByteBufUtils.writeItemStack(buf, this.stacks[i]);
        }

        buf.writeInt(this.recipeIndex);
    }

    public IMessage onMessage(MessageGUIGolemWorkbenchRecipe message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player != null && player.worldObj.provider.dimensionId == message.dim) {
            TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityGolemWorkbench) {
                TileEntityGolemWorkbench teGW = (TileEntityGolemWorkbench)te;
                if (teGW.isUseableByPlayer(player)) {
                    teGW.receiveRecipeFromClient(message.recipeIndex, message.stacks);
                }
            }
        }

        return null;
    }

    public static void sendToServer(TileEntityGolemWorkbench te, ItemStack[] stacks, int recipeIndex) {
        World world = te.getWorld();
        if (world.isRemote) {
            PacketHandler.INSTANCE.sendToServer(new MessageGUIGolemWorkbenchRecipe(world, te.xCoord, te.yCoord, te.zCoord, stacks, recipeIndex));
        }
    }
}
