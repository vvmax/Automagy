package tuhljin.automagy.lib.compat;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.relauncher.Side;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.Level;
import thaumcraft.api.aspects.Aspect;
import tuhljin.automagy.api.AutomagyAPI;
import tuhljin.automagy.api.example.betterstorage.ProviderForConnectable;
import tuhljin.automagy.api.example.betterstorage.ProviderForCrate;
import tuhljin.automagy.api.example.minefactoryreloaded.ProviderForDeepStorage;
import tuhljin.automagy.api.example.storagedrawers.ProviderForStorageDrawers;
import tuhljin.automagy.api.inventarium.DefaultInventariumContentsProvider;
import tuhljin.automagy.api.inventarium.SidedInventariumContentsProvider;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.config.ModRecipes;
import tuhljin.automagy.items.ModItems;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.lib.compat.nei.NEIProviders;

public class CompatibilityManager {
    public static boolean modLoadedNEI = false;
    public static boolean modLoadedBotania = false;
    public static BlockFluidBase milkBlock;
    public static BlockFluidBase mushroomSoupBlock;

    public static void init() {
        FMLInterModComms.sendMessage("Waila", "register", "tuhljin.automagy.lib.compat.waila.WailaProviders.wailaCallbackRegister");
    }

    public static void postInit() {
        AutomagyAPI.registerInventariumContentsProvider(new DefaultInventariumContentsProvider(), 500);
        AutomagyAPI.registerInventariumContentsProvider(new SidedInventariumContentsProvider(), 499);
        if (Loader.isModLoaded("betterstorage")) {
            AutomagyAPI.registerInventariumContentsProvider(new ProviderForCrate(), 100);
            AutomagyAPI.registerInventariumContentsProvider(new ProviderForConnectable(), 101);
            FMLLog.info("[Automagy] Integrated with BetterStorage.");
        }

        if (Loader.isModLoaded("StorageDrawers")) {
            FMLLog.info(
                "[Automagy] Attempting integration with Storage Drawers. IF THIS FAILS, be sure you have the latest version of Storage Drawers installed!"
            );
            AutomagyAPI.registerInventariumContentsProvider(new ProviderForStorageDrawers(), 120);
            FMLLog.info("[Automagy] Integrated with Storage Drawers.");
        }

        try {
            Class c = Class.forName("powercrystals.minefactoryreloaded.api.IDeepStorageUnit");
            AutomagyAPI.registerInventariumContentsProvider(new ProviderForDeepStorage(), 400);
            FMLLog.info("[Automagy] Integrated with MineFactoryReloaded's IDeepStorageUnit.");
        } catch (Exception var5) {
        }

        if (Loader.isModLoaded("Botania")) {
            modLoadedBotania = true;
            if (AutomagyConfig.botaniaLooniumBlacklistPhialXP) {
                if (BotaniaHandler.looniumBlacklist(ModItems.phialXP)) {
                    FMLLog.info("[Automagy] Placed XP phial on Botania's Loonium blacklist.");
                } else {
                    FMLLog.warning("[Automagy] Failed to place XP phial on Botania's Loonium blacklist.");
                }
            }
        }

        Method methodNEIHideItem = null;

        try {
            methodNEIHideItem = Class.forName("codechicken.nei.api.API").getMethod("hideItem", ItemStack.class);
        } catch (Exception var4) {
            FMLLog.info("[Automagy] No NEI integration because mod \"NotEnoughItems\" was not found.");
        }

        if (methodNEIHideItem != null) {
            boolean ok = true;

            try {
                methodNEIHideItem.invoke(null, new ItemStack(ModBlocks.torchInversion_off));
                methodNEIHideItem.invoke(null, new ItemStack(ModBlocks.specialProcessBlock));
                methodNEIHideItem.invoke(null, new ItemStack(ModBlocks.thaumostaticSpire));
                methodNEIHideItem.invoke(null, new ItemStack(ModBlocks.tallyBlockWorld));
                methodNEIHideItem.invoke(null, new ItemStack(ModBlocks.tallyBlockDrops));
                if (!AutomagyConfig.listCreativeJar) {
                    methodNEIHideItem.invoke(null, new ItemStack(ModBlocks.creativeJar, 1, 3));
                }

                if (!AutomagyConfig.listThinkingCap) {
                    methodNEIHideItem.invoke(null, new ItemStack(ModItems.creativeTool, 1, 0));
                }
            } catch (Exception var3) {
                FMLLog.info("[Automagy] Error while attempting NEI integration: Failed to hide items.");
                ok = false;
            }

            Side side = FMLCommonHandler.instance().getEffectiveSide();
            if (side == Side.CLIENT) {
                NEIProviders.register();
            }

            modLoadedNEI = true;
            if (ok) {
                FMLLog.info("[Automagy] NEI integration was successful.");
            }
        }
    }

