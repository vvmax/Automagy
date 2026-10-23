package tuhljin.automagy.api.nethermind;

import net.minecraft.block.Block;

public interface INetherRune extends INetherRuneBase {
    Block blockCreated(int var1);

    int blockCreatedMetadata(int var1);
}
