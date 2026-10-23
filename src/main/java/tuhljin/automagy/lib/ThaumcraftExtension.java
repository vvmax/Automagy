package tuhljin.automagy.lib;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.potions.PotionFluxTaint;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockCosmeticSolid;
import thaumcraft.common.blocks.BlockMetalDevice;
import thaumcraft.common.blocks.BlockStoneDevice;
import thaumcraft.common.blocks.BlockWoodenDevice;
import thaumcraft.common.blocks.ItemJarFilled;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.monster.EntityPech;
import thaumcraft.common.items.ItemResource;
import thaumcraft.common.items.armor.ItemHoverHarness;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockZap;
import thaumcraft.common.lib.network.playerdata.PacketResearchComplete;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;
import thaumcraft.common.tiles.TileEssentiaReservoir;
import thaumcraft.common.tiles.TileHole;
import thaumcraft.common.tiles.TileMirror;
import thaumcraft.common.tiles.TileMirrorEssentia;
import thaumcraft.common.tiles.TileWandPedestal;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;

public class ThaumcraftExtension {
    private static Boolean reservoirClassExists = null;
    static Method placeItemStackIntoInventory;
    static Method setBiomeAt;

    public static void modComms() {
        FMLInterModComms.sendMessage("Thaumcraft", "championWhiteList", "Automagy.WispNether:2");
    }

    public static void setupPechTrade() {
        HashMap<Integer, Integer> valuedItems = null;

        try {
            Field f = ReflectionHelper.findField(EntityPech.class, "valuedItems");
            valuedItems = (HashMap<Integer, Integer>)f.get(null);
        } catch (Exception var2) {
            FMLLog.severe("[Automagy] Error: Failed to alter what pechs value.");
        }

        if (valuedItems != null) {
            valuedItems.put(Item.getIdFromItem(ModItems.edible), 7);
            FMLLog.info("[Automagy] Pech trade integration complete.");
        }
    }

    public static boolean addFluxTaintPotionEffect(EntityLivingBase entity, int duration, int amplifier) {
        try {
            entity.addPotionEffect(new PotionEffect(PotionFluxTaint.instance.id, duration, amplifier, false));
            return true;
        } catch (Exception var4) {
            return false;
        }
    }

    public static boolean playerHasWandEquipped(EntityPlayer player) {
        ItemStack stack = player.getCurrentEquippedItem();
        return stack != null && stack.getItem() instanceof ItemWandCasting;
    }

