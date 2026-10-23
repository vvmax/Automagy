package tuhljin.automagy.config;

import cpw.mods.fml.common.registry.GameRegistry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.oredict.RecipeSorter.Category;
import thaumcraft.api.ItemApi;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionEnchantmentRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.wands.ItemWandCasting;
import tuhljin.automagy.blocks.BlockNethermind;
import tuhljin.automagy.blocks.BlockThaumostaticPylon;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.lib.recipe.AdvGolemRecipe;
import tuhljin.automagy.lib.recipe.PreserveFilterRecipe;

public class ModRecipes {
    public static List essentiaLocusConstructRecipe;
    public static List inventariumConstructRecipe;
    public static List thaumostaticSpireConstructRecipe;
    public static List nethermindConstructRecipe;
    public static List mobSpawnerConstructRecipe;
    public static List advNodeJarRecipe;
    public static int NUM_GOLEM_TYPES = 8;
    private static HashMap<Object, Object> recipes = new HashMap<>();
    private static HashMap<Object, Object> recipesAlt = new HashMap<>();
    public static CrucibleRecipe[] infusedOreRefining = new CrucibleRecipe[6];
    public static CrucibleRecipe duplicateConfluxDisc;
    public static Enchantment enchantmentLure = Enchantment.lure;
    public static Enchantment enchantmentLuckOfTheSea = Enchantment.luckOfTheSea;
    public static InfusionEnchantmentRecipe enchantmentRecipeLure;
    public static InfusionEnchantmentRecipe enchantmentRecipeLuckOfTheSea;
    public static AspectList visCostAdvNodeJar = new AspectList()
        .add(Aspect.FIRE, 125)
        .add(Aspect.EARTH, 125)
        .add(Aspect.ORDER, 125)
        .add(Aspect.AIR, 125)
        .add(Aspect.ENTROPY, 125)
        .add(Aspect.WATER, 125);

