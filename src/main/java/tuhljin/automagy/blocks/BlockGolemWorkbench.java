package tuhljin.automagy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.References;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityGolemWorkbench;

public class BlockGolemWorkbench extends ModBlockSpecialRender {
    public BlockGolemWorkbench(String name) {
        super(name, Material.wood);
        this.setHardness(2.5F);
        this.setStepSound(soundTypeWood);
        this.setTextureName("thaumcraft:woodplain");
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int p_149915_2_) {
        return new TileEntityGolemWorkbench();
    }

    @Override
    public int getRenderType() {
        return References.renderBlockGolemWorkbench;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (side > 1 && side < 6) {
            ItemStack heldStack = player.inventory.getCurrentItem();
            if (heldStack != null && heldStack.getItem() == ModItems.golemWorkbenchUpgrade) {
                if (!world.isRemote) {
                    int upgrade = heldStack.getMetadata();
                    TileEntityGolemWorkbench te = (TileEntityGolemWorkbench)world.getTileEntity(x, y, z);
                    if (te.installUpgrade(upgrade, side)) {
                        if (!player.capabilities.isCreativeMode) {
                            TjUtil.consumePlayerItem(player, player.inventory.currentItem);
                        }

                        world.playSoundAtEntity(player, "thaumcraft:upgrade", 0.5F, 1.0F);
                    }
                }

                return true;
            }
        }

        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te != null && te instanceof TileEntityGolemWorkbench) {
                player.openGui(Automagy.instance, 11, world, x, y, z);
            }
        }

        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
        TileEntityGolemWorkbench te = null;

        try {
            te = (TileEntityGolemWorkbench)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null) {
            TjUtil.dropItemsIntoWorld(te, world, x, y, z, this);
            ItemStack[] upgradeStacks = te.getUpgradeStacks();
            if (upgradeStacks != null) {
                TjUtil.dropItemsIntoWorld(upgradeStacks, world, x, y, z);
            }
        }

        super.breakBlock(world, x, y, z, block, p_149749_6_);
    }
}
