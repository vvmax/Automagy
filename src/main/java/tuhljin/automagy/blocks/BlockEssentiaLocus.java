package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.api.ItemApi;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXSparkle;
import thaumcraft.common.Thaumcraft;
import tuhljin.automagy.items.ItemCrystalEye;
import tuhljin.automagy.lib.IAutomagyLocationLink;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.tiles.TileEntityEssentiaLocus;

public class BlockEssentiaLocus extends ModBlockSpecialRender {
    public long nextNoise = 0L;

    public BlockEssentiaLocus(String name) {
        super(name, ModBlocks.materialNiceStone, ItemBlockRarityRare.class);
        this.setBlockBounds(0.25F, 0.32F, 0.25F, 0.75F, 0.9F, 0.75F);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityEssentiaLocus();
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return TileEntityEssentiaLocus.checkValidity(world, x, y, z, true);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = super.getDrops(world, x, y, z, metadata, fortune);

        for (int i = 0; i < 6; i++) {
            ItemStack shard = ItemApi.getItem("itemShard", 2);
            drops.add(shard);
        }

        return drops;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block p_149749_5_, int p_149749_6_) {
        world.playSoundEffect(x, y, z, "thaumcraft:craftfail", 0.75F, 1.0F);
        TileEntityEssentiaLocus te = null;

        try {
            te = (TileEntityEssentiaLocus)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null) {
            TjUtil.dropItemsIntoWorld(te.eyesContainingInventory, world, x, y, z, null);
        }

        super.breakBlock(world, x, y, z, p_149749_5_, p_149749_6_);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote) {
            TileEntityEssentiaLocus te = null;

            try {
                te = (TileEntityEssentiaLocus)world.getTileEntity(x, y, z);
            } catch (Exception var7) {
            }

            if (te != null) {
                te.beginPlacementProcess();
            }

            world.notifyBlockOfNeighborChange(x - 1, y - 3, z - 1, this);
            world.notifyBlockOfNeighborChange(x - 1, y - 3, z, this);
            world.notifyBlockOfNeighborChange(x - 1, y - 3, z + 1, this);
            world.notifyBlockOfNeighborChange(x, y - 3, z - 1, this);
            world.notifyBlockOfNeighborChange(x, y - 3, z + 1, this);
            world.notifyBlockOfNeighborChange(x + 1, y - 3, z - 1, this);
            world.notifyBlockOfNeighborChange(x + 1, y - 3, z, this);
            world.notifyBlockOfNeighborChange(x + 1, y - 3, z + 1, this);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        if (rand.nextBoolean()) {
            Thaumcraft.proxy.blockRunes(world, x, y, z, 0.4F, 0.4F, 0.9F, 12, 0.02F);
        }

        boolean hasAggregator = false;
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityEssentiaLocus) {
            hasAggregator = ((TileEntityEssentiaLocus)te).hasAggregator;
        }

        int n = rand.nextInt(2) + 2;

        for (int i = 0; i < n; i++) {
            float x2 = x - 1 + rand.nextInt(3) + rand.nextFloat();
            float z2 = z - 1 + rand.nextInt(3) + rand.nextFloat();
            if (hasAggregator && ((TileEntityEssentiaLocus)te).getAspects().size() > 0) {
                float x3 = x - (x2 - x) + 1.0F;
                float z3 = z - (z2 - z) + 1.0F;
                FXSparkle ef2 = new FXSparkle(world, x2, y - 1.2F, z2, x3, y + 1.5F, z3, 1.0F, rand.nextBoolean() ? 2 : 7, 7);
                ef2.setGravity(-0.05F);
                ef2.noClip = true;
                ParticleEngine.instance.addEffect(world, ef2);
            } else {
                Thaumcraft.proxy.sparkle(x2, y - 1.2F, z2, rand.nextFloat() + 0.5F, 2, -0.25F);
            }
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime >= this.nextNoise) {
            this.nextNoise = currentTime + 25000L + rand.nextInt(20000);
            world.playSound(x + 0.5, y + 0.5, z + 0.5, "thaumcraft:brain", 0.05F, world.rand.nextFloat() * 0.5F + 0.2F, false);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileEntityEssentiaLocus te = (TileEntityEssentiaLocus)world.getTileEntity(x, y, z);
        ItemStack playerStack = player.getCurrentEquippedItem();
        if (playerStack == null) {
            ItemStack stack = te.removeEye();
            if (stack != null) {
                if (!player.inventory.addItemStackToInventory(stack)) {
                    player.dropPlayerItemWithRandomChoice(stack, false);
                }

                world.playSoundEffect(x, y, z, "random.pop", 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F);
                return true;
            }
        } else {
            te.checkMaxes();
            if (te.itemIsValidEye(playerStack)) {
                if (te.insertEye(playerStack)) {
                    if (playerStack.stackSize <= 0) {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                    }

                    world.playSoundEffect(x, y, z, "random.pop", 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F);
                }

                return true;
            }

            if (playerStack.getItem() instanceof IAutomagyLocationLink) {
                if (world.isRemote) {
                    WorldSpecificCoordinates coord = ((ItemCrystalEye)playerStack.getItem()).getLinkLocation(playerStack);
                    if (coord != null && coord.x == x && coord.y == y && coord.z == z) {
                        TjUtil.sendChatToPlayer(player, "Automagy.chat.essentiaLocus.itemLinkedSelf");
                    } else {
                        TjUtil.sendFormattedChatToPlayer(player, "Automagy.chat.essentiaLocus.itemUnlinked", te.getMaxDistance());
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!world.isRemote) {
            TileEntityEssentiaLocus te = (TileEntityEssentiaLocus)world.getTileEntity(x, y, z);
            te.checkAggregator();
        }
    }
}
