package tuhljin.automagy.blocks;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.common.blocks.CustomStepSound;
import tuhljin.automagy.items.ItemCrystalEye;
import tuhljin.automagy.lib.IAutomagyLocationLink;
import tuhljin.automagy.lib.References;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.tiles.TileEntityScribePointer;

public class BlockScribePointer extends ModBlockSpecialRender {
    public BlockScribePointer(String name) {
        super(name, ModBlocks.materialNiceStone);
        this.setHardness(6.0F);
        this.setStepSound(new CustomStepSound("crystal", 1.0F, 1.0F));
        this.setBlockBounds(0.715F, 0.003F, 0.02F, 0.98F, 0.31F, 0.285F);
        this.setTextureName("quartz_block_top");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int p_149915_2_) {
        return new TileEntityScribePointer(world);
    }

    @Override
    public int getRenderType() {
        return References.renderBlockUnseenScribePointerRI;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int md = world.getBlockMetadata(x, y - 1, z);
        world.setBlockMetadataWithNotify(x, y, z, md, 2);
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return this.canBlockStay(world, x, y, z);
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        return world.getBlock(x, y - 1, z) == ModBlocks.unseenScribe;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!this.canBlockStay(world, x, y, z)) {
            this.dropBlockAsItem(world, x, y, z, 0, 0);
            world.setBlockToAir(x, y, z);
        } else {
            int md = world.getBlockMetadata(x, y - 1, z);
            if (world.getBlockMetadata(x, y, z) != md) {
                world.setBlockMetadataWithNotify(x, y, z, md, 3);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileEntityScribePointer te = (TileEntityScribePointer)world.getTileEntity(x, y, z);
        ItemStack playerStack = player.getCurrentEquippedItem();
        if (playerStack == null) {
            ItemStack stack = te.removeEye();
            if (stack != null) {
                if (!player.inventory.addItemStackToInventory(stack)) {
                    player.dropPlayerItemWithRandomChoice(stack, false);
                }

                world.playSoundEffect(x, y, z, "random.pop", 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F);
                return true;
            }
        } else {
            if (te.itemIsValidEye(playerStack)) {
                if (te.insertEye(playerStack)) {
                    if (playerStack.stackSize <= 0) {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                    }

                    world.playSoundEffect(x, y, z, "random.pop", 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F);
                }

                return true;
            }

            if (playerStack.getItem() instanceof IAutomagyLocationLink) {
                if (world.isRemote) {
                    WorldSpecificCoordinates coord = ((ItemCrystalEye)playerStack.getItem()).getLinkLocation(playerStack);
                    if (coord != null && coord.x == x && coord.y == y && coord.z == z) {
                        TjUtil.sendChatToPlayer(player, "Automagy.chat.scribePointer.itemLinkedSelf");
                    } else {
                        TjUtil.sendFormattedChatToPlayer(player, "Automagy.chat.scribePointer.itemUnlinked", TileEntityScribePointer.RANGE);
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockaccess, int x, int y, int z) {
        int md = blockaccess.getBlockMetadata(x, y, z);
        switch (md) {
            case 0:
                this.setBlockBounds(0.715F, 0.003F, 0.02F, 0.98F, 0.31F, 0.285F);
                break;
            case 1:
                this.setBlockBounds(0.715F, 0.003F, 0.715F, 0.98F, 0.31F, 0.98F);
                break;
            case 2:
                this.setBlockBounds(0.02F, 0.003F, 0.715F, 0.285F, 0.31F, 0.98F);
                break;
            case 3:
                this.setBlockBounds(0.02F, 0.003F, 0.02F, 0.285F, 0.31F, 0.285F);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block p_149749_5_, int p_149749_6_) {
        TileEntityScribePointer te = null;

        try {
            te = (TileEntityScribePointer)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null) {
            TjUtil.dropItemsIntoWorld(te.eyesContainingInventory, world, x, y, z, null);
        }

        super.breakBlock(world, x, y, z, p_149749_5_, p_149749_6_);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_) {
        return null;
    }
}
