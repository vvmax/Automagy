package tuhljin.automagy.tiles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.AspectSourceHelper;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.common.config.ConfigBlocks;
import tuhljin.automagy.api.nethermind.INetherRune;
import tuhljin.automagy.api.nethermind.INetherRuneBase;
import tuhljin.automagy.api.nethermind.INetherRuneCreature;
import tuhljin.automagy.api.nethermind.RuneCategory;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.entities.EntityWispNether;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.network.MessageParticles;

public class TileEntityNethermind extends ModTileEntityWithBasicAspectContainer {
    public static int MIN_NONBASIC_RUNES_FOR_EXCLUSIVITY = 4;
    public static int TICKS_BEFORE_ALLOW_DISABLE = 10;
    public static int EERIE_SPREAD_CHANCE = 25;
    public static float SPEED_RUNE_MULTIPLIER = 0.075F;
    public static float MAX_SPEED_RUNES = 12.0F;
    public static int MIN_LURE_DISTANCE_BASE = 6;
    public static int MAX_LURE_DISTANCE = 10;
    public static int MAX_NEARBY_ENTITIES = 9;
    public static int INSTABILITY_PER_SUMMON = 80;
    public static int INSTABILITY_LEVEL_DESTROY_SUMMONED = 350;
    public static int INSTABILITY_PER_SPEED_RUNE = 5;
    public static int MIN_Y = 10;
    public static int VALIDITY_CHECK_FREQUENCY = 60;
    private int ticksSinceValidityCheck = VALIDITY_CHECK_FREQUENCY;
    public Set<BlockCoord> specifyingRunePos = new HashSet<>();
    public int numSpeedRunes = 0;
    public int currentRuneX;
    public int currentRuneY = -1;
    public int currentRuneZ;
    public int currentRuneType;
    public int ticksToConjure;
    public int poweredTime = 0;
    public int instability = 0;
    protected int numCreatureRunes = 0;
    protected int dimensionLureY = -1;
    protected boolean didInstabilityCheckThisSummon = false;
    private int ticksEnabled;
    private int minLureDistance;

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote) {
            if (this.isEnabled()) {
                this.ticksEnabled++;
                if (this.ticksSinceValidityCheck >= VALIDITY_CHECK_FREQUENCY) {
                    if (!this.mapStructure(null)) {
                        this.tryDisable();
                        return;
                    }
                } else {
                    this.ticksSinceValidityCheck++;
                }

                int prevPower = this.poweredTime;
                if (this.poweredTime > 0) {
                    if (this.numCreatureRunes > 0) {
                        this.poweredTime -= 2;
                    } else {
                        this.poweredTime--;
                    }
                }

                if (this.poweredTime < 1) {
                    int amt = this.isEnabled() ? VisNetHandler.drainVis(this.worldObj, this.xCoord, this.yCoord + 1, this.zCoord, Aspect.FIRE, 20) : 0;
                    if (amt > 0) {
                        this.poweredTime += amt;
                        if (prevPower < 1) {
                            this.markDirty(true);
                        } else {
                            this.markDirty(false);
                        }
                    } else if (prevPower > 0) {
                        this.markDirty(true);
                    }
                }

                if (this.poweredTime > 0) {
                    boolean nethermindNeedEssentia = this.aspects.visSize() > 0;
                    if (nethermindNeedEssentia || this.numCreatureRunes >= 1 && this.lureAspectsNeeded() != 0) {
                        int n = Math.max(5 - this.numSpeedRunes, 1);
                        if (n == 1 || this.ticksEnabled % n == 0) {
                            if (this.numCreatureRunes > 0) {
                                TileEntityDimensionLure lure = this.getDimensionLure();
                                if (lure == null) {
                                    return;
                                }

                                if (lure.consumeEssentia()) {
                                    INetherRuneCreature rune = this.getCurrentCreatureRune(false);
                                    if (rune == null) {
                                        this.requireValidityCheck();
                                        return;
                                    }

                                    lure.addBait(rune, this.currentRuneType);
                                }
                            }

                            if (nethermindNeedEssentia) {
                                for (Aspect a : this.aspects.getAspects()) {
                                    if (this.aspects.getAmount(a) > 0 && AspectSourceHelper.drainEssentia(this, a, ForgeDirection.UNKNOWN, 12)) {
                                        this.aspects.reduce(a, 1);
                                        this.markDirty();
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        this.ticksToConjure--;
                        if (this.ticksToConjure == 0) {
                            if (this.numCreatureRunes <= 0) {
                                BlockCoord pos = this.findValidPosition(false);
                                if (pos != null) {
                                    INetherRune rune = this.getCurrentBlockRune(false);
                                    if (rune == null) {
                                        this.requireValidityCheck();
                                        return;
                                    }

                                    Block block = rune.blockCreated(this.currentRuneType);
                                    if (block == null) {
                                        this.requireValidityCheck();
                                        return;
                                    }

                                    this.worldObj.setBlock(pos.x, pos.y, pos.z, block, rune.blockCreatedMetadata(this.currentRuneType), 3);
                                    this.worldObj.notifyBlockOfNeighborChange(pos.x, pos.y, pos.z, block);
                                    MessageParticles.sendToClients(this.worldObj, pos.x, pos.y, pos.z, 0, 0, 0, (short)9);
                                    this.doBiomeSpread(false);
                                } else {
                                    this.ticksToConjure = 60;
                                }
                            } else {
                                INetherRuneCreature runex = this.getCurrentCreatureRune(false);
                                TileEntityDimensionLure lurex = this.getDimensionLure();
                                if (runex == null || lurex == null) {
                                    this.requireValidityCheck();
                                    return;
                                }

                                int numToSpawn = runex.getSpawnCount(this.currentRuneType);
                                if (!this.didInstabilityCheckThisSummon) {
                                    this.didInstabilityCheckThisSummon = true;
                                    int extraInstability = runex.getInstability(this.currentRuneType);
                                    extraInstability += this.worldObj.rand.nextInt(10);
                                    this.instabilityBuildup(lurex.getStabilityModifiers() * 10 - extraInstability);
                                    if (this.instability > 0 && this.worldObj.rand.nextInt(500) < this.instability) {
                                        boolean destroySpawn = this.instabilityDischarge(runex, true);
                                        if (destroySpawn) {
                                            numToSpawn--;
                                        }
                                    }
                                }

                                boolean anySpawned = false;
                                int delay = 0;

                                for (int i = 0; i < numToSpawn; i++) {
                                    Boolean spawned = this.trySpawnCreatureFromRune(runex);
                                    if (spawned == null) {
                                        delay = 60;
                                    } else if (spawned) {
                                        anySpawned = true;
                                    } else {
                                        delay = Math.max(delay, 10);
                                    }
                                }

                                if (!anySpawned && numToSpawn >= 1) {
                                    this.ticksToConjure = delay;
                                } else {
                                    lurex.consumeBait(runex, this.currentRuneType);
                                    this.doBiomeSpread(true);
                                }
                            }
                        }

                        if (this.ticksToConjure < 1) {
                            this.setNextRune();
                        } else {
                            this.markDirty(false);
                        }
                    }

                    if (this.instability > 0
                        && this.ticksEnabled % 20 == 0
                        && this.worldObj.rand.nextBoolean()
                        && this.worldObj.rand.nextInt(5000) < this.instability) {
                        this.instabilityDischarge(null, false);
                    }
                }
            }
        } else if (this.isEnabled()) {
            this.ticksEnabled++;
            if (this.isConjuring() && !this.hasActiveDimensionLure() && this.ticksEnabled % 16 == 0 && this.worldObj.rand.nextBoolean()) {
                this.worldObj.spawnParticle("dripLava", this.xCoord + 0.6, this.yCoord - 0.02, this.zCoord + 0.6, 0.0, 0.0, 0.0);
            }
        }
    }

    protected int lureAspectsNeeded() {
        TileEntityDimensionLure lure = this.getDimensionLure();
        return lure == null ? -1 : lure.aspects.visSize();
    }

    protected boolean spawnNetherWisp() {
        Entity entity = new EntityWispNether(this.worldObj);
        return this.getNearbyEntitiesCount(entity) < MAX_NEARBY_ENTITIES ? this.doSpawn(null, entity) : false;
    }

    protected Boolean trySpawnCreatureFromRune(INetherRuneCreature rune) {
        Entity entity = rune.entityCreated(this.currentRuneType, this.worldObj);
        if (entity == null) {
            return null;
        } else {
            return this.getNearbyEntitiesCount(entity) >= MAX_NEARBY_ENTITIES ? null : this.doSpawn(rune, entity);
        }
    }

    private boolean doSpawn(INetherRuneCreature rune, Entity entity) {
        EntityLiving entityliving = entity instanceof EntityLiving ? (EntityLiving)entity : null;

        for (int i = 0; i < 10; i++) {
            double x = this.xCoord + 0.5 + this.worldObj.rand.nextDouble() * 3.0 - this.worldObj.rand.nextDouble() * 3.0;
            double z = this.zCoord + 0.5 + this.worldObj.rand.nextDouble() * 3.0 - this.worldObj.rand.nextDouble() * 3.0;
            double y = this.lureMidPoint() - entity.height / 2.0;
            entity.setLocationAndAngles(x, y, z, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
            if (this.entityCanSpawn(entityliving)) {
                this.spawnMob(entity);
                if (rune != null) {
                    rune.onEntitySpawned(this.currentRuneType, entity);
                }

                this.worldObj.playAuxSFX(2004, this.xCoord, (int)this.lureMidPoint(), this.zCoord, 0);
                if (entityliving != null) {
                    entityliving.spawnExplosionParticle();
                    MessageParticles.sendToClients(
                        this.worldObj, (int)Math.round(entity.posX), (int)Math.round(entity.posY), (int)Math.round(entity.posZ), 0, 0, 0, (short)9
                    );
                }

                return true;
            }
        }

        return false;
    }

    private double lureMidPoint() {
        return this.dimensionLureY > -1 ? (this.yCoord + this.dimensionLureY + 1) / 2.0 : this.yCoord;
    }

    protected BlockCoord findValidPosition(boolean smallArea) {
        int minX = this.xCoord - (smallArea ? 2 : 4);
        int maxX = this.xCoord + (smallArea ? 2 : 4);
        int minY = Math.max(this.yCoord - (smallArea ? 6 : 11), 1);
        int maxY = Math.max(this.yCoord - 3, 1);
        int minZ = this.zCoord - (smallArea ? 2 : 4);
        int maxZ = this.zCoord + (smallArea ? 2 : 4);
        int attempt = 0;

        do {
            int x = TjUtil.randInt(this.worldObj.rand, minX, maxX);
            int y = TjUtil.randInt(this.worldObj.rand, minY, maxY);
            int z = TjUtil.randInt(this.worldObj.rand, minZ, maxZ);
            if (this.worldObj.isAirBlock(x, y, z)) {
                return new BlockCoord(x, y, z);
            }
        } while (++attempt < 33);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (this.worldObj.isAirBlock(x, y, z)) {
                        return new BlockCoord(x, y, z);
                    }
                }
            }
        }

        return null;
    }

    public boolean mapStructure(EntityPlayer player) {
        this.ticksSinceValidityCheck = 0;
        this.specifyingRunePos.clear();
        this.numSpeedRunes = 0;
        this.numCreatureRunes = 0;
        this.dimensionLureY = -1;
        this.minLureDistance = MIN_LURE_DISTANCE_BASE;
        if (this.mapRuneAtOffset(player, -1, 0, -1)
            && this.mapRuneAtOffset(player, 0, 0, -1)
            && this.mapRuneAtOffset(player, 1, 0, -1)
            && this.mapRuneAtOffset(player, -1, 0, 0)
            && this.mapRuneAtOffset(player, 1, 0, 0)
            && this.mapRuneAtOffset(player, -1, 0, 1)
            && this.mapRuneAtOffset(player, 0, 0, 1)
            && this.mapRuneAtOffset(player, 1, 0, 1)
            && this.mapRuneAtOffset(player, -2, 0, 0)
            && this.mapRuneAtOffset(player, -3, 0, 0)
            && this.mapRuneAtOffset(player, -3, -1, 0)
            && this.mapRuneAtOffset(player, 2, 0, 0)
            && this.mapRuneAtOffset(player, 3, 0, 0)
            && this.mapRuneAtOffset(player, 3, -1, 0)
            && this.mapRuneAtOffset(player, 0, 0, -2)
            && this.mapRuneAtOffset(player, 0, 0, -3)
            && this.mapRuneAtOffset(player, 0, -1, -3)
            && this.mapRuneAtOffset(player, 0, 0, 2)
            && this.mapRuneAtOffset(player, 0, 0, 3)
            && this.mapRuneAtOffset(player, 0, -1, 3)) {
            if (this.numCreatureRunes > 0) {
                if (this.numCreatureRunes < MIN_NONBASIC_RUNES_FOR_EXCLUSIVITY) {
                    if (player != null) {
                        TjUtil.sendChatToPlayer(player, "Automagy.chat.nethermind.needMoreMobRunes");
                    }

                    return false;
                }

                int yOffset = 1;
                boolean found = false;

                boolean lureTooClose;
                label92:
                for (lureTooClose = false; yOffset <= MAX_LURE_DISTANCE; yOffset++) {
                    int y = this.yCoord - yOffset;
                    if (yOffset >= MIN_LURE_DISTANCE_BASE) {
                        TileEntity te = this.worldObj.getTileEntity(this.xCoord, y, this.zCoord);
                        if (te != null && te instanceof TileEntityDimensionLure) {
                            if (yOffset < this.minLureDistance) {
                                lureTooClose = true;
                            } else if (((TileEntityDimensionLure)te).isEnabled()) {
                                found = true;
                                this.dimensionLureY = y;
                            }
                            break;
                        }
                    }

                    for (int x = this.xCoord - 2; x <= this.xCoord + 2; x++) {
                        for (int z = this.zCoord - 2; z <= this.zCoord + 2; z++) {
                            if (TjUtil.doesBlockMovement(this.worldObj, x, y, z)) {
                                Block block = this.worldObj.getBlock(x, y, z);
                                if (block != ConfigBlocks.blockFluxGoo && block != ConfigBlocks.blockFluxGas && !block.isAir(this.worldObj, x, y, z)) {
                                    break label92;
                                }
                            }
                        }
                    }
                }

                if (!found) {
                    if (player != null) {
                        TjUtil.sendChatToPlayer(player, "Automagy.chat.nethermind.missingLure");
                        if (lureTooClose) {
                            TjUtil.sendChatToPlayer(player, "Automagy.chat.nethermind.missingLureExtraDistance");
                        }
                    }

                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    protected boolean mapRuneAtOffset(EntityPlayer player, int x, int y, int z) {
        x += this.xCoord;
        y += this.yCoord;
        z += this.zCoord;
        Block block = this.worldObj.getBlock(x, y, z);
        if (block instanceof INetherRuneBase) {
            INetherRuneBase rune = (INetherRuneBase)block;
            int type = rune.getRuneType(this.worldObj, x, y, z);
            RuneCategory sr = rune.getRuneCategory(type);
            if (sr == RuneCategory.speed) {
                if (this.numSpeedRunes < MAX_SPEED_RUNES) {
                    this.numSpeedRunes++;
                }
            } else if (type >= 0) {
                if (sr == RuneCategory.creature) {
                    if (!(block instanceof INetherRuneCreature)) {
                        return false;
                    }

                    if (this.specifyingRunePos.size() != this.numCreatureRunes) {
                        if (player != null) {
                            TjUtil.sendChatToPlayer(player, "Automagy.chat.nethermind.invalidMix");
                        }

                        return false;
                    }

                    this.specifyingRunePos.add(new BlockCoord(x, y, z));
                    this.numCreatureRunes++;
                    this.minLureDistance = Math.max(this.minLureDistance, ((INetherRuneCreature)block).requiredLureDistance(this.worldObj, x, y, z));
                } else {
                    if (!(block instanceof INetherRune)) {
                        return false;
                    }

                    if (this.numCreatureRunes > 0) {
                        if (player != null) {
                            TjUtil.sendChatToPlayer(player, "Automagy.chat.nethermind.invalidMix");
                        }

                        return false;
                    }

                    this.specifyingRunePos.add(new BlockCoord(x, y, z));
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public TileEntityDimensionLure getDimensionLure() {
        if (this.dimensionLureY >= 0) {
            TileEntity te = this.worldObj.getTileEntity(this.xCoord, this.dimensionLureY, this.zCoord);
            if (te instanceof TileEntityDimensionLure) {
                return (TileEntityDimensionLure)te;
            }

            this.requireValidityCheck();
        }

        return null;
    }

    public boolean hasActiveDimensionLure() {
        if (this.dimensionLureY == -1) {
            for (int yOffset = MIN_LURE_DISTANCE_BASE; yOffset <= MAX_LURE_DISTANCE; yOffset++) {
                int y = this.yCoord - yOffset;
                TileEntity te = this.worldObj.getTileEntity(this.xCoord, y, this.zCoord);
                if (te != null && te instanceof TileEntityDimensionLure) {
                    if (!((TileEntityDimensionLure)te).isEnabled()) {
                        this.dimensionLureY = -2;
                        return false;
                    }

                    this.dimensionLureY = y;
                    return true;
                }
            }

            this.dimensionLureY = -2;
            return false;
        } else {
            return this.dimensionLureY != -2;
        }
    }

    public boolean tryEnable(EntityPlayer player) {
        if (!this.isEnabled()) {
            if (this.yCoord < MIN_Y) {
                if (player != null) {
                    TjUtil.sendChatToPlayer(player, "Automagy.chat.nethermind.yTooLow");
                }
            } else if (this.mapStructure(player)) {
                this.ticksEnabled = 0;
                this.ticksToConjure = -1;
                this.markDirty(false);
                this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 1, 3);
                this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "thaumcraft:craftstart", 1.0F, 1.0F);
                MessageParticles.sendToClients(this.worldObj, this.xCoord, this.yCoord, this.zCoord, 0, 0, 0, (short)2);
                return true;
            }
        }

        return false;
    }

    public boolean tryDisable() {
        if (this.isEnabled() && this.ticksEnabled > TICKS_BEFORE_ALLOW_DISABLE) {
            this.aspects.aspects.clear();
            this.poweredTime = 0;
            this.markDirty(false);
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 0, 3);
            this.deactivateRune(this.currentRuneX, this.currentRuneY, this.currentRuneZ);
            this.currentRuneY = -1;
            this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "thaumcraft:craftfail", 1.0F, 1.0F);
            TileEntityDimensionLure lure = this.getDimensionLure();
            if (lure != null) {
                lure.clearActiveRune();
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean isEnabled() {
        return this.getBlockMetadata() == 1;
    }

    public boolean isConjuring() {
        if (this.poweredTime <= 0 || this.ticksToConjure <= 0 || !this.isEnabled() || this.aspects.visSize() != 0) {
            return false;
        } else if (this.hasActiveDimensionLure()) {
            TileEntityDimensionLure lure = this.getDimensionLure();
            if (lure == null) {
                this.dimensionLureY = -1;
                return false;
            } else {
                return lure.aspects.visSize() == 0;
            }
        } else {
            return true;
        }
    }

    public Block getBlockConjuring() {
        INetherRune rune = this.getCurrentBlockRune(false);
        return rune.blockCreated(this.currentRuneType);
    }

    public void activateRune() {
        if (this.currentRuneType >= 0 && this.currentRuneY >= 0) {
            Block block = this.worldObj.getBlock(this.currentRuneX, this.currentRuneY, this.currentRuneZ);
            if (block instanceof INetherRuneBase) {
                ((INetherRuneBase)block).activateRune(this.worldObj, this.currentRuneX, this.currentRuneY, this.currentRuneZ, this.currentRuneType);
            }
        }
    }

    public void deactivateRune(int x, int y, int z) {
        if (this.currentRuneType >= 0 && y >= 0) {
            Block block = this.worldObj.getBlock(x, y, z);
            if (block instanceof INetherRuneBase) {
                ((INetherRuneBase)block).deactivateRune(this.worldObj, x, y, z, this.currentRuneType);
            }
        }
    }

    public void setNextRune() {
        int prevX = this.currentRuneX;
        int prevY = this.currentRuneY;
        int prevZ = this.currentRuneZ;
        this.aspects.aspects.clear();
        int size = this.specifyingRunePos.size();
        if (size < MIN_NONBASIC_RUNES_FOR_EXCLUSIVITY) {
            int r = this.worldObj.rand.nextInt(MIN_NONBASIC_RUNES_FOR_EXCLUSIVITY);
            if (r + 1 > size) {
                this.deactivateRune(this.currentRuneX, this.currentRuneY, this.currentRuneZ);
                this.currentRuneY = -1;
                this.currentRuneType = ModBlocks.runedObsidian.getRandomBasicRuneType(this.worldObj.rand);
                this.ticksToConjure = this.getTicksToConjure(ModBlocks.runedObsidian, this.currentRuneType);
                this.aspects = ModBlocks.runedObsidian.essentiaCostPerCreation(this.currentRuneType).copy();
                this.markDirty();
                return;
            }
        }

        int item = this.worldObj.rand.nextInt(size);
        int i = 0;

        for (BlockCoord bc : this.specifyingRunePos) {
            if (i == item) {
                this.currentRuneX = bc.x;
                this.currentRuneY = bc.y;
                this.currentRuneZ = bc.z;
                INetherRuneBase rune = this.getCurrentRune(true);
                if (rune == null) {
                    this.deactivateRune(prevX, prevY, prevZ);
                    this.requireValidityCheck();
                    this.markDirty(false);
                    return;
                }

                this.currentRuneType = rune.getRuneType(this.worldObj, this.currentRuneX, this.currentRuneY, this.currentRuneZ);
                this.ticksToConjure = this.getTicksToConjure(rune, this.currentRuneType);
                AspectList aspectsCost = rune.essentiaCostPerCreation(this.currentRuneType);
                if (aspectsCost == null) {
                    this.deactivateRune(prevX, prevY, prevZ);
                    this.requireValidityCheck();
                    this.markDirty(false);
                    return;
                }

                if (this.numCreatureRunes > 0 && rune instanceof INetherRuneCreature) {
                    TileEntityDimensionLure lure = this.getDimensionLure();
                    if (lure == null) {
                        this.deactivateRune(prevX, prevY, prevZ);
                        this.requireValidityCheck();
                        this.markDirty(false);
                        return;
                    }

                    lure.setActiveRune((INetherRuneCreature)rune, this.currentRuneType);
                    this.didInstabilityCheckThisSummon = false;
                }

                this.aspects = aspectsCost.copy();
                this.markDirty();
                if (prevX != this.currentRuneX || prevY != this.currentRuneY || prevZ != this.currentRuneZ) {
                    this.deactivateRune(prevX, prevY, prevZ);
                    this.activateRune();
                }

                return;
            }

            i++;
        }
    }

    private int getTicksToConjure(INetherRuneBase rune, int runeType) {
        int ticksToConjure = rune.ticksUntilCreated(runeType);
        if (ticksToConjure > 0 && this.numSpeedRunes > 0) {
            float f = ticksToConjure * SPEED_RUNE_MULTIPLIER * this.numSpeedRunes;
            ticksToConjure = Math.max(Math.round(ticksToConjure - f), 1);
        }

        return ticksToConjure;
    }

    public INetherRune getCurrentBlockRune(boolean ignoreCurrentType) {
        if (!ignoreCurrentType && this.currentRuneType < 0) {
            return ModBlocks.runedObsidian;
        } else {
            Block block = this.worldObj.getBlock(this.currentRuneX, this.currentRuneY, this.currentRuneZ);
            return block instanceof INetherRune ? (INetherRune)block : null;
        }
    }

    public INetherRuneCreature getCurrentCreatureRune(boolean ignoreCurrentType) {
        if (!ignoreCurrentType && this.currentRuneType < 0) {
            return null;
        } else {
            Block block = this.worldObj.getBlock(this.currentRuneX, this.currentRuneY, this.currentRuneZ);
            return block instanceof INetherRuneCreature ? (INetherRuneCreature)block : null;
        }
    }

    public INetherRuneBase getCurrentRune(boolean ignoreCurrentType) {
        return (INetherRuneBase)(this.numCreatureRunes > 0 ? this.getCurrentCreatureRune(ignoreCurrentType) : this.getCurrentBlockRune(ignoreCurrentType));
    }

    public int getNearbyEntitiesCount(Entity entity) {
        double x = this.xCoord;
        double y = (this.yCoord + this.dimensionLureY + 1) / 2.0;
        double z = this.zCoord;
        int range = 6;
        return this.worldObj
            .getEntitiesWithinAABB(entity.getClass(), AxisAlignedBB.getBoundingBox(x, y, z, x + 1.0, y + 1.0, z + 1.0).expand(range, 4.0, range))
            .size();
    }

    public boolean entityCanSpawn(EntityLiving entity) {
        return entity.worldObj.checkNoEntityCollision(entity.boundingBox)
            && entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox).isEmpty()
            && !entity.worldObj.isAnyLiquid(entity.boundingBox);
    }

    public void doBiomeSpread(boolean nether) {
        if (this.worldObj.provider.dimensionId != -1 && this.worldObj.rand.nextInt(100) < EERIE_SPREAD_CHANCE) {
            if (nether) {
                int x = this.xCoord + this.worldObj.rand.nextInt(8) - this.worldObj.rand.nextInt(8);
                int z = this.zCoord + this.worldObj.rand.nextInt(8) - this.worldObj.rand.nextInt(8);
                if (!TjUtil.isBiomeAt(this.worldObj, x, z, BiomeGenBase.hell)) {
                    ThaumcraftExtension.setBiomeTo(this.worldObj, x, z, BiomeGenBase.hell);
                }
            }

            int x = this.xCoord + this.worldObj.rand.nextInt(12) - this.worldObj.rand.nextInt(12);
            int z = this.zCoord + this.worldObj.rand.nextInt(12) - this.worldObj.rand.nextInt(12);
            BiomeGenBase bg = this.worldObj.getBiomeGenForCoords(x, z);
            if (!ThaumcraftExtension.isBiomeEerie(bg) && (!nether || !TjUtil.isBiomeAt(this.worldObj, x, z, BiomeGenBase.hell))) {
                ThaumcraftExtension.setBiomeToEerie(this.worldObj, x, z);
            }
        }
    }

    public void instabilityBuildup(int modifier) {
        if (this.instability < 1000) {
            int change = INSTABILITY_PER_SUMMON + INSTABILITY_PER_SPEED_RUNE * this.numSpeedRunes - modifier;
            if (change > 0) {
                this.instability += change;
                if (this.instability > 1000) {
                    this.instability = 1000;
                }

                this.markDirty(false);
            }
        }
    }

    public boolean instabilityDischarge(INetherRuneCreature rune, boolean spawning) {
        boolean preventSummon = false;
        int amt;
        if (spawning && this.instability >= INSTABILITY_LEVEL_DESTROY_SUMMONED) {
            if (this.worldObj.rand.nextInt(this.instability) > INSTABILITY_LEVEL_DESTROY_SUMMONED / 2) {
                amt = INSTABILITY_LEVEL_DESTROY_SUMMONED;
                TileEntityDimensionLure lure = this.getDimensionLure();
                if (lure != null) {
                    amt += lure.getStabilityModifiers();
                    if (amt > this.instability) {
                        amt = this.instability - 1;
                    }
                }

                preventSummon = true;
                BlockCoord bc = this.worldObj.rand.nextInt(3) == 0 ? this.makeFluxGoo() : this.makeFluxGas();
                int x;
                int y;
                int z;
                if (bc == null) {
                    x = this.xCoord;
                    y = (int)this.lureMidPoint();
                    z = this.zCoord;
                } else {
                    x = bc.x;
                    y = bc.y;
                    z = bc.z;
                }

                MessageParticles.sendToClients(this.worldObj, x, y, z, 0, 0, 0, (short)9);
                if (rune != null) {
                    Entity entity = rune.entityCreated(this.currentRuneType, this.worldObj);
                    if (entity instanceof EntityLiving) {
                        float pitch = 0.4F + this.worldObj.rand.nextFloat() / 3.0F;
                        TjUtil.playDeathSound((EntityLiving)entity, this.worldObj, x + 0.5, y + 0.5, z + 0.5, pitch);
                    }
                }
            } else {
                amt = 1;
            }
        } else {
            amt = this.worldObj.rand.nextInt(Math.min(this.instability, 10)) + 1;
        }

        this.instability -= amt;
        this.markDirty(false);
        if (preventSummon) {
            return true;
        } else {
            this.instabilityEffect(false);
            return false;
        }
    }

    public void instabilityEffect(boolean lowCap) {
        int r = this.worldObj.rand.nextInt(21);
        if (r < 10) {
            this.zapNothing();
        } else if (r < 15) {
            if (!this.zapPlayer(false)) {
                this.zapNothing();
            }
        } else if (r < 17) {
            if (!this.zapPlayer(true)) {
                this.unstableExplosion();
            }
        } else if (lowCap || r < 19) {
            this.unstableExplosion();
        } else if (r == 19) {
            if (this.instability >= 100 && this.worldObj.rand.nextInt(4) != 0) {
                TileEntityDimensionLure lure = this.getDimensionLure();
                if (lure != null && lure.getStabilityModifiers() < 20 && !this.spawnNetherWisp()) {
                    this.instabilityEffect(true);
                }
            } else {
                this.instabilityEffect(true);
            }
        } else {
            this.instabilityEffect(true);
            this.instabilityEffect(true);
        }
    }

    protected BlockCoord makeFluxGas() {
        BlockCoord bc = this.findValidPosition(true);
        if (bc != null) {
            this.worldObj.setBlock(bc.x, bc.y, bc.z, ConfigBlocks.blockFluxGas, 7, 3);
            this.worldObj.playSoundEffect(bc.x, bc.y, bc.z, "random.fizz", 0.3F, 1.0F);
        }

        return bc;
    }

    protected BlockCoord makeFluxGoo() {
        BlockCoord bc = this.findValidPosition(true);
        if (bc != null) {
            this.worldObj.setBlock(bc.x, bc.y, bc.z, ConfigBlocks.blockFluxGoo, 7, 3);
            this.worldObj.playSoundEffect(bc.x, bc.y, bc.z, "game.neutral.swim", 0.3F, 1.0F);
        }

        return bc;
    }

    protected void unstableExplosion() {
        double y = (int)this.lureMidPoint() + this.worldObj.rand.nextInt(3) - this.worldObj.rand.nextInt(3);
        int x = this.xCoord + this.worldObj.rand.nextInt(4) - this.worldObj.rand.nextInt(4);
        int z = this.zCoord + this.worldObj.rand.nextInt(4) - this.worldObj.rand.nextInt(4);
        this.worldObj.createExplosion(null, x + 0.5F, y, z + 0.5F, 1.0F, false);
    }

    protected void zapNothing() {
        float sourceY = (this.worldObj.rand.nextBoolean() ? this.yCoord : this.dimensionLureY) + 0.5F;
        float x = this.xCoord + 0.5F + this.worldObj.rand.nextFloat() * 8.0F - this.worldObj.rand.nextFloat() * 8.0F;
        float y = sourceY + this.worldObj.rand.nextFloat() * 5.0F - this.worldObj.rand.nextFloat() * 5.0F;
        float z = this.zCoord + 0.5F + this.worldObj.rand.nextFloat() * 8.0F - this.worldObj.rand.nextFloat() * 8.0F;
        ThaumcraftExtension.zapLocation(this.worldObj, this.xCoord, sourceY, this.zCoord, x, y, z);
    }

    protected boolean zapPlayer(boolean all) {
        List<EntityPlayer> targets = this.worldObj
            .getEntitiesWithinAABB(
                EntityPlayer.class,
                AxisAlignedBB.getBoundingBox(this.xCoord, this.dimensionLureY, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1)
                    .expand(10.0, 10.0, 10.0)
            );
        if (targets != null && targets.size() > 0) {
            if (all) {
                for (EntityPlayer target : targets) {
                    this.zapEntity(target);
                }
            } else {
                EntityPlayer target = targets.get(this.worldObj.rand.nextInt(targets.size()));
                this.zapEntity(target);
            }

            return true;
        } else {
            return false;
        }
    }

    private void zapEntity(Entity target) {
        double d1 = TjUtil.getDistanceBetweenPoints_squared(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, target.posX, target.posY, target.posZ);
        double d2 = TjUtil.getDistanceBetweenPoints_squared(
            this.xCoord + 0.5, this.dimensionLureY + 0.5, this.zCoord + 0.5, target.posX, target.posY, target.posZ
        );
        ThaumcraftExtension.zapEntity(target, this.worldObj, this.xCoord, d1 < d2 ? this.yCoord : this.dimensionLureY, this.zCoord);
    }

    public Entity spawnMob(Entity entity) {
        if (entity instanceof EntityLivingBase && entity.worldObj != null) {
            ((EntityLiving)entity).onSpawnWithEgg((IEntityLivingData)null);
            this.worldObj.spawnEntityInWorld(entity);
        }

        return entity;
    }

    public void requireValidityCheck() {
        this.ticksSinceValidityCheck = VALIDITY_CHECK_FREQUENCY;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.aspects.readFromNBT(nbttagcompound);
        this.currentRuneX = nbttagcompound.getInteger("currentRuneX");
        this.currentRuneY = nbttagcompound.getInteger("currentRuneY");
        this.currentRuneZ = nbttagcompound.getInteger("currentRuneZ");
        this.currentRuneType = nbttagcompound.getInteger("currentRuneType");
        this.ticksToConjure = nbttagcompound.getInteger("ticksToConjure");
        this.poweredTime = nbttagcompound.getInteger("poweredTime");
        this.instability = nbttagcompound.getInteger("instability");
        this.didInstabilityCheckThisSummon = nbttagcompound.getBoolean("instabilityChecked");
        if (this.worldObj != null && this.worldObj.isRemote) {
            this.dimensionLureY = -1;
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        this.aspects.writeToNBT(nbttagcompound);
        nbttagcompound.setInteger("currentRuneX", this.currentRuneX);
        nbttagcompound.setInteger("currentRuneY", this.currentRuneY);
        nbttagcompound.setInteger("currentRuneZ", this.currentRuneZ);
        nbttagcompound.setInteger("currentRuneType", this.currentRuneType);
        nbttagcompound.setInteger("ticksToConjure", this.ticksToConjure);
        nbttagcompound.setInteger("poweredTime", this.poweredTime);
        nbttagcompound.setInteger("instability", this.instability);
        nbttagcompound.setBoolean("instabilityChecked", this.didInstabilityCheckThisSummon);
    }
}
