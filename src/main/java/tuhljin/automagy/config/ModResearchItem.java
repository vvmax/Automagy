package tuhljin.automagy.config;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.apache.logging.log4j.Level;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionEnchantmentRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.api.research.ResearchPage.PageType;
import tuhljin.automagy.lib.ThaumcraftExtension;

public class ModResearchItem extends ResearchItem {
    private boolean isConcealedSameCategory;
    private String concealedUnlessResearch = "";

    public ModResearchItem(String key, AspectList tags, int col, int row, int complex, ItemStack icon) {
        super(key, "AUTOMAGY", tags, col, row, complex, icon);
    }

    public ModResearchItem(String key, AspectList tags, int col, int row, int complex, ResourceLocation icon) {
        super(key, "AUTOMAGY", tags, col, row, complex, icon);
    }

    public ModResearchItem(String key) {
        super(key, "AUTOMAGY");
    }

    @Override
    public String getName() {
        return StatCollector.translateToLocal("Automagy.research_name." + this.key);
    }

    @Override
    public String getText() {
        return StatCollector.translateToLocal("Automagy.research_text." + this.key);
    }

    public ModResearchItem setParentsHidden(String... par) {
        if (this.parentsHidden != null && Arrays.asList(this.parentsHidden).contains("INFUSION") && !Arrays.asList(par).contains("INFUSION")) {
            int l = par.length + 1;
            String[] newParentsHidden = Arrays.copyOf(par, l);
            newParentsHidden[l - 1] = "INFUSION";
            super.setParentsHidden(newParentsHidden);
        } else {
            super.setParentsHidden(par);
        }

        return this;
    }

    public ModResearchItem createPages(Object... par) {
        ResearchPage[] pages = new ResearchPage[par.length];
        boolean addedInfusionParent = false;
        int i = 0;

        for (Object obj : par) {
            ResearchPage page;
            if (obj instanceof ResearchPage) {
                page = (ResearchPage)obj;
            } else if (obj instanceof Item) {
                page = getRecipePage((Item)obj, -1);
            } else if (obj instanceof ItemStack) {
                ItemStack stack = (ItemStack)obj;
                page = getRecipePage(stack.getItem(), stack.getMetadata());
            } else if (obj instanceof Block) {
                page = getRecipePage((Block)obj, -1);
            } else if (obj instanceof String) {
                page = new ResearchPage("Automagy.research_page." + this.key + "." + (String)obj);
            } else if (obj instanceof String[]) {
                String[] arr = (String[])obj;
                page = new ResearchPage(arr[0], "Automagy.research_page." + this.key + "." + arr[1]);
            } else if (obj instanceof List) {
                page = new ResearchPage((List)obj);
            } else if (obj instanceof IArcaneRecipe) {
                page = new ResearchPage((IArcaneRecipe)obj);
            } else if (obj instanceof CrucibleRecipe) {
                page = new ResearchPage((CrucibleRecipe)obj);
            } else if (obj instanceof InfusionEnchantmentRecipe) {
                page = new ResearchPage((InfusionEnchantmentRecipe)obj);
            } else {
                FMLLog.log(Level.ERROR, "[Automagy] Failed to generate page #" + (i + 1) + " for ResearchItem with key \"" + this.key + "\"");
                page = new ResearchPage("ERROR: FAILED TO GENERATE PAGE #" + (i + 1));
            }

            if (!addedInfusionParent && page.type == PageType.INFUSION_CRAFTING) {
                if (this.parentsHidden != null && this.parentsHidden.length != 0) {
                    int l = this.parentsHidden.length + 1;
                    String[] newParentsHidden = Arrays.copyOf(this.parentsHidden, l);
                    newParentsHidden[l - 1] = "INFUSION";
                    super.setParentsHidden(newParentsHidden);
                } else {
                    super.setParentsHidden("INFUSION");
                }

                addedInfusionParent = true;
            }

            pages[i] = page;
            i++;
        }

        this.setPages(pages);
        return this;
    }

