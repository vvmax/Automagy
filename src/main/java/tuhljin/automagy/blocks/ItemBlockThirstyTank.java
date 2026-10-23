package tuhljin.automagy.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import tuhljin.automagy.items.ItemGlyph;
import tuhljin.automagy.tiles.ModTileEntity;
import tuhljin.automagy.tiles.TileEntityThirstyTank;

public class ItemBlockThirstyTank extends ItemBlock {
    public static final String GLYPH_KEY = "Glyphs";

    public ItemBlockThirstyTank(Block block) {
        super(block);
    }

    @Override
    public boolean placeBlockAt(
        ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata
    ) {
        boolean placed = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
        if (placed && stack.hasTagCompound()) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(stack.stackTagCompound);
            if (fluidStack != null) {
                TileEntityThirstyTank te = (TileEntityThirstyTank)world.getTileEntity(x, y, z);
                te.setFluid(fluidStack);
            }

            if (stack.stackTagCompound.hasKey("Glyphs")) {
                int[] glyphs = ModTileEntity.getIntArrayFromNbtOrDefault(stack.stackTagCompound, "Glyphs", 0, 6);
                TileEntityThirstyTank te = (TileEntityThirstyTank)world.getTileEntity(x, y, z);
                te.setGlyphs(glyphs);
            }
        }

        return placed;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean p_77624_4_) {
        if (stack.hasTagCompound()) {
            if (stack.stackTagCompound.hasKey("Glyphs")) {
                int[] glyphs = ModTileEntity.getIntArrayFromNbtOrDefault(stack.stackTagCompound, "Glyphs", 0, 6);
                Map<Integer, Integer> glyphCount = new HashMap<>();

                for (int id : glyphs) {
                    if (id > 0) {
                        Integer c = glyphCount.get(id);
                        glyphCount.put(id, (c == null ? 0 : c) + 1);
                    }
                }

                FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(stack.stackTagCompound);
                if (fluidStack != null) {
                    Integer capacity = glyphCount.get(8);
                    capacity = 16 + (capacity == null ? 0 : capacity) * TileEntityThirstyTank.CAPACITY_IN_BUCKETS_PER_UPGRADE;
                    list.add(
                        StatCollector.translateToLocalFormatted("Automagy.tip.tankFluidData", fluidStack.amount, 1000 * capacity, fluidStack.getLocalizedName())
                    );
                }

                for (Entry<Integer, Integer> entry : glyphCount.entrySet()) {
                    String s = ItemGlyph.getLocalizedGlyphName(entry.getKey());
                    int c = entry.getValue();
                    if (c > 1) {
                        s = s + EnumChatFormatting.DARK_GRAY + " (" + c + ")";
                    }

                    list.add(s);
                }
            } else {
                FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(stack.stackTagCompound);
                if (fluidStack != null) {
                    list.add(StatCollector.translateToLocalFormatted("Automagy.tip.tankFluidData", fluidStack.amount, 16000, fluidStack.getLocalizedName()));
                }
            }
        }
    }
}
