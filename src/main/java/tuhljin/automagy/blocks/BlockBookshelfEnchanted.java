package tuhljin.automagy.blocks;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBookshelfEnchanted extends ModBlock implements IInventariumSlotUpgrade {
    public static int ENCHANTPOWER_GREATWOOD = 4;
    public static int ENCHANTPOWER_SILVERWOOD = 8;
    public static int READSLOTS_GREATWOOD = 216;
    public static int READSLOTS_SILVERWOOD = 486;
    public IIcon greatwoodTopBtm;
    public IIcon silverwoodSide;
    public IIcon silverwoodTopBtm;

    public BlockBookshelfEnchanted(String name) {
        super(name, Material.wood, ModSubtypedItemBlock.class);
        this.setHardness(1.5F);
        this.setStepSound(soundTypeWood);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
    }

    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        String t = this.getTextureName();
        this.greatwoodTopBtm = ir.registerIcon("thaumcraft:planks_greatwood");
        this.silverwoodTopBtm = ir.registerIcon("thaumcraft:planks_silverwood");
        this.silverwoodSide = ir.registerIcon(t + "Silverwood");
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (meta == 1) {
            return side < 2 ? this.silverwoodTopBtm : this.silverwoodSide;
        } else {
            return side < 2 ? this.greatwoodTopBtm : this.blockIcon;
        }
    }

    @Override
    public int damageDropped(int d) {
        return d;
    }

    @Override
    public float getEnchantPowerBonus(World world, int x, int y, int z) {
        return world.getBlockMetadata(x, y, z) == 1 ? ENCHANTPOWER_SILVERWOOD : ENCHANTPOWER_GREATWOOD;
    }

    @Override
    public int getInventariumSlotAllowance(IBlockAccess blockaccess, int x, int y, int z) {
        return blockaccess.getBlockMetadata(x, y, z) == 1 ? READSLOTS_SILVERWOOD : READSLOTS_GREATWOOD;
    }
}
