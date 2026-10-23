package tuhljin.automagy.lib;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.ReflectionHelper;
import io.netty.buffer.ByteBuf;
import java.awt.Color;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import org.apache.logging.log4j.Level;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.entities.EntityXPOrbSuppressFollow;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;

public class TjUtil {
    private static Random rand = new Random();
    public static final char decimalSeparator;
    private static char[] c = new char[]{'k', 'm', 'b', 't'};
    private static int MAX_XP_ORB_VALUE = 100;
    private static Method getDeathSound = null;
    private static Method getSoundVolume = null;

    public static void sendChatToPlayer(EntityPlayer player, String message) {
        message = StatCollector.translateToLocal(message);
        sendRawChatToPlayer(player, message, EnumChatFormatting.DARK_AQUA);
    }

    public static void sendFormattedChatToPlayer(EntityPlayer player, String message, Object... data) {
        message = StatCollector.translateToLocalFormatted(message, data);
        sendRawChatToPlayer(player, message, EnumChatFormatting.DARK_AQUA);
    }

    public static void sendRawChatToPlayer(EntityPlayer player, String message, EnumChatFormatting chatFormatting) {
        if (chatFormatting != null) {
            message = chatFormatting + message;
        }

        player.addChatMessage(new ChatComponentText(message));
    }

    public static ArrayList<String> getMultiLineLocalizedString(String message) {
        message = StatCollector.translateToLocal(message);
        String[] s = message.split("\\\\n");
        return new ArrayList<>(Arrays.asList(s));
    }

    public static Pattern getSafePatternUsingAsteriskForWildcard(String str) {
        String s = str.replaceAll("\\*+", ".*");
        s = Pattern.quote(s);
        s = s.replace(".*", "\\E.*\\Q");
        s = s.replace("\\Q\\E", "");

        try {
            return Pattern.compile(s);
        } catch (Exception var4) {
            FMLLog.severe("[Automagy] Failed to create regex pattern. Name filter will not function properly. String was: " + str);
            return null;
        }
    }

