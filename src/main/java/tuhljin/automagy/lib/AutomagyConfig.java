package tuhljin.automagy.lib;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.api.AutomagyAPI;
import tuhljin.automagy.blocks.BlockAmnesiac;
import tuhljin.automagy.blocks.BlockNethermind;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.items.ItemPhialXP;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.tiles.TileEntityAmnesiacStone;

public class AutomagyConfig {
    protected static Configuration config;
    public static final String CATEGORY_NETHERMIND = "nethermind";
    public static final String CATEGORY_LOOT = "loot";
    public static final String CATEGORY_KEYS = "keys";
    public static boolean versionChecking = true;
    public static boolean listCreativeJar = true;
    public static boolean listThinkingCap = true;
    public static boolean usableThinkingCap = true;
    public static boolean redcrystalEmitsLight = true;
    public static boolean thirstyTankDrinksRain = true;
    public static boolean thirstyTankPreserveInfiniteWater = true;
    public static boolean allowAdvNodeJarring = true;
    public static int waterBottleAmount = 0;
    public static int milkingCooldownPerCow = 2000;
    public static int lootWeightRingEnderDisjunctionLesser = 1;
    public static int lootWeightPhialXP = 3;
    public static int lootWeightBlacksmithPhialXP = 3;
    public static boolean netherHeartDrops = true;
    public static boolean netherHeartDropNetherOnly = true;
    public static int nethermindBasicLavaChance;
    public static AspectList nethermindRockCostBasic;
    public static AspectList nethermindRockCost;
    public static AspectList nethermindLavaCost;
    public static AspectList nethermindQuartzCost;
    public static AspectList nethermindGlowstoneCost;
    public static AspectList nethermindSoulSandCost;
    public static boolean dimensionLureAllowed = true;
    public static int sliversFromOreRefining = 11;
    public static AspectList nethermindZombiePigmanCost;
    public static AspectList nethermindBlazeCost;
    public static AspectList nethermindGhastCost;
    public static AspectList nethermindSkeletonCost;
    public static AspectList nethermindSlimeCost;
    public static AspectList nethermindBatCost;
    public static AspectList nethermindWispCost;
    public static AspectList dimLureZombiePigmanCost;
    public static AspectList dimLureBlazeCost;
    public static AspectList dimLureGhastCost;
    public static AspectList dimLureSkeletonCost;
    public static AspectList dimLureSlimeCost;
    public static AspectList dimLureBatCost;
    public static AspectList dimLureWispCost;
    public static int dimLureZombiePigmanUses;
    public static int dimLureBlazeUses;
    public static int dimLureGhastUses;
    public static int dimLureSkeletonUses;
    public static int dimLureSlimeUses;
    public static int dimLureBatUses;
    public static int dimLureWispUses;
    public static int nethermindRockSpeed;
    public static int nethermindLavaSpeed;
    public static int nethermindQuartzSpeed;
    public static int nethermindGlowstoneSpeed;
    public static int nethermindSoulSandSpeed;
    public static int nethermindSpawnerSpeed;
    public static int focusUpgradeGratis = 81;
    public static int focusUpgradeTransposing = 82;
    public static int focusUpgradeStorage = 83;
    public static int defaultKeyCraft = 46;
    public static int netherSpireChance = 140;
    public static boolean wailaXPStone = true;
    public static boolean wailaBrainInAJar = true;
    public static Map<HashableItemWithoutSize, TileEntityAmnesiacStone.ItemDrainResult> amnesiacDrainItems = new HashMap<>();
    public static boolean useMFRMilk = true;
    public static boolean useMFRMushroomSoup = true;
    public static boolean overrideMFRMilkAspects = true;
    public static boolean overrideMFRMushroomSoupAspects = true;
    public static boolean useForestryMilk = true;
    public static boolean overrideForestryMilkAspects = true;
    public static boolean botaniaLooniumBlacklistPhialXP = false;
    private static int realWaterBottleAmount;

