package tuhljin.automagy.items;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.tiles.TileEntityTallyBase;

public class ItemTallyLens extends ModSubtypedItem {
    protected IIcon iconWorld;
    protected IIcon iconDrops;

    public ItemTallyLens(String name) {
        super(name, 3);
    }

    @Override
    public void registerIcons(IIconRegister ir) {
        super.registerIcons(ir);
        String t = this.getIconString();
        this.iconWorld = ir.registerIcon(t + "World");
        this.iconDrops = ir.registerIcon(t + "Drops");
    }

    @Override
    public IIcon getIconFromDamage(int d) {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        switch (d) {
            case 1:
                return this.iconWorld;
            case 2:
                return this.iconDrops;
            default:
                return this.itemIcon;
        }
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
        if (!world.isRemote) {
            int d = stack.getMetadata();
            if (d == 1 || d == 2) {
                Block block = world.getBlock(x, y, z);
                if (block == ModBlocks.tallyBlock) {
                    TileEntityTallyBase te = (TileEntityTallyBase)world.getTileEntity(x, y, z);
                    ItemStack filterStack = te.silentlyExtractItem();
                    boolean requireAllMatches = te.requireAllMatches;
                    Block newBlock = d == 1 ? ModBlocks.tallyBlockWorld : ModBlocks.tallyBlockDrops;
                    int md = world.getBlockMetadata(x, y, z);
                    if (md > 5) {
                        md -= 6;
                    }

                    world.setBlock(x, y, z, newBlock, md, 3);
                    if (filterStack != null) {
                        te = (TileEntityTallyBase)world.getTileEntity(x, y, z);
                        te.setInventorySlotContents(0, filterStack);
                    }

                    if (te.requireAllMatches != requireAllMatches) {
                        te.requireAllMatches = requireAllMatches;
                        te.markDirty();
                    }

                    if (!player.capabilities.isCreativeMode) {
                        stack.stackSize--;
                    }

                    world.playSoundAtEntity(player, "thaumcraft:upgrade", 0.5F, 1.0F);
                }
            }
        }

        return false;
    }
}
