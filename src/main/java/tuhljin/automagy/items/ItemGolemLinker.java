package tuhljin.automagy.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.golems.EntityGolemBase;
import tuhljin.automagy.entities.GolemLink;
import tuhljin.automagy.tiles.IGolemLinkableDevice;

public class ItemGolemLinker extends ModItem {
    public ItemGolemLinker(String name) {
        super(name);
        this.setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float par8, float par9, float par10) {
        if (player.isSneaking()) {
            if (stack.stackTagCompound != null) {
                stack.stackTagCompound.removeTag("linkX");
                stack.stackTagCompound.removeTag("linkY");
                stack.stackTagCompound.removeTag("linkZ");
                stack.stackTagCompound.removeTag("linkDim");
            }

            TileEntity te = world.getTileEntity(x, y, z);
            if (!world.isRemote && te instanceof IGolemLinkableDevice) {
                ((IGolemLinkableDevice)te).forgetGolemLinkID();
                world.playSoundAtEntity(player, "game.potion.smash", 1.0F, 0.6F + world.rand.nextFloat() / 4.0F);

                for (int i = 0; i < 10; i++) {
                    Thaumcraft.proxy
                        .sparkle(
                            x + 0.25F + world.rand.nextFloat() * 0.5F,
                            y + 0.65F + world.rand.nextFloat() * 0.15F,
                            z + 0.25F + world.rand.nextFloat() * 0.5F,
                            1.0F,
                            5,
                            -0.05F
                        );
                }
            }

            return true;
        } else {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof IGolemLinkableDevice) {
                if (!world.isRemote) {
                    int dim = world.provider.dimensionId;
                    stack.stackTagCompound = new NBTTagCompound();
                    stack.stackTagCompound.setInteger("linkX", x);
                    stack.stackTagCompound.setInteger("linkY", y);
                    stack.stackTagCompound.setInteger("linkZ", z);
                    stack.stackTagCompound.setInteger("linkDim", dim);
                    world.playSoundAtEntity(player, "random.orb", 0.7F, 0.8F + world.rand.nextFloat() * 0.1F);
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public boolean interactWithGolem(ItemStack stack, EntityPlayer player, EntityGolemBase golem) {
        if (player.isSneaking()) {
            return false;
        } else {
            if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("linkX")) {
                World world = player.worldObj;
                if (world.provider.dimensionId == stack.stackTagCompound.getInteger("linkDim")) {
                    player.swingItem();
                    if (!world.isRemote) {
                        IGolemLinkableDevice device = getLinkedDevice(stack, world);
                        if (device != null) {
                            GolemLink.establishLink(golem, device);
                            world.playSoundAtEntity(player, "random.orb", 0.7F, 0.8F + world.rand.nextFloat() * 0.1F);
                        }
                    }

                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack, int pass) {
        return stack.stackTagCompound != null && stack.stackTagCompound.hasKey("linkX");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        IGolemLinkableDevice device = getLinkedDevice(stack, player.worldObj);
        if (device != null) {
            int x = stack.stackTagCompound.getInteger("linkX");
            int y = stack.stackTagCompound.getInteger("linkY");
            int z = stack.stackTagCompound.getInteger("linkZ");
            Block block = player.worldObj.getBlock(x, y, z);
            if (block != null) {
                Item blockItem = block.getItem(player.worldObj, x, y, z);
                String name;
                if (blockItem == null) {
                    name = block.getUnlocalizedName();
                } else {
                    ItemStack blockStack = new ItemStack(blockItem, 1, block.getDamageValue(player.worldObj, x, y, z));
                    name = blockItem.getUnlocalizedName(blockStack);
                }

                if (name != null && !name.isEmpty()) {
                    name = StatCollector.translateToLocal(name + ".name");
                    if (name.substring(0, 5).equals("tile.") && name.substring(name.lastIndexOf(46) + 1).equals("name")) {
                        name = name.substring(name.indexOf(46) + 1, name.length() - 5);
                        name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    }

                    String s = StatCollector.translateToLocalFormatted("Automagy.tip.golemLinker.linkToBlock", EnumChatFormatting.DARK_AQUA + name);
                    list.add(EnumChatFormatting.DARK_GRAY + s);
                }
            }
        }
    }

    public static IGolemLinkableDevice getLinkedDevice(ItemStack stack, World world) {
        if (stack.stackTagCompound != null
            && stack.stackTagCompound.hasKey("linkX")
            && world.provider.dimensionId == stack.stackTagCompound.getInteger("linkDim")) {
            int x = stack.stackTagCompound.getInteger("linkX");
            int y = stack.stackTagCompound.getInteger("linkY");
            int z = stack.stackTagCompound.getInteger("linkZ");
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof IGolemLinkableDevice) {
                return (IGolemLinkableDevice)te;
            }
        }

        return null;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.uncommon;
    }
}
