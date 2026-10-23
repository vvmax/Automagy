package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import tuhljin.automagy.items.ItemGolemLinker;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.network.MessageParticles;
import tuhljin.automagy.tiles.TileEntityGolemTaskmaster;
import tuhljin.automagy.tiles.TileEntityInventarium;

public class BlockGolemTaskmaster extends ModBlockSpecialRender {
    public long nextNoise = 0L;

    public BlockGolemTaskmaster(String name) {
        super(name, Material.iron);
        this.setStepSound(Block.soundTypeMetal);
        this.setBlockBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.85F, 0.8F);
        this.setHardness(5.0F);
        this.setResistance(10.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityGolemTaskmaster();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int l = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 2.5) & 3;
        world.setBlockMetadataWithNotify(x, y, z, l, 2);
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return !TjUtil.isAcceptableSurfaceForBlock(world, x, y - 1, z, true, true, true) ? false : super.canPlaceBlockAt(world, x, y, z);
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        return !TjUtil.isAcceptableSurfaceForBlock(world, x, y - 1, z, true, true, true) ? false : super.canBlockStay(world, x, y, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!this.canBlockStay(world, x, y, z)) {
            this.dropBlockAsItem(world, x, y, z, 0, 0);
            world.setBlockToAir(x, y, z);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityGolemTaskmaster && ((TileEntityGolemTaskmaster)te).isActive()) {
            if (rand.nextInt(6) == 0) {
                Thaumcraft.proxy.blockRunes(world, x, y, z, 1.0F, 0.4F, 0.0F, 7, 0.002F);
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime >= this.nextNoise) {
                this.nextNoise = currentTime + 25000L + rand.nextInt(20000);
                world.playSound(x + 0.5, y + 0.5, z + 0.5, "thaumcraft:brain", 0.05F, world.rand.nextFloat() * 0.5F + 0.2F, false);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        ItemStack playerItem = player.getCurrentEquippedItem();
        if (playerItem == null || playerItem.getItem() instanceof ItemGolemLinker) {
            return false;
        } else if (ThaumcraftExtension.playerHasWandEquipped(player)) {
            if (!world.isRemote) {
                TileEntityGolemTaskmaster te = (TileEntityGolemTaskmaster)world.getTileEntity(x, y, z);
                if (te.linkY != -1) {
                    TjUtil.sendFormattedChatToPlayer(player, "Automagy.chat.golemTaskmaster.alreadyLinked", te.linkX, te.linkY, te.linkZ);
                    return true;
                }

                TileEntityInventarium teI = TileEntityInventarium.getCoreNear(world, x, y, z);
                if (teI != null) {
                    if (teI.addMemoryUser(x, y, z)) {
                        te.setLink(teI);
                        world.playSoundAtEntity(player, "thaumcraft:wand", 1.0F, 1.0F);
                        MessageParticles.sendToClients(world, x, y, z, teI.xCoord, teI.yCoord, teI.zCoord, (short)7);
                    } else {
                        TjUtil.sendChatToPlayer(player, "Automagy.chat.golemTaskmaster.noConnectionsLeft");
                    }
                } else {
                    TjUtil.sendChatToPlayer(player, "Automagy.chat.golemTaskmaster.outOfRange");
                }
            }

            return true;
        } else {
            return false;
        }
    }
}
