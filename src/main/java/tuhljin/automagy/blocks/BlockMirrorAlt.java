package tuhljin.automagy.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import thaumcraft.common.blocks.BlockMirror;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityMirrorInput;
import tuhljin.automagy.tiles.TileEntityMirrorWithEye;

public class BlockMirrorAlt extends BlockMirror {
    public IIcon iconSocketed;

    public BlockMirrorAlt(String name) {
        this.setUnlocalizedName(name);
        this.setTextureName("Automagy:mirrorInputFrame");
        if (ModBlocks.tab != null) {
            this.setCreativeTab(ModBlocks.tab);
        } else {
            this.setCreativeTab(null);
        }

        GameRegistry.registerBlock(this, ItemBlockMirrorAlt.class, name);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister ir) {
        this.icon = ir.registerIcon(this.getTextureName());
        this.iconSocketed = ir.registerIcon(this.getTextureName() + "-socketed");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
    }

    @Override
    public int damageDropped(int meta) {
        return meta < 6 ? 0 : 6;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityMirrorInput();
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int md) {
        return new TileEntityMirrorInput();
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        if (world.getBlock(x, y, z) != this) {
            return new ArrayList<>();
        } else {
            ArrayList<ItemStack> drops = new ArrayList<>();
            drops.add(new ItemStack(this, 1, metadata < 6 ? 0 : 6));
            return drops;
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block p_149749_5_, int p_149749_6_) {
        TileEntityMirrorWithEye te = null;

        try {
            te = (TileEntityMirrorWithEye)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null) {
            TjUtil.dropItemsIntoWorld(te.eyesContainingInventory, world, x, y, z, null);
            if (te instanceof TileEntityMirrorInput) {
                ArrayList<ItemStack> stacks = ((TileEntityMirrorInput)te).outputSpace.stacks;
                if (stacks.size() > 0) {
                    TjUtil.dropItemsIntoWorld(stacks, world, x, y, z);
                }
            }
        }

        super.breakBlock(world, x, y, z, p_149749_5_, p_149749_6_);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (!world.isRemote && entity instanceof EntityItem && !entity.isDead && ((EntityItem)entity).timeUntilPortal == 0) {
            TileEntityMirrorInput te = (TileEntityMirrorInput)world.getTileEntity(x, y, z);
            if (te != null) {
                te.transport((EntityItem)entity);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            ItemStack playerStack = player.getCurrentEquippedItem();
            if (playerStack != null) {
                TileEntityMirrorInput te = (TileEntityMirrorInput)world.getTileEntity(x, y, z);
                if (te.itemIsValidEye(playerStack)) {
                    if (te.insertEye(playerStack)) {
                        if (playerStack.stackSize <= 0) {
                            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                        }

                        world.playSoundEffect(x, y, z, "thaumcraft:key", 1.0F, 0.8F);
                    } else if (te.getEyeCount() == 0) {
                        world.playSoundEffect(x, y, z, "random.break", 0.9F, 0.9F);
                    }

                    return true;
                }
            }
        }

        return true;
    }
}
