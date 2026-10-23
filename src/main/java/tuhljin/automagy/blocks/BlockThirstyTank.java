package tuhljin.automagy.blocks;

import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import tuhljin.automagy.blocks.fluid.BlockFillableByBucket;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.RedstoneCalc;
import tuhljin.automagy.lib.References;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityThirstyTank;

public class BlockThirstyTank extends BlockFillableByBucket {
    private FluidStack lastFluidStack = null;
    private int[] lastGlyphs = null;

    public BlockThirstyTank(String name) {
        super(name, ModBlocks.materialNiceStone, ItemBlockThirstyTank.class, true);
        this.setTextureName("thaumcraft:pedestal_top");
        this.setBlockBounds(0.031F, 0.0F, 0.031F, 0.969F, 0.939F, 0.969F);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityThirstyTank();
    }

    @Override
    public int getRenderType() {
        return References.renderBlockThirstyTank;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        return ((TileEntityThirstyTank)world.getTileEntity(x, y, z)).getComparatorStrength();
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
        TileEntityThirstyTank te = null;

        try {
            te = (TileEntityThirstyTank)world.getTileEntity(x, y, z);
            this.lastFluidStack = te.tank.getFluid();
            this.lastGlyphs = null;

            for (int i = 0; i < te.glyphs.length; i++) {
                if (te.glyphs[i] != 0) {
                    this.lastGlyphs = te.glyphs;
                    break;
                }
            }
        } catch (Exception var9) {
            this.lastFluidStack = null;
            this.lastGlyphs = null;
        }

        super.breakBlock(world, x, y, z, block, p_149749_6_);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<>();
        Item item = this.getItemDropped(metadata, world.rand, fortune);
        ItemStack stack = new ItemStack(item, 1, this.damageDropped(metadata));
        TileEntityThirstyTank te = null;

        try {
            te = (TileEntityThirstyTank)world.getTileEntity(x, y, z);
        } catch (Exception var13) {
        }

        FluidStack fluidStack = null;
        if (te != null) {
            fluidStack = te.tank.getFluid();
        } else {
            fluidStack = this.lastFluidStack;
        }

        if (fluidStack != null) {
            if (stack.stackTagCompound == null) {
                stack.stackTagCompound = new NBTTagCompound();
            }

            fluidStack.writeToNBT(stack.stackTagCompound);
        }

        this.lastFluidStack = null;
        int[] glyphs = null;
        if (te != null) {
            glyphs = te.glyphs;
        } else {
            glyphs = this.lastGlyphs;
        }

        if (glyphs != null) {
            if (stack.stackTagCompound == null) {
                stack.stackTagCompound = new NBTTagCompound();
            }

            stack.stackTagCompound.setIntArray("Glyphs", glyphs);
        }

        this.lastGlyphs = null;
        drops.add(stack);
        return drops;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9) {
        if (super.onBlockActivated(world, x, y, z, player, side, par7, par8, par9)) {
            return true;
        } else {
            ItemStack heldStack = player.inventory.getCurrentItem();
            if (heldStack != null && heldStack.getItem() == ModItems.tankGlyph) {
                int glyph = heldStack.getMetadata();
                if (glyph > 0) {
                    if (!world.isRemote) {
                        TileEntityThirstyTank te = (TileEntityThirstyTank)world.getTileEntity(x, y, z);
                        if (te.installGlyph(glyph, side)) {
                            if (!player.capabilities.isCreativeMode) {
                                TjUtil.consumePlayerItem(player, player.inventory.currentItem);
                            }

                            world.playSoundAtEntity(player, "thaumcraft:upgrade", 0.5F, 1.0F);
                        }
                    }

                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        TileEntityThirstyTank te = (TileEntityThirstyTank)world.getTileEntity(x, y, z);
        RedstoneCalc.PowerResult result = RedstoneCalc.getRedstonePowerAt(world, x, y, z);
        if (result != null) {
            te.receivingSignal = true;
            te.markDirty(false);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!world.isRemote) {
            TileEntityThirstyTank te = null;

            try {
                te = (TileEntityThirstyTank)world.getTileEntity(x, y, z);
            } catch (Exception var8) {
            }

            if (te != null) {
                RedstoneCalc.PowerResult result = RedstoneCalc.getRedstonePowerAt(world, x, y, z);
                te.updateRedstoneInput(result != null);
            }
        }
    }
}
