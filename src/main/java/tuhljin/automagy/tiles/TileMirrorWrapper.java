package tuhljin.automagy.tiles;

import java.lang.ref.WeakReference;
import net.minecraft.item.ItemStack;
import thaumcraft.common.tiles.TileMirror;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

public class TileMirrorWrapper implements IMirrorVariant {
    private WeakReference<TileMirror> mirrorRef;

    public TileMirrorWrapper(TileMirror mirror) {
        this.mirrorRef = new WeakReference<>(mirror);
    }

    @Override
    public WorldSpecificCoordinates getDestinationMirrorCoordinates() {
        TileMirror mirror = this.mirrorRef.get();
        return mirror == null ? null : ThaumcraftExtension.getLinkedMirrorCoordinates(mirror);
    }

    @Override
    public boolean isLinked() {
        TileMirror mirror = this.mirrorRef.get();
        return mirror == null ? false : mirror.linked;
    }

    @Override
    public int addStackToOutput(ItemStack stack) {
        TileMirror mirror = this.mirrorRef.get();
        if (mirror != null) {
            mirror.addStack(stack);
            return 0;
        } else {
            return stack == null ? 0 : stack.stackSize;
        }
    }

    @Override
    public void markMirrorDirty() {
        TileMirror mirror = this.mirrorRef.get();
        if (mirror != null) {
            mirror.markDirty();
        }
    }
}
