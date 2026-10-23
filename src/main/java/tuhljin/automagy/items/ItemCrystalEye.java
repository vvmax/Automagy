package tuhljin.automagy.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import tuhljin.automagy.lib.IAutomagyLocationLink;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

public class ItemCrystalEye extends ModItem implements IAutomagyLocationLink {
    public ItemCrystalEye(String name) {
        super(name);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
        if (!world.isRemote) {
            int dim = world.provider.dimensionId;
            if (stack.stackTagCompound == null) {
                stack.stackTagCompound = new NBTTagCompound();
            }

            stack.stackTagCompound.setInteger("lookX", x);
            stack.stackTagCompound.setInteger("lookY", y);
            stack.stackTagCompound.setInteger("lookZ", z);
            stack.stackTagCompound.setInteger("lookDim", dim);
            stack.stackTagCompound.setString("lookDimName", DimensionManager.getProvider(dim).getDimensionName());
            world.playSoundAtEntity(player, "thaumcraft:wand", 1.0F, 1.0F);
        }

        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("lookX")) {
            int x = stack.stackTagCompound.getInteger("lookX");
            int y = stack.stackTagCompound.getInteger("lookY");
            int z = stack.stackTagCompound.getInteger("lookZ");
            int dim = stack.stackTagCompound.getInteger("lookDim");
            String dimName = stack.stackTagCompound.getString("lookDimName");
            list.add(StatCollector.translateToLocalFormatted("Automagy.tip.crystalEye.link", x, y, z, dimName));
            if (player.worldObj.provider.dimensionId == dim) {
                float distance = TjUtil.getDistanceBetweenPoints(
                    MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ), x, y, z
                );
                String s = String.format("%.2f", distance);
                s = StatCollector.translateToLocalFormatted("Automagy.tip.crystalEye.linkDistance", s);
                String name = TjUtil.getBlockNameAt(player.worldObj, x, y, z);
                if (name != null) {
                    s = s + "  " + EnumChatFormatting.DARK_AQUA + "[" + name + "]";
                }

                list.add(EnumChatFormatting.DARK_GRAY + s);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack stack, int pass) {
        return stack.stackTagCompound != null && stack.stackTagCompound.hasKey("lookX");
    }

    @Override
    public WorldSpecificCoordinates getLinkLocation(ItemStack stack) {
        if (stack != null && stack.stackTagCompound != null && stack.stackTagCompound.hasKey("lookX")) {
            int x = stack.stackTagCompound.getInteger("lookX");
            int y = stack.stackTagCompound.getInteger("lookY");
            int z = stack.stackTagCompound.getInteger("lookZ");
            int dim = stack.stackTagCompound.getInteger("lookDim");
            return new WorldSpecificCoordinates(dim, x, y, z);
        } else {
            return null;
        }
    }
}
