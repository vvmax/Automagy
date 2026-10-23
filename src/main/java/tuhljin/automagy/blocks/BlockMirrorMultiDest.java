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
import net.minecraft.world.World;
import thaumcraft.common.blocks.BlockMirror;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.network.MessageParticles;
import tuhljin.automagy.tiles.TileEntityInventarium;
import tuhljin.automagy.tiles.TileEntityMirrorMultiDest;

public class BlockMirrorMultiDest extends BlockMirror {
    public BlockMirrorMultiDest(String name) {
        this.setUnlocalizedName(name);
        this.setTextureName("Automagy:mirrorMultiDestFrame");
        if (ModBlocks.tab != null) {
            this.setCreativeTab(ModBlocks.tab);
        } else {
            this.setCreativeTab(null);
        }

        GameRegistry.registerBlock(this, ItemBlockMirrorMultiDest.class, name);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister ir) {
        this.icon = ir.registerIcon(this.getTextureName());
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
        return new TileEntityMirrorMultiDest();
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int md) {
        return new TileEntityMirrorMultiDest();
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
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (!world.isRemote && entity instanceof EntityItem && !entity.isDead && ((EntityItem)entity).timeUntilPortal == 0) {
            TileEntityMirrorMultiDest te = (TileEntityMirrorMultiDest)world.getTileEntity(x, y, z);
            if (te != null) {
                te.transport((EntityItem)entity);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (ThaumcraftExtension.playerHasWandEquipped(player)) {
            if (!world.isRemote) {
                TileEntityMirrorMultiDest te = (TileEntityMirrorMultiDest)world.getTileEntity(x, y, z);
                if (te.linkY != -1) {
                    TjUtil.sendFormattedChatToPlayer(player, "Automagy.chat.mirrorMultiDest.alreadyLinked", te.linkX, te.linkY, te.linkZ);
                    return true;
                }

                TileEntityInventarium teI = TileEntityInventarium.getCoreNear(world, x, y, z);
                if (teI != null) {
                    if (teI.addMultiDestMirror(x, y, z)) {
                        te.setLink(teI);
                        world.playSoundAtEntity(player, "thaumcraft:wand", 1.0F, 1.0F);
                        MessageParticles.sendToClients(world, x, y, z, teI.xCoord, teI.yCoord, teI.zCoord, (short)6);
                    } else {
                        TjUtil.sendChatToPlayer(player, "Automagy.chat.mirrorMultiDest.noConnectionsLeft");
                    }
                } else {
                    TjUtil.sendChatToPlayer(player, "Automagy.chat.mirrorMultiDest.outOfRange");
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block p_149749_5_, int p_149749_6_) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityMirrorMultiDest) {
            ArrayList<ItemStack> pending = ((TileEntityMirrorMultiDest)te).pendingOutput.stacks;
            if (!world.isRemote && !pending.isEmpty()) {
                TjUtil.dropItemsIntoWorld(pending, world, x, y, z);
                pending.clear();
            }
            TileEntityInventarium teInv = ((TileEntityMirrorMultiDest)te).getLinkedInventarium();
            if (teInv != null) {
                teInv.removeMultiDestMirror();
            }
        }

        super.breakBlock(world, x, y, z, p_149749_5_, p_149749_6_);
    }
}
