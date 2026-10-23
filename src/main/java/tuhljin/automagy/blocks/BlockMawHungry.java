package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockChestHungry;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityMawHungry;

public class BlockMawHungry extends ModBlockSpecialRender {
    public BlockMawHungry(String name) {
        super(name, ModBlocks.materialNiceStone);
        this.setBlockBounds(0.315F, -0.011F, 0.315F, 0.685F, 0.05F, 0.685F);
        this.setHardness(1.5F);
        this.setResistance(2000.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileEntityMawHungry();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        if (rand.nextInt(4) <= 0) {
            int orientation = world.getBlockMetadata(x, y, z);
            double angle = rand.nextDouble() * Math.PI * 2.0;
            double xa = Math.cos(angle);
            double za = Math.sin(angle);
            float dist = rand.nextFloat() * 0.5F;
            double x1;
            double y1;
            double z1;
            double x2;
            double y2;
            double z2;
            switch (orientation) {
                case 0:
                    x1 = x + 0.5 + xa * dist;
                    z1 = z + 0.5 + za * dist;
                    x2 = x + 0.5 + xa * 0.02;
                    z2 = z + 0.5 + za * 0.02;
                    y1 = y + 0.7;
                    y2 = y + 1.1;
                    break;
                case 1:
                default:
                    x1 = x + 0.5 + xa * dist;
                    z1 = z + 0.5 + za * dist;
                    x2 = x + 0.5 + xa * 0.02;
                    z2 = z + 0.5 + za * 0.02;
                    y1 = y + 0.3;
                    y2 = y - 0.1;
                    break;
                case 2:
                    x1 = x + 0.5 + xa * dist;
                    y1 = y + 0.5 + za * dist;
                    x2 = x + 0.5 + xa * 0.02;
                    y2 = y + 0.5 + za * 0.02;
                    z1 = z + 0.7;
                    z2 = z + 1.1;
                    break;
                case 3:
                    x1 = x + 0.5 + xa * dist;
                    y1 = y + 0.5 + za * dist;
                    x2 = x + 0.5 + xa * 0.02;
                    y2 = y + 0.5 + za * 0.02;
                    z1 = z + 0.3;
                    z2 = z - 0.1;
                    break;
                case 4:
                    y1 = y + 0.5 + xa * dist;
                    z1 = z + 0.5 + za * dist;
                    y2 = y + 0.5 + xa * 0.02;
                    z2 = z + 0.5 + za * 0.02;
                    x1 = x + 0.7;
                    x2 = x + 1.1;
                    break;
                case 5:
                    y1 = y + 0.5 + xa * dist;
                    z1 = z + 0.5 + za * dist;
                    y2 = y + 0.5 + xa * 0.02;
                    z2 = z + 0.5 + za * 0.02;
                    x1 = x + 0.3;
                    x2 = x - 0.1;
            }

            int r = rand.nextInt(120);
            if (r < 3) {
                float gravity = rand.nextFloat() * 0.01F;
                if (orientation != 0) {
                    gravity *= -1.0F;
                }

                Thaumcraft.proxy.wispFX3(world, x1, y1, z1, x2, y2, z2, 0.05F, 2, r == 0, gravity);
            } else {
                Thaumcraft.proxy.wispFX3(world, x1, y1, z1, x2, y2, z2, 0.02F, 5, false, 0.0F);
            }
        }
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockaccess, int x, int y, int z) {
        int orientation = blockaccess.getBlockMetadata(x, y, z);
        switch (orientation) {
            case 0:
                this.setBlockBounds(0.315F, 0.95F, 0.315F, 0.685F, 1.011F, 0.685F);
                break;
            case 1:
                this.setBlockBounds(0.315F, -0.011F, 0.315F, 0.685F, 0.05F, 0.685F);
                break;
            case 2:
                this.setBlockBounds(0.315F, 0.315F, 0.95F, 0.685F, 0.685F, 1.011F);
                break;
            case 3:
                this.setBlockBounds(0.315F, 0.315F, 0.05F, 0.685F, 0.685F, -0.011F);
                break;
            case 4:
                this.setBlockBounds(0.95F, 0.315F, 0.315F, 1.011F, 0.685F, 0.685F);
                break;
            case 5:
                this.setBlockBounds(-0.011F, 0.315F, 0.315F, 0.05F, 0.685F, 0.685F);
        }
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float p_149660_6_, float p_149660_7_, float p_149660_8_, int metadata) {
        return side;
    }

    public boolean locationIsValid(World world, int x, int y, int z, int side, boolean checkSidedSlots) {
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        int x2 = x - dir.offsetX;
        int y2 = y - dir.offsetY;
        int z2 = z - dir.offsetZ;
        if (!world.isAirBlock(x2, y2, z2)) {
            TileEntity te = world.getTileEntity(x2, y2, z2);
            if (te instanceof IInventory) {
                if (checkSidedSlots && te instanceof ISidedInventory) {
                    int[] slots = ((ISidedInventory)te).getSlotsForFace(side);
                    if (slots == null || slots.length == 0) {
                        return false;
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
        return this.locationIsValid(world, x, y, z, side, true);
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        int md = world.getBlockMetadata(x, y, z);
        return this.locationIsValid(world, x, y, z, md, false);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!this.canBlockStay(world, x, y, z)) {
            this.dropBlockAsItem(world, x, y, z, 0, 0);
            world.setBlockToAir(x, y, z);
        }
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (!world.isRemote && entity instanceof EntityItem && !entity.isDead) {
            EntityItem item = (EntityItem)entity;
            TileEntityMawHungry te = null;

            try {
                te = (TileEntityMawHungry)world.getTileEntity(x, y, z);
            } catch (Exception var9) {
            }

            if (te != null) {
                te.moveItem(item);
            }
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (world.getBlockMetadata(x, y, z) == 1 && world.getBlock(x, y - 1, z) instanceof BlockChestHungry) {
            world.scheduleBlockUpdate(x, y, z, this, 1);
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        EntityItem item = TjUtil.dropItemIntoWorld(new ItemStack(this, 1, 0), world, x, y, z);
        if (item != null) {
            item.motionX = 0.0;
            item.motionY = 0.0;
            item.motionZ = 0.0;
            item.delayBeforeCanPickup = 10;
        }

        world.setBlockToAir(x, y, z);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_) {
        return null;
    }
}
