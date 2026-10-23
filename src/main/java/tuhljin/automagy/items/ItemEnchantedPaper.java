package tuhljin.automagy.items;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import tuhljin.automagy.Automagy;

public class ItemEnchantedPaper extends ModSubtypedItem {
    public static final int NUM_SLOTS = 9;
    public static final int STACK_LIMIT = 999;
    protected IIcon[] icons = new IIcon[3];
    protected static ItemStack examinedStack = null;
    protected static ArrayList<String> examinedStackResults = null;

    public ItemEnchantedPaper(String name) {
        super(name, 4);
    }

    @Override
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.icons[0] = ir.registerIcon("Automagy:enchantedPaperWhitelist");
        this.icons[1] = ir.registerIcon("Automagy:enchantedPaperBlacklist");
        this.icons[2] = ir.registerIcon("Automagy:blankEnchantedBook");
    }

    @Override
    public IIcon getIconFromDamage(int d) {
        return d != 0 && d <= this.icons.length ? this.icons[d - 1] : super.getIconFromDamage(d);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        int d = stack.getMetadata();
        return d != 1 && d != 2 ? 64 : 1;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            int meta = stack.getMetadata();
            if (meta == 1) {
                FMLNetworkHandler.openGui(player, Automagy.instance, 4, world, (int)player.posX, (int)player.posY, (int)player.posZ);
            } else if (meta == 2) {
                FMLNetworkHandler.openGui(player, Automagy.instance, 5, world, (int)player.posX, (int)player.posY, (int)player.posZ);
            }
        }

        return stack;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        if (!ItemStack.areItemStacksEqual(stack, examinedStack)) {
            examinedStack = stack;
            examinedStackResults = new ArrayList<>();
            InventoryWithFilterOptions inv = getFilterInventory(stack);
            if (inv != null) {
                int count = 0;

                for (int i = 0; i < 9; i++) {
                    ItemStack slotStack = inv.getStackInSlot(i);
                    if (slotStack != null) {
                        String s = slotStack.getDisplayName();
                        if (inv.useItemCount) {
                            s = "" + EnumChatFormatting.DARK_AQUA + slotStack.stackSize + " " + EnumChatFormatting.GRAY + s;
                        }

                        examinedStackResults.add(s);
                        count++;
                    }
                }

                if (count > 0) {
                    if (inv.ignoreMetadata) {
                        examinedStackResults.add(
                            ""
                                + EnumChatFormatting.DARK_GRAY
                                + EnumChatFormatting.ITALIC
                                + StatCollector.translateToLocal("Automagy.gui.filter.ignoreMetadata")
                        );
                    }

                    if (inv.ignoreNBT) {
                        examinedStackResults.add(
                            "" + EnumChatFormatting.DARK_GRAY + EnumChatFormatting.ITALIC + StatCollector.translateToLocal("Automagy.gui.filter.ignoreNBT")
                        );
                    }
                }

                if (!inv.nameFilter.isEmpty()) {
                    examinedStackResults.add(EnumChatFormatting.AQUA + "\"" + inv.nameFilter + "\"");
                }
            }
        }

        for (String line : examinedStackResults) {
            list.add(line);
        }
    }

    public static InventoryWithFilterOptions getFilterInventory(ItemStack stack) {
        return stackIsFilter(stack) ? new InventoryWithFilterOptions(stack, "Filter Inventory", 9, 999) : null;
    }

    public static boolean stackIsFilter(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemEnchantedPaper) {
            int meta = stack.getMetadata();
            if (meta == 1 || meta == 2) {
                return true;
            }
        }

        return false;
    }

    public static boolean stackIsWhitelist(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemEnchantedPaper) {
            int meta = stack.getMetadata();
            if (meta == 1) {
                return true;
            }
        }

        return false;
    }

    public static boolean stackIsBlacklist(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemEnchantedPaper) {
            int meta = stack.getMetadata();
            if (meta == 2) {
                return true;
            }
        }

        return false;
    }
}
