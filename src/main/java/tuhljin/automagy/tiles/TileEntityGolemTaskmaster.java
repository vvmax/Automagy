package tuhljin.automagy.tiles;

import java.util.Iterator;
import java.util.Map.Entry;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.ai.inventory.AIFillGoto;
import thaumcraft.common.entities.golems.EntityGolemBase;
import tuhljin.automagy.entities.GolemLink;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.lib.inventory.MappedItemsOrderedByTimeAdded;

public class TileEntityGolemTaskmaster extends ModTileEntity implements IGolemLinkableDevice, IInventariumMemoryUser {
    public static final int FILL_CORE = 0;
    public static int MAX_LINKS = 6;
    public static int RANGE = 5;
    public static int FREQUENCY_HASREQUESTS = 1;
    public static int FREQUENCY_NOREQUESTS = 20;
    public int numLinksSeen = 0;
    public int linkX;
    public int linkY = -1;
    public int linkZ;
    private String linkName = null;
    private int nextGolemUpdate = 0;
    public static int rotationSpeedFactor = 3240;
    public static double clientRenderFloatingTop = 0.3;
    public int clientRenderRotationHelper = 0;
    public double clientRenderFloatingDistance = 0.0;
    private String linkID = null;
    TileEntityGolemTaskmaster.EntitySelectorRequestGolem entitySelectorRequestGolem = new TileEntityGolemTaskmaster.EntitySelectorRequestGolem();

    public boolean isActive() {
        return this.linkY != -1;
    }

    public int getRange() {
        return RANGE;
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        boolean active = this.isActive();
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

        if (this.nextGolemUpdate > 0) {
            this.nextGolemUpdate--;
            if (this.nextGolemUpdate > 0) {
                return;
            }
        }

        this.nextGolemUpdate = FREQUENCY_NOREQUESTS;
        this.numLinksSeen = 0;
        if (active) {
            TileEntityInventarium teInv = null;
            MappedItemsOrderedByTimeAdded requests = null;
            if (!this.worldObj.isRemote) {
                TileEntity te = this.worldObj.getTileEntity(this.linkX, this.linkY, this.linkZ);
                if (!(te instanceof TileEntityInventarium)) {
                    this.linkY = -1;
                    this.markDirty();
                    return;
                }

                teInv = (TileEntityInventarium)te;
                requests = teInv.getRequestedItems();
                if (requests.size() > 0) {
                    this.nextGolemUpdate = FREQUENCY_HASREQUESTS;
                }
            }

            int grabRange = this.getRange();
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                this.xCoord - grabRange,
                this.yCoord - grabRange,
                this.zCoord - grabRange,
                this.xCoord + grabRange + 1,
                this.yCoord + grabRange + 1,
                this.zCoord + grabRange + 1
            );

            for (Entity entity : this.worldObj.selectEntitiesWithinAABB(EntityGolemBase.class, box, this.entitySelectorRequestGolem)) {
                EntityGolemBase golem = (EntityGolemBase)entity;
                if (!this.worldObj.isRemote) {
                    boolean preciseMode = !golem.getToggles()[0];
                    if (!preciseMode) {
                        golem.setToggle(0, false);
                    }

                    AIFillGoto task = getGolemFillTask(golem);
                    if (task != null) {
                        boolean filterOK = true;
                        Iterator<Entry<HashableItemWithoutSize, Integer>> iter = requests.getIterator();

                        while (iter.hasNext()) {
                            filterOK = false;
                            Entry<HashableItemWithoutSize, Integer> entry = iter.next();
                            HashableItemWithoutSize item = entry.getKey();
                            int amt = entry.getValue() - teInv.getRequestedAmountEnRoute(item);
                            if (amt > 0) {
                                ItemStack stack = item.getItemStack(amt);
                                int max = ThaumcraftExtension.getMaxGolemCanCarry(golem, stack);
                                if (stack.stackSize > max) {
                                    stack.stackSize = max;
                                }

                                ThaumcraftExtension.setGolemItemFilter(golem, stack);
                                int prevTicksExisted = golem.ticksExisted;
                                golem.ticksExisted = 0;
                                if (task.shouldExecute()) {
                                    teInv.markRequestEnRoute(golem, stack);
                                    filterOK = true;
                                    break;
                                }

                                golem.ticksExisted = prevTicksExisted;
                            }
                        }

                        if (!filterOK) {
                            ThaumcraftExtension.clearGolemItemFilter(golem);
                        }
                    }
                }
            }
        }
    }

    public TileEntityInventarium getLinkedInventarium() {
        TileEntity te = this.worldObj.getTileEntity(this.linkX, this.linkY, this.linkZ);
        return te instanceof TileEntityInventarium ? (TileEntityInventarium)te : null;
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

    public static AIFillGoto getGolemFillTask(EntityLiving entity) {
        for (EntityAITaskEntry entityaitaskentry : entity.tasks.taskEntries) {
            EntityAIBase entityaibase1 = entityaitaskentry.action;
            if (entityaibase1 instanceof AIFillGoto) {
                return (AIFillGoto)entityaibase1;
            }
        }

        return null;
    }

    @Override
    public void breakLink() {
        this.linkY = -1;
        this.markDirty();
    }

    @Override
    public void setLink(TileEntityInventarium teInv) {
        this.linkX = teInv.xCoord;
        this.linkY = teInv.yCoord;
        this.linkZ = teInv.zCoord;
        this.markDirty();
    }

    @Override
    public boolean isLinkedTo(TileEntityInventarium teInv) {
        return teInv.xCoord == this.linkX && teInv.yCoord == this.linkY && teInv.zCoord == this.linkZ;
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        if (this.linkName != null || this.getGolemLinkID() != null) {
            nbttagcompound.setString("linkName", this.linkName);
            nbttagcompound.setInteger("linkX", this.linkX);
            nbttagcompound.setInteger("linkY", this.linkY);
            nbttagcompound.setInteger("linkZ", this.linkZ);
        }
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        String newLinkName = nbttagcompound.getString("linkName");
        if (!newLinkName.equals(this.linkName)) {
            this.linkName = newLinkName;
            this.linkID = null;
        }

        this.linkX = nbttagcompound.getInteger("linkX");
        this.linkY = nbttagcompound.getInteger("linkY");
        this.linkZ = nbttagcompound.getInteger("linkZ");
    }

    public class EntitySelectorRequestGolem implements IEntitySelector {
        @Override
        public boolean isEntityApplicable(Entity entity) {
            if (TileEntityGolemTaskmaster.this.numLinksSeen >= TileEntityGolemTaskmaster.MAX_LINKS) {
                return false;
            } else {
                EntityGolemBase golem = (EntityGolemBase)entity;
                if (GolemLink.golemLinkedTo(golem, TileEntityGolemTaskmaster.this)) {
                    TileEntityGolemTaskmaster.this.numLinksSeen++;
                    if (!golem.inactive
                        && !golem.paused
                        && golem.motionX == 0.0
                        && Math.abs(golem.motionY) < 0.1
                        && golem.motionZ == 0.0
                        && golem.getCore() == 0
                        && golem.getCarried() == null
                        && !TileEntityInventarium.isGolemEnRoute(golem)) {
                        return true;
                    }
                }

                return false;
            }
        }
    }
}
