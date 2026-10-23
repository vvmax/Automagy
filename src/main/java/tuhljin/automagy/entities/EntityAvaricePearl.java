package tuhljin.automagy.entities;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import thaumcraft.api.damagesource.DamageSourceThaumcraft;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;

public class EntityAvaricePearl extends EntityThrowable implements IEntityAdditionalSpawnData, IItemMetadata {
    public static final int LIMITEDMODE_MAX_STACKS = 10;
    public static Map<String, Integer> extraChanceForResearch = new HashMap<>();
    public static int grabRange = 5;
    public static EntityAvaricePearl.EntitySelectorItem entitySelectorItem = new EntityAvaricePearl.EntitySelectorItem();
    public double startX;
    public double startY = -1.0;
    public double startZ;
    public boolean noPearlOnImpact = false;
    public int metadata = 0;
    public int stacksGrabbed = 0;
    protected int thrownFromSlot = -1;

    public EntityAvaricePearl(
        World par1World, int metadata, EntityLivingBase par2EntityLivingBase, int thrownFromSlot, boolean noPearlOnImpact, double x, double y, double z
    ) {
        super(par1World, par2EntityLivingBase);
        this.metadata = metadata;
        this.noPearlOnImpact = noPearlOnImpact;
        this.thrownFromSlot = thrownFromSlot;
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        NBTTagCompound nbt = this.getEntityData();
        nbt.setDouble("startX", this.startX);
        nbt.setDouble("startY", this.startY);
        nbt.setDouble("startZ", this.startZ);
        nbt.setInteger("metadata", metadata);
        nbt.setBoolean("noPearlOnImpact", noPearlOnImpact);
        nbt.setShort("thrownFromSlot", (short)thrownFromSlot);
    }

    @SideOnly(Side.CLIENT)
    public EntityAvaricePearl(World par1World, double x, double y, double z, int metadata) {
        super(par1World, x, y, z);
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.metadata = metadata;
        NBTTagCompound nbt = this.getEntityData();
        nbt.setDouble("startX", this.startX);
        nbt.setDouble("startY", this.startY);
        nbt.setDouble("startZ", this.startZ);
        nbt.setInteger("metadata", metadata);
    }

    public EntityAvaricePearl(World par1World) {
        super(par1World);
    }