    public static boolean playerHasWandEquippedNonSceptre(EntityPlayer player) {
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null) {
            return false;
        } else {
            Item item = stack.getItem();
            return item instanceof ItemWandCasting && !((ItemWandCasting)item).isSceptre(stack);
        }
    }

    public static boolean wandIsStaff(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemWandCasting ? ((ItemWandCasting)stack.getItem()).isStaff(stack) : false;
    }

    public static ItemStack getStackInRechargePedestal(TileEntity te) {
        if (!(te instanceof TileWandPedestal)) {
            return null;
        } else {
            ItemStack stack = null;
            if (!(te instanceof IInventory)) {
                FMLLog.warning("[Automagy] Error: Expected TileWandPedestal to implement IInventory.");
                return null;
            } else {
                try {
                    return ((IInventory)te).getStackInSlot(0);
                } catch (Exception var3) {
                    FMLLog.warning("[Automagy] Could not invoke TileWandPedestal method getStackInSlot.");
                    return null;
                }
            }
        }
    }

    public static boolean blockIsPedestal(Block block, int metadata, boolean allowWandRecharger) {
        if (block instanceof BlockStoneDevice) {
            if (metadata == 1) {
                return true;
            }

            if (metadata == 5) {
                return allowWandRecharger;
            }
        }

        return false;
    }

    public static boolean blockIsArcaneBore(Block block, int metadata, boolean allowWandRecharger) {
        return block instanceof BlockWoodenDevice ? metadata == 5 : false;
    }

    public static boolean tileIsPortableHole(TileEntity te, Block block) {
        return te instanceof TileHole && (block == null || ((TileHole)te).oldblock == block);
    }

    public static boolean blockIsFetter(World world, int x, int y, int z, boolean activeOnly) {
        Block block = world.getBlock(x, y, z);
        if (block instanceof BlockCosmeticSolid) {
            int metadata = world.getBlockMetadata(x, y, z);
            if (metadata == 9) {
                return !activeOnly;
            }

            if (metadata == 10) {
                return true;
            }
        }

        return false;
    }

    public static boolean blockIsObsidianTotem(IBlockAccess blockaccess, int x, int y, int z) {
        Block block = blockaccess.getBlock(x, y, z);
        return block instanceof BlockCosmeticSolid ? blockaccess.getBlockMetadata(x, y, z) == 0 : false;
    }

    public static boolean blockIsNitor(IBlockAccess blockaccess, int x, int y, int z) {
        return blockaccess.getBlock(x, y, z) == ConfigBlocks.blockAiry ? blockaccess.getBlockMetadata(x, y, z) == 1 : false;
    }

    public static boolean blockIsItemGrate(IBlockAccess blockaccess, int x, int y, int z, boolean openOnly) {
        Block block = blockaccess.getBlock(x, y, z);
        if (block instanceof BlockMetalDevice) {
            int metadata = blockaccess.getBlockMetadata(x, y, z);
            if (metadata == 6) {
                return !openOnly;
            }

            if (metadata == 5) {
                return true;
            }
        }

        return false;
    }

    public static boolean tileIsMirror(TileEntity te) {
        return te instanceof TileMirror || te instanceof TileMirrorEssentia;
    }

    public static boolean itemIsJarLabel(ItemStack stack) {
        return stack.getItem() instanceof ItemResource && stack.getMetadata() == 13;
    }

    public static ThaumcraftExtension.EssentiaJarItemRef getJarFromHarness(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemHoverHarness && stack.hasTagCompound()) {
            ItemStack jarStack = ItemStack.loadItemStackFromNBT(stack.stackTagCompound.getCompoundTag("jar"));
            if (jarStack != null) {
                Item item = jarStack.getItem();
                if (item instanceof ItemJarFilled) {
                    return new ThaumcraftExtension.EssentiaJarItemRef(jarStack, (ItemJarFilled)item);
                }
            }
        }

        return null;
    }

    public static boolean objectIsEssentiaReservoir(Object obj) {
        if (reservoirClassExists == null) {
            reservoirClassExists = false;

            try {
                Class.forName("thaumcraft.common.tiles.TileEssentiaReservoir");
                reservoirClassExists = true;
            } catch (Exception var2) {
            }
        }

        return !reservoirClassExists ? false : obj instanceof TileEssentiaReservoir;
    }

    public static AspectList getSortedAspectList(AspectList aspects) {
        if (aspects != null && aspects.size() >= 2) {
            AspectList aspectsNew = new AspectList();
            Aspect[] array = aspects.getAspectsSorted();

            for (int i = array.length - 1; i >= 0; i--) {
                Aspect a = array[i];
                if (!a.isPrimal()) {
                    aspectsNew.add(a, aspects.getAmount(a));
                }
            }

            for (int ix = array.length - 1; ix >= 0; ix--) {
                Aspect a = array[ix];
                if (a.isPrimal()) {
                    aspectsNew.add(a, aspects.getAmount(a));
                }
            }

            return aspectsNew;
        } else {
            return aspects;
        }
    }

    public static AspectList getAspectsFromItem(ItemStack stack) {
        if (stack.hasTagCompound()) {
            AspectList aspects = new AspectList();
            aspects.readFromNBT(stack.getTagCompound());
            return aspects.size() > 0 ? aspects : null;
        } else {
            return null;
        }
    }

    public static boolean isResearchComplete(EntityPlayer player, String key) {
        String name = player.getCommandSenderName();
        return ThaumcraftApiHelper.isResearchComplete(name, key);
    }

    public static boolean isResearchDiscovered(EntityPlayer player, String key) {
        String name = player.getCommandSenderName();
        return ThaumcraftApiHelper.isResearchComplete(name, key) || ThaumcraftApiHelper.isResearchComplete(name, "@" + key);
    }

    public static void triggerResearchUnlock(EntityPlayerMP player, String key) {
        PacketHandler.INSTANCE.sendTo(new PacketResearchComplete("@" + key), player);
        Thaumcraft.proxy.getResearchManager().completeResearch(player, "@" + key);
    }

    public static WorldSpecificCoordinates getLinkedMirrorCoordinates(TileEntity te) {
        if (te instanceof TileMirror) {
            TileMirror tem = (TileMirror)te;
            if (tem.linked) {
                return new WorldSpecificCoordinates(tem.linkDim, tem.linkX, tem.linkY, tem.linkZ);
            }
        } else if (te instanceof TileMirrorEssentia) {
            TileMirrorEssentia tem = (TileMirrorEssentia)te;
            if (tem.linked) {
                return new WorldSpecificCoordinates(tem.linkDim, tem.linkX, tem.linkY, tem.linkZ);
            }
        }

        return null;
    }

    public static int getTemporaryWarp(EntityPlayer player) {
        return Thaumcraft.proxy.getPlayerKnowledge().getWarpTemp(player.getCommandSenderName());
    }

    public static int getStickyWarp(EntityPlayer player) {
        return Thaumcraft.proxy.getPlayerKnowledge().getWarpSticky(player.getCommandSenderName());
    }

    public static void resetWarpCounter(EntityPlayer player) {
        Thaumcraft.proxy
            .getPlayerKnowledge()
            .setWarpCounter(player.getCommandSenderName(), Thaumcraft.proxy.getPlayerKnowledge().getWarpTotal(player.getCommandSenderName()));
    }

    public static ItemFocusBasic getWandFocus(ItemStack wand) {
        Item wi = wand.getItem();
        if (wi instanceof ItemWandCasting) {
            ItemStack stack = ((ItemWandCasting)wi).getFocusItem(wand);
            if (stack != null) {
                Item focus = stack.getItem();
                if (focus instanceof ItemFocusBasic) {
                    return (ItemFocusBasic)focus;
                }
            }
        }

        return null;
    }

    public static ItemStack getWandFocusStack(ItemStack wand) {
        Item wi = wand.getItem();
        return wi instanceof ItemWandCasting ? ((ItemWandCasting)wi).getFocusItem(wand) : null;
    }

    public static ItemStack applyWandFocus(ItemStack wandStack, ItemStack focusStack, boolean allowOverwrite) {
        if (!(wandStack.getItem() instanceof ItemWandCasting)) {
            return null;
        } else {
            ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
            ItemStack prevFocus = wand.getFocusItem(wandStack);
            if (allowOverwrite || prevFocus == null) {
                wand.setFocus(wandStack, focusStack);
            }

            return prevFocus;
        }
    }

    public static boolean playerIsWearingBauble(EntityPlayer player, HashableItemWithoutSize item) {
        IInventory baubles = BaublesApi.getBaubles(player);
        if (baubles != null) {
            int size = baubles.getSizeInventory();

            for (int i = 0; i < size; i++) {
                ItemStack stack = baubles.getStackInSlot(i);
                if (stack != null && item.isSameItem(stack)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean areAspectListsIdentical(AspectList a, AspectList b) {
        if (a == null) {
            return b == null;
        } else if (b == null) {
            return false;
        } else if (a.size() != b.size()) {
            return false;
        } else {
            for (Aspect aspect : a.getAspects()) {
                if (a.getAmount(aspect) != b.getAmount(aspect)) {
                    return false;
                }
            }

            return true;
        }
    }

    public static ThaumcraftExtension.AspectWithAmount drawEssentia(
        IBlockAccess blockaccess, int x, int y, int z, ForgeDirection orientation, AspectList extractAspectAmounts, int suction
    ) {
        TileEntity te = ThaumcraftApiHelper.getConnectableTile(blockaccess, x, y, z, orientation);
        if (te != null) {
            IEssentiaTransport ic = (IEssentiaTransport)te;
            if (!ic.canOutputTo(orientation.getOpposite())) {
                return null;
            }

            for (Aspect aspect : extractAspectAmounts.aspects.keySet()) {
                if (ic.getSuctionType(orientation.getOpposite()) == aspect
                    && ic.getSuctionAmount(orientation.getOpposite()) < suction
                    && suction >= ic.getMinimumSuction()) {
                    int takeMax = extractAspectAmounts.getAmount(aspect);
                    int took = ic.takeEssentia(aspect, takeMax, orientation.getOpposite());
                    if (took > 0) {
                        return new ThaumcraftExtension.AspectWithAmount(aspect, took);
                    }
                }
            }
        }

        return null;
    }

    public static ItemStack insertStackIntoInventory(ItemStack stack, IInventory inv, int side, boolean doit) {
        if (placeItemStackIntoInventory == null) {
            try {
                Class clazz = ReflectionHelper.getClass(
                    ThaumcraftExtension.class.getClassLoader(), "thaumcraft.common.lib.utils.InventoryUtils", "thaumcraft.common.lib.InventoryHelper"
                );
                placeItemStackIntoInventory = clazz.getMethod("placeItemStackIntoInventory", ItemStack.class, IInventory.class, int.class, boolean.class);
            } catch (Exception var7) {
                FMLLog.warning("[Automagy] Error: Could not find inventory utility method.");
                return stack;
            }
        }

        try {
            ItemStack result = (ItemStack)placeItemStackIntoInventory.invoke(null, stack, inv, side, doit);
            return result;
        } catch (Exception var6) {
            FMLLog.warning("[Automagy] Error: Could not invoke inventory utility method.");
            return stack;
        }
    }

    public static boolean isBiomeEerie(World world, int x, int z) {
        return isBiomeEerie(world.getBiomeGenForCoords(x, z));
    }

    public static boolean isBiomeEerie(BiomeGenBase bg) {
        return bg.biomeID == ThaumcraftWorldGenerator.biomeEerie.biomeID;
    }

    public static void setBiomeTo(World world, int x, int z, BiomeGenBase biome) {
        if (setBiomeAt == null) {
            try {
                Class clazz = ReflectionHelper.getClass(
                    ThaumcraftExtension.class.getClassLoader(), "thaumcraft.common.lib.utils.Utils", "thaumcraft.common.lib.Utils"
                );
                setBiomeAt = clazz.getMethod("setBiomeAt", World.class, int.class, int.class, BiomeGenBase.class);
            } catch (Exception var6) {
                FMLLog.warning("[Automagy] Error: Could not invoke set biome utility method.");
                return;
            }
        }

        try {
            setBiomeAt.invoke(null, world, x, z, biome);
        } catch (Exception var5) {
            FMLLog.warning("[Automagy] Error: Could not invoke set biome utility method.");
        }
    }

    public static void setBiomeToEerie(World world, int x, int z) {
        setBiomeTo(world, x, z, ThaumcraftWorldGenerator.biomeEerie);
    }

    public static boolean canGolemTake(EntityGolemBase golem, ItemStack stack, boolean checkOreDict, boolean ignoreDamage, boolean ignoreNBT) {
        if (golem != null && stack != null) {
            IInventory inv = golem.inventory;
            int size = golem.inventory.slotCount;

            for (int slot = 0; slot < size; slot++) {
                ItemStack stack2 = inv.getStackInSlot(slot);
                if (stack2 != null && InventoryUtils.areItemStacksEqual(stack2, stack, checkOreDict, ignoreDamage, ignoreNBT)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean canGolemTake(EntityGolemBase golem, ItemStack stack) {
        return golem == null ? null : canGolemTake(golem, stack, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT());
    }

    public static ItemStack giveItemToGolem(EntityGolemBase golem, ItemStack stack) {
        ItemStack carried = golem.getCarried();
        int max = 0;
        if (carried == null) {
            max = golem.getCarryLimit();
            carried = stack.copy();
            carried.stackSize = 0;
        } else if (TjUtil.canItemsStack(stack, carried)) {
            max = golem.getCarrySpace();
        }

        ItemStack leftover = stack.copy();
        if (max < 1) {
            return leftover;
        } else {
            int change = Math.min(max, stack.stackSize);
            carried.stackSize += change;
            golem.setCarried(carried);
            leftover.stackSize -= change;
            return leftover.stackSize > 0 ? leftover : null;
        }
    }

    public static void makeGolemDrop(EntityGolemBase golem, int amt) {
        ItemStack carried = golem.getCarried();
        if (carried != null) {
            amt = Math.min(amt, carried.stackSize);
            ItemStack dropStack = carried.copy();
            dropStack.stackSize = amt;
            TjUtil.dropItemIntoWorldSimple(dropStack, golem.worldObj, golem.posX, golem.posY + 0.1, golem.posZ);
            int leftover = carried.stackSize - amt;
            if (leftover > 0) {
                carried.stackSize = leftover;
                golem.setCarried(carried);
            } else {
                golem.setCarried(null);
            }
        }
    }

    public static void limitGolemCarrying(EntityGolemBase golem, int max) {
        ItemStack carried = golem.getCarried();
        if (carried != null && carried.stackSize > max) {
            makeGolemDrop(golem, carried.stackSize - max);
        }
    }

    public static ItemStack getGolemCarriedItem(EntityGolemBase golem) {
        return golem.getCarried();
    }

    public static boolean golemHasOrderUpgrade(EntityGolemBase golem) {
        return golem.getUpgradeAmount(4) > 0;
    }

    public static int getGolemAirUpgrades(EntityGolemBase golem) {
        return golem.getUpgradeAmount(0);
    }

    public static int getMaxGolemCanCarry(EntityGolemBase golem, ItemStack stack) {
        return Math.min(Math.min(stack.getMaxStackSize(), golem.getCarryLimit()), 127);
    }

    public static boolean setGolemItemFilter(EntityGolemBase golem, ItemStack... stacks) {
        if (golem == null) {
            return false;
        } else {
            boolean changed = false;
            IInventory inv = golem.inventory;
            int size = golem.inventory.slotCount;
            int listSize = stacks == null ? 0 : stacks.length;

            for (int slot = 0; slot < size; slot++) {
                ItemStack newStack = slot < listSize ? stacks[slot] : null;
                if (newStack != null && newStack.stackSize > 256) {
                    newStack = newStack.copy();
                    newStack.stackSize = 256;
                }

                ItemStack oldStack = inv.getStackInSlot(slot);
                if (!ItemStack.areItemStacksEqual(newStack, oldStack)) {
                    inv.setInventorySlotContents(slot, newStack);
                    changed = true;
                }
            }

            if (changed) {
                inv.markDirty();
            }

            return changed;
        }
    }

    public static boolean clearGolemItemFilter(EntityGolemBase golem) {
        return setGolemItemFilter(golem, (ItemStack)null);
    }

    public static ArrayList<ItemStack> getGolemItemFilter(EntityGolemBase golem) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        IInventory inv = golem.inventory;
        int size = golem.inventory.slotCount;

        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = inv.getStackInSlot(slot);
            if (stack != null) {
                stacks.add(stack);
            }
        }

        return stacks;
    }

    public static FocusUpgradeType createFocusUpgrade(int id, String name, AspectList cost) {
        return new FocusUpgradeType(
            id,
            new ResourceLocation("Automagy".toLowerCase(), "textures/foci/" + name + ".png"),
            "Automagy.focus.upgrade." + name + ".name",
            "Automagy.focus.upgrade." + name + ".text",
            cost
        );
    }

    public static AspectList getObjectTotalAspects(ItemStack stack) {
        AspectList tags = ThaumcraftApiHelper.getObjectAspects(stack);
        return ThaumcraftApiHelper.getBonusObjectTags(stack, tags);
    }

    public static boolean hasPlayerScanned(EntityPlayer player, ItemStack stack) {
        int h = ScanManager.generateItemHash(stack.getItem(), stack.getMetadata());
        List<String> list = Thaumcraft.proxy.getScannedObjects().get(player.getCommandSenderName());
        return list != null && (list.contains("@" + h) || list.contains("#" + h));
    }

    public static void zapLocation(World world, float x, float y, float z, float targetX, float targetY, float targetZ) {
        PacketHandler.INSTANCE
            .sendToAllAround(new PacketFXBlockZap(x, y, z, targetX, targetY, targetZ), new TargetPoint(world.provider.dimensionId, x, y, z, 32.0));
    }

    public static void zapEntity(Entity target, int damage, World world, float x, float y, float z) {
        PacketHandler.INSTANCE
            .sendToAllAround(
                new PacketFXBlockZap(x, y, z, (float)target.posX, (float)target.posY + target.height / 2.0F, (float)target.posZ),
                new TargetPoint(world.provider.dimensionId, x, y, z, 32.0)
            );
        target.attackEntityFrom(DamageSource.magic, damage);
    }

    public static void zapEntity(Entity target, World world, int x, int y, int z) {
        zapEntity(target, 4 + world.rand.nextInt(4), world, x + 0.5F, y + 0.5F, z + 0.5F);
    }

    @SideOnly(Side.CLIENT)
    public static void renderAspectsForPlayer(GuiContainer gui, EntityPlayer player, AspectList tags, int var16, int var17) {
        int shiftx = -8;
        int shifty = -8;
        if (Thaumcraft.instance.aspectShift) {
            shiftx -= 8;
            shifty -= 8;
        }

        int x = var16 + 17;
        int y = var17 + 7 - 33;
        GL11.glDisable(2929);
        int index = 0;
        if (tags.size() > 0) {
            for (Aspect tag : tags.getAspectsSortedAmount()) {
                if (tag != null) {
                    x = var16 + 17 + index * 18;
                    y = var17 + 7 - 33;
                    UtilsFX.bindTexture("textures/aspects/_back.png");
                    GL11.glPushMatrix();
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    GL11.glTranslated(x + shiftx - 2, y + shifty - 2, 0.0);
                    GL11.glScaled(1.25, 1.25, 0.0);
                    UtilsFX.drawTexturedQuadFull(0, 0, UtilsFX.getGuiZLevel(gui));
                    GL11.glDisable(3042);
                    GL11.glPopMatrix();
                    if (Thaumcraft.proxy.playerKnowledge.hasDiscoveredAspect(player.getCommandSenderName(), tag)) {
                        UtilsFX.drawTag(x + shiftx, y + shifty, tag, tags.getAmount(tag), 0, UtilsFX.getGuiZLevel(gui));
                    } else {
                        UtilsFX.bindTexture("textures/aspects/_unknown.png");
                        GL11.glPushMatrix();
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 771);
                        GL11.glTranslated(x + shiftx, y + shifty, 0.0);
                        UtilsFX.drawTexturedQuadFull(0, 0, UtilsFX.getGuiZLevel(gui));
                        GL11.glDisable(3042);
                        GL11.glPopMatrix();
                    }

                    index++;
                }
            }
        }

        GL11.glEnable(2929);
    }

    public static class AspectWithAmount {
        public Aspect aspect;
        public int amount;

        public AspectWithAmount(Aspect aspect, int amount) {
            this.aspect = aspect;
            this.amount = amount;
        }
    }

    public static class EssentiaJarItemRef {
        public ItemStack stack;
        public ItemJarFilled item;

        public EssentiaJarItemRef(ItemStack stack, ItemJarFilled item) {
            this.stack = stack;
            this.item = item;
        }

        public AspectList getAspects() {
            return this.item.getAspects(this.stack);
        }

        public void setAspects(AspectList aspects) {
            this.item.setAspects(this.stack, aspects);
        }
    }
}
