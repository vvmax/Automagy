package tuhljin.automagy.blocks.fluid;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXSlimyBubble;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.WarpEvents;
import thaumcraft.common.lib.potions.PotionBlurredVision;
import thaumcraft.common.lib.potions.PotionWarpWard;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.network.MessageParticles;
import tuhljin.automagy.network.MessageSound;

public class BlockFluidVishroom extends ModBlockFluid {
    public static int INTERVAL = 60;
    public static int MYCELIUM_FROM_STACK_RATE = 20;
    public static int MOOSHROOM_TRANSFORM_TIME = 1200;
    public static int WARPING_REMOVES_CHANCE = 8;
    public static int POOF_CHANCE_MYCELIUM_ITEM = 5;
    public static int POOF_CHANCE_MYCELIUM_BLOCK = 6;

    public BlockFluidVishroom(String name) {
        super(name, ModBlocks.fluidVishroom, Material.water);
        this.setLightOpacity(5);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (!world.isRemote) {
            if (entity instanceof EntityLivingBase) {
                NBTTagCompound data = entity.getEntityData();
                Long t = world.getTotalWorldTime();
                if (t - data.getLong("fluidVishroomTimer") >= INTERVAL) {
                    data.setLong("fluidVishroomTimer", t);
                    EntityLivingBase e = (EntityLivingBase)entity;
                    EntityPlayer player = e instanceof EntityPlayer ? (EntityPlayer)entity : null;
                    if (!e.isPotionActive(Potion.weakness)) {
                        e.addPotionEffect(new PotionEffect(Potion.weakness.id, 200, 1));
                    } else if (player != null && !e.isPotionActive(PotionBlurredVision.instance)) {
                        e.addPotionEffect(new PotionEffect(Potion.weakness.id, 200, 1));
                        e.addPotionEffect(new PotionEffect(PotionBlurredVision.instance.id, 200, 1));
                    } else {
                        e.addPotionEffect(new PotionEffect(Potion.weakness.id, 200, 1));
                        if (world.rand.nextInt(10) == 0) {
                            if (e instanceof EntityMooshroom) {
                                e.addPotionEffect(new PotionEffect(Potion.regeneration.id, 320, 0));
                            } else {
                                e.addPotionEffect(new PotionEffect(Potion.poison.id, 80, 2));
                            }
                        }

                        if (player != null) {
                            player.addPotionEffect(new PotionEffect(PotionBlurredVision.instance.id, 200, 1));
                        }
                    }

                    if (player != null && this.isSourceBlock(world, x, y, z)) {
                        player.removePotionEffect(PotionWarpWard.instance.id);
                        if (!Config.wuss) {
                            boolean doWarpCheck = false;
                            boolean usedUp = false;
                            int warp = ThaumcraftExtension.getTemporaryWarp(player) + ThaumcraftExtension.getStickyWarp(player);
                            if (t - data.getLong("fluidVishroomWarpTimer") <= INTERVAL) {
                                int diff = warp - data.getInteger("fluidVishroomWarpSeen");
                                if (diff > 0) {
                                    warp += diff;
                                    doWarpCheck = true;
                                    boolean playedSound = false;

                                    for (int i = 1; i <= diff; i++) {
                                        if (world.rand.nextInt(WARPING_REMOVES_CHANCE) == 0) {
                                            usedUp = true;
                                            break;
                                        }
                                    }

                                    if (world.rand.nextInt(4) == 0) {
                                        Thaumcraft.addStickyWarpToPlayer(player, 1);
                                        playedSound = true;
                                        diff--;
                                    }

                                    if (diff > 0) {
                                        Thaumcraft.addWarpToPlayer(player, diff, true);
                                        if (!playedSound) {
                                            MessageSound.sendToClient(player, (short)0);
                                        }
                                    }

                                    player.addPotionEffect(new PotionEffect(Potion.hunger.id, 600, 1));
                                }
                            }

                            data.setLong("fluidVishroomWarpTimer", t);
                            data.setInteger("fluidVishroomWarpSeen", warp);
                            if (doWarpCheck || world.rand.nextInt(60) == 0) {
                                if (!doWarpCheck) {
                                    ThaumcraftExtension.resetWarpCounter(player);
                                }

                                WarpEvents.checkWarpEvent(player);
                            }

                            if (usedUp) {
                                MessageParticles.sendToClients(world, x, y, z, 300, 0, 0, (short)2);
                                MessageParticles.sendToClients(world, x, y, z, 50, 0, 0, (short)3);
                                world.playSoundEffect(
                                    x + 0.5, y + 0.5, z + 0.5, "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F
                                );
                                world.playSoundEffect(
                                    x + 0.5, y + 0.5, z + 0.5, "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F
                                );
                                this.poof(world, x, y, z);
                                return;
                            }
                        }
                    }
                }

                if (entity instanceof EntityCow && !(entity instanceof EntityMooshroom) && !entity.isDead && this.isSourceBlock(world, x, y, z)) {
                    int c = data.getInteger("fluidVishroomCowTime");
                    if (c >= MOOSHROOM_TRANSFORM_TIME) {
                        EntityMooshroom mooshroom = turnCowIntoMooshroom((EntityCow)entity);
                        mooshroom.addPotionEffect(new PotionEffect(Potion.regeneration.id, 320, 0));
                        this.poof(world, x, y, z);
                        if (world.isAirBlock(x, y + 1, z)) {
                            this.poof(world, x, y + 1, z);
                        }

                        return;
                    }

                    data.setInteger("fluidVishroomCowTime", c + 1);
                    if (c % 25 == 0) {
                        MessageParticles.sendToClients(world, x, y, z, 100, 0, 0, (short)2);
                    }
                }
            } else if (entity instanceof EntityItem && this.isSourceBlock(world, x, y, z)) {
                NBTTagCompound datax = entity.getEntityData();
                Long tx = world.getTotalWorldTime();
                if (tx - datax.getLong("fluidVishroomTimer") >= MYCELIUM_FROM_STACK_RATE) {
                    datax.setLong("fluidVishroomTimer", tx);
                    EntityItem item = (EntityItem)entity;
                    ItemStack stack = item.getEntityItem();
                    if (TjUtil.areItemsEqualIgnoringSize(stack, new ItemStack(Blocks.grass))) {
                        if (stack.stackSize > 1) {
                            stack.stackSize--;
                            item.setEntityItemStack(stack);
                        } else {
                            item.setDead();
                        }

                        EntityItem newItem = TjUtil.dropItemIntoWorldSimple(new ItemStack(Blocks.mycelium), world, x, y, z);
                        newItem.motionY += 0.25;
                        newItem.motionX *= 0.1F;
                        newItem.motionZ *= 0.1F;
                        world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
                        MessageParticles.sendToClients(world, x, y, z, 0, 0, 0, (short)2);
                        if (world.rand.nextInt(POOF_CHANCE_MYCELIUM_ITEM) == 0) {
                            this.poof(world, x, y, z);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        if (this.isSourceBlock(world, x, y, z)) {
            if (rand.nextInt(5) == 0) {
                boolean didSpread = false;
                int r = rand.nextInt(10);
                if (r < 6) {
                    ForgeDirection dir = ForgeDirection.getOrientation(r < 6 ? r : r - 4);
                    didSpread = this.doSpread(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, world.rand, 0);
                } else {
                    switch (r) {
                        case 6:
                            didSpread = this.doSpread(world, x + 1, y, z + 1, world.rand, 0);
                            break;
                        case 7:
                            didSpread = this.doSpread(world, x + 1, y, z - 1, world.rand, 0);
                            break;
                        case 8:
                            didSpread = this.doSpread(world, x - 1, y, z - 1, world.rand, 0);
                            break;
                        case 9:
                            didSpread = this.doSpread(world, x - 1, y, z + 1, world.rand, 0);
                    }
                }

                if (didSpread && rand.nextInt(POOF_CHANCE_MYCELIUM_BLOCK) == 0) {
                    this.poof(world, x, y, z);
                    return;
                }
            }

            world.scheduleBlockUpdate(x, y, z, this, this.tickRate);
        }

        super.updateTick(world, x, y, z, rand);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        if (this.isSourceBlock(world, x, y, z)) {
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1);
            List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
            if (list.size() != 0) {
                float h = rand.nextFloat() * 0.075F;
                FXSlimyBubble ef = new FXSlimyBubble(world, x + rand.nextFloat(), y + 0.1F, z + rand.nextFloat(), 0.075F + h);
                ef.setAlphaF(0.8F);
                ef.setRBGColorF(1.0F, 0.28F, 0.53F);
                ParticleEngine.instance.addEffect(world, ef);
                if (rand.nextInt(8) == 0) {
                    double var21 = x + rand.nextFloat();
                    double var22 = y + this.maxY;
                    double var23 = z + rand.nextFloat();
                    world.playSound(var21, var22, var23, "liquid.lavapop", 0.1F + rand.nextFloat() * 0.1F, 0.9F + rand.nextFloat() * 0.15F, false);
                }
            }
        }
    }

    private boolean doSpread(World world, int x, int y, int z, Random rand, int count) {
        Block block = world.getBlock(x, y, z);
        if (block == this) {
            if (count > 2) {
                return false;
            } else if (this.isSourceBlock(world, x, y, z)) {
                return false;
            } else {
                int r = rand.nextInt(10);
                ForgeDirection dir = ForgeDirection.getOrientation(r < 6 ? r : r - 4);
                return r < 6
                    ? this.doSpread(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, world.rand, count + 1)
                    : this.doSpread(
                        world, x + dir.offsetX + (rand.nextInt(3) - 1), y + dir.offsetY, z + dir.offsetZ + (rand.nextInt(3) - 1), world.rand, count + 1
                    );
            }
        } else if (block == Blocks.grass) {
            Block block2 = world.getBlock(x, y + 1, z);
            if (block2 == Blocks.tallgrass
                || block2 == Blocks.double_plant
                || block2 == Blocks.red_flower
                || block2 == Blocks.yellow_flower
                || world.isAirBlock(x, y + 1, z) && rand.nextInt(8) == 0) {
                world.setBlock(x, y + 1, z, rand.nextBoolean() ? Blocks.red_mushroom : Blocks.brown_mushroom);
                MessageParticles.sendToClients(world, x, y + 1, z, 0, 0, 0, (short)2);
            }

            world.setBlock(x, y, z, Blocks.mycelium);
            world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
            MessageParticles.sendToClients(world, x, y, z, 0, 0, 0, (short)2);
            return true;
        } else {
            if (block == Blocks.tallgrass || block == Blocks.double_plant || block == Blocks.red_flower || block == Blocks.yellow_flower) {
                Block mushroom = rand.nextBoolean() ? Blocks.red_mushroom : Blocks.brown_mushroom;
                if (!mushroom.canBlockStay(world, x, y, z)) {
                    return this.doSpread(world, x, y - 1, z, rand, count + 1);
                }

                world.setBlock(x, y, z, mushroom);
                world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
                MessageParticles.sendToClients(world, x, y, z, 0, 0, 0, (short)2);
            }

            return false;
        }
    }

    private void poof(World world, int x, int y, int z) {
        world.setBlock(x, y, z, ConfigBlocks.blockFluxGas);
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x - 0.5, y - 0.1, z - 0.5, x + 1.5, y + 1.1, z + 1.5);

        for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, box)) {
            player.addPotionEffect(new PotionEffect(Potion.confusion.id, 180, 1));
        }
    }

    public static EntityMooshroom turnCowIntoMooshroom(EntityCow cow) {
        cow.setDead();
        EntityMooshroom mooshroom = new EntityMooshroom(cow.worldObj);
        mooshroom.setLocationAndAngles(cow.posX, cow.posY, cow.posZ, cow.rotationYaw, cow.rotationPitch);
        mooshroom.setHealth(cow.getHealth());
        mooshroom.renderYawOffset = cow.renderYawOffset;
        cow.worldObj.spawnEntityInWorld(mooshroom);
        cow.worldObj.spawnParticle("largeexplode", cow.posX, cow.posY + cow.height / 2.0F, cow.posZ, 0.0, 0.0, 0.0);
        cow.playSound("random.fizz", 0.5F, 2.6F + (cow.worldObj.rand.nextFloat() - cow.worldObj.rand.nextFloat()) * 0.8F);
        MessageParticles.sendToClients(cow.worldObj, (int)cow.posX, (int)cow.posY, (int)cow.posZ, 0, 0, 0, (short)3);
        return mooshroom;
    }
}
