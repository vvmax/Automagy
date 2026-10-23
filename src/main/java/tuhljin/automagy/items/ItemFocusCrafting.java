package tuhljin.automagy.items;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.items.wands.ItemFocusPouch;
import thaumcraft.common.items.wands.ItemWandCasting;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.lib.ThaumcraftExtension;

public class ItemFocusCrafting extends ModItemFocus {
    public static AspectList visCost = new AspectList().add(Aspect.ORDER, 7);
    public static AspectList visCostEmpty = new AspectList();
    protected IIcon iconDepth;
    protected IIcon iconOrnament;
    public static FocusUpgradeType gratis = ThaumcraftExtension.createFocusUpgrade(
        AutomagyConfig.focusUpgradeGratis, "gratis", new AspectList().add(Aspect.HUNGER, 1).add(Aspect.MAGIC, 1)
    );
    public static FocusUpgradeType transposing = ThaumcraftExtension.createFocusUpgrade(
        AutomagyConfig.focusUpgradeTransposing, "transposing", new AspectList().add(Aspect.EXCHANGE, 1).add(Aspect.TOOL, 1)
    );
    public static FocusUpgradeType storage = ThaumcraftExtension.createFocusUpgrade(
        AutomagyConfig.focusUpgradeStorage, "storage", new AspectList().add(Aspect.ELDRITCH, 1)
    );

    public ItemFocusCrafting(String name) {
        super(name);
    }

