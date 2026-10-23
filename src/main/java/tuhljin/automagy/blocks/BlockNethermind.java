package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import tuhljin.automagy.lib.ThaumcraftExtension;
import tuhljin.automagy.renderers.IBlockGlowOverlay;
import tuhljin.automagy.renderers.RenderBlockGlowOverlay;
import tuhljin.automagy.tiles.TileEntityDimensionLure;
import tuhljin.automagy.tiles.TileEntityNethermind;

public class BlockNethermind extends ModBlockContainer implements IBlockGlowOverlay {
    public static AspectList activationVisCost = new AspectList().add(Aspect.FIRE, 75).add(Aspect.ORDER, 25);
    public static AspectList defaultRockCostBasic = new AspectList().add(Aspect.FIRE, 2).add(Aspect.EARTH, 4);
    public static AspectList defaultRockCost = new AspectList().add(Aspect.FIRE, 1).add(Aspect.EARTH, 2);
    public static AspectList defaultLavaCost = new AspectList().add(Aspect.FIRE, 6).add(Aspect.EARTH, 2);
    public static AspectList defaultQuartzCost = new AspectList().add(Aspect.CRYSTAL, 3).add(Aspect.EARTH, 1);
    public static AspectList defaultGlowstoneCost = new AspectList().add(Aspect.LIGHT, 10).add(Aspect.SENSES, 3);
    public static AspectList defaultSoulSandCost = new AspectList().add(Aspect.EARTH, 1).add(Aspect.TRAP, 1).add(Aspect.SOUL, 1);
    public static int defaultRockSpeed = 60;
    public static int defaultLavaSpeed = 90;
    public static int defaultQuartzSpeed = 100;
    public static int defaultGlowstoneSpeed = 100;
    public static int defaultSoulSandSpeed = 100;
    public static int defaultLavaChance = 15;
    public static int defaultSpawnerSpeed = 360;
    public static AspectList defaultZombiePigmanCost = new AspectList().add(Aspect.FLESH, 2).add(Aspect.FIRE, 2);
    public static AspectList defaultZombiePigmanCost2 = new AspectList().add(Aspect.HUNGER, 15).add(Aspect.MIND, 9).add(Aspect.GREED, 3);
    public static int defaultZombiePigmanUses = 15;
    public static AspectList defaultBlazeCost = new AspectList().add(Aspect.FIRE, 4).add(Aspect.MAGIC, 2);
    public static AspectList defaultBlazeCost2 = new AspectList().add(Aspect.HUNGER, 10).add(Aspect.LIFE, 5);
    public static int defaultBlazeUses = 10;
    public static AspectList defaultGhastCost = new AspectList().add(Aspect.UNDEAD, 1).add(Aspect.SOUL, 2);
    public static AspectList defaultGhastCost2 = new AspectList().add(Aspect.HUNGER, 6).add(Aspect.EXCHANGE, 1).add(Aspect.SOUL, 4).add(Aspect.WATER, 4);
    public static int defaultGhastUses = 6;
    public static AspectList defaultSkeletonCost = new AspectList().add(Aspect.DEATH, 1).add(Aspect.FLESH, 1);
    public static AspectList defaultSkeletonCost2 = new AspectList().add(Aspect.HUNGER, 20).add(Aspect.WEAPON, 5).add(Aspect.SENSES, 5);
    public static int defaultSkeletonUses = 20;
    public static AspectList defaultSlimeCost = new AspectList().add(Aspect.SLIME, 1).add(Aspect.FIRE, 2);
    public static AspectList defaultSlimeCost2 = new AspectList().add(Aspect.HUNGER, 16).add(Aspect.SLIME, 1);
    public static int defaultSlimeUses = 8;
    public static AspectList defaultBatCost = new AspectList().add(Aspect.BEAST, 1).add(Aspect.FIRE, 1).add(Aspect.ENTROPY, 1);
    public static AspectList defaultBatCost2 = new AspectList().add(Aspect.HUNGER, 10).add(Aspect.MAN, 1);
    public static int defaultBatUses = 10;
    public static AspectList defaultWispCost = new AspectList().add(Aspect.AURA, 1).add(Aspect.FIRE, 2).add(Aspect.AIR, 1);
    public static AspectList defaultWispCost2 = new AspectList().add(Aspect.HUNGER, 8).add(Aspect.AURA, 3);
    public static int defaultWispUses = 8;
    private IIcon iconBottom;
    private IIcon iconSide;
    private IIcon iconTopGlow;
    private IIcon iconBottomGlow;

