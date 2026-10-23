package tuhljin.automagy.gui;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.config.Config;
import tuhljin.automagy.lib.SortItemStack;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.lib.compat.CompatibilityManager;
import tuhljin.automagy.lib.compat.nei.INEICompatPseudoSlots;
import tuhljin.automagy.lib.inventory.FilteringItemList;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.lib.inventory.IItemMap;
import tuhljin.automagy.lib.inventory.MappedItemsOrderedByTimeAdded;
import tuhljin.automagy.network.MessageGUIInventariumRequest;
import tuhljin.automagy.network.MessageItemStacksNeedPacket;
import tuhljin.automagy.tiles.IInventariumBook;
import tuhljin.automagy.tiles.IInventariumBookLinksMirror;

public class GUIScribe extends ModGuiContainer implements INEICompatPseudoSlots {
    public static int INVENTORY_ROWS = 6;
    public static int INVENTORY_ROW_SIZE = 10;
    public static int DEMAND_ROWS = 5;
    public static int DEMAND_ROW_SIZE = 3;
    public static int REQUESTSIZE_CTRL = 10;
    public IInventariumBook book;
    private InventoryPlayer inventoryPlayer;
    public final ResourceLocation texture = new ResourceLocation("Automagy:textures/gui/scribeItems.png");
    protected boolean showLocal = false;
    protected ArrayList<ItemStack> stacksLocal = new ArrayList<>();
    protected ArrayList<ItemStack> stacksInventarium = new ArrayList<>();
    protected GuiButtonPseudoSlot[] pSlots = new GuiButtonPseudoSlot[INVENTORY_ROWS * INVENTORY_ROW_SIZE];
    protected boolean sortingItemCount = true;
    protected List<ItemStack> stacksRequested = new ArrayList<>();
    protected GuiButtonPseudoSlot[] demandSlots = new GuiButtonPseudoSlot[DEMAND_ROWS * DEMAND_ROW_SIZE];
    public boolean hasDemand;
    public boolean hasLocal;
    public boolean canHaveMirror;
    protected GuiButtonHoverAndSound pagePrev;
    protected GuiButtonHoverAndSound pageNext;
    protected GuiButtonScaledText mirrorSettingsBtn;
    public int page = 1;
    protected int maxPages;
    protected GuiTextFieldWithReporting textField;
    private String prevText;
    protected GuiButtonCheckbox checkboxLocal;
    protected GuiButtonCheckbox checkboxSortByCount;
    private boolean alreadyInit = false;
    private ContainerScribe containerScribe;
    public static boolean alwaysTreatChunkAsUnloaded = true;
    public static boolean chunkLoaded = true;
    private static FilteringItemList contentsForUnloadedChunk;
    private static FilteringItemList contentsForUnloadedChunkLocal;
    private static MappedItemsOrderedByTimeAdded requestsForUnloadedChunk = new MappedItemsOrderedByTimeAdded("ItemsRequested");

    private GUIScribe(ContainerScribe containerScribe, InventoryPlayer inventoryPlayer, IInventariumBook book, boolean hasLocal, boolean hasDemand) {
        super(containerScribe);
        this.containerScribe = containerScribe;
        this.book = book;
        this.inventoryPlayer = inventoryPlayer;
        this.hasLocal = hasLocal;
        this.hasDemand = hasDemand;
        this.canHaveMirror = book instanceof IInventariumBookLinksMirror;
        this.xSize = 207;
        this.ySize = 223;
        this.scaleFactor = 0.5F;
        if (hasDemand) {
            this.xSize = this.xSize + ContainerScribe.XOFFSET_DEMAND;
        }

        if (hasLocal) {
            this.showLocal = book.doesPlayerPreferLocalView(inventoryPlayer.player);
        }

        this.sortingItemCount = !book.doesPlayerPreferSortByName(inventoryPlayer.player);
    }

