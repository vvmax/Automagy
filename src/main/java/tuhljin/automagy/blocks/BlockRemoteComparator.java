package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import tuhljin.automagy.lib.IAutomagyLocationLink;
import tuhljin.automagy.lib.IRemoteComparatorOverride;
import tuhljin.automagy.lib.NeighborNotifier;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityRemoteComparator;

public class BlockRemoteComparator extends ModBlockSpecialRender {
    public BlockRemoteComparator(String name) {
        super(name, Material.circuits);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
        this.setStepSound(soundTypeStone);
        this.setHardness(2.5F);
        this.setHarvestLevel("pickaxe", 0);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileEntityRemoteComparator();
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return !TjUtil.isAcceptableSurfaceForBlock(world, x, y - 1, z, true, true, false) ? false : super.canPlaceBlockAt(world, x, y, z);
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        return !TjUtil.isAcceptableSurfaceForBlock(world, x, y - 1, z, true, true, false) ? false : super.canBlockStay(world, x, y, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!this.canBlockStay(world, x, y, z)) {
            this.dropBlockAsItem(world, x, y, z, 0, 0);
            world.setBlockToAir(x, y, z);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
        TileEntityRemoteComparator te = null;

        try {
            te = (TileEntityRemoteComparator)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null) {
            TjUtil.dropItemsIntoWorld(te, world, x, y, z, null);
        }

        NeighborNotifier.notifyBlocksOfExtendedNeighborChange(world, x, y, z, this);
        super.breakBlock(world, x, y, z, block, p_149749_6_);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int l = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 2.5) & 3;
        world.setBlockMetadataWithNotify(x, y, z, l, 2);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        TileEntityRemoteComparator te = null;

        try {
            te = (TileEntityRemoteComparator)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null && te.getFloatingDisplayItem() != null) {
            int color = -1;
            if (te.coordinatesAreInRange()) {
                if (te.isOverridden()) {
                    Block block = world.getBlock(x, y - 1, z);
                    if (block instanceof IRemoteComparatorOverride) {
                        color = ((IRemoteComparatorOverride)block).getRemoteComparatorParticleColor(world, x, y - 1, z, te);
                    }
                } else if (te.getBlockAtLinkedLocationIfValid() != null) {
                    color = 2;
                }

                if (color == -1) {
                    return;
                }
            } else {
                color = 5;
            }

            Thaumcraft.proxy.sparkle(x + 0.2F + rand.nextFloat() * 0.6F, y + 0.2F, z + 0.2F + rand.nextFloat() * 0.6F, 1.0F, color, -0.1F);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileEntityRemoteComparator te = (TileEntityRemoteComparator)world.getTileEntity(x, y, z);
        ItemStack stack = te.getStackInSlot(0);
        ItemStack playerStack = player.getCurrentEquippedItem();
        if (stack == null) {
            if (playerStack != null) {
                if (te.canInsertItem(0, playerStack, 1)) {
                    te.setInventorySlotContents(0, playerStack.splitStack(1));
                    if (playerStack.stackSize <= 0) {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                    }

                    te.markDirty();
                    world.notifyBlocksOfNeighborChange(x, y, z, this);
                    world.playSoundEffect(x, y, z, "random.pop", 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F);
                    return true;
                }

                if (playerStack.getItem() instanceof IAutomagyLocationLink) {
                    if (world.isRemote) {
                        TjUtil.sendChatToPlayer(player, "Automagy.chat.remoteComparator.itemUnlinked");
                    }

                    return true;
                }
            }
        } else if (playerStack == null || playerStack.getItem() instanceof IAutomagyLocationLink) {
            if (!player.inventory.addItemStackToInventory(stack)) {
                player.dropPlayerItemWithRandomChoice(stack, false);
            }

            te.setInventorySlotContents(0, null);
            te.markDirty();
            world.notifyBlocksOfNeighborChange(x, y, z, this);
            world.playSoundEffect(x, y, z, "random.pop", 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F);
            return true;
        }

        return false;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess blockaccess, int x, int y, int z, int side) {
        return side != 1 ? this.isProvidingWeakPower(blockaccess, x, y, z, side) : 0;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess blockaccess, int x, int y, int z, int side) {
        TileEntityRemoteComparator te = null;

        try {
            te = (TileEntityRemoteComparator)blockaccess.getTileEntity(x, y, z);
        } catch (Exception var8) {
        }

        return te == null ? 0 : te.getRedstoneSignalStrength();
    }
}
