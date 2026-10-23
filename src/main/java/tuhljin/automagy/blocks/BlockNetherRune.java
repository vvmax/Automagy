package tuhljin.automagy.blocks;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.AspectList;
import tuhljin.automagy.api.nethermind.INetherRune;
import tuhljin.automagy.api.nethermind.RuneCategory;
import tuhljin.automagy.lib.AutomagyConfig;

public class BlockNetherRune extends ModBlock implements INetherRune {
    private IIcon[] secondaryIconsDim;
    private IIcon[] secondaryIconsActive;

    public BlockNetherRune(String name) {
        super(name, ModBlocks.materialNiceStone, ItemBlockNetherRune.class);
        this.setResistance(2000.0F);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
        list.add(new ItemStack(item, 1, 2));
        list.add(new ItemStack(item, 1, 3));
        list.add(new ItemStack(item, 1, 4));
        list.add(new ItemStack(item, 1, 6));
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        this.secondaryIconsActive = new IIcon[7];
        this.secondaryIconsActive[0] = ir.registerIcon(this.textureName + "Rock");
        this.secondaryIconsActive[1] = ir.registerIcon(this.textureName + "Lava");
        this.secondaryIconsActive[2] = ir.registerIcon(this.textureName + "Crystal");
        this.secondaryIconsActive[3] = ir.registerIcon(this.textureName + "Light");
        this.secondaryIconsActive[4] = ir.registerIcon(this.textureName + "Soul");
        this.secondaryIconsActive[6] = ir.registerIcon(this.textureName + "Celerity");
        this.secondaryIconsDim = new IIcon[7];
        this.secondaryIconsDim[0] = ir.registerIcon(this.textureName + "RockDim");
        this.secondaryIconsDim[1] = ir.registerIcon(this.textureName + "LavaDim");
        this.secondaryIconsDim[2] = ir.registerIcon(this.textureName + "CrystalDim");
        this.secondaryIconsDim[3] = ir.registerIcon(this.textureName + "LightDim");
        this.secondaryIconsDim[4] = ir.registerIcon(this.textureName + "SoulDim");
        this.secondaryIconsDim[6] = this.secondaryIconsActive[6];
        this.blockIcon = this.secondaryIconsActive[0];
    }

    @Override
    public int damageDropped(int metadata) {
        return metadata > 7 ? metadata - 8 : metadata;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(IBlockAccess blockaccess, int x, int y, int z, int side) {
        int meta = blockaccess.getBlockMetadata(x, y, z);
        return meta > 7 ? this.secondaryIconsActive[meta - 8] : this.secondaryIconsDim[meta];
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (meta == 0) {
            return this.blockIcon;
        } else {
            return meta > 7 ? this.secondaryIconsActive[meta - 8] : this.secondaryIconsActive[meta];
        }
    }

    @Override
    public boolean isFireSource(World world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    @Override
    public int getRuneType(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        return meta > 7 ? meta - 8 : meta;
    }

    @Override
    public Block blockCreated(int runeType) {
        switch (runeType) {
            case 0:
                return Blocks.netherrack;
            case 1:
                return Blocks.lava;
            case 2:
                return Blocks.quartz_ore;
            case 3:
                return Blocks.glowstone;
            case 4:
                return Blocks.soul_sand;
            default:
                FMLLog.warning("[Automagy] BlockNetherRune blockCreated: Invalid runeType.");
                return null;
        }
    }

    @Override
    public int blockCreatedMetadata(int runeType) {
        return 0;
    }

    @Override
    public int ticksUntilCreated(int runeType) {
        switch (runeType) {
            case 0:
                return AutomagyConfig.nethermindRockSpeed;
            case 1:
                return AutomagyConfig.nethermindLavaSpeed;
            case 2:
                return AutomagyConfig.nethermindQuartzSpeed;
            case 3:
                return AutomagyConfig.nethermindGlowstoneSpeed;
            case 4:
                return AutomagyConfig.nethermindSoulSandSpeed;
            default:
                return -1;
        }
    }

    @Override
    public AspectList essentiaCostPerCreation(int runeType) {
        switch (runeType) {
            case 0:
                return AutomagyConfig.nethermindRockCost;
            case 1:
                return AutomagyConfig.nethermindLavaCost;
            case 2:
                return AutomagyConfig.nethermindQuartzCost;
            case 3:
                return AutomagyConfig.nethermindGlowstoneCost;
            case 4:
                return AutomagyConfig.nethermindSoulSandCost;
            default:
                FMLLog.warning("[Automagy] BlockNetherRune essentiaCostPerBlockCreated: Invalid runeType.");
                return null;
        }
    }

    @Override
    public void activateRune(World world, int x, int y, int z, int runeType) {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta < 8) {
            world.setBlockMetadataWithNotify(x, y, z, meta + 8, 3);
        }
    }

    @Override
    public void deactivateRune(World world, int x, int y, int z, int runeType) {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta > 7) {
            world.setBlockMetadataWithNotify(x, y, z, meta - 8, 3);
        }
    }

    @Override
    public RuneCategory getRuneCategory(int runeType) {
        return runeType == 6 ? RuneCategory.speed : null;
    }

    @Override
    public MapColor getMapColor(int p_149728_1_) {
        return MapColor.obsidianColor;
    }

    @Override
    public boolean isBeaconBase(IBlockAccess worldObj, int x, int y, int z, int beaconX, int beaconY, int beaconZ) {
        return y + 1 == beaconY;
    }
}