    public static int getSideFromEntityFacing(EntityLivingBase entity) {
        int l = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 2.5) & 3;
        switch (l) {
            case 0:
                return 2;
            case 1:
                return 5;
            case 2:
                return 3;
            case 3:
                return 4;
            default:
                return -1;
        }
    }

    public static int getOppositeSide(int side) {
        return side >= 0 && side < ForgeDirection.OPPOSITES.length ? ForgeDirection.OPPOSITES[side] : 6;
    }

    public static int getSWNESide(int side) {
        switch (side) {
            case 2:
                return 2;
            case 3:
                return 0;
            case 4:
                return 1;
            case 5:
                return 3;
            default:
                return -1;
        }
    }

    public static boolean isEntityLookingDown(EntityLivingBase entity) {
        return entity.rotationPitch > 0.0F;
    }

    public static int getNextSideOnBlockFromDir(int side, int dir) {
        switch (side) {
            case 0:
            case 1:
                switch (dir) {
                    case 0:
                    case 1:
                        return 6;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        return dir;
                    default:
                        return 6;
                }
            case 2:
            case 3:
                switch (dir) {
                    case 0:
                        return 0;
                    case 1:
                        return 1;
                    case 2:
                    case 3:
                        return 6;
                    case 4:
                        return 4;
                    case 5:
                        return 5;
                    default:
                        return 6;
                }
            case 4:
            case 5:
                switch (dir) {
                    case 0:
                        return 0;
                    case 1:
                        return 1;
                    case 2:
                        return 2;
                    case 3:
                        return 3;
                    case 4:
                    case 5:
                        return 6;
                }
        }

        return 6;
    }

    public static float getDistanceBetweenPoints(int x1, int y1, int z1, int x2, int y2, int z2) {
        float f = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2);
        return MathHelper.sqrt_float(f);
    }

    public static double getDistanceBetweenPoints_squared(double x1, double y1, double z1, double x2, double y2, double z2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2);
    }

    public static double getDistanceBetweenPoints_double(double x1, double y1, double z1, double x2, double y2, double z2) {
        double f = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2);
        return MathHelper.sqrt_double(f);
    }

    public static void dropItemsIntoWorld(IInventory inventory, World world, int x, int y, int z, Block block) {
        if (inventory != null) {
            for (int i1 = 0; i1 < inventory.getSizeInventory(); i1++) {
                ItemStack stack = inventory.getStackInSlot(i1);
                if (stack != null) {
                    dropItemIntoWorld(stack, world, x, y, z);
                }
            }

            if (block != null) {
                world.updateNeighborsAboutBlockChange(x, y, z, block);
            }
        }
    }

    public static void dropItemsIntoWorld(IInventory inventory, World world, double x, double y, double z) {
        if (inventory != null) {
            for (int i1 = 0; i1 < inventory.getSizeInventory(); i1++) {
                ItemStack stack = inventory.getStackInSlot(i1);
                if (stack != null) {
                    dropItemIntoWorld(stack, world, x, y, z);
                }
            }
        }
    }

    public static void dropItemsIntoWorld(ArrayList<ItemStack> list, World world, int x, int y, int z) {
        if (list != null) {
            int size = list.size();

            for (int i1 = 0; i1 < size; i1++) {
                ItemStack stack = list.get(i1);
                if (stack != null) {
                    dropItemIntoWorld(stack, world, x, y, z);
                }
            }
        }
    }

    public static void dropItemsIntoWorld(ItemStack[] list, World world, int x, int y, int z) {
        if (list != null) {
            for (ItemStack stack : list) {
                if (stack != null) {
                    dropItemIntoWorld(stack, world, x, y, z);
                }
            }
        }
    }

    public static EntityItem dropItemIntoWorld(ItemStack stack, World world, double x, double y, double z) {
        float f = rand.nextFloat() * 0.8F + 0.1F;
        float f1 = rand.nextFloat() * 0.8F + 0.1F;
        EntityItem entityitem = null;
        float f2 = rand.nextFloat() * 0.8F + 0.1F;

        while (stack.stackSize > 0) {
            int j1 = rand.nextInt(21) + 10;
            if (j1 > stack.stackSize) {
                j1 = stack.stackSize;
            }

            stack.stackSize -= j1;
            entityitem = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(stack.getItem(), j1, stack.getMetadata()));
            float f3 = 0.05F;
            entityitem.motionX = (float)rand.nextGaussian() * f3;
            entityitem.motionY = (float)rand.nextGaussian() * f3 + 0.2F;
            entityitem.motionZ = (float)rand.nextGaussian() * f3;
            if (stack.hasTagCompound()) {
                entityitem.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
            }

            world.spawnEntityInWorld(entityitem);
        }

        return entityitem;
    }

    public static EntityItem dropItemIntoWorldSimple(ItemStack stack, World world, double x, double y, double z) {
        EntityItem entityitem = new EntityItem(world, x, y, z, stack);
        float f3 = 0.05F;
        entityitem.motionX = (float)rand.nextGaussian() * f3;
        entityitem.motionY = (float)rand.nextGaussian() * f3 + 0.2F;
        entityitem.motionZ = (float)rand.nextGaussian() * f3;
        world.spawnEntityInWorld(entityitem);
        return entityitem;
    }

    public static boolean canItemsStack(ItemStack stack1, ItemStack stack2) {
        return (stack1 == null || stack2 == null || areItemsEqualIgnoringSize(stack1, stack2)) && stack1.isStackable();
    }

    public static boolean areItemsEqualIgnoringSize(ItemStack stack1, ItemStack stack2) {
        return stack1 != null
            && stack2 != null
            && stack1.getItem() == stack2.getItem()
            && (!stack2.getHasSubtypes() || stack2.getMetadata() == stack1.getMetadata())
            && ItemStack.areItemStackTagsEqual(stack2, stack1);
    }

    public static ItemStack addToInventory(ItemStack stack, IInventory inv, int firstSlot, int lastSlot) {
        stack = stack.copy();
        boolean reverse = firstSlot > lastSlot;
        int offSlot = reverse ? lastSlot - 1 : lastSlot + 1;
        int stacksTo = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
        if (stacksTo > 1 && stack.isStackable()) {
            int slot = firstSlot;

            while (slot != offSlot) {
                ItemStack slottedStack = inv.getStackInSlot(slot);
                if (slottedStack != null && canItemsStack(stack, slottedStack)) {
                    int newSize = stack.stackSize + slottedStack.stackSize;
                    if (newSize <= stacksTo) {
                        ItemStack newStack = slottedStack.copy();
                        newStack.stackSize = newSize;
                        inv.setInventorySlotContents(slot, newStack);
                        return null;
                    }

                    if (slottedStack.stackSize < stacksTo) {
                        stack.stackSize = newSize - stacksTo;
                        ItemStack newStack = slottedStack.copy();
                        newStack.stackSize = stacksTo;
                        inv.setInventorySlotContents(slot, newStack);
                    }
                }

                if (reverse) {
                    slot--;
                } else {
                    slot++;
                }
            }
        }

        int slot = firstSlot;

        while (slot != offSlot) {
            ItemStack slottedStackx = inv.getStackInSlot(slot);
            if (slottedStackx == null) {
                if (stack.stackSize <= stacksTo) {
                    inv.setInventorySlotContents(slot, stack.copy());
                    return null;
                }

                ItemStack newStack = stack.copy();
                newStack.stackSize = stacksTo;
                stack.stackSize -= stacksTo;
                inv.setInventorySlotContents(slot, newStack);
            }

            if (reverse) {
                slot--;
            } else {
                slot++;
            }
        }

        return stack;
    }

    public static ItemStack addToInventory(ItemStack stack, IInventory inv) {
        return addToInventory(stack, inv, 0, inv.getSizeInventory() - 1);
    }

    public static boolean isPlayerOnline(String name) {
        for (EntityPlayerMP curr : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (curr.getCommandSenderName() == name) {
                return true;
            }
        }

        return false;
    }

    public static boolean areFloatsAlmostEqual_lowprec(float f1, float f2) {
        float epsilon = 1.0E-5F;
        return Math.abs(f1 - f2) < epsilon;
    }

    public static boolean areDoublesAlmostEqual_lowprec(double d1, double d2) {
        double epsilon = 1.0E-5;
        return Math.abs(d1 - d2) < epsilon;
    }

    public static int randInt(Random rand, int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }

    public static int round_to_int(double d) {
        return (int)Math.floor(d + 0.5);
    }

    public static BlockCoord getBlockCoordPlayerIsLookingAt(EntityPlayer player, World world) {
        if (player.worldObj != world && player.worldObj.provider.dimensionId != world.provider.dimensionId) {
            return null;
        } else {
            float f = 1.0F;
            float f1 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
            float f2 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
            double d0 = player.prevPosX + (player.posX - player.prevPosX) * f;
            double d1 = player.prevPosY + (player.posY - player.prevPosY) * f + 1.62 - player.yOffset;
            double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * f;
            Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
            float f3 = MathHelper.cos(-f2 * (float) (Math.PI / 180.0) - (float) Math.PI);
            float f4 = MathHelper.sin(-f2 * (float) (Math.PI / 180.0) - (float) Math.PI);
            float f5 = -MathHelper.cos(-f1 * (float) (Math.PI / 180.0));
            float f6 = MathHelper.sin(-f1 * (float) (Math.PI / 180.0));
            float f7 = f4 * f5;
            float f8 = f3 * f5;
            double d3 = 5.0;
            Vec3 vec31 = vec3.addVector(f7 * d3, f6 * d3, f8 * d3);
            MovingObjectPosition movingobjectposition = world.rayTraceBlocks(vec3, vec31, true);
            if (movingobjectposition == null) {
                return null;
            } else if (movingobjectposition.typeOfHit == MovingObjectType.BLOCK) {
                int ix = movingobjectposition.blockX;
                int iy = movingobjectposition.blockY;
                int iz = movingobjectposition.blockZ;
                return new BlockCoord(ix, iy, iz);
            } else {
                return null;
            }
        }
    }

    public static boolean isAcceptableSurfaceForBlock(World world, int x, int y, int z, boolean allowGlowstone, boolean allowGlass, boolean allowPedestal) {
        if (World.doesBlockHaveSolidTopSurface(world, x, y, z)) {
            return true;
        } else {
            if (allowGlowstone || allowGlass || allowPedestal) {
                Block block = world.getBlock(x, y, z);
                if (allowGlowstone && block == Blocks.glowstone) {
                    return true;
                }

                if (allowGlass && block == Blocks.glass) {
                    return true;
                }

                if (allowPedestal && ThaumcraftExtension.blockIsPedestal(block, world.getBlockMetadata(x, y, z), false)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean playerHasItem(EntityPlayer player, HashableItemWithoutSize item) {
        InventoryPlayer inv = player.inventory;

        for (int i = 0; i < inv.mainInventory.length; i++) {
            if (inv.mainInventory[i] != null && item.isSameItem(inv.mainInventory[i])) {
                return true;
            }
        }

        return false;
    }

    public static boolean playerHasPartialStackedItem(EntityPlayer player, ItemStack stack) {
        if (!stack.isStackable()) {
            return false;
        } else {
            int max = stack.getMaxStackSize();
            if (max <= 1) {
                return false;
            } else {
                InventoryPlayer inv = player.inventory;

                for (int i = 0; i < inv.mainInventory.length; i++) {
                    ItemStack invStack = inv.mainInventory[i];
                    if (invStack != null && invStack.isItemEqual(stack) && invStack.stackSize < max) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    public static void consumePlayerItem(EntityPlayer player, int slot, ItemStack replace, boolean replaceOnlyIfNoContainer) {
        boolean replaced = false;
        boolean needUpdate = false;
        boolean containerTookSlot = false;
        InventoryPlayer inv = player.inventory;
        ItemStack stack = inv.getStackInSlot(slot);
        if (stack != null) {
            ItemStack container = null;
            if (stack.getItem().hasContainerItem(stack)) {
                container = stack.getItem().getContainerItem(stack);
                if (replaceOnlyIfNoContainer && container != null) {
                    replace = null;
                }
            }

            stack.stackSize--;
            if (stack.stackSize == 0) {
                if (container != null && !playerHasPartialStackedItem(player, container)) {
                    inv.setInventorySlotContents(slot, container);
                    containerTookSlot = true;
                    needUpdate = true;
                }

                if (!containerTookSlot && replace != null && !playerHasPartialStackedItem(player, replace)) {
                    inv.setInventorySlotContents(slot, replace);
                    replaced = true;
                    needUpdate = true;
                }
            }

            if (container != null && !containerTookSlot) {
                if (!inv.addItemStackToInventory(container)) {
                    if (player instanceof EntityPlayerMP) {
                        player.dropPlayerItemWithRandomChoice(container, false);
                    }
                } else {
                    needUpdate = true;
                }
            }

            if (replace != null && !replaced) {
                if (!inv.addItemStackToInventory(replace)) {
                    if (player instanceof EntityPlayerMP) {
                        player.dropPlayerItemWithRandomChoice(replace, false);
                    }
                } else {
                    needUpdate = true;
                }
            }

            if (needUpdate && player instanceof EntityPlayerMP && !(player instanceof FakePlayer)) {
                ((EntityPlayerMP)player).sendContainerToPlayer(player.inventoryContainer);
            }
        }
    }

    public static void consumePlayerItem(EntityPlayer player, int slot, ItemStack replace) {
        consumePlayerItem(player, slot, replace, false);
    }

    public static void consumePlayerItem(EntityPlayer player, int slot) {
        consumePlayerItem(player, slot, null, false);
    }

    public static ArrayList<EntityPlayer> getPlayersInRangeSquared(World world, double x, double y, double z, double rangeSquared) {
        ArrayList<EntityPlayer> list = new ArrayList<>();

        for (int i = 0; i < world.playerEntities.size(); i++) {
            EntityPlayer player = (EntityPlayer)world.playerEntities.get(i);
            double dist = player.getDistanceSq(x, y, z);
            if (rangeSquared < 0.0 || dist < rangeSquared) {
                list.add(player);
            }
        }

        return list;
    }

    public static ArrayList<EntityPlayer> getPlayersIn2DRangeSquared(World world, double x, double z, double rangeSquared) {
        ArrayList<EntityPlayer> list = new ArrayList<>();

        for (int i = 0; i < world.playerEntities.size(); i++) {
            EntityPlayer player = (EntityPlayer)world.playerEntities.get(i);
            double dist = get2DRangeSquared(x, z, player.posX, player.posZ);
            if (rangeSquared < 0.0 || dist < rangeSquared) {
                list.add(player);
            }
        }

        return list;
    }

    public static double get2DRangeSquared(double x, double z, double x2, double z2) {
        double xx = x - x2;
        double zz = z - z2;
        return xx * xx + zz * zz;
    }

    public static boolean willAdditionOverflowInt(int left, int right) {
        return right < 0 && right != Integer.MIN_VALUE ? willSubtractionOverflowInt(left, -right) : (~(left ^ right) & (left ^ left + right)) < 0;
    }

    public static boolean willSubtractionOverflowInt(int left, int right) {
        return right < 0 ? willAdditionOverflowInt(left, -right) : ((left ^ right) & (left ^ left - right)) < 0;
    }

    public static int overflowSafeAddInt(int a, int b) {
        return willAdditionOverflowInt(a, b) ? Integer.MAX_VALUE : a + b;
    }

    public static int overflowSafeSubtractInt(int a, int b) {
        return willSubtractionOverflowInt(a, b) ? Integer.MIN_VALUE : a - b;
    }

    private static String numberFormatLetter(double n, int iteration) {
        double d = (long)n / 100L / 10.0;
        boolean isRound = d * 10.0 % 10.0 == 0.0;
        return d < 1000.0
            ? (!(d > 99.9) && !isRound && (isRound || !(d > 9.99)) ? d + "" : (int)d * 10 / 10) + "" + c[iteration]
            : numberFormatLetter(d, iteration + 1);
    }

    public static String numberFormatLetter(int n) {
        return n < 1000 ? Integer.toString(n) : numberFormatLetter(n, 0);
    }

    public static void itemStackToBytes(ByteBuf buf, ItemStack stack) {
        if (stack == null) {
            buf.writeShort(-1);
        } else {
            buf.writeShort(Item.getIdFromItem(stack.getItem()));
            buf.writeInt(stack.stackSize);
            buf.writeShort(stack.getMetadata());
            ByteBufUtils.writeTag(buf, stack.getTagCompound());
        }
    }

    public static ItemStack itemStackFromBytes(ByteBuf buf) {
        int itemID = buf.readShort();
        if (itemID < 0) {
            return null;
        } else {
            int size = buf.readInt();
            short damage = buf.readShort();
            ItemStack stack = new ItemStack(Item.getItemById(itemID), size, damage);
            NBTTagCompound tag = ByteBufUtils.readTag(buf);
            if (tag != null) {
                stack.setTagCompound(tag);
            }

            return stack;
        }
    }

    public static NBTTagCompound writeLargeItemStackToNBT(ItemStack stack, NBTTagCompound nbt) {
        nbt.setShort("id", (short)Item.getIdFromItem(stack.getItem()));
        nbt.setInteger("Count", stack.stackSize);
        nbt.setShort("Damage", (short)stack.getMetadata());
        if (stack.stackTagCompound != null) {
            nbt.setTag("tag", stack.stackTagCompound);
        }

        return nbt;
    }

    public static ItemStack readLargeItemStackFromNBT(NBTTagCompound nbt) {
        Item item = Item.getItemById(nbt.getShort("id"));
        if (item == null) {
            return null;
        } else {
            int damage = Math.max(nbt.getShort("Damage"), 0);
            ItemStack stack = new ItemStack(item, nbt.getInteger("Count"), damage);
            if (nbt.hasKey("tag", 10)) {
                stack.stackTagCompound = nbt.getCompoundTag("tag");
            }

            return stack;
        }
    }

    public static int getPlayerXP(EntityPlayer player) {
        return (int)(getExperienceForLevel(player.experienceLevel) + player.experience * player.xpBarCap());
    }

    public static int getExperienceForLevel(int level) {
        if (level == 0) {
            return 0;
        } else if (level > 0 && level < 16) {
            return level * 17;
        } else {
            return level > 15 && level < 31
                ? (int)(1.5 * Math.pow(level, 2.0) - 29.5 * level + 360.0)
                : (int)(3.5 * Math.pow(level, 2.0) - 151.5 * level + 2220.0);
        }
    }

    public static int getXpToNextLevel(int level) {
        int levelXP = getExperienceForLevel(level);
        int nextXP = getExperienceForLevel(level + 1);
        return nextXP - levelXP;
    }

    public static int getLevelForExperience(int experience) {
        int i = 0;

        while (getExperienceForLevel(i) <= experience) {
            i++;
        }

        return i - 1;
    }

    public static void spawnExperienceIntoWorld(int xp, World world, double x, double y, double z, boolean addMotion, int orbValueCap, int suppressFollowTime) {
        while (xp > 0) {
            int orbXP = EntityXPOrb.getXPSplit(xp);
            if (orbXP > orbValueCap) {
                orbXP = orbValueCap;
            }

            xp -= orbXP;
            EntityXPOrb orb;
            if (suppressFollowTime > 0) {
                orb = new EntityXPOrbSuppressFollow(world, x, y, z, orbXP, suppressFollowTime);
            } else {
                orb = new EntityXPOrb(world, x, y, z, orbXP);
            }

            if (addMotion) {
                orb.motionX *= 0.2;
                orb.motionY *= 0.5;
                orb.motionZ *= 0.2;
            }

            world.spawnEntityInWorld(orb);
        }
    }

    public static void spawnSuppressedExperienceIntoWorld(int xp, int suppressFollowTime, World world, double x, double y, double z) {
        spawnExperienceIntoWorld(xp, world, x, y, z, true, MAX_XP_ORB_VALUE, suppressFollowTime);
    }

    public static void spawnExperienceIntoWorld(int xp, World world, double x, double y, double z, boolean addMotion) {
        spawnExperienceIntoWorld(xp, world, x, y, z, addMotion, MAX_XP_ORB_VALUE, 0);
    }

    public static boolean isPrecipitationAt(World world, int x, int z) {
        if (!world.isRaining()) {
            return false;
        } else {
            BiomeGenBase bg = world.getBiomeGenForCoords(x, z);
            return bg.getEnableSnow() || bg.canSpawnLightningBolt();
        }
    }

    public static boolean isChunkLoaded(World world, int blockX, int blockZ) {
        return world.getChunkFromBlockCoords(blockX, blockZ).isChunkLoaded;
    }

    public static boolean isBiomeAt(World world, int x, int z, BiomeGenBase biome) {
        BiomeGenBase b = world.getBiomeGenForCoords(x, z);
        return b == biome;
    }

    public static boolean isBiomeAt(World world, int x, int z, String biome) {
        BiomeGenBase b = world.getBiomeGenForCoords(x, z);
        return b.biomeName.equals(biome);
    }

    public static ArrayList<ItemStack> getDropsFromBlock(World world, int x, int y, int z, boolean includeSilkDrops) {
        Block block = world.getBlock(x, y, z);
        if (block == null) {
            return null;
        } else {
            int md = world.getBlockMetadata(x, y, z);
            ArrayList<ItemStack> drops = block.getDrops(world, x, y, z, md, 0);
            if (includeSilkDrops) {
                ItemStack stack = getStackFromBlock(block, world, x, y, z);
                if (stack != null) {
                    boolean found = false;

                    for (ItemStack drop : drops) {
                        if (areItemsEqualIgnoringSize(stack, drop)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        drops.add(stack);
                    }
                }
            }

            return drops;
        }
    }

    public static ItemStack getStackFromBlock(Block block, World world, int x, int y, int z) {
        Item item = Item.getItemFromBlock(block);
        return item == null ? null : new ItemStack(item, 1, block.getDamageValue(world, x, y, z));
    }

    public static ItemStack getStackFromBlock(World world, int x, int y, int z) {
        return getStackFromBlock(world.getBlock(x, y, z), world, x, y, z);
    }

    public static String getBlockNameAt(World world, int x, int y, int z) {
        String name = null;
        if (isChunkLoaded(world, x, z)) {
            Block block = world.getBlock(x, y, z);
            if (block != null) {
                Item blockItem = block.getItem(world, x, y, z);
                if (blockItem == null) {
                    name = block.getUnlocalizedName();
                } else {
                    ItemStack blockStack = new ItemStack(blockItem, 1, block.getDamageValue(world, x, y, z));
                    name = blockItem.getUnlocalizedName(blockStack);
                }

                if (name != null) {
                    if (name.isEmpty()) {
                        return null;
                    }

                    String temp = name + ".name";
                    String name2 = StatCollector.translateToLocal(temp);
                    if (temp.equals(name2)) {
                        temp = name + "." + world.getBlockMetadata(x, y, z) + ".name";
                        name2 = StatCollector.translateToLocal(temp);
                        if (temp.equals(name2)) {
                            if (name.substring(0, 5).equals("tile.")) {
                                name = name.substring(5);
                            }

                            name = name.substring(0, 1).toUpperCase() + name.substring(1);
                        } else {
                            name = name2;
                        }
                    } else {
                        name = name2;
                    }
                }
            }
        }

        return name;
    }

    public static Fluid canDrainTank(IFluidHandler tank, ForgeDirection dir, Fluid fluid) {
        if (fluid != null) {
            return tank.canDrain(dir, fluid) ? fluid : null;
        } else {
            FluidTankInfo[] infos = tank.getTankInfo(dir);
            if (infos != null && infos.length >= 1) {
                for (FluidTankInfo info : infos) {
                    if (info.fluid != null && info.capacity > 0) {
                        fluid = info.fluid.getFluid();
                        if (tank.canDrain(dir, fluid)) {
                            return fluid;
                        }
                    }
                }

                return null;
            } else {
                return null;
            }
        }
    }

    public static Fluid canDrainTank(IFluidHandler tank, ForgeDirection dir, FluidStack fluidStack) {
        return canDrainTank(tank, dir, fluidStack == null ? null : fluidStack.getFluid());
    }

    public static String getBlockUnlocalizedName(Block block, World world, int x, int y, int z) {
        Item blockItem = Item.getItemFromBlock(block);
        if (blockItem == null) {
            return block.getUnlocalizedName();
        } else {
            ItemStack blockStack = new ItemStack(blockItem, 1, block.getDamageValue(world, x, y, z));
            return blockItem.getUnlocalizedName(blockStack);
        }
    }

    public static void playDeathSound(EntityLiving entity, World world, double x, double y, double z, float pitch) {
        if (getDeathSound == null) {
            try {
                getDeathSound = ReflectionHelper.findMethod(EntityLivingBase.class, entity, new String[]{"getDeathSound", "func_70673_aS"});
                getSoundVolume = ReflectionHelper.findMethod(EntityLivingBase.class, entity, new String[]{"getSoundVolume", "func_70599_aP"});
            } catch (Exception var12) {
                FMLLog.warning("[Automagy] Failed to find mob death sound.");
                var12.printStackTrace();
                return;
            }
        }

        try {
            String s = (String)getDeathSound.invoke(entity);
            Float v = (Float)getSoundVolume.invoke(entity);
            if (s != null && !s.isEmpty() && v != null) {
                world.playSoundEffect(x, y, z, s, v, pitch);
            }
        } catch (Exception var11) {
            FMLLog.warning("[Automagy] Failed to play mob death sound.");
            var11.printStackTrace();
        }
    }

    public static float dualInterpolate(float a, float b, float d) {
        return d < 0.5
            ? tuhljin.automagy.codechicken.lib.math.MathHelper.interpolate(a, b, d * 2.0F)
            : tuhljin.automagy.codechicken.lib.math.MathHelper.interpolate(b, a, (d - 0.5F) * 2.0F);
    }

    public static boolean doesBlockMovement(IBlockAccess blockaccess, int x, int y, int z) {
        Block block = blockaccess.getBlock(x, y, z);
        return block != null && block.getMaterial().blocksMovement();
    }

    public static int colorApproachLinear(int a, int b, double max) {
        Color colorA = new Color(a);
        Color colorB = new Color(b);
        int red = (int)Math.round(tuhljin.automagy.codechicken.lib.math.MathHelper.approachLinear((double)colorA.getRed(), (double)colorB.getRed(), max));
        int green = (int)Math.round(tuhljin.automagy.codechicken.lib.math.MathHelper.approachLinear((double)colorA.getGreen(), (double)colorB.getGreen(), max));
        int blue = (int)Math.round(tuhljin.automagy.codechicken.lib.math.MathHelper.approachLinear((double)colorA.getBlue(), (double)colorB.getBlue(), max));
        Color result = new Color(red / 255.0F, green / 255.0F, blue / 255.0F);
        return result.getRGB();
    }

    public static void printError(String s) {
        FMLLog.log(Level.ERROR, "[Automagy] " + s);
    }

    public static void debug(String s) {
    }

    public static void debugxyz(int x, int y, int z, String s) {
    }

    static {
        DecimalFormat format = (DecimalFormat)DecimalFormat.getInstance();
        decimalSeparator = format.getDecimalFormatSymbols().getDecimalSeparator();
    }
}
