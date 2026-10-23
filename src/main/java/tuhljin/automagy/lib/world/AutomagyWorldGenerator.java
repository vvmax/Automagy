package tuhljin.automagy.lib.world;

import cpw.mods.fml.common.IWorldGenerator;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.ChestGenHooks;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;
import thaumcraft.common.lib.world.biomes.BiomeHandler;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.lib.compat.CompatibilityManager;

public class AutomagyWorldGenerator implements IWorldGenerator {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (AutomagyConfig.netherSpireChance > 0 && world.provider.dimensionId == -1 && random.nextInt(AutomagyConfig.netherSpireChance) == 0) {
            this.tryGenerateNetherSpire(random, chunkX, chunkZ, world);
        }
    }

    public void tryGenerateNetherSpire(Random rand, int chunkX, int chunkZ, World world) {
        int areaWidth = 10;
        int airHeight = 30;
        int minLavaHeight = 3;
        int x = chunkX * 16 + rand.nextInt(16 - areaWidth);
        int z = chunkZ * 16 + rand.nextInt(16 - areaWidth);
        int y = this.getYAboveLavaPool(world, x, z, minLavaHeight, areaWidth, 1);

        label52:
        while (y > 0) {
            boolean allAir = true;
            int y2 = y;

            while (true) {
                if (y2 < y + airHeight) {
                    if (this.airInArea(world, x, y2, z, areaWidth)) {
                        y2++;
                        continue;
                    }

                    y = this.getYAboveLavaPool(world, x, z, minLavaHeight, areaWidth, y + 1);
                    allAir = false;
                }

                if (allAir) {
                    break label52;
                }
                break;
            }
        }

        if (y > 0) {
            int heightAbove = airHeight - 17 - rand.nextInt(5);

            for (int x2 = x + 3; x2 < x + 6; x2++) {
                for (int z2 = z + 3; z2 < z + 6; z2++) {
                    this.createObsidianColumn(rand, world, x2, y, z2, heightAbove);
                }
            }

            this.createObsidianColumn(rand, world, x + 4, y, z + 2, heightAbove);
            this.createObsidianColumn(rand, world, x + 6, y, z + 4, heightAbove);
            this.createObsidianColumn(rand, world, x + 4, y, z + 6, heightAbove);
            this.createObsidianColumn(rand, world, x + 2, y, z + 4, heightAbove);
            this.createFieryTotem(rand, world, x + 3, y + heightAbove, z + 3, 5);
            this.createFieryTotem(rand, world, x + 3, y + heightAbove, z + 5, 5);
            this.createFieryTotem(rand, world, x + 5, y + heightAbove, z + 3, 5);
            this.createFieryTotem(rand, world, x + 5, y + heightAbove, z + 5, 5);
            this.createFieryTotem(rand, world, x + 4, y + heightAbove + 3, z + 4, 5);
            this.createFieryTotem(rand, world, x + 4, y + heightAbove, z + 3, 2);
            this.createFieryTotem(rand, world, x + 3, y + heightAbove, z + 4, 2);
            this.createFieryTotem(rand, world, x + 5, y + heightAbove, z + 4, 2);
            this.createFieryTotem(rand, world, x + 4, y + heightAbove, z + 5, 2);
            this.createFieryNode(rand, world, x + 4, y + heightAbove + 2, z + 4);
            world.setBlock(x + 4, y + heightAbove + 1, z + 4, Blocks.mob_spawner, 0, 3);
            TileEntityMobSpawner spawner = (TileEntityMobSpawner)world.getTileEntity(x + 4, y + heightAbove + 1, z + 4);
            if (spawner != null) {
                spawner.func_145881_a().setEntityName("Automagy.WispNether");
            }

            this.generateDungeonChest(rand, world, x + 4, y + heightAbove, z + 4, true);
        }
    }

    private int getYAboveLavaPool(World world, int x, int z, int minLavaHeight, int minLavaWidth, int startY) {
        int maxY = world.getActualHeight() - 20;
        int lavaCount = 0;

        for (int y = startY; y < maxY; y++) {
            if (this.lavaInArea(world, x, y, z, minLavaWidth)) {
                lavaCount++;
            } else {
                if (lavaCount >= minLavaHeight && world.isAirBlock(x, y, z)) {
                    return y;
                }

                lavaCount = 0;
            }
        }

        return -1;
    }

    private boolean lavaInArea(World world, int x, int y, int z, int width) {
        int maxX = x + width;
        int maxZ = z + width;

        for (int startZ = z; x < maxX; x++) {
            for (int var9 = startZ; var9 < maxZ; var9++) {
                if (world.getBlock(x, y, var9) != Blocks.lava) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean airInArea(World world, int x, int y, int z, int width) {
        int maxX = x + width;
        int maxZ = z + width;

        for (int startZ = z; x < maxX; x++) {
            for (int var9 = startZ; var9 < maxZ; var9++) {
                if (!world.isAirBlock(x, y, var9)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void createObsidianColumn(Random rand, World world, int x, int y, int z, int heightAbove) {
        int y2;
        for (y2 = y - 1; y2 > 0 && world.getBlock(x, y2, z).getMaterial() == Material.lava; y2--) {
            if (rand.nextInt(10) == 0) {
                if (rand.nextInt(4) == 0) {
                    world.setBlock(x, y2, z, ModBlocks.runedObsidian, rand.nextInt(6), 3);
                } else {
                    world.setBlock(x, y2, z, ConfigBlocks.blockCosmeticSolid, 1, 3);
                }
            } else {
                world.setBlock(x, y2, z, Blocks.obsidian);
            }
        }

        for (int deep = 3; deep > 0 && y2 > 0; y2--) {
            Block block = world.getBlock(x, y2, z);
            if (block != Blocks.netherrack && block != Blocks.soul_sand && block != Blocks.gravel) {
                break;
            }

            world.setBlock(x, y2, z, Blocks.obsidian);
            deep--;
        }

        for (int var10 = y; var10 < y + heightAbove; var10++) {
            if (rand.nextInt(10) == 0) {
                if (rand.nextInt(4) == 0) {
                    world.setBlock(x, var10, z, ModBlocks.runedObsidian, rand.nextInt(6), 3);
                } else {
                    world.setBlock(x, var10, z, ConfigBlocks.blockCosmeticSolid, 1, 3);
                }
            } else {
                world.setBlock(x, var10, z, Blocks.obsidian);
            }
        }
    }

    private void createFieryTotem(Random rand, World world, int x, int y, int z, int height) {
        int y2;
        for (y2 = y; y2 < y + height - 2; y2++) {
            world.setBlock(x, y2, z, ConfigBlocks.blockCosmeticSolid);
        }

        world.setBlock(x, y2, z, ModBlocks.runedObsidian, rand.nextInt(6), 3);
        world.setBlock(x, y2 + 1, z, Blocks.fire);
    }

    private void createFieryNode(Random rand, World world, int x, int y, int z) {
        BiomeGenBase bg = world.getBiomeGenForCoords(x, z);
        int baura = BiomeHandler.getBiomeAura(bg);
        int value = rand.nextInt(baura / 2) + baura / 2;
        AspectList al = new AspectList();
        al.add(Aspect.FIRE, 2);
        if (rand.nextBoolean()) {
            al.merge(Aspect.DEATH, 1);
        } else {
            al.merge(Aspect.UNDEAD, 1);
        }

        if (rand.nextBoolean()) {
            al.merge(Aspect.DARKNESS, 1);
        }

        if (CompatibilityManager.ForbiddenMagicAspect.NETHER != null) {
            al.merge(CompatibilityManager.ForbiddenMagicAspect.NETHER, 1);
        }

        int[] spread = new int[al.size()];
        float total = 0.0F;

        for (int a = 0; a < spread.length; a++) {
            if (al.getAmount(al.getAspectsSorted()[a]) == 2) {
                spread[a] = 50 + rand.nextInt(25);
            } else {
                spread[a] = 25 + rand.nextInt(50);
            }

            total += spread[a];
        }

        for (int a = 0; a < spread.length; a++) {
            al.merge(al.getAspectsSorted()[a], (int)(spread[a] / total * value));
        }

        if (al.getAmount(Aspect.FIRE) < 50) {
            al.add(Aspect.FIRE, rand.nextInt(20) + 20);
        }

        ThaumcraftWorldGenerator.createNodeAt(world, x, y, z, NodeType.DARK, NodeModifier.BRIGHT, al);
    }

    private void generateDungeonChest(Random rand, World world, int x, int y, int z, boolean bonusAutomagyItems) {
        ChestGenHooks info1 = ChestGenHooks.getInfo("dungeonChest");
        ChestGenHooks info2 = ChestGenHooks.getInfo("dungeonChest");
        world.setBlock(x, y, z, Blocks.chest, 0, 3);
        TileEntityChest chest = (TileEntityChest)world.getTileEntity(x, y, z);
        if (chest != null) {
            WeightedRandomChestContent.generateChestContents(rand, info1.getItems(rand), chest, info1.getCount(rand));
            WeightedRandomChestContent.generateChestContents(rand, info2.getItems(rand), chest, info2.getCount(rand));
        }

        if (bonusAutomagyItems) {
            ItemStack loot2 = null;
            int r = rand.nextInt(10);
            ItemStack loot;
            if (r < 4) {
                loot = new ItemStack(ModItems.avaricePearl, rand.nextInt(2) + 2, 0);
            } else if (r < 7) {
                if (rand.nextBoolean()) {
                    loot = new ItemStack(ModItems.enchantedPaper, rand.nextInt(4) + 8, 0);
                    loot2 = new ItemStack(ModItems.enchantedPaper, rand.nextInt(2) + 1, 3);
                } else {
                    loot = new ItemStack(ModItems.enchantedPaper, rand.nextInt(4) + 3, 3);
                    loot2 = new ItemStack(ModItems.enchantedPaper, rand.nextInt(3) + 2, 0);
                }
            } else if (r < 9) {
                loot = new ItemStack(ModItems.shardSliver, rand.nextInt(6) + 5, 1);
            } else {
                loot = new ItemStack(ModItems.edible);
                loot2 = new ItemStack(ModItems.avaricePearl, rand.nextInt(1) + 1, 0);
            }

            for (int i = 0; i < chest.getSizeInventory(); i++) {
                if (chest.getStackInSlot(i) == null) {
                    chest.setInventorySlotContents(i, loot);
                    if (loot2 == null) {
                        break;
                    }

                    loot = loot2;
                    loot2 = null;
                }
            }
        }
    }
}
