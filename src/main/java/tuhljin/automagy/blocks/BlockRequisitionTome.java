package tuhljin.automagy.blocks;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.lib.References;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.network.MessageParticles;
import tuhljin.automagy.tiles.TileEntityInventarium;
import tuhljin.automagy.tiles.TileEntityRequisitionTome;

public class BlockRequisitionTome extends ModBlockSpecialRender {
    public BlockRequisitionTome(String name) {
        super(name, ModBlocks.materialNiceStone);
        this.setTextureName("thaumcraft:pedestal_top");
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityRequisitionTome();
    }

    @Override
    public int getRenderType() {
        return References.renderBlockRequisitionTomeRI;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int l = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 2.5) & 3;
        world.setBlockMetadataWithNotify(x, y, z, l, 2);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntityRequisitionTome te = (TileEntityRequisitionTome)world.getTileEntity(x, y, z);
            if (te.isUseableByPlayer(player)) {
                if (te.isInventariumLoaded()) {
                    FMLNetworkHandler.openGui(player, Automagy.instance, 13, world, x, y, z);
                } else {
                    TjUtil.sendChatToPlayer(player, "Automagy.chat.inventarium.chunkUnloaded");
                }

                return true;
            }

            if (ThaumcraftExtension.playerHasWandEquipped(player)) {
                if (te.linkY != -1) {
                    TjUtil.sendFormattedChatToPlayer(player, "Automagy.chat.reqTome.alreadyLinked", te.linkX, te.linkY, te.linkZ);
                    return true;
                }

                TileEntityInventarium teI = TileEntityInventarium.getCoreNear(world, x, y, z);
                if (teI != null) {
                    if (teI.addMemoryUser(x, y, z)) {
                        te.setLink(teI);
                        world.playSoundAtEntity(player, "thaumcraft:wand", 1.0F, 1.0F);
                        MessageParticles.sendToClients(world, x, y, z, teI.xCoord, teI.yCoord, teI.zCoord, (short)5);
                        MessageParticles.sendToClients(world, x, y, z, teI.xCoord, teI.yCoord, teI.zCoord, (short)7);
                    } else {
                        TjUtil.sendChatToPlayer(player, "Automagy.chat.reqTome.noConnectionsLeft");
                    }
                } else {
                    TjUtil.sendChatToPlayer(player, "Automagy.chat.reqTome.outOfRange");
                }
            }
        }

        return true;
    }
}
