package tuhljin.automagy.tiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.AspectSourceHelper;
import tuhljin.automagy.api.nethermind.INetherRuneCreature;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.network.MessageParticles;

public class TileEntityDimensionLure extends ModTileEntityWithBasicAspectContainer {
    public static int PILLAR_MAX_HEIGHT = 4;
    public static int VALIDITY_CHECK_FREQUENCY = 60;
    private int ticksSinceValidityCheck = VALIDITY_CHECK_FREQUENCY;
    public int stability = -1;
    protected Map<String, Integer> baitLeft = new HashMap<>();
    private int nethermindY = -1;
    public long lastNoise;

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote && this.isEnabled()) {
            if (this.ticksSinceValidityCheck >= VALIDITY_CHECK_FREQUENCY) {
                this.ticksSinceValidityCheck = 0;
                this.stability = -1;
                if (this.getNethermind() == null || !this.isStructureValid()) {
                    this.disable(true);
                    return;
                }
            } else {
                this.ticksSinceValidityCheck++;
            }
        }
    }

    public boolean isEnabled() {
        return this.getBlockMetadata() == 1;
    }

    public boolean isStructureValid() {
        return this.checkRuneAtOffset(-1, -3)
            && this.checkRuneAtOffset(0, -3)
            && this.checkRuneAtOffset(1, -3)
            && this.checkRuneAtOffset(-1, 3)
            && this.checkRuneAtOffset(0, 3)
            && this.checkRuneAtOffset(1, 3)
            && this.checkRuneAtOffset(-3, -1)
            && this.checkRuneAtOffset(-3, 0)
            && this.checkRuneAtOffset(-3, 1)
            && this.checkRuneAtOffset(3, -1)
            && this.checkRuneAtOffset(3, 0)
            && this.checkRuneAtOffset(3, 1)
            && this.checkRuneAtOffset(-2, -2)
            && this.checkRuneAtOffset(-2, 2)
            && this.checkRuneAtOffset(2, -2)
            && this.checkRuneAtOffset(2, 2);
    }

    private boolean checkRuneAtOffset(int x, int z) {
        return this.worldObj.getBlock(this.xCoord + x, this.yCoord, this.zCoord + z) == ModBlocks.runedObsidian;
    }

    public boolean tryEnable() {
        if (this.getNethermind() != null && this.isStructureValid()) {
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 1, 3);
            this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "thaumcraft:brain", 0.8F, this.worldObj.rand.nextFloat() * 0.3F + 0.2F);
            this.worldObj.playSoundEffect(this.xCoord, this.yCoord + 1, this.zCoord, "mob.ghast.fireball", 1.0F, 0.5F);
            MessageParticles.sendToClients(this.worldObj, this.xCoord, this.yCoord, this.zCoord, 500, 0, 0, (short)2);
            this.doEffectAtOffset(-1, -3);
            this.doEffectAtOffset(0, -3);
            this.doEffectAtOffset(1, -3);
            this.doEffectAtOffset(-1, 3);
            this.doEffectAtOffset(0, 3);
            this.doEffectAtOffset(1, 3);
            this.doEffectAtOffset(-3, -1);
            this.doEffectAtOffset(-3, 0);
            this.doEffectAtOffset(-3, 1);
            this.doEffectAtOffset(3, -1);
            this.doEffectAtOffset(3, 0);
            this.doEffectAtOffset(3, 1);
            this.doEffectAtOffset(-2, -2);
            this.doEffectAtOffset(-2, 2);
            this.doEffectAtOffset(2, -2);
            this.doEffectAtOffset(2, 2);
            return true;
        } else {
            return false;
        }
    }

    private void doEffectAtOffset(int x, int z) {
        x += this.xCoord;
        z += this.zCoord;
        MessageParticles.sendToClients(this.worldObj, x, this.yCoord, z, 0, 0, 0, (short)11);
        if (this.worldObj.rand.nextInt(3) > 0 && this.worldObj.isAirBlock(x, this.yCoord + 1, z)) {
            this.worldObj.setBlock(x, this.yCoord + 1, z, Blocks.fire);
        }
    }

    public void disable(boolean replaceBlock) {
        TileEntityNethermind nethermind = this.getNethermind();
        this.nethermindY = -1;
        if (replaceBlock) {
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 0, 3);
            this.clearActiveRune();
        }

        this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "thaumcraft:craftfail", 1.0F, 1.0F);
        if (nethermind != null) {
            nethermind.requireValidityCheck();
        }
    }

    public TileEntityNethermind getNethermind() {
        if (this.nethermindY != -1) {
            TileEntity te = this.worldObj.getTileEntity(this.xCoord, this.nethermindY, this.zCoord);
            if (te instanceof TileEntityNethermind) {
                return (TileEntityNethermind)te;
            }

            this.nethermindY = -1;
        }

        for (int yOffset = TileEntityNethermind.MIN_LURE_DISTANCE_BASE; yOffset <= TileEntityNethermind.MAX_LURE_DISTANCE; yOffset++) {
            int y = this.yCoord + yOffset;
            TileEntity te = this.worldObj.getTileEntity(this.xCoord, y, this.zCoord);
            if (te instanceof TileEntityNethermind) {
                this.nethermindY = y;
                return (TileEntityNethermind)te;
            }
        }

        return null;
    }

    public boolean isConjuring() {
        TileEntityNethermind nethermind = this.getNethermind();
        return nethermind == null ? false : nethermind.isConjuring();
    }

    public double getMidPoint() {
        if (this.nethermindY == -1) {
            this.getNethermind();
        }

        return this.nethermindY == -1 ? this.yCoord + 0.5 : (this.yCoord + this.nethermindY + 1) / 2.0;
    }

    public boolean consumeEssentia() {
        for (Aspect a : this.aspects.getAspects()) {
            int amt = this.aspects.getAmount(a);
            if (amt > 0 && AspectSourceHelper.drainEssentia(this, a, ForgeDirection.UNKNOWN, 12)) {
                this.aspects.reduce(a, 1);
                this.markDirty();
                return amt == 1 && this.aspects.visSize() == 0;
            }
        }

        return false;
    }

    public void setActiveRune(INetherRuneCreature rune, int runeType) {
        String mob = rune.getEntityIdentifier(runeType);
        if (mob != null) {
            AspectList aspectsCost = rune.secondaryEssentiaCost(runeType);
            aspectsCost = aspectsCost == null ? new AspectList() : aspectsCost.copy();
            Integer bait = this.baitLeft.get(mob);
            if (bait != null && bait > 0) {
                this.aspects = new AspectList();

                for (Aspect a : aspectsCost.getAspects()) {
                    this.aspects.add(a, 0);
                }
            } else {
                this.aspects = aspectsCost;
            }

            this.markDirty();
        }
    }

    public void clearActiveRune() {
        this.aspects.aspects.clear();
        this.markDirty();
        this.ticksSinceValidityCheck = VALIDITY_CHECK_FREQUENCY;
    }

    public void consumeBait(INetherRuneCreature rune, int runeType) {
        String mob = rune.getEntityIdentifier(runeType);
        if (mob != null) {
            Integer bait = this.baitLeft.get(mob);
            if (bait != null) {
                bait = bait - 1;
                if (bait < 1) {
                    this.baitLeft.remove(mob);
                } else {
                    this.baitLeft.put(mob, bait);
                }

                this.markDirty(false);
            }
        }
    }

    public void addBait(INetherRuneCreature rune, int runeType) {
        String mob = rune.getEntityIdentifier(runeType);
        if (mob != null) {
            int uses = rune.secondaryEssentiaCostUses(runeType);
            Integer prevBait = this.baitLeft.get(mob);
            if (prevBait != null) {
                uses += prevBait;
            }

            this.baitLeft.put(mob, uses);
            this.markDirty(false);
        }
    }

    public int getStabilityModifiers() {
        if (this.stability == -1) {
            int p1 = this.getPillarHeight(this.xCoord - 3, this.yCoord, this.zCoord - 3);
            int p2 = this.getPillarHeight(this.xCoord + 3, this.yCoord, this.zCoord - 3);
            int p3 = this.getPillarHeight(this.xCoord - 3, this.yCoord, this.zCoord + 3);
            int p4 = this.getPillarHeight(this.xCoord + 3, this.yCoord, this.zCoord + 3);
            this.stability = p1 + p2 + p3 + p4;
            if (p1 > 0 && p1 == p2 && p1 == p3 && p1 == p4) {
                this.stability += 3;
            }

            int nitor = 0;
            if (p1 > 0 && ThaumcraftExtension.blockIsNitor(this.worldObj, this.xCoord - 3, this.yCoord + p1, this.zCoord - 3)) {
                nitor++;
            }

            if (p2 > 0 && ThaumcraftExtension.blockIsNitor(this.worldObj, this.xCoord + 3, this.yCoord + p2, this.zCoord - 3)) {
                nitor++;
            }

            if (p3 > 0 && ThaumcraftExtension.blockIsNitor(this.worldObj, this.xCoord - 3, this.yCoord + p3, this.zCoord + 3)) {
                nitor++;
            }

            if (p4 > 0 && ThaumcraftExtension.blockIsNitor(this.worldObj, this.xCoord + 3, this.yCoord + p4, this.zCoord + 3)) {
                nitor++;
            }

            if (this.nethermindY > 0) {
                if (ThaumcraftExtension.blockIsNitor(this.worldObj, this.xCoord - 3, this.nethermindY - 2, this.zCoord)) {
                    nitor++;
                }

                if (ThaumcraftExtension.blockIsNitor(this.worldObj, this.xCoord + 3, this.nethermindY - 2, this.zCoord)) {
                    nitor++;
                }

                if (ThaumcraftExtension.blockIsNitor(this.worldObj, this.xCoord, this.nethermindY - 2, this.zCoord - 3)) {
                    nitor++;
                }

                if (ThaumcraftExtension.blockIsNitor(this.worldObj, this.xCoord, this.nethermindY - 2, this.zCoord + 3)) {
                    nitor++;
                }
            }

            this.stability += nitor * 2;
        }

        return this.stability;
    }

    private int getPillarHeight(int x, int y, int z) {
        int height;
        for (height = 0; ThaumcraftExtension.blockIsObsidianTotem(this.worldObj, x, y, z) && height < PILLAR_MAX_HEIGHT; height++) {
            y++;
        }

        return height;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.aspects.readFromNBT(nbttagcompound);
        NBTTagList stacksTagList = nbttagcompound.getTagList("baitLeft", 10);
        this.baitLeft.clear();

        for (int i = 0; i < stacksTagList.tagCount(); i++) {
            NBTTagCompound nbtcomp = stacksTagList.getCompoundTagAt(i);
            String id = nbtcomp.getString("mobID");
            if (id != null) {
                Integer amt = nbtcomp.getInteger("amt");
                if (amt != null && amt > 0) {
                    this.baitLeft.put(id, amt);
                }
            }
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        this.aspects.writeToNBT(nbttagcompound);
        NBTTagList stacksTagList = new NBTTagList();

        for (Entry<String, Integer> entry : this.baitLeft.entrySet()) {
            NBTTagCompound nbtcomp = new NBTTagCompound();
            String id = entry.getKey();
            Integer amt = entry.getValue();
            if (amt != null) {
                nbtcomp.setString("mobID", id);
                nbtcomp.setInteger("amt", amt);
                stacksTagList.appendTag(nbtcomp);
            }
        }

        nbttagcompound.setTag("baitLeft", stacksTagList);
    }
}