    public GUIScribe(InventoryPlayer inventoryPlayer, World world, IInventariumBook book, boolean hasLocal, boolean hasDemand) {
        this(new ContainerScribe(inventoryPlayer, world, book, hasLocal, hasDemand), inventoryPlayer, book, hasLocal, hasDemand);
    }

    @Override
    public void initGui() {
        super.initGui();
        int xOffset = this.hasDemand ? ContainerScribe.XOFFSET_DEMAND : 0;
        int btns = 0;

        for (int y = 1; y <= INVENTORY_ROWS; y++) {
            for (int x = 1; x <= INVENTORY_ROW_SIZE; x++) {
                int xPos = this.guiLeft + 12 + (x - 1) * 18 + xOffset;
                if (x > INVENTORY_ROW_SIZE / 2) {
                    xPos += 6;
                }

                this.pSlots[btns] = new GuiButtonPseudoSlot(btns, this, xPos, this.guiTop + 8 + (y - 1) * 18);
                this.buttonList.add(this.pSlots[btns]);
                btns++;
            }
        }

        int y = this.guiTop + 27 + (INVENTORY_ROWS - 1) * 18;
        this.pagePrev = new GuiButtonHoverAndSound(btns++, this.guiLeft + 12 + xOffset, y, 11, 5, this.texture, 1, 226, 1, 232, "thaumcraft:page");
        this.buttonList.add(this.pagePrev);
        this.pageNext = new GuiButtonHoverAndSound(btns++, this.guiLeft + 184 + xOffset, y, 12, 5, this.texture, 12, 226, 12, 232, "thaumcraft:page");
        this.buttonList.add(this.pageNext);
        ResourceLocation temp = new ResourceLocation("Automagy:textures/gui/eagerChest.png");
        if (this.hasLocal) {
            this.checkboxLocal = new GuiButtonCheckbox(btns++, this.guiLeft + 108 + xOffset, this.guiTop + 119, !this.showLocal, temp, 176, 0);
            this.buttonList.add(this.checkboxLocal);
        }

        this.checkboxSortByCount = new GuiButtonCheckbox(btns++, this.guiLeft + 147 + xOffset, this.guiTop + 119, this.sortingItemCount, temp, 176, 0);
        this.buttonList.add(this.checkboxSortByCount);
        this.textField = new GuiTextFieldWithReporting(
            0, this, this.fontRendererObj, (int)((this.guiLeft + 28 + xOffset) / this.scaleFactorTextField), (int)((y + 1) / this.scaleFactorTextField), 94, 16
        );
        this.textField.setEmptyMessage(StatCollector.translateToLocal("Automagy.gui.scribe.search"));
        this.textFieldList.add(this.textField);
        this.textField.setFocused(true);
        if (chunkLoaded) {
            this.setStacks(this.book.getInventariumItemList(), false);
            if (this.hasLocal) {
                this.setStacks(this.book.getLocalItemList(), true);
            }
        } else {
            this.setStacks(contentsForUnloadedChunk, false);
            if (this.hasLocal) {
                this.setStacks(contentsForUnloadedChunkLocal, true);
            }
        }

        if (this.hasDemand) {
            int slot = 0;

            for (int var12 = 1; var12 <= DEMAND_ROWS; var12++) {
                for (int x = 1; x <= DEMAND_ROW_SIZE; x++) {
                    int xPos = this.guiLeft - 19 + (x - 1) * 18;
                    this.demandSlots[slot] = new GuiButtonPseudoSlot(btns, this, xPos, this.guiTop + 35 + (var12 - 1) * 18);
                    this.buttonList.add(this.demandSlots[slot]);
                    slot++;
                    btns++;
                }
            }

            if (chunkLoaded) {
                this.updateRequestedItems(this.book.getInventariumRequestedItemList());
            } else {
                this.updateRequestedItems(requestsForUnloadedChunk);
            }
        }

        if (this.canHaveMirror) {
            int height = (int)(20.0F * this.scaleFactor);
            this.mirrorSettingsBtn = new GuiButtonScaledText(
                0,
                this.guiLeft + 140,
                this.guiTop + 98 + height * 2,
                26,
                height,
                StatCollector.translateToLocal("Automagy.gui.scribe.mirrors"),
                this.scaleFactor,
                true
            );
            this.buttonList.add(this.mirrorSettingsBtn);
        }

        if (this.alreadyInit) {
            MessageItemStacksNeedPacket.sendToServer();
        } else {
            this.alreadyInit = true;
        }
    }