    public static void load(File file) {
        config = new Configuration(file);
        config.load();
        config.addCustomCategoryComment("loot", "Loot weights. Higher for higher chance. 1 is rare. 0 disables.");
        versionChecking = loadBoolean("version_checking", "On startup, check whether a newer version of the mod exists", true);
        listCreativeJar = loadBoolean("list_creative_jar", "Show the Infinity Jar in Creative Tab and NEI", true);
        listThinkingCap = loadBoolean("list_thinking_cap", "Show the thinking cap in Creative Tab and NEI", true);
        usableThinkingCap = loadBoolean("thinking_cap_usable", "Allow the thinking cap to be used to craft advanced golems", true);
        redcrystalEmitsLight = loadBoolean("redcrystal_emits_light", "Should redcrystal emit light when it has a redstone signal", true);
        thirstyTankDrinksRain = loadBoolean(
            "thirstytank_drink_rain", "Should thirsty tanks slowly fill with water if exposed to the sky while it is raining", true
        );
        thirstyTankPreserveInfiniteWater = loadBoolean(
            "thirstytank_preserve_water_source", "Should thirsty tanks preserve water source blocks next to them", thirstyTankPreserveInfiniteWater
        );
        allowAdvNodeJarring = loadBoolean("adv_node_jarring", "Allow Advanced Node Jarring", allowAdvNodeJarring);
        useMFRMilk = loadBoolean(
            "mfr_milk_used", "Should MineFactory Reloaded's milk be created when the thirsty tank milks cows rather than Automagy's own", useMFRMilk
        );
        useMFRMushroomSoup = loadBoolean(
            "mfr_mushroom_soup_used",
            "Should MineFactory Reloaded's mushroom soup be created when the thirsty tank milks mooshrooms rather than Automagy's own",
            useMFRMushroomSoup
        );
        overrideMFRMilkAspects = loadBoolean(
            "mfr_override_aspect_milk",
            "Should Automagy change the Aspects associated with MineFactory Reloaded's milk (arguably more balanced if we do, given what Automagy can do with it)",
            overrideMFRMilkAspects
        );
        overrideMFRMushroomSoupAspects = loadBoolean(
            "mfr_override_aspect_mushroom_soup",
            "Should Automagy change the Aspects associated with MineFactory Reloaded's mushroom soup",
            overrideMFRMushroomSoupAspects
        );
        useForestryMilk = loadBoolean(
            "forestry_milk_used",
            "Should Forestry's milk be created when the thirsty tank milks cows rather than Automagy's own (this is ignored if MFR milk exists and config is set to use it)",
            useForestryMilk
        );
        overrideForestryMilkAspects = loadBoolean(
            "forestry_override_aspect_milk", "Should Automagy assign Aspects to Forestry's milk", overrideForestryMilkAspects
        );
        waterBottleAmount = loadInteger(
            "waterbottle_amount",
            "How much water (in mB) a water bottle is considered to contain. 0 (default) means bottles can be filled from and emptied into Automagy tanks without changing the amount in a tank. -1 means empty and water bottles won't interact with Automagy tanks. -2 means bottles should use the fluid amount registered with Forge (normally 1000).",
            waterBottleAmount
        );
        realWaterBottleAmount = waterBottleAmount;
        milkingCooldownPerCow = loadInteger(
            "cow_milking_cooldown", "Number of ticks that must pass after a thirsty tank milks a cow before it can be milked again.", milkingCooldownPerCow
        );
        lootWeightRingEnderDisjunctionLesser = loadInteger(
            "loot", "lesser_disjunction", "Lesser Ring of Ender Disjunction", lootWeightRingEnderDisjunctionLesser
        );
        lootWeightPhialXP = loadInteger("loot", "phial_xp", "Phial of Enchanter's Concentrate", lootWeightPhialXP);
        lootWeightBlacksmithPhialXP = loadInteger(
            "loot", "phial_xp_blacksmith", "Phial of Enchanter's Concentrate (village blacksmith chest)", lootWeightBlacksmithPhialXP
        );
        netherHeartDrops = loadBoolean("emberheart_spawnerdrop", "Make blaze spawners drop an ember heart", true);
        netherHeartDropNetherOnly = loadBoolean("emberheart_spawnerdrop_netheronly", "Only drop ember heart from spawners in the Nether", true);
        nethermindBasicLavaChance = loadInteger(
            "nethermind",
            "lava_basic_chance",
            "Percentage chance that a basic rune results in conjuring lava instead of netherrack.",
            BlockNethermind.defaultLavaChance
        );
        nethermindRockCostBasic = loadAspectList(
            "nethermind",
            "netherrack_basic_cost",
            "Nethermind's essentia cost to generate netherrack without the Rock rune.",
            BlockNethermind.defaultRockCostBasic
        );
        nethermindRockCost = loadAspectList(
            "nethermind", "netherrack_cost", "Nethermind's essentia cost to generate netherrack using the Rock rune.", BlockNethermind.defaultRockCost
        );
        nethermindRockSpeed = loadInteger("nethermind", "netherrack_speed", null, BlockNethermind.defaultRockSpeed);
        nethermindLavaCost = loadAspectList("nethermind", "lava_cost", "Nethermind's essentia cost to generate lava.", BlockNethermind.defaultLavaCost);
        nethermindLavaSpeed = loadInteger("nethermind", "lava_speed", null, BlockNethermind.defaultLavaSpeed);
        nethermindQuartzCost = loadAspectList(
            "nethermind", "quartz_cost", "Nethermind's essentia cost to generate quartz ore.", BlockNethermind.defaultQuartzCost
        );
        nethermindQuartzSpeed = loadInteger("nethermind", "quartz_speed", null, BlockNethermind.defaultQuartzSpeed);
        nethermindGlowstoneCost = loadAspectList(
            "nethermind", "glowstone_cost", "Nethermind's essentia cost to generate glowstone.", BlockNethermind.defaultGlowstoneCost
        );
        nethermindGlowstoneSpeed = loadInteger("nethermind", "glowstone_speed", null, BlockNethermind.defaultGlowstoneSpeed);
        nethermindSoulSandCost = loadAspectList(
            "nethermind", "soulsand_cost", "Nethermind's essentia cost to generate soul sand.", BlockNethermind.defaultSoulSandCost
        );
        nethermindSoulSandSpeed = loadInteger("nethermind", "soulsand_speed", null, BlockNethermind.defaultSoulSandSpeed);
        dimensionLureAllowed = loadBoolean(
            "allow_dimension_lure", "Setting this to false will disable Dimensional Lure research and crafting recipes", dimensionLureAllowed
        );
        sliversFromOreRefining = loadInteger(
            "slivers_from_infused_stone",
            "Number of slivers Entropic Refining yields from infused stone. Default "
                + sliversFromOreRefining
                + ". Set to 0 to disabled Entropic Refining research and recipes.",
            sliversFromOreRefining
        );
        nethermindSpawnerSpeed = loadInteger(
            "nethermind",
            "spawner_speed",
            "Time in ticks for Nethermind + Dimensional Lure to spawn creatures (before upgrades)",
            BlockNethermind.defaultSpawnerSpeed
        );
        nethermindZombiePigmanCost = loadAspectList(
            "nethermind",
            "spawn_zombiepigman_cost1",
            "Cost to spawn each zombie pigman, used by Nethermind. Also see spawn_zombiepigman_cost2.",
            BlockNethermind.defaultZombiePigmanCost
        );
        dimLureZombiePigmanCost = loadAspectList(
            "nethermind",
            "spawn_zombiepigman_cost2",
            "Used by Dimensional Lure to spawn multiple zombie pigmen, in addition to the cost specified in spawn_zombiepigman_cost1.",
            BlockNethermind.defaultZombiePigmanCost2
        );
        dimLureZombiePigmanUses = loadInteger(
            "nethermind",
            "spawn_zombiepigman_uses",
            "The number of zombie pigman spawns that the essentia specified by spawn_zombiepigman_cost2 is good for.",
            BlockNethermind.defaultZombiePigmanUses
        );
        nethermindBlazeCost = loadAspectList("nethermind", "spawn_blaze_cost1", null, BlockNethermind.defaultBlazeCost);
        dimLureBlazeCost = loadAspectList("nethermind", "spawn_blaze_cost2", null, BlockNethermind.defaultBlazeCost2);
        dimLureBlazeUses = loadInteger("nethermind", "spawn_blaze_uses", null, BlockNethermind.defaultBlazeUses);
        nethermindGhastCost = loadAspectList("nethermind", "spawn_ghast_cost1", null, BlockNethermind.defaultGhastCost);
        dimLureGhastCost = loadAspectList("nethermind", "spawn_ghast_cost2", null, BlockNethermind.defaultGhastCost2);
        dimLureGhastUses = loadInteger("nethermind", "spawn_ghast_uses", null, BlockNethermind.defaultGhastUses);
        nethermindSkeletonCost = loadAspectList("nethermind", "spawn_skeleton_cost1", null, BlockNethermind.defaultSkeletonCost);
        dimLureSkeletonCost = loadAspectList("nethermind", "spawn_skeleton_cost2", null, BlockNethermind.defaultSkeletonCost2);
        dimLureSkeletonUses = loadInteger("nethermind", "spawn_skeleton_uses", null, BlockNethermind.defaultSkeletonUses);
        nethermindSlimeCost = loadAspectList("nethermind", "spawn_slime_cost1", null, BlockNethermind.defaultSlimeCost);
        dimLureSlimeCost = loadAspectList("nethermind", "spawn_slime_cost2", null, BlockNethermind.defaultSlimeCost2);
        dimLureSlimeUses = loadInteger("nethermind", "spawn_slime_uses", null, BlockNethermind.defaultSlimeUses);
        nethermindBatCost = loadAspectList("nethermind", "spawn_bat_cost1", null, BlockNethermind.defaultBatCost);
        dimLureBatCost = loadAspectList("nethermind", "spawn_bat_cost2", null, BlockNethermind.defaultBatCost2);
        dimLureBatUses = loadInteger("nethermind", "spawn_bat_uses", null, BlockNethermind.defaultBatUses);
        nethermindWispCost = loadAspectList("nethermind", "spawn_wisp_cost1", null, BlockNethermind.defaultWispCost);
        dimLureWispCost = loadAspectList("nethermind", "spawn_wisp_cost2", null, BlockNethermind.defaultWispCost2);
        dimLureWispUses = loadInteger("nethermind", "spawn_wisp_uses", null, BlockNethermind.defaultWispUses);
        boolean nethermindIgnoreCustom = loadBoolean(
            "nethermind",
            "ignore_section",
            "IMPORTANT: If true, this section's customizations will be ignored. (This is useful for those who want to use the default settings since the defaults may change with a newer version of Automagy. Otherwise, you'd need to delete this section each time such an update is applied.)",
            true
        );
        if (nethermindIgnoreCustom) {
            nethermindBasicLavaChance = BlockNethermind.defaultLavaChance;
            nethermindRockCostBasic = BlockNethermind.defaultRockCostBasic;
            nethermindRockCost = BlockNethermind.defaultRockCost;
            nethermindRockSpeed = BlockNethermind.defaultRockSpeed;
            nethermindLavaCost = BlockNethermind.defaultLavaCost;
            nethermindLavaSpeed = BlockNethermind.defaultLavaSpeed;
            nethermindQuartzCost = BlockNethermind.defaultQuartzCost;
            nethermindQuartzSpeed = BlockNethermind.defaultQuartzSpeed;
            nethermindGlowstoneCost = BlockNethermind.defaultGlowstoneCost;
            nethermindGlowstoneSpeed = BlockNethermind.defaultGlowstoneSpeed;
            nethermindSoulSandCost = BlockNethermind.defaultSoulSandCost;
            nethermindSoulSandSpeed = BlockNethermind.defaultSoulSandSpeed;
            nethermindSpawnerSpeed = BlockNethermind.defaultSpawnerSpeed;
            nethermindZombiePigmanCost = BlockNethermind.defaultZombiePigmanCost;
            nethermindBlazeCost = BlockNethermind.defaultBlazeCost;
            nethermindGhastCost = BlockNethermind.defaultGhastCost;
            nethermindSkeletonCost = BlockNethermind.defaultSkeletonCost;
            nethermindSlimeCost = BlockNethermind.defaultSlimeCost;
            nethermindBatCost = BlockNethermind.defaultBatCost;
            nethermindWispCost = BlockNethermind.defaultWispCost;
            dimLureZombiePigmanCost = BlockNethermind.defaultZombiePigmanCost2;
            dimLureBlazeCost = BlockNethermind.defaultBlazeCost2;
            dimLureGhastCost = BlockNethermind.defaultGhastCost2;
            dimLureSkeletonCost = BlockNethermind.defaultSkeletonCost2;
            dimLureSlimeCost = BlockNethermind.defaultSlimeCost2;
            dimLureBatCost = BlockNethermind.defaultBatCost2;
            dimLureWispCost = BlockNethermind.defaultWispCost2;
            dimLureZombiePigmanUses = BlockNethermind.defaultZombiePigmanUses;
            dimLureBlazeUses = BlockNethermind.defaultBlazeUses;
            dimLureGhastUses = BlockNethermind.defaultGhastUses;
            dimLureSkeletonUses = BlockNethermind.defaultSkeletonUses;
            dimLureSlimeUses = BlockNethermind.defaultSlimeUses;
            dimLureBatUses = BlockNethermind.defaultBatUses;
            dimLureWispUses = BlockNethermind.defaultWispUses;
        }

        netherSpireChance = loadInteger(
            "worldgen_nether_spire_chance",
            "World gen: Chance that a Nether Spire is generated where possible. 0 disables. 1 is 100% of the time. Higher number means less likely. Default "
                + netherSpireChance
                + ".",
            netherSpireChance
        );
        focusUpgradeGratis = loadInteger("focus_id_gratis", null, focusUpgradeGratis);
        focusUpgradeTransposing = loadInteger("focus_id_transposing", null, focusUpgradeTransposing);
        focusUpgradeStorage = loadInteger("focus_id_storage", null, focusUpgradeStorage);
        wailaXPStone = loadBoolean("waila_soul_stone", "Use Waila to add information about how much experience a soul stone is holding.", wailaXPStone);
        wailaBrainInAJar = loadBoolean(
            "waila_brain_jar", "Use Waila to add information about how much experience a brain in a jar is holding.", wailaBrainInAJar
        );
        botaniaLooniumBlacklistPhialXP = loadBoolean(
            "botania_blacklist_phial_xp", "Prevent Botania's Loonium from generating phials of enchanter's concentrate", botaniaLooniumBlacklistPhialXP
        );
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.CLIENT) {
            config.addCustomCategoryComment("keys", "Default keybindings. Can still be adjusted by the player in-game.");
            defaultKeyCraft = loadKeyBinding("focus_craft", "Wand Focus: Crafting - open GUI", defaultKeyCraft);
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void loadServerConfig() {
        AutomagyAPI.invContainerBlacklist.clear();
        String[] arr = loadStringArray(
            "general",
            "inventarium_container_blacklist",
            "A list of any block names for containers that the Inventarium / Unseen Scribes should ignore.",
            new String[]{Block.blockRegistry.getNameForObject(ModBlocks.tallyBlock)}
        );
        if (arr != null && arr.length > 0) {
            for (String s : arr) {
                try {
                    Block b = Block.getBlockFromName(s);
                    if (b != null) {
                        AutomagyAPI.invContainerBlacklist.add(b);
                    } else {
                        FMLLog.warning("[Automagy] Ignoring invalid Inventarium blacklist config entry: " + s);
                    }
                } catch (Exception var6) {
                    FMLLog.warning("[Automagy] Ignoring invalid Inventarium blacklist config entry: " + s);
                }
            }
        }

        populateBookshelvesList();
        amnesiacDrainItems.clear();
        arr = loadStringArray(
            "general",
            "amnesiac_drain_item",
            "A list of any item names that the Amnesiac's Stone should convert into XP along with the XP value and (optionally) replacement items (e.g. empty bottles). Format: \"<String Mod ID>:<String Item ID>[,<Integer Metadata>]=<Integer XP>[,<String Mod ID>:<String Item ID>[,<Integer Metadata>]]\". Put each of these on a separate line. The item indicated before the equals sign will turn into the amount of XP indicated after it. If another item is indicated after the XP amount, then it will be dropped in the first's place. Alternatively, use a \"*\" for the second item to automatically use the first's container item.",
            new String[]{
                Item.itemRegistry.getNameForObject(Items.experience_bottle)
                    + "="
                    + BlockAmnesiac.XP_PER_BOTTLE_DEFAULT
                    + ","
                    + Item.itemRegistry.getNameForObject(Items.glass_bottle),
                Item.itemRegistry.getNameForObject(ModItems.phialXP) + ",0=" + ItemPhialXP.DEFAULT_XP + ",*",
                "TConstruct:oreBerries,5=13"
            }
        );
        if (arr != null && arr.length > 0) {
            for (String s : arr) {
                AutomagyConfig.AmnesiacDrainData data = processAmnesiacDrainLine(s);
                if (data == null) {
                    FMLLog.warning("[Automagy] Ignoring invalid config entry in amnesiac_drain_item: " + s);
                } else {
                    amnesiacDrainItems.put(data.target, data.drainResult);
                }
            }
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void populateBookshelvesList() {
        AutomagyAPI.invOtherBookshelves.clear();
        String[] arr = loadStringArray(
            "general",
            "inventarium_other_bookshelves",
            "A list of any block names that the Inventarium should treat as bookshelves that can increase its maximum slot count.",
            new String[]{"Natura:Natura.bookshelf"}
        );
        if (arr != null && arr.length > 0) {
            for (String s : arr) {
                try {
                    Block b = Block.getBlockFromName(s);
                    if (b != null) {
                        AutomagyAPI.invOtherBookshelves.add(b);
                    } else {
                        FMLLog.warning("[Automagy] Ignoring invalid Inventarium bookshelf config entry: " + s);
                    }
                } catch (Exception var6) {
                    FMLLog.warning("[Automagy] Ignoring invalid Inventarium bookshelf config entry: " + s);
                }
            }
        }
    }

    public static boolean loadBoolean(String category, String name, String desc, boolean def) {
        Property p = config.get(category, name, def);
        if (desc != null) {
            p.comment = desc;
        }

        return p.getBoolean(def);
    }

    public static boolean loadBoolean(String name, String desc, boolean def) {
        return loadBoolean("general", name, desc, def);
    }

    public static int loadInteger(String category, String name, String desc, int def) {
        Property p = config.get(category, name, def);
        if (desc != null) {
            p.comment = desc;
        }

        return p.getInt(def);
    }

    public static int loadInteger(String name, String desc, int def) {
        return loadInteger("general", name, desc, def);
    }

    public static String loadString(String category, String name, String desc, String def) {
        Property p = config.get(category, name, def);
        if (desc != null) {
            p.comment = desc;
        }

        return p.getString();
    }

    public static String[] loadStringArray(String category, String name, String desc, String[] def) {
        Property p = config.get(category, name, def);
        if (desc != null) {
            p.comment = desc;
        }

        return p.getStringList();
    }

    public static AspectList loadAspectList(String category, String name, String desc, AspectList def) {
        Property p = config.get(category, name, getStringsFromAspects(def));
        if (desc != null) {
            p.comment = desc;
        }

        return getAspectsFromStrings(p.getStringList());
    }

    public static int loadKeyBinding(String name, String desc, int def) {
        String s = loadString("keys", name, desc, Automagy.proxy.getKeyName(def));
        return Automagy.proxy.getKeyIndex(s);
    }

    public static String[] getStringsFromAspects(AspectList aspects) {
        ArrayList<String> list = new ArrayList<>();

        for (Aspect aspect : aspects.getAspects()) {
            String s = aspect.getName() + ":" + aspects.getAmount(aspect);
            list.add(s.toLowerCase(Locale.ENGLISH));
        }

        return list.toArray(new String[list.size()]);
    }

    public static AspectList getAspectsFromStrings(String[] strings) {
        AspectList aspects = new AspectList();

        for (String s : strings) {
            if (!s.contains(":")) {
                throw new IllegalArgumentException(
                    "Automagy was not configured properly. Expected a string in format of \"ASPECT_TAG:INTEGER\", e.g. \"terra:5\". Got \""
                        + s
                        + "\". Check your Automagy config file."
                );
            }

            String[] parts = s.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                    "Automagy was not configured properly. Expected a string in format of \"ASPECT_TAG:INTEGER\", e.g. \"terra:5\". Got \""
                        + s
                        + "\". Check your Automagy config file."
                );
            }

            Aspect a = Aspect.getAspect(parts[0].toLowerCase(Locale.ENGLISH));
            if (a == null) {
                throw new IllegalArgumentException(
                    "Automagy was not configured properly. Expected a string in format of \"ASPECT_TAG:INTEGER\", e.g. \"terra:5\". Got \""
                        + s
                        + "\". Check your Automagy config file."
                );
            }

            int num;
            try {
                num = Integer.parseInt(parts[1]);
            } catch (Exception var10) {
                throw new IllegalArgumentException(
                    "Automagy was not configured properly. Expected a string in format of \"ASPECT_TAG:INTEGER\", e.g. \"terra:5\". Got \""
                        + s
                        + "\". Check your Automagy config file."
                );
            }

            aspects.merge(a, num);
        }

        return aspects;
    }

    public static int getRealWaterBottleAmount() {
        if (realWaterBottleAmount < 0) {
            ItemStack waterBottle = new ItemStack(Items.potionitem);
            FluidStack waterBottleLiquid = FluidContainerRegistry.getFluidForFilledItem(waterBottle);
            realWaterBottleAmount = waterBottleLiquid == null ? 0 : (waterBottleLiquid.amount >= 0 ? waterBottleLiquid.amount : 0);
        }

        return realWaterBottleAmount;
    }

    public static TileEntityAmnesiacStone.ItemDrainResult getAmnesiacItemDrainResult(ItemStack stack) {
        return null;
    }

    private static AutomagyConfig.AmnesiacDrainData processAmnesiacDrainLine(String s) {
        AutomagyConfig.AmnesiacDrainData data = new AutomagyConfig.AmnesiacDrainData();
        String[] parts = s.split("=");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                "Automagy was not configured properly. Invalid line in amnesiac_drain_item. Check your Automagy config file. Malformed section: \"" + s + "\""
            );
        } else {
            ItemStack target = getItemFromStringWithOptionalMetadata(parts[0], "amnesiac_drain_item");
            if (target == null) {
                return null;
            } else {
                data.target = new HashableItemWithoutSize(target);
                String[] resultParts = parts[1].split(",", 2);
                if (resultParts.length > 1) {
                    if (resultParts[1].equals("*")) {
                        data.drainResult.replacementUseContainer = true;
                    } else {
                        ItemStack leftover = getItemFromStringWithOptionalMetadata(resultParts[1], "amnesiac_drain_item");
                        if (leftover == null) {
                            throw new IllegalArgumentException(
                                "Automagy was not configured properly. Invalid line in amnesiac_drain_item. Check your Automagy config file. Malformed section: \""
                                    + resultParts[1]
                                    + "\""
                            );
                        }

                        data.drainResult.replacement = leftover;
                    }
                }

                int xp;
                try {
                    xp = Integer.parseInt(resultParts[0]);
                } catch (Exception var7) {
                    throw new IllegalArgumentException(
                        "Automagy was not configured properly. Invalid line in amnesiac_drain_item. Could not get XP value. Check your Automagy config file. Malformed section: \""
                            + parts[1]
                            + "\""
                    );
                }

                data.drainResult.xp = xp;
                return data;
            }
        }
    }

    private static ItemStack getItemFromStringWithOptionalMetadata(String s, String errSection) {
        String[] itemParts = s.split(",");
        if (itemParts.length > 2) {
            throw new IllegalArgumentException(
                "Automagy was not configured properly. Invalid line in " + errSection + ". Check your Automagy config file. Malformed section: \"" + s + "\""
            );
        } else {
            int metadata = 32767;
            if (itemParts.length == 2) {
                try {
                    metadata = Integer.parseInt(itemParts[1]);
                } catch (Exception var8) {
                    throw new IllegalArgumentException(
                        "Automagy was not configured properly. Invalid line in "
                            + errSection
                            + ". Check your Automagy config file. Malformed section: \""
                            + s
                            + "\""
                    );
                }
            }

            ItemStack stack = null;
            String[] nameParts = itemParts[0].split(":", 2);
            Item item = GameRegistry.findItem(nameParts[0], nameParts[1]);
            if (item == null) {
                Block block = GameRegistry.findBlock(nameParts[0], nameParts[1]);
                if (block != null) {
                    stack = new ItemStack(block, 1, metadata);
                }
            } else {
                stack = new ItemStack(item, 1, metadata);
            }

            return stack;
        }
    }

    private static class AmnesiacDrainData {
        public HashableItemWithoutSize target;
        public TileEntityAmnesiacStone.ItemDrainResult drainResult = new TileEntityAmnesiacStone.ItemDrainResult();

        private AmnesiacDrainData() {
        }
    }
}
