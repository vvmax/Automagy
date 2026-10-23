package tuhljin.automagy.items;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.world.BlockEvent;

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
        if (world.isRemote) {
            return stack;
        }
        boolean flag = true;
        MovingObjectPosition pos = this.getMovingObjectPositionFromPlayer(world, player, flag);
        if (pos == null) {
            return stack;
        } else {
            FillBucketEvent event = new FillBucketEvent(player, stack, world, pos);
            if (MinecraftForge.EVENT_BUS.post(event)) {
                return stack;
            }
            if (event.getResult() == Event.Result.ALLOW) {
                return player.capabilities.isCreativeMode || event.result == null ? stack : event.result;
            }
            if (pos.typeOfHit == MovingObjectType.BLOCK) {
                int i = pos.blockX;
                int j = pos.blockY;
                int k = pos.blockZ;
                if (!world.canMineBlock(player, i, j, k)) {
                    return stack;
                }
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

                if (!world.canMineBlock(player, i, j, k) || !player.canPlayerEdit(i, j, k, pos.sideHit, stack)) {
                    return stack;
                }

                if (this.placeLiquid(world, i, j, k, player, ForgeDirection.getOrientation(pos.sideHit))
                    && !player.capabilities.isCreativeMode) {
                    return new ItemStack(Items.bucket);
                }
            }

            return stack;
        }
    }

    public boolean tryPlaceContainedLiquid(World world, int x, int y, int z) {
        return this.placeLiquid(world, x, y, z, null, ForgeDirection.UNKNOWN);
    }

    private boolean placeLiquid(World world, int x, int y, int z, EntityPlayer player, ForgeDirection side) {
        // Never edit a client world or nest a placement inside someone else's transaction.
        if (world.isRemote || world.captureBlockSnapshots || y < 0 || y >= world.getHeight()) {
            return false;
        }
        Block previous = world.getBlock(x, y, z);
        int metadata = world.getBlockMetadata(x, y, z);
        Material material = previous.getMaterial();
        if (!world.isAirBlock(x, y, z) && material.isSolid() || previous.hasTileEntity(metadata)) {
            return false;
        }
        boolean dropReplacedBlock = !world.isAirBlock(x, y, z) && !material.isLiquid();
        if (player != null && dropReplacedBlock
            && MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(x, y, z, world, previous, metadata, player))) {
            return false;
        }

        BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(world, x, y, z);
        int snapshotStart = world.capturedBlockSnapshots.size();
        boolean approved = false;
        try {
            boolean placed;
            world.captureBlockSnapshots = true;
            try {
                placed = world.setBlock(x, y, z, this.fluidBlock, 0, 3);
            } finally {
                world.captureBlockSnapshots = false;
                world.capturedBlockSnapshots.subList(snapshotStart, world.capturedBlockSnapshots.size()).clear();
            }
            if (!placed) {
                return false;
            }
            approved = player == null || !ForgeEventFactory.onPlayerBlockPlace(player, snapshot, side).isCanceled();
            if (!approved) {
                return false;
            }
            return true;
        } finally {
            if (!approved) {
                boolean restoring = world.restoringBlockSnapshots;
                world.restoringBlockSnapshots = true;
                try {
                    snapshot.restore(true, false);
                } finally {
                    world.restoringBlockSnapshots = restoring;
                }
            } else {
                // Drops and fluid updates must only happen after protection permits the placement.
                if (dropReplacedBlock) {
                    previous.dropBlockAsItem(world, x, y, z, metadata, 0);
                }
                this.fluidBlock.onBlockAdded(world, x, y, z);
                world.markAndNotifyBlock(x, y, z, null, previous, this.fluidBlock, 3);
            }
        }
    }
}
