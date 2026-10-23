package tuhljin.automagy.network;

import cpw.mods.fml.common.FMLLog;
import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.gui.GUIScribe;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityInventarium;

public class MessageItemStacksRequested extends TemplateMessageItemStacksList<MessageItemStacksRequested> {
    public MessageItemStacksRequested() {
    }

    public MessageItemStacksRequested(int dim, int x, int y, int z, ArrayList<ItemStack> list, boolean partial) {
        super(dim, x, y, z, list, partial);
    }

    @Override
    public void onReceived(World world) {
        if (!GUIScribe.alwaysTreatChunkAsUnloaded && GUIScribe.chunkLoaded && TjUtil.isChunkLoaded(world, this.x, this.z)) {
            TileEntity te = world.getTileEntity(this.x, this.y, this.z);
            if (te instanceof TileEntityInventarium) {
                ((TileEntityInventarium)te).receiveItemsRequestedListPacket(this.list, this.partial);
            }
        } else {
            GUIScribe.handleStacksForUnloadedChunk(this.list, this.partial, true, false);
        }
    }

    public static void sendToClient(EntityPlayerMP player, World world, int x, int y, int z, ArrayList<ItemStack> list, boolean partial) {
        if (!world.isRemote) {
            int dim = world.provider.dimensionId;
            PacketHandler.INSTANCE.sendTo(new MessageItemStacksRequested(dim, x, y, z, list, partial), player);
        }
    }

    public static void sendToClient(EntityPlayer player, World world, int x, int y, int z, ArrayList<ItemStack> list, boolean partial) {
        if (player instanceof EntityPlayerMP) {
            sendToClient((EntityPlayerMP)player, world, x, y, z, list, partial);
        } else {
            FMLLog.warning("[Automagy] Could not send item requests packet to client. Player object was the wrong type!");
        }
    }

    public static void sendToClient(EntityPlayerMP player, int dim, int x, int y, int z, ArrayList<ItemStack> list, boolean partial) {
        PacketHandler.INSTANCE.sendTo(new MessageItemStacksRequested(dim, x, y, z, list, partial), player);
    }

    public static void sendToClient(EntityPlayer player, int dim, int x, int y, int z, ArrayList<ItemStack> list, boolean partial) {
        if (player instanceof EntityPlayerMP) {
            sendToClient((EntityPlayerMP)player, dim, x, y, z, list, partial);
        } else {
            FMLLog.warning("[Automagy] Could not send item requests packet to client. Player object was the wrong type!");
        }
    }
}