    public void setStacks(IItemMap list, boolean local) {
        if (list != null) {
            if (local) {
                this.stacksLocal = list.getItemStacks();
            } else {
                this.stacksInventarium = list.getItemStacks();
            }

            this.sortStacks(local);
        } else {
            if (local) {
                this.stacksLocal.clear();
            } else {
                this.stacksInventarium.clear();
            }

            this.updateStacksDisplay();
        }
    }

    public void sortStacks(boolean local) {
        if (this.sortingItemCount) {
            Collections.sort(local ? this.stacksLocal : this.stacksInventarium, new SortItemStack.CompareBySizeReverse());
        } else {
            Collections.sort(local ? this.stacksLocal : this.stacksInventarium, new SortItemStack.CompareByName());
        }

        if (local == this.showLocal) {
            this.updateStacksDisplay();
        }
    }

    public void updateStacksDisplay() {
        ArrayList<ItemStack> stacks = this.showLocal ? this.stacksLocal : this.stacksInventarium;
        String search = this.textField.getText().toLowerCase();
        int size = stacks.size();
        int slotNum = 0;
        int stackNum = INVENTORY_ROWS * INVENTORY_ROW_SIZE * (this.page - 1);

        while (true) {
            ItemStack stack;
            boolean found;
            do {
                if (slotNum >= this.pSlots.length) {
                    this.maxPages = Math.max((int)Math.ceil((double)stacks.size() / (INVENTORY_ROWS * INVENTORY_ROW_SIZE)), 1);
                    if (this.page > this.maxPages) {
                        this.page = this.maxPages;
                    }

                    return;
                }

                stack = stackNum < size ? stacks.get(stackNum) : null;
                stackNum++;
                if (stack == null || search.isEmpty()) {
                    break;
                }

                ArrayList<String> tooltip = (ArrayList<String>)stack.getTooltip(this.inventoryPlayer.player, false);
                found = false;

                for (String s : tooltip) {
                    s = EnumChatFormatting.getTextWithoutFormattingCodes(s);
                    if (s.toLowerCase().contains(search)) {
                        found = true;
                        break;
                    }
                }
            } while (!found);

            this.pSlots[slotNum].setItemStack(stack);
            slotNum++;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        int xOffset = this.hasDemand ? ContainerScribe.XOFFSET_DEMAND : 0;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(this.texture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k + xOffset, l, 0, 0, this.xSize - (this.hasDemand ? 25 : 0), INVENTORY_ROWS * 18 + 43);
        this.drawTexturedModalRect(k + xOffset, l + INVENTORY_ROWS * 18 + 17, 0, 126, this.xSize - (this.hasDemand ? 45 : 15), 96);
        if (this.hasDemand) {
            this.drawTexturedModalRect(k - 24, l + 23, 194, 151, 61, 105);
        }

        if (this.canHaveMirror) {
            this.drawTexturedModalRect(k + 14, l + 162, 214, 51, 32, 31);
            if (this.containerScribe.hasMirror()) {
                this.drawTexturedModalRect(k + 22, l + 169, 240, 17, 16, 16);
            } else {
                this.drawTexturedModalRect(k + 22, l + 169, 240, 0, 16, 16);
            }
        }

        GL11.glPushMatrix();
        GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        if (this.hasLocal) {
            this.drawStringScaledNoShadow(
                this.fontRendererObj,
                StatCollector.translateToLocal(this.showLocal ? "Automagy.gui.scribe.itemsLocal" : "Automagy.gui.scribe.itemsSystem"),
                this.guiLeft + 117 + xOffset,
                this.guiTop + 121,
                4144959
            );
        }

        this.drawStringScaledNoShadow(
            this.fontRendererObj,
            StatCollector.translateToLocal(this.sortingItemCount ? "Automagy.gui.scribe.sortCount" : "Automagy.gui.scribe.sortName"),
            this.guiLeft + 156 + xOffset,
            this.guiTop + 121,
            4144959
        );
        if (this.hasDemand) {
            this.drawCenteredStringScaledNoShadow(
                this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.scribe.deliver"), this.guiLeft + 7, this.guiTop + 27, 4144959
            );
        }

        GL11.glPopMatrix();
        if (CompatibilityManager.modLoadedNEI) {
            GL11.glPushMatrix();
            GL11.glTranslatef(this.guiLeft, this.guiTop, 0.0F);
            this.drawPseudoSlots(this.pSlots, var2, var3, true, false);
            if (this.hasDemand) {
                this.drawPseudoSlots(this.demandSlots, var2, var3, true, false);
            }

            GL11.glPopMatrix();
        }

        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
    }

    @Override
    public void drawScreen(int x, int y, float p_73863_3_) {
        super.drawScreen(x, y, p_73863_3_);
        GL11.glPushMatrix();
        GL11.glTranslatef(this.guiLeft, this.guiTop, 0.0F);
        this.drawPseudoSlots(this.pSlots, x, y, !CompatibilityManager.modLoadedNEI, true);
        if (this.hasDemand) {
            this.drawPseudoSlots(this.demandSlots, x, y, !CompatibilityManager.modLoadedNEI, true);
        }

        GL11.glPopMatrix();
        int scroll = Mouse.getDWheel();
        if (scroll < 0) {
            int oldPage = this.page;
            this.buttonClicked(this.pageNext, false);
            if (this.page != oldPage) {
                this.pageNext.playPressSound(this.mc.getSoundHandler());
            }
        } else if (scroll > 0) {
            int oldPage = this.page;
            this.buttonClicked(this.pagePrev, false);
            if (this.page != oldPage) {
                this.pagePrev.playPressSound(this.mc.getSoundHandler());
            }
        }

        if (this.textField != null && !this.textField.getText().equals(this.prevText)) {
            this.prevText = this.textField.getText();
            this.page = 1;
            this.updateStacksDisplay();
        }
    }

    public void drawPseudoSlots(GuiButtonPseudoSlot[] slots, int x, int y, boolean hoverBox, boolean tooltipOrAspects) {
        for (int i = 0; i < slots.length; i++) {
            GuiButtonPseudoSlot pSlot = slots[i];
            if (pSlot != null && pSlot.pointIsOver(x, y)) {
                if (this.inventoryPlayer.getItemStack() == null) {
                    if (hoverBox) {
                        GL11.glDisable(2896);
                        GL11.glDisable(2929);
                        int j1 = pSlot.xPosition - this.guiLeft;
                        int k1 = pSlot.yPosition - this.guiTop;
                        GL11.glColorMask(true, true, true, false);
                        this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
                        GL11.glColorMask(true, true, true, true);
                        GL11.glEnable(2896);
                        GL11.glEnable(2929);
                    }

                    if (tooltipOrAspects) {
                        ItemStack stack = pSlot.getItemStack();
                        if (stack != null) {
                            if (!CompatibilityManager.modLoadedNEI) {
                                this.renderToolTip(stack, x - this.guiLeft, y - this.guiTop + 10);
                            }

                            if ((GuiScreen.isShiftKeyDown() && !Config.showTags || !GuiScreen.isShiftKeyDown() && Config.showTags && !Mouse.isGrabbed())
                                && ThaumcraftExtension.hasPlayerScanned(this.mc.thePlayer, stack)) {
                                AspectList tags = ThaumcraftExtension.getObjectTotalAspects(stack);
                                if (tags != null) {
                                    ThaumcraftExtension.renderAspectsForPlayer(this, this.mc.thePlayer, tags, x - this.guiLeft, y - this.guiTop);
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void buttonClicked(GuiButton button, boolean rightClick) {
        if (button == this.pageNext) {
            if (this.page < this.maxPages && this.pSlots[this.pSlots.length - 1].getItemStack() != null) {
                this.page++;
                this.updateStacksDisplay();
            }
        } else if (button == this.pagePrev) {
            if (this.page > 1) {
                this.page--;
                this.updateStacksDisplay();
            }
        } else if (button == this.checkboxLocal) {
            this.checkboxLocal.checked = !this.checkboxLocal.checked;
            this.showLocal = !this.checkboxLocal.checked;
            this.page = 1;
            this.sortStacks(this.showLocal);
            this.book.setPlayerLocalViewPreference(this.inventoryPlayer.player, this.showLocal);
        } else if (button == this.checkboxSortByCount) {
            this.checkboxSortByCount.checked = !this.checkboxSortByCount.checked;
            this.sortingItemCount = this.checkboxSortByCount.checked;
            this.sortStacks(this.showLocal);
            this.book.setPlayerSortingPreference(this.inventoryPlayer.player, !this.sortingItemCount);
        } else if (button == this.mirrorSettingsBtn) {
            this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, 0);
        } else if (this.hasDemand && button instanceof GuiButtonPseudoSlot) {
            ItemStack stack = ((GuiButtonPseudoSlot)button).getItemStack();
            if (stack != null) {
                int amt = rightClick ? -1 : 1;
                if (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)) {
                    int max = stack.getMaxStackSize();
                    if (max > 1) {
                        amt *= max;
                    }
                } else if (Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157)) {
                    amt *= REQUESTSIZE_CTRL;
                }

                boolean isDemandSlot = button.id >= this.demandSlots[0].id;
                if (isDemandSlot) {
                    if (stack.stackSize + amt < 0) {
                        amt = stack.stackSize * -1;
                    }
                } else if (amt > 0 && !Keyboard.isKeyDown(56) && !Keyboard.isKeyDown(184)) {
                    int currentDemand = this.getDisplayedRequestAmount(new HashableItemWithoutSize(stack));
                    if (currentDemand >= stack.stackSize) {
                        amt = 0;
                    } else if (stack.stackSize - currentDemand - amt < 0) {
                        amt = stack.stackSize - currentDemand;
                    }
                }

                if (amt != 0) {
                    ItemStack requestStack = stack.copy();
                    requestStack.stackSize = amt;
                    MessageGUIInventariumRequest.sendToServer(this.book, requestStack, !isDemandSlot);
                    if (isDemandSlot) {
                        int count = stack.stackSize + amt;
                        stack.stackSize += amt;
                    }
                }
            }
        }
    }

    @Override
    public GuiButtonPseudoSlot getPseudoSlotAt(int x, int y) {
        for (int i = 0; i < this.pSlots.length; i++) {
            GuiButtonPseudoSlot pSlot = this.pSlots[i];
            if (pSlot.pointIsOver(x, y)) {
                return pSlot;
            }
        }

        if (this.hasDemand) {
            for (int ix = 0; ix < this.demandSlots.length; ix++) {
                GuiButtonPseudoSlot pSlot = this.demandSlots[ix];
                if (pSlot.pointIsOver(x, y)) {
                    return pSlot;
                }
            }
        }

        return null;
    }

    public void updateRequestedItems(MappedItemsOrderedByTimeAdded list) {
        if (list != null) {
            this.stacksRequested = list.getItemStacks();
            this.stacksRequested = Lists.reverse(this.stacksRequested);
        }

        this.updateRequestedStacksDisplay();
    }

    public void updateRequestedStacksDisplay() {
        int demandPage = 1;
        String search = this.textField.getText().toLowerCase();
        int size = this.stacksRequested.size();
        int slotNum = 0;
        int stackNum = DEMAND_ROWS * DEMAND_ROW_SIZE * (demandPage - 1);

        while (true) {
            ItemStack stack;
            boolean found;
            do {
                if (slotNum >= this.demandSlots.length) {
                    return;
                }

                stack = stackNum < size ? this.stacksRequested.get(stackNum) : null;
                stackNum++;
                if (stack == null) {
                    break;
                }

                boolean searchAffectsDemandListing = true;
                if (!searchAffectsDemandListing || search.isEmpty()) {
                    break;
                }

                ArrayList<String> tooltip = (ArrayList<String>)stack.getTooltip(this.inventoryPlayer.player, false);
                found = false;

                for (String s : tooltip) {
                    s = EnumChatFormatting.getTextWithoutFormattingCodes(s);
                    if (s.toLowerCase().contains(search)) {
                        found = true;
                        break;
                    }
                }
            } while (!found);

            this.demandSlots[slotNum].setItemStack(stack);
            slotNum++;
        }
    }

    public int getDisplayedRequestAmount(HashableItemWithoutSize item) {
        if (this.hasDemand) {
            for (GuiButtonPseudoSlot pSlot : this.demandSlots) {
                if (item.isSameItem(pSlot.getItemStack())) {
                    return pSlot.stack.stackSize;
                }
            }
        }

        return 0;
    }

    public static void handleStacksForUnloadedChunk(ArrayList<ItemStack> list, boolean partial, boolean isRequestList, boolean local) {
        chunkLoaded = false;
        if (isRequestList) {
            if (!partial) {
                requestsForUnloadedChunk.setAll(list);
            } else {
                boolean needUpdate = false;
                GuiScreen gui = Minecraft.getMinecraft().currentScreen;
                GUIScribe guiScribe = gui instanceof GUIScribe ? (GUIScribe)gui : null;

                for (ItemStack stack : list) {
                    HashableItemWithoutSize item = new HashableItemWithoutSize(stack);
                    requestsForUnloadedChunk.set(item, stack.stackSize);
                    if (guiScribe != null) {
                        int amt = guiScribe.getDisplayedRequestAmount(item);
                        if (amt == 0 || amt != stack.stackSize) {
                            needUpdate = true;
                        }
                    }
                }

                if (needUpdate) {
                    guiScribe.updateRequestedItems(requestsForUnloadedChunk);
                }
            }
        } else if (local) {
            if (partial && contentsForUnloadedChunkLocal != null) {
                for (ItemStack stackx : list) {
                    contentsForUnloadedChunkLocal.set(new HashableItemWithoutSize(stackx), stackx.stackSize > 0 ? stackx.stackSize : 0);
                }
            } else {
                contentsForUnloadedChunkLocal = new FilteringItemList(list, false);
            }

            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            if (gui instanceof GUIScribe) {
                ((GUIScribe)gui).setStacks(contentsForUnloadedChunkLocal, false);
            }
        } else {
            if (partial && contentsForUnloadedChunk != null) {
                for (ItemStack stackx : list) {
                    contentsForUnloadedChunk.set(new HashableItemWithoutSize(stackx), stackx.stackSize > 0 ? stackx.stackSize : 0);
                }
            } else {
                contentsForUnloadedChunk = new FilteringItemList(list, false);
            }

            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            if (gui instanceof GUIScribe) {
                ((GUIScribe)gui).setStacks(contentsForUnloadedChunk, false);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (!chunkLoaded) {
            chunkLoaded = true;
            contentsForUnloadedChunk = null;
            contentsForUnloadedChunkLocal = null;
            requestsForUnloadedChunk.clear();
        }
    }
}
