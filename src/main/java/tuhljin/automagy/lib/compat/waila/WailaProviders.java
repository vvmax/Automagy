package tuhljin.automagy.lib.compat.waila;

import mcp.mobius.waila.api.IWailaRegistrar;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.tiles.TileJarBrain;
import tuhljin.automagy.blocks.BlockBoiler;
import tuhljin.automagy.blocks.BlockGolemInhibitor;
import tuhljin.automagy.blocks.BlockHourglass;
import tuhljin.automagy.blocks.BlockThirstyTank;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.lib.IOrientableRedstoneConductor;
import tuhljin.automagy.tiles.IXPContainer;
import tuhljin.automagy.tiles.TileEntityJarXP;

public class WailaProviders {
    public static void wailaCallbackRegister(IWailaRegistrar registrar) {
        registrar.registerBodyProvider(new ProviderForIOrientableRedstoneConductor(), IOrientableRedstoneConductor.class);
        registrar.registerBodyProvider(new ProviderForHourglass(), BlockHourglass.class);
        registrar.registerBodyProvider(new ProviderForGolemInhibitor(), BlockGolemInhibitor.class);
        registrar.registerBodyProvider(new ProviderForTank(), BlockBoiler.class);
        registrar.registerBodyProvider(new ProviderForThirstyTank(), BlockThirstyTank.class);
        ProviderForXPContainer providerForXPContainer = new ProviderForXPContainer();
        if (AutomagyConfig.wailaXPStone) {
            registrar.registerBodyProvider(providerForXPContainer, IXPContainer.class);
        } else {
            registrar.registerBodyProvider(providerForXPContainer, TileEntityJarXP.class);
        }

        if (AutomagyConfig.wailaBrainInAJar) {
            registrar.registerBodyProvider(new ProviderForBrainInAJar(), TileJarBrain.class);
        }

        registrar.registerBodyProvider(new ProviderForGolems(), EntityGolemBase.class);
    }
}
