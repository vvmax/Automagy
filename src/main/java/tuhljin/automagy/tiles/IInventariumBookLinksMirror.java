package tuhljin.automagy.tiles;

import net.minecraft.inventory.IInventory;

public interface IInventariumBookLinksMirror extends IInventariumBook {
    boolean hasInputEye();

    boolean hasOutputEye();

    boolean isInputMirrorInRange();

    boolean isOutputMirrorInRange();

    IMirrorVariant getInputMirror();

    IMirrorVariant getOutputMirror();

    IInventory getMirrorSettingsInventory();

    boolean isTrackingDestinations();
}
