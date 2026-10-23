package tuhljin.automagy.lib;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXSparkle;
import thaumcraft.client.fx.particles.FXWisp;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.tiles.TileAlembic;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.entities.EntityAvaricePearl;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.events.AutomagyRenderEventHandler;
import tuhljin.automagy.lib.events.ClientEventHandler;
import tuhljin.automagy.particles.FXDropXP;
import tuhljin.automagy.renderers.BlockBoilerRenderer;
import tuhljin.automagy.renderers.BlockEssentiaLocusRenderer;
import tuhljin.automagy.renderers.BlockGolemInhibitorRenderer;
import tuhljin.automagy.renderers.BlockGolemTaskmasterRenderer;
import tuhljin.automagy.renderers.BlockGolemWorkbenchRenderer;
import tuhljin.automagy.renderers.BlockHourglassRenderer;
import tuhljin.automagy.renderers.BlockHungryMawRenderer;
import tuhljin.automagy.renderers.BlockInventariumRenderer;
import tuhljin.automagy.renderers.BlockJarXPRenderer;
import tuhljin.automagy.renderers.BlockMirrorAltRenderer;
import tuhljin.automagy.renderers.BlockModChestRenderer;
import tuhljin.automagy.renderers.BlockRedcrystalMercRenderer;
import tuhljin.automagy.renderers.BlockRedcrystalRenderer;
import tuhljin.automagy.renderers.BlockRemoteComparatorRenderer;
import tuhljin.automagy.renderers.BlockRequisitionTomeRenderer;
import tuhljin.automagy.renderers.BlockScribePointerRenderer;
import tuhljin.automagy.renderers.BlockThaumostaticPylonRenderer;
import tuhljin.automagy.renderers.BlockThaumostaticRefuelerSpireRenderer;
import tuhljin.automagy.renderers.BlockThirstyTankRenderer;
import tuhljin.automagy.renderers.BlockUnseenScribeRenderer;
import tuhljin.automagy.renderers.ItemBlockBasicRenderer;
import tuhljin.automagy.renderers.ModChestRenderer;
import tuhljin.automagy.renderers.Render3DItemCrystalBrainBlock;
import tuhljin.automagy.renderers.Render3DItemGolemInhibitor;
import tuhljin.automagy.renderers.Render3DItemGolemTaskmaster;
import tuhljin.automagy.renderers.Render3DItemHourglass;
import tuhljin.automagy.renderers.Render3DItemJarXP;
import tuhljin.automagy.renderers.Render3DItemRedcrystal;
import tuhljin.automagy.renderers.Render3DItemRedcrystalMerc;
import tuhljin.automagy.renderers.Render3DItemRemoteComparator;
import tuhljin.automagy.renderers.Render3DItemThirstyTank;
import tuhljin.automagy.renderers.RenderBlockGlowOverlay;
import tuhljin.automagy.renderers.RenderItemHorizontal;
import tuhljin.automagy.renderers.RenderItemProjectileWithEntityMetadata;
import tuhljin.automagy.renderers.RenderTileGlowOverlayMobLure;
import tuhljin.automagy.renderers.TileAlembicRendererOverride;
import tuhljin.automagy.renderers.TileEntityGreedyChestRenderer;
import tuhljin.automagy.tiles.TileEntityBoiler;
import tuhljin.automagy.tiles.TileEntityChestBase;
import tuhljin.automagy.tiles.TileEntityDimensionLure;
import tuhljin.automagy.tiles.TileEntityEagerChest;
import tuhljin.automagy.tiles.TileEntityEssentiaLocus;
import tuhljin.automagy.tiles.TileEntityGolemInhibitor;
import tuhljin.automagy.tiles.TileEntityGolemTaskmaster;
import tuhljin.automagy.tiles.TileEntityGolemWorkbench;
import tuhljin.automagy.tiles.TileEntityGreedyChest;
import tuhljin.automagy.tiles.TileEntityHourglass;
import tuhljin.automagy.tiles.TileEntityInventarium;
import tuhljin.automagy.tiles.TileEntityJarXP;
import tuhljin.automagy.tiles.TileEntityMawHungry;
import tuhljin.automagy.tiles.TileEntityMirrorMultiDest;
import tuhljin.automagy.tiles.TileEntityMirrorWithEye;
import tuhljin.automagy.tiles.TileEntityMobLure;
import tuhljin.automagy.tiles.TileEntityRedcrystal;
import tuhljin.automagy.tiles.TileEntityRedcrystalMerc;
import tuhljin.automagy.tiles.TileEntityRemoteComparator;
import tuhljin.automagy.tiles.TileEntityRequisitionTome;
import tuhljin.automagy.tiles.TileEntityScribePointer;
import tuhljin.automagy.tiles.TileEntityTenaciousChest;
import tuhljin.automagy.tiles.TileEntityThaumostaticController;
import tuhljin.automagy.tiles.TileEntityThaumostaticPylon;
import tuhljin.automagy.tiles.TileEntityThaumostaticRefuelerSpire;
import tuhljin.automagy.tiles.TileEntityThirstyTank;
import tuhljin.automagy.tiles.TileEntityUnseenScribe;
import tuhljin.automagy.tiles.TileEntityXPStone;

