package tuhljin.automagy.lib.compat.waila;

import java.util.List;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import tuhljin.automagy.items.ItemGlyph;
import tuhljin.automagy.tiles.TileEntityThirstyTank;

public class ProviderForThirstyTank extends ProviderForTank {
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity te = accessor.getTileEntity();
        if (!(te instanceof TileEntityThirstyTank)) {
            return super.getWailaBody(itemStack, currenttip, accessor, config);
        } else {
            currenttip = super.getWailaBody(itemStack, currenttip, accessor, config);
            int side = accessor.getSide().ordinal();
            int glyph = ((TileEntityThirstyTank)te).glyphs[side];
            if (glyph > 0) {
                String s = ItemGlyph.getLocalizedGlyphName(glyph);
                currenttip.add(EnumChatFormatting.AQUA + s);
            }

            return currenttip;
        }
    }
}
