package tuhljin.automagy.lib.events;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import thaumcraft.api.research.ScanResult;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.tiles.TileJarBrain;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.config.ModResearchItems;
import tuhljin.automagy.entities.EntityAICharmWalk;
import tuhljin.automagy.entities.GolemLink;
import tuhljin.automagy.entities.GolemTaskData;
import tuhljin.automagy.gui.AutomagyGUIHandler;
import tuhljin.automagy.items.CachedFilterData;
import tuhljin.automagy.items.ItemBaubleEnderDisjunction;
import tuhljin.automagy.items.ItemEnchantedPaper;
import tuhljin.automagy.items.ItemEntityEye;
import tuhljin.automagy.items.ItemGolemLinker;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.DeferredItemDrops;
import tuhljin.automagy.lib.compat.CompatibilityManager;
import tuhljin.automagy.lib.inventory.FilteringItemList;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.network.MessageClientSync;
import tuhljin.automagy.network.MessageParticles;
import tuhljin.automagy.tiles.IGolemLinkableDevice;
import tuhljin.automagy.tiles.TileEntityAmnesiacStone;
import tuhljin.automagy.tiles.TileEntityGolemTaskmaster;
import tuhljin.automagy.tiles.TileEntityInventarium;
import tuhljin.automagy.tiles.TileEntityMobLure;

