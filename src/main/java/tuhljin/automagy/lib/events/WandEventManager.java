package tuhljin.automagy.lib.events;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.api.wands.IWandTriggerManager;
import thaumcraft.api.wands.WandTriggerRegistry;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockChestHungry;
import thaumcraft.common.blocks.BlockMagicalLog;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.foci.ItemFocusPortableHole;
import thaumcraft.common.tiles.TileJarNode;
import thaumcraft.common.tiles.TileOwned;
import thaumcraft.common.tiles.TileWarded;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.config.ModRecipes;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.network.MessageParticles;

public class WandEventManager implements IWandTriggerManager {
    private static final int REACT_OBSIDIAN = 0;
    private static final int REACT_WARDEDBLOCK = 1;

    public WandEventManager() {
        WandTriggerRegistry.registerWandBlockTrigger(this, 0, Blocks.obsidian, 0, "Automagy");
        if (AutomagyConfig.allowAdvNodeJarring) {
            WandTriggerRegistry.registerWandBlockTrigger(this, 1, ConfigBlocks.blockWarded, -1, "Automagy");
        }
    }

    @Override
    public boolean performTrigger(World world, ItemStack wand, EntityPlayer player, int x, int y, int z, int side, int event) {
        switch (event) {
            case 0:
                if (ThaumcraftExtension.isResearchComplete(player, "HUNGRYMAW") && this.transformObsidian(world, wand, player, x, y, z)) {
                    return false;
                }
                break;
            case 1:
                if (ThaumcraftExtension.isResearchComplete(player, "ADVNODEJAR")) {
                    return this.attemptJarNode(world, wand, player, x, y, z);
                }
        }

        return false;
    }

    public boolean attemptJarNode(World world, ItemStack wandStack, EntityPlayer player, int x, int y, int z) {
        ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
        if (wand.getFocus(wandStack) != null) {
            return false;
        } else {
            for (int xx = x - 2; xx <= x; xx++) {
                for (int yy = y - 3; yy <= y; yy++) {
                    for (int zz = z - 2; zz <= z; zz++) {
                        if (this.isAdvJarStructureAt(world, xx, yy, zz, player)) {
                            if (ThaumcraftApiHelper.consumeVisFromWandCrafting(wandStack, player, ModRecipes.visCostAdvNodeJar, true)) {
                                if (!world.isRemote) {
                                    this.createAdvJarFromStructure(world, xx, yy, zz);
                                    return true;
                                }

                                return false;
                            }

                            return false;
                        }
                    }
                }
            }

            return false;
        }
    }

    public boolean transformObsidian(World world, ItemStack wand, EntityPlayer player, int x, int y, int z) {
        if (ThaumcraftExtension.getWandFocus(wand) instanceof ItemFocusPortableHole) {
            Block b = world.getBlock(x, y - 1, z);
            if (b instanceof BlockChestHungry) {
                if (!world.isRemote) {
                    MessageParticles.sendToClients(world, x, y - 1, z, 0, 0, 0, (short)0);
                    world.setBlock(x, y - 1, z, ModBlocks.specialProcessBlock, 0, 3);
                } else {
                    for (int i = 0; i < 10; i++) {
                        Thaumcraft.proxy
                            .wispFX3(
                                world,
                                x + 0.5F + world.rand.nextFloat() / 2.0F,
                                y - 0.5F + world.rand.nextFloat() / 2.0F,
                                z + 0.5F + world.rand.nextFloat() / 2.0F,
                                x + 0.3F + world.rand.nextFloat() * 0.4F,
                                y + 0.5F,
                                z + 0.3F + world.rand.nextFloat() * 0.4F,
                                0.5F,
                                5,
                                true,
                                -0.025F
                            );
                        Thaumcraft.proxy
                            .wispFX3(
                                world,
                                x + 0.5F - world.rand.nextFloat() / 2.0F,
                                y - 0.5F - world.rand.nextFloat() / 2.0F,
                                z + 0.5F - world.rand.nextFloat() / 2.0F,
                                x + 0.3F + world.rand.nextFloat() * 0.4F,
                                y + 0.5F,
                                z + 0.3F + world.rand.nextFloat() * 0.4F,
                                0.5F,
                                5,
                                true,
                                -0.025F
                            );
                    }
                }

                return true;
            }
        }

        return false;
    }

