package tuhljin.automagy.tiles;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.logging.log4j.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectSourceHelper;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.container.InventoryFake;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.EnumGolemType;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.FilteringItemList;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.lib.inventory.InventoryCraftingCachedResult;
import tuhljin.automagy.lib.inventory.InventoryCraftingWithPlaceholder;
import tuhljin.automagy.network.MessageGUIGolemWorkbenchRecipe;

public class TileEntityGolemWorkbench extends ModTileEntityWithInventory implements IWandable {
    public static final int EMPTY_CORE = 1;
    public static final int MAX_RECIPES = 3;
    public static int CRAFTING_TIME = 38;
    public static int NUM_COMPONENT_SLOTS = 9;
    public static int CRAFTINGHEAT_MAX = 12000;
    public static int CRAFTINGHEAT_SLOWER = 1200;
    public static int CRAFTINGHEAT_ON_COMPLETION = 10;
    public EntityGolemBase currentGolem = null;
    public ItemStack currentRecipeOutput = null;
    public int progress = 0;
    public boolean recipeIsAdvanced = false;
    public int craftingHeat = 0;
    public InventoryCraftingWithPlaceholder[] invCrafting = new InventoryCraftingWithPlaceholder[3];
    protected int recipeIndex = 0;
    protected int[] replacementIndex = new int[]{1, 1, 1, 1};
    protected int[] upgrades = new int[]{-1, -1, -1, -1};
    private int ticks = 0;
    protected int recipes = 1;
    protected boolean fuzzy = false;
    private int craftTimeSpeedUpgrades = 0;
    private int craftTimeStrengthUpgrades = 0;
    private int heatImpactsAt = CRAFTINGHEAT_SLOWER;
    private FakePlayer fakePlayer = null;
    public static Map<EnumGolemType, Integer> baseGolemSpeeds = new HashMap<>();
    TileEntityGolemWorkbench.EntitySelectorCraftingGolem entitySelectorCraftingGolem = new TileEntityGolemWorkbench.EntitySelectorCraftingGolem();

