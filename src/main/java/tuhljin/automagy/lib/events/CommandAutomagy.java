package tuhljin.automagy.lib.events;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.research.ScanManager;
import tuhljin.automagy.network.MessageResetScannedObjects;

public class CommandAutomagy extends CommandBase {
    private List aliases = new ArrayList();

    public CommandAutomagy() {
        this.aliases.add("tca");
    }

    @Override
    public String getCommandName() {
        return "automagy";
    }

    @Override
    public List getCommandAliases() {
        return this.aliases;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] p_82358_1_, int i) {
        return i == 1;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/automagy scanneditem <player> reset <held|ALL>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] arr) {
        if (arr.length > 2) {
            EntityPlayerMP player = getPlayer(sender, arr[1]);
            if (arr[0].equalsIgnoreCase("scanneditem") && arr.length == 4 && arr[2].equalsIgnoreCase("reset")) {
                if (arr[3].equalsIgnoreCase("held")) {
                    ItemStack stack = player.getHeldItem();
                    if (stack == null) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Scan reset failed. You aren't holding an item."));
                    } else {
                        String targetName = player.getCommandSenderName();
                        int h = ScanManager.generateItemHash(stack.getItem(), stack.getMetadata());
                        List<String> list = Thaumcraft.proxy.getScannedObjects().get(targetName);
                        if (list != null && (list.contains("@" + h) || list.contains("#" + h))) {
                            list.remove("@" + h);
                            list.remove("#" + h);
                            MessageResetScannedObjects.resyncClientScannedItems(player);
                            String senderName = sender.getCommandSenderName();
                            String item = EnumChatFormatting.LIGHT_PURPLE + stack.getDisplayName() + EnumChatFormatting.DARK_PURPLE;
                            if (targetName.equals(senderName)) {
                                sender.addChatMessage(
                                    new ChatComponentText(EnumChatFormatting.DARK_PURPLE + "You removed " + item + " from your list of scanned items.")
                                );
                            } else {
                                sender.addChatMessage(
                                    new ChatComponentText(
                                        EnumChatFormatting.DARK_PURPLE
                                            + "Removed "
                                            + item
                                            + " from "
                                            + player.getCommandSenderName()
                                            + "'s list of scanned items."
                                    )
                                );
                                player.addChatMessage(
                                    new ChatComponentText(
                                        EnumChatFormatting.DARK_PURPLE
                                            + sender.getCommandSenderName()
                                            + " removed "
                                            + item
                                            + " from your list of scanned items."
                                    )
                                );
                            }
                        } else {
                            sender.addChatMessage(
                                new ChatComponentText(
                                    EnumChatFormatting.RED
                                        + "Scan reset failed. That item has no aspects or has not been scanned by "
                                        + player.getCommandSenderName()
                                        + "."
                                )
                            );
                        }
                    }
                } else if (arr[3].equals("ALL")) {
                    String targetName = player.getCommandSenderName();
                    String senderName = sender.getCommandSenderName();
                    Thaumcraft.proxy.getScannedObjects().remove(targetName);
                    MessageResetScannedObjects.sendToClient(player);
                    if (targetName.equals(senderName)) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_PURPLE + "You have EMPTIED your list of scanned items."));
                    } else {
                        sender.addChatMessage(
                            new ChatComponentText(EnumChatFormatting.DARK_PURPLE + player.getCommandSenderName() + "'s list of scanned items has been EMPTIED.")
                        );
                        player.addChatMessage(
                            new ChatComponentText(EnumChatFormatting.DARK_PURPLE + sender.getCommandSenderName() + " has EMPTIED your list of scanned items.")
                        );
                    }
                } else {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Scan reset failed. Expected argument of 'held' or 'ALL' (all caps)."));
                }

                return;
            }
        }

        if (arr.length != 0) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid arguments."));
        }

        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage: " + this.getCommandUsage(sender)));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You can also use /tca instead of /automagy."));
    }
}
