package tuhljin.automagy.lib;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumcraft.common.items.wands.ItemWandCasting;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.items.ItemFocusCrafting;
import tuhljin.automagy.network.MessageKeyPressed;

public class AutomagyKeyHandler {
    public static KeyBinding keyCraft;
    public static long lastPressCraft = 0L;
    public static long minTimeBetweenPress = 500L;

    public AutomagyKeyHandler() {
        keyCraft = new KeyBinding("Automagy.key.focusCrafting", AutomagyConfig.defaultKeyCraft, "key.categories.misc");
        ClientRegistry.registerKeyBinding(keyCraft);
    }

    public static String getKeyName(KeyBinding key) {
        return Automagy.proxy.getKeyName(key.getKeyCode());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void playerTick(PlayerTickEvent event) {
        if (event.side != Side.SERVER) {
            if (keyCraft.getIsKeyPressed() && FMLClientHandler.instance().getClient().inGameHasFocus) {
                long t = System.currentTimeMillis();
                if (t - lastPressCraft >= minTimeBetweenPress) {
                    lastPressCraft = t;
                    EntityPlayer player = event.player;
                    boolean found = false;
                    boolean insufficientVis = false;
                    int suitableWandSlot = -1;
                    boolean suitableNonHeldWandIsStaff = false;
                    ItemStack stack = player.getCurrentEquippedItem();
                    ItemFocusCrafting focus = ItemFocusCrafting.getCraftingFocusOnWand(stack);
                    if (focus != null) {
                        if (focus.canPerformFunction(player, stack, false)) {
                            found = true;
                        } else {
                            insufficientVis = true;
                        }
                    }

                    if (!found && !insufficientVis && ThaumcraftExtension.playerHasWandEquippedNonSceptre(player)) {
                        suitableWandSlot = player.inventory.currentItem;
                    }

                    if (!found) {
                        int size = InventoryPlayer.getHotbarSize();

                        for (int slot = 0; slot < size; slot++) {
                            if (slot != player.inventory.currentItem) {
                                ItemStack stackx = player.inventory.getStackInSlot(slot);
                                if (stackx != null) {
                                    Item item = stackx.getItem();
                                    if (item instanceof ItemWandCasting) {
                                        ItemFocusCrafting focusx = ItemFocusCrafting.getCraftingFocusOnWand(stackx);
                                        if (focusx != null) {
                                            if (focusx.canPerformFunction(player, stackx, false)) {
                                                found = true;
                                                player.inventory.currentItem = slot;
                                                break;
                                            }

                                            insufficientVis = true;
                                        } else if (!((ItemWandCasting)item).isSceptre(stackx)) {
                                            if (suitableWandSlot == -1) {
                                                suitableWandSlot = slot;
                                                suitableNonHeldWandIsStaff = ThaumcraftExtension.wandIsStaff(stackx);
                                            } else if (suitableNonHeldWandIsStaff && !ThaumcraftExtension.wandIsStaff(stackx)) {
                                                suitableWandSlot = slot;
                                                suitableNonHeldWandIsStaff = false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (found) {
                        MessageKeyPressed.sendToServer((short)0, player.inventory.currentItem);
                    } else {
                        if (!found && suitableWandSlot != -1) {
                            stack = player.inventory.getStackInSlot(suitableWandSlot);
                            ItemStack oldFocus = ThaumcraftExtension.getWandFocusStack(stack);
                            if (ItemFocusCrafting.takeTransposingCraftingFocusForPlayer(player, oldFocus, false) != null) {
                                player.inventory.currentItem = suitableWandSlot;
                                MessageKeyPressed.sendToServer((short)1, suitableWandSlot);
                                insufficientVis = false;
                            }
                        }

                        if (insufficientVis) {
                            player.playSound("thaumcraft:wandfail", 0.1F, 0.8F + player.worldObj.rand.nextFloat() * 0.1F);
                        }
                    }
                }
            }
        }
    }
}
