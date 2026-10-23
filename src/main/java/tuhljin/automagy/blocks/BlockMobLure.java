package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.Color;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.common.blocks.BlockCosmeticSolid;
import thaumcraft.common.blocks.BlockCustomOreItem;
import tuhljin.automagy.lib.References;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.renderers.IBlockGlowOverlayWithRecolor;
import tuhljin.automagy.tiles.TileEntityMobLure;

public class BlockMobLure extends ModBlockContainer implements IBlockGlowOverlayWithRecolor {
    private IIcon iconOverlay;
    public static int[] colors = BlockCustomOreItem.colors;
    public int colorTarget = 1;
    public int color = colors[0];
    private static final int FadeSteps = 750;
    int nextStep = 0;
    int prevColor = colors[0];
    public long nextNoise = 0L;

    public BlockMobLure(String name) {
        super(name, ModBlocks.materialNiceStone);
        this.setHarvestLevel("pickaxe", 2);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityMobLure();
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.iconOverlay = ir.registerIcon(this.textureName + "Overlay");
    }

    @Override
    public MapColor getMapColor(int p_149728_1_) {
        return MapColor.obsidianColor;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!world.isRemote && block instanceof BlockCosmeticSolid) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityMobLure) {
                TileEntityMobLure teML = (TileEntityMobLure)te;
                if (!teML.calcVerticalLimits()) {
                    teML.tryDisable();
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (ThaumcraftExtension.playerHasWandEquipped(player)) {
            if (!world.isRemote) {
                TileEntityMobLure te = null;

                try {
                    te = (TileEntityMobLure)world.getTileEntity(x, y, z);
                } catch (Exception var12) {
                }

                if (te != null) {
                    if (te.isEnabled()) {
                        te.tryDisable();
                    } else {
                        te.tryEnable();
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public IIcon getOverlayTexture(IBlockAccess world, int x, int y, int z, int side) {
        return world.getBlockMetadata(x, y, z) == 0 ? null : this.iconOverlay;
    }

    @Override
    public IIcon getOverlayInvTexture(int side, int meta) {
        return this.iconOverlay;
    }

    @Override
    public Integer getOverlayColor(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        return meta == 0 ? null : this.getOverlayInvColor(meta);
    }

    @Override
    public Integer getOverlayInvColor(int meta) {
        this.color = this.fade(colors[this.colorTarget]);
        return this.color;
    }

    @Override
    public int getRenderType() {
        return References.renderBlockMobLure;
    }

    private Color fade(Color oldColour, Color colour, int currentStep) {
        int dRed = colour.getRed() - oldColour.getRed();
        int dGreen = colour.getGreen() - oldColour.getGreen();
        int dBlue = colour.getBlue() - oldColour.getBlue();
        return dRed == 0 && dGreen == 0 && dBlue == 0
            ? colour
            : new Color(
                oldColour.getRed() + dRed * currentStep / 750,
                oldColour.getGreen() + dGreen * currentStep / 750,
                oldColour.getBlue() + dBlue * currentStep / 750
            );
    }

    private int fade(int colour) {
        if (this.nextStep > 750) {
            this.prevColor = colour;
            if (this.nextStep > 950) {
                this.nextStep = 0;
                this.colorTarget++;
                if (this.colorTarget >= colors.length) {
                    this.colorTarget = 0;
                }
            } else {
                this.nextStep++;
            }

            return colour;
        } else {
            Color c = this.fade(new Color(this.prevColor), new Color(colour), this.nextStep);
            this.nextStep++;
            return c.getRGB();
        }
    }

    public boolean canLureAffectEntity(IBlockAccess world, int x, int y, int z, EntityLivingBase entity, double rangeSquared) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityMobLure) {
            TileEntityMobLure teML = (TileEntityMobLure)te;
            if (teML.isEnabled()) {
                return this.canActiveLureAffectEntity(teML, entity, rangeSquared);
            }
        }

        return false;
    }

    public boolean canActiveLureAffectEntity(TileEntityMobLure te, EntityLivingBase entity, double rangeSquared) {
        if (entity.posY >= te.yBottom && entity.posY <= te.yTop) {
            double x1 = te.xCoord + 0.5;
            double z1 = te.zCoord + 0.5;
            double x2 = entity.posX;
            double z2 = entity.posZ;
            double distanceXZ = (x1 - x2) * (x1 - x2) + (z1 - z2) * (z1 - z2);
            if (distanceXZ <= rangeSquared) {
                return true;
            }
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        if (world.getBlockMetadata(x, y, z) != 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime >= this.nextNoise) {
                this.nextNoise = currentTime + 15000L + rand.nextInt(10000);
                world.playSound(x + 0.5, y + 0.5, z + 0.5, "thaumcraft:brain", 0.4F, world.rand.nextFloat() * 0.25F + 1.4F, false);
            }
        }
    }
}
