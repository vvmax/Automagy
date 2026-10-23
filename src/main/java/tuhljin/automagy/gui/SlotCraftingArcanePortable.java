package tuhljin.automagy.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.container.SlotCraftingArcaneWorkbench;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import tuhljin.automagy.items.ItemFocusCrafting;

public class SlotCraftingArcanePortable extends SlotCraftingArcaneWorkbench {
    public final ContainerPortableArcaneCrafting container;
    private final IInventory craftMatrix;
    private EntityPlayer thePlayer;

    public SlotCraftingArcanePortable(
        EntityPlayer par1EntityPlayer,
        IInventory par2iInventory,
        IInventory par3iInventory,
        int par4,
        int par5,
        int par6,
        ContainerPortableArcaneCrafting container
    ) {
        super(par1EntityPlayer, par2iInventory, par3iInventory, par4, par5, par6);
        this.thePlayer = par1EntityPlayer;
        this.craftMatrix = par2iInventory;
        this.container = container;
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack par1ItemStack) {
        AspectList aspects = ThaumcraftCraftingManager.findMatchingArcaneRecipeAspects(this.craftMatrix, this.thePlayer);
        if (aspects.size() > 0) {
            ItemStack wandstack = this.craftMatrix.getStackInSlot(10);
            if (wandstack != null) {
                ItemWandCasting wand = (ItemWandCasting)wandstack.getItem();
                ItemFocusCrafting focus = ItemFocusCrafting.getCraftingFocusOnWand(wandstack);
                AspectList extraCost = focus == null ? ItemFocusCrafting.visCost : focus.getVisCost(wand.getFocusItem(wandstack));
                if (extraCost.size() > 0) {
                    ThaumcraftApiHelper.consumeVisFromWand(wandstack, player, extraCost, true, false);
                }
            }
        }

        super.onPickupFromSlot(player, par1ItemStack);
        this.container.onSlotChanged(null);
    }
}