    private boolean isWardedSilverwoodLog(World world, int x, int y, int z, EntityPlayer pOwner) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileWarded) {
            TileWarded warded = (TileWarded)te;
            if (warded.block == ConfigBlocks.blockMagicalLog && BlockMagicalLog.limitToValidMetadata(warded.blockMd) == 1) {
                return pOwner.getCommandSenderName().hashCode() == warded.owner;
            }
        }

        return false;
    }

    private boolean isWardedGlass(World world, int x, int y, int z, EntityPlayer pOwner) {
        if (world.getBlock(x, y, z) == ConfigBlocks.blockCosmeticOpaque && world.getBlockMetadata(x, y, z) == 2) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileOwned) {
                return pOwner.getCommandSenderName().equals(((TileOwned)te).owner);
            }
        }

        return false;
    }

    private boolean isAdvJarStructureAt(World world, int x, int y, int z, EntityPlayer pOwner) {
        int[][][] blueprint = new int[][][]{
            {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}, {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}, {{2, 2, 2}, {2, 3, 2}, {2, 2, 2}}, {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}
        };

        for (int yy = 0; yy < 4; yy++) {
            for (int xx = 0; xx < 3; xx++) {
                for (int zz = 0; zz < 3; zz++) {
                    int index = blueprint[yy][xx][zz];
                    if (index == 1) {
                        if (!this.isWardedSilverwoodLog(world, x + xx, y - yy + 2, z + zz, pOwner)) {
                            return false;
                        }
                    } else if (index == 2) {
                        if (!this.isWardedGlass(world, x + xx, y - yy + 2, z + zz, pOwner)) {
                            return false;
                        }
                    } else if (index == 3) {
                        TileEntity tile = world.getTileEntity(x + xx, y - yy + 2, z + zz);
                        if (tile == null || !(tile instanceof INode) || tile instanceof TileJarNode) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private void createAdvJarFromStructure(World world, int x, int y, int z) {
        int[][][] blueprint = new int[][][]{
            {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}, {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}, {{2, 2, 2}, {2, 3, 2}, {2, 2, 2}}, {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}
        };

        for (int yy = 0; yy < 4; yy++) {
            for (int xx = 0; xx < 3; xx++) {
                for (int zz = 0; zz < 3; zz++) {
                    int index = blueprint[yy][xx][zz];
                    if (index == 3) {
                        TileEntity tile = world.getTileEntity(x + xx, y - yy + 2, z + zz);
                        INode node = (INode)tile;
                        AspectList na = node.getAspects().copy();
                        int nt = node.getNodeType().ordinal();
                        int nm = -1;
                        if (node.getNodeModifier() != null) {
                            nm = node.getNodeModifier().ordinal();
                        }

                        String nid = node.getId();
                        node.setAspects(new AspectList());
                        world.removeTileEntity(x + xx, y - yy + 2, z + zz);
                        world.setBlock(x + xx, y - yy + 2, z + zz, ConfigBlocks.blockJar, 2, 3);
                        tile = world.getTileEntity(x + xx, y - yy + 2, z + zz);
                        TileJarNode jar = (TileJarNode)tile;
                        jar.setAspects(na);
                        if (nm >= 0) {
                            jar.setNodeModifier(NodeModifier.values()[nm]);
                        }

                        jar.setNodeType(NodeType.values()[nt]);
                        jar.setId(nid);
                        world.addBlockEvent(x + xx, y - yy + 2, z + zz, ConfigBlocks.blockJar, 9, 0);
                    } else {
                        world.setBlockToAir(x + xx, y - yy + 2, z + zz);
                    }
                }
            }
        }
    }
}
