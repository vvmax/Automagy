package tuhljin.automagy.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.HashSet;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.config.ConfigBlocks;
import tuhljin.automagy.api.nethermind.INetherRune;
import tuhljin.automagy.api.nethermind.RuneCategory;
import tuhljin.automagy.lib.AutomagyConfig;

public class BlockRunedObsidian extends ModBlock implements INetherRune {
    public static final int NETHERRACK = -1;
    public static final int LAVA = -2;
    private IIcon[] secondaryIcons;

    public BlockRunedObsidian(String name) {
        super(name, ModBlocks.materialNiceStone);
        this.setResistance(2000.0F);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        this.secondaryIcons = new IIcon[5];
        this.secondaryIcons[0] = ir.registerIcon(this.textureName + "2");
        this.secondaryIcons[1] = ir.registerIcon(this.textureName + "3");
        this.secondaryIcons[2] = ir.registerIcon(this.textureName + "4");
        this.secondaryIcons[3] = ir.registerIcon(this.textureName + "5");
        this.secondaryIcons[4] = ir.registerIcon(this.textureName + "6");
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (meta == 15) {
            return ConfigBlocks.blockCosmeticSolid.getIcon(0, 1);
        } else {
            return meta >= 0 && meta <= this.secondaryIcons.length - 1 ? this.secondaryIcons[meta] : this.blockIcon;
        }
    }

    @Override
    public boolean isFireSource(World world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
        if (world.isRemote) {
            return 15;
        } else {
            HashSet<Integer> adjacentRunes = new HashSet<>();
            adjacentRunes.add(0);
            adjacentRunes.add(1);
            adjacentRunes.add(2);
            adjacentRunes.add(3);
            adjacentRunes.add(4);
            adjacentRunes.add(5);

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                Block block = world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
                if (block == ModBlocks.runedObsidian) {
                    int md = world.getBlockMetadata(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
                    if (adjacentRunes.contains(md)) {
                        adjacentRunes.remove(md);
                    }
                }
            }

            int size = adjacentRunes.size();
            if (size > 0) {
                int item = world.rand.nextInt(size);
                int i = 0;

                for (int n : adjacentRunes) {
                    if (i == item) {
                        return n;
                    }

                    i++;
                }
            }

            return world.rand.nextInt(6);
        }
    }

    public int getRandomBasicRuneType(Random rand) {
        int r = rand.nextInt(100) + 1;
        return r <= AutomagyConfig.nethermindBasicLavaChance ? -2 : -1;
    }

    @Override
    public int getRuneType(World world, int x, int y, int z) {
        return -1;
    }

    @Override
    public Block blockCreated(int runeType) {
        return runeType == -2 ? Blocks.lava : Blocks.netherrack;
    }

    @Override
    public int blockCreatedMetadata(int runeType) {
        return 0;
    }

    @Override
    public int ticksUntilCreated(int runeType) {
        return runeType == -2 ? AutomagyConfig.nethermindLavaSpeed : AutomagyConfig.nethermindRockSpeed;
    }

    @Override
    public AspectList essentiaCostPerCreation(int runeType) {
        return runeType == -2 ? AutomagyConfig.nethermindLavaCost : AutomagyConfig.nethermindRockCostBasic;
    }

    @Override
    public void activateRune(World world, int x, int y, int z, int runeType) {
    }

    @Override
    public void deactivateRune(World world, int x, int y, int z, int runeType) {
    }

    @Override
    public RuneCategory getRuneCategory(int runeType) {
        return null;
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
