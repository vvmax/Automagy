package tuhljin.automagy.lib.compat.nei;

import codechicken.lib.inventory.InventoryUtils;
import codechicken.nei.FastTransferManager;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.DefaultOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.DefaultOverlayHandler.DistributedIngred;
import codechicken.nei.recipe.DefaultOverlayHandler.IngredientDistribution;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SimpleCraftingOverlayHandler extends DefaultOverlayHandler {
    public final int firstCraftingSlotNumber;
    public final int columnSlotDivisor;
    public final int rowSlotDivisor;
    public final int rows;
    public final int columns;

    public SimpleCraftingOverlayHandler(int firstCraftingSlotNumber, int rows, int columns, int columnSlotDivisor, int rowSlotDivisor, int offsetX, int offsetY) {
        super(offsetX, offsetY);
        this.firstCraftingSlotNumber = firstCraftingSlotNumber;
        this.rows = rows;
        this.columns = columns;
        this.columnSlotDivisor = columnSlotDivisor;
        this.rowSlotDivisor = rowSlotDivisor;
    }

    public SimpleCraftingOverlayHandler(int firstCraftingSlotNumber) {
        this(firstCraftingSlotNumber, 3, 3, 18, 18, 25, 6);
    }

    public void overlayRecipe(GuiContainer gui, IRecipeHandler recipe, int recipeIndex, boolean shift) {
        List<PositionedStack> ingredients = this.getIngredientStacksFromRecipe(recipe, recipeIndex);
        List<DistributedIngred> ingredStacks = this.getPermutationIngredients(ingredients);
        if (this.clearIngredients(gui, ingredients)) {
            this.findInventoryQuantities(gui, ingredStacks);
            List<IngredientDistribution> assignedIngredients = this.assignIngredients(ingredients, ingredStacks);
            if (assignedIngredients != null) {
                this.assignIngredSlots(gui, ingredients, assignedIngredients);
                int quantity = this.calculateRecipeQuantity(assignedIngredients);
                if (quantity != 0) {
                    this.moveIngredients(gui, assignedIngredients, quantity);
                }
            }
        }
    }

    public List<PositionedStack> getIngredientStacksFromRecipe(IRecipeHandler recipe, int recipeIndex) {
        return recipe.getIngredientStacks(recipeIndex);
    }

    public Slot[][] mapIngredSlots(GuiContainer gui, List<PositionedStack> ingredients) {
        int size = ingredients.size();
        Slot[][] recipeSlotList = new Slot[size][];
        int slotCount = 0;
        int numIngredientSlots = this.rows * this.columns;

        for (int i = 0; i < size; i++) {
            LinkedList<Slot> recipeSlots = new LinkedList<>();
            PositionedStack pstack = ingredients.get(i);
            if (slotCount < numIngredientSlots) {
                int column = this.getColumn(pstack);
                int row = this.getRow(pstack);
                Slot slot = gui.inventorySlots.getSlot(this.firstCraftingSlotNumber + column + row * 3);
                recipeSlots.add(slot);
                slotCount++;
            }

            recipeSlotList[i] = recipeSlots.toArray(new Slot[0]);
        }

        return recipeSlotList;
    }

    protected int getColumn(PositionedStack pstack) {
        return Math.round((float)(pstack.relx - this.offsetx) / this.columnSlotDivisor);
    }

    protected int getRow(PositionedStack pstack) {
        return Math.round((float)(pstack.rely - this.offsety) / this.rowSlotDivisor);
    }

    protected boolean clearIngredients(GuiContainer gui, List<PositionedStack> ingreds) {
        int numIngredientSlots = this.rows * this.columns;

        for (int i = this.firstCraftingSlotNumber; i < this.firstCraftingSlotNumber + numIngredientSlots; i++) {
            Slot slot = gui.inventorySlots.getSlot(i);
            if (slot.getHasStack()) {
                FastTransferManager.clickSlot(gui, slot.slotNumber, 0, 1);
                if (slot.getHasStack()) {
                    return false;
                }
            }
        }

        return true;
    }

    protected List<DistributedIngred> getPermutationIngredients(List<PositionedStack> ingredients) {
        ArrayList<DistributedIngred> ingredStacks = new ArrayList<>();

        for (PositionedStack posstack : ingredients) {
            for (ItemStack pstack : posstack.items) {
                DistributedIngred istack = this.findIngred(ingredStacks, pstack);
                if (istack == null) {
                    ingredStacks.add(istack = new DistributedIngred(pstack));
                }

                istack.recipeAmount = istack.recipeAmount + pstack.stackSize;
            }
        }

        return ingredStacks;
    }

    protected void findInventoryQuantities(GuiContainer gui, List<DistributedIngred> ingredStacks) {
        for (Slot slot : gui.inventorySlots.inventorySlots) {
            if (slot.getHasStack() && this.canMoveFrom(slot, gui)) {
                ItemStack pstack = slot.getStack();
                DistributedIngred istack = this.findIngred(ingredStacks, pstack);
                if (istack != null) {
                    istack.invAmount = istack.invAmount + pstack.stackSize;
                }
            }
        }
    }

    protected Slot[][] assignIngredSlots(GuiContainer gui, List<PositionedStack> ingredients, List<IngredientDistribution> assignedIngredients) {
        Slot[][] recipeSlots = this.mapIngredSlots(gui, ingredients);
        HashMap<Slot, Integer> distribution = new HashMap<>();

        for (int i = 0; i < recipeSlots.length; i++) {
            for (Slot slot : recipeSlots[i]) {
                if (!distribution.containsKey(slot)) {
                    distribution.put(slot, -1);
                }
            }
        }

        HashSet<Slot> avaliableSlots = new HashSet<>(distribution.keySet());
        HashSet<Integer> remainingIngreds = new HashSet<>();
        ArrayList<LinkedList<Slot>> assignedSlots = new ArrayList<>();

        for (int i = 0; i < ingredients.size(); i++) {
            remainingIngreds.add(i);
            assignedSlots.add(new LinkedList<>());
        }

        while (avaliableSlots.size() > 0 && remainingIngreds.size() > 0) {
            Iterator<Integer> iterator = remainingIngreds.iterator();

            while (iterator.hasNext()) {
                int i = iterator.next();
                boolean assigned = false;
                DistributedIngred istack = assignedIngredients.get(i).distrib;

                for (Slot slotx : recipeSlots[i]) {
                    if (avaliableSlots.contains(slotx)) {
                        avaliableSlots.remove(slotx);
                        if (!slotx.getHasStack()) {
                            istack.numSlots++;
                            assignedSlots.get(i).add(slotx);
                            assigned = true;
                            break;
                        }
                    }
                }

                if (!assigned || istack.numSlots * istack.stack.getMaxStackSize() >= istack.invAmount) {
                    iterator.remove();
                }
            }
        }

        for (int i = 0; i < ingredients.size(); i++) {
            assignedIngredients.get(i).slots = assignedSlots.get(i).toArray(new Slot[0]);
        }

        return recipeSlots;
    }

    protected List<IngredientDistribution> assignIngredients(List<PositionedStack> ingredients, List<DistributedIngred> ingredStacks) {
        ArrayList<IngredientDistribution> assignedIngredients = new ArrayList<>();

        for (PositionedStack posstack : ingredients) {
            DistributedIngred biggestIngred = null;
            ItemStack permutation = null;
            int biggestSize = 0;

            for (ItemStack pstack : posstack.items) {
                for (int j = 0; j < ingredStacks.size(); j++) {
                    DistributedIngred istack = ingredStacks.get(j);
                    if (InventoryUtils.canStack(pstack, istack.stack) && istack.invAmount - istack.distributed >= pstack.stackSize) {
                        int relsize = (istack.invAmount - istack.invAmount / istack.recipeAmount * istack.distributed) / pstack.stackSize;
                        if (relsize > biggestSize) {
                            biggestSize = relsize;
                            biggestIngred = istack;
                            permutation = pstack;
                            break;
                        }
                    }
                }
            }

            if (biggestIngred == null) {
                return null;
            }

            biggestIngred.distributed = biggestIngred.distributed + permutation.stackSize;
            assignedIngredients.add(new IngredientDistribution(biggestIngred, permutation));
        }

        return assignedIngredients;
    }

    protected int calculateRecipeQuantity(List<IngredientDistribution> assignedIngredients) {
        int quantity = Integer.MAX_VALUE;

        for (IngredientDistribution distrib : assignedIngredients) {
            DistributedIngred istack = distrib.distrib;
            if (istack.numSlots == 0) {
                return 0;
            }

            int allSlots = istack.invAmount;
            if (allSlots / istack.numSlots > istack.stack.getMaxStackSize()) {
                allSlots = istack.numSlots * istack.stack.getMaxStackSize();
            }

            quantity = Math.min(quantity, allSlots / istack.distributed);
        }

        return quantity;
    }

    protected void moveIngredients(GuiContainer gui, List<IngredientDistribution> assignedIngredients, int quantity) {
        for (IngredientDistribution distrib : assignedIngredients) {
            ItemStack pstack = distrib.permutation;
            int transferCap = quantity * pstack.stackSize;
            int transferred = 0;
            int destSlotIndex = 0;
            Slot dest = distrib.slots[0];
            int slotTransferred = 0;
            int slotTransferCap = pstack.getMaxStackSize();

            for (Slot slot : gui.inventorySlots.inventorySlots) {
                if (slot.getHasStack() && this.canMoveFrom(slot, gui)) {
                    ItemStack stack = slot.getStack();
                    if (InventoryUtils.canStack(stack, pstack)) {
                        FastTransferManager.clickSlot(gui, slot.slotNumber);
                        int amount = Math.min(transferCap - transferred, stack.stackSize);

                        for (int c = 0; c < amount; c++) {
                            FastTransferManager.clickSlot(gui, dest.slotNumber, 1);
                            transferred++;
                            if (++slotTransferred >= slotTransferCap) {
                                if (++destSlotIndex == distrib.slots.length) {
                                    dest = null;
                                    break;
                                }

                                dest = distrib.slots[destSlotIndex];
                                slotTransferred = 0;
                            }
                        }

                        FastTransferManager.clickSlot(gui, slot.slotNumber);
                        if (transferred >= transferCap || dest == null) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
