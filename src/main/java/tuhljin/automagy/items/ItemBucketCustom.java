package tuhljin.automagy.items;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class ItemBucketCustom extends ModItem {
    public Block fluidBlock;

    public ItemBucketCustom(String name, Block fluidBlock) {
        super(name);
        this.fluidBlock = fluidBlock;
        this.setMaxStackSize(1);
        this.setContainerItem(Items.bucket);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        boolean flag = true;
        MovingObjectPosition pos = this.getMovingObjectPositionFromPlayer(world, player, flag);
        if (pos == null) {
            return stack;
        } else {
            if (pos.typeOfHit == MovingObjectType.BLOCK) {
                int i = pos.blockX;
                int j = pos.blockY;
                int k = pos.blockZ;
                if (pos.sideHit == 0) {
                    j--;
                }

                if (pos.sideHit == 1) {
                    j++;
                }

                if (pos.sideHit == 2) {
                    k--;
                }

                if (pos.sideHit == 3) {
                    k++;
                }

                if (pos.sideHit == 4) {
                    i--;
                }

                if (pos.sideHit == 5) {
                    i++;
                }

                if (!player.canPlayerEdit(i, j, k, pos.sideHit, stack)) {
                    return stack;
                }

                if (this.tryPlaceContainedLiquid(world, i, j, k) && !player.capabilities.isCreativeMode) {
                    return new ItemStack(Items.bucket);
                }
            }

            return stack;
        }
    }

    public boolean tryPlaceContainedLiquid(World world, int x, int y, int z) {
        Material material = world.getBlock(x, y, z).getMaterial();
        boolean flag = !material.isSolid();
        if (!world.isAirBlock(x, y, z) && !flag) {
            return false;
        } else {
            if (!world.isRemote && flag && !material.isLiquid()) {
                world.breakBlock(x, y, z, true);
            }

            world.setBlock(x, y, z, this.fluidBlock, 0, 3);
            return true;
        }
    }
}
