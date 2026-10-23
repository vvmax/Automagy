package tuhljin.automagy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.config.ModRecipes;
import tuhljin.automagy.config.ModResearchItems;
import tuhljin.automagy.entities.ModEntities;
import tuhljin.automagy.gui.AutomagyGUIHandler;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.lib.AutomagyLoot;
import tuhljin.automagy.lib.CreativeTabAutomagy;
import tuhljin.automagy.lib.ProxyCommon;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.compat.CompatibilityManager;
import tuhljin.automagy.lib.events.AutomagyEventHandler;
import tuhljin.automagy.lib.events.CommandAutomagy;
import tuhljin.automagy.lib.events.WandEventManager;
import tuhljin.automagy.lib.world.AutomagyWorldGenerator;
import tuhljin.automagy.network.PacketHandler;
import tuhljin.automagy.tiles.ModTiles;

@Mod(
    modid = "Automagy",
    name = "Automagy",
    version = "0.28.2",
    dependencies = "required-after:Thaumcraft@[4.2.3.5,);after:Waila;after:MineFactoryReloaded;after:MineFactoryReloaded|CompatThaumcraft"
)
public class Automagy {
    @SidedProxy(
        clientSide = "tuhljin.automagy.lib.ProxyClient",
        serverSide = "tuhljin.automagy.lib.ProxyCommon"
    )
    public static ProxyCommon proxy;
    @Instance("Automagy")
    public static Automagy instance;
    public static AutomagyWorldGenerator worldGen = new AutomagyWorldGenerator();
    WandEventManager wandManager;
    public static CreativeTabs creativeTab = new CreativeTabAutomagy(CreativeTabs.getNextID(), "Automagy");

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        AutomagyConfig.load(event.getSuggestedConfigurationFile());
        PacketHandler.init();
        ModItems.init();
        ModBlocks.init();
        ModItems.initFluidContainers();
        GameRegistry.registerWorldGenerator(worldGen, 1);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
        ModTiles.register();
        ModEntities.register();
        CompatibilityManager.init();
        ModRecipes.registerRecipesAndAspectBreakdown();
        ModRecipes.registerInfusionEnchantmentRecipes();
        ThaumcraftExtension.modComms();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new AutomagyGUIHandler());
        MinecraftForge.EVENT_BUS.register(new AutomagyEventHandler());
        FMLCommonHandler.instance().bus().register(new AutomagyEventHandler());
        proxy.registerKeyBindings();
        FMLLog.info("[Automagy] Warning: Humorous one-liner not found.");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ModResearchItems.registerResearch();
        CompatibilityManager.postInit();
        AutomagyLoot.addLoot();
        this.wandManager = new WandEventManager();
    }

    @EventHandler
    public static void load(FMLLoadCompleteEvent e) throws Throwable {
        CompatibilityManager.onLoadComplete();
        ModEntities.registerAspects();
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        AutomagyConfig.loadServerConfig();
        event.registerServerCommand(new CommandAutomagy());
    }
}