    public TileEntityGolemWorkbench() {
        super("blockGolemWorkbench", NUM_COMPONENT_SLOTS);
        this.invCrafting[0] = new InventoryCraftingWithPlaceholder("Golem Crafting Inventory");
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        if (this.fuzzy && slot == 0) {
            return false;
        } else {
            if (stack != null) {
                for (int i = 0; i < this.invCrafting.length; i++) {
                    if (this.invCrafting[i] != null && this.invCrafting[i].containsItem(stack)) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote) {
            this.ticks++;
            if (this.craftingHeat > 0) {
                this.craftingHeat--;
            } else if (this.craftingHeat < 0 && this.currentGolem == null && this.ticks % 5 == 0) {
                this.craftingHeat++;
            }

            if (ThaumcraftExtension.blockIsFetter(this.worldObj, this.xCoord, this.yCoord - 1, this.zCoord, true)) {
                return;
            }

            double grabRange = 3.0;
            List<EntityGolemBase> entities = null;
            if (this.progress == 0) {
                AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                    this.xCoord - grabRange,
                    this.yCoord - grabRange,
                    this.zCoord - grabRange,
                    this.xCoord + grabRange + 1.0,
                    this.yCoord + grabRange,
                    this.zCoord + grabRange + 1.0
                );
                entities = this.worldObj.selectEntitiesWithinAABB(EntityGolemBase.class, box, this.entitySelectorCraftingGolem);
                if (entities.size() == 0 || !this.anyGolemHandsFree(entities)) {
                    return;
                }
            }

            if (this.currentRecipeOutput != null || this.ticks % 20 == 0) {
                ItemStack result = this.getCraftableResults();
                if (!ItemStack.areItemStacksEqual(this.currentRecipeOutput, result)) {
                    this.currentRecipeOutput = result;
                    this.currentGolem = null;
                    this.progress = 0;
                    this.ticks = 4;
                    this.recipeIsAdvanced = false;
                    if (this.currentRecipeOutput != null) {
                        HashMap<HashableItemWithoutSize, Integer> map = this.invCrafting[this.recipeIndex].getItemMap();
                        if (map.size() > 1) {
                            this.recipeIsAdvanced = !this.invCrafting[this.recipeIndex].recipeFitsSmallGrid();
                        }
                    }
                }
            }

            if (this.currentRecipeOutput == null) {
                return;
            }

            if (this.currentGolem == null) {
                if (entities == null) {
                    AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                        this.xCoord - grabRange,
                        this.yCoord - grabRange,
                        this.zCoord - grabRange,
                        this.xCoord + grabRange + 1.0,
                        this.yCoord + grabRange,
                        this.zCoord + grabRange + 1.0
                    );
                    entities = this.worldObj.selectEntitiesWithinAABB(EntityGolemBase.class, box, this.entitySelectorCraftingGolem);
                }

                for (EntityGolemBase golem : entities) {
                    if ((!this.recipeIsAdvanced || golem.advanced) && ThaumcraftExtension.canGolemTake(golem, this.currentRecipeOutput)) {
                        this.currentGolem = golem;
                        this.progress = 0;
                        break;
                    }
                }

                if (this.currentGolem == null) {
                    return;
                }
            } else if (!this.entitySelectorCraftingGolem.isEntityApplicable(this.currentGolem)
                || !ThaumcraftExtension.canGolemTake(this.currentGolem, this.currentRecipeOutput)
                || TjUtil.getDistanceBetweenPoints_double(
                        this.xCoord, this.yCoord, this.zCoord, this.currentGolem.posX, this.currentGolem.posY, this.currentGolem.posZ
                    )
                    > grabRange) {
                this.currentGolem = null;
                this.progress = 0;
                return;
            }

            if (this.craftingHeat > this.heatImpactsAt && AspectSourceHelper.drainEssentia(this, Aspect.CRAFT, ForgeDirection.UNKNOWN, 8)) {
                this.craftingHeat = -700;
            }

            float goal = this.getCraftingTime(this.currentGolem);
            if (this.craftingHeat > this.heatImpactsAt) {
                goal *= 1.5F * ((float)this.craftingHeat / this.heatImpactsAt);
                if (goal > 200.0F) {
                    goal = 200.0F;
                }
            }

            if (this.progress < goal) {
                if (this.currentGolem.motionX == 0.0 && Math.abs(this.currentGolem.motionY) < 0.1 && this.currentGolem.motionZ == 0.0) {
                    this.progress++;
                    this.craftingHeat += 5;
                    if (this.ticks % (this.craftingHeat < this.heatImpactsAt ? 6 : 20) == 0) {
                        if (this.worldObj.rand.nextBoolean()) {
                            this.currentGolem.startRightArmTimer();
                        } else {
                            this.currentGolem.startLeftArmTimer();
                        }

                        this.ticks = this.worldObj.rand.nextInt(3) + 2;
                    }

                    if (this.craftingHeat > this.heatImpactsAt + 5) {
                        this.currentGolem.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 200, 2));
                    }
                }
            } else {
                ItemStack leftover = ThaumcraftExtension.giveItemToGolem(this.currentGolem, this.currentRecipeOutput);
                if (leftover != null) {
                    TjUtil.dropItemIntoWorld(leftover, this.worldObj, this.xCoord, this.yCoord + 0.6, this.zCoord);
                }

                this.consumeCraftingIngredients();
                ItemStack[] recipe = this.fireCraftingEvent(this.invCrafting[this.recipeIndex], this.currentRecipeOutput);
                if (recipe != null) {
                    for (ItemStack stack : recipe) {
                        if (stack != null && stack.stackSize > 1) {
                            stack.stackSize--;
                            ItemStack leftoverx = this.insertItemIntoLastAvailableSlot(stack);
                            if (leftoverx != null) {
                                TjUtil.dropItemIntoWorld(stack, this.worldObj, this.xCoord, this.yCoord + 0.6, this.zCoord);
                            }
                        }
                    }
                }

                this.progress = 0;
                this.ticks = 0;
                this.craftingHeat = this.craftingHeat + CRAFTINGHEAT_ON_COMPLETION;
                this.worldObj
                    .playSoundAtEntity(
                        this.currentGolem, "random.pop", 0.1F, ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F
                    );
            }

