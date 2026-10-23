package tuhljin.automagy.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemEntityEye extends ModItem {
    public static final String TAG_MOB_IS_AGEABLE = "mobAges";
    public static final String TAG_IS_PLAYER = "entityIsPlayer";

    public ItemEntityEye(String name) {
        super(name);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity) {
        if (player.isSneaking()) {
            return false;
        } else {
            if (!player.worldObj.isRemote) {
                this.linkToEntity(player.getCurrentEquippedItem(), entity, player);
            } else {
                player.swingItem();
            }

            return true;
        }
    }

    public void linkToEntity(ItemStack stack, EntityLivingBase entity, EntityPlayer player) {
        if (entity instanceof EntityPlayer) {
            String name = entity.getCommandSenderName();
            if (name != null && !name.isEmpty()) {
                if (stack.stackTagCompound == null) {
                    stack.stackTagCompound = new NBTTagCompound();
                }

                stack.stackTagCompound.setString("mobName", name);
                stack.stackTagCompound.setBoolean("mobAges", false);
                stack.stackTagCompound.setBoolean("entityIsPlayer", true);
                if (player != null) {
                    player.worldObj.playSoundAtEntity(player, "thaumcraft:wand", 1.0F, 1.0F);
                    if (player != entity) {
                        player.swingItem();
                    }
                }
            }
        } else {
            String name = EntityList.getEntityString(entity);
            if (name != null && !name.isEmpty()) {
                if (stack.stackTagCompound == null) {
                    stack.stackTagCompound = new NBTTagCompound();
                }

                stack.stackTagCompound.setString("mobName", name);
                stack.stackTagCompound.setBoolean("mobAges", entity instanceof EntityAgeable);
                stack.stackTagCompound.removeTag("entityIsPlayer");
                if (player != null) {
                    player.worldObj.playSoundAtEntity(player, "thaumcraft:wand", 1.0F, 1.0F);
                    player.swingItem();
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("mobName")) {
            String name = getLocalizedEntityName(stack.stackTagCompound.getString("mobName"));
            if (stack.stackTagCompound.getBoolean("entityIsPlayer")) {
                list.add(
                    StatCollector.translateToLocalFormatted(
                        "Automagy.tip.crystalEntityEye.linkPlayer", EnumChatFormatting.AQUA + name + EnumChatFormatting.GRAY
                    )
                );
            } else {
                list.add(
                    StatCollector.translateToLocalFormatted("Automagy.tip.crystalEntityEye.link", EnumChatFormatting.DARK_AQUA + name + EnumChatFormatting.GRAY)
                );
            }
        } else {
            list.add(StatCollector.translateToLocal("Automagy.tip.crystalEntityEye.desc"));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack stack, int pass) {
        return stack.stackTagCompound != null && stack.stackTagCompound.hasKey("mobName");
    }

    public static boolean hasLinkedMob(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemEntityEye && stack.stackTagCompound != null && stack.stackTagCompound.hasKey("mobName");
    }

    public static String getLocalizedEntityName(String mobName) {
        String s = "entity." + mobName + ".name";
        String name = StatCollector.translateToLocal(s);
        return s.equals(name) ? mobName : name;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (player.isSneaking()) {
            if (stack.stackTagCompound != null
                && stack.stackTagCompound.getBoolean("entityIsPlayer")
                && stack.stackTagCompound.getString("mobName") == player.getCommandSenderName()) {
                return stack;
            }

            player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        }

        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 40;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.block;
    }

    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
        this.linkToEntity(stack, player, player);
        return stack;
    }

    public ItemStack createEyeAttunedTo(EntityLivingBase entity) {
        ItemStack stack = new ItemStack(this);
        this.linkToEntity(stack, entity, null);
        return stack;
    }
}
