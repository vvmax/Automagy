package tuhljin.automagy.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thaumcraft.api.ItemApi;
import thaumcraft.common.tiles.TileJarFillable;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.tiles.TileEntityAmnesiacStone;
import tuhljin.automagy.tiles.TileEntityJarXP;

public class ItemPhialXP extends ModItem {
    public static int DEFAULT_XP = 95;
    private int xpValue = -1;

    public ItemPhialXP(String name) {
        super(name);
        ItemStack phial = ItemApi.getItem("itemEssence", 0);
        this.setContainerItem(phial.getItem());
    }

    @Override
    public boolean hasEffect(ItemStack par1ItemStack, int pass) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(StatCollector.translateToLocal("Automagy.tip.phialXP"));
    }

    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
        if (!player.capabilities.isCreativeMode) {
            stack.stackSize--;
        }

        if (!world.isRemote) {
            player.addExperience(this.getXPValue());
            world.playSoundAtEntity(player, "random.orb", 0.5F, 0.5F * ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.8F));
        }

        if (!player.capabilities.isCreativeMode) {
            ItemStack container = this.getContainerItem(stack).copy();
            if (stack.stackSize <= 0) {
                return container;
            }

            boolean added = player.inventory.addItemStackToInventory(container);
            if (!added && !world.isRemote) {
                TjUtil.dropItemIntoWorld(container, world, Math.round(player.posX), player.posY, player.posZ);
            }
        }

        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 12;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.drink;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
        return par1ItemStack;
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileJarFillable) {
            if (!world.isRemote) {
                TileEntityJarXP jar = TileEntityJarXP.transformWardedJar((TileJarFillable)te);
                if (jar != null && jar.addExactXP(this.getXPValue())) {
                    stack.stackSize--;
                    ItemStack container = this.getContainerItem(stack);
                    if (container != null) {
                        container = container.copy();
                        if (!player.inventory.addItemStackToInventory(container)) {
                            world.spawnEntityInWorld(new EntityItem(world, x + 0.5F, y + 0.5F, z + 0.5F, container));
                        }
                    }

                    world.playSoundAtEntity(player, "game.neutral.swim", 0.25F, 1.0F);
                    player.inventoryContainer.detectAndSendChanges();
                }

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public int getXPValue() {
        if (this.xpValue == -1) {
            TileEntityAmnesiacStone.ItemDrainResult drainResult = AutomagyConfig.amnesiacDrainItems
                .get(new HashableItemWithoutSize(new ItemStack(ModItems.phialXP)));
            if (drainResult == null) {
                this.xpValue = DEFAULT_XP;
            } else {
                this.xpValue = Math.max(drainResult.xp, 1);
            }
        }

        return this.xpValue;
    }
}