            if (this.craftingHeat > CRAFTINGHEAT_MAX) {
                this.craftingHeat = CRAFTINGHEAT_MAX;
            }
        }
    }

    private boolean anyGolemHandsFree(List<EntityGolemBase> entities) {
        for (EntityGolemBase golem : entities) {
            if (ThaumcraftExtension.getGolemCarriedItem(golem) == null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void setWorldObj(World w) {
        super.setWorldObj(w);

        for (int i = 0; i < this.recipes; i++) {
            if (this.invCrafting[i] != null) {
                this.invCrafting[i].setWorld(w);
            }
        }
    }

    public ItemStack getPlaceholderItem() {
        return this.fuzzy ? this.getStackInSlot(0) : null;
    }

    public float getCraftingTime(EntityGolemBase golem) {
        int goal = CRAFTING_TIME;
        if (this.craftTimeStrengthUpgrades > 0) {
            int strength = golem.getGolemStrength();
            goal -= strength * this.craftTimeStrengthUpgrades;
        }

        if (this.craftTimeSpeedUpgrades > 0) {
            int speed = baseGolemSpeeds.get(golem.getGolemType());
            if (golem.decoration.contains("B")) {
                speed++;
            }

            if (golem.decoration.contains("P")) {
                speed--;
            }

            speed += ThaumcraftExtension.getGolemAirUpgrades(golem);
            if (golem.advanced) {
                speed++;
            }

            goal -= speed * this.craftTimeSpeedUpgrades;
        }

        return Math.max((float)goal, 4.1F);
    }

    protected ItemStack getCraftableResults() {
        if (this.recipeIndex > this.recipes - 1) {
            this.recipeIndex = 0;
        }

        ItemStack result = null;
        boolean checkedPrevIndex = false;
        if (this.progress > 0) {
            result = this.getCraftingResult(this.recipeIndex);
            if (result != null && this.containsAllIngredients(this.recipeIndex)) {
                return result;
            }

            checkedPrevIndex = true;
        }

        int nextIndex = this.recipeIndex;
        boolean hasIngredients = false;

        do {
            if (++nextIndex >= this.recipes) {
                nextIndex = 0;
            }

            if (nextIndex == this.recipeIndex) {
                if (!checkedPrevIndex) {
                    result = this.getCraftingResult(nextIndex);
                    if (result != null) {
                        hasIngredients = this.containsAllIngredients(nextIndex);
                    }
                }
                break;
            }

            result = this.getCraftingResult(nextIndex);
            if (result != null) {
                hasIngredients = this.containsAllIngredients(nextIndex);
            }
        } while (result == null || !hasIngredients);

        if (this.recipeIndex != nextIndex) {
            this.recipeIndex = nextIndex;
            this.markDirty(false);
        }

        return hasIngredients ? result : null;
    }

    public ItemStack getCraftingResult(int index) {
        if (this.fuzzy) {
            ItemStack placeholder = this.getStackInSlot(0);
            if (placeholder != null) {
                this.invCrafting[index].setPlaceholder(placeholder, this.getFuzzyReplacement(index));
                ItemStack result = this.invCrafting[index].getCraftingResult();
                if (result == null && this.progress == 0) {
                    int prevIndex = this.replacementIndex[index];

                    do {
                        this.invCrafting[index].setPlaceholder(placeholder, this.getFuzzyReplacement(index));
                        result = this.invCrafting[index].getCraftingResult();
                    } while (result == null && prevIndex != this.replacementIndex[index]);
                }

                return result;
            }

            this.invCrafting[index].setPlaceholder(null, null);
        }

        return this.invCrafting[index].getCraftingResult();
    }

    protected ItemStack getFuzzyReplacement(int recIndex) {
        if (this.progress != 0) {
            return this.getStackInSlot(this.replacementIndex[recIndex]);
        } else {
            int last = this.replacementIndex[recIndex];

            ItemStack result;
            do {
                this.replacementIndex[recIndex]++;
                if (this.replacementIndex[recIndex] >= NUM_COMPONENT_SLOTS) {
                    this.replacementIndex[recIndex] = 1;
                }

                result = this.getStackInSlot(this.replacementIndex[recIndex]);
            } while (result == null && this.replacementIndex[recIndex] != last);

            if (last != this.replacementIndex[recIndex]) {
                this.markDirty(false);
            }

            return result;
        }
    }

    public boolean containsAllIngredients(int index) {
        HashMap<HashableItemWithoutSize, Integer> map = this.invCrafting[index].getItemMap(this.fuzzy);
        FilteringItemList list = new FilteringItemList();

        for (int i = this.fuzzy ? 1 : 0; i < NUM_COMPONENT_SLOTS; i++) {
            ItemStack stack = this.getStackInSlot(i);
            if (stack != null) {
                list.add(stack);
            }
        }

        for (Entry<HashableItemWithoutSize, Integer> entry : map.entrySet()) {
            HashableItemWithoutSize item = entry.getKey();
            int num = entry.getValue();
            if (list.get(item) < num) {
                return false;
            }
        }

        return true;
    }

    protected void consumeCraftingIngredients() {
        for (int craftSlot = 0; craftSlot < 9; craftSlot++) {
            ItemStack ingredient = this.invCrafting[this.recipeIndex].getStackInSlot(craftSlot, this.fuzzy);
            if (ingredient != null) {
                for (int slot = this.fuzzy ? 1 : 0; slot < NUM_COMPONENT_SLOTS; slot++) {
                    ItemStack stack = this.getStackInSlot(slot);
                    if (TjUtil.areItemsEqualIgnoringSize(ingredient, stack)) {
                        this.decrStackSize(slot, 1);
                        ItemStack container = stack.getItem().getContainerItem(stack);
                        if (container != null && container.isItemStackDamageable() && container.getMetadata() > container.getMaxDurability()) {
                            break;
                        }

                        if (!stack.getItem().doesContainerItemLeaveCraftingGrid(stack) && this.getStackInSlot(slot) == null) {
                            this.setInventorySlotContents(slot, container);
                            break;
                        }

                        if (container != null) {
                            ItemStack leftover = container;
                            if (this.currentGolem != null && ThaumcraftExtension.golemHasOrderUpgrade(this.currentGolem)) {
                                leftover = this.insertItemIntoLastAvailableSlot(container);
                            }

                            if (leftover != null) {
                                TjUtil.dropItemIntoWorld(leftover.copy(), this.worldObj, this.xCoord, this.yCoord + 0.6, this.zCoord);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public ItemStack insertItemIntoLastAvailableSlot(ItemStack stack) {
        return TjUtil.addToInventory(stack, this, this.getSizeInventory() - 1, this.fuzzy ? 1 : 0);
    }

    public void clientSendRecipeToServer(ItemStack[] stacks, int index) {
        MessageGUIGolemWorkbenchRecipe.sendToServer(this, stacks, index);
    }

    public void receiveRecipeFromClient(int index, ItemStack[] stacks) {
        if (stacks == null || stacks.length != this.inventorySlots.length || index < 0 || index >= this.invCrafting.length) {
            FMLLog.warning("[Automagy] TileEntityGolemWorkbench received invalid packet data. Ignoring.");
        } else if (this.invCrafting[index] != null) {
            for (int i = 0; i < stacks.length; i++) {
                this.invCrafting[index].setInventorySlotContents(i, stacks[i]);
            }

            this.markDirty();
        }
    }

    public ItemStack[] getUpgradeStacks() {
        ItemStack[] stacks = null;

        for (int i = 0; i < this.upgrades.length; i++) {
            if (this.upgrades[i] != -1) {
                if (stacks == null) {
                    stacks = new ItemStack[this.upgrades.length];
                }

                stacks[i] = new ItemStack(ModItems.golemWorkbenchUpgrade, 1, this.upgrades[i]);
            }
        }

        return stacks;
    }

    public boolean installUpgrade(int upgrade, int side) {
        side = TjUtil.getSWNESide(TjUtil.getOppositeSide(side));
        if (side < 0 || side >= this.upgrades.length) {
            throw new IllegalArgumentException("Invalid side given as argument.");
        } else if (upgrade < 0 || this.upgrades[side] >= 0) {
            return false;
        } else if (upgrade == 1 && this.recipes >= 3) {
            return false;
        } else {
            this.upgrades[side] = upgrade;
            this.updateUpgradeEffects();
            this.markDirty();
            return true;
        }
    }

    public int removeUpgrade(int side) {
        side = TjUtil.getSWNESide(TjUtil.getOppositeSide(side));
        if (side >= 0 && side < this.upgrades.length) {
            int drop = this.upgrades[side];
            if (drop >= 0) {
                this.upgrades[side] = -1;
                this.updateUpgradeEffects();
                this.markDirty();
            }

            return drop;
        } else {
            throw new IllegalArgumentException("Invalid side given as argument.");
        }
    }

    public void updateUpgradeEffects() {
        this.craftTimeSpeedUpgrades = 0;
        this.craftTimeStrengthUpgrades = 0;
        int prevRecipes = this.recipes;
        this.recipes = 1;
        int lethargyAdjustment = 0;
        boolean prevFuzzy = this.fuzzy;
        this.fuzzy = false;

        for (int i = 0; i < this.upgrades.length; i++) {
            switch (this.upgrades[i]) {
                case 0:
                    this.craftTimeSpeedUpgrades++;
                    lethargyAdjustment++;
                    break;
                case 1:
                    if (this.recipes < 3) {
                        this.recipes++;
                        if (this.invCrafting[this.recipes - 1] == null) {
                            this.invCrafting[this.recipes - 1] = new InventoryCraftingWithPlaceholder("Golem Crafting Inventory " + this.recipes);
                            this.invCrafting[this.recipes - 1].setWorld(this.worldObj);
                        }
                    }
                case 2:
                case 4:
                default:
                    break;
                case 3:
                    this.craftTimeStrengthUpgrades++;
                    lethargyAdjustment -= 2;
                    break;
                case 5:
                    if (!this.fuzzy) {
                        this.fuzzy = true;
                        lethargyAdjustment += 3;
                    }
            }
        }

        this.heatImpactsAt = CRAFTINGHEAT_SLOWER - (int)(lethargyAdjustment * (CRAFTINGHEAT_SLOWER * 0.1F));
        if (prevFuzzy != this.fuzzy) {
            if (this.fuzzy) {
                this.accessibleSlots = new int[NUM_COMPONENT_SLOTS - 1];

                for (int i = 0; i < NUM_COMPONENT_SLOTS - 1; i++) {
                    this.accessibleSlots[i] = i + 1;
                }
            } else {
                this.accessibleSlots = new int[NUM_COMPONENT_SLOTS];
                int i = 0;

                while (i < NUM_COMPONENT_SLOTS) {
                    this.accessibleSlots[i] = i++;
                }
            }
        }

        if (prevRecipes > this.recipes) {
            for (int i = this.recipes; i < 3; i++) {
                if (this.invCrafting[i] != null) {
                    this.invCrafting[i].clearSlots();
                }
            }
        }
    }

    @Override
    public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
        if (!world.isRemote && side > 1 && side < 6 && player.isSneaking()) {
            int upgrade = this.removeUpgrade(side);
            if (upgrade >= 0) {
                ForgeDirection dir = ForgeDirection.getOrientation(side);
                TjUtil.dropItemIntoWorldSimple(
                    new ItemStack(ModItems.golemWorkbenchUpgrade, 1, upgrade),
                    world,
                    x + 0.5 + dir.offsetX * 0.6,
                    y + 0.5 + dir.offsetY * 0.6,
                    z + 0.5 + dir.offsetZ * 0.6
                );
                world.playSoundAtEntity(player, "thaumcraft:zap", 0.5F, 1.0F);
            }
        }

        return 0;
    }

    @Override
    public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
        return null;
    }

    @Override
    public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {
    }

    @Override
    public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {
    }

    public int getNumRecipes() {
        return this.recipes;
    }

    public boolean usingPlaceholders() {
        return this.fuzzy;
    }

    public ItemStack[] fireCraftingEvent(InventoryCraftingCachedResult craftMatrix, ItemStack output) {
        if (this.fakePlayer == null) {
            this.fakePlayer = FakePlayerFactory.get((WorldServer)this.worldObj, new GameProfile((UUID)null, "FakeCraftingGolem"));
        }

        ItemStack[] recipe = craftMatrix.getCopyOfStacks();

        try {
            FMLCommonHandler.instance().firePlayerCraftingEvent(this.fakePlayer, output, new InventoryFake(recipe));
            return recipe;
        } catch (Exception var5) {
            FMLLog.log(Level.ERROR, "Exception upon firing crafting event.");
            var5.printStackTrace();
            return null;
        }
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.craftingHeat = nbttagcompound.getInteger("craftingHeat");
        this.upgrades = getIntArrayFromNbtOrDefault(nbttagcompound, "upgrades", -1, 4);
        this.updateUpgradeEffects();
        this.invCrafting[0].readCustomNBT(nbttagcompound);

        for (int i = 0; i < this.recipes; i++) {
            this.invCrafting[i].readCustomNBT(nbttagcompound);
        }

        this.recipeIndex = nbttagcompound.getInteger("recipeIndex");
        if (this.recipeIndex > this.recipes - 1) {
            this.recipeIndex = 0;
        }

        this.replacementIndex = getIntArrayFromNbtOrDefault(nbttagcompound, "phReplaceIndex", 1, 3);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setInteger("craftingHeat", this.craftingHeat);
        nbttagcompound.setIntArray("upgrades", this.upgrades);

        for (int i = 0; i < this.recipes; i++) {
            this.invCrafting[i].writeCustomNBT(nbttagcompound);
        }

        nbttagcompound.setInteger("recipeIndex", this.recipeIndex);
        nbttagcompound.setIntArray("phReplaceIndex", this.replacementIndex);
    }

    static {
        baseGolemSpeeds.put(EnumGolemType.STRAW, 5);
        baseGolemSpeeds.put(EnumGolemType.WOOD, 4);
        baseGolemSpeeds.put(EnumGolemType.TALLOW, 3);
        baseGolemSpeeds.put(EnumGolemType.CLAY, 3);
        baseGolemSpeeds.put(EnumGolemType.FLESH, 4);
        baseGolemSpeeds.put(EnumGolemType.STONE, 2);
        baseGolemSpeeds.put(EnumGolemType.IRON, 1);
        baseGolemSpeeds.put(EnumGolemType.THAUMIUM, 2);
    }

    public class EntitySelectorCraftingGolem implements IEntitySelector {
        @Override
        public boolean isEntityApplicable(Entity entity) {
            EntityGolemBase golem = (EntityGolemBase)entity;
            if (!golem.inactive && !golem.paused && golem.getCore() == 1 && golem.getCarried() == null) {
                ChunkCoordinates coord = golem.getHomePosition();
                ForgeDirection dir = ForgeDirection.getOrientation(golem.homeFacing);
                if (dir != ForgeDirection.UNKNOWN
                    && coord.posX == TileEntityGolemWorkbench.this.xCoord + dir.offsetX
                    && coord.posY == TileEntityGolemWorkbench.this.yCoord + dir.offsetY
                    && coord.posZ == TileEntityGolemWorkbench.this.zCoord + dir.offsetZ) {
                    return true;
                }
            }

            return false;
        }
    }
}
