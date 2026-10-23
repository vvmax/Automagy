package tuhljin.automagy.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;

@Deprecated
public class RenderItemTransparentOverlay implements IItemRenderer {
    private static RenderItem renderItem = new RenderItem();

    @Override
    public boolean handleRenderType(ItemStack itemStack, ItemRenderType type) {
        return false;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack itemStack, Object... data) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        IIcon icon = itemStack.getIconIndex();
        renderItem.renderIcon(0, 0, icon, 16, 16);
    }
}
