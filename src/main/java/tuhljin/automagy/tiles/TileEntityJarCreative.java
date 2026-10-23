package tuhljin.automagy.tiles;

import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.tiles.TileJarFillableVoid;

public class TileEntityJarCreative extends TileJarFillableVoid {
    private int refillTime = 0;

    @Override
    public void markDirty() {
        this.markDirty(true);
    }

    public void markDirty(boolean updateClients) {
        super.markDirty();
        if (updateClients) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (!this.worldObj.isRemote && this.worldObj.rand.nextBoolean()) {
            if (this.aspect != null && this.amount < this.maxAmount) {
                if (this.amount < this.maxAmount / 2) {
                    this.refillTime = this.maxAmount - this.amount;
                }

                if (this.refillTime > 0) {
                    this.amount++;
                    this.markDirty();
                }
            }

            if (this.refillTime > 0) {
                this.refillTime--;
            }
        }
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        Aspect prev = this.aspect;
        boolean b = super.takeFromContainer(tt, am);
        if (b && (this.refillTime > 0 || this.amount == 0)) {
            this.addToContainer(prev, am);
        }

        return b;
    }

    @Override
    public int getSuctionAmount(ForgeDirection loc) {
        return 0;
    }

    @Override
    public Aspect getSuctionType(ForgeDirection loc) {
        return null;
    }
}
