package tuhljin.automagy.blocks;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.tiles.TileEntityHourglassMagic;

public class BlockHourglassMagic extends BlockHourglass {
    public static final int TIMER_MAGIC_MINIMUM = 1;
    public static final int TIMER_MAGIC_MAXIMUM = 3600;

    public BlockHourglassMagic(String name) {
        super(name, ModBlocks.materialNiceIron);
        this.setStepSound(Block.soundTypeMetal);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileEntityHourglassMagic();
    }

    @Override
    public MapColor getMapColor(int p_149728_1_) {
        return MapColor.goldColor;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            if (ThaumcraftExtension.playerHasWandEquipped(player)) {
                FMLNetworkHandler.openGui(player, Automagy.instance, 1, world, x, y, z);
            } else {
                TileEntityHourglassMagic te = null;

                try {
                    te = (TileEntityHourglassMagic)world.getTileEntity(x, y, z);
                } catch (Exception var12) {
                }

                if (te == null) {
                    return true;
                }

                if ((te.modeSignalReact != 2 || !te.receivingSignal) && (te.modeSignalReact != 3 || te.receivingSignal)) {
                    if (te.startFlipOnServer(true)) {
                        world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "thaumcraft:cameraclack", 1.0F, 1.0F);
                    }
                } else {
                    world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "random.click", 0.6F, 0.8F);
                }
            }
        }

        return true;
    }
}
