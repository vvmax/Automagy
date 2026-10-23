package tuhljin.automagy.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import thaumcraft.api.ItemApi;
import thaumcraft.common.blocks.BlockJar;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.RedstoneCalc;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityJarXP;

public class BlockJarXP extends BlockJar {
    public static final String TAG_XP = "xp";

    public BlockJarXP(String name) {
        this.setUnlocalizedName(name);
        this.setTextureName("Automagy:" + name);
        if (ModBlocks.tab != null) {
            this.setCreativeTab(ModBlocks.tab);
        } else {
            this.setCreativeTab(null);
        }

        GameRegistry.registerBlock(this, ItemBlockJarXP.class, name);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs p_149666_2_, List list) {
        ItemStack stack = new ItemStack(item);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("xp", TileEntityJarXP.MAX_XP);
        stack.setTagCompound(nbt);
        list.add(stack);
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityJarXP();
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<>();
        TileEntity te = world.getTileEntity(x, y, z);
        if (te != null && te instanceof TileEntityJarXP) {
            int xp = ((TileEntityJarXP)te).getXP();
            ItemStack drop;
            if (xp > 0) {
                drop = new ItemStack(this);
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setInteger("xp", xp);
                drop.setTagCompound(nbt);
            } else {
                drop = ItemApi.getBlock("blockJar", 0);
            }

            drops.add(drop);
        }

        return drops;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9) {
        ItemStack heldStack = player.inventory.getCurrentItem();
        if (heldStack != null) {
            ItemStack phialXP = new ItemStack(ModItems.phialXP);
            if (TjUtil.areItemsEqualIgnoringSize(heldStack, phialXP)) {
                TileEntityJarXP jar = (TileEntityJarXP)world.getTileEntity(x, y, z);
                if (jar.addExactXP(ModItems.phialXP.getXPValue())) {
                    if (world.isRemote) {
                        world.playSound(
                            x + 0.5F, y + 0.5F, z + 0.5F, "game.neutral.swim", 0.25F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F, false
                        );
                    }

                    TjUtil.consumePlayerItem(player, player.inventory.currentItem);
                }

                return true;
            }

            if (TjUtil.areItemsEqualIgnoringSize(heldStack, ModItems.phialXP.getContainerItem(phialXP))) {
                TileEntityJarXP jar = (TileEntityJarXP)world.getTileEntity(x, y, z);
                if (jar.removeExactXP(ModItems.phialXP.getXPValue())) {
                    if (world.isRemote) {
                        world.playSound(
                            x + 0.5F, y + 0.5F, z + 0.5F, "game.neutral.swim", 0.25F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F, false
                        );
                    }

                    TjUtil.consumePlayerItem(player, player.inventory.currentItem, phialXP);
                }

                return true;
            }

            ItemStack xpBottle = new ItemStack(Items.experience_bottle);
            if (TjUtil.areItemsEqualIgnoringSize(heldStack, new ItemStack(Items.glass_bottle))) {
                TileEntityJarXP jar = (TileEntityJarXP)world.getTileEntity(x, y, z);
                if (jar.removeExactXP(BlockAmnesiac.XP_PER_BOTTLE_DEFAULT)) {
                    if (world.isRemote) {
                        world.playSound(
                            x + 0.5F, y + 0.5F, z + 0.5F, "game.neutral.swim", 0.25F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F, false
                        );
                    }

                    TjUtil.consumePlayerItem(player, player.inventory.currentItem, xpBottle);
                }

                return true;
            }
        }

        return true;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        TileEntity te = world.getTileEntity(x, y, z);
        return te != null && te instanceof TileEntityJarXP
            ? RedstoneCalc.getRedstoneSignalStrengthFromValues(((TileEntityJarXP)te).getXP(), TileEntityJarXP.MAX_XP)
            : 0;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        ArrayList<ItemStack> drops = this.getDrops(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
        return drops.size() == 1 ? drops.get(0) : super.getPickBlock(target, world, x, y, z);
    }
}
