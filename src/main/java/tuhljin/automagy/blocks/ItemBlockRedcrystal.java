package tuhljin.automagy.blocks;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityRedcrystalMerc;

public class ItemBlockRedcrystal extends ItemBlock {
    public ItemBlockRedcrystal(Block block) {
        super(block);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (world.getBlock(x, y, z) != Blocks.snow_layer) {
            if (side == 0) {
                y--;
            }

            if (side == 1) {
                y++;
            }

            if (side == 2) {
                z--;
            }

            if (side == 3) {
                z++;
            }

            if (side == 4) {
                x--;
            }

            if (side == 5) {
                x++;
            }

            if (!world.isAirBlock(x, y, z)) {
                return false;
            }
        }

        if (!player.canPlayerEdit(x, y, z, side, stack)) {
            return false;
        } else {
            if (this.blockInstance.canPlaceBlockOnSide(world, x, y, z, side)) {
                this.blockInstance.onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, 0);
                if (this.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, 0)) {
                    world.playSoundEffect(
                        x + 0.5F,
                        y + 0.5F,
                        z + 0.5F,
                        this.blockInstance.stepSound.getPlaceSound(),
                        (this.blockInstance.stepSound.getVolume() + 1.0F) / 2.0F,
                        this.blockInstance.stepSound.getFrequency() * 0.8F
                    );
                    stack.stackSize--;
                }
            }

            return true;
        }
    }

    @Override
    public boolean placeBlockAt(
        ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata
    ) {
        if (!this.blockInstance.canPlaceBlockOnSide(world, x, y, z, side)) {
            return false;
        } else {
            BlockRedcrystal block = (BlockRedcrystal)this.blockInstance;
            if (block instanceof BlockRedcrystalMerc) {
                side = 1;
            }

            block.nextTEOrientation = side;
            if (block instanceof BlockRedcrystalAmp) {
                block.nextTENoConnections = true;
            }

            boolean placed = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
            if (placed) {
                if (block instanceof BlockRedcrystalAmp) {
                    int connectSide = TjUtil.getSideFromEntityFacing(player);
                    int opposite = TjUtil.getOppositeSide(connectSide);
                    if (side > 1 && opposite == side) {
                        connectSide = TjUtil.isEntityLookingDown(player) ? 0 : 1;
                    }

                    block.setConnection(world, x, y, z, true, connectSide);
                } else if (block instanceof BlockRedcrystalMerc && stack.stackTagCompound != null && stack.stackTagCompound.hasKey("mirrorX")) {
                    TileEntity te = world.getTileEntity(x, y, z);
                    if (te instanceof TileEntityRedcrystalMerc) {
                        int mx = stack.stackTagCompound.getInteger("mirrorX");
                        int my = stack.stackTagCompound.getInteger("mirrorY");
                        int mz = stack.stackTagCompound.getInteger("mirrorZ");
                        int mdim = stack.stackTagCompound.getInteger("mirrorDim");
                        ((TileEntityRedcrystalMerc)te).setMirrorLink(mdim, mx, my, mz);
                    }
                }
            }

            return placed;
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return stack.getItem() == Item.getItemFromBlock(ModBlocks.redcrystalMerc) ? EnumRarity.uncommon : EnumRarity.common;
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && stack.getItem() == Item.getItemFromBlock(ModBlocks.redcrystalMerc)) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (ThaumcraftExtension.tileIsMirror(te)) {
                ItemStack newStack = stack.copy();
                newStack.stackSize = 1;
                int dim = world.provider.dimensionId;
                newStack.setTagInfo("mirrorX", new NBTTagInt(x));
                newStack.setTagInfo("mirrorY", new NBTTagInt(y));
                newStack.setTagInfo("mirrorZ", new NBTTagInt(z));
                newStack.setTagInfo("mirrorDim", new NBTTagInt(dim));
                newStack.setTagInfo("mirrorDimName", new NBTTagString(DimensionManager.getProvider(dim).getDimensionName()));
                world.playSoundEffect(x, y, z, "thaumcraft:jar", 1.0F, 2.0F);
                if (!player.inventory.addItemStackToInventory(newStack) && !world.isRemote) {
                    world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, newStack));
                }

                if (!player.capabilities.isCreativeMode) {
                    stack.stackSize--;
                }

                player.inventoryContainer.detectAndSendChanges();
            }
        }

        return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("mirrorX")) {
            int x = stack.stackTagCompound.getInteger("mirrorX");
            int y = stack.stackTagCompound.getInteger("mirrorY");
            int z = stack.stackTagCompound.getInteger("mirrorZ");
            String dimName = stack.stackTagCompound.getString("mirrorDimName");
            list.add(StatCollector.translateToLocalFormatted("Automagy.tip.redcrystalMerc.link", x, y, z, dimName));
        }
    }
}