    public static ResearchPage getRecipePage(Object item, int damage) {
        boolean isBlock = item instanceof Block;
        if (isBlock || item instanceof Item) {
            Object o;
            if (damage == -1) {
                o = ModRecipes.getRecipe(item);
            } else {
                o = isBlock ? ModRecipes.getRecipe((Block)item, damage) : ModRecipes.getRecipe((Item)item, damage);
            }

            if (o instanceof InfusionRecipe) {
                Object o2;
                if (damage == -1) {
                    o2 = ModRecipes.getAlternateRecipe(item);
                } else {
                    o2 = isBlock ? ModRecipes.getAlternateRecipe((Block)item, damage) : ModRecipes.getAlternateRecipe((Item)item, damage);
                }

                if (o2 instanceof InfusionRecipe) {
                    return new ResearchPage(new InfusionRecipe[]{(InfusionRecipe)o, (InfusionRecipe)o2});
                }

                return new ResearchPage((InfusionRecipe)o);
            }

            if (o instanceof CrucibleRecipe) {
                return new ResearchPage((CrucibleRecipe)o);
            }

            if (o instanceof IArcaneRecipe) {
                Object o2x;
                if (damage == -1) {
                    o2x = ModRecipes.getAlternateRecipe(item);
                } else {
                    o2x = isBlock ? ModRecipes.getAlternateRecipe((Block)item, damage) : ModRecipes.getAlternateRecipe((Item)item, damage);
                }

                if (o2x instanceof IArcaneRecipe) {
                    return new ResearchPage(new IArcaneRecipe[]{(IArcaneRecipe)o, (IArcaneRecipe)o2x});
                }

                return new ResearchPage((IArcaneRecipe)o);
            }

            if (o instanceof IRecipe) {
                Object o2xx;
                if (damage == -1) {
                    o2xx = ModRecipes.getAlternateRecipe(item);
                } else {
                    o2xx = isBlock ? ModRecipes.getAlternateRecipe((Block)item, damage) : ModRecipes.getAlternateRecipe((Item)item, damage);
                }

                if (o2xx instanceof IRecipe) {
                    return new ResearchPage(new IRecipe[]{(IRecipe)o, (IRecipe)o2xx});
                }

                return new ResearchPage((IRecipe)o);
            }

            if (o instanceof List) {
                return new ResearchPage((List)o);
            }
        }

        String itemRef;
        if (isBlock) {
            itemRef = ((Block)item).getUnlocalizedName();
        } else if (item instanceof Item) {
            itemRef = ((Item)item).getUnlocalizedName();
        } else {
            itemRef = "[UNKNOWN]";
        }

        if (damage != -1) {
            itemRef = itemRef + ":" + damage;
        }

        FMLLog.log(Level.ERROR, "[Automagy] Failed to find recipe page for item \"" + itemRef + "\"");
        return new ResearchPage("ERROR: RECIPE NOT FOUND FOR \"" + itemRef + "\"");
    }

    public ModResearchItem setParents(String... par) {
        super.setParents(par);
        return this;
    }

    public ModResearchItem setSecondary() {
        super.setSecondary();
        return this;
    }

    public ModResearchItem setHidden() {
        super.setHidden();
        return this;
    }

    public ModResearchItem setHiddenUntilScanAndKillEntity(String... par) {
        this.setHidden();

        for (String s : par) {
            ModResearchItems.cluesOnKill.put(s, this.key);
        }

        return this;
    }

    public ModResearchItem setConcealedSameCategory() {
        this.setConcealed();
        this.isConcealedSameCategory = true;
        return this;
    }

    public ModResearchItem setConcealedUnless(String key) {
        this.setConcealed();
        this.concealedUnlessResearch = key;
        return this;
    }

    public boolean isConcealedSameCategory() {
        return this.isConcealedSameCategory;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isConcealed() {
        boolean conceal = super.isConcealed();
        return !conceal
                || (
                        this.concealedUnlessResearch.isEmpty()
                            || !ThaumcraftExtension.isResearchComplete(FMLClientHandler.instance().getClient().thePlayer, this.concealedUnlessResearch)
                    )
                    && (!this.isConcealedSameCategory || !this.allSameCategoryParentsResearched())
            ? conceal
            : false;
    }

    @SideOnly(Side.CLIENT)
    public boolean allSameCategoryParentsResearched() {
        ResearchItem res = this;
        String player = FMLClientHandler.instance().getClient().thePlayer.getDisplayName();
        if (super.parents != null && super.parents.length > 0) {
            for (String pt : super.parents) {
                ResearchItem parent = ResearchCategories.getResearch(pt);
                if (parent != null && parent.category == res.category && !ThaumcraftApiHelper.isResearchComplete(player, parent.key)) {
                    return false;
                }
            }
        }

        if (res.parentsHidden != null && res.parentsHidden.length > 0) {
            for (String ptx : res.parentsHidden) {
                ResearchItem parent = ResearchCategories.getResearch(ptx);
                if (parent != null && parent.category == res.category && !ThaumcraftApiHelper.isResearchComplete(player, parent.key)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void replacePage(int pageNum, ResearchPage newPage) {
        ResearchPage[] pages = this.getPages();
        pages[pageNum - 1] = newPage;
    }
}
