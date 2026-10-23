package tuhljin.automagy.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.IconFlipped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

public class ItemGlyph extends ModSubtypedItem {
    public static final int CYCLE_ICONS_EXCEPT_BOVINE = 20;
    public static final int VOID = 1;
    public static final int CONSUMPTION = 2;
    public static final int SIPHONING = 3;
    public static final int ENVY = 4;
    public static final int TEMPERANCE = 5;
    public static final int PRESERVATION = 6;
    public static final int GUZZLER = 7;
    public static final int RESERVOIR = 8;
    public static final int BOVINE = 9;
    protected IIcon[] icons = new IIcon[9];
    protected IIcon[] iconsInstalled = new IIcon[9];

    public ItemGlyph(String name) {
        super(name, 10);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister ir) {
        this.itemIcon = ir.registerIcon("Automagy:glyph-aquaink");
        this.registerIcon(ir, 1, "void");
        this.registerIcon(ir, 2, "consume");
        this.registerIcon(ir, 3, "siphon");
        this.registerIcon(ir, 4, "envy");
        this.registerIcon(ir, 5, "temperance");
        this.registerIcon(ir, 6, "preserve");
        this.registerIcon(ir, 7, "guzzler");
        this.registerIcon(ir, 8, "reservoir");
        this.registerIcon(ir, 9, "bovine");
    }

    @SideOnly(Side.CLIENT)
    private void registerIcon(IIconRegister ir, int id, String s) {
        this.icons[id - 1] = ir.registerIcon("Automagy:glyph-" + s);
        this.iconsInstalled[id - 1] = ir.registerIcon("Automagy:glyphover-" + s);
        this.iconsInstalled[id - 1] = new IconFlipped(this.iconsInstalled[id - 1], true, false);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getInstalledIcon(int id) {
        return this.iconsInstalled[id - 1];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int d) {
        if (d == 20) {
            long t = System.currentTimeMillis() / 1000L;
            d = (int)(t % 9L);
        }

        return d > 0 && d <= this.icons.length ? this.icons[d - 1] : this.itemIcon;
    }

    public static String getLocalizedGlyphName(int id) {
        return StatCollector.translateToLocal("item.glyph." + id + ".name");
    }
}
