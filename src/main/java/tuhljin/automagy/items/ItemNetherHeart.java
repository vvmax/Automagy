package tuhljin.automagy.items;

import java.util.Random;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.IWarpingGear;
import thaumcraft.common.lib.potions.PotionSunScorned;
import tuhljin.automagy.network.MessageFloatXYZ;
import tuhljin.automagy.network.MessageSound;

public class ItemNetherHeart extends ModItem implements IWarpingGear {
    public static int MAX_WARP = 6;

    public ItemNetherHeart(String name) {
        super(name);
        this.setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 520;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.block;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
        return par1ItemStack;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        if (player.worldObj.isRemote) {
            if (count == 460) {
                player.playSound("thaumcraft:heartbeat", 0.4F, 0.5F);
            } else if (count == 275) {
                player.playSound("thaumcraft:heartbeat", 1.0F, 0.5F);
            } else if (count < 400 && count % 90 == 0) {
                player.playSound("mob.blaze.breathe", 0.1F, 0.9F);
            }
        } else {
            if (count < 400) {
                if (count != 1) {
                    if (count % 35 == 0) {
                        Random rand = player.worldObj.rand;
                        if (rand.nextInt(3) == 0) {
                            int attempt = 0;

                            do {
                                int x = (int)(player.posX + 0.5) + rand.nextInt(7) - 3;
                                int y = (int)(player.posY + 0.5) + rand.nextInt(7) - 3;
                                int z = (int)(player.posZ + 0.5) + rand.nextInt(7) - 3;
                                if (canSupportFire(player.worldObj, x, y, z)) {
                                    player.worldObj.setBlock(x, y, z, Blocks.fire);
                                    break;
                                }
                            } while (++attempt < 200);
                        } else {
                            player.worldObj.playSoundAtEntity(player, "fire.fire", 0.4F, 0.5F + rand.nextFloat() / 2.0F);
                        }
                    }
                } else {
                    Random rand = player.worldObj.rand;
                    int loop = 0;

                    do {
                        int attempt = 0;

                        do {
                            int x = (int)(player.posX + 0.5) + rand.nextInt(9) - 4;
                            int y = (int)(player.posY + 0.5) + rand.nextInt(9) - 4;
                            int z = (int)(player.posZ + 0.5) + rand.nextInt(9) - 4;
                            if (canSupportFire(player.worldObj, x, y, z)) {
                                player.worldObj.setBlock(x, y, z, Blocks.fire);
                                break;
                            }
                        } while (++attempt < 200);
                    } while (++loop < 40);

                    boolean ret = this.increaseWarp(stack);
                    if (ret) {
                        player.addPotionEffect(new PotionEffect(PotionSunScorned.instance.id, 800, 1));
                        MessageSound.sendToClient(player, (short)0);
                    }

                    player.worldObj.playSoundAtEntity(player, "mob.ghast.fireball", 1.0F, 0.5F);
                }
            }

            if (count % 15 == 0) {
                Vec3 vec = player.getLookVec();
                double x = player.posX + vec.xCoord;
                double y = player.posY + vec.yCoord + player.height * 0.75;
                double z = player.posZ + vec.zCoord;
                MessageFloatXYZ.sendToClients(player.worldObj, x, y, z, (short)0);
            }
        }
    }

    @Override
    public int getWarp(ItemStack stack, EntityPlayer player) {
        int d = stack.getMetadata();
        if (d == 32767) {
            long t = System.currentTimeMillis() / 1000L;
            d = (int)(t % MAX_WARP);
        }

        return Math.min(d + 1, MAX_WARP);
    }

    public boolean increaseWarp(ItemStack stack) {
        int warp = this.getWarp(stack, null);
        if (warp < MAX_WARP) {
            stack.setMetadata(warp);
            return true;
        } else {
            return false;
        }
    }

    public static boolean canSupportFire(IBlockAccess blockaccess, int x, int y, int z) {
        if (blockaccess.isAirBlock(x, y, z)) {
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                if (blockaccess.isSideSolid(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir.getOpposite(), false)) {
                    return true;
                }
            }
        }

        return false;
    }
}
