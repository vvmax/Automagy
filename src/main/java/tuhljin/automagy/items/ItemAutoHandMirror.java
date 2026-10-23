package tuhljin.automagy.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thaumcraft.common.items.relics.ItemHandMirror;
import thaumcraft.common.tiles.TileMirror;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.FilteringItemList;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;

public class ItemAutoHandMirror extends ItemHandMirror {
    public static final int NUM_FILTER_SLOTS = 2;

    public ItemAutoHandMirror(String name) {
        this.setMaxStackSize(1);
        this.setUnlocalizedName(name);
        this.setTextureName("Automagy:" + name);
        if (ModItems.tab != null) {
            this.setCreativeTab(ModItems.tab);
        }

        GameRegistry.registerItem(this, name);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.uncommon;
    }

    @Override
    public void registerIcons(IIconRegister ir) {
        IIcon icon = ir.registerIcon(this.getIconString());
        ReflectionHelper.setPrivateValue(ItemHandMirror.class, this, icon, "icon");
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && stack.hasTagCompound()) {
            int dim = stack.stackTagCompound.getInteger("linkDim");
            World targetWorld = MinecraftServer.getServer().worldServerForDimension(dim);
            if (targetWorld != null) {
                int x = stack.stackTagCompound.getInteger("linkX");
                int y = stack.stackTagCompound.getInteger("linkY");
                int z = stack.stackTagCompound.getInteger("linkZ");
                TileEntity te = targetWorld.getTileEntity(x, y, z);
                if (te != null && te instanceof TileMirror) {
                    player.openGui(
                        Automagy.instance,
                        12,
                        world,
                        MathHelper.floor_double(player.posX),
                        MathHelper.floor_double(player.posY),
                        MathHelper.floor_double(player.posZ)
                    );
                } else {
                    InventoryForItem inv = new InventoryForItemRestrictedToFilters(stack, "Hungry Hand Mirror Inventory", 2);
                    TjUtil.dropItemsIntoWorld(inv, world, player.posX, player.posY + 0.1, player.posZ);
                    stack.setTagCompound(null);
                    world.playSoundAtEntity(player, "thaumcraft:zap", 1.0F, 0.8F);
                    TjUtil.sendRawChatToPlayer(player, "ï¿½5ï¿½o" + StatCollector.translateToLocal("tc.handmirrorerror"), null);
                }
            }
        }

        return stack;
    }

    public int handlePickup(ItemStack mirror, FilteringItemList playerInvMap, HashableItemWithoutSize item, int amt, EntityPlayer player, World world) {
        if (amt > 0 && mirror.hasTagCompound()) {
            int toTake = CachedFilterData.getData(mirror).getMaxToMoveFromPlayer(playerInvMap, item);
            if (toTake > 0) {
                if (amt < toTake) {
                    toTake = amt;
                }

                ItemStack moveStack = item.getItemStack(toTake);
                ItemStack mirrorCopy = mirror.copy();
                if (transport(mirror, moveStack, player, world)) {
                    playerInvMap.subtract(item, toTake);
                    return amt - toTake;
                }

                double var10003 = player.posY + 0.1;
                TjUtil.dropItemsIntoWorld(CachedFilterData.getData(mirrorCopy).inventoryOfFilters, world, player.posX, var10003, player.posZ);
                CachedFilterData.clearData(mirrorCopy);
            }
        }

        return amt;
    }
}