    public static void onLoadComplete() {
        if (AutomagyConfig.useMFRMilk
            || AutomagyConfig.useMFRMushroomSoup
            || AutomagyConfig.overrideMFRMilkAspects
            || AutomagyConfig.overrideMFRMushroomSoupAspects) {
            boolean noErrors = true;
            Class<?> mfrThings = null;

            try {
                mfrThings = Class.forName("powercrystals.minefactoryreloaded.setup.MFRThings");
            } catch (Exception var11) {
                noErrors = false;
                FMLLog.info("[Automagy] No MFR integration because mod \"MineFactoryReloaded\" was not found.");
            }

            if (mfrThings != null) {
                if (AutomagyConfig.useMFRMilk || AutomagyConfig.overrideMFRMilkAspects) {
                    try {
                        Field f = mfrThings.getField("milkLiquid");
                        Object o = f.get(null);
                        if (o instanceof BlockFluidBase) {
                            if (AutomagyConfig.useMFRMilk) {
                                milkBlock = (BlockFluidBase)o;
                            }

                            if (AutomagyConfig.overrideMFRMilkAspects) {
                                ModRecipes.copyObjectAspects(ModBlocks.milk, (Block)o);
                            }
                        } else {
                            noErrors = false;
                            FMLLog.info("[Automagy] MineFactoryReloaded integration failure: milkLiquid was unexpected type.");
                        }
                    } catch (Exception var10) {
                        noErrors = false;
                        FMLLog.info("[Automagy] MineFactoryReloaded integration failure: Could not find milkLiquid.");
                        var10.printStackTrace();
                    }
                }

                if (AutomagyConfig.useMFRMushroomSoup || AutomagyConfig.overrideMFRMushroomSoupAspects) {
                    try {
                        Field f = mfrThings.getField("mushroomSoupLiquid");
                        Object o = f.get(null);
                        if (o instanceof BlockFluidBase) {
                            if (AutomagyConfig.useMFRMushroomSoup) {
                                mushroomSoupBlock = (BlockFluidBase)o;
                            }

                            if (AutomagyConfig.overrideMFRMushroomSoupAspects) {
                                ModRecipes.copyObjectAspects(ModBlocks.mushroomFluid, (Block)o);
                            }
                        } else {
                            noErrors = false;
                            FMLLog.info("[Automagy] MineFactoryReloaded integration failure: mushroomSoupLiquid was unexpected type.");
                        }
                    } catch (Exception var9) {
                        noErrors = false;
                        FMLLog.info("[Automagy] MineFactoryReloaded integration failure: Could not find mushroomSoupLiquid.");
                        var9.printStackTrace();
                    }
                }
            }

            if (noErrors) {
                FMLLog.info("[Automagy] Successfully integrated with MineFactoryReloaded fluids.");
            }
        }

        if (AutomagyConfig.useForestryMilk && milkBlock == null || AutomagyConfig.overrideForestryMilkAspects) {
            boolean noErrors = true;
            Class<?> forestryFluids = null;

            try {
                forestryFluids = Class.forName("forestry.core.fluids.Fluids");
            } catch (Exception var8) {
                noErrors = false;
                FMLLog.info("[Automagy] No Forestry integration because that mod was not found.");
            }

            if (forestryFluids != null) {
                Object milkObj = null;

                try {
                    Field f = forestryFluids.getField("MILK");
                    milkObj = f.get(null);
                } catch (Exception var7) {
                    noErrors = false;
                    FMLLog.info("[Automagy] Forestry integration failure: Could not find MILK.");
                    var7.printStackTrace();
                }

                if (milkObj != null) {
                    try {
                        Method m = forestryFluids.getMethod("getBlock");
                        Object o = m.invoke(milkObj);
                        if (o instanceof BlockFluidBase) {
                            if (AutomagyConfig.useForestryMilk && milkBlock == null) {
                                milkBlock = (BlockFluidBase)o;
                            }

                            if (AutomagyConfig.overrideForestryMilkAspects) {
                                ModRecipes.copyObjectAspects(ModBlocks.milk, (Block)o);
                            }
                        } else {
                            noErrors = false;
                            FMLLog.info("[Automagy] Forestry integration failure: MILK block was unexpected type.");
                        }
                    } catch (Exception var6) {
                        noErrors = false;
                        FMLLog.info("[Automagy] Forestry integration failure: Could not find MILK block.");
                        var6.printStackTrace();
                    }
                }
            }

            if (noErrors) {
                FMLLog.info("[Automagy] Successfully integrated with Forestry fluids.");
            }
        }

        if (milkBlock == null) {
            milkBlock = ModBlocks.milk;
        }

        if (mushroomSoupBlock == null) {
            mushroomSoupBlock = ModBlocks.mushroomFluid;
        }

        FluidContainerRegistry.registerFluidContainer(new FluidStack(milkBlock.getFluid(), 1000), new ItemStack(Items.milk_bucket), new ItemStack(Items.bucket));
        if (Loader.isModLoaded("ForbiddenMagic")) {
            Class<?> darkAspects = null;

            try {
                darkAspects = Class.forName("fox.spiteful.forbidden.DarkAspects");
            } catch (Exception var5) {
                FMLLog.log(Level.ERROR, "[Automagy] Failed to integrate with \"Forbidden Magic\" aspects.");
            }

            if (darkAspects != null) {
                CompatibilityManager.ForbiddenMagicAspect.NETHER = getAddonAspect(darkAspects, "NETHER");
                CompatibilityManager.ForbiddenMagicAspect.WRATH = getAddonAspect(darkAspects, "WRATH");
                CompatibilityManager.ForbiddenMagicAspect.ENVY = getAddonAspect(darkAspects, "ENVY");
                CompatibilityManager.ForbiddenMagicAspect.GLUTTONY = getAddonAspect(darkAspects, "GLUTTONY");
                CompatibilityManager.ForbiddenMagicAspect.PRIDE = getAddonAspect(darkAspects, "PRIDE");
                CompatibilityManager.ForbiddenMagicAspect.LUST = getAddonAspect(darkAspects, "LUST");
                CompatibilityManager.ForbiddenMagicAspect.SLOTH = getAddonAspect(darkAspects, "SLOTH");
                FMLLog.info("[Automagy] Finished integrating with Forbidden Magic aspects.");
            }
        }
    }

    public static boolean fillBotaniaPetalApothecary(TileEntity te, boolean doFill) {
        return !modLoadedBotania ? false : BotaniaHandler.fillBotaniaPetalApothecary(te, doFill);
    }

    private static Aspect getAddonAspect(Class clazz, String fieldName) {
        try {
            Field f = clazz.getField(fieldName);
            Object o = f.get(null);
            return o instanceof Aspect ? (Aspect)o : null;
        } catch (Exception var4) {
            FMLLog.log(Level.ERROR, "[Automagy] Failed to find \"Forbidden Magic\" aspect " + fieldName + ".");
            return null;
        }
    }

    public static class ForbiddenMagicAspect {
        public static Aspect NETHER;
        public static Aspect WRATH;
        public static Aspect ENVY;
        public static Aspect GLUTTONY;
        public static Aspect PRIDE;
        public static Aspect LUST;
        public static Aspect SLOTH;
    }
}
