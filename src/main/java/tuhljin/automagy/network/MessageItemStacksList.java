package tuhljin.automagy.network;

import cpw.mods.fml.common.FMLLog;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.gui.GUIScribe;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.IReceivesItemListPacket;

public class MessageItemStacksList extends TemplateMessageItemStacksList<MessageItemStacksList> {
    protected boolean local;

    public MessageItemStacksList() {
    }

    public MessageItemStacksList(int dim, int x, int y, int z, ArrayList<ItemStack> list, boolean partial, boolean local) {
        super(dim, x, y, z, list, partial);
        this.local = local;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        this.local = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeBoolean(this.local);
    }

    @Override
    public void onReceived(World world) {
        if (!GUIScribe.alwaysTreatChunkAsUnloaded && GUIScribe.chunkLoaded && TjUtil.isChunkLoaded(world, this.x, this.z)) {
            TileEntity te = world.getTileEntity(this.x, this.y, this.z);
            if (te instanceof IReceivesItemListPacket) {
                ((IReceivesItemListPacket)te).receiveItemListPacket(this.list, this.partial);
            }
        } else {
            GUIScribe.handleStacksForUnloadedChunk(this.list, this.partial, false, this.local);
        }
    }

    public static void sendToClient(EntityPlayerMP player, World world, int x, int y, int z, ArrayList<ItemStack> list, boolean partial, boolean local) {
        if (!world.isRemote) {
            int dim = world.provider.dimensionId;
            PacketHandler.INSTANCE.sendTo(new MessageItemStacksList(dim, x, y, z, list, partial, local), player);
        }
    }

    public static void sendToClient(EntityPlayer player, World world, int x, int y, int z, ArrayList<ItemStack> list, boolean partial, boolean local) {
        if (player instanceof EntityPlayerMP) {
            sendToClient((EntityPlayerMP)player, world, x, y, z, list, partial, local);
        } else {
            FMLLog.warning("[Automagy] Could not send item list packet to client. Player object was the wrong type!");
        }
    }

    public static void sendToClient(EntityPlayerMP player, int dim, int x, int y, int z, ArrayList<ItemStack> list, boolean partial, boolean local) {
        PacketHandler.INSTANCE.sendTo(new MessageItemStacksList(dim, x, y, z, list, partial, local), player);
    }

    public static void sendToClient(EntityPlayer player, int dim, int x, int y, int z, ArrayList<ItemStack> list, boolean partial, boolean local) {
        if (player instanceof EntityPlayerMP) {
            sendToClient((EntityPlayerMP)player, dim, x, y, z, list, partial, local);
        } else {
            FMLLog.warning("[Automagy] Could not send item list packet to client. Player object was the wrong type!");
        }
    }
}
