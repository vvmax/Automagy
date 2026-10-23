package tuhljin.automagy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.common.items.wands.ItemWandCasting;
import tuhljin.automagy.lib.NeighborNotifier;
import tuhljin.automagy.lib.RedstoneCalc;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityHourglass;

public class BlockHourglass extends ModBlockSpecialRender {
    public static final int TIMER_MINIMUM = 5;
    public static final int TIMER_MAXIMUM = 60;
    public static final int TIMER_INCREMENTS = 5;

    protected BlockHourglass(String name, Material material) {
        super(name, material);
        this.setHardness(1.0F);
        this.setBlockBounds(0.35F, 0.0F, 0.35F, 0.65F, 0.4F, 0.65F);
    }

    public BlockHourglass(String name) {
        this(name, Material.wood);
        this.setStepSound(Block.soundTypeWood);
        this.setTextureName("glass");
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileEntityHourglass();
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return this.canBlockStay(world, x, y, z) && super.canPlaceBlockAt(world, x, y, z);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int l = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 2.5) & 3;
        world.setBlockMetadataWithNotify(x, y, z, l, 2);
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        if (!World.doesBlockHaveSolidTopSurface(world, x, y - 1, z)) {
            Block block = world.getBlock(x, y - 1, z);
            if (block != Blocks.glowstone && block != Blocks.glass && !ThaumcraftExtension.blockIsPedestal(block, world.getBlockMetadata(x, y - 1, z), false)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        TileEntityHourglass te = (TileEntityHourglass)world.getTileEntity(x, y, z);
        RedstoneCalc.PowerResult result = RedstoneCalc.getRedstonePowerAt(world, x, y, z, getRedstoneInputDirections(world, x, y, z));
        if (result != null) {
            te.receivingSignal = true;
            te.markDirty(false);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!this.canBlockStay(world, x, y, z)) {
            this.dropBlockAsItem(world, x, y, z, 0, 0);
            world.setBlockToAir(x, y, z);
        } else if (!world.isRemote) {
            TileEntityHourglass te = null;

            try {
                te = (TileEntityHourglass)world.getTileEntity(x, y, z);
            } catch (Exception var8) {
            }

            if (te != null) {
                RedstoneCalc.PowerResult result = RedstoneCalc.getRedstonePowerAt(world, x, y, z, getRedstoneInputDirections(world, x, y, z));
                te.updateRedstoneInput(result != null);
            }
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
        NeighborNotifier.notifyBlocksOfExtendedNeighborChange(world, x, y, z, this);
        super.breakBlock(world, x, y, z, block, p_149749_6_);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntityHourglass te = null;

            try {
                te = (TileEntityHourglass)world.getTileEntity(x, y, z);
            } catch (Exception var14) {
            }

            if (te == null) {
                return true;
            }

            ItemStack stack = player.getCurrentEquippedItem();
            if (stack != null) {
                Item equippedItem = stack.getItem();
                if (equippedItem == Items.stick || equippedItem instanceof ItemWandCasting) {
                    int target = te.getTargetTimeSeconds();
                    target += 5;
                    if (target > 60) {
                        target = 5;
                    }

                    te.setTargetTimeSeconds(target);
                    TjUtil.sendFormattedChatToPlayer(player, "Automagy.chat.hourglass.setTimer", target);
                    world.playSoundAtEntity(player, "thaumcraft:key", 1.0F, 1.0F);
                    return true;
                }
            }

            if (te.startFlipOnServer(true)) {
                world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "thaumcraft:cameraclack", 1.0F, 1.0F);
            }
        }

        return true;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess blockaccess, int x, int y, int z, int side) {
        int orientation = blockaccess.getBlockMetadata(x, y, z);
        if (side != 2 && side != 3) {
            if ((side == 4 || side == 5) && (orientation == 1 || orientation == 3)) {
                return 0;
            }
        } else if (orientation == 0 || orientation == 2) {
            return 0;
        }

        TileEntityHourglass te = null;

        try {
            te = (TileEntityHourglass)blockaccess.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        return te == null ? 0 : te.getRedstoneSignalStrength();
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        TileEntityHourglass te = null;

        try {
            te = (TileEntityHourglass)world.getTileEntity(x, y, z);
        } catch (Exception var8) {
        }

        return te == null ? 0 : te.getComparatorSignalStrength();
    }

    public static ForgeDirection[] getRedstoneInputDirections(IBlockAccess blockaccess, int x, int y, int z) {
        int orientation = blockaccess.getBlockMetadata(x, y, z);
        return orientation != 0 && orientation != 2
            ? new ForgeDirection[]{ForgeDirection.EAST, ForgeDirection.WEST, ForgeDirection.DOWN, ForgeDirection.UP}
            : new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.DOWN, ForgeDirection.UP};
    }
}
