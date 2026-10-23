package tuhljin.automagy.blocks.fluid;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import tuhljin.automagy.blocks.ModBlockSpecialRender;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.compat.CompatibilityManager;
import tuhljin.automagy.tiles.ITileWithTank;

public abstract class BlockFillableByBucket extends ModBlockSpecialRender {
    public boolean useWaterBottles;

    public BlockFillableByBucket(String name, Material material, Class<? extends ItemBlock> itemBlockClass, boolean useWaterBottles) {
        super(name, material, itemBlockClass);
        this.useWaterBottles = useWaterBottles && AutomagyConfig.waterBottleAmount != -1;
    }

    public BlockFillableByBucket(String name, Material material, boolean useWaterBottles) {
        super(name, material);
        this.useWaterBottles = useWaterBottles && AutomagyConfig.waterBottleAmount != -1;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9) {
        ItemStack heldStack = player.inventory.getCurrentItem();
        if (heldStack != null) {
            FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(heldStack);
            if (liquid != null) {
                if (!world.isRemote) {
                    ITileWithTank te = (ITileWithTank)world.getTileEntity(x, y, z);
                    boolean returnBottle = false;
                    if (heldStack.isItemEqual(new ItemStack(Items.potionitem))) {
                        if (!this.useWaterBottles) {
                            return false;
                        }

                        if (AutomagyConfig.getRealWaterBottleAmount() == 0) {
                            FluidStack fillFluid = te.getTank().getFluid();
                            if (fillFluid != null && fillFluid.isFluidEqual(FluidRegistry.getFluidStack(FluidRegistry.WATER.getName(), 1))) {
                                TjUtil.consumePlayerItem(player, player.inventory.currentItem, new ItemStack(Items.glass_bottle));
                            }

                            return true;
                        }

                        if (AutomagyConfig.getRealWaterBottleAmount() > 0) {
                            liquid.amount = AutomagyConfig.getRealWaterBottleAmount();
                            returnBottle = true;
                        }
                    }

                    int amount = te.fill(ForgeDirection.UNKNOWN, liquid, false);
                    if (amount == liquid.amount) {
                        te.fill(ForgeDirection.UNKNOWN, liquid, true);
                        if (!player.capabilities.isCreativeMode) {
                            if (returnBottle) {
                                TjUtil.consumePlayerItem(player, player.inventory.currentItem, new ItemStack(Items.glass_bottle), true);
                            } else if (FluidContainerRegistry.isBucket(heldStack)) {
                                TjUtil.consumePlayerItem(player, player.inventory.currentItem, new ItemStack(Items.bucket), true);
                            } else if (heldStack.isItemEqual(new ItemStack(Items.mushroom_stew))) {
                                TjUtil.consumePlayerItem(player, player.inventory.currentItem, new ItemStack(Items.bowl), true);
                            } else {
                                TjUtil.consumePlayerItem(player, player.inventory.currentItem);
                            }
                        }
                    }
                } else if (!this.useWaterBottles && heldStack.isItemEqual(new ItemStack(Items.potionitem))) {
                    return false;
                }

                return true;
            }

            if (FluidContainerRegistry.isBucket(heldStack)) {
                if (!world.isRemote) {
                    ITileWithTank tex = (ITileWithTank)world.getTileEntity(x, y, z);
                    FluidStack fillFluid = tex.getTank().getFluid();
                    if (fillFluid == null) {
                        return true;
                    }

                    ItemStack fillStack = FluidContainerRegistry.fillFluidContainer(fillFluid, heldStack);
                    if (fillStack != null) {
                        int amount = FluidContainerRegistry.getFluidForFilledItem(fillStack).amount;
                        FluidStack drained = tex.drain(ForgeDirection.UNKNOWN, amount, false);
                        if (amount == drained.amount) {
                            tex.drain(ForgeDirection.UNKNOWN, amount, true);
                            if (!player.capabilities.isCreativeMode) {
                                if (heldStack.stackSize == 1) {
                                    player.inventory.setInventorySlotContents(player.inventory.currentItem, fillStack);
                                } else {
                                    TjUtil.consumePlayerItem(player, player.inventory.currentItem);
                                    if (!player.inventory.addItemStackToInventory(fillStack)) {
                                        player.dropPlayerItemWithRandomChoice(fillStack, false);
                                    } else if (player instanceof EntityPlayerMP) {
                                        ((EntityPlayerMP)player).sendContainerToPlayer(player.inventoryContainer);
                                    }
                                }
                            }
                        }
                    } else if (fillFluid.amount >= 1000) {
                        if (fillFluid.isFluidEqual(new FluidStack(ModBlocks.fluidMilk, 1))) {
                            tex.drain(ForgeDirection.UNKNOWN, 1000, true);
                            TjUtil.consumePlayerItem(player, player.inventory.currentItem, new ItemStack(Items.milk_bucket));
                        } else if (fillFluid.isFluidEqual(new FluidStack(ModBlocks.fluidMushroom, 1))) {
                            tex.drain(ForgeDirection.UNKNOWN, 1000, true);
                            TjUtil.consumePlayerItem(player, player.inventory.currentItem, new ItemStack(ModItems.bucketMushroom));
                        }
                    } else if (player.capabilities.isCreativeMode && heldStack.isItemEqual(new ItemStack(Items.bucket))) {
                        tex.drain(ForgeDirection.UNKNOWN, 1000, true);
                    }
                }

                return true;
            }

            if (this.useWaterBottles && heldStack.isItemEqual(new ItemStack(Items.glass_bottle))) {
                if (!world.isRemote) {
                    ITileWithTank texx = (ITileWithTank)world.getTileEntity(x, y, z);
                    FluidStack fillFluidx = texx.getTank().getFluid();
                    if (fillFluidx != null && fillFluidx.isFluidEqual(FluidRegistry.getFluidStack(FluidRegistry.WATER.getName(), 1))) {
                        if (AutomagyConfig.getRealWaterBottleAmount() == 0) {
                            TjUtil.consumePlayerItem(player, player.inventory.currentItem, new ItemStack(Items.potionitem));
                        } else if (texx.drainExactAmount(AutomagyConfig.getRealWaterBottleAmount(), true)) {
                            TjUtil.consumePlayerItem(player, player.inventory.currentItem, new ItemStack(Items.potionitem));
                        }
                    }
                }

                return true;
            }

            if (heldStack.isItemEqual(new ItemStack(Items.bowl))) {
                if (!world.isRemote) {
                    ITileWithTank texx = (ITileWithTank)world.getTileEntity(x, y, z);
                    FluidStack fillFluidx = texx.getTank().getFluid();
                    if (fillFluidx != null) {
                        if (fillFluidx.isFluidEqual(new FluidStack(CompatibilityManager.mushroomSoupBlock.getFluid(), 1))) {
                            if (texx.drainExactAmount(1000, true)) {
                                TjUtil.consumePlayerItem(player, player.inventory.currentItem, new ItemStack(Items.mushroom_stew));
                            }
                        } else if (fillFluidx.isFluidEqual(new FluidStack(ModBlocks.fluidVishroom, 1)) && texx.drainExactAmount(1000, true)) {
                            TjUtil.consumePlayerItem(player, player.inventory.currentItem, new ItemStack(ModItems.food, 1, 0));
                        }
                    }
                }

                return true;
            }

            if (heldStack.isItemEqual(new ItemStack(Items.mushroom_stew))) {
                if (!world.isRemote) {
                    ITileWithTank texx = (ITileWithTank)world.getTileEntity(x, y, z);
                    if (texx.fillExactAmount(CompatibilityManager.mushroomSoupBlock)) {
                        TjUtil.consumePlayerItem(player, player.inventory.currentItem, new ItemStack(Items.bowl));
                    }
                }

                return true;
            }

            if (heldStack.isItemEqual(new ItemStack(ModItems.food, 1, 0))) {
                if (!world.isRemote) {
                    ITileWithTank texx = (ITileWithTank)world.getTileEntity(x, y, z);
                    if (texx.fillExactAmount(new FluidStack(ModBlocks.fluidVishroom, 1000))) {
                        TjUtil.consumePlayerItem(player, player.inventory.currentItem, new ItemStack(Items.bowl));
                    }
                }

                return true;
            }
        }

        return false;
    }
}
