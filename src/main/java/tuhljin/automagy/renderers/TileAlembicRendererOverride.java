package tuhljin.automagy.renderers;

import net.minecraft.world.World;
import thaumcraft.client.renderers.tile.TileAlembicRenderer;
import thaumcraft.common.tiles.TileAlembic;
import tuhljin.automagy.blocks.ModBlocks;

public class TileAlembicRendererOverride extends TileAlembicRenderer {
    @Override
    public void renderTileEntityAt(TileAlembic tile, double par2, double par4, double par6, float par8) {
        if (!tile.aboveFurnace && !tile.aboveAlembic) {
            World world = tile.getWorld();
            if (world != null && world.getBlock(tile.xCoord, tile.yCoord - 1, tile.zCoord) == ModBlocks.boiler) {
                tile.aboveFurnace = true;
            }
        }

        super.renderTileEntityAt(tile, par2, par4, par6, par8);
    }
}
