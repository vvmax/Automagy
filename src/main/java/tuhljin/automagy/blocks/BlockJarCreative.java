package tuhljin.automagy.blocks;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockJar;
import tuhljin.automagy.lib.AutomagyConfig;
import tuhljin.automagy.tiles.TileEntityJarCreative;

public class BlockJarCreative extends BlockJar {
    public static final int VALID_METADATA = 3;

    public BlockJarCreative(String name) {
        this.setUnlocalizedName(name);
        this.setTextureName("Automagy:" + name);
        if (AutomagyConfig.listCreativeJar && ModBlocks.tab != null) {
            this.setCreativeTab(ModBlocks.tab);
        } else {
            this.setCreativeTab(null);
        }

        GameRegistry.registerBlock(this, ItemBlockJarCreative.class, name);
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityJarCreative();
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 3));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);

        try {
            this.iconJarTopVoid = ir.registerIcon("Automagy:jar_top_infinity");
            this.iconJarSideVoid = ir.registerIcon("Automagy:jar_side_infinity");
        } catch (Exception var3) {
            FMLLog.bigWarning(
                "[Automagy] Failed to access Infinity Jar icons. Your version of Thaumcraft may be out of date. Please update to version 4.2.1.2 or later."
            );
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack != null && stack.getItem() instanceof IEssentiaContainerItem) {
            IEssentiaContainerItem item = (IEssentiaContainerItem)stack.getItem();
            AspectList aspects = item.getAspects(stack);
            if (aspects != null) {
                if (!world.isRemote) {
                    TileEntityJarCreative te = null;

                    try {
                        te = (TileEntityJarCreative)world.getTileEntity(x, y, z);
                    } catch (Exception var15) {
                    }

                    if (te != null && aspects.size() > 0) {
                        te.aspect = null;
                        te.amount = 0;
                        te.addToContainer(aspects.getAspects()[0], Math.min(1, aspects.getAmount(aspects.getAspects()[0])));
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        return new ArrayList<>();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        int r = rand.nextInt(10);
        if (r < 3) {
            float minX = -0.05F;
            float maxX = 0.05F;
            float finalX = rand.nextFloat() * (maxX - minX) + minX;
            TileEntityJarCreative te = null;

            try {
                te = (TileEntityJarCreative)world.getTileEntity(x, y, z);
            } catch (Exception var12) {
            }

            if (te != null && te.aspect != null) {
                Color c = new Color(te.aspect.getColor());
                Thaumcraft.proxy.blockRunes(world, x, y, z, c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, 7, finalX);
            } else {
                Thaumcraft.proxy.blockRunes(world, x, y, z, 1.1F, 0.51F, 1.56F, 7, finalX);
            }
        } else if (r < 7) {
            float x2 = x;
            float z2 = z;
            r = rand.nextInt(4);
            switch (r) {
                case 0:
                    x2 = (float)(x2 + rand.nextFloat() * 0.9);
                    z2 = (float)(z2 + 0.125);
                    break;
                case 1:
                    x2 = (float)(x2 + rand.nextFloat() * 0.9);
                    z2 = (float)(z2 + 0.875);
                    break;
                case 2:
                    x2 = (float)(x2 + 0.125);
                    z2 = (float)(z2 + rand.nextFloat() * 0.9);
                    break;
                case 3:
                    x2 = (float)(x2 + 0.875);
                    z2 = (float)(z2 + rand.nextFloat() * 0.9);
            }

            Thaumcraft.proxy.sparkle(x2, y + 0.1F, z2, 1.0F, 5, -0.02F);
        }
    }
}
