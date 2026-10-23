package tuhljin.automagy.blocks;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import tuhljin.automagy.codechicken.lib.math.MathHelper;
import tuhljin.automagy.lib.References;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.renderers.IBlockGlowOverlayWithAlpha;
import tuhljin.automagy.renderers.IBlockGlowOverlayWithRecolor;
import tuhljin.automagy.tiles.TileEntityXPStone;

public class BlockXPStone extends ModBlockContainer implements IBlockGlowOverlayWithAlpha, IBlockGlowOverlayWithRecolor {
    public static float XP_FRACTION_LOST_ON_BREAK = 0.2F;
    private IIcon iconOverlay;
    private int lastColor = 2096945;

    public BlockXPStone(String name) {
        super(name, ModBlocks.materialNiceStone);
        this.setTextureName("Automagy:blockSoulStone");
        this.setHardness(5.0F);
        this.setResistance(2000.0F);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.iconOverlay = ir.registerIcon(this.textureName + "Overlay");
    }

    @Override
    public IIcon getOverlayTexture(IBlockAccess world, int x, int y, int z, int side) {
        return this.iconOverlay;
    }

    @Override
    public IIcon getOverlayInvTexture(int side, int meta) {
        return meta == 1 ? this.iconOverlay : null;
    }

    @Override
    public float getOverlayAlpha(IBlockAccess world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityXPStone) {
            float f = ((TileEntityXPStone)te).getXPFraction();
            if (f > 0.0F && f < 0.2F) {
                f = MathHelper.interpolate(0.17F, 0.2F, f / 0.2F);
            }

            return f;
        } else {
            return 0.0F;
        }
    }

    @Override
    public float getOverlayInvAlpha(int meta) {
        return meta == 1 ? 1.0F : 0.0F;
    }

    @Override
    public Integer getOverlayColor(IBlockAccess world, int x, int y, int z) {
        return this.getOverlayInvColor(0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Integer getOverlayInvColor(int meta) {
        World world = FMLClientHandler.instance().getClient().theWorld;
        long xpColor = world.getTotalWorldTime() % 150L;
        xpColor /= 15L;
        float f10 = 255.0F;
        float f11 = ((float)xpColor + 0.0F) / 2.0F;
        int l = (int)((net.minecraft.util.MathHelper.sin(f11 + 0.0F) + 1.0F) * 0.5F * f10);
        int i1 = (int)f10;
        int j1 = (int)((net.minecraft.util.MathHelper.sin(f11 + (float) (Math.PI * 4.0 / 3.0)) + 1.0F) * 0.1F * f10);
        int k1 = l << 16 | i1 << 8 | j1;
        this.lastColor = TjUtil.colorApproachLinear(this.lastColor, k1, 1.0);
        return this.lastColor;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer) {
        TileEntity te = worldObj.getTileEntity(target.blockX, target.blockY, target.blockZ);
        if (te instanceof TileEntityXPStone) {
            TileEntityXPStone stone = (TileEntityXPStone)te;
            int xp = stone.getXP();
            Random rand = worldObj.rand;
            if (xp > 0 && (rand.nextInt(15) == 0 || rand.nextInt(TileEntityXPStone.MAX_XP * 3) < xp)) {
                float size = MathHelper.interpolate(0.025F, 0.1F, stone.getXPFraction());
                Thaumcraft.proxy
                    .wispFX2(
                        worldObj,
                        target.hitVec.xCoord + (rand.nextDouble() - rand.nextDouble()) * 0.2,
                        target.hitVec.yCoord + (rand.nextDouble() - rand.nextDouble()) * 0.2,
                        target.hitVec.zCoord + (rand.nextDouble() - rand.nextDouble()) * 0.2,
                        size,
                        rand.nextBoolean() ? 1 : 3,
                        true,
                        false,
                        0.2F
                    );
                return true;
            }
        }

        return super.addHitEffects(worldObj, target, effectRenderer);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return References.renderBlockXPStone;
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityXPStone();
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        return ((TileEntityXPStone)world.getTileEntity(x, y, z)).getComparatorStrength();
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityXPStone) {
            int xp = ((TileEntityXPStone)te).getXP();
            if (xp > 0) {
                int loss = (int)(xp * XP_FRACTION_LOST_ON_BREAK);
                loss = Math.max(loss, 1);
                xp -= loss;
                if (xp > 0) {
                    TjUtil.spawnExperienceIntoWorld(xp, world, x + 0.5, y + 0.5, z + 0.5, true);
                }
            }
        }

        super.breakBlock(world, x, y, z, block, p_149749_6_);
    }
}