    public static void registerRecipesAndAspectBreakdown() {
        ItemStack quicksilver = ItemApi.getItem("itemResource", 3);
        ItemStack amber = ItemApi.getItem("itemResource", 6);
        ItemStack amberBlock = ItemApi.getBlock("blockCosmeticOpaque", 0);
        ItemStack arcaneStoneBlock = ItemApi.getBlock("blockCosmeticSolid", 6);
        ItemStack arcaneStoneBricks = ItemApi.getBlock("blockCosmeticSolid", 7);
        ItemStack zombieBrain = ItemApi.getItem("itemZombieBrain", 0);
        ItemStack greatwoodPlanks = ItemApi.getBlock("blockWoodenDevice", 6);
        ItemStack silverwoodPlanks = ItemApi.getBlock("blockWoodenDevice", 7);
        ItemStack visFilter = ItemApi.getItem("itemResource", 8);
        ItemStack visRelay = ItemApi.getBlock("blockMetalDevice", 14);
        ItemStack alchemicalConstruct = ItemApi.getBlock("blockMetalDevice", 9);
        ItemStack salisMundus = ItemApi.getItem("itemResource", 14);
        ItemStack thaumiumNugget = ItemApi.getItem("itemNugget", 6);
        ItemStack thaumiumIngot = ItemApi.getItem("itemResource", 2);
        ItemStack voidmetalIngot = ItemApi.getItem("itemResource", 16);
        ItemStack balancedShard = ItemApi.getItem("itemShard", 6);
        ItemStack nitor = ItemApi.getItem("itemResource", 1);
        ItemStack arcaneSlab = ItemApi.getBlock("blockSlabStone", 0);
        ItemStack clusterOrder = ItemApi.getBlock("blockCrystal", 4);
        ItemStack totem = ItemApi.getBlock("blockCosmeticSolid", 0);
        ItemStack tallowBlock = ItemApi.getBlock("blockCosmeticSolid", 5);
        ItemStack mirroredGlass = ItemApi.getItem("itemResource", 10);
        ItemStack[] golemUpgrades = new ItemStack[]{
            ItemApi.getItem("itemGolemUpgrade", 0),
            ItemApi.getItem("itemGolemUpgrade", 2),
            ItemApi.getItem("itemGolemUpgrade", 3),
            ItemApi.getItem("itemGolemUpgrade", 1),
            ItemApi.getItem("itemGolemUpgrade", 4),
            ItemApi.getItem("itemGolemUpgrade", 5)
        };
        ItemStack redcrystal = new ItemStack(ModBlocks.redcrystal);
        ItemStack runedObsidian = new ItemStack(ModBlocks.runedObsidian);
        ItemStack enchantedPaper = new ItemStack(ModItems.enchantedPaper, 1, 0);
        ItemStack enchantedTome = new ItemStack(ModItems.enchantedPaper, 1, 3);
        ItemStack whitelist = new ItemStack(ModItems.enchantedPaper, 1, 1);
        ItemStack blacklist = new ItemStack(ModItems.enchantedPaper, 1, 2);
        ItemStack[] slivers = new ItemStack[6];
        ItemStack aquaInk = new ItemStack(ModItems.tankGlyph, 1, 0);
        ItemStack empty = ItemApi.getBlock("blockHole", 15);
        ItemStack string = new ItemStack(Items.string);
        ItemStack feather = new ItemStack(Items.feather);
        ItemStack quartz = new ItemStack(Items.quartz);
        Aspect[] primalList = new Aspect[]{Aspect.AIR, Aspect.FIRE, Aspect.WATER, Aspect.EARTH, Aspect.ORDER, Aspect.ENTROPY};

        for (int i = 0; i < primalList.length; i++) {
            addShapelessArcaneCraftingRecipe(ModItems.shardSliver, 4, i, "SLIVERS", new AspectList().add(Aspect.ENTROPY, 1), ItemApi.getItem("itemShard", i));
            ItemStack sliver = new ItemStack(ModItems.shardSliver, 1, i);
            slivers[i] = sliver;
            ThaumcraftApi.registerObjectTag(sliver, new AspectList().add(primalList[i], 1));
            addShapelessArcaneCraftingRecipe(
                ItemApi.getItem("itemShard", i).getItem(),
                1,
                i,
                "SLIVERS",
                new AspectList().add(Aspect.ORDER, 2).add(primalList[i], 2),
                sliver,
                sliver,
                sliver,
                sliver
            );
            addCrucibleRecipe(ModItems.crystalBrain, 1, i, "CRYSTALBRAIN", zombieBrain, new AspectList().add(Aspect.CRYSTAL, 10).add(primalList[i], 8));
            ThaumcraftApi.registerObjectTag(
                new ItemStack(ModItems.crystalBrain, 1, i),
                new AspectList().add(Aspect.CRYSTAL, 8).add(primalList[i], 8).add(Aspect.MIND, 4).add(Aspect.UNDEAD, 2)
            );
            if (AutomagyConfig.sliversFromOreRefining > 0) {
                infusedOreRefining[i] = ThaumcraftApi.addCrucibleRecipe(
                    "ENTROPICREFINING",
                    new ItemStack(ModItems.shardSliver, AutomagyConfig.sliversFromOreRefining, i),
                    ItemApi.getBlock("blockCustomOre", i + 1),
                    new AspectList().add(Aspect.ENTROPY, 3).add(Aspect.CRYSTAL, 1)
                );
            }
        }

        addShapelessArcaneCraftingRecipe(
            arcaneStoneBlock.getItem(),
            3,
            6,
            "SLIVERS",
            new AspectList().add(Aspect.EARTH, 1).add(Aspect.FIRE, 1),
            Blocks.stone,
            new ItemStack(ModItems.shardSliver, 1, 32767),
            Blocks.stone
        );
        addShapelessArcaneCraftingRecipe(
            ItemApi.getBlock("blockCosmeticSolid", 2).getItem(),
            1,
            2,
            "SLIVERS_TRAVEL",
            new AspectList().add(Aspect.EARTH, 3).add(Aspect.AIR, 3),
            arcaneStoneBricks,
            slivers[0],
            slivers[3]
        );
        addShapelessArcaneCraftingRecipe(
            ItemApi.getBlock("blockCosmeticSolid", 3).getItem(),
            1,
            3,
            "SLIVERS_WARDING",
            new AspectList().add(Aspect.FIRE, 3).add(Aspect.ORDER, 3),
            arcaneStoneBricks,
            slivers[1],
            slivers[4]
        );
        addShapelessArcaneCraftingRecipe(ModBlocks.torchInversion_on, 1, 0, "SLIVERS", new AspectList().add(Aspect.ORDER, 1), Blocks.redstone_torch, slivers[4]);
        addBasicRecipe(ModBlocks.hourglass, 1, 0, " _ ", "|S|", " _R", '_', "slabWood", '|', Blocks.glass_pane, 'S', Blocks.sand, 'R', Items.repeater);
        addArcaneCraftingRecipe(
            ModBlocks.hourglassMagic,
            1,
            0,
            "MAGICHOURGLASS",
            new AspectList().add(Aspect.ORDER, 4).add(Aspect.AIR, 2).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1),
            " R ",
            "OHA",
            " P ",
            'R',
            redcrystal,
            'O',
            slivers[4],
            'H',
            new ItemStack(ModBlocks.hourglass),
            'P',
            Blocks.light_weighted_pressure_plate,
            'A',
            slivers[0]
        );
        addArcaneCraftingRecipe(
            ModBlocks.translucent,
            1,
            0,
            "NITORLIGHT",
            new AspectList().add(Aspect.EARTH, 2).add(Aspect.WATER, 2).add(Aspect.FIRE, 4),
            " A ",
            "ANA",
            " A ",
            'A',
            amber,
            'N',
            nitor
        );
        addArcaneCraftingRecipe(
            ModBlocks.translucent,
            1,
            1,
            "NITORLIGHT",
            new AspectList().add(Aspect.EARTH, 2).add(Aspect.WATER, 2).add(Aspect.FIRE, 4),
            "A A",
            " N ",
            "A A",
            'A',
            amber,
            'N',
            nitor
        );
        addCrucibleRecipe(
            ModItems.crystalEye, 1, 0, "REMOTECOMPARATOR", new ItemStack(Items.spider_eye), new AspectList().add(Aspect.CRYSTAL, 2).add(Aspect.SENSES, 4)
        );
        addCrucibleRecipe(
            ModItems.crystalEntityEye, 1, 0, "ENTITYSIGNAL", new ItemStack(ModItems.crystalEye), new AspectList().add(Aspect.BEAST, 4).add(Aspect.SOUL, 2)
        );
        ShapelessOreRecipe recipe = new ShapelessOreRecipe(new ItemStack(ModItems.crystalEye, 1, 0), ModItems.crystalEye);
        GameRegistry.addRecipe(recipe);
        recipe = new ShapelessOreRecipe(new ItemStack(ModItems.crystalEntityEye, 1, 0), ModItems.crystalEntityEye);
        GameRegistry.addRecipe(recipe);
        addShapelessBasicRecipe(ModItems.food, 1, 0, ItemApi.getBlock("blockCustomPlant", 5), Blocks.brown_mushroom, Items.bowl);
        addAlternateShapelessBasicRecipe(ModItems.food, 1, 0, ItemApi.getBlock("blockCustomPlant", 5), Blocks.red_mushroom, Items.bowl);
        addArcaneCraftingRecipe(
            ModBlocks.remoteComparator,
            1,
            0,
            "REMOTECOMPARATOR",
            new AspectList().add(Aspect.AIR, 5).add(Aspect.ENTROPY, 5).add(Aspect.ORDER, 5),
            " R ",
            "RAR",
            "SSS",
            'R',
            redcrystal,
            'A',
            "shardAir",
            'S',
            arcaneStoneBlock
        );
        addArcaneCraftingRecipe(
            ModBlocks.visReader,
            1,
            0,
            "VISREADER",
            new AspectList().add(Aspect.AIR, 3).add(Aspect.EARTH, 3).add(Aspect.FIRE, 3).add(Aspect.WATER, 3).add(Aspect.ORDER, 3).add(Aspect.ENTROPY, 3),
            " T ",
            "SQS",
            " SR",
            'T',
            ItemApi.getItem("itemThaumometer", 0),
            'S',
            arcaneStoneBlock,
            'Q',
            quartz,
            'R',
            redcrystal
        );
        addArcaneCraftingRecipe(
            ModBlocks.tallyBlock,
            1,
            0,
            "TALLYBLOCK",
            new AspectList().add(Aspect.AIR, 2).add(Aspect.EARTH, 2).add(Aspect.ORDER, 3),
            " G ",
            "WQW",
            " WR",
            'G',
            "blockGlass",
            'W',
            greatwoodPlanks,
            'Q',
            quartz,
            'R',
            redcrystal
        );
        addArcaneCraftingRecipe(
            ModItems.tallyLens,
            1,
            0,
            "TALLYBLOCK_LENS",
            new AspectList().add(Aspect.AIR, 2).add(Aspect.EARTH, 1),
            " g ",
            "gPg",
            " gR",
            'g',
            Items.gold_nugget,
            'P',
            Blocks.glass_pane,
            'R',
            Items.redstone
        );
        addCrucibleRecipe(
            ModItems.tallyLens, 1, 1, "TALLYBLOCK_LENS", new ItemStack(ModItems.tallyLens), new AspectList().add(Aspect.SENSES, 4).add(Aspect.VOID, 4)
        );
        addCrucibleRecipe(
            ModItems.tallyLens, 1, 2, "TALLYBLOCK_LENS", new ItemStack(ModItems.tallyLens), new AspectList().add(Aspect.SENSES, 4).add(Aspect.MINE, 4)
        );
        addInfusionRecipe(
            ModBlocks.golemWorkbench,
            1,
            0,
            "GOLEMWORKBENCH",
            3,
            new AspectList().add(Aspect.CRAFT, 10).add(Aspect.MAN, 5).add(Aspect.ORDER, 5),
            new ItemStack(Blocks.crafting_table),
            whitelist,
            new ItemStack(Blocks.chest)
        );
        addArcaneCraftingRecipe(
            ModItems.golemWorkbenchUpgrade,
            1,
            0,
            "GOLEMWORKBENCH_UPGRADE",
            new AspectList().add(Aspect.AIR, 15),
            "FTN",
            "TGT",
            "RTQ",
            'F',
            Items.feather,
            'T',
            thaumiumIngot,
            'N',
            nitor,
            'G',
            golemUpgrades[0],
            'R',
            Blocks.redstone_block,
            'Q',
            Blocks.quartz_block
        );
        addArcaneCraftingRecipe(
            ModItems.golemWorkbenchUpgrade,
            1,
            1,
            "GOLEMWORKBENCH_UPGRADE",
            new AspectList().add(Aspect.FIRE, 15),
            "BTN",
            "TGT",
            "WTO",
            'B',
            Items.blaze_powder,
            'T',
            thaumiumIngot,
            'N',
            nitor,
            'G',
            golemUpgrades[1],
            'W',
            whitelist,
            'O',
            Blocks.obsidian
        );
        addArcaneCraftingRecipe(
            ModItems.golemWorkbenchUpgrade,
            1,
            3,
            "GOLEMWORKBENCH_UPGRADE",
            new AspectList().add(Aspect.EARTH, 15),
            "VTN",
            "TGT",
            "RTQ",
            'V',
            Blocks.vine,
            'T',
            thaumiumIngot,
            'N',
            nitor,
            'G',
            golemUpgrades[3],
            'R',
            Blocks.redstone_block,
            'Q',
            Blocks.quartz_block
        );
        ItemStack entropyEssence = ItemApi.getItem("itemWispEssence", 0);
        ((IEssentiaContainerItem)entropyEssence.getItem()).setAspects(entropyEssence, new AspectList().add(Aspect.ENTROPY, 2));
        addArcaneCraftingRecipe(
            ModItems.golemWorkbenchUpgrade,
            1,
            5,
            "GOLEMWORKBENCH_UPGRADE",
            new AspectList().add(Aspect.ENTROPY, 15),
            "eTN",
            "TGT",
            "BTD",
            'e',
            entropyEssence,
            'T',
            thaumiumIngot,
            'N',
            nitor,
            'G',
            golemUpgrades[5],
            'B',
            enchantedTome,
            'D',
            Items.diamond
        );
        addArcaneCraftingRecipe(
            ModBlocks.greedyChest,
            1,
            0,
            "GREEDYCHEST",
            new AspectList().add(Aspect.AIR, 7).add(Aspect.ORDER, 2).add(Aspect.ENTROPY, 2),
            "W_W",
            "WAW",
            "WWW",
            'W',
            greatwoodPlanks,
            '_',
            Blocks.trapdoor,
            'A',
            ModItems.avaricePearl
        );
        addCrucibleRecipe(ModItems.avaricePearl, 1, 0, "GREEDYCHEST", new ItemStack(Items.ender_pearl), new AspectList().add(Aspect.GREED, 5));
        addArcaneCraftingRecipe(
            ModItems.enchantedPaper,
            4,
            0,
            "ENCHANTEDPAPER",
            new AspectList().add(Aspect.AIR, 1).add(Aspect.EARTH, 1).add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.ORDER, 1).add(Aspect.ENTROPY, 1),
            "gPg",
            "PgP",
            "gPg",
            'P',
            Items.paper,
            'g',
            Items.gold_nugget
        );
        addShapelessArcaneCraftingRecipe(ModItems.enchantedPaper, 1, 1, "ENCHANTEDPAPER", new AspectList().add(Aspect.ORDER, 2), enchantedPaper, "dyeWhite");
        addShapelessArcaneCraftingRecipe(ModItems.enchantedPaper, 1, 2, "ENCHANTEDPAPER", new AspectList().add(Aspect.ENTROPY, 2), enchantedPaper, "dyeBlack");
        addAlternateFilterRecipe(ModItems.enchantedPaper, 1, 1, "ENCHANTEDPAPER", new AspectList().add(Aspect.ORDER, 2), blacklist, "dyeWhite");
        addAlternateFilterRecipe(ModItems.enchantedPaper, 1, 2, "ENCHANTEDPAPER", new AspectList().add(Aspect.ENTROPY, 2), whitelist, "dyeBlack");
        addOffBookFilterRecipe(ModItems.enchantedPaper, 1, 1, "ENCHANTEDPAPER", new AspectList().add(Aspect.ORDER, 1), whitelist, whitelist);
        addOffBookFilterRecipe(ModItems.enchantedPaper, 1, 2, "ENCHANTEDPAPER", new AspectList().add(Aspect.ORDER, 1), blacklist, blacklist);
        addOffBookFilterRecipe(ModItems.enchantedPaper, 1, 1, "ENCHANTEDPAPER", new AspectList().add(Aspect.ORDER, 1), whitelist, blacklist);
        addArcaneCraftingRecipe(
            ModItems.enchantedPaper,
            1,
            3,
            "ENCHANTEDPAPER",
            new AspectList().add(Aspect.AIR, 5).add(Aspect.EARTH, 5).add(Aspect.FIRE, 5).add(Aspect.WATER, 5).add(Aspect.ORDER, 5).add(Aspect.ENTROPY, 5),
            "LPP",
            "GPP",
            "LPP",
            'L',
            Items.leather,
            'P',
            ModItems.enchantedPaper,
            'G',
            Items.gold_ingot
        );
        addAlternateArcaneCraftingRecipe(
            ModItems.enchantedPaper,
            1,
            3,
            "ENCHANTEDPAPER",
            new AspectList().add(Aspect.AIR, 7).add(Aspect.EARTH, 7).add(Aspect.FIRE, 7).add(Aspect.WATER, 7).add(Aspect.ORDER, 7).add(Aspect.ENTROPY, 7),
            " B ",
            "GSG",
            " B ",
            'B',
            Items.book,
            'G',
            Items.gold_ingot,
            'S',
            "slimeball"
        );
        addArcaneCraftingRecipe(
            ModBlocks.thirstyTank,
            1,
            0,
            "THIRSTYTANK",
            new AspectList().add(Aspect.WATER, 16).add(Aspect.ORDER, 4).add(Aspect.ENTROPY, 4),
            "SGS",
            "GAG",
            "SWS",
            'S',
            arcaneStoneBlock,
            'G',
            "blockGlass",
            'A',
            ModItems.avaricePearl,
            'W',
            "shardWater"
        );
        addShapelessArcaneCraftingRecipe(
            ModItems.ingredientHorizontal, 2, 0, "AVARICIOUS", new AspectList().add(Aspect.ENTROPY, 2), ModItems.avaricePearl, slivers[5]
        );
        addArcaneCraftingRecipe(
            ModItems.avaricePearl,
            1,
            1,
            "AVARICIOUS",
            new AspectList().add(Aspect.ORDER, 5).add(Aspect.AIR, 5),
            "GDG",
            "ASO",
            " P ",
            'P',
            new ItemStack(ModItems.avaricePearl, 1, 0),
            'S',
            salisMundus,
            'A',
            "shardAir",
            'O',
            "shardOrder",
            'D',
            Items.diamond,
            'G',
            Items.glowstone_dust
        );
        addArcaneCraftingRecipe(
            ModItems.edible,
            1,
            0,
            "EAGERCHEST",
            new AspectList().add(Aspect.ORDER, 2).add(Aspect.ENTROPY, 2),
            "P",
            "Q",
            "B",
            'P',
            new ItemStack(ModItems.ingredientHorizontal, 1, 0),
            'Q',
            quicksilver,
            'B',
            Items.glass_bottle
        );
        addArcaneCraftingRecipe(
            ModBlocks.eagerChest,
            1,
            0,
            "EAGERCHEST",
            new AspectList().add(Aspect.ORDER, 5).add(Aspect.ENTROPY, 5),
            "w_w",
            "WAW",
            "WWW",
            'W',
            greatwoodPlanks,
            'w',
            "plankWood",
            '_',
            Blocks.trapdoor,
            'A',
            new ItemStack(ModItems.edible, 1, 0)
        );
        addArcaneCraftingRecipe(
            ModBlocks.tenaciousChest,
            1,
            0,
            "TENACIOUSCHEST",
            new AspectList().add(Aspect.AIR, 5).add(Aspect.EARTH, 2).add(Aspect.WATER, 2),
            "W_W",
            "WSW",
            "WWW",
            'W',
            "plankWood",
            '_',
            Blocks.trapdoor,
            'S',
            "slimeball"
        );
        addInfusionRecipe(
            ModItems.ringEnderDisjunction,
            1,
            0,
            "ENDERDISJUNCTION",
            4,
            new AspectList().add(Aspect.TRAP, 10).add(Aspect.ENTROPY, 15).add(Aspect.ELDRITCH, 2).add(Aspect.WEAPON, 4),
            ItemApi.getItem("itemBaubleBlanks", 1),
            new ItemStack(ModItems.avaricePearl),
            string,
            amber,
            new ItemStack(Blocks.tripwire_hook),
            string,
            new ItemStack(Blocks.web)
        );
        addArcaneCraftingRecipe(
            ModItems.focusCrafting,
            1,
            0,
            "FOCUSCRAFTING",
            new AspectList().add(Aspect.EARTH, 10).add(Aspect.ORDER, 30).add(Aspect.ENTROPY, 5),
            "OQE",
            "QTQ",
            "BQO",
            'Q',
            quartz,
            'O',
            "shardOrder",
            'E',
            "shardEarth",
            'B',
            balancedShard,
            'T',
            ItemApi.getBlock("blockTable", 15)
        );
        addCrucibleRecipe(ModBlocks.redcrystal, 1, 0, "REDCRYSTAL", new ItemStack(Items.redstone), new AspectList().add(Aspect.CRYSTAL, 1));
        addShapelessArcaneCraftingRecipe(
            ModBlocks.redcrystalAmp, 2, 0, "REDCRYSTAL_AMP", new AspectList().add(Aspect.FIRE, 2), redcrystal, slivers[1], redcrystal
        );
        addShapelessArcaneCraftingRecipe(
            ModBlocks.redcrystalDim, 2, 0, "REDCRYSTAL_DIM", new AspectList().add(Aspect.ENTROPY, 2), redcrystal, slivers[5], redcrystal
        );
        addShapelessArcaneCraftingRecipe(
            ModBlocks.redcrystalDense, 2, 0, "REDCRYSTAL_DENSE", new AspectList().add(Aspect.EARTH, 2), redcrystal, slivers[3], redcrystal
        );
        addArcaneCraftingRecipe(
            ModBlocks.redcrystalRes,
            1,
            0,
            "REDCRYSTAL_RES",
            new AspectList().add(Aspect.AIR, 8).add(Aspect.ORDER, 4),
            " R ",
            "QSQ",
            'R',
            redcrystal,
            'S',
            "shardAir",
            'Q',
            Items.quartz
        );
        addArcaneCraftingRecipe(
            ModBlocks.redcrystalMerc,
            1,
            0,
            "REDCRYSTAL_MIRRORBOUND",
            new AspectList().add(Aspect.FIRE, 5).add(Aspect.ORDER, 10).add(Aspect.ENTROPY, 5).add(Aspect.AIR, 10),
            " O ",
            "GRQ",
            "SSS",
            'O',
            "shardOrder",
            'G',
            mirroredGlass,
            'Q',
            quicksilver,
            'S',
            arcaneStoneBlock,
            'R',
            new ItemStack(ModBlocks.redcrystalRes)
        );
        ItemStack wand = ItemApi.getItem("itemWandCasting", 0);
        ((ItemWandCasting)wand.getItem()).setRod(wand, ConfigItems.WAND_ROD_GREATWOOD);
        ((ItemWandCasting)wand.getItem()).setCap(wand, ConfigItems.WAND_CAP_GOLD);
        ItemStack obsidian = new ItemStack(Blocks.obsidian);
        List hungryMawRecipe = Arrays.asList(
            new AspectList(),
            3,
            3,
            3,
            Arrays.asList(
                null,
                null,
                wand,
                null,
                null,
                new ItemStack(ConfigItems.itemFocusPortableHole),
                null,
                null,
                null,
                null,
                null,
                null,
                empty,
                obsidian,
                empty,
                null,
                empty,
                null,
                null,
                null,
                null,
                obsidian,
                ItemApi.getBlock("blockChestHungry", 0),
                obsidian,
                null,
                obsidian,
                null
            )
        );
        storeRecipe(ModBlocks.hungryMaw, 0, hungryMawRecipe);
        addArcaneCraftingRecipe(
            ModBlocks.finicalMaw,
            1,
            0,
            "FINICALMAW",
            new AspectList().add(Aspect.ORDER, 10).add(Aspect.AIR, 4).add(Aspect.ENTROPY, 4).add(Aspect.EARTH, 4),
            " Io",
            "IMI",
            "oI ",
            'M',
            new ItemStack(ModBlocks.hungryMaw),
            'I',
            Items.iron_ingot,
            'o',
            slivers[4]
        );
        addArcaneCraftingRecipe(
            ModItems.autoHandMirror,
            1,
            0,
            "AUTOHANDMIRROR",
            new AspectList().add(Aspect.ORDER, 54).add(Aspect.AIR, 25).add(Aspect.ENTROPY, 25),
            " F ",
            "tSt",
            " H ",
            'F',
            new ItemStack(ModBlocks.finicalMaw),
            'S',
            salisMundus,
            'H',
            ItemApi.getItem("itemHandMirror", 0),
            't',
            thaumiumNugget
        );
        addArcaneCraftingRecipe(
            ModBlocks.golemInhibitor,
            1,
            0,
            "GOLEMINHIBITOR",
            new AspectList().add(Aspect.ENTROPY, 15).add(Aspect.ORDER, 5).add(Aspect.AIR, 5).add(Aspect.FIRE, 5),
            "ABQ",
            "TIT",
            "IRI",
            'I',
            Items.iron_ingot,
            'T',
            thaumiumIngot,
            'B',
            new ItemStack(ModItems.crystalBrain, 1, 5),
            'R',
            new ItemStack(ModBlocks.redcrystalAmp),
            'A',
            "shardAir",
            'Q',
            quartz
        );
        addInfusionRecipe(
            ModBlocks.entitySignal,
            1,
            0,
            "ENTITYSIGNAL",
            1,
            new AspectList().add(Aspect.EARTH, 25).add(Aspect.SENSES, 15).add(Aspect.LIFE, 15),
            new ItemStack(ModItems.crystalBrain, 1, 3),
            new ItemStack(Items.golden_carrot),
            arcaneStoneBlock,
            redcrystal,
            arcaneStoneBlock,
            new ItemStack(Items.egg),
            arcaneStoneBlock,
            new ItemStack(Items.leather),
            arcaneStoneBlock
        );
        addInfusionRecipe(
            ModItems.golemLinker,
            1,
            0,
            "GOLEMLINKER",
            3,
            new AspectList().add(Aspect.ORDER, 16).add(Aspect.SOUL, 4).add(Aspect.MAN, 4).add(Aspect.TOOL, 8),
            ItemApi.getBlock("blockMagicalLog", 1),
            new ItemStack(Items.diamond),
            new ItemStack(Blocks.quartz_block),
            ItemApi.getItem("itemGolemUpgrade", 4)
        );
        addInfusionRecipe(
            ModBlocks.essentiaLocus,
            1,
            0,
            "ESSENTIALOCUS",
            4,
            new AspectList().add(Aspect.WATER, 32).add(Aspect.MAGIC, 24).add(Aspect.SENSES, 16).add(Aspect.MIND, 8).add(Aspect.ORDER, 8),
            new ItemStack(ModItems.crystalBrain, 1, 2),
            new ItemStack(Items.water_bucket),
            visFilter,
            ItemApi.getItem("itemGoggles", 0),
            ItemApi.getItem("itemResonator", 0)
        );
        wand = new ItemStack(Blocks.water);
        essentiaLocusConstructRecipe = Arrays.asList(
            new AspectList(),
            5,
            3,
            5,
            Arrays.asList(
                null,
                null,
                null,
                null,
                null,
                null,
                empty,
                empty,
                empty,
                null,
                null,
                empty,
                new ItemStack(ModBlocks.essentiaLocus, 1),
                empty,
                null,
                null,
                empty,
                empty,
                empty,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                empty,
                empty,
                empty,
                null,
                null,
                empty,
                ItemApi.getBlock("blockCrystal", 2),
                empty,
                null,
                null,
                empty,
                empty,
                empty,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                silverwoodPlanks,
                silverwoodPlanks,
                silverwoodPlanks,
                null,
                silverwoodPlanks,
                wand,
                wand,
                wand,
                silverwoodPlanks,
                silverwoodPlanks,
                wand,
                new ItemStack(Blocks.lapis_block),
                wand,
                silverwoodPlanks,
                silverwoodPlanks,
                wand,
                wand,
                wand,
                silverwoodPlanks,
                null,
                silverwoodPlanks,
                silverwoodPlanks,
                silverwoodPlanks,
                null
            )
        );
        addInfusionRecipe(
            ModBlocks.essentiaLocusAggregator,
            1,
            0,
            "ESSENTIALOCUSAGGREGATOR",
            4,
            new AspectList()
                .add(Aspect.WATER, 32)
                .add(Aspect.MAGIC, 24)
                .add(Aspect.SLIME, 16)
                .add(Aspect.TRAVEL, 8)
                .add(Aspect.EXCHANGE, 8)
                .add(Aspect.DARKNESS, 16),
            ItemApi.getBlock("blockMirror", 6),
            new ItemStack(Items.map),
            ItemApi.getBlock("blockJar", 3),
            balancedShard,
            new ItemStack(Blocks.lapis_block),
            quicksilver,
            balancedShard
        );
        addInfusionRecipe(
            ModBlocks.inventariumCore,
            1,
            0,
            "INVENTARIUM",
            4,
            new AspectList().add(Aspect.ORDER, 40).add(Aspect.MIND, 24).add(Aspect.SENSES, 8).add(Aspect.MECHANISM, 8),
            new ItemStack(ModItems.crystalBrain, 1, 4),
            enchantedPaper,
            new ItemStack(Items.item_frame),
            new ItemStack(Blocks.chest),
            new ItemStack(ModBlocks.remoteComparator)
        );
        wand = new ItemStack(Blocks.bookshelf);
        inventariumConstructRecipe = Arrays.asList(
            new AspectList(),
            5,
            3,
            5,
            Arrays.asList(
                null,
                null,
                null,
                null,
                null,
                null,
                empty,
                empty,
                empty,
                null,
                null,
                empty,
                new ItemStack(ModBlocks.inventariumCore, 1),
                empty,
                null,
                null,
                empty,
                empty,
                empty,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                wand,
                null,
                wand,
                null,
                wand,
                empty,
                empty,
                empty,
                wand,
                null,
                empty,
                ItemApi.getBlock("blockCrystal", 4),
                empty,
                null,
                wand,
                empty,
                empty,
                empty,
                wand,
                null,
                wand,
                null,
                wand,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                amberBlock,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        );
        addShapelessArcaneCraftingRecipe(
            ModBlocks.unseenScribe,
            1,
            0,
            "INVENTARIUM",
            new AspectList().add(Aspect.AIR, 15).add(Aspect.ORDER, 30).add(Aspect.EARTH, 5),
            ItemApi.getItem("itemInkwell", 0),
            enchantedTome,
            nitor,
            "shardOrder",
            ItemApi.getBlock("blockTable", 0),
            slivers[0]
        );
        addArcaneCraftingRecipe(
            ModBlocks.enchantedBookshelf,
            1,
            0,
            "ENCHANTEDPAPER",
            new AspectList().add(Aspect.AIR, 5).add(Aspect.EARTH, 5).add(Aspect.FIRE, 5).add(Aspect.WATER, 5).add(Aspect.ORDER, 5).add(Aspect.ENTROPY, 5),
            "WWW",
            "BBB",
            "WWW",
            'W',
            greatwoodPlanks,
            'B',
            enchantedTome
        );
        addArcaneCraftingRecipe(
            ModBlocks.enchantedBookshelf,
            1,
            1,
            "INVENTARIUM_EXTRA",
            new AspectList().add(Aspect.EARTH, 14).add(Aspect.FIRE, 14).add(Aspect.ORDER, 14),
            "WWW",
            "SBS",
            "WWW",
            'W',
            silverwoodPlanks,
            'S',
            new ItemStack(ModBlocks.enchantedBookshelf),
            'B',
            Items.enchanted_book
        );
        addArcaneCraftingRecipe(
            ModBlocks.unseenScribePointer,
            1,
            0,
            "INVENTARIUM_POINTER",
            new AspectList().add(Aspect.EARTH, 5).add(Aspect.AIR, 30).add(Aspect.ORDER, 5),
            "QQQ",
            "QEQ",
            "GOG",
            'W',
            silverwoodPlanks,
            'Q',
            Items.quartz,
            'O',
            clusterOrder,
            'G',
            Items.gold_ingot,
            'E',
            Items.ender_eye
        );
        addInfusionRecipe(
            ModBlocks.golemTaskmaster,
            1,
            0,
            "INVENTARIUM_DELIVERY",
            4,
            new AspectList().add(Aspect.ORDER, 30).add(Aspect.MIND, 20).add(Aspect.MAN, 20).add(Aspect.SOUL, 20).add(Aspect.GREED, 8).add(Aspect.MECHANISM, 8),
            new ItemStack(ModItems.crystalBrain, 1, 4),
            new ItemStack(Items.ender_eye),
            arcaneStoneBlock,
            amber,
            silverwoodPlanks,
            balancedShard,
            arcaneStoneBlock,
            whitelist,
            silverwoodPlanks
        );
        addArcaneCraftingRecipe(
            ModBlocks.metalGlow,
            1,
            0,
            "INVENTARIUM_DELIVERY",
            new AspectList().add(Aspect.WATER, 10).add(Aspect.FIRE, 10).add(Aspect.ORDER, 10),
            "IOI",
            "DBD",
            "IGI",
            'I',
            Items.iron_ingot,
            'O',
            "shardOrder",
            'D',
            Items.diamond,
            'A',
            amber,
            'B',
            zombieBrain,
            'G',
            new ItemStack(ModBlocks.translucent, 1, 32767)
        );
        addArcaneCraftingRecipe(
            ModBlocks.reqTome,
            1,
            0,
            "INVENTARIUM_DELIVERY",
            new AspectList().add(Aspect.EARTH, 10).add(Aspect.AIR, 10).add(Aspect.ORDER, 54),
            " C ",
            "WBA",
            "EPE",
            'C',
            ItemApi.getItem("itemResource", 15),
            'W',
            whitelist,
            'B',
            enchantedTome,
            'A',
            ModItems.avaricePearl,
            'E',
            Items.emerald,
            'P',
            ItemApi.getBlock("blockStoneDevice", 1)
        );
        addArcaneCraftingRecipe(
            ModBlocks.thaumostaticPylon,
            1,
            0,
            "THAUMOSTATICREFUELER",
            new AspectList().add(Aspect.AIR, 35).add(Aspect.ORDER, 20).add(Aspect.FIRE, 20),
            "QIQ",
            "ABA",
            "QIQ",
            'Q',
            Items.quartz,
            'I',
            Items.iron_ingot,
            'A',
            "shardAir",
            'B',
            ItemApi.getBlock("blockTube", 0)
        );
        addInfusionRecipe(
            ModBlocks.thaumostaticPylon,
            1,
            1,
            "THAUMOSTATICREFUELER",
            4,
            new AspectList()
                .add(Aspect.AIR, 50)
                .add(Aspect.MIND, 20)
                .add(Aspect.ENERGY, 20)
                .add(Aspect.FLIGHT, 16)
                .add(Aspect.TRAVEL, 8)
                .add(Aspect.EXCHANGE, 8),
            new ItemStack(ModItems.crystalBrain, 1, 0),
            new ItemStack(Items.compass),
            feather,
            alchemicalConstruct,
            visRelay,
            feather,
            new ItemStack(ModBlocks.thaumostaticPylon)
        );
        wand = new ItemStack(ModBlocks.thaumostaticPylon);
        thaumostaticSpireConstructRecipe = Arrays.asList(
            BlockThaumostaticPylon.activationVisCost,
            1,
            5,
            1,
            Arrays.asList(
                ItemApi.getBlock("blockCosmeticSolid", 4),
                wand,
                new ItemStack(ModBlocks.thaumostaticPylon, 1, 1),
                wand,
                new ItemStack(Blocks.quartz_block, 1, 1)
            )
        );
        addInfusionRecipe(
            ModBlocks.nethermind,
            1,
            0,
            "NETHERMIND",
            5,
            new AspectList().add(Aspect.FIRE, 64).add(Aspect.MIND, 32).add(Aspect.TRAVEL, 16).add(Aspect.DARKNESS, 16).add(Aspect.ELDRITCH, 16),
            new ItemStack(ModItems.crystalBrain, 1, 1),
            new ItemStack(ModItems.netherHeart, 1, 32767),
            new ItemStack(Items.lava_bucket),
            runedObsidian,
            new ItemStack(Blocks.soul_sand),
            new ItemStack(Blocks.netherrack),
            runedObsidian,
            new ItemStack(ModItems.avaricePearl),
            new ItemStack(Items.nether_wart),
            runedObsidian
        );
        nethermindConstructRecipe = Arrays.asList(
            BlockNethermind.activationVisCost,
            7,
            3,
            7,
            Arrays.asList(
                null,
                null,
                null,
                runedObsidian,
                null,
                null,
                null,
                null,
                null,
                null,
                runedObsidian,
                null,
                null,
                null,
                null,
                null,
                runedObsidian,
                runedObsidian,
                runedObsidian,
                null,
                null,
                runedObsidian,
                runedObsidian,
                runedObsidian,
                new ItemStack(ModBlocks.nethermind),
                runedObsidian,
                runedObsidian,
                runedObsidian,
                null,
                null,
                runedObsidian,
                runedObsidian,
                runedObsidian,
                null,
                null,
                null,
                null,
                null,
                runedObsidian,
                null,
                null,
                null,
                null,
                null,
                null,
                runedObsidian,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                runedObsidian,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                runedObsidian,
                null,
                null,
                null,
                null,
                null,
                runedObsidian,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                runedObsidian,
                null,
                null,
                null
            )
        );
        addShapelessArcaneCraftingRecipeLinkable(
            ModBlocks.runedObsidian,
            1,
            0,
            "NETHERMIND",
            new AspectList().add(Aspect.FIRE, 6).add(Aspect.EARTH, 3).add(Aspect.AIR, 1).add(Aspect.ENTROPY, 1),
            slivers[1],
            Blocks.obsidian,
            Items.quartz
        );
        addAlternateShapelessArcaneCraftingRecipe(
            ModBlocks.runedObsidian,
            1,
            0,
            "NETHERMIND",
            new AspectList().add(Aspect.FIRE, 6).add(Aspect.EARTH, 3).add(Aspect.AIR, 1).add(Aspect.ENTROPY, 1),
            slivers[1],
            ItemApi.getBlock("blockCosmeticSolid", 1),
            Items.quartz
        );
        addArcaneCraftingRecipe(
            ModBlocks.netherRune,
            1,
            0,
            "NETHERMIND",
            new AspectList().add(Aspect.FIRE, 3).add(Aspect.EARTH, 6),
            "BSB",
            "NRN",
            "BSB",
            'N',
            Blocks.netherrack,
            'R',
            runedObsidian,
            'S',
            arcaneStoneBlock,
            'B',
            Items.netherbrick
        );
        addArcaneCraftingRecipe(
            ModBlocks.netherRune,
            1,
            1,
            "NETHERMIND",
            new AspectList().add(Aspect.FIRE, 30).add(Aspect.EARTH, 10),
            "BSM",
            "LRL",
            "MSB",
            'L',
            Items.lava_bucket,
            'R',
            runedObsidian,
            'S',
            arcaneStoneBlock,
            'B',
            Items.blaze_rod,
            'M',
            Items.magma_cream
        );
        addArcaneCraftingRecipe(
            ModBlocks.netherRune,
            1,
            2,
            "NETHERRUNE_CRYSTAL",
            new AspectList().add(Aspect.FIRE, 30).add(Aspect.EARTH, 10).add(Aspect.ORDER, 20),
            "FSD",
            "QRQ",
            "DSO",
            'D',
            Items.diamond,
            'O',
            "shardOrder",
            'F',
            "shardFire",
            'R',
            runedObsidian,
            'S',
            arcaneStoneBlock,
            'Q',
            Blocks.quartz_block
        );
        addArcaneCraftingRecipe(
            ModBlocks.netherRune,
            1,
            3,
            "NETHERRUNE_LIGHT",
            new AspectList().add(Aspect.FIRE, 30).add(Aspect.AIR, 30),
            "ASg",
            "GRG",
            "gSF",
            'g',
            Items.glowstone_dust,
            'A',
            "shardAir",
            'F',
            "shardFire",
            'R',
            runedObsidian,
            'S',
            arcaneStoneBlock,
            'G',
            Blocks.glowstone
        );
        addArcaneCraftingRecipe(
            ModBlocks.netherRune,
            1,
            4,
            "NETHERRUNE_SOUL",
            new AspectList().add(Aspect.FIRE, 10).add(Aspect.EARTH, 20).add(Aspect.ENTROPY, 10),
            "ASG",
            "BRB",
            "GSA",
            'G',
            Items.ghast_tear,
            'A',
            amberBlock,
            'R',
            runedObsidian,
            'S',
            arcaneStoneBlock,
            'B',
            Blocks.soul_sand
        );
        addArcaneCraftingRecipe(
            ModBlocks.netherRune,
            1,
            6,
            "NETHERRUNE_CELERITY",
            new AspectList().add(Aspect.FIRE, 30).add(Aspect.EARTH, 25).add(Aspect.AIR, 30),
            "ESA",
            "PRP",
            "ASE",
            'E',
            Items.emerald,
            'A',
            new ItemStack(ModItems.avaricePearl),
            'R',
            runedObsidian,
            'S',
            arcaneStoneBlock,
            'P',
            new ItemStack(Items.potionitem, 1, 8194)
        );
        addInfusionRecipe(
            ModBlocks.mobLure,
            1,
            0,
            "MOBLURE",
            3,
            new AspectList().add(Aspect.FIRE, 16).add(Aspect.MIND, 10).add(Aspect.HUNGER, 10).add(Aspect.DARKNESS, 8),
            new ItemStack(ModItems.crystalBrain, 1, 1),
            new ItemStack(Items.fermented_spider_eye),
            new ItemStack(Items.carrot_on_a_stick),
            runedObsidian,
            ItemApi.getItem("itemShard", 5),
            ItemApi.getItem("itemShard", 1),
            ItemApi.getBlock("blockCosmeticSolid", 0)
        );
        if (AutomagyConfig.dimensionLureAllowed) {
            wand = new ItemStack(ModBlocks.dimensionLure);
            mobSpawnerConstructRecipe = Arrays.asList(
                new AspectList(),
                7,
                1,
                7,
                Arrays.asList(
                    null,
                    null,
                    runedObsidian,
                    runedObsidian,
                    runedObsidian,
                    null,
                    null,
                    null,
                    runedObsidian,
                    null,
                    null,
                    null,
                    runedObsidian,
                    null,
                    runedObsidian,
                    null,
                    null,
                    null,
                    null,
                    null,
                    runedObsidian,
                    runedObsidian,
                    null,
                    null,
                    wand,
                    null,
                    null,
                    runedObsidian,
                    runedObsidian,
                    null,
                    null,
                    null,
                    null,
                    null,
                    runedObsidian,
                    null,
                    runedObsidian,
                    null,
                    null,
                    null,
                    runedObsidian,
                    null,
                    null,
                    null,
                    runedObsidian,
                    runedObsidian,
                    runedObsidian,
                    null,
                    null
                )
            );
            AspectList aspects = new AspectList()
                .add(Aspect.FIRE, 64)
                .add(Aspect.HUNGER, 32)
                .add(Aspect.GREED, 8)
                .add(Aspect.TRAP, 16)
                .add(Aspect.TRAVEL, 16)
                .add(Aspect.SENSES, 8);
            ItemStack mobLure = new ItemStack(ModBlocks.mobLure);
            ItemStack[] componentsBase = new ItemStack[]{
                new ItemStack(Items.golden_apple),
                new ItemStack(ModItems.crystalBrain, 1, 3),
                new ItemStack(Items.ender_pearl),
                new ItemStack(Blocks.sticky_piston),
                new ItemStack(Blocks.web),
                new ItemStack(Blocks.trapdoor)
            };
            int minEnchantLevel = 1;
            ItemStack rodWithLure = new ItemStack(Items.fishing_rod);
            rodWithLure.addEnchantment(enchantmentLure, minEnchantLevel);
            ItemStack bookWithLure = Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(enchantmentLure, minEnchantLevel));
            ItemStack[] components1 = new ItemStack[componentsBase.length + 1];
            ItemStack[] components2 = new ItemStack[componentsBase.length + 1];
            components1[0] = rodWithLure;
            components2[0] = bookWithLure;

            for (int ix = 0; ix < componentsBase.length; ix++) {
                components1[ix + 1] = componentsBase[ix];
                components2[ix + 1] = componentsBase[ix];
            }

            addInfusionRecipe(ModBlocks.dimensionLure, 1, 0, "DIMENSIONLURE", 6, aspects, mobLure, components1);
            addAlternateInfusionRecipe(ModBlocks.dimensionLure, 1, 0, "DIMENSIONLURE", 6, aspects, mobLure, components2);
            addArcaneCraftingRecipe(
                ModBlocks.netherRuneCreature,
                1,
                0,
                "NETHERRUNE_ZOMBIE",
                new AspectList().add(Aspect.FIRE, 20).add(Aspect.EARTH, 15).add(Aspect.ENTROPY, 15),
                "GST",
                "BRB",
                "TSG",
                'B',
                zombieBrain,
                'G',
                Items.golden_sword,
                'T',
                tallowBlock,
                'R',
                runedObsidian,
                'S',
                arcaneStoneBlock
            );
            addArcaneCraftingRecipe(
                ModBlocks.netherRuneCreature,
                1,
                1,
                "DIMENSIONLURE",
                new AspectList().add(Aspect.FIRE, 25).add(Aspect.AIR, 10),
                "PSF",
                "BRB",
                "FSP",
                'B',
                Items.blaze_rod,
                'P',
                Items.blaze_powder,
                'F',
                slivers[1],
                'R',
                runedObsidian,
                'S',
                arcaneStoneBlock
            );
            addArcaneCraftingRecipe(
                ModBlocks.netherRuneCreature,
                1,
                2,
                "NETHERRUNE_GHAST",
                new AspectList().add(Aspect.FIRE, 20).add(Aspect.AIR, 15).add(Aspect.ENTROPY, 15),
                "FSs",
                "TRT",
                "sSF",
                'T',
                Items.ghast_tear,
                'F',
                Items.fire_charge,
                's',
                Blocks.soul_sand,
                'R',
                runedObsidian,
                'S',
                arcaneStoneBlock
            );
            addArcaneCraftingRecipe(
                ModBlocks.netherRuneCreature,
                1,
                3,
                "NETHERRUNE_SKELETON",
                new AspectList().add(Aspect.EARTH, 15).add(Aspect.ENTROPY, 20),
                "sSB",
                "HRH",
                "BSs",
                'H',
                new ItemStack(Items.skull, 1, 1),
                's',
                Items.stone_sword,
                'B',
                Blocks.nether_brick,
                'R',
                runedObsidian,
                'S',
                arcaneStoneBlock
            );
            addArcaneCraftingRecipe(
                ModBlocks.netherRuneCreature,
                1,
                4,
                "NETHERRUNE_SLIME",
                new AspectList().add(Aspect.FIRE, 20).add(Aspect.EARTH, 10).add(Aspect.WATER, 10),
                "sSP",
                "MRM",
                "PSs",
                'M',
                Items.magma_cream,
                's',
                Items.slime_ball,
                'P',
                Items.blaze_powder,
                'R',
                runedObsidian,
                'S',
                arcaneStoneBlock
            );
            addArcaneCraftingRecipe(
                ModBlocks.netherRuneCreature,
                1,
                5,
                "NETHERRUNE_BAT",
                new AspectList().add(Aspect.FIRE, 20).add(Aspect.AIR, 15).add(Aspect.ENTROPY, 15),
                "FST",
                "GRG",
                "TSF",
                'G',
                Items.gunpowder,
                'F',
                slivers[1],
                'T',
                Blocks.tnt,
                'R',
                runedObsidian,
                'S',
                arcaneStoneBlock
            );
            ItemStack fireEssence = ItemApi.getItem("itemWispEssence", 0);
            ((IEssentiaContainerItem)fireEssence.getItem()).setAspects(fireEssence, new AspectList().add(Aspect.FIRE, 2));
            addArcaneCraftingRecipe(
                ModBlocks.netherRuneCreature,
                1,
                6,
                "NETHERRUNE_WISP",
                new AspectList().add(Aspect.FIRE, 40).add(Aspect.AIR, 20).add(Aspect.ENTROPY, 10),
                "DSs",
                "ERE",
                "sSD",
                'E',
                fireEssence,
                'D',
                Items.diamond,
                's',
                salisMundus,
                'R',
                runedObsidian,
                'S',
                arcaneStoneBlock
            );
        }

        addArcaneCraftingRecipe(
            ModBlocks.amnesiacBlock,
            1,
            0,
            "AMNESIASTONE",
            new AspectList().add(Aspect.ENTROPY, 20).add(Aspect.EARTH, 4).add(Aspect.WATER, 4),
            "SES",
            "SBS",
            'S',
            arcaneStoneBricks,
            'E',
            "shardEntropy",
            'B',
            new ItemStack(ModItems.crystalBrain, 1, 5)
        );
        addArcaneCraftingRecipe(
            ModBlocks.xpStone,
            1,
            0,
            "XPSTONE",
            new AspectList().add(Aspect.ENTROPY, 60).add(Aspect.EARTH, 30).add(Aspect.ORDER, 10),
            "ESE",
            "ABA",
            "ESE",
            'A',
            arcaneStoneBlock,
            'S',
            Blocks.soul_sand,
            'B',
            zombieBrain,
            'E',
            "shardEntropy"
        );
        addShapelessArcaneCraftingRecipe(
            ModItems.tankGlyph,
            2,
            0,
            "THIRSTYTANK_GLYPH",
            new AspectList().add(Aspect.WATER, 1),
            slivers[2],
            slivers[2],
            "slimeball",
            "dyeBlack",
            Items.clay_ball
        );
        addArcaneCraftingRecipe(
            ModItems.tankGlyph,
            1,
            1,
            "THIRSTYTANK_GLYPH",
            new AspectList().add(Aspect.WATER, 3).add(Aspect.ENTROPY, 10),
            "Oi ",
            "B i",
            "si ",
            'i',
            aquaInk,
            's',
            arcaneSlab,
            'B',
            Items.blaze_powder,
            'O',
            Blocks.obsidian
        );
        addArcaneCraftingRecipe(
            ModItems.tankGlyph,
            1,
            2,
            "THIRSTYTANK_GLYPH",
            new AspectList().add(Aspect.WATER, 3).add(Aspect.ENTROPY, 15),
            " i ",
            "iEi",
            "siB",
            'i',
            aquaInk,
            's',
            arcaneSlab,
            'E',
            "shardEntropy",
            'B',
            Items.blaze_powder
        );
        addArcaneCraftingRecipe(
            ModItems.tankGlyph,
            1,
            3,
            "THIRSTYTANK_GLYPH",
            new AspectList().add(Aspect.WATER, 3).add(Aspect.ORDER, 2).add(Aspect.ENTROPY, 2),
            " ii",
            "iH ",
            "si ",
            'i',
            aquaInk,
            's',
            arcaneSlab,
            'H',
            Blocks.hopper
        );
        addArcaneCraftingRecipe(
            ModItems.tankGlyph,
            1,
            4,
            "THIRSTYTANK_GLYPH",
            new AspectList().add(Aspect.WATER, 3).add(Aspect.FIRE, 5),
            " i ",
            "iAi",
            "sQ ",
            'i',
            aquaInk,
            's',
            arcaneSlab,
            'A',
            ModItems.avaricePearl,
            'Q',
            quicksilver
        );
        addArcaneCraftingRecipe(
            ModItems.tankGlyph,
            1,
            5,
            "THIRSTYTANK_GLYPH",
            new AspectList().add(Aspect.WATER, 3).add(Aspect.ORDER, 10),
            "ir ",
            "rir",
            "sri",
            'i',
            aquaInk,
            's',
            arcaneSlab,
            'r',
            Items.redstone
        );
        addArcaneCraftingRecipe(
            ModItems.tankGlyph,
            1,
            6,
            "THIRSTYTANK_GLYPH",
            new AspectList().add(Aspect.WATER, 3).add(Aspect.ORDER, 5),
            "iii",
            " i ",
            "sSO",
            'i',
            aquaInk,
            's',
            arcaneSlab,
            'S',
            Items.slime_ball,
            'O',
            "shardOrder"
        );
        addArcaneCraftingRecipe(
            ModItems.tankGlyph,
            1,
            7,
            "THIRSTYTANK_GLYPH",
            new AspectList().add(Aspect.WATER, 3).add(Aspect.FIRE, 5).add(Aspect.ENTROPY, 5),
            " ii",
            "iSP",
            "siM",
            'i',
            aquaInk,
            's',
            arcaneSlab,
            'S',
            new ItemStack(ModItems.ingredientHorizontal, 1, 0),
            'M',
            Items.speckled_melon,
            'P',
            new ItemStack(Items.potionitem, 1, 8226)
        );
        addArcaneCraftingRecipe(
            ModItems.tankGlyph,
            1,
            8,
            "THIRSTYTANK_GLYPH",
            new AspectList().add(Aspect.WATER, 16).add(Aspect.AIR, 3).add(Aspect.ENTROPY, 3),
            "BPB",
            "iWi",
            "si ",
            'i',
            aquaInk,
            's',
            arcaneSlab,
            'B',
            Items.bucket,
            'P',
            Items.ender_pearl,
            'W',
            "shardWater"
        );
        wand = ModItems.crystalEntityEye.createEyeAttunedTo(new EntityCow(null));
        obsidian = ModItems.crystalEntityEye.createEyeAttunedTo(new EntityMooshroom(null));
        addArcaneCraftingRecipe(
            ModItems.tankGlyph,
            1,
            9,
            "THIRSTYTANK_GLYPH",
            new AspectList().add(Aspect.WATER, 3).add(Aspect.EARTH, 15),
            "i i",
            "LiL",
            "sME",
            'i',
            aquaInk,
            's',
            arcaneSlab,
            'L',
            Items.leather,
            'M',
            Items.milk_bucket,
            'E',
            wand
        );
        addAlternateArcaneCraftingRecipe(
            ModItems.tankGlyph,
            1,
            9,
            "THIRSTYTANK_GLYPH",
            new AspectList().add(Aspect.WATER, 3).add(Aspect.EARTH, 15),
            "i i",
            "LiL",
            "sSE",
            'i',
            aquaInk,
            's',
            arcaneSlab,
            'L',
            Items.leather,
            'S',
            Items.mushroom_stew,
            'E',
            obsidian
        );
        addArcaneCraftingRecipe(
            ModBlocks.boiler,
            1,
            0,
            "ALCHEMYBOILER",
            new AspectList().add(Aspect.WATER, 7).add(Aspect.FIRE, 5),
            "ICI",
            "IFI",
            "III",
            'I',
            Items.iron_ingot,
            'C',
            ItemApi.getBlock("blockMetalDevice", 0),
            'F',
            Blocks.furnace
        );
        ShapelessOreRecipe recipex = new ShapelessOreRecipe(new ItemStack(Items.mushroom_stew, 1, 0), ModItems.bucketMushroom, Items.bowl);
        GameRegistry.addRecipe(recipex);
        recipex = new ShapelessOreRecipe(new ItemStack(ModItems.food, 1, 0), ModItems.bucketVishroom, Items.bowl);
        GameRegistry.addRecipe(recipex);
        ItemStack wGlass = ItemApi.getBlock("blockCosmeticOpaque", 2);
        ItemStack wLog = ItemApi.getBlock("blockMagicalLog", 5);
        ItemStack node = ItemApi.getBlock("blockAiry", 5);
        advNodeJarRecipe = Arrays.asList(
            visCostAdvNodeJar,
            3,
            4,
            3,
            Arrays.asList(
                wLog,
                wLog,
                wLog,
                wLog,
                wLog,
                wLog,
                wLog,
                wLog,
                wLog,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                node,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass,
                wGlass
            )
        );
        addInfusionRecipe(
            ModItems.ingredient,
            2,
            0,
            "MIRRORINPUT",
            8,
            new AspectList().add(Aspect.TRAVEL, 8).add(Aspect.DARKNESS, 8).add(Aspect.EXCHANGE, 8),
            new ItemStack(Items.compass),
            quicksilver,
            new ItemStack(Items.ender_pearl),
            voidmetalIngot
        );
        AspectList duplicateConfluxDiscAspects = new AspectList()
            .add(Aspect.TRAVEL, 2)
            .add(Aspect.DARKNESS, 2)
            .add(Aspect.EXCHANGE, 2)
            .add(Aspect.ELDRITCH, 2)
            .add(Aspect.METAL, 8);
        duplicateConfluxDisc = ThaumcraftApi.addCrucibleRecipe(
            "MIRRORINPUT", new ItemStack(ModItems.ingredient, 2, 0), new ItemStack(ModItems.ingredient, 1, 0), duplicateConfluxDiscAspects
        );
        addArcaneCraftingRecipe(
            ModBlocks.mirrorAlt,
            1,
            0,
            "MIRRORINPUT",
            new AspectList().add(Aspect.AIR, 8).add(Aspect.ENTROPY, 8).add(Aspect.EARTH, 4),
            " g ",
            "gGg",
            " C ",
            'C',
            new ItemStack(ModItems.ingredient),
            'g',
            Items.gold_nugget,
            'G',
            mirroredGlass
        );
        addInfusionRecipe(
            ModBlocks.mirrorMultiDest,
            1,
            0,
            "MIRRORMULTIDEST",
            5,
            new AspectList().add(Aspect.MIND, 32).add(Aspect.GREED, 8).add(Aspect.ORDER, 16),
            new ItemStack(ModBlocks.mirrorAlt),
            new ItemStack(Blocks.iron_bars),
            enchantedTome,
            new ItemStack(Items.map)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.hourglass),
            new AspectList().add(Aspect.MECHANISM, 6).add(Aspect.ENERGY, 4).add(Aspect.TREE, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.hourglassMagic),
            new AspectList()
                .add(Aspect.MECHANISM, 7)
                .add(Aspect.ENERGY, 4)
                .add(Aspect.TREE, 1)
                .add(Aspect.EARTH, 1)
                .add(Aspect.ENTROPY, 1)
                .add(Aspect.ORDER, 2)
                .add(Aspect.MOTION, 2)
                .add(Aspect.MAGIC, 1)
        );
        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.crystalEye), new AspectList().add(Aspect.SENSES, 6).add(Aspect.CRYSTAL, 2).add(Aspect.MAGIC, 1));
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.remoteComparator),
            new AspectList()
                .add(Aspect.SENSES, 2)
                .add(Aspect.VOID, 2)
                .add(Aspect.MAGIC, 1)
                .add(Aspect.ENERGY, 4)
                .add(Aspect.MECHANISM, 3)
                .add(Aspect.CRYSTAL, 4)
                .add(Aspect.ORDER, 2)
        );
        ThaumcraftApi.registerObjectTag(enchantedPaper, new AspectList().add(Aspect.MIND, 1).add(Aspect.ORDER, 1));
        ThaumcraftApi.registerObjectTag(whitelist, new AspectList().add(Aspect.MIND, 1).add(Aspect.ORDER, 3).add(Aspect.SENSES, 1));
        ThaumcraftApi.registerObjectTag(blacklist, new AspectList().add(Aspect.MIND, 1).add(Aspect.ORDER, 1).add(Aspect.SENSES, 1).add(Aspect.ENTROPY, 2));
        ThaumcraftApi.registerObjectTag(redcrystal, new AspectList().add(Aspect.ENERGY, 2).add(Aspect.MECHANISM, 1).add(Aspect.CRYSTAL, 1));
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.redcrystalAmp), new AspectList().add(Aspect.ENERGY, 2).add(Aspect.MECHANISM, 1).add(Aspect.CRYSTAL, 1).add(Aspect.FIRE, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.redcrystalDim),
            new AspectList().add(Aspect.ENERGY, 2).add(Aspect.MECHANISM, 1).add(Aspect.CRYSTAL, 1).add(Aspect.ENTROPY, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.redcrystalDense),
            new AspectList().add(Aspect.ENERGY, 2).add(Aspect.MECHANISM, 1).add(Aspect.CRYSTAL, 1).add(Aspect.EARTH, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.redcrystalMerc),
            new AspectList()
                .add(Aspect.ENERGY, 5)
                .add(Aspect.MECHANISM, 1)
                .add(Aspect.CRYSTAL, 5)
                .add(Aspect.AIR, 5)
                .add(Aspect.ORDER, 2)
                .add(Aspect.MAGIC, 1)
                .add(Aspect.TRAVEL, 3)
                .add(Aspect.EXCHANGE, 4)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.visReader),
            new AspectList()
                .add(Aspect.ENERGY, 1)
                .add(Aspect.MECHANISM, 1)
                .add(Aspect.CRYSTAL, 2)
                .add(Aspect.AIR, 1)
                .add(Aspect.EARTH, 3)
                .add(Aspect.MAGIC, 2)
                .add(Aspect.SENSES, 2)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.hungryMaw),
            new AspectList().add(Aspect.EARTH, 2).add(Aspect.FIRE, 2).add(Aspect.DARKNESS, 1).add(Aspect.EXCHANGE, 1).add(Aspect.HUNGER, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.greedyChest),
            new AspectList()
                .add(Aspect.TREE, 9)
                .add(Aspect.GREED, 5)
                .add(Aspect.ELDRITCH, 4)
                .add(Aspect.EXCHANGE, 4)
                .add(Aspect.TRAVEL, 3)
                .add(Aspect.MAGIC, 2)
                .add(Aspect.AIR, 2)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.eagerChest),
            new AspectList().add(Aspect.TREE, 9).add(Aspect.GREED, 3).add(Aspect.ELDRITCH, 1).add(Aspect.EXCHANGE, 5).add(Aspect.MAGIC, 1).add(Aspect.ORDER, 3)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.ingredientHorizontal, 1, 0),
            new AspectList().add(Aspect.GREED, 2).add(Aspect.ELDRITCH, 2).add(Aspect.TRAVEL, 2).add(Aspect.MAGIC, 1).add(Aspect.ENTROPY, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.avaricePearl),
            new AspectList().add(Aspect.GREED, 5).add(Aspect.ELDRITCH, 4).add(Aspect.TRAVEL, 4).add(Aspect.MAGIC, 1).add(Aspect.MAGIC, 2)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.avaricePearl, 1, 1),
            new AspectList()
                .add(Aspect.GREED, 13)
                .add(Aspect.ELDRITCH, 4)
                .add(Aspect.TRAVEL, 4)
                .add(Aspect.MAGIC, 5)
                .add(Aspect.CRYSTAL, 4)
                .add(Aspect.AIR, 5)
                .add(Aspect.ORDER, 3)
                .add(Aspect.AURA, 3)
                .add(Aspect.EXCHANGE, 2)
                .add(Aspect.SENSES, 2)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.ringEnderDisjunction, 0, 0),
            new AspectList()
                .add(Aspect.TRAP, 10)
                .add(Aspect.ENTROPY, 10)
                .add(Aspect.ELDRITCH, 6)
                .add(Aspect.GREED, 5)
                .add(Aspect.WEAPON, 4)
                .add(Aspect.TRAVEL, 4)
                .add(Aspect.CRYSTAL, 2)
                .add(Aspect.MAGIC, 2)
                .add(Aspect.SENSES, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.ringEnderDisjunction, 0, 1),
            new AspectList()
                .add(Aspect.TRAP, 5)
                .add(Aspect.ENTROPY, 5)
                .add(Aspect.ELDRITCH, 3)
                .add(Aspect.GREED, 2)
                .add(Aspect.WEAPON, 2)
                .add(Aspect.TRAVEL, 2)
                .add(Aspect.CRYSTAL, 1)
                .add(Aspect.MAGIC, 1)
                .add(Aspect.SENSES, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.focusCrafting, 1, 0),
            new AspectList().add(Aspect.CRAFT, 5).add(Aspect.MAGIC, 4).add(Aspect.EXCHANGE, 2).add(Aspect.VOID, 1).add(Aspect.CRYSTAL, 2).add(Aspect.TOOL, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.golemInhibitor),
            new AspectList()
                .add(Aspect.AIR, 4)
                .add(Aspect.MIND, 4)
                .add(Aspect.FIRE, 2)
                .add(Aspect.METAL, 10)
                .add(Aspect.ENTROPY, 8)
                .add(Aspect.ENERGY, 3)
                .add(Aspect.MAGIC, 6)
                .add(Aspect.CRYSTAL, 10)
                .add(Aspect.TRAP, 4)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.entitySignal),
            new AspectList()
                .add(Aspect.EARTH, 10)
                .add(Aspect.SENSES, 10)
                .add(Aspect.LIFE, 10)
                .add(Aspect.CRYSTAL, 9)
                .add(Aspect.METAL, 8)
                .add(Aspect.MIND, 4)
                .add(Aspect.BEAST, 2)
                .add(Aspect.ENERGY, 2)
                .add(Aspect.MECHANISM, 2)
                .add(Aspect.SOUL, 2)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.inventariumCore),
            new AspectList()
                .add(Aspect.MIND, 10)
                .add(Aspect.MECHANISM, 10)
                .add(Aspect.ORDER, 10)
                .add(Aspect.ENERGY, 4)
                .add(Aspect.MAGIC, 4)
                .add(Aspect.SENSES, 10)
                .add(Aspect.VOID, 6)
                .add(Aspect.CRYSTAL, 10)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.thaumostaticPylon),
            new AspectList()
                .add(Aspect.AIR, 10)
                .add(Aspect.METAL, 8)
                .add(Aspect.CRYSTAL, 6)
                .add(Aspect.ENERGY, 10)
                .add(Aspect.MAGIC, 2)
                .add(Aspect.EXCHANGE, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.runedObsidian),
            new AspectList().add(Aspect.FIRE, 4).add(Aspect.EARTH, 3).add(Aspect.ENERGY, 1).add(Aspect.DARKNESS, 1).add(Aspect.VOID, 1).add(Aspect.EXCHANGE, 1)
        );
        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.netherHeart), new AspectList().add(Aspect.FIRE, 6).add(Aspect.MAGIC, 6).add(Aspect.BEAST, 5));
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.nethermind),
            new AspectList()
                .add(Aspect.FIRE, 10)
                .add(Aspect.MIND, 10)
                .add(Aspect.TRAVEL, 10)
                .add(Aspect.ELDRITCH, 9)
                .add(Aspect.MAGIC, 9)
                .add(Aspect.DARKNESS, 8)
                .add(Aspect.GREED, 5)
                .add(Aspect.EARTH, 4)
                .add(Aspect.EXCHANGE, 3)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.mobLure),
            new AspectList()
                .add(Aspect.FIRE, 10)
                .add(Aspect.MIND, 10)
                .add(Aspect.HUNGER, 10)
                .add(Aspect.DARKNESS, 8)
                .add(Aspect.CRYSTAL, 9)
                .add(Aspect.GREED, 3)
                .add(Aspect.SENSES, 3)
                .add(Aspect.ELDRITCH, 2)
                .add(Aspect.TRAP, 2)
                .add(Aspect.ENTROPY, 2)
                .add(Aspect.MAGIC, 2)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.essentiaLocusAggregator),
            new AspectList()
                .add(Aspect.TRAVEL, 10)
                .add(Aspect.EXCHANGE, 10)
                .add(Aspect.WATER, 8)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.SENSES, 8)
                .add(Aspect.DARKNESS, 5)
                .add(Aspect.SLIME, 5)
                .add(Aspect.ELDRITCH, 4)
        );
        ThaumcraftApi.registerObjectTag(new ItemStack(ModBlocks.amnesiacBlock), new AspectList());
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.xpStone),
            new AspectList().add(Aspect.ENTROPY, 13).add(Aspect.EARTH, 8).add(Aspect.MAGIC, 4).add(Aspect.MIND, 4).add(Aspect.UNDEAD, 2).add(Aspect.SOUL, 2)
        );
        ThaumcraftApi.registerObjectTag(
            enchantedTome,
            new AspectList()
                .add(Aspect.MIND, 8)
                .add(Aspect.GREED, 2)
                .add(Aspect.AIR, 1)
                .add(Aspect.EARTH, 1)
                .add(Aspect.FIRE, 1)
                .add(Aspect.WATER, 1)
                .add(Aspect.ORDER, 1)
                .add(Aspect.ENTROPY, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.translucent, 1, 0),
            new AspectList().add(Aspect.TRAP, 5).add(Aspect.CRYSTAL, 5).add(Aspect.LIGHT, 5).add(Aspect.ENERGY, 3).add(Aspect.FIRE, 3).add(Aspect.SENSES, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.translucent, 1, 1),
            new AspectList().add(Aspect.TRAP, 5).add(Aspect.CRYSTAL, 5).add(Aspect.LIGHT, 5).add(Aspect.ENERGY, 3).add(Aspect.FIRE, 3).add(Aspect.SENSES, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.enchantedBookshelf, 1, 0),
            new AspectList()
                .add(Aspect.MIND, 10)
                .add(Aspect.TREE, 3)
                .add(Aspect.GREED, 4)
                .add(Aspect.AIR, 3)
                .add(Aspect.EARTH, 3)
                .add(Aspect.FIRE, 3)
                .add(Aspect.WATER, 3)
                .add(Aspect.ORDER, 3)
                .add(Aspect.ENTROPY, 3)
                .add(Aspect.MAGIC, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.enchantedBookshelf, 1, 1),
            new AspectList()
                .add(Aspect.MIND, 10)
                .add(Aspect.TREE, 3)
                .add(Aspect.GREED, 5)
                .add(Aspect.AIR, 6)
                .add(Aspect.EARTH, 6)
                .add(Aspect.FIRE, 6)
                .add(Aspect.WATER, 6)
                .add(Aspect.ORDER, 6)
                .add(Aspect.ENTROPY, 6)
                .add(Aspect.MAGIC, 5)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.unseenScribePointer, 1),
            new AspectList()
                .add(Aspect.ORDER, 4)
                .add(Aspect.CRYSTAL, 5)
                .add(Aspect.VOID, 2)
                .add(Aspect.METAL, 3)
                .add(Aspect.GREED, 2)
                .add(Aspect.MAGIC, 3)
                .add(Aspect.SENSES, 5)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.tallyLens, 1, 0), new AspectList().add(Aspect.AIR, 1).add(Aspect.EARTH, 1).add(Aspect.CRYSTAL, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.food, 1, 0),
            new AspectList().add(Aspect.HUNGER, 4).add(Aspect.PLANT, 1).add(Aspect.DARKNESS, 1).add(Aspect.MAGIC, 2).add(Aspect.POISON, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.milk, 1, 32767), new AspectList().add(Aspect.WATER, 2).add(Aspect.HUNGER, 2).add(Aspect.HEAL, 2)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.mushroomFluid, 1, 32767),
            new AspectList().add(Aspect.HUNGER, 3).add(Aspect.PLANT, 1).add(Aspect.DARKNESS, 1).add(Aspect.WATER, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModBlocks.vishroomFluid, 1, 32767),
            new AspectList().add(Aspect.HUNGER, 3).add(Aspect.PLANT, 1).add(Aspect.DARKNESS, 1).add(Aspect.MAGIC, 2).add(Aspect.POISON, 1).add(Aspect.WATER, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.bucketMushroom, 1, 0),
            new AspectList().add(Aspect.HUNGER, 3).add(Aspect.PLANT, 1).add(Aspect.DARKNESS, 1).add(Aspect.WATER, 1).add(Aspect.METAL, 8).add(Aspect.VOID, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.bucketVishroom, 1, 0),
            new AspectList()
                .add(Aspect.HUNGER, 3)
                .add(Aspect.PLANT, 1)
                .add(Aspect.DARKNESS, 1)
                .add(Aspect.MAGIC, 2)
                .add(Aspect.POISON, 1)
                .add(Aspect.WATER, 1)
                .add(Aspect.METAL, 8)
        );
        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.tankGlyph, 1, 0), new AspectList().add(Aspect.WATER, 1).add(Aspect.SLIME, 1));
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.tankGlyph, 1, 1), new AspectList().add(Aspect.WATER, 3).add(Aspect.ENTROPY, 3).add(Aspect.FIRE, 2).add(Aspect.VOID, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.tankGlyph, 1, 2), new AspectList().add(Aspect.WATER, 3).add(Aspect.ENTROPY, 4).add(Aspect.HUNGER, 2)
        );
        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.tankGlyph, 1, 3), new AspectList().add(Aspect.WATER, 3).add(Aspect.EXCHANGE, 3));
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.tankGlyph, 1, 4),
            new AspectList().add(Aspect.WATER, 3).add(Aspect.GREED, 3).add(Aspect.ELDRITCH, 2).add(Aspect.TRAVEL, 2).add(Aspect.MAGIC, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.tankGlyph, 1, 5), new AspectList().add(Aspect.WATER, 3).add(Aspect.MECHANISM, 3).add(Aspect.ORDER, 3)
        );
        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.tankGlyph, 1, 6), new AspectList().add(Aspect.WATER, 3).add(Aspect.ORDER, 3));
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.tankGlyph, 1, 7), new AspectList().add(Aspect.WATER, 3).add(Aspect.HUNGER, 8).add(Aspect.GREED, 1)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.tankGlyph, 1, 8), new AspectList().add(Aspect.WATER, 7).add(Aspect.ELDRITCH, 3).add(Aspect.VOID, 3).add(Aspect.MAGIC, 2)
        );
        ThaumcraftApi.registerObjectTag(
            new ItemStack(ModItems.tankGlyph, 1, 9), new AspectList().add(Aspect.WATER, 3).add(Aspect.SENSES, 4).add(Aspect.BEAST, 3).add(Aspect.EARTH, 3)
        );
        AspectList aspects = duplicateConfluxDiscAspects.copy().remove(Aspect.METAL, duplicateConfluxDiscAspects.getAmount(Aspect.METAL) / 2);
        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.ingredient, 1, 0), aspects);
        AspectList var65 = aspects.copy().add(Aspect.METAL, 2).add(Aspect.TRAVEL, 1);
        ThaumcraftApi.registerObjectTag(new ItemStack(ModBlocks.mirrorAlt), var65);
        AspectList var66 = var65.copy().add(Aspect.METAL, 1).add(Aspect.MIND, 11);
        ThaumcraftApi.registerObjectTag(new ItemStack(ModBlocks.mirrorMultiDest), var66);
        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.phialXP), new AspectList().add(Aspect.MIND, 30));
        if (AutomagyConfig.usableThinkingCap) {
            RecipeSorter.register("Automagy:advGolem", AdvGolemRecipe.class, Category.SHAPELESS, "after:minecraft:shapeless");

            for (int ix = 0; ix < NUM_GOLEM_TYPES; ix++) {
                ItemStack advGolem = new ItemStack(ConfigItems.itemGolemPlacer, 1, ix);
                advGolem.setTagInfo("advanced", new NBTTagByte((byte)1));
                GameRegistry.addRecipe(
                    new AdvGolemRecipe(advGolem, new ItemStack(ConfigItems.itemGolemPlacer, 1, ix), new ItemStack(ModItems.creativeTool, 1, 0))
                );
            }
        }

        ThaumcraftApi.addWarpToItem(new ItemStack(ModBlocks.nethermind), 2);
        ThaumcraftApi.addWarpToItem(new ItemStack(ModBlocks.mobLure), 2);
        ThaumcraftApi.addWarpToItem(new ItemStack(ModBlocks.dimensionLure), 2);
        ThaumcraftApi.addWarpToItem(new ItemStack(ModBlocks.xpStone), 1);
    }

    public static void registerInfusionEnchantmentRecipes() {
        ItemStack salisMundus = ItemApi.getItem("itemResource", 14);
        enchantmentRecipeLure = ThaumcraftApi.addInfusionEnchantmentRecipe(
            "ENCHANT_FISHING",
            enchantmentLure,
            1,
            new AspectList().add(Aspect.WATER, 8).add(Aspect.HARVEST, 8).add(Aspect.BEAST, 8).add(Aspect.HUNGER, 3),
            new ItemStack[]{new ItemStack(Items.carrot_on_a_stick), new ItemStack(Items.sugar), salisMundus}
        );
        enchantmentRecipeLuckOfTheSea = ThaumcraftApi.addInfusionEnchantmentRecipe(
            "ENCHANT_FISHING",
            enchantmentLuckOfTheSea,
            2,
            new AspectList().add(Aspect.WATER, 8).add(Aspect.HARVEST, 8).add(Aspect.BEAST, 8).add(Aspect.GREED, 3),
            new ItemStack[]{new ItemStack(Items.golden_carrot), new ItemStack(Blocks.tripwire_hook), salisMundus}
        );
    }

    public static void addBasicRecipe(Item item, int count, int damage, Object... par) {
        ShapedOreRecipe recipe = new ShapedOreRecipe(new ItemStack(item, count, damage), par);
        GameRegistry.addRecipe(recipe);
        storeRecipe(item, damage, recipe);
    }

    public static void addShapelessBasicRecipe(Item item, int count, int damage, Object... par) {
        ShapelessOreRecipe recipe = new ShapelessOreRecipe(new ItemStack(item, count, damage), par);
        GameRegistry.addRecipe(recipe);
        storeRecipe(item, damage, recipe);
    }

    public static void addAlternateShapelessBasicRecipe(Item item, int count, int damage, Object... par) {
        ShapelessOreRecipe recipe = new ShapelessOreRecipe(new ItemStack(item, count, damage), par);
        GameRegistry.addRecipe(recipe);
        storeRecipe(item, damage, recipe, true);
    }

    public static void addArcaneCraftingRecipe(Item item, int count, int damage, String research, AspectList aspects, Object... recipe) {
        ShapedArcaneRecipe arcaneRecipe = ThaumcraftApi.addArcaneCraftingRecipe(research, new ItemStack(item, count, damage), aspects, recipe);
        storeRecipe(item, damage, arcaneRecipe);
    }

    public static void addShapelessArcaneCraftingRecipe(Item item, int count, int damage, String research, AspectList aspects, Object... recipe) {
        ShapelessArcaneRecipe arcaneRecipe = ThaumcraftApi.addShapelessArcaneCraftingRecipe(research, new ItemStack(item, count, damage), aspects, recipe);
        storeRecipe(item, damage, arcaneRecipe);
    }

    public static void addAlternateArcaneCraftingRecipe(Item item, int count, int damage, String research, AspectList aspects, Object... recipe) {
        ShapedArcaneRecipe arcaneRecipe = ThaumcraftApi.addArcaneCraftingRecipe(research, new ItemStack(item, count, damage), aspects, recipe);
        storeRecipe(item, damage, arcaneRecipe, true);
    }

    public static void addAlternateShapelessArcaneCraftingRecipe(Item item, int count, int damage, String research, AspectList aspects, Object... recipe) {
        ShapelessArcaneRecipe arcaneRecipe = ThaumcraftApi.addShapelessArcaneCraftingRecipe(research, new ItemStack(item, count, damage), aspects, recipe);
        storeRecipe(item, damage, arcaneRecipe, true);
    }

    public static void addAlternateShapelessArcaneCraftingRecipe(Block block, int count, int damage, String research, AspectList aspects, Object... recipe) {
        ShapelessArcaneRecipe arcaneRecipe = ThaumcraftApi.addShapelessArcaneCraftingRecipe(research, new ItemStack(block, count, damage), aspects, recipe);
        storeRecipe(block, damage, arcaneRecipe, true, false);
    }

    public static void addCrucibleRecipe(Item item, int count, int damage, String research, ItemStack catalyst, AspectList aspects) {
        CrucibleRecipe recipe = ThaumcraftApi.addCrucibleRecipe(research, new ItemStack(item, count, damage), catalyst, aspects);
        storeRecipe(item, damage, recipe);
    }

    public static void addInfusionRecipe(
        Item item, int count, int damage, String research, int instability, AspectList aspects, ItemStack input, ItemStack... components
    ) {
        InfusionRecipe recipe = ThaumcraftApi.addInfusionCraftingRecipe(research, new ItemStack(item, count, damage), instability, aspects, input, components);
        storeRecipe(item, damage, recipe);
    }

    public static void addInfusionRecipe(
        Block block, int count, int damage, String research, int instability, AspectList aspects, ItemStack input, ItemStack... components
    ) {
        addInfusionRecipe(false, block, count, damage, research, instability, aspects, input, components);
    }

    public static void addAlternateInfusionRecipe(
        Block block, int count, int damage, String research, int instability, AspectList aspects, ItemStack input, ItemStack... components
    ) {
        addInfusionRecipe(true, block, count, damage, research, instability, aspects, input, components);
    }

    public static void addInfusionRecipe(
        boolean alternate, Block block, int count, int damage, String research, int instability, AspectList aspects, ItemStack input, ItemStack... components
    ) {
        InfusionRecipe recipe = ThaumcraftApi.addInfusionCraftingRecipe(research, new ItemStack(block, count, damage), instability, aspects, input, components);
        storeRecipe(block, damage, recipe, alternate, false);
    }

    public static Object getRecipe(Object item) {
        return recipes.get(item);
    }

    public static Object getAlternateRecipe(Object item) {
        return recipesAlt.get(item);
    }

    public static Object getRecipe(Item item, int damage) {
        return recipes.get(item.getUnlocalizedName() + ":" + damage);
    }

    public static Object getAlternateRecipe(Item item, int damage) {
        return recipesAlt.get(item.getUnlocalizedName() + ":" + damage);
    }

    public static Object getRecipe(Block block, int damage) {
        return recipes.get(block.getUnlocalizedName() + ":" + damage);
    }

    public static Object getAlternateRecipe(Block block, int damage) {
        return recipesAlt.get(block.getUnlocalizedName() + ":" + damage);
    }

    public static void storeRecipe(Item item, int damage, Object recipe) {
        storeRecipe(item, damage, recipe, false);
    }

    public static void storeRecipe(Item item, int damage, Object recipe, boolean alternate) {
        HashMap list = alternate ? recipesAlt : recipes;
        if (item.getHasSubtypes()) {
            list.put(item.getUnlocalizedName() + ":" + damage, recipe);
        } else {
            list.put(item, recipe);
        }
    }

    public static void addBasicRecipe(Block item, int count, int damage, Object... par) {
        ShapedOreRecipe recipe = new ShapedOreRecipe(new ItemStack(item, count, damage), par);
        GameRegistry.addRecipe(recipe);
        storeRecipe(item, damage, recipe);
    }

    public static void addShapelessArcaneCraftingRecipe(Block item, int count, int damage, String research, AspectList aspects, Object... recipe) {
        ShapelessArcaneRecipe arcaneRecipe = ThaumcraftApi.addShapelessArcaneCraftingRecipe(research, new ItemStack(item, count, damage), aspects, recipe);
        storeRecipe(item, damage, arcaneRecipe);
    }

    public static void addShapelessArcaneCraftingRecipeLinkable(Block item, int count, int damage, String research, AspectList aspects, Object... recipe) {
        ShapelessArcaneRecipe arcaneRecipe = ThaumcraftApi.addShapelessArcaneCraftingRecipe(research, new ItemStack(item, count, damage), aspects, recipe);
        storeRecipeForceLinkable(item, damage, arcaneRecipe);
    }

    public static void addCrucibleRecipe(Block block, int count, int damage, String research, ItemStack catalyst, AspectList aspects) {
        CrucibleRecipe recipe = ThaumcraftApi.addCrucibleRecipe(research, new ItemStack(block, count, damage), catalyst, aspects);
        storeRecipe(block, damage, recipe);
    }

    public static void addArcaneCraftingRecipe(Block block, int count, int damage, String research, AspectList aspects, Object... recipe) {
        ShapedArcaneRecipe arcaneRecipe = ThaumcraftApi.addArcaneCraftingRecipe(research, new ItemStack(block, count, damage), aspects, recipe);
        storeRecipe(block, damage, arcaneRecipe);
    }

    public static void storeRecipe(Block block, int damage, Object recipe) {
        storeRecipe(block, damage, recipe, false, false);
    }

    public static void storeRecipeForceLinkable(Block item, int damage, Object recipe) {
        storeRecipe(item, damage, recipe, false, true);
    }

    public static void storeRecipe(Block block, int damage, Object recipe, boolean alternate, boolean forceStringKey) {
        HashMap list = alternate ? recipesAlt : recipes;
        if (!forceStringKey && !Item.getItemFromBlock(block).getHasSubtypes()) {
            list.put(block, recipe);
        } else {
            list.put(block.getUnlocalizedName() + ":" + damage, recipe);
        }
    }

    private static void addFilterRecipe(int storeMode, Item item, int count, int damage, String research, AspectList aspects, Object... recipe) {
        IArcaneRecipe arcaneRecipe = new PreserveFilterRecipe(research, new ItemStack(item, count, damage), aspects, recipe);
        ThaumcraftApi.getCraftingRecipes().add(arcaneRecipe);
        if (storeMode > 0) {
            storeRecipe(item, damage, arcaneRecipe, storeMode != 1);
        }
    }

    public static void addFilterRecipe(Item item, int count, int damage, String research, AspectList aspects, Object... recipe) {
        addFilterRecipe(1, item, count, damage, research, aspects, recipe);
    }

    public static void addAlternateFilterRecipe(Item item, int count, int damage, String research, AspectList aspects, Object... recipe) {
        addFilterRecipe(2, item, count, damage, research, aspects, recipe);
    }

    public static void addOffBookFilterRecipe(Item item, int count, int damage, String research, AspectList aspects, Object... recipe) {
        addFilterRecipe(0, item, count, damage, research, aspects, recipe);
    }

    public static void copyObjectAspects(Block from, int fromMeta, Block to, int toMeta) {
        AspectList aspects = ThaumcraftApiHelper.getObjectAspects(new ItemStack(from, 1, fromMeta));
        if (aspects != null) {
            ThaumcraftApi.registerObjectTag(new ItemStack(to, 1, toMeta), aspects.copy());
        }
    }

    public static void copyObjectAspects(Block from, Block to) {
        copyObjectAspects(from, 32767, to, 32767);
    }
}
