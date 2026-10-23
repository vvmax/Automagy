package tuhljin.automagy.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler {
    public static int DEFAULT_PACKET_RANGE = 20;
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("Automagy".toLowerCase());

    public static void init() {
        int id = 0;
        INSTANCE.registerMessage(MessageGUIVisReader.class, MessageGUIVisReader.class, id++, Side.SERVER);
        INSTANCE.registerMessage(MessageGUIHourglassMagic.class, MessageGUIHourglassMagic.class, id++, Side.SERVER);
        INSTANCE.registerMessage(MessageGUIGreedyChestConfig.class, MessageGUIGreedyChestConfig.class, id++, Side.SERVER);
        INSTANCE.registerMessage(MessageGUIEagerChestConfig.class, MessageGUIEagerChestConfig.class, id++, Side.SERVER);
        INSTANCE.registerMessage(MessageGUIFilterPaper.class, MessageGUIFilterPaper.class, id++, Side.SERVER);
        INSTANCE.registerMessage(MessageGUIEntitySignal.class, MessageGUIEntitySignal.class, id++, Side.SERVER);
        INSTANCE.registerMessage(MessageGUITallyBlock.class, MessageGUITallyBlock.class, id++, Side.SERVER);
        INSTANCE.registerMessage(MessageGUIGolemWorkbenchRecipe.class, MessageGUIGolemWorkbenchRecipe.class, id++, Side.SERVER);
        INSTANCE.registerMessage(MessageGUIInventariumRequest.class, MessageGUIInventariumRequest.class, id++, Side.SERVER);
        INSTANCE.registerMessage(MessageGUIInventariumBookPref.class, MessageGUIInventariumBookPref.class, id++, Side.SERVER);
        INSTANCE.registerMessage(MessageKeyPressed.class, MessageKeyPressed.class, id++, Side.SERVER);
        INSTANCE.registerMessage(MessageHourglassFlipped.class, MessageHourglassFlipped.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(MessageGreedyChestStole.class, MessageGreedyChestStole.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(MessageGUIEagerChestClientUpdate.class, MessageGUIEagerChestClientUpdate.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(MessageThaumostaticSparks.class, MessageThaumostaticSparks.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(MessageBoilerVenting.class, MessageBoilerVenting.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(MessageParticles.class, MessageParticles.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(MessageSound.class, MessageSound.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(MessageFloatXYZ.class, MessageFloatXYZ.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(MessageResetScannedObjects.class, MessageResetScannedObjects.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(MessageItemStacksList.class, MessageItemStacksList.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(MessageItemStacksRequested.class, MessageItemStacksRequested.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(MessageItemStacksNeedPacket.class, MessageItemStacksNeedPacket.class, id++, Side.SERVER);
        INSTANCE.registerMessage(MessageClientSync.class, MessageClientSync.class, id++, Side.CLIENT);
    }
}
