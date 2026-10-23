package tuhljin.automagy.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;
import thaumcraft.common.blocks.BlockCustomOreItem;

public class ItemCrystalBrain extends ModSubtypedItem {
    @SideOnly(Side.CLIENT)
    public IIcon iconOverlay;

    public ItemCrystalBrain(String name) {
        super(name, 6);
    }

    @Override
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.iconOverlay = ir.registerIcon(this.getIconString() + "-overlay");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamageForRenderPass(int par1, int renderPass) {
        if (renderPass == 0) {
            return super.getIconFromDamageForRenderPass(par1, renderPass);
        } else {
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            return this.iconOverlay;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        return renderPass == 0 ? BlockCustomOreItem.colors[stack.getMetadata() + 1] : super.getColorFromItemStack(stack, renderPass);
    }
}
