package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.api.ItemApi;
import thaumcraft.common.Thaumcraft;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityInventarium;

public class BlockInventarium extends ModBlockSpecialRender {
    public long nextNoise = 0L;

    public BlockInventarium(String name) {
        super(name, ModBlocks.materialNiceStone, ItemBlockRarityRare.class);
        this.setBlockBounds(0.25F, 0.32F, 0.25F, 0.75F, 0.9F, 0.75F);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityInventarium();
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return TileEntityInventarium.checkValidity(world, x, y, z, true);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = super.getDrops(world, x, y, z, metadata, fortune);

        for (int i = 0; i < 6; i++) {
            ItemStack shard = ItemApi.getItem("itemShard", 4);
            drops.add(shard);
        }

        return drops;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block p_149749_5_, int p_149749_6_) {
        world.playSoundEffect(x, y, z, "thaumcraft:craftfail", 0.75F, 1.0F);
        TileEntityInventarium te = null;

        try {
            te = (TileEntityInventarium)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null) {
            te.breakAllLinks();
        }

        super.breakBlock(world, x, y, z, p_149749_5_, p_149749_6_);
        world.notifyBlocksOfNeighborChange(x - 2, y - 1, z - 1, this);
        world.notifyBlocksOfNeighborChange(x - 2, y - 1, z + 1, this);
        world.notifyBlocksOfNeighborChange(x + 2, y - 1, z - 1, this);
        world.notifyBlocksOfNeighborChange(x + 2, y - 1, z + 1, this);
        world.notifyBlocksOfNeighborChange(x - 1, y - 1, z - 2, this);
        world.notifyBlocksOfNeighborChange(x + 1, y - 1, z - 2, this);
        world.notifyBlocksOfNeighborChange(x - 1, y - 1, z + 2, this);
        world.notifyBlocksOfNeighborChange(x + 1, y - 1, z + 2, this);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote) {
            TileEntityInventarium te = null;

            try {
                te = (TileEntityInventarium)world.getTileEntity(x, y, z);
            } catch (Exception var7) {
            }

            if (te != null) {
                te.beginPlacementProcess();
            }

            world.notifyBlocksOfNeighborChange(x - 2, y - 1, z - 1, this);
            world.notifyBlocksOfNeighborChange(x - 2, y - 1, z + 1, this);
            world.notifyBlocksOfNeighborChange(x + 2, y - 1, z - 1, this);
            world.notifyBlocksOfNeighborChange(x + 2, y - 1, z + 1, this);
            world.notifyBlocksOfNeighborChange(x - 1, y - 1, z - 2, this);
            world.notifyBlocksOfNeighborChange(x + 1, y - 1, z - 2, this);
            world.notifyBlocksOfNeighborChange(x - 1, y - 1, z + 2, this);
            world.notifyBlocksOfNeighborChange(x + 1, y - 1, z + 2, this);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        if (rand.nextBoolean()) {
            Thaumcraft.proxy.blockRunes(world, x, y, z, 1.1F, 0.51F, 1.0F, 12, 0.02F);
        }

        TileEntityInventarium te = (TileEntityInventarium)world.getTileEntity(x, y, z);

        for (int i = 0; i <= te.maxMemoryUsers; i++) {
            if (rand.nextInt(6) == 0) {
                Thaumcraft.proxy.sparkle(x + rand.nextFloat(), y - 1.2F, z + rand.nextFloat(), rand.nextFloat() + 0.5F, 4, -0.25F);
            }
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime >= this.nextNoise) {
            this.nextNoise = currentTime + 25000L + rand.nextInt(20000);
            world.playSound(x + 0.5, y + 0.5, z + 0.5, "thaumcraft:brain", 0.05F, world.rand.nextFloat() * 0.5F + 0.2F, false);
        }

        if (rand.nextInt(3) < 2) {
            ArrayList<BlockCoord> list = new ArrayList<>();

            for (int l = x - 3; l <= x + 3; l++) {
                for (int i1 = z - 3; i1 <= z + 3; i1++) {
                    if (rand.nextInt(40) == 0) {
                        for (int j1 = y - 3; j1 <= y + 3; j1++) {
                            if (TileEntityInventarium.blockIsBookshelf(world, l, j1, i1)) {
                                if (!world.isAirBlock((l - x) / 2 + x, j1, (i1 - z) / 2 + z)) {
                                    break;
                                }

                                list.add(new BlockCoord(l, j1, i1));
                            }
                        }
                    }
                }
            }

            if (list.size() > 0) {
                Collections.shuffle(list);
                int max = rand.nextInt(1) + 1;
                int num = 1;

                for (BlockCoord bc : list) {
                    world.spawnParticle(
                        "enchantmenttable",
                        x + 0.5,
                        y + 1.8,
                        z + 0.5,
                        bc.x - x + rand.nextFloat() - 0.5,
                        bc.y - y - rand.nextFloat() - 1.0F,
                        bc.z - z + rand.nextFloat() - 0.5
                    );
                    if (++num > max) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!ThaumcraftExtension.playerHasWandEquipped(player)) {
            return false;
        } else {
            if (!world.isRemote) {
                TileEntityInventarium te = (TileEntityInventarium)world.getTileEntity(x, y, z);
                if (TileEntityInventarium.checkValidity(world, x, y, z, false)) {
                    te.checkMaxes();
                    te.getItemList();
                    te.verifyMemoryUsers();
                    TjUtil.sendFormattedChatToPlayer(player, "Automagy.chat.inventarium.statusSlots", te.usedSlots, te.maxSlotsRead);
                    if (te.maxMemoryUsers <= 0 && !ThaumcraftExtension.isResearchComplete(player, "INVENTARIUM_DELIVERY")) {
                        TjUtil.sendFormattedChatToPlayer(player, "Automagy.chat.inventarium.statusScribes", te.getNumScribes(), te.maxInvNodes);
                    } else {
                        TjUtil.sendFormattedChatToPlayer(
                            player, "Automagy.chat.inventarium.statusNodes", te.getNumScribes(), te.maxInvNodes, te.getNumMemoryUsers(), te.maxMemoryUsers
                        );
                    }

                    te.markAsPreferredLink();
                }
            }

            return true;
        }
    }
}
