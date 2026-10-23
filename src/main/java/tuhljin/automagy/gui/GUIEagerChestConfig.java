package tuhljin.automagy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.items.InventoryWithFilterOptions;
import tuhljin.automagy.items.ItemEnchantedPaper;
import tuhljin.automagy.network.MessageGUIEagerChestConfig;
import tuhljin.automagy.network.PacketHandler;
import tuhljin.automagy.tiles.TileEntityEagerChest;

public class GUIEagerChestConfig extends ModGuiContainer {
    public TileEntityEagerChest te;
    public final ResourceLocation texture = new ResourceLocation("Automagy:textures/gui/eagerChest.png");
    private GuiButtonCheckbox checkbox;
    private boolean[] filterExtraText = new boolean[]{false, false, false, false};
    private boolean[] filterIsBlacklist = new boolean[]{false, false, false, false};

    private GUIEagerChestConfig(InventoryPlayer inventoryPlayer, TileEntityEagerChest te, ContainerEagerChestConfig container) {
        super(container);
        this.te = te;
        container.attachToGui(this);
    }

    public GUIEagerChestConfig(InventoryPlayer inventoryPlayer, TileEntityEagerChest te) {
        this(inventoryPlayer, te, new ContainerEagerChestConfig(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();
        this.checkbox = new GuiButtonCheckbox(0, this.guiLeft + 30, this.guiTop + 65, this.te.clientIsValidUser, this.texture, this.xSize, 0);
        this.buttonList.add(this.checkbox);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        this.checkbox.checked = this.te.clientIsValidUser;
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        GL11.glPushMatrix();
        GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        this.drawCenteredStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.eagerChest.give"), this.guiLeft + 27, this.guiTop, 16777215
        );
        this.drawCenteredStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.eagerChest.take"), this.guiLeft + 113, this.guiTop, 16777215
        );
        this.drawStringScaled(
            this.fontRendererObj, StatCollector.translateToLocal("Automagy.gui.eagerChest.trade"), this.guiLeft + 42, this.guiTop + 66, 16777215
        );
        GL11.glPopMatrix();

        for (int i = 0; i < this.filterExtraText.length; i++) {
            if (this.filterExtraText[i]) {
                float old = this.scaleFactor;
                this.scaleFactor *= 0.7F;
                GL11.glPushMatrix();
                GL11.glScalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
                String s = "Automagy.gui.eagerChest.quantityResult.";
                s = s + (i < 2 ? "give" : "take");
                s = s + (this.filterIsBlacklist[i] ? "Blacklist." : "Whitelist.");
                float x = this.guiLeft + (i < 2 ? 40.5F : 128.0F);
                float y = this.guiTop + ((i + 1) % 2 == 0 ? 44.5F : 16.0F);
                this.drawStringScaled(this.fontRendererObj, StatCollector.translateToLocal(s + "1"), x, y, 16777215);
                this.drawStringScaled(this.fontRendererObj, StatCollector.translateToLocal(s + "2"), x, y + 6.0F, 16777215);
                GL11.glPopMatrix();
                this.scaleFactor = old;
            }
        }

        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
    }

    @Override
    protected void buttonClicked(GuiButton button, boolean rightClick) {
        if (button instanceof GuiButtonCheckbox) {
            GuiButtonCheckbox checkbox = (GuiButtonCheckbox)button;
            boolean optIn = !checkbox.checked;
            this.te.clientIsValidUser = optIn;
            PacketHandler.INSTANCE.sendToServer(new MessageGUIEagerChestConfig(optIn));
        }
    }

    @Override
    public void onSlotChanged(SlotRestrictedWithReporting slot) {
        ItemStack stack = slot.getStack();
        if (stack != null) {
            this.filterIsBlacklist[slot.id] = ItemEnchantedPaper.stackIsBlacklist(stack);
            InventoryWithFilterOptions inv = ItemEnchantedPaper.getFilterInventory(stack);
            if (inv != null) {
                this.filterExtraText[slot.id] = inv.useItemCount;
                return;
            }
        }

        this.filterExtraText[slot.id] = false;
    }
}
