package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemBlockMirrorMultiDest extends ModSubtypedItemBlock {
    public static IIcon pane;
    public static IIcon paneTrans;
    public IIcon paneOpen;
    public IIcon inputFrame;

    public ItemBlockMirrorMultiDest(Block block) {
        super(block);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister ir) {
        pane = ir.registerIcon("Automagy:mirrorMultiDestPane");
        paneTrans = ir.registerIcon("Automagy:mirrorMultiDestPaneLinked");
        this.paneOpen = ir.registerIcon("thaumcraft:mirrorpaneopen");
        this.inputFrame = ir.registerIcon("Automagy:mirrorMultiDestFrame");
    }

    @Override
    public IIcon getIconFromDamageForRenderPass(int damage, int pass) {
        if (pass != 0) {
            return this.inputFrame;
        } else {
            return damage % 2 == 0 ? pane : paneTrans;
        }
    }

    @Override
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int d = stack.getMetadata() < 6 ? 0 : 6;
        return super.getUnlocalizedName() + "." + d;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.uncommon;
    }
}
