package tuhljin.automagy.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import thaumcraft.api.ItemApi;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.blocks.ItemJarNode;
import thaumcraft.common.config.Config;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.items.ItemFocusCrafting;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.lib.AutomagyKeyHandler;

public class ModResearchItems {
    public static Map<String, String> cluesOnKill = new HashMap<>();

    public static void registerResearch() {
        boolean mirrorsEnabled = Config.allowMirrors;
        ResearchCategories.registerCategory(
            "AUTOMAGY", new ResourceLocation("Automagy:textures/gui/researchIcon.png"), new ResourceLocation("thaumcraft:textures/gui/gui_researchback.png")
        );
        new ModResearchItem("REDSTONETHEORY", new AspectList(), 0, 0, 0, new ItemStack(Items.redstone, 1))
            .createPages("1", "2", "3", ModBlocks.torchInversion_on, "4", ModBlocks.hourglass, "5")
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        ArrayList<IArcaneRecipe> sliverecipes = new ArrayList<>();
        ArrayList<IArcaneRecipe> sliverecipesUndo = new ArrayList<>();

        for (int damage = 0; damage < ModItems.shardSliver.numSubtypes; damage++) {
            sliverecipes.add((IArcaneRecipe)ModRecipes.getRecipe(ModItems.shardSliver, damage));
            sliverecipesUndo.add((IArcaneRecipe)ModRecipes.getRecipe(ItemApi.getItem("itemShard", damage).getItem(), damage));
        }

        new ModResearchItem("SLIVERS", new AspectList(), -1, -2, 0, new ItemStack(ModItems.shardSliver, 1, 32767))
            .createPages(
                "1",
                new ResearchPage(sliverecipes.toArray(new IArcaneRecipe[0])),
                new ResearchPage(sliverecipesUndo.toArray(new IArcaneRecipe[0])),
                ItemApi.getBlock("blockCosmeticSolid", 6)
            )
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        if (AutomagyConfig.sliversFromOreRefining > 0) {
            new ModResearchItem(
                    "ENTROPICREFINING",
                    new AspectList().add(Aspect.MAGIC, 1).add(Aspect.ENTROPY, 3).add(Aspect.MINE, 2),
                    -5,
                    -2,
                    1,
                    new ResourceLocation("automagy:textures/gui/r_entore.png")
                )
                .createPages("1", new ResearchPage(ModRecipes.infusedOreRefining))
                .setParentsHidden("ENTROPICPROCESSING")
                .registerResearchItem();
        }

        new ModResearchItem("SLIVERS_TRAVEL", new AspectList().add(Aspect.EARTH, 1).add(Aspect.AIR, 1), -3, -3, 0, ItemApi.getBlock("blockCosmeticSolid", 2))
            .createPages("1", ItemApi.getBlock("blockCosmeticSolid", 2))
            .setParents("SLIVERS")
            .setParentsHidden("PAVETRAVEL")
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem("SLIVERS_WARDING", new AspectList().add(Aspect.FIRE, 1).add(Aspect.ORDER, 1), -3, -1, 0, ItemApi.getBlock("blockCosmeticSolid", 3))
            .createPages("1", ItemApi.getBlock("blockCosmeticSolid", 3))
            .setParents("SLIVERS")
            .setParentsHidden("PAVEWARD")
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "MAGICHOURGLASS",
                new AspectList().add(Aspect.MECHANISM, 2).add(Aspect.ENERGY, 2).add(Aspect.MOTION, 3),
                -3,
                1,
                1,
                new ItemStack(ModBlocks.hourglassMagic)
            )
            .createPages("1", ModBlocks.hourglassMagic, "2", "3")
            .setParents("REDCRYSTAL")
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "REDCRYSTAL", new AspectList().add(Aspect.ENERGY, 2).add(Aspect.CRYSTAL, 2).add(Aspect.MAGIC, 2), 0, 2, 1, new ItemStack(ModBlocks.redcrystal)
            )
            .createPages("1", ModBlocks.redcrystal, "2")
            .setParents("REDSTONETHEORY")
            .setSiblings(new String[]{"REDCRYSTAL_AMP", "REDCRYSTAL_DIM", "REDCRYSTAL_DENSE"})
            .registerResearchItem();
        new ModResearchItem(
                "REDCRYSTAL_AMP",
                new AspectList().add(Aspect.ENERGY, 1).add(Aspect.MECHANISM, 1).add(Aspect.CRYSTAL, 1).add(Aspect.FIRE, 2),
                -2,
                3,
                1,
                new ItemStack(ModBlocks.redcrystalAmp)
            )
            .createPages("1", ModBlocks.redcrystalAmp)
            .setParents("REDCRYSTAL")
            .setConcealed()
            .setStub()
            .setRound()
            .registerResearchItem();
        new ModResearchItem(
                "REDCRYSTAL_DIM",
                new AspectList().add(Aspect.ENERGY, 1).add(Aspect.MECHANISM, 1).add(Aspect.CRYSTAL, 1).add(Aspect.ENTROPY, 2),
                2,
                3,
                1,
                new ItemStack(ModBlocks.redcrystalDim)
            )
            .createPages("1", ModBlocks.redcrystalDim)
            .setParents("REDCRYSTAL")
            .setConcealed()
            .setStub()
            .setRound()
            .registerResearchItem();
        new ModResearchItem(
                "REDCRYSTAL_DENSE",
                new AspectList().add(Aspect.ENERGY, 1).add(Aspect.MECHANISM, 1).add(Aspect.CRYSTAL, 1).add(Aspect.EARTH, 2),
                0,
                4,
                1,
                new ItemStack(ModBlocks.redcrystalDense)
            )
            .createPages("1", ModBlocks.redcrystalDense)
            .setParents("REDCRYSTAL")
            .setConcealed()
            .setStub()
            .setRound()
            .registerResearchItem();
        new ModResearchItem(
                "REDCRYSTAL_RES",
                new AspectList().add(Aspect.ENERGY, 4).add(Aspect.MECHANISM, 3).add(Aspect.CRYSTAL, 2).add(Aspect.AIR, 8),
                -3,
                5,
                2,
                new ItemStack(ModBlocks.redcrystalRes)
            )
            .createPages("1", ModBlocks.redcrystalRes, "2")
            .setParentsHidden("REDCRYSTAL")
            .setConcealed()
            .registerResearchItem();
        if (mirrorsEnabled) {
            new ModResearchItem(
                    "REDCRYSTAL_MIRRORBOUND",
                    new AspectList().add(Aspect.TRAVEL, 8).add(Aspect.ENERGY, 5).add(Aspect.DARKNESS, 3).add(Aspect.MAGIC, 5),
                    -5,
                    4,
                    2,
                    new ItemStack(ModBlocks.redcrystalMerc)
                )
                .createPages("1", ModBlocks.redcrystalMerc, "2", new String[]{"MIRRORESSENTIA", "MIRRORESSENTIA"})
                .setParents("REDCRYSTAL_RES")
                .setParentsHidden("MIRROR")
                .setConcealedSameCategory()
                .registerResearchItem();
        }

        new ModResearchItem(
                "FOCUSCRAFTING",
                new AspectList().add(Aspect.CRAFT, 5).add(Aspect.MAGIC, 5).add(Aspect.TOOL, 2),
                -4,
                7,
                1,
                new ItemStack(ModItems.focusCrafting)
            )
            .createPages(
                formattedResearchPage("FOCUSCRAFTING", "1", Automagy.proxy.getKeyName(AutomagyConfig.defaultKeyCraft)),
                ModItems.focusCrafting,
                "2",
                new String[]{"ROD_greatwood_staff", "STAFF"}
            )
            .setParentsHidden("FOCUSFIRE")
            .registerResearchItem();
        new ModResearchItem(
                "FOCUSCRAFTING_UPGRADE",
                new AspectList().add(Aspect.CRAFT, 3).add(Aspect.VOID, 3).add(Aspect.ELDRITCH, 3).add(Aspect.EXCHANGE, 3),
                -5,
                8,
                2,
                ItemFocusCrafting.gratis.icon
            )
            .createPages("1", "2")
            .setParents("FOCUSCRAFTING")
            .setParentsHidden("FOCALMANIPULATION")
            .setConcealedSameCategory()
            .setSecondary()
            .registerResearchItem();
        if (AutomagyConfig.allowAdvNodeJarring) {
            ItemStack jarredNode = ItemApi.getItem("itemJarNode", 0);
            ItemJarNode jarNodeItem = (ItemJarNode)jarredNode.getItem();
            jarNodeItem.setAspects(jarredNode, new AspectList().add(Aspect.AIR, 60).add(Aspect.FIRE, 60).add(Aspect.WATER, 60).add(Aspect.EARTH, 60));
            jarNodeItem.setNodeAttributes(jarredNode, NodeType.NORMAL, NodeModifier.BRIGHT, "");
            new ModResearchItem(
                    "ADVNODEJAR", new AspectList().add(Aspect.AURA, 7).add(Aspect.GREED, 4).add(Aspect.ORDER, 4).add(Aspect.TRAVEL, 4), -7, 6, 3, jarredNode
                )
                .createPages("1", ModRecipes.advNodeJarRecipe, "2")
                .setParentsHidden("NODEJAR", "FOCUSWARDING", "WARDEDARCANA")
                .setConcealedUnless("NODEJAR")
                .setSpecial()
                .registerResearchItem();
        }

        new ModResearchItem("NITORLIGHT", new AspectList().add(Aspect.LIGHT, 2), -5, 0, 1, new ItemStack(ModBlocks.translucent, 1, 32767))
            .createPages("1", new ItemStack(ModBlocks.translucent), new ItemStack(ModBlocks.translucent, 1, 1))
            .setParentsHidden("NITOR")
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "ENCHANT_FISHING",
                new AspectList().add(Aspect.WATER, 3).add(Aspect.HARVEST, 3).add(Aspect.MAGIC, 2),
                -6,
                2,
                1,
                new ResourceLocation("automagy:textures/gui/r_enchant_fishing.png")
            )
            .createPages("1", ModRecipes.enchantmentRecipeLure, ModRecipes.enchantmentRecipeLuckOfTheSea)
            .setParentsHidden("INFUSIONENCHANTMENT")
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "REMOTECOMPARATOR",
                new AspectList().add(Aspect.ENERGY, 2).add(Aspect.MECHANISM, 2).add(Aspect.SENSES, 4).add(Aspect.AIR, 2),
                3,
                1,
                1,
                new ItemStack(ModBlocks.remoteComparator)
            )
            .createPages("1", ModBlocks.remoteComparator, "2", ModItems.crystalEye)
            .setParents("REDCRYSTAL")
            .registerResearchItem();
        new ModResearchItem(
                "GREEDYCHEST", new AspectList().add(Aspect.GREED, 5).add(Aspect.VOID, 3).add(Aspect.EXCHANGE, 3), 3, 8, 1, new ItemStack(ModBlocks.greedyChest)
            )
            .createPages("1", new ItemStack(ModItems.avaricePearl, 1, 0), "2", ModBlocks.greedyChest, "3", new String[]{"ENCHANTEDPAPER", "ENCHANTEDPAPER"})
            .setParents("HUNGRYCHEST")
            .registerResearchItem();
        new ModResearchItem(
                "ENCHANTEDPAPER",
                new AspectList().add(Aspect.ORDER, 5).add(Aspect.MIND, 3).add(Aspect.MAGIC, 3),
                5,
                7,
                1,
                new ItemStack(ModItems.enchantedPaper)
            )
            .createPages(
                "1",
                new ItemStack(ModItems.enchantedPaper, 1, 0),
                "2",
                new ItemStack(ModItems.enchantedPaper, 1, 1),
                new ItemStack(ModItems.enchantedPaper, 1, 2),
                "3",
                ModRecipes.getRecipe(ModItems.enchantedPaper, 3),
                ModRecipes.getAlternateRecipe(ModItems.enchantedPaper, 3),
                new ItemStack(ModBlocks.enchantedBookshelf)
            )
            .setParents("GREEDYCHEST")
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "THIRSTYTANK",
                new AspectList().add(Aspect.HUNGER, 8).add(Aspect.WATER, 5).add(Aspect.VOID, 3).add(Aspect.EXCHANGE, 3),
                0,
                9,
                2,
                new ItemStack(ModBlocks.thirstyTank)
            )
            .createPages("1", ModBlocks.thirstyTank, "2")
            .setParents("GREEDYCHEST")
            .registerResearchItem();
        new ModResearchItem(
                "THIRSTYTANK_GLYPH",
                new AspectList().add(Aspect.HUNGER, 3).add(Aspect.WATER, 3).add(Aspect.MECHANISM, 2),
                -1,
                8,
                1,
                new ItemStack(ModItems.tankGlyph, 1, 20)
            )
            .createPages(
                "1",
                new ItemStack(ModItems.tankGlyph),
                "2",
                "3",
                new ItemStack(ModItems.tankGlyph, 1, 1),
                new ItemStack(ModItems.tankGlyph, 1, 2),
                new ItemStack(ModItems.tankGlyph, 1, 3),
                new ItemStack(ModItems.tankGlyph, 1, 4),
                new ItemStack(ModItems.tankGlyph, 1, 5),
                new ItemStack(ModItems.tankGlyph, 1, 6),
                new ItemStack(ModItems.tankGlyph, 1, 7),
                new ItemStack(ModItems.tankGlyph, 1, 8)
            )
            .setParents("THIRSTYTANK")
            .setParentsHidden("AVARICIOUS")
            .setConcealedUnless("THIRSTYTANK")
            .registerResearchItem();
        new ModResearchItem(
                "THIRSTYTANK_GLYPH_BOVINE",
                new AspectList().add(Aspect.HUNGER, 2).add(Aspect.WATER, 2).add(Aspect.BEAST, 3).add(Aspect.HARVEST, 3),
                -2,
                8,
                2,
                new ItemStack(ModItems.tankGlyph, 1, 9)
            )
            .createPages("1", new ItemStack(ModItems.tankGlyph, 1, 9))
            .setParents("THIRSTYTANK_GLYPH")
            .setParentsHidden("ENTITYSIGNAL")
            .setConcealedUnless("THIRSTYTANK_GLYPH")
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem("AVARICIOUS", new AspectList().add(Aspect.GREED, 2).add(Aspect.ORDER, 1), 4, 10, 1, new ItemStack(ModItems.avaricePearl, 1, 1))
            .createPages("1", new ItemStack(ModItems.ingredientHorizontal), "2", "3", new ItemStack(ModItems.avaricePearl, 1, 1))
            .setParents("GREEDYCHEST")
            .setHidden()
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "ENDERDISJUNCTION",
                new AspectList().add(Aspect.ENTROPY, 4).add(Aspect.TRAP, 5).add(Aspect.ELDRITCH, 3),
                2,
                11,
                2,
                new ItemStack(ModItems.ringEnderDisjunction)
            )
            .createPages("1", new ItemStack(ModItems.ringEnderDisjunction), new String[]{"RUNICAUGMENTATION", "RUNICAUGMENTATION"})
            .setParents("AVARICIOUS")
            .setConcealedUnless("GREEDYCHEST")
            .registerResearchItem();
        new ModResearchItem(
                "EAGERCHEST", new AspectList().add(Aspect.EXCHANGE, 5).add(Aspect.VOID, 3).add(Aspect.MAN, 3), 6, 9, 1, new ItemStack(ModBlocks.eagerChest)
            )
            .createPages("1", new ItemStack(ModItems.edible, 1, 0), "2", ModBlocks.eagerChest)
            .setParents("AVARICIOUS", "ENCHANTEDPAPER")
            .setConcealedUnless("GREEDYCHEST")
            .registerResearchItem();
        new ModResearchItem("TENACIOUSCHEST", new AspectList().add(Aspect.GREED, 3).add(Aspect.VOID, 3), 6, 11, 1, new ItemStack(ModBlocks.tenaciousChest))
            .createPages("1", ModBlocks.tenaciousChest, "2")
            .setParents("AVARICIOUS")
            .setConcealedUnless("GREEDYCHEST")
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "HUNGRYMAW",
                new AspectList().add(Aspect.HUNGER, 5).add(Aspect.VOID, 3).add(Aspect.DARKNESS, 3).add(Aspect.EXCHANGE, 3),
                1,
                6,
                1,
                new ItemStack(ModBlocks.hungryMaw)
            )
            .createPages("1", ModBlocks.hungryMaw, "2")
            .setParents("HUNGRYCHEST", "FOCUSPORTABLEHOLE")
            .registerResearchItem();
        new ModResearchItem(
                "FINICALMAW", new AspectList().add(Aspect.HUNGER, 8).add(Aspect.ORDER, 8).add(Aspect.EXCHANGE, 5), 3, 6, 1, new ItemStack(ModBlocks.finicalMaw)
            )
            .createPages("1", ModBlocks.finicalMaw)
            .setParents("HUNGRYMAW", "ENCHANTEDPAPER")
            .setConcealedUnless("HUNGRYMAW")
            .setSecondary()
            .registerResearchItem();
        if (mirrorsEnabled) {
            new ModResearchItem(
                    "AUTOHANDMIRROR",
                    new AspectList().add(Aspect.HUNGER, 8).add(Aspect.ORDER, 8).add(Aspect.EXCHANGE, 5),
                    4,
                    4,
                    1,
                    new ItemStack(ModItems.autoHandMirror)
                )
                .createPages("1", ModItems.autoHandMirror, "2")
                .setParents("FINICALMAW")
                .setParentsHidden("MIRRORHAND")
                .setConcealedUnless("HUNGRYMAW")
                .setSecondary()
                .registerResearchItem();
        }

        new ModResearchItem(
                "VISREADER", new AspectList().add(Aspect.SENSES, 5).add(Aspect.MAGIC, 5).add(Aspect.AURA, 3), 3, -1, 1, new ItemStack(ModBlocks.visReader)
            )
            .createPages("1", ModBlocks.visReader, "2", "3", "4", "5", new String[]{"INFUSION", "INFUSION"}, new String[]{"ESSENTIALOCUS", "ESSENTIALOCUS"})
            .setParents("REMOTECOMPARATOR", "DISTILESSENTIA")
            .setConcealedUnless("REMOTECOMPARATOR")
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "TALLYBLOCK", new AspectList().add(Aspect.SENSES, 3).add(Aspect.ENERGY, 3).add(Aspect.ORDER, 3), 5, 2, 1, new ItemStack(ModBlocks.tallyBlock)
            )
            .createPages("1", ModBlocks.tallyBlock, "2")
            .setParents("REMOTECOMPARATOR", "ENCHANTEDPAPER")
            .setConcealedUnless("REMOTECOMPARATOR")
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "TALLYBLOCK_LENS",
                new AspectList().add(Aspect.SENSES, 3).add(Aspect.VOID, 2).add(Aspect.MINE, 2),
                6,
                1,
                1,
                new ItemStack(ModItems.tallyLens, 1, 32767)
            )
            .createPages("1", new ItemStack(ModItems.tallyLens, 1, 0), new ItemStack(ModItems.tallyLens, 1, 1), new ItemStack(ModItems.tallyLens, 1, 2))
            .setParents("TALLYBLOCK")
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "GOLEMWORKBENCH",
                new AspectList().add(Aspect.CRAFT, 8).add(Aspect.ORDER, 3).add(Aspect.MAN, 5).add(Aspect.SOUL, 5),
                7,
                6,
                1,
                new ItemStack(ModBlocks.golemWorkbench)
            )
            .createPages(
                "1",
                ModBlocks.golemWorkbench,
                "2",
                new String[]{"UPGRADEORDER", "UPGRADEORDER"},
                new String[]{"UPGRADEENTROPY", "UPGRADEENTROPY"},
                new String[]{"ADVANCEDGOLEM", "ADVANCEDGOLEM"},
                new String[]{"GOLEMFETTER", "GOLEMFETTER"}
            )
            .setParents("ENCHANTEDPAPER")
            .setParentsHidden("COREEMPTY")
            .setConcealedUnless("GOLEMSTRAW")
            .registerResearchItem();
        new ModResearchItem(
                "GOLEMWORKBENCH_UPGRADE",
                new AspectList().add(Aspect.CRAFT, 5).add(Aspect.MAN, 1).add(Aspect.SOUL, 1),
                8,
                6,
                1,
                new ItemStack(ModItems.golemWorkbenchUpgrade, 1, 32767)
            )
            .createPages(
                "1",
                new ItemStack(ModItems.golemWorkbenchUpgrade, 1, 0),
                "2",
                new ItemStack(ModItems.golemWorkbenchUpgrade, 1, 1),
                "3",
                new ItemStack(ModItems.golemWorkbenchUpgrade, 1, 3),
                new ItemStack(ModItems.golemWorkbenchUpgrade, 1, 5)
            )
            .setParents("GOLEMWORKBENCH")
            .setParentsHidden("UPGRADEAIR", "UPGRADEEARTH", "UPGRADEFIRE", "UPGRADEWATER", "UPGRADEORDER", "UPGRADEENTROPY")
            .setConcealedSameCategory()
            .setSecondary()
            .registerResearchItem();
        ArrayList<CrucibleRecipe> brainipes = new ArrayList<>();

        for (int damage = 0; damage < 6; damage++) {
            brainipes.add((CrucibleRecipe)ModRecipes.getRecipe(ModItems.crystalBrain, damage));
        }

        new ModResearchItem(
                "CRYSTALBRAIN",
                new AspectList()
                    .add(Aspect.MIND, 5)
                    .add(Aspect.WATER, 1)
                    .add(Aspect.AIR, 1)
                    .add(Aspect.ORDER, 1)
                    .add(Aspect.CRYSTAL, 5)
                    .add(Aspect.FIRE, 1)
                    .add(Aspect.EARTH, 1)
                    .add(Aspect.ENTROPY, 1),
                7,
                -1,
                3,
                new ItemStack(ModItems.crystalBrain, 1, 32767)
            )
            .createPages("1", new ResearchPage(brainipes.toArray(new CrucibleRecipe[0])))
            .setParents("REMOTECOMPARATOR")
            .setConcealedUnless("REDCRYSTAL")
            .registerResearchItem();
        new ModResearchItem(
                "GOLEMINHIBITOR",
                new AspectList().add(Aspect.MIND, 5).add(Aspect.TRAP, 8).add(Aspect.MECHANISM, 4).add(Aspect.SOUL, 2).add(Aspect.MAN, 2),
                6,
                -5,
                2,
                new ItemStack(ModBlocks.golemInhibitor)
            )
            .createPages("1", ModBlocks.golemInhibitor, new String[]{"GOLEMLINKER", "GOLEMLINKER"})
            .setParents("CRYSTALBRAIN")
            .setParentsHidden("GOLEMFETTER", "THAUMIUM")
            .setHidden()
            .setItemTriggers(new ItemStack[]{new ItemStack(ModItems.crystalBrain, 1, 5)})
            .registerResearchItem();
        new ModResearchItem(
                "GOLEMLINKER",
                new AspectList().add(Aspect.ORDER, 8).add(Aspect.MAN, 4).add(Aspect.SOUL, 4).add(Aspect.TOOL, 4),
                7,
                -5,
                1,
                new ItemStack(ModItems.golemLinker)
            )
            .createPages("1", ModItems.golemLinker, "2")
            .setParents("GOLEMINHIBITOR")
            .setParentsHidden("INFUSION", "UPGRADEORDER", "GOLEMBELL")
            .setConcealedSameCategory()
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "ENTITYSIGNAL",
                new AspectList().add(Aspect.EARTH, 10).add(Aspect.SENSES, 5).add(Aspect.LIFE, 5).add(Aspect.BEAST, 3).add(Aspect.SOUL, 3),
                9,
                -4,
                2,
                new ItemStack(ModBlocks.entitySignal)
            )
            .createPages("1", ModBlocks.entitySignal, "2", "3", ModItems.crystalEntityEye)
            .setParents("CRYSTALBRAIN")
            .setHidden()
            .setItemTriggers(new ItemStack[]{new ItemStack(ModItems.crystalBrain, 1, 3)})
            .registerResearchItem();
        new ModResearchItem(
                "ESSENTIALOCUS",
                new AspectList().add(Aspect.MIND, 5).add(Aspect.WATER, 10).add(Aspect.MAGIC, 8),
                4,
                -3,
                1,
                new ItemStack(ModBlocks.essentiaLocus, 1)
            )
            .createPages(
                "1", ModBlocks.essentiaLocus, "2", ModRecipes.essentiaLocusConstructRecipe, "3", new String[]{"ESSENTIARESERVOIR", "ESSENTIARESERVOIR"}
            )
            .setParents("CRYSTALBRAIN", "VISREADER")
            .setParentsHidden("TUBES", "GOGGLES")
            .setHidden()
            .setItemTriggers(new ItemStack[]{new ItemStack(ModItems.crystalBrain, 1, 2)})
            .registerResearchItem();
        if (mirrorsEnabled) {
            new ModResearchItem(
                    "ESSENTIALOCUSAGGREGATOR",
                    new AspectList().add(Aspect.MAGIC, 5).add(Aspect.SLIME, 5).add(Aspect.TRAVEL, 5).add(Aspect.DARKNESS, 5),
                    2,
                    -4,
                    1,
                    new ItemStack(ModBlocks.essentiaLocusAggregator, 1)
                )
                .createPages("1", ModBlocks.essentiaLocusAggregator, "2")
                .setParents("ESSENTIALOCUS")
                .setParentsHidden("MIRRORESSENTIA", "JARVOID")
                .setConcealedSameCategory()
                .setSecondary()
                .registerResearchItem();
        }

        new ModResearchItem(
                "INVENTARIUM", new AspectList().add(Aspect.SENSES, 5).add(Aspect.MIND, 5).add(Aspect.ORDER, 5), 8, 3, 1, new ItemStack(ModBlocks.unseenScribe)
            )
            .createPages("1", ModBlocks.inventariumCore, "2", ModRecipes.inventariumConstructRecipe, "3", ModBlocks.unseenScribe, "4", "5")
            .setParents("CRYSTALBRAIN", "TALLYBLOCK")
            .setParentsHidden("VISPOWER")
            .setHidden()
            .setItemTriggers(new ItemStack[]{new ItemStack(ModItems.crystalBrain, 1, 4)})
            .setSiblings("INVENTARIUM_EXTRA")
            .registerResearchItem();
        new ModResearchItem("INVENTARIUM_EXTRA", new AspectList().add(Aspect.MIND, 5), 10, 3, 1, new ItemStack(ModBlocks.enchantedBookshelf, 1, 1))
            .createPages("1", new ItemStack(ModBlocks.enchantedBookshelf, 1, 1))
            .setParents("INVENTARIUM")
            .setConcealed()
            .setRound()
            .registerResearchItem();
        new ModResearchItem(
                "INVENTARIUM_POINTER",
                new AspectList().add(Aspect.SENSES, 5).add(Aspect.CRYSTAL, 3).add(Aspect.VOID, 3),
                7,
                4,
                1,
                new ItemStack(ModBlocks.unseenScribePointer, 1)
            )
            .createPages("1", ModBlocks.unseenScribePointer, "2")
            .setParents("INVENTARIUM")
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "INVENTARIUM_DELIVERY",
                new AspectList().add(Aspect.GREED, 5).add(Aspect.MAN, 3).add(Aspect.MIND, 5).add(Aspect.SOUL, 3).add(Aspect.ORDER, 3),
                10,
                5,
                2,
                new ItemStack(ModBlocks.reqTome)
            )
            .createPages(
                "1",
                ModBlocks.golemTaskmaster,
                "2",
                new ItemStack(ModBlocks.metalGlow, 1, 0),
                "3",
                "4",
                "5",
                ModBlocks.reqTome,
                "6",
                new String[]{"MIRRORINPUT", "MIRRORINPUT"}
            )
            .setParents("INVENTARIUM")
            .setParentsHidden("GOLEMLINKER", "GOLEMINHIBITOR", "NITORLIGHT")
            .setConcealedUnless("INVENTARIUM")
            .registerResearchItem();
        new ModResearchItem(
                "THAUMOSTATICREFUELER",
                new AspectList().add(Aspect.MIND, 5).add(Aspect.AIR, 5).add(Aspect.FLIGHT, 5).add(Aspect.ENERGY, 5),
                10,
                1,
                2,
                new ItemStack(ModBlocks.thaumostaticSpire)
            )
            .createPages(
                "1",
                new ItemStack(ModBlocks.thaumostaticPylon),
                "2",
                new ItemStack(ModBlocks.thaumostaticPylon, 1, 1),
                "3",
                ModRecipes.thaumostaticSpireConstructRecipe,
                "4"
            )
            .setParents("CRYSTALBRAIN")
            .setParentsHidden("HOVERHARNESS", "VISPOWER", "THAUMIUM", "DISTILESSENTIA")
            .setHidden()
            .setItemTriggers(new ItemStack[]{new ItemStack(ModItems.crystalBrain, 1, 0)})
            .registerResearchItem();
        new ModResearchItem(
                "NETHERMIND",
                new AspectList().add(Aspect.MIND, 5).add(Aspect.FIRE, 10).add(Aspect.ELDRITCH, 5).add(Aspect.EXCHANGE, 5),
                11,
                -2,
                1,
                new ItemStack(ModBlocks.nethermind, 1, 1)
            )
            .createPages(
                "1",
                new ItemStack(ModBlocks.runedObsidian, 1, 0),
                ModBlocks.nethermind,
                "2",
                ModRecipes.nethermindConstructRecipe,
                "3",
                pageWithAspectList("NETHERMIND", "4", AutomagyConfig.nethermindRockCost),
                new ItemStack(ModBlocks.netherRune, 1, 0),
                pageWithAspectList("NETHERMIND", "5", AutomagyConfig.nethermindLavaCost),
                new ItemStack(ModBlocks.netherRune, 1, 1)
            )
            .setParents("CRYSTALBRAIN")
            .setParentsHidden("GREEDYCHEST", "VISPOWER", "DISTILESSENTIA")
            .setHidden()
            .setItemTriggers(new ItemStack[]{new ItemStack(ModItems.crystalBrain, 1, 1)})
            .registerResearchItem();
        new ModResearchItem(
                "NETHERRUNE_CRYSTAL",
                new AspectList().add(Aspect.CRYSTAL, 9).add(Aspect.ENERGY, 6).add(Aspect.EARTH, 3),
                13,
                -2,
                1,
                new ItemStack(ModBlocks.netherRune, 1, 2)
            )
            .createPages(pageWithAspectList("NETHERRUNE_CRYSTAL", "1", AutomagyConfig.nethermindQuartzCost), new ItemStack(ModBlocks.netherRune, 1, 2))
            .setParents("NETHERMIND")
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "NETHERRUNE_SOUL",
                new AspectList().add(Aspect.SOUL, 9).add(Aspect.TRAP, 3).add(Aspect.EARTH, 3).add(Aspect.ENTROPY, 3),
                13,
                -1,
                1,
                new ItemStack(ModBlocks.netherRune, 1, 4)
            )
            .createPages(pageWithAspectList("NETHERRUNE_SOUL", "1", AutomagyConfig.nethermindSoulSandCost), new ItemStack(ModBlocks.netherRune, 1, 4))
            .setParents("NETHERMIND")
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "NETHERRUNE_LIGHT", new AspectList().add(Aspect.LIGHT, 9).add(Aspect.SENSES, 6), 13, 0, 1, new ItemStack(ModBlocks.netherRune, 1, 3)
            )
            .createPages(pageWithAspectList("NETHERRUNE_LIGHT", "1", AutomagyConfig.nethermindGlowstoneCost), new ItemStack(ModBlocks.netherRune, 1, 3))
            .setParents("NETHERMIND")
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "NETHERRUNE_CELERITY",
                new AspectList().add(Aspect.ELDRITCH, 9).add(Aspect.TRAVEL, 6).add(Aspect.EXCHANGE, 3),
                12,
                0,
                1,
                new ItemStack(ModBlocks.netherRune, 1, 6)
            )
            .createPages("1", new ItemStack(ModBlocks.netherRune, 1, 6), new String[]{"DIMENSIONLURE", "DIMENSIONLURE"})
            .setParents("NETHERMIND")
            .setParentsHidden("GREEDYCHEST")
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "MOBLURE", new AspectList().add(Aspect.FIRE, 5).add(Aspect.MIND, 5).add(Aspect.HUNGER, 5), 12, -4, 2, new ItemStack(ModBlocks.mobLure)
            )
            .createPages("1", ModBlocks.mobLure, "2", "3", "4")
            .setParents("NETHERMIND")
            .setConcealedUnless("CRYSTALBRAIN")
            .setSecondary()
            .registerResearchItem();
        if (AutomagyConfig.dimensionLureAllowed) {
            new ModResearchItem(
                    "DIMENSIONLURE",
                    new AspectList().add(Aspect.FIRE, 5).add(Aspect.ELDRITCH, 5).add(Aspect.HUNGER, 5).add(Aspect.TRAVEL, 5).add(Aspect.TRAP, 5),
                    11,
                    -6,
                    2,
                    new ItemStack(ModBlocks.dimensionLure)
                )
                .createPages(
                    "1",
                    ModBlocks.dimensionLure,
                    "2",
                    ModRecipes.mobSpawnerConstructRecipe,
                    "3",
                    "4",
                    pageWithAspectListsAndNum(
                        "DIMENSIONLURE", "5", AutomagyConfig.nethermindBlazeCost, AutomagyConfig.dimLureBlazeUses, AutomagyConfig.dimLureBlazeCost
                    ),
                    new ItemStack(ModBlocks.netherRuneCreature, 1, 1),
                    "6"
                )
                .setParents("MOBLURE", "ENTITYSIGNAL")
                .setConcealedUnless("NETHERMIND")
                .registerResearchItem();
            new ModResearchItem(
                    "NETHERRUNE_ZOMBIE",
                    new AspectList().add(Aspect.TRAP, 3).add(Aspect.GREED, 3).add(Aspect.MIND, 3),
                    10,
                    -8,
                    1,
                    new ItemStack(ModBlocks.netherRuneCreature, 1, 0)
                )
                .createPages(
                    pageWithAspectListsAndNum(
                        "NETHERRUNE_ZOMBIE",
                        "1",
                        AutomagyConfig.nethermindZombiePigmanCost,
                        AutomagyConfig.dimLureZombiePigmanUses,
                        AutomagyConfig.dimLureZombiePigmanCost
                    ),
                    new ItemStack(ModBlocks.netherRuneCreature, 1, 0)
                )
                .setParents("DIMENSIONLURE")
                .setParentsHidden("TALLOW")
                .setSecondary()
                .setHiddenUntilScanAndKillEntity("PigZombie")
                .registerResearchItem();
            new ModResearchItem(
                    "NETHERRUNE_GHAST",
                    new AspectList().add(Aspect.TRAP, 3).add(Aspect.SOUL, 3).add(Aspect.UNDEAD, 3),
                    11,
                    -8,
                    1,
                    new ItemStack(ModBlocks.netherRuneCreature, 1, 2)
                )
                .createPages(
                    "1",
                    new ItemStack(ModBlocks.netherRuneCreature, 1, 2),
                    pageWithAspectListsAndNum(
                        "NETHERRUNE_GHAST", "2", AutomagyConfig.nethermindGhastCost, AutomagyConfig.dimLureGhastUses, AutomagyConfig.dimLureGhastCost
                    )
                )
                .setParents("DIMENSIONLURE")
                .setSecondary()
                .setHiddenUntilScanAndKillEntity("Ghast")
                .registerResearchItem();
            new ModResearchItem(
                    "NETHERRUNE_SKELETON",
                    new AspectList().add(Aspect.TRAP, 3).add(Aspect.DEATH, 3).add(Aspect.WEAPON, 3),
                    12,
                    -8,
                    1,
                    new ItemStack(ModBlocks.netherRuneCreature, 1, 3)
                )
                .createPages(
                    pageWithAspectListsAndNum(
                        "NETHERRUNE_SKELETON",
                        "1",
                        AutomagyConfig.nethermindSkeletonCost,
                        AutomagyConfig.dimLureSkeletonUses,
                        AutomagyConfig.dimLureSkeletonCost
                    ),
                    new ItemStack(ModBlocks.netherRuneCreature, 1, 3)
                )
                .setParents("DIMENSIONLURE")
                .setSecondary()
                .setHiddenUntilScanAndKillEntity("Skeleton")
                .registerResearchItem();
            new ModResearchItem(
                    "NETHERRUNE_SLIME",
                    new AspectList().add(Aspect.TRAP, 3).add(Aspect.SLIME, 3).add(Aspect.FIRE, 3),
                    13,
                    -7,
                    1,
                    new ItemStack(ModBlocks.netherRuneCreature, 1, 4)
                )
                .createPages(
                    pageWithAspectListsAndNum(
                        "NETHERRUNE_SLIME", "1", AutomagyConfig.nethermindSlimeCost, AutomagyConfig.dimLureSlimeUses, AutomagyConfig.dimLureSlimeCost
                    ),
                    new ItemStack(ModBlocks.netherRuneCreature, 1, 4)
                )
                .setParents("DIMENSIONLURE")
                .setSecondary()
                .setHiddenUntilScanAndKillEntity("LavaSlime")
                .registerResearchItem();
            new ModResearchItem(
                    "NETHERRUNE_BAT",
                    new AspectList().add(Aspect.TRAP, 3).add(Aspect.BEAST, 3).add(Aspect.FIRE, 3),
                    13,
                    -6,
                    1,
                    new ItemStack(ModBlocks.netherRuneCreature, 1, 5)
                )
                .createPages(
                    pageWithAspectListsAndNum(
                        "NETHERRUNE_BAT", "1", AutomagyConfig.nethermindBatCost, AutomagyConfig.dimLureBatUses, AutomagyConfig.dimLureBatCost
                    ),
                    new ItemStack(ModBlocks.netherRuneCreature, 1, 5)
                )
                .setParents("DIMENSIONLURE")
                .setSecondary()
                .setHiddenUntilScanAndKillEntity("Thaumcraft.Firebat")
                .registerResearchItem();
            new ModResearchItem(
                    "NETHERRUNE_WISP",
                    new AspectList().add(Aspect.TRAP, 3).add(Aspect.AURA, 3).add(Aspect.FIRE, 3),
                    13,
                    -8,
                    1,
                    new ItemStack(ModBlocks.netherRuneCreature, 1, 6)
                )
                .createPages(
                    "1",
                    new ItemStack(ModBlocks.netherRuneCreature, 1, 6),
                    pageWithAspectListsAndNum(
                        "NETHERRUNE_WISP", "2", AutomagyConfig.nethermindWispCost, AutomagyConfig.dimLureWispUses, AutomagyConfig.dimLureWispCost
                    )
                )
                .setParents("DIMENSIONLURE")
                .setSecondary()
                .setHiddenUntilScanAndKillEntity("Automagy.WispNether")
                .registerResearchItem();
        }

        new ModResearchItem("AMNESIASTONE", new AspectList().add(Aspect.MIND, 5).add(Aspect.VOID, 5), 5, -7, 1, new ItemStack(ModBlocks.amnesiacBlock))
            .createPages("1", ModBlocks.amnesiacBlock, "2")
            .setParents("GOLEMINHIBITOR", "JARBRAIN")
            .setConcealedUnless("CRYSTALBRAIN")
            .setSecondary()
            .registerResearchItem();
        new ModResearchItem(
                "XPSTONE",
                new AspectList().add(Aspect.MIND, 2).add(Aspect.EARTH, 2).add(Aspect.ENTROPY, 2).add(Aspect.EXCHANGE, 2),
                4,
                -7,
                1,
                new ItemStack(ModBlocks.xpStone, 1, 1)
            )
            .createPages("1", ModBlocks.xpStone, "2", "3", "4")
            .setParents("AMNESIASTONE")
            .registerResearchItem();
        if (mirrorsEnabled) {
            new ModResearchItem(
                    "MIRRORINPUT",
                    new AspectList().add(Aspect.TRAVEL, 4).add(Aspect.ELDRITCH, 2).add(Aspect.DARKNESS, 2).add(Aspect.SENSES, 2),
                    9,
                    8,
                    2,
                    new ItemStack(ModBlocks.mirrorAlt)
                )
                .createPages("1", new ItemStack(ModBlocks.mirrorAlt, 1, 0), "2", "3", new ItemStack(ModItems.ingredient), ModRecipes.duplicateConfluxDisc)
                .setParentsHidden("MIRROR", "VOIDMETAL", "REMOTECOMPARATOR")
                .setConcealedSameCategory()
                .registerResearchItem();
            new ModResearchItem(
                    "MIRRORMULTIDEST",
                    new AspectList().add(Aspect.TRAVEL, 2).add(Aspect.ELDRITCH, 2).add(Aspect.DARKNESS, 2).add(Aspect.MIND, 2),
                    10,
                    6,
                    2,
                    new ItemStack(ModBlocks.mirrorMultiDest)
                )
                .createPages("1", new ItemStack(ModBlocks.mirrorMultiDest, 1, 0), "2", "3")
                .setParents("MIRRORINPUT", "INVENTARIUM_DELIVERY")
                .setSecondary()
                .setConcealedUnless("INVENTARIUM_DELIVERY")
                .registerResearchItem();
        }

        new ModResearchItem(
                "ALCHEMYBOILER",
                new AspectList().add(Aspect.MAGIC, 2).add(Aspect.WATER, 5).add(Aspect.SLIME, 2).add(Aspect.FIRE, 3),
                -2,
                11,
                2,
                new ItemStack(ModBlocks.boiler)
            )
            .createPages("1", ModBlocks.boiler)
            .setParentsHidden("DISTILESSENTIA")
            .registerResearchItem();
        new ModResearchItem(
                "VISHROOMSOUP",
                new AspectList().add(Aspect.MIND, 1).add(Aspect.ELDRITCH, 1).add(Aspect.MAGIC, 1),
                -4,
                10,
                1,
                new ItemStack(ModItems.bucketVishroom)
            )
            .createPages("1", new ItemStack(ModItems.food), "2", "3")
            .setHidden()
            .setSecondary()
            .setItemTriggers(new ItemStack[]{new ItemStack(ModItems.food, 1, 0), new ItemStack(ModBlocks.vishroomFluid)})
            .registerResearchItem();
        ThaumcraftApi.addWarpToResearch("CRYSTALBRAIN", 1);
        ThaumcraftApi.addWarpToResearch("XPSTONE", 1);
        ThaumcraftApi.addWarpToResearch("NETHERMIND", 3);
        ThaumcraftApi.addWarpToResearch("DIMENSIONLURE", 4);
    }

    public static ResearchPage pageWithAspectList(String research, String s, AspectList aspects) {
        return formattedResearchPage(research, s, commaListFromAspects(aspects));
    }

    public static ResearchPage pageWithAspectListsAndNum(String research, String s, AspectList aspects1, int num, AspectList aspects2) {
        return formattedResearchPage(research, s, commaListFromAspects(aspects1), num, commaListFromAspects(aspects2));
    }

    public static void makeClientAdjustments() {
        String key = AutomagyKeyHandler.getKeyName(AutomagyKeyHandler.keyCraft);
        if (key != null && !key.equals("NONE")) {
            ResearchItem r = ResearchCategories.getResearch("FOCUSCRAFTING");
            if (r instanceof ModResearchItem) {
                ((ModResearchItem)r).replacePage(1, formattedResearchPage("FOCUSCRAFTING", "1", key));
            }
        }
    }

    private static ResearchPage formattedResearchPage(String research, String s, Object... o) {
        s = StatCollector.translateToLocalFormatted("Automagy.research_page." + research + "." + s, o);
        return new ResearchPage(s);
    }

    private static String commaListFromAspects(AspectList aspects) {
        StringBuilder sb = new StringBuilder();
        int num = 0;

        for (Aspect aspect : aspects.getAspects()) {
            int amt = aspects.getAmount(aspect);
            if (amt > 0) {
                if (num > 0) {
                    sb.append(", ");
                }

                sb.append(amt);
                sb.append(" ");
                sb.append(aspect.getName());
                num++;
            }
        }

        return sb.toString();
    }
}