    @Override
    protected void onImpact(MovingObjectPosition par1MovingObjectPosition) {
        for (int i = 0; i < 32; i++) {
            this.worldObj
                .spawnParticle(
                    "portal", this.posX, this.posY + this.rand.nextDouble() * 2.0, this.posZ, this.rand.nextGaussian(), 0.5, this.rand.nextGaussian()
                );
            this.worldObj
                .spawnParticle(
                    "portal", this.posX, this.posY + this.rand.nextDouble() * 2.0, this.posZ, this.rand.nextGaussian(), -0.5, this.rand.nextGaussian()
                );
        }

        if (!this.worldObj.isRemote) {
            boolean shouldTriggerResearchUnlock = false;
            this.playSound("mob.endermen.portal", 1.0F, 0.6F + this.rand.nextFloat() / 4.0F);
            EntityPlayerMP player = null;
            String playerName = null;
            Entity homeEntity;
            if (this.getThrower() != null && this.getThrower() instanceof EntityPlayerMP && !(this.getThrower() instanceof FakePlayer)) {
                player = (EntityPlayerMP)this.getThrower();
                playerName = player.getCommandSenderName();
                if (playerName != "Rcon" && TjUtil.isPlayerOnline(playerName)) {
                    homeEntity = player;
                } else {
                    player = null;
                    homeEntity = this;
                }
            } else {
                homeEntity = this;
            }

            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                this.posX - grabRange, this.posY - 1.5, this.posZ - grabRange, this.posX + grabRange, this.posY + grabRange, this.posZ + grabRange
            );
            List<Entity> entities = this.worldObj.selectEntitiesWithinAABB(Entity.class, box, entitySelectorItem);
            if (player == null && this.startY != -1.0) {
                this.setPosition(this.startX, this.startY, this.startZ);
            }

            EntityItem newItem = null;
            EntityItem newItem2 = null;
            int stacks = entities.size();
            if (this.metadata == 0) {
                if (stacks <= 0) {
                    if (this.rand.nextInt(10) == 0 && player != null) {
                        player.attackEntityFrom(DamageSourceThaumcraft.taint, 0.5F);
                        ThaumcraftExtension.addFluxTaintPotionEffect(player, 50, 0);
                    }
                } else {
                    int r = this.rand.nextInt(100) + 1;
                    boolean playerCouldDiscoverResearch = false;
                    float chancePerStack = 7.5F;
                    if (player != null
                        && !ThaumcraftExtension.isResearchDiscovered(player, "AVARICIOUS")
                        && ThaumcraftExtension.isResearchComplete(player, "GREEDYCHEST")) {
                        int i = extraChanceForResearch.containsKey(playerName) ? extraChanceForResearch.get(playerName) + 1 : 1;
                        chancePerStack += i * 10;
                        extraChanceForResearch.put(playerName, i);
                        playerCouldDiscoverResearch = true;
                    }

                    if (!(r <= stacks * chancePerStack)) {
                        if (r < 21 && player != null) {
                            player.attackEntityFrom(DamageSourceThaumcraft.taint, 1.0F);
                            ThaumcraftExtension.addFluxTaintPotionEffect(player, 100, 0);
                        }
                    } else {
                        int num = 0;

                        for (Entity entity : entities) {
                            num += ((EntityItem)entity).getEntityItem().stackSize;
                        }

                        r = this.rand.nextInt(100) + 1;
                        newItem = this.dropItem(ModItems.ingredientHorizontal, 1);
                        this.worldObj.playSoundAtEntity(homeEntity, "game.potion.smash", 1.0F, 0.6F + this.rand.nextFloat() / 4.0F);
                        if (playerCouldDiscoverResearch) {
                            shouldTriggerResearchUnlock = true;
                        }

                        if (r > num * 0.6666667F) {
                            newItem2 = this.dropItem(Items.gold_nugget, this.rand.nextInt(3) + 1);
                            if (player != null) {
                                player.attackEntityFrom(DamageSourceThaumcraft.taint, 3.0F);
                                ThaumcraftExtension.addFluxTaintPotionEffect(player, 100, 0);
                            }
                        } else if (player != null) {
                            player.attackEntityFrom(DamageSourceThaumcraft.taint, 5.0F);
                            ThaumcraftExtension.addFluxTaintPotionEffect(player, 200, 0);
                        }
                    }
                }
            }

            if (newItem == null && !this.noPearlOnImpact) {
                newItem = this.entityDropItem(new ItemStack(ModItems.avaricePearl, 1, this.metadata), 0.0F);
            }

            if (player != null) {
                if (newItem != null) {
                    newItem.delayBeforeCanPickup = 0;
                    newItem.onCollideWithPlayer(player);
                }

                if (newItem2 != null) {
                    newItem2.delayBeforeCanPickup = 0;
                    newItem2.onCollideWithPlayer(player);
                }
            }

            if (this.metadata != 1 || this.stacksGrabbed < 10) {
                for (Entity entity : entities) {
                    entity.setWorld(homeEntity.worldObj);
                    entity.setPosition(homeEntity.posX, homeEntity.posY + 0.5, homeEntity.posZ);
                    if (player != null) {
                        ((EntityItem)entity).delayBeforeCanPickup = 0;
                        entity.onCollideWithPlayer(player);
                    }

                    this.stacksGrabbed++;
                    if (this.metadata == 1 && this.stacksGrabbed == 10) {
                        break;
                    }
                }
            }

            if (player != null && stacks > 0) {
                this.setWorld(player.getEntityWorld());
                this.setPosition(player.posX, player.posY + 0.5, player.posZ);
                this.playSound("mob.endermen.portal", 1.0F, 0.6F + this.rand.nextFloat() / 4.0F);
            }

            if (shouldTriggerResearchUnlock) {
                ThaumcraftExtension.triggerResearchUnlock(player, "AVARICIOUS");
                if (extraChanceForResearch.containsKey(playerName)) {
                    extraChanceForResearch.remove(playerName);
                }

                TjUtil.sendRawChatToPlayer(
                    player,
                    EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.ITALIC + StatCollector.translateToLocal("Automagy.chat.triggerAvariciousResearch"),
                    null
                );
            }
        }

