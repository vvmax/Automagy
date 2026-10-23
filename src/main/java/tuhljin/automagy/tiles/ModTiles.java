package tuhljin.automagy.tiles;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModTiles {
    public static void register() {
        GameRegistry.registerTileEntity(TileEntityRedcrystal.class, "tileEntityRedcrystal");
        GameRegistry.registerTileEntity(TileEntityRedcrystalMerc.class, "tileEntityRedcrystalMerc");
        GameRegistry.registerTileEntity(TileEntityRemoteComparator.class, "tileEntityRemoteComparator");
        GameRegistry.registerTileEntity(TileEntityVisReader.class, "tileEntityVisReader");
        GameRegistry.registerTileEntity(TileEntityTally.class, "tileEntityTally");
        GameRegistry.registerTileEntity(TileEntityTallyWorld.class, "tileEntityTallyWorld");
        GameRegistry.registerTileEntity(TileEntityTallyDrops.class, "tileEntityTallyBlock");
        GameRegistry.registerTileEntity(TileEntityHourglass.class, "tileEntityHourglass");
        GameRegistry.registerTileEntity(TileEntityHourglassMagic.class, "tileEntityHourglassMagic");
        GameRegistry.registerTileEntity(TileEntityGreedyChest.class, "tileEntityGreedyChest");
        GameRegistry.registerTileEntity(TileEntityEagerChest.class, "tileEntityEagerChest");
        GameRegistry.registerTileEntity(TileEntityTenaciousChest.class, "tileEntityTenaciousChest");
        GameRegistry.registerTileEntity(TileEntityTorchInversion.class, "tileEntityTorchInversion");
        GameRegistry.registerTileEntity(TileEntityMawHungry.class, "tileEntityHungryMaw");
        GameRegistry.registerTileEntity(TileEntityMawFinical.class, "tileEntityFinicalMaw");
        GameRegistry.registerTileEntity(TileEntityGolemInhibitor.class, "tileEntityGolemInhibitor");
        GameRegistry.registerTileEntity(TileEntityEntitySignal.class, "tileEntityEntitySignal");
        GameRegistry.registerTileEntity(TileEntityEssentiaLocus.class, "tileEntityEssentiaLocus");
        GameRegistry.registerTileEntity(TileEntityInventarium.class, "tileEntityInventarium");
        GameRegistry.registerTileEntity(TileEntityUnseenScribe.class, "tileEntityUnseenScribe");
        GameRegistry.registerTileEntity(TileEntityScribePointer.class, "tileEntityScribePointer");
        GameRegistry.registerTileEntity(TileEntityGolemTaskmaster.class, "tileEntityGolemTaskmaster");
        GameRegistry.registerTileEntity(TileEntityRequisitionTome.class, "tileEntityRequisitionTome");
        GameRegistry.registerTileEntity(TileEntityThaumostaticRefuelerSpire.class, "tileEntityThaumostaticRefuelerSpire");
        GameRegistry.registerTileEntity(TileEntityThaumostaticController.class, "tileEntityThaumostaticController");
        GameRegistry.registerTileEntity(TileEntityThaumostaticPylon.class, "tileEntityThaumostaticPylon");
        GameRegistry.registerTileEntity(TileEntityGolemWorkbench.class, "tileEntityGolemWorkbench");
        GameRegistry.registerTileEntity(TileEntityThirstyTank.class, "tileEntityThirstyTank");
        GameRegistry.registerTileEntity(TileEntityBoiler.class, "tileEntityBoiler");
        GameRegistry.registerTileEntity(TileEntityNethermind.class, "tileEntityNethermind");
        GameRegistry.registerTileEntity(TileEntityAmnesiacStone.class, "tileEntityAmnesiacStone");
        GameRegistry.registerTileEntity(TileEntityXPStone.class, "tileEntityXPStone");
        GameRegistry.registerTileEntity(TileEntityMobLure.class, "tileEntityMobLure");
        GameRegistry.registerTileEntity(TileEntityDimensionLure.class, "tileEntityDimensionLure");
        GameRegistry.registerTileEntity(TileEntityJarCreative.class, "tileEntityCreativeJar");
        GameRegistry.registerTileEntity(TileEntityJarXP.class, "tileEntityXPJar");
        GameRegistry.registerTileEntity(TileEntityMirrorInput.class, "tileEntityMirrorInput");
        GameRegistry.registerTileEntity(TileEntityMirrorMultiDest.class, "tileEntityMirrorMultiDest");
        GameRegistry.registerTileEntity(TileEntitySpecialProcess.class, "tileEntitySpecialProcess");
    }
}