public class AutomagyEventHandler {
    @SubscribeEvent
    public void onDeferredDropsTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isRemote) DeferredItemDrops.tick(event.world);
    }
    public static int ENDERMAN_SPAWN_INCREASE_CHANCE = 16;
    protected WorldSpecificCoordinates lastBlazeSpawner = null;
    protected List<Map> entityDataMaps;
    private double lastEnderTeleportNewY = -1.0;

    public AutomagyEventHandler() {
        this.entityDataMaps = new ArrayList<>();
        this.entityDataMaps.add(TileEntityMobLure.secondaryCharmedEntitiesStrength);
        this.entityDataMaps.add(TileEntityMobLure.secondaryCharmedEntitiesTime);
        this.entityDataMaps.add(TileEntityMobLure.secondaryCharmedEntitiesCoord);
        this.entityDataMaps.add(EntityAICharmWalk.charmedEntities);
    }

    @SubscribeEvent
    public void onWorldLoad(Load event) {
        if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
            ThaumcraftExtension.setupPechTrade();
            TileEntityInventarium.coreLocations.clear();
            TileEntityMobLure.lureLocations.clear();
            CachedFilterData.newCache();

            for (Map map : this.entityDataMaps) {
                map.clear();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            MessageClientSync.sendToClient((EntityPlayerMP)event.player);
        }
    }

    @SubscribeEvent
    public void onEntityConstructing(EntityConstructing event) {
        if (event.entity instanceof EntityGolemBase && GolemLink.get((EntityGolemBase)event.entity) == null) {
            GolemLink link = GolemLink.register((EntityGolemBase)event.entity);
            GolemTaskData var3 = GolemTaskData.register((EntityGolemBase)event.entity);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGH
    )
    public void onEntityInteractEvent(EntityInteractEvent event) {
        if (event.entityPlayer != null) {
            ItemStack heldStack = event.entityPlayer.getCurrentEquippedItem();
            if (heldStack != null) {
                Item item = heldStack.getItem();
                if (item instanceof ItemEntityEye && !event.entityPlayer.isSneaking()) {
                    ((ItemEntityEye)item).linkToEntity(heldStack, (EntityLivingBase)event.target, event.entityPlayer);
                    event.setCanceled(true);
                } else if (event.target instanceof EntityGolemBase
                    && item instanceof ItemGolemLinker
                    && ((ItemGolemLinker)item).interactWithGolem(heldStack, event.entityPlayer, (EntityGolemBase)event.target)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.world != null
            && !event.world.isRemote
            && event.action == Action.RIGHT_CLICK_BLOCK
            && event.entityPlayer.getCurrentEquippedItem() == null
            && event.entityPlayer.isSneaking()) {
            TileEntity te = event.world.getTileEntity(event.x, event.y, event.z);
            if (te instanceof TileJarBrain) {
                TileEntity te2 = event.world.getTileEntity(event.x, event.y - 1, event.z);
                if (te2 instanceof TileEntityAmnesiacStone) {
                    ((TileEntityAmnesiacStone)te2).drainAllXPFromBrain((TileJarBrain)te);
                }
            }
        }
    }

    @SubscribeEvent
    public void onCheckSpawn(CheckSpawn event) {
        if (event.entityLiving instanceof EntitySkeleton
            || event.entityLiving instanceof EntitySpider
            || event.entityLiving instanceof EntityZombie
            || event.entityLiving instanceof EntityCreeper) {
            World world = event.world;
            HashableItemWithoutSize item = new HashableItemWithoutSize(ModItems.ingredientHorizontal, 0, null);
            if (world.rand.nextInt(ENDERMAN_SPAWN_INCREASE_CHANCE) == 0
                && this.biomeIsValidSpawn(world, (int)event.x, (int)event.y, (int)event.z, EntityEnderman.class)
                && this.getNearbyPlayerWithItem(item, world, event.x, event.y, event.z, 289.0) != null) {
                float f = event.x + 0.5F;
                float f1 = event.y;
                float f2 = event.z + 0.5F;

                EntityLiving enderman;
                try {
                    enderman = EntityEnderman.class.getConstructor(World.class).newInstance(event.world);
                } catch (Exception var9) {
                    var9.printStackTrace();
                    return;
                }

                enderman.setLocationAndAngles(f, f1, f2, world.rand.nextFloat() * 360.0F, 0.0F);
                Result canSpawn = ForgeEventFactory.canEntitySpawn(enderman, world, f, f1, f2);
                if (canSpawn == Result.ALLOW || canSpawn == Result.DEFAULT && enderman.getCanSpawnHere()) {
                    event.setResult(Result.DENY);
                    world.spawnEntityInWorld(enderman);
                    if (!ForgeEventFactory.doSpecialSpawn(enderman, world, f, f1, f2)) {
                        enderman.onSpawnWithEgg(null);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEnderTeleport(EnderTeleportEvent event) {
        EntityLivingBase entity = event.entityLiving;
        if (entity != null && !(entity instanceof EntityPlayer)) {
            World world = entity.worldObj;
            boolean teleportingToSiren = false;
            if (entity instanceof EntityLiving && ((EntityLiving)entity).getAttackTarget() == null) {
                TileEntityMobLure teML = TileEntityMobLure.getLureNear(world, entity.posX, entity.posY, entity.posZ);
                if (teML != null) {
                    double range = teML.getRange();
                    if (ModBlocks.mobLure.canActiveLureAffectEntity(teML, entity, range * range)) {
                        double x1 = teML.xCoord + 0.5;
                        double z1 = teML.zCoord + 0.5;
                        double x2 = entity.posX;
                        double z2 = entity.posZ;
                        double distanceXZ = (x1 - x2) * (x1 - x2) + (z1 - z2) * (z1 - z2);
                        if (distanceXZ <= 16.0) {
                            event.setCanceled(true);
                            return;
                        }

                        event.targetX = teML.xCoord + (world.rand.nextBoolean() ? 1 : -1);
                        event.targetY = teML.yCoord;
                        event.targetZ = teML.zCoord + (world.rand.nextBoolean() ? 1 : -1);
                        teleportingToSiren = true;
                    }
                }
            }

            if (!(entity.getHealth() <= 0.0F) && !entity.isDead) {
                if (!this.positionIsValidForTeleport(entity, event.targetX, event.targetY, event.targetZ)) {
                    if (teleportingToSiren) {
                        event.setCanceled(true);
                    }
                } else if (teleportingToSiren && event.targetY - this.lastEnderTeleportNewY > 5.0) {
                    event.setCanceled(true);
                } else {
                    boolean goingAway = false;
                    boolean arriving = false;
                    boolean deadBeforeArriving = false;
                    Map<EntityPlayer, Float> map = this.getDisjunctionPlayersAndDamage(
                        world, entity.posX, entity.posY, entity.posZ, ItemBaubleEnderDisjunction.TELEPORT_RIFT_FEEDBACK_SIZE, null
                    );
                    if (map.size() > 0) {
                        goingAway = true;
                        float goingAwayDamage = 0.0F;

                        for (Entry<EntityPlayer, Float> entry : map.entrySet()) {
                            goingAwayDamage += entry.getValue();
                        }

                        if (entity.getHealth() <= goingAwayDamage) {
                            deadBeforeArriving = true;
                        }
                    }

                    if (!deadBeforeArriving) {
                        Map<EntityPlayer, Float> map2 = this.getDisjunctionPlayersAndDamage(
                            world, event.targetX, event.targetY, event.targetZ, ItemBaubleEnderDisjunction.TELEPORT_RIFT_FEEDBACK_SIZE, goingAway ? map : null
                        );
                        if (map2.size() > 0) {
                            arriving = true;
                            if (goingAway) {
                                map.putAll(map2);
                            } else {
                                map = map2;
                            }
                        }
                    }

                    if (map.size() > 0) {
                        double preX = entity.posX;
                        double preY = entity.posY;
                        double preZ = entity.posZ;
                        if (!deadBeforeArriving) {
                            float damageTotal = 0.0F;

                            for (Entry<EntityPlayer, Float> entry : map.entrySet()) {
                                damageTotal += entry.getValue();
                            }

                            if (entity.getHealth() <= damageTotal) {
                                entity.setPosition(event.targetX, event.targetY, event.targetZ);
                            }
                        }

                        for (Entry<EntityPlayer, Float> entry : map.entrySet()) {
                            EntityPlayer player = entry.getKey();
                            Float damage = entry.getValue();
                            DamageSource source = new EntityDamageSource("magic", player).setMagicDamage();
                            entity.hurtResistantTime = 0;
                            entity.attackEntityFrom(source, damage);
                            if (goingAway && entity.getHealth() <= 0.0F) {
                                event.setCanceled(true);
                                break;
                            }
                        }

                        if (goingAway) {
                            MessageParticles.sendToClients(world, (int)preX, (int)preY, (int)preZ, 0, 0, 0, (short)3);
                            world.playSoundEffect(preX, preY + 1.0, preZ, "thaumcraft:ice", 0.5F, 1.2F + world.rand.nextFloat() / 4.0F);
                            world.playSoundEffect(preX, preY + 1.0, preZ, "thaumcraft:ice", 0.3F, 0.4F + world.rand.nextFloat() / 4.0F);
                        }

                        if (arriving) {
                            int x = (int)event.targetX;
                            int y = (int)event.targetY;
                            int z = (int)event.targetZ;
                            MessageParticles.sendToClients(world, x, y, z, 0, 0, 0, (short)3);
                            world.playSoundEffect(x + 0.5, y + 1, z + 0.5, "thaumcraft:ice", 0.5F, 1.2F + world.rand.nextFloat() / 4.0F);
                            world.playSoundEffect(x + 0.5, y + 1, z + 0.5, "thaumcraft:ice", 0.3F, 0.4F + world.rand.nextFloat() / 4.0F);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBreakBlock(BreakEvent event) {
        this.lastBlazeSpawner = null;
        if (AutomagyConfig.netherHeartDrops
            && event.block instanceof BlockMobSpawner
            && (!AutomagyConfig.netherHeartDropNetherOnly || event.world.provider.dimensionId == -1)) {
            TileEntity te = event.world.getTileEntity(event.x, event.y, event.z);
            if (te instanceof TileEntityMobSpawner) {
                MobSpawnerBaseLogic logic = ((TileEntityMobSpawner)te).func_145881_a();
                if ("Blaze".equals(logic.getEntityNameToSpawn())) {
                    this.lastBlazeSpawner = new WorldSpecificCoordinates(event.world, event.x, event.y, event.z);
                }
            }
        }
    }

    @SubscribeEvent
    public void onHarvestDrops(HarvestDropsEvent event) {
        if (this.lastBlazeSpawner != null && event.block instanceof BlockMobSpawner) {
            if (!event.isSilkTouching
                && event.world.provider.dimensionId == this.lastBlazeSpawner.dim
                && event.x == this.lastBlazeSpawner.x
                && event.y == this.lastBlazeSpawner.y
                && event.z == this.lastBlazeSpawner.z) {
                event.drops.add(new ItemStack(ModItems.netherHeart));
            }

            this.lastBlazeSpawner = null;
        }
    }

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack stack = event.item.getEntityItem();
        if (player != null && stack != null && stack.stackSize >= 1) {
            int leftover = stack.stackSize;
            InventoryPlayer inv = player.inventory;
            HashableItemWithoutSize item = null;
            FilteringItemList playerInvMap = null;

            for (int i = 0; i < inv.mainInventory.length; i++) {
                ItemStack invStack = inv.mainInventory[i];
                if (invStack != null && invStack.getItem() == ModItems.autoHandMirror) {
                    if (item == null) {
                        item = new HashableItemWithoutSize(stack);
                        playerInvMap = CachedFilterData.getPlayerInvMap(player, item, leftover);
                    }

                    leftover = ModItems.autoHandMirror.handlePickup(invStack, playerInvMap, item, leftover, player, player.worldObj);
                }

                if (leftover < 1) {
                    break;
                }
            }

            stack.stackSize = leftover;
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (event.entity.worldObj != null && !event.entity.worldObj.isRemote && event.entityLiving instanceof EntityGolemBase) {
            EntityGolemBase golem = (EntityGolemBase)event.entityLiving;
            int taskID = GolemTaskData.get(golem).getTaskID();
            if (taskID != 0) {
                IGolemLinkableDevice device = GolemLink.getLinkedDevice(golem);
                if (device instanceof TileEntityGolemTaskmaster) {
                    TileEntityInventarium teInv = ((TileEntityGolemTaskmaster)device).getLinkedInventarium();
                    if (teInv != null) {
                        boolean fetchPhaseTwo = teInv.isTaskInPhaseTwo(taskID);
                        ItemStack carried = golem.getCarried();
                        if (!fetchPhaseTwo) {
                            if (carried != null) {
                                teInv.markRequestPhaseTwo(golem);
                            } else if (golem.inactive) {
                                teInv.requestFailedByGolem(golem);
                            } else {
                                teInv.golemContinueClaim(golem, taskID);
                            }
                        } else if (carried == null) {
                            teInv.requestFulfilledByGolem(golem);
                        } else {
                            teInv.golemContinueClaim(golem, taskID);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void fillBucket(FillBucketEvent event) {
        if (event.target.typeOfHit == MovingObjectType.BLOCK) {
            World world = event.world;
            int x = event.target.blockX;
            int y = event.target.blockY;
            int z = event.target.blockZ;
            Block block = world.getBlock(x, y, z);
            if ((block == ModBlocks.milk || block == CompatibilityManager.milkBlock) && world.getBlockMetadata(x, y, z) == 0) {
                world.setBlockToAir(x, y, z);
                event.result = new ItemStack(Items.milk_bucket);
                event.setResult(Result.ALLOW);
                return;
            }

            if (block == ModBlocks.mushroomFluid && world.getBlockMetadata(x, y, z) == 0) {
                world.setBlockToAir(x, y, z);
                event.result = new ItemStack(ModItems.bucketMushroom);
                event.setResult(Result.ALLOW);
                return;
            }

            if (block == ModBlocks.vishroomFluid && world.getBlockMetadata(x, y, z) == 0) {
                world.setBlockToAir(x, y, z);
                event.result = new ItemStack(ModItems.bucketVishroom);
                event.setResult(Result.ALLOW);
                return;
            }
        }
    }

    @SubscribeEvent
    public void onCrafting(ItemCraftedEvent event) {
        if (ItemEnchantedPaper.stackIsFilter(event.crafting)) {
            int meta = event.crafting.getMetadata();
            ItemStack sourceFilter = null;
            int sourceFilterSlot = 0;
            int filtersFound = 0;
            int size = event.craftMatrix.getSizeInventory();

            for (int i = 0; i < size; i++) {
                ItemStack stack = event.craftMatrix.getStackInSlot(i);
                if (stack != null && ItemEnchantedPaper.stackIsFilter(stack)) {
                    if (sourceFilter == null) {
                        sourceFilter = stack;
                        sourceFilterSlot = i;
                    }

                    if (++filtersFound > 2) {
                        break;
                    }
                }
            }

            if (sourceFilter != null && filtersFound == 2) {
                sourceFilter.stackSize++;
                event.craftMatrix.setInventorySlotContents(sourceFilterSlot, sourceFilter);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (!event.entity.worldObj.isRemote && event.recentlyHit && event.entity instanceof IMob) {
            Entity source = event.source.getEntity();
            if (source instanceof EntityPlayerMP && !(source instanceof FakePlayer)) {
                String sID = EntityList.getEntityString(event.entity);
                if (sID != null) {
                    String research = ModResearchItems.cluesOnKill.get(sID);
                    if (research != null && (!(event.entity instanceof EntitySkeleton) || ((EntitySkeleton)event.entity).getSkeletonType() == 1)) {
                        EntityPlayerMP player = (EntityPlayerMP)source;
                        if (ThaumcraftExtension.isResearchComplete(player, "DIMENSIONLURE") && !ThaumcraftExtension.isResearchDiscovered(player, research)) {
                            ScanResult sr = new ScanResult((byte)2, 0, 0, event.entity, "");
                            if (ScanManager.hasBeenScanned(player, sr)) {
                                ThaumcraftExtension.triggerResearchUnlock(player, research);
                            }
                        }
                    }
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderGameOverlay(Pre event) {
        if (event.type == ElementType.CROSSHAIRS && AutomagyGUIHandler.hideCrosshairs) {
            event.setCanceled(true);
        }
    }

    public EntityPlayer getNearbyPlayerWithItem(HashableItemWithoutSize item, World world, double x, double y, double z, double maxDistance) {
        EntityPlayer entityplayer = null;

        for (int i = 0; i < world.playerEntities.size(); i++) {
            EntityPlayer entityplayer1 = (EntityPlayer)world.playerEntities.get(i);
            double d5 = entityplayer1.getDistanceSq(x, y, z);
            if ((maxDistance < 0.0 || d5 < maxDistance * maxDistance) && TjUtil.playerHasItem(entityplayer1, item)) {
                return entityplayer1;
            }
        }

        return null;
    }

    protected Map<EntityPlayer, Float> getDisjunctionPlayersAndDamage(
        World world, double x, double y, double z, double maxDistance, Map<EntityPlayer, Float> omit
    ) {
        Map<EntityPlayer, Float> map = new HashMap<>();
        double maxDistanceSq = maxDistance * maxDistance;

        for (int i = 0; i < world.playerEntities.size(); i++) {
            EntityPlayer player = (EntityPlayer)world.playerEntities.get(i);
            if (omit == null || !omit.containsKey(player)) {
                double d5 = player.getDistanceSq(x, y, z);
                if (d5 < maxDistanceSq) {
                    float highestDamage = 0.0F;
                    IInventory baubles = BaublesApi.getBaubles(player);
                    if (baubles != null) {
                        float damage = 0.0F;
                        int size = baubles.getSizeInventory();

                        for (int iB = 0; iB < size; iB++) {
                            ItemStack stack = baubles.getStackInSlot(iB);
                            if (stack != null && stack.getItem() == ModItems.ringEnderDisjunction) {
                                int newDamage = ModItems.ringEnderDisjunction.getDisjunctionDamageAndConsumeVis(stack, player);
                                if (newDamage > 0) {
                                    if (newDamage > highestDamage) {
                                        highestDamage = newDamage;
                                    }

                                    damage += newDamage;
                                }
                            }
                        }

                        if (damage > 0.0F) {
                            damage = highestDamage + (damage - highestDamage) * ItemBaubleEnderDisjunction.TELEPORT_RIFT_FEEDBACK_DIMINISH;
                            map.put(player, damage);
                        }
                    }
                }
            }
        }

        return map;
    }

    public boolean positionIsValidForTeleport(EntityLivingBase entity, double x, double y, double z) {
        double oldX = entity.posX;
        double oldY = entity.posY;
        double oldZ = entity.posZ;
        double newY = y;
        int i = MathHelper.floor_double(x);
        int j = MathHelper.floor_double(y);
        int k = MathHelper.floor_double(z);
        if (entity.worldObj.blockExists(i, j, k)) {
            boolean flag1 = false;

            while (!flag1 && j > 0) {
                Block block = entity.worldObj.getBlock(i, j - 1, k);
                if (block.getMaterial().blocksMovement()) {
                    flag1 = true;
                } else {
                    newY--;
                    j--;
                }
            }

            if (flag1) {
                entity.setPosition(x, newY, z);
                if (entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox).isEmpty() && !entity.worldObj.isAnyLiquid(entity.boundingBox)) {
                    entity.setPosition(oldX, oldY, oldZ);
                    this.lastEnderTeleportNewY = newY;
                    return true;
                }

                entity.setPosition(oldX, oldY, oldZ);
            }
        }

        return false;
    }

    public boolean biomeIsValidSpawn(World world, int x, int y, int z, Class<? extends EntityLivingBase> entityClass) {
        List list = world.getChunkProvider().getPossibleCreatures(EnumCreatureType.monster, x, y, z);
        if (list != null) {
            for (Object entry : list) {
                try {
                    SpawnListEntry spawnEntry = (SpawnListEntry)entry;
                    if (spawnEntry.entityClass == entityClass) {
                        return true;
                    }
                } catch (Exception var10) {
                }
            }
        }

        return false;
    }
}