public class ProxyClient extends ProxyCommon {
    @Override
    public void init() {
        super.init();
        if (AutomagyConfig.versionChecking) {
            new VersionChecker();
        }

        FMLCommonHandler.instance().bus().register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new AutomagyRenderEventHandler());
        RenderBlockGlowOverlay.renderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(RenderBlockGlowOverlay.renderID, new RenderBlockGlowOverlay());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRedcrystalMerc.class, new BlockRedcrystalMercRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRedcrystal.class, new BlockRedcrystalRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRemoteComparator.class, new BlockRemoteComparatorRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHourglass.class, new BlockHourglassRenderer());
        References.renderBlockBoiler = RenderingRegistry.getNextAvailableRenderId();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBoiler.class, new BlockBoilerRenderer());
        RenderingRegistry.registerBlockHandler(new ItemBlockBasicRenderer(TileEntityBoiler.class, References.renderBlockBoiler));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMawHungry.class, new BlockHungryMawRenderer());
        BlockMirrorAltRenderer BMAR = new BlockMirrorAltRenderer();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMirrorWithEye.class, BMAR);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMirrorMultiDest.class, BMAR);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGolemInhibitor.class, new BlockGolemInhibitorRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGolemTaskmaster.class, new BlockGolemTaskmasterRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEssentiaLocus.class, new BlockEssentiaLocusRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityInventarium.class, new BlockInventariumRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGreedyChest.class, new TileEntityGreedyChestRenderer());
        References.renderBlockGreedyChestRI = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new BlockModChestRenderer(TileEntityGreedyChest.class, References.renderBlockGreedyChestRI));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEagerChest.class, new ModChestRenderer("eagerchest"));
        References.renderBlockEagerChestRI = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new BlockModChestRenderer(TileEntityEagerChest.class, References.renderBlockEagerChestRI));
        References.renderBlockTenaciousChestRI = this.registerChestRenderer(TileEntityTenaciousChest.class, "tenaciousChest");
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityUnseenScribe.class, new BlockUnseenScribeRenderer());
        References.renderBlockUnseenScribeRI = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new ItemBlockBasicRenderer(TileEntityUnseenScribe.class, References.renderBlockUnseenScribeRI));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityScribePointer.class, new BlockScribePointerRenderer());
        References.renderBlockUnseenScribePointerRI = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new ItemBlockBasicRenderer(TileEntityScribePointer.class, References.renderBlockUnseenScribePointerRI));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRequisitionTome.class, new BlockRequisitionTomeRenderer());
        References.renderBlockRequisitionTomeRI = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new ItemBlockBasicRenderer(TileEntityRequisitionTome.class, References.renderBlockRequisitionTomeRI));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGolemWorkbench.class, new BlockGolemWorkbenchRenderer());
        References.renderBlockGolemWorkbench = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new ItemBlockBasicRenderer(TileEntityGolemWorkbench.class, References.renderBlockGolemWorkbench));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThaumostaticRefuelerSpire.class, new BlockThaumostaticRefuelerSpireRenderer());
        References.renderBlockThaumostaticRefueler = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(
            new ItemBlockBasicRenderer(TileEntityThaumostaticRefuelerSpire.class, References.renderBlockThaumostaticRefueler)
        );
        BlockThaumostaticPylonRenderer bTPR = new BlockThaumostaticPylonRenderer();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThaumostaticPylon.class, bTPR);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThaumostaticController.class, bTPR);
        References.renderBlockThaumostaticPylon = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new ItemBlockBasicRenderer(TileEntityThaumostaticPylon.class, References.renderBlockThaumostaticPylon));
        RenderingRegistry.registerBlockHandler(new ItemBlockBasicRenderer(TileEntityThaumostaticController.class, References.renderBlockThaumostaticPylon));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThirstyTank.class, new BlockThirstyTankRenderer());
        References.renderBlockThirstyTank = RenderingRegistry.getNextAvailableRenderId();
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.thirstyTank), new Render3DItemThirstyTank());
        References.renderBlockMobLure = RenderingRegistry.getNextAvailableRenderId();
        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityMobLure.class, new RenderTileGlowOverlayMobLure(ModBlocks.mobLure, References.renderBlockMobLure)
        );
        RenderingRegistry.registerBlockHandler(new ItemBlockBasicRenderer(TileEntityMobLure.class, References.renderBlockMobLure));
        References.renderBlockDimensionLure = RenderingRegistry.getNextAvailableRenderId();
        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityDimensionLure.class, new RenderTileGlowOverlayMobLure(ModBlocks.dimensionLure, References.renderBlockDimensionLure)
        );
        RenderingRegistry.registerBlockHandler(new ItemBlockBasicRenderer(TileEntityDimensionLure.class, References.renderBlockDimensionLure));
        References.renderBlockXPStone = RenderingRegistry.getNextAvailableRenderId();
        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityXPStone.class, new RenderTileGlowOverlayMobLure(ModBlocks.xpStone, References.renderBlockXPStone)
        );
        RenderingRegistry.registerBlockHandler(new ItemBlockBasicRenderer(TileEntityXPStone.class, References.renderBlockXPStone));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityJarXP.class, new BlockJarXPRenderer());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.jarExperience), new Render3DItemJarXP());
        IItemRenderer itemRenderer = new Render3DItemRedcrystal();
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.redcrystal), itemRenderer);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.redcrystalAmp), itemRenderer);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.redcrystalDim), itemRenderer);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.redcrystalDense), itemRenderer);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.redcrystalRes), itemRenderer);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.redcrystalMerc), new Render3DItemRedcrystalMerc());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.remoteComparator), new Render3DItemRemoteComparator());
        itemRenderer = new Render3DItemHourglass();
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.hourglass), itemRenderer);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.hourglassMagic), itemRenderer);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.golemInhibitor), new Render3DItemGolemInhibitor());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.golemTaskmaster), new Render3DItemGolemTaskmaster());
        itemRenderer = new Render3DItemCrystalBrainBlock();
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.essentiaLocus), itemRenderer);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.inventariumCore), itemRenderer);
        MinecraftForgeClient.registerItemRenderer(ModItems.ingredientHorizontal, new RenderItemHorizontal());
        RenderingRegistry.registerEntityRenderingHandler(EntityAvaricePearl.class, new RenderItemProjectileWithEntityMetadata(ModItems.avaricePearl));
        ClientRegistry.bindTileEntitySpecialRenderer(TileAlembic.class, new TileAlembicRendererOverride());
    }

    @Override
    public void registerKeyBindings() {
        FMLCommonHandler.instance().bus().register(new AutomagyKeyHandler());
    }

    @Override
    public void ghostlyBallStreamFX(
        World worldObj, double posX, double posY, double posZ, double posX2, double posY2, double posZ2, float size, int type, boolean shrink, float gravity
    ) {
        FXWisp ef = new FXWisp(worldObj, posX, posY, posZ, posX2, posY2, posZ2, size, type);
        ef.setGravity(gravity);
        ef.shrink = shrink;
        ef.noClip = true;
        ParticleEngine.instance.addEffect(worldObj, ef);
    }

    @Override
    public void oldBlockSparkle(World world, int x, int y, int z, int color, int count) {
        if (world.isRemote) {
            for (int a = 0; a < Thaumcraft.proxy.particleCount(count); a++) {
                FXSparkle fx = new FXSparkle(
                    world,
                    x + world.rand.nextFloat(),
                    y + world.rand.nextFloat(),
                    z + world.rand.nextFloat(),
                    1.75F,
                    color == -1 ? world.rand.nextInt(5) : color,
                    3 + world.rand.nextInt(3)
                );
                fx.setGravity(0.2F);
                ParticleEngine.instance.addEffect(world, fx);
            }
        }
    }

    @Override
    public void xpDropletFX(World world, float i, float j, float k, float r, float g, float b) {
        FXDropXP obj = new FXDropXP(world, i, j, k, r, g, b);
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(obj);
    }

    @Override
    public String getKeyName(int key) {
        return GameSettings.getKeyDisplayString(key);
    }

    @Override
    public int getKeyIndex(String key) {
        return Keyboard.getKeyIndex(key);
    }

    @Override
    public World getWorldByID(int dimensionID) {
        World world = super.getWorldByID(dimensionID);
        if (world == null) {
            world = FMLClientHandler.instance().getWorldClient();
            if (world.provider.dimensionId != dimensionID) {
                return null;
            }
        }

        return world;
    }

    private int registerChestRenderer(Class<? extends TileEntityChestBase> clazz, String texture) {
        ClientRegistry.bindTileEntitySpecialRenderer(clazz, new ModChestRenderer(texture));
        int renderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new BlockModChestRenderer(clazz, renderID));
        return renderID;
    }
}