    public BlockNethermind(String name) {
        super(name, ModBlocks.materialNiceStone, ItemBlockRarityRare.class);
        this.setResistance(2000.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityNethermind();
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.iconBottom = ir.registerIcon(this.textureName + "Bottom");
        this.iconSide = ir.registerIcon(this.textureName + "Side");
        this.iconTopGlow = ir.registerIcon(this.textureName + "TopGlow");
        this.iconBottomGlow = ir.registerIcon(this.textureName + "BottomGlow");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == 0) {
            return this.iconBottom;
        } else {
            return side == 1 ? this.blockIcon : this.iconSide;
        }
    }

    @Override
    public int getRenderType() {
        return RenderBlockGlowOverlay.renderID;
    }

    @Override
    public IIcon getOverlayTexture(IBlockAccess world, int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta == 1) {
            if (side == 0) {
                return this.iconBottomGlow;
            }

            if (side == 1) {
                return this.iconTopGlow;
            }
        }

        return null;
    }

    @Override
    public IIcon getOverlayInvTexture(int side, int meta) {
        if (side == 0) {
            return this.iconBottomGlow;
        } else {
            return side == 1 ? this.iconTopGlow : null;
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!ThaumcraftExtension.playerHasWandEquipped(player)) {
            return false;
        } else {
            if (!world.isRemote) {
                TileEntityNethermind te = null;

                try {
                    te = (TileEntityNethermind)world.getTileEntity(x, y, z);
                } catch (Exception var12) {
                }

                if (te != null) {
                    if (te.isEnabled()) {
                        if (te.tryDisable()) {
                        }
                    } else if ((
                            player.capabilities.isCreativeMode
                                || ThaumcraftApiHelper.consumeVisFromWandCrafting(player.getCurrentEquippedItem(), player, activationVisCost, false)
                        )
                        && te.tryEnable(player)
                        && !player.capabilities.isCreativeMode) {
                        ThaumcraftApiHelper.consumeVisFromWandCrafting(player.getCurrentEquippedItem(), player, activationVisCost, true);
                    }
                }
            }

            return true;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        if (rand.nextInt(4) != 0) {
            TileEntityNethermind te = null;

            try {
                te = (TileEntityNethermind)world.getTileEntity(x, y, z);
            } catch (Exception var9) {
            }

            if (te != null && te.isConjuring()) {
                int r = world.rand.nextInt(20);
                String p;
                if (r < 5) {
                    p = "smoke";
                } else if (r < 8) {
                    p = "largesmoke";
                } else if (r < 16) {
                    p = "lava";
                } else {
                    p = "flame";
                }

                world.spawnParticle(p, x + rand.nextFloat() * 0.5 + 0.25, y + 1.1, z + rand.nextFloat() * 0.5 + 0.25, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        TileEntityNethermind te = null;

        try {
            te = (TileEntityNethermind)world.getTileEntity(x, y, z);
        } catch (Exception var9) {
        }

        if (te != null) {
            te.deactivateRune(te.currentRuneX, te.currentRuneY, te.currentRuneZ);
            TileEntityDimensionLure lure = te.getDimensionLure();
            if (lure != null) {
                lure.clearActiveRune();
            }
        }

        super.breakBlock(world, x, y, z, block, metadata);
    }

    @Override
    public MapColor getMapColor(int p_149728_1_) {
        return MapColor.obsidianColor;
    }
}
