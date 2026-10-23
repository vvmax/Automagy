package tuhljin.automagy.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import thaumcraft.common.items.wands.ItemWandCasting;
import tuhljin.automagy.items.ItemFocusCrafting;
import tuhljin.automagy.lib.ThaumcraftExtension;

public class MessageKeyPressed implements IMessage, IMessageHandler<MessageKeyPressed, IMessage> {
    public static final short FOCUS_CRAFTING = 0;
    public static final short FOCUS_CRAFTING_TRANSPOSE = 1;
    private int id;
    private int data;

    public MessageKeyPressed() {
    }

    public MessageKeyPressed(int id, int data) {
        this.id = id;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.id = buf.readByte();
        this.data = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.id);
        buf.writeByte(this.data);
    }

    public IMessage onMessage(MessageKeyPressed message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        if (player != null) {
            switch (message.id) {
                case 0:
                case 1:
                    int slot = message.data;
                    if (slot >= 0 && slot < InventoryPlayer.getHotbarSize()) {
                        ItemStack wand = player.inventory.getStackInSlot(slot);
                        if (wand != null && wand.getItem() instanceof ItemWandCasting) {
                            if (message.id == 1) {
                                if (((ItemWandCasting)wand.getItem()).isSceptre(wand)) {
                                    return null;
                                }

                                ItemStack oldFocusstack = ThaumcraftExtension.getWandFocusStack(wand);
                                ItemStack focusstack = ItemFocusCrafting.takeTransposingCraftingFocusForPlayer(player, oldFocusstack, true);
                                if (focusstack == null) {
                                    return null;
                                }

                                ThaumcraftExtension.applyWandFocus(wand, focusstack, true);
                                player.worldObj.playSoundAtEntity(player, "thaumcraft:cameraticks", 0.3F, 0.9F);
                            }

                            ItemFocusCrafting focus = ItemFocusCrafting.getCraftingFocusOnWand(wand);
                            if (focus != null) {
                                focus.openGUI(player, wand, slot);
                            }
                        }
                    }
            }
        }

        return null;
    }

    public static void sendToServer(short id, int data) {
        PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(id, data));
    }
}