    @Override
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.iconDepth = ir.registerIcon(this.getIconString() + "-depth");
        this.iconOrnament = ir.registerIcon("Automagy:focus_orn_transposing");
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        return this.isUpgradedWith(focusstack, gratis) ? visCostEmpty : visCost;
    }

    @Override
    public int getFocusColor(ItemStack focusstack) {
        return 5030220;
    }

    @Override
    public IIcon getFocusDepthLayerIcon(ItemStack focusstack) {
        return this.iconDepth;
    }

    @Override
    public IIcon getOrnament(ItemStack focusstack) {
        return this.isUpgradedWith(focusstack, transposing) ? this.iconOrnament : null;
    }

    @Override
    public String getSortingHelper(ItemStack focusstack) {
        return "ACRAFT" + super.getSortingHelper(focusstack);
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        ArrayList<FocusUpgradeType> list = new ArrayList<>();
        if (!this.isUpgradedWith(focusstack, gratis)) {
            list.add(gratis);
        }

        if (!this.isUpgradedWith(focusstack, storage)) {
            list.add(storage);
        }

        if (!this.isUpgradedWith(focusstack, transposing)) {
            list.add(transposing);
        }

        return list.size() > 0 ? list.toArray(new FocusUpgradeType[list.size()]) : null;
    }

    @Override
    public boolean canApplyUpgrade(ItemStack focusstack, EntityPlayer player, FocusUpgradeType type, int rank) {
        return ThaumcraftExtension.isResearchComplete(player, "FOCUSCRAFTING_UPGRADE");
    }

    @Override
    public void addFocusInformation(ItemStack focusstack, EntityPlayer player, List list, boolean par4) {
        LinkedHashMap<Short, Integer> map = new LinkedHashMap<>();

        for (short id : this.getAppliedUpgrades(focusstack)) {
            if (id >= 0) {
                int amt = 1;
                if (map.containsKey(id)) {
                    amt = map.get(id) + 1;
                }

                map.put(id, amt);
            }
        }

        for (Short idx : map.keySet()) {
            String s = EnumChatFormatting.DARK_PURPLE
                + FocusUpgradeType.types[idx].getLocalizedName()
                + (map.get(idx) > 1 ? " " + StatCollector.translateToLocal("enchantment.level." + map.get(idx)) : "");
            if (idx == storage.id) {
                int count = 0;
                InventoryForItem inv = new InventoryForItem(focusstack, "Focus Inventory", 11);

                for (int i = 0; i < inv.getSizeInventory(); i++) {
                    if (inv.getStackInSlot(i) != null) {
                        count++;
                    }
                }

                if (count > 0) {
                    if (count == 1) {
                        if (count > 0) {
                            s = s + " " + EnumChatFormatting.LIGHT_PURPLE + StatCollector.translateToLocal("Automagy.tip.focusCrafting.stored1");
                        }
                    } else {
                        s = s + " " + EnumChatFormatting.LIGHT_PURPLE + StatCollector.translateToLocalFormatted("Automagy.tip.focusCrafting.stored", count);
                    }
                }
            }

            list.add(s);
        }
    }

    @Override
    public ItemStack onFocusRightClick(ItemStack wandstack, World world, EntityPlayer player, MovingObjectPosition movingobjectposition) {
        if (!world.isRemote) {
            this.openGUI(player, wandstack, player.inventory.currentItem);
        } else if (!this.canPerformFunction(player, wandstack, false)) {
            player.playSound("thaumcraft:wandfail", 0.1F, 0.8F + player.worldObj.rand.nextFloat() * 0.1F);
        }

        return wandstack;
    }

    public boolean canPerformFunction(EntityPlayer player, ItemStack wand, boolean doConsumeVis) {
        ItemStack focusstack = ((ItemWandCasting)wand.getItem()).getFocusItem(wand);
        return this.isUpgradedWith(focusstack, gratis) || ThaumcraftApiHelper.consumeVisFromWand(wand, player, visCost, doConsumeVis, false);
    }

    public void openGUI(EntityPlayer player, ItemStack wand, int slot) {
        if (this.canPerformFunction(player, wand, true)) {
            FMLNetworkHandler.openGui(player, Automagy.instance, 14, player.worldObj, slot, -1, 0);
        }
    }

    public static boolean isWandWithCraftingFocus(ItemStack stack) {
        return stack != null ? ThaumcraftExtension.getWandFocus(stack) instanceof ItemFocusCrafting : false;
    }

    public static ItemFocusCrafting getCraftingFocusOnWand(ItemStack stack) {
        if (stack != null) {
            ItemFocusBasic focus = ThaumcraftExtension.getWandFocus(stack);
            if (focus instanceof ItemFocusCrafting) {
                return (ItemFocusCrafting)focus;
            }
        }

        return null;
    }

    public static boolean wandHasRequiredVis(EntityPlayer player, ItemStack wandstack, boolean useDefaultCostIfWrongFocus) {
        ItemFocusCrafting focus = getCraftingFocusOnWand(wandstack);
        if (focus == null) {
            return useDefaultCostIfWrongFocus ? ThaumcraftApiHelper.consumeVisFromWand(wandstack, player, visCost, false, false) : false;
        } else {
            return focus.canPerformFunction(player, wandstack, false);
        }
    }

    public static ItemStack takeTransposingCraftingFocusForPlayer(EntityPlayer player, ItemStack oldFocus, boolean doit) {
        ItemStack result = takeTransposingCraftingFocusForInventory(player.inventory, oldFocus, doit);
        if (result == null) {
            IInventory baubles = BaublesApi.getBaubles(player);
            if (baubles != null) {
                result = takeTransposingCraftingFocusForInventory(baubles, oldFocus, doit);
            }
        }

        return result;
    }

    public static ItemStack takeTransposingCraftingFocusForInventory(IInventory inv, ItemStack oldFocus, boolean doit) {
        int size = inv.getSizeInventory();

        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null) {
                Item item = stack.getItem();
                if (item instanceof ItemFocusCrafting) {
                    if (((ItemFocusCrafting)item).isUpgradedWith(stack, transposing)) {
                        if (doit) {
                            inv.setInventorySlotContents(i, oldFocus);
                        }

                        return stack;
                    }
                } else if (item instanceof ItemFocusPouch) {
                    ItemFocusPouch pouch = (ItemFocusPouch)item;
                    ItemStack[] pouchInv = pouch.getInventory(stack);

                    for (int j = 0; j < pouchInv.length; j++) {
                        ItemStack stackInPouch = pouchInv[j];
                        if (stackInPouch != null) {
                            Item itemInPouch = stackInPouch.getItem();
                            if (itemInPouch instanceof ItemFocusCrafting && ((ItemFocusCrafting)itemInPouch).isUpgradedWith(stackInPouch, transposing)) {
                                if (doit) {
                                    pouchInv[j] = oldFocus;
                                    pouch.setInventory(stack, pouchInv);
                                    inv.setInventorySlotContents(i, stack);
                                }

                                return stackInPouch;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }
}
