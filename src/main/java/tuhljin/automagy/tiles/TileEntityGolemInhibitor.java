package tuhljin.automagy.tiles;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.golems.EntityGolemBase;
import tuhljin.automagy.entities.GolemLink;
import tuhljin.automagy.lib.ThaumcraftExtension;

public class TileEntityGolemInhibitor extends ModTileEntity implements IGolemLinkableDevice {
    public static int rangeIncrements = 5;
    public static int maxRangeIncrementors = 4;
    public static int MAX_LINKS = 6;
    public boolean matchOmitsGolem = true;
    public int numLinksSeen = 0;
    private String linkName = null;
    public static int rotationSpeedFactor = 3240;
    public static double clientRenderFloatingTop = 0.3;
    public int clientRenderRotationHelper = 0;
    public double clientRenderFloatingDistance = 0.0;
    private String linkID = null;

    public boolean toggleMatchOmitsGolem() {
        this.matchOmitsGolem = !this.matchOmitsGolem;
        this.markDirty();
        return this.matchOmitsGolem;
    }

    public boolean isActive() {
        return ThaumcraftExtension.blockIsFetter(this.worldObj, this.xCoord, this.yCoord - 1, this.zCoord, true);
    }

    public int getRange() {
        int count = 0;

        while (ThaumcraftExtension.blockIsFetter(this.worldObj, this.xCoord, this.yCoord - 1 - count, this.zCoord, true)) {
            if (++count == maxRangeIncrementors) {
                break;
            }
        }

        return count * rangeIncrements;
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        int linksSeen = 0;
        boolean active = this.isActive();
        if (active) {
            int grabRange = this.getRange();
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                this.xCoord - grabRange,
                this.yCoord - 1.5,
                this.zCoord - grabRange,
                this.xCoord + grabRange + 1,
                this.yCoord + grabRange,
                this.zCoord + grabRange + 1
            );

            for (Entity entity : this.worldObj.getEntitiesWithinAABB(EntityGolemBase.class, box)) {
                EntityGolemBase golem = (EntityGolemBase)entity;
                if (this.worldObj.isRemote) {
                }

                if (!golem.inactive) {
                    if (linksSeen < MAX_LINKS && GolemLink.golemLinkedTo(golem, this)) {
                        linksSeen++;
                        if (!this.matchOmitsGolem) {
                            golem.inactive = true;
                        }
                    } else if (this.matchOmitsGolem) {
                        golem.inactive = true;
                    }
                }
            }
        }

        this.numLinksSeen = linksSeen;
        if (this.worldObj.isRemote) {
            if (!active) {
                if (this.clientRenderRotationHelper != 0) {
                    if (this.clientRenderRotationHelper > rotationSpeedFactor / 2) {
                        this.clientRenderRotationHelper += 16;
                    } else {
                        this.clientRenderRotationHelper -= 16;
                    }

                    if (this.clientRenderRotationHelper >= rotationSpeedFactor) {
                        this.clientRenderRotationHelper = this.clientRenderRotationHelper - rotationSpeedFactor;
                    }

                    if (this.clientRenderRotationHelper <= 16) {
                        this.clientRenderRotationHelper = 0;
                    }
                } else if (this.clientRenderFloatingDistance > 0.0) {
                    this.clientRenderFloatingDistance -= 0.005;
                    if (this.clientRenderFloatingDistance < 0.01) {
                        this.clientRenderFloatingDistance = 0.0;
                    }
                }

                return;
            }

            if (this.clientRenderFloatingDistance < clientRenderFloatingTop) {
                this.clientRenderFloatingDistance += 0.008;
            }

            if (this.clientRenderFloatingDistance > clientRenderFloatingTop / 2.0) {
                this.clientRenderRotationHelper += 5;
                if (this.clientRenderRotationHelper >= rotationSpeedFactor) {
                    this.clientRenderRotationHelper = this.clientRenderRotationHelper - rotationSpeedFactor;
                }
            }
        }
    }

    @Override
    public String getGolemLinkID() {
        if (this.linkID == null) {
            if (this.linkName == null || this.linkName.isEmpty()) {
                if (this.worldObj.isRemote) {
                    return null;
                }

                int t = (int)(System.currentTimeMillis() / 1000L);
                this.linkName = Integer.toHexString(t);
                this.markDirty();
            }

            this.linkID = this.linkName + ":" + this.worldObj.provider.dimensionId + "," + this.xCoord + "," + this.yCoord + "," + this.zCoord;
        }

        return this.linkID;
    }

    @Override
    public void forgetGolemLinkID() {
        int t = (int)(System.currentTimeMillis() / 1000L);
        this.linkName = Integer.toHexString(t);
        this.linkID = null;
        this.markDirty();
        if (this.numLinksSeen > 0) {
            Thaumcraft.proxy
                .sparkle(
                    this.xCoord + 0.2F + this.worldObj.rand.nextFloat() * 0.6F,
                    this.yCoord + 0.2F,
                    this.zCoord + 0.2F + this.worldObj.rand.nextFloat() * 0.6F,
                    1.0F,
                    5,
                    -0.1F
                );
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setBoolean("matchOmitsGolem", this.matchOmitsGolem);
        if (this.linkName != null || this.getGolemLinkID() != null) {
            nbttagcompound.setString("linkName", this.linkName);
        }
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.matchOmitsGolem = nbttagcompound.getBoolean("matchOmitsGolem");
        String newLinkName = nbttagcompound.getString("linkName");
        if (!newLinkName.equals(this.linkName)) {
            this.linkName = newLinkName;
            this.linkID = null;
        }
    }
}
