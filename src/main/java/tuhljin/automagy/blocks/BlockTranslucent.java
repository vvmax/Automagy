package tuhljin.automagy.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.common.blocks.BlockCosmeticOpaque;
import thaumcraft.common.config.ConfigBlocks;

public class BlockTranslucent extends BlockCosmeticOpaque {
    public BlockTranslucent(String name) {
        this.setUnlocalizedName(name);
        this.setTextureName("thaumcraft:amberblock");
        if (ModBlocks.tab != null) {
            this.setCreativeTab(ModBlocks.tab);
        }

        GameRegistry.registerBlock(this, ModSubtypedItemBlock.class, name);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
    }

    @Override
    public int damageDropped(int d) {
        return d;
    }

    @Override
    public int getLightValue(IBlockAccess blockaccess, int x, int y, int z) {
        int md = blockaccess.getBlockMetadata(x, y, z);
        return md < 2 ? 15 : 0;
    }

    @Override
    public MapColor getMapColor(int p_149728_1_) {
        return MapColor.redColor;
    }

    @Override
    public int getRenderType() {
        return ConfigBlocks.blockCosmeticOpaqueRI;
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public int getLightOpacity() {
        return 3;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isNormalCube() {
        return true;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        Block block = world.getBlock(x, y, z);
        if (world.getBlockMetadata(x, y, z)
            != world.getBlockMetadata(x - Facing.offsetsXForSide[side], y - Facing.offsetsYForSide[side], z - Facing.offsetsZForSide[side])) {
            return true;
        } else {
            return block != this && block != ConfigBlocks.blockCosmeticOpaque ? super.shouldSideBeRendered(world, x, y, z, side) : false;
        }
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return null;
    }

    @Override
    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
        if (entity instanceof EntityWither) {
            return this != Blocks.bedrock && this != Blocks.end_portal && this != Blocks.end_portal_frame && this != Blocks.command_block;
        } else {
            return !(entity instanceof EntityDragon) ? true : this != Blocks.obsidian && this != Blocks.end_stone && this != Blocks.bedrock;
        }
    }

    @Override
    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
        world.setBlockToAir(x, y, z);
        this.onBlockDestroyedByExplosion(world, x, y, z, explosion);
    }

    @Override
    public float getBlockHardness(World p_149712_1_, int p_149712_2_, int p_149712_3_, int p_149712_4_) {
        return this.blockHardness;
    }

    @Override
    public float getExplosionResistance(Entity p_149638_1_) {
        return this.blockResistance / 5.0F;
    }
}
