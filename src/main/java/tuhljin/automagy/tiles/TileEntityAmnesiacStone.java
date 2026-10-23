package tuhljin.automagy.tiles;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.common.tiles.TileJarBrain;
import tuhljin.automagy.blocks.BlockAmnesiac;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.lib.RedstoneCalc;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;

public class TileEntityAmnesiacStone extends ModTileEntity {
    public static int MAX_ITEMS_PER_DRAIN = 5;
    public Boolean isSharingStorage = null;
    protected int comparatorStrength = 0;
    private int ticks = 0;
    private int totalXP = 0;
    private int totalXPMax = 0;

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote) {
            if (this.isSharingStorage == null) {
                this.isSharingStorage = false;

                for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                    if (dir != ForgeDirection.UP
                        && this.worldObj.getTileEntity(this.xCoord + dir.offsetX, this.yCoord + dir.offsetY, this.zCoord + dir.offsetZ) instanceof TileEntityXPStone
                        )
                     {
                        this.isSharingStorage = true;
                        break;
                    }
                }
            }

            this.ticks++;
            if (this.ticks % 2 == 0 && !this.calcComparatorStrength(this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord))) {
                AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord + 1, this.zCoord, this.xCoord + 1, this.yCoord + 1.5, this.zCoord + 1);

                for (EntityPlayer player : this.getPlayersInArea(bb)) {
                    if (!player.isDead) {
                        ModBlocks.amnesiacBlock.drainExperience(this.worldObj, this.xCoord, this.yCoord, this.zCoord, player, BlockAmnesiac.DRAIN_WALK, true);
                    }
                }

                List<TileEntityAmnesiacStone.PairedItemToDrain> toDrain = this.getXPDrainableItemsInArea(bb);
                if (toDrain != null) {
                    int itemsDrained = 0;

                    for (TileEntityAmnesiacStone.PairedItemToDrain entry : toDrain) {
                        EntityItem entity = entry.entity;
                        ItemStack stack = entity.getEntityItem();
                        int consume = Math.min(stack.stackSize, MAX_ITEMS_PER_DRAIN - itemsDrained);
                        itemsDrained += consume;
                        int left = stack.stackSize - consume;
                        ItemStack replacement = null;
                        if (entry.result.replacementUseContainer) {
                            ItemStack container = stack.getItem().getContainerItem(stack);
                            if (container != null) {
                                if (container.isItemStackDamageable() && container.getMetadata() > container.getMaxDurability()) {
                                    replacement = null;
                                } else {
                                    replacement = container.copy();
                                }
                            }
                        } else if (entry.result.replacement != null) {
                            replacement = entry.result.replacement.copy();
                        }

                        if (replacement != null) {
                            replacement.stackSize = consume;
                            EntityItem remainder = new EntityItem(this.worldObj, entity.posX, entity.posY, entity.posZ, replacement);
                            this.worldObj.spawnEntityInWorld(remainder);
                        }

                        if (left < 1) {
                            entity.setDead();
                        } else {
                            stack = stack.copy();
                            stack.stackSize = left;
                            entity.setEntityItemStack(stack);
                        }

                        int xpEach = entry.result.xp;
                        TjUtil.spawnSuppressedExperienceIntoWorld(
                            xpEach * consume,
                            BlockAmnesiac.XP_ORB_SUPPRESS_FOLLOW_TIME,
                            this.worldObj,
                            this.xCoord + 0.5,
                            this.yCoord + 1.25,
                            this.zCoord + 0.5
                        );
                        if (itemsDrained >= MAX_ITEMS_PER_DRAIN) {
                            break;
                        }
                    }

                    if (itemsDrained > 0) {
                        this.worldObj
                            .playSoundEffect(
                                this.xCoord + 0.5, this.yCoord + 1.25, this.zCoord + 0.5, "random.fizz", 1.0F, 2.0F + this.worldObj.rand.nextFloat() * 0.4F
                            );
                    }
                }
            }
        }
    }

    public List<EntityPlayer> getPlayersInArea(AxisAlignedBB bb) {
        return this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, bb);
    }

    public List<TileEntityAmnesiacStone.PairedItemToDrain> getXPDrainableItemsInArea(AxisAlignedBB bb) {
        List<EntityItem> entities = this.worldObj.getEntitiesWithinAABB(EntityItem.class, bb);
        if (entities.size() == 0) {
            return null;
        } else {
            List<TileEntityAmnesiacStone.PairedItemToDrain> list = new ArrayList<>();

            for (EntityItem item : entities) {
                if (!item.isDead) {
                    ItemStack stack = item.getEntityItem();
                    if (stack != null && stack.stackSize > 0) {
                        HashableItemWithoutSize hi = new HashableItemWithoutSize(stack);
                        if (AutomagyConfig.amnesiacDrainItems.containsKey(hi)) {
                            list.add(new TileEntityAmnesiacStone.PairedItemToDrain(item, AutomagyConfig.amnesiacDrainItems.get(hi)));
                        } else {
                            hi = HashableItemWithoutSize.getWithWildcardDamage(hi);
                            if (AutomagyConfig.amnesiacDrainItems.containsKey(hi)) {
                                list.add(new TileEntityAmnesiacStone.PairedItemToDrain(item, AutomagyConfig.amnesiacDrainItems.get(hi)));
                            }
                        }
                    }
                }
            }

            return list;
        }
    }

    public boolean calcComparatorStrength(TileEntity te) {
        boolean hasBrain = false;
        int xp = 0;
        int max = 0;
        if (te instanceof TileJarBrain) {
            hasBrain = true;
            TileJarBrain brain = (TileJarBrain)te;
            xp = brain.xp;
            max = brain.xpMax;
            if (Boolean.TRUE.equals(this.isSharingStorage)) {
                for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                    if (dir != ForgeDirection.UP) {
                        TileEntity te2 = this.worldObj.getTileEntity(this.xCoord + dir.offsetX, this.yCoord + dir.offsetY, this.zCoord + dir.offsetZ);
                        if (te2 instanceof TileEntityXPStone) {
                            TileEntityXPStone stone = (TileEntityXPStone)te2;
                            xp += stone.getXP();
                            max += stone.getMaxXP();
                        }
                    }
                }
            }
        }

        if (this.totalXP != xp || this.totalXPMax != max) {
            this.totalXP = xp;
            this.totalXPMax = max;
            int strength = hasBrain ? RedstoneCalc.getRedstoneSignalStrengthFromValues(this.totalXP, max) : 0;
            if (this.comparatorStrength != strength) {
                this.comparatorStrength = strength;
                this.markDirty(false);
            }
        }

        return hasBrain;
    }

    public void doXPTransfer(TileEntityXPStone stone, int xpStone) {
        TileEntity te = this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord);
        if (te instanceof TileJarBrain) {
            TileJarBrain brain = (TileJarBrain)te;
            int xpBrain = brain.xp;
            if (xpBrain != xpStone) {
                int average = (xpBrain + xpStone) / 2;
                if (xpBrain < average) {
                    int max = Math.min(brain.xpMax - xpBrain, average - xpBrain);
                    int drained = stone.drainXP(max);
                    setBrainInJarXP(brain, xpBrain + drained);
                } else {
                    int max = Math.min(xpBrain, average - xpStone);
                    if (max == 0) {
                        max = 1;
                    }

                    int added = stone.addXP(max);
                    setBrainInJarXP(brain, xpBrain - added);
                }
            }
        }
    }

    public void drainAllXPFromBrain(TileJarBrain brain) {
        int xp = brain.xp - 1;
        if (xp > 0) {
            setBrainInJarXP(brain, 1);
            this.spawnExperience(xp);

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                if (dir != ForgeDirection.UP) {
                    TileEntity te = this.worldObj.getTileEntity(this.xCoord + dir.offsetX, this.yCoord + dir.offsetY, this.zCoord + dir.offsetZ);
                    if (te instanceof TileEntityXPStone) {
                        int drained = ((TileEntityXPStone)te).quickDrain();
                        this.spawnExperience(drained);
                    }
                }
            }
        }
    }

    public void spawnExperience(int xp) {
        TjUtil.spawnExperienceIntoWorld(xp, this.worldObj, this.xCoord, this.yCoord + 1, this.zCoord, false);
    }

    public int getComparatorStrength() {
        return this.comparatorStrength;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.comparatorStrength = nbttagcompound.getShort("compStr");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setShort("compStr", (short)this.comparatorStrength);
    }

    public static void setBrainInJarXP(TileJarBrain brain, int xp) {
        brain.xp = xp;
        brain.getWorld().markBlockForUpdate(brain.xCoord, brain.yCoord, brain.zCoord);
        brain.markDirty();
    }

    public static class ItemDrainResult {
        public int xp;
        public ItemStack replacement;
        public boolean replacementUseContainer = false;
    }

    public static class PairedItemToDrain {
        public EntityItem entity;
        public TileEntityAmnesiacStone.ItemDrainResult result;

        public PairedItemToDrain(EntityItem entity, TileEntityAmnesiacStone.ItemDrainResult result) {
            this.entity = entity;
            this.result = result;
        }
    }
}