        this.setDead();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.worldObj.isRemote) {
            if (this.metadata == 1) {
                for (int i = 0; i < 3; i++) {
                    this.worldObj
                        .spawnParticle(
                            "portal", this.posX, this.posY + this.rand.nextDouble() * 2.0, this.posZ, this.rand.nextGaussian(), 2.0, this.rand.nextGaussian()
                        );
                    this.worldObj
                        .spawnParticle(
                            "portal", this.posX, this.posY + this.rand.nextDouble() * 2.0, this.posZ, this.rand.nextGaussian(), -2.0, this.rand.nextGaussian()
                        );
                }
            }
        } else if (this.metadata == 1 && this.stacksGrabbed < 10 && this.isEntityAlive()) {
            boolean hasPlayer = false;
            if (this.getThrower() != null && this.getThrower() instanceof EntityPlayerMP && !(this.getThrower() instanceof FakePlayer)) {
                EntityPlayerMP player = (EntityPlayerMP)this.getThrower();
                String playerName = player.getCommandSenderName();
                if (playerName != "Rcon" && TjUtil.isPlayerOnline(playerName)) {
                    hasPlayer = true;
                    AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                        this.posX - grabRange,
                        this.posY - grabRange,
                        this.posZ - grabRange,
                        this.posX + grabRange,
                        this.posY + grabRange,
                        this.posZ + grabRange
                    );
                    List<Entity> entities = this.worldObj.selectEntitiesWithinAABB(Entity.class, box, entitySelectorItem);
                    if (entities.size() > 0) {
                        ItemStack dummy = null;
                        if (player != null && this.thrownFromSlot != -1 && player.inventory.getStackInSlot(this.thrownFromSlot) == null) {
                            dummy = new ItemStack(ModItems.avaricePearl, 1, 15);
                            player.inventory.setInventorySlotContents(this.thrownFromSlot, dummy);
                        }

                        for (Entity entity : entities) {
                            entity.setWorld(player.worldObj);
                            ((EntityItem)entity).delayBeforeCanPickup = 0;
                            entity.onCollideWithPlayer(player);
                            this.stacksGrabbed++;
                            if (this.stacksGrabbed == 10) {
                                break;
                            }
                        }

                        if (dummy != null && ItemStack.areItemStacksEqual(player.inventory.getStackInSlot(this.thrownFromSlot), dummy)) {
                            player.inventory.setInventorySlotContents(this.thrownFromSlot, null);
                        }
                    }
                }
            }

            if (!hasPlayer) {
                AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                    this.posX - grabRange, this.posY - grabRange, this.posZ - grabRange, this.posX + grabRange, this.posY + grabRange, this.posZ + grabRange
                );

                for (Entity entityx : this.worldObj.selectEntitiesWithinAABB(Entity.class, box, entitySelectorItem)) {
                    entityx.setPosition(this.startX, this.startY + 0.5, this.startZ);
                    this.stacksGrabbed++;
                    if (this.stacksGrabbed == 10) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
        NBTTagCompound nbt = this.getEntityData();
        nbt.setShort("stacksGrabbed", (short)this.stacksGrabbed);
        super.writeToNBT(par1nbtTagCompound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound) {
        super.readEntityFromNBT(par1nbtTagCompound);
        NBTTagCompound nbt = this.getEntityData();
        this.startX = nbt.getDouble("startX");
        this.startY = nbt.hasKey("startY") ? nbt.getDouble("startY") : -1.0;
        this.startZ = nbt.getDouble("startZ");
        this.metadata = nbt.getInteger("metadata");
        this.noPearlOnImpact = nbt.getBoolean("noPearlOnImpact");
        this.stacksGrabbed = nbt.getShort("stacksGrabbed");
        this.thrownFromSlot = nbt.hasKey("thrownFromSlot") ? nbt.getShort("thrownFromSlot") : -1;
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeShort(this.metadata);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.metadata = additionalData.readShort();
    }

    @Override
    public int getItemMetadata() {
        return this.metadata;
    }

    public static class EntitySelectorItem implements IEntitySelector {
        @Override
        public boolean isEntityApplicable(Entity entity) {
            return entity instanceof EntityItem;
        }
    }
}
