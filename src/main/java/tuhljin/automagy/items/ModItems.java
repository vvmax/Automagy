package tuhljin.automagy.items;

import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import tuhljin.automagy.Automagy;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.entities.EntityAvaricePearl;
import tuhljin.automagy.lib.AutomagyConfig;

public class ModItems {
    public static CreativeTabs tab;
    public static ModSubtypedItem shardSliver;
    public static ModItem avaricePearl;
    public static ModItem enchantedPaper;
    public static ModItem crystalBrain;
    public static ModItem golemLinker;
    public static ModSubtypedItem ingredient;
    public static ModItem ingredientHorizontal;
    public static ModItem edible;
    public static ItemFoodstuff food;
    public static ModItem netherHeart;
    public static ModItem tallyLens;
    public static ItemGlyph tankGlyph;
    public static ModItem golemWorkbenchUpgrade;
    public static ModItem crystalEye;
    public static ItemEntityEye crystalEntityEye;
    public static ItemBaubleEnderDisjunction ringEnderDisjunction;
    public static ModItemFocus focusCrafting;
    public static ItemAutoHandMirror autoHandMirror;
    public static ModItem bucketMushroom;
    public static ModItem bucketVishroom;
    public static ItemPhialXP phialXP;
    public static ModItem creativeTool;

    public static void init() {
        tab = Automagy.creativeTab;
        shardSliver = new ItemShardSliver("shardSliver");
        enchantedPaper = new ItemEnchantedPaper("enchantedPaper");
        avaricePearl = new ItemAvaricePearl("avaricePearl");
        crystalEye = new ItemCrystalEye("crystalEye");
        crystalEntityEye = new ItemEntityEye("crystalEntityEye");
        crystalBrain = new ItemCrystalBrain("crystalBrain");
        golemLinker = new ItemGolemLinker("golemLinker");
        ingredient = new ItemIngredient("ingredient");
        ingredientHorizontal = new ItemHorizontal("horizontal");
        edible = new ItemEdible("edible");
        food = new ItemFoodstuff("foodstuff");
        netherHeart = new ItemNetherHeart("netherHeart");
        tallyLens = new ItemTallyLens("tallyLens");
        tankGlyph = new ItemGlyph("glyph");
        golemWorkbenchUpgrade = new ItemGolemWorkbenchUpgrade("golemWorkbenchUpgrade");
        ringEnderDisjunction = new ItemBaubleEnderDisjunction("baubleEnderDisjunction");
        autoHandMirror = new ItemAutoHandMirror("autoHandMirror");
        focusCrafting = new ItemFocusCrafting("focusCrafting");
        phialXP = new ItemPhialXP("phialExtra");
        if (!AutomagyConfig.listThinkingCap) {
            tab = null;
        }

        creativeTool = new ItemCreativeTool("creativeTool");
        BlockDispenser.dispenseBehaviorRegistry.putObject(avaricePearl, new BehaviorProjectileDispenseWithItemStack() {
            @Override
            protected IProjectile getProjectileEntity(World par1World, IPosition par2IPosition) {
                return new EntityAvaricePearl(par1World, par2IPosition.getX(), par2IPosition.getY(), par2IPosition.getZ(), this.theItemStack.getMetadata());
            }
        });
    }

    public static void initFluidContainers() {
        bucketMushroom = new ItemBucketCustom("bucketMushroom", ModBlocks.mushroomFluid);
        bucketVishroom = new ItemBucketCustom("bucketVishroom", ModBlocks.vishroomFluid);
        FluidContainerRegistry.registerFluidContainer(new FluidStack(ModBlocks.fluidMushroom, 1000), new ItemStack(bucketMushroom), new ItemStack(Items.bucket));
        FluidContainerRegistry.registerFluidContainer(new FluidStack(ModBlocks.fluidVishroom, 1000), new ItemStack(bucketVishroom), new ItemStack(Items.bucket));
    }
}
