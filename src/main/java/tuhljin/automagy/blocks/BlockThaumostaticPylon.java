package tuhljin.automagy.blocks;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import tuhljin.automagy.lib.References;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityThaumostaticController;
import tuhljin.automagy.tiles.TileEntityThaumostaticPylon;
import tuhljin.automagy.tiles.TileEntityThaumostaticPylonBase;

public class BlockThaumostaticPylon extends ModBlockSpecialRender {
    public static AspectList activationVisCost = new AspectList().add(Aspect.AIR, 50).add(Aspect.FIRE, 20).add(Aspect.ORDER, 20).add(Aspect.WATER, 10);

    public BlockThaumostaticPylon(String name) {
        super(name, Material.iron, ItemBlockThaumostaticPylon.class);
        this.setStepSound(Block.soundTypeMetal);
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setTextureName("Automagy:blockQuartzBuffer");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return (TileEntity)(metadata == 1 ? new TileEntityThaumostaticController() : new TileEntityThaumostaticPylon());
    }

    @Override
    public int getRenderType() {
        return References.renderBlockThaumostaticPylon;
    }

    @Override
    public int damageDropped(int metadata) {
        return metadata;
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (side > 1 && world.getBlockMetadata(x, y, z) == 1 && ThaumcraftExtension.playerHasWandEquipped(player)) {
            TileEntityThaumostaticController te = null;

            try {
                te = (TileEntityThaumostaticController)world.getTileEntity(x, y, z);
            } catch (Exception var12) {
            }

            if (te != null) {
                if (te.active) {
                    if (!world.isRemote) {
                        Aspect a = te.switchSuction(ForgeDirection.getOrientation(side));
                        if (a != null) {
                            TjUtil.sendFormattedChatToPlayer(player, "Automagy.chat.thaumostaticSpire.setSuction", a.getName());
                            world.playSoundAtEntity(player, "thaumcraft:tool", 0.5F, 0.9F + world.rand.nextFloat() * 0.2F);
                        }
                    }

                    return true;
                }

                if (te.checkValidity()) {
                    if (!world.isRemote) {
                        if (player.capabilities.isCreativeMode
                            || ThaumcraftApiHelper.consumeVisFromWandCrafting(player.getCurrentEquippedItem(), player, activationVisCost, true)) {
                            te.activate();
                        }
                    } else if (!player.capabilities.isCreativeMode
                        && !ThaumcraftApiHelper.consumeVisFromWandCrafting(player.getCurrentEquippedItem(), player, activationVisCost, false)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        TileEntityThaumostaticPylonBase te = null;

        try {
            te = (TileEntityThaumostaticPylonBase)world.getTileEntity(x, y, z);
        } catch (Exception var8) {
        }

        te.assureValidity();
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block p_149749_5_, int p_149749_6_) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityThaumostaticController && ((TileEntityThaumostaticController)te).active) {
            ((TileEntityThaumostaticController)te).deactivate();
        }

        super.breakBlock(world, x, y, z, p_149749_5_, p_149749_6_);
    }
}
