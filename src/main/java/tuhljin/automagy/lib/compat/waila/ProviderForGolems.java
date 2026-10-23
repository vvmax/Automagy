package tuhljin.automagy.lib.compat.waila;

import java.util.List;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thaumcraft.common.entities.golems.EntityGolemBase;
import tuhljin.automagy.entities.GolemLink;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.tiles.IGolemLinkableDevice;
import tuhljin.automagy.tiles.TileEntityInventarium;

public class ProviderForGolems implements IWailaEntityProvider {
    public Entity getWailaOverride(IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    public List<String> getWailaHead(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        if (entity instanceof EntityGolemBase) {
            EntityGolemBase golem = (EntityGolemBase)entity;
            GolemLink link = GolemLink.get(golem);
            String s = link.getLinkID();
            if (s != null && !s.isEmpty()) {
                WorldSpecificCoordinates coord = GolemLink.getCoordinatesFromLinkString(s);
                if (coord != null) {
                    World world = accessor.getPlayer().worldObj;
                    if (coord.dim == world.provider.dimensionId && TjUtil.isChunkLoaded(world, coord.x, coord.z)) {
                        TileEntity te = world.getTileEntity(coord.x, coord.y, coord.z);
                        if (te != null && te instanceof IGolemLinkableDevice && GolemLink.golemLinkedTo(golem, (IGolemLinkableDevice)te)) {
                            String name = this.getNameOfBlock(world, coord.x, coord.y, coord.z);
                            if (name != null) {
                                name = EnumChatFormatting.DARK_AQUA + "[" + name + "]" + EnumChatFormatting.GRAY;
                                String xyz = coord.x + "," + coord.y + "," + coord.z;
                                currenttip.add(StatCollector.translateToLocalFormatted("wailaTip.golem.linkedBlock", name, xyz));
                            }
                        }

                        if (TileEntityInventarium.isGolemEnRoute(golem)) {
                            currenttip.add(StatCollector.translateToLocal("wailaTip.golem.taskFetch"));
                        }
                    }
                }
            }
        }

        return currenttip;
    }

    public List<String> getWailaTail(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    String getNameOfBlock(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block != null) {
            Item blockItem = block.getItem(world, x, y, z);
            String name;
            if (blockItem == null) {
                name = block.getUnlocalizedName();
            } else {
                ItemStack blockStack = new ItemStack(blockItem, 1, block.getDamageValue(world, x, y, z));
                name = blockItem.getUnlocalizedName(blockStack);
            }

            if (name != null && !name.isEmpty()) {
                name = StatCollector.translateToLocal(name + ".name");
                if (name.substring(0, 5).equals("tile.") && name.substring(name.lastIndexOf(46) + 1).equals("name")) {
                    name = name.substring(name.indexOf(46) + 1, name.length() - 5);
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                }

                return name;
            }
        }

        return null;
    }
}
