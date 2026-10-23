package tuhljin.automagy.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tuhljin.automagy.lib.inventory.IItemMap;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.network.MessageItemStacksList;
import tuhljin.automagy.network.MessageItemStacksRequested;
import tuhljin.automagy.tiles.IChestForGui;
import tuhljin.automagy.tiles.IInventariumBook;
import tuhljin.automagy.tiles.IInventariumBookLinksMirror;
import tuhljin.automagy.tiles.TileEntityBoiler;
import tuhljin.automagy.tiles.TileEntityEagerChest;
import tuhljin.automagy.tiles.TileEntityEntitySignal;
import tuhljin.automagy.tiles.TileEntityGolemWorkbench;
import tuhljin.automagy.tiles.TileEntityGreedyChest;
import tuhljin.automagy.tiles.TileEntityHourglassMagic;
import tuhljin.automagy.tiles.TileEntityMawFinical;
import tuhljin.automagy.tiles.TileEntityTallyBase;
import tuhljin.automagy.tiles.TileEntityVisReader;

public class AutomagyGUIHandler implements IGuiHandler {
    public static boolean hideCrosshairs = false;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == 4) {
            return new ContainerEnchantedPaper(false, player);
        } else if (ID == 5) {
            return new ContainerEnchantedPaper(true, player);
        } else if (ID == 12) {
            return new ContainerAutoHandMirror(player.inventory, world);
        } else if (ID == 14) {
            return new ContainerPortableArcaneCrafting(player, x);
        } else {
            TileEntity te = world.getTileEntity(x, y, z);
            switch (ID) {
                case 0:
                    if (te instanceof TileEntityVisReader) {
                        return new ContainerVisReader(player.inventory, (TileEntityVisReader)te);
                    }
                    break;
                case 1:
                    if (te instanceof TileEntityHourglassMagic) {
                        return new ContainerHourglassMagic(player.inventory, (TileEntityHourglassMagic)te);
                    }
                    break;
                case 2:
                    if (te instanceof TileEntityGreedyChest) {
                        return new ContainerGreedyChest(player.inventory, (TileEntityGreedyChest)te);
                    }
                    break;
                case 3:
                    if (te instanceof TileEntityGreedyChest) {
                        return new ContainerGreedyChestConfig(player.inventory, (TileEntityGreedyChest)te);
                    }
                case 4:
                case 5:
                case 12:
                case 14:
                case 17:
                default:
                    break;
                case 6:
                    if (te instanceof TileEntityEagerChest) {
                        return new ContainerEagerChest(player.inventory, (TileEntityEagerChest)te);
                    }
                    break;
                case 7:
                    if (te instanceof TileEntityEagerChest) {
                        return new ContainerEagerChestConfig(player.inventory, (TileEntityEagerChest)te);
                    }
                    break;
                case 8:
                    if (te instanceof TileEntityMawFinical) {
                        return new ContainerFinicalMaw(player.inventory, (TileEntityMawFinical)te);
                    }
                    break;
                case 9:
                    if (te instanceof TileEntityEntitySignal) {
                        return new ContainerEntitySignal(player.inventory, (TileEntityEntitySignal)te);
                    }
                    break;
                case 10:
                    if (te instanceof TileEntityTallyBase) {
                        return new ContainerTallyBlock(player.inventory, (TileEntityTallyBase)te);
                    }
                    break;
                case 11:
                    if (te instanceof TileEntityGolemWorkbench) {
                        return new ContainerGolemWorkbench(player.inventory, (TileEntityGolemWorkbench)te);
                    }
                    break;
                case 13:
                    if (te instanceof IInventariumBook) {
                        IInventariumBook book = (IInventariumBook)te;
                        sendInventariumContents(book, player, world, x, y, z);
                        return new ContainerScribe(player.inventory, world, book, book.hasLocalItems(), book.canRequestItems());
                    }
                    break;
                case 15:
                    if (te instanceof TileEntityBoiler) {
                        return new ContainerBoiler(player.inventory, (TileEntityBoiler)te);
                    }
                    break;
                case 16:
                    if (te instanceof IChestForGui) {
                        return new ContainerChestBase(player.inventory, (IChestForGui)te);
                    }
                    break;
                case 18:
                    if (te instanceof IInventariumBookLinksMirror) {
                        return new ContainerReqTomeMirrorSettings(player.inventory, (IInventariumBookLinksMirror)te, false);
                    }
                    break;
                case 19:
                    if (te instanceof IInventariumBookLinksMirror) {
                        return new ContainerReqTomeMirrorSettings(player.inventory, (IInventariumBookLinksMirror)te, true);
                    }
            }

            return null;
        }
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == 4) {
            return new GUIEnchantedPaper(false, player);
        } else if (ID == 5) {
            return new GUIEnchantedPaper(true, player);
        } else if (ID == 12) {
            return new GUIAutoHandMirror(player.inventory, world);
        } else if (ID == 14) {
            return new GUIPortableArcaneCrafting(player, x);
        } else {
            TileEntity te = world.getTileEntity(x, y, z);
            switch (ID) {
                case 0:
                    if (te instanceof TileEntityVisReader) {
                        return new GUIVisReader(player.inventory, (TileEntityVisReader)te);
                    }
                    break;
                case 1:
                    if (te instanceof TileEntityHourglassMagic) {
                        return new GUIHourglassMagic(player.inventory, (TileEntityHourglassMagic)te);
                    }
                    break;
                case 2:
                    if (te instanceof TileEntityGreedyChest) {
                        return new GUIGreedyChest(player.inventory, (TileEntityGreedyChest)te);
                    }
                    break;
                case 3:
                    if (te instanceof TileEntityGreedyChest) {
                        return new GUIGreedyChestConfig(player.inventory, (TileEntityGreedyChest)te);
                    }
                case 4:
                case 5:
                case 12:
                case 14:
                case 17:
                default:
                    break;
                case 6:
                    if (te instanceof TileEntityEagerChest) {
                        return new GUIEagerChest(player.inventory, (TileEntityEagerChest)te);
                    }
                    break;
                case 7:
                    if (te instanceof TileEntityEagerChest) {
                        return new GUIEagerChestConfig(player.inventory, (TileEntityEagerChest)te);
                    }
                    break;
                case 8:
                    if (te instanceof TileEntityMawFinical) {
                        return new GUIFinicalMaw(player.inventory, (TileEntityMawFinical)te);
                    }
                    break;
                case 9:
                    if (te instanceof TileEntityEntitySignal) {
                        return new GUIEntitySignal(player.inventory, (TileEntityEntitySignal)te);
                    }
                    break;
                case 10:
                    if (te instanceof TileEntityTallyBase) {
                        return new GUITallyBlock(player.inventory, (TileEntityTallyBase)te);
                    }
                    break;
                case 11:
                    if (te instanceof TileEntityGolemWorkbench) {
                        return new GUIGolemWorkbench(player.inventory, (TileEntityGolemWorkbench)te);
                    }
                case 13:
                    if (te instanceof IInventariumBook) {
                        IInventariumBook book = (IInventariumBook)te;
                        return new GUIScribe(player.inventory, world, book, book.hasLocalItems(), book.canRequestItems());
                    }
                    break;
                case 15:
                    if (te instanceof TileEntityBoiler) {
                        return new GUIBoiler(player.inventory, (TileEntityBoiler)te);
                    }
                    break;
                case 16:
                    if (te instanceof IChestForGui) {
                        return new GUIChestBase(player.inventory, (IChestForGui)te);
                    }
                    break;
                case 18:
                    if (te instanceof IInventariumBookLinksMirror) {
                        return new GUIReqTomeMirrorSettings(player.inventory, (IInventariumBookLinksMirror)te, false);
                    }
                    break;
                case 19:
                    if (te instanceof IInventariumBookLinksMirror) {
                        return new GUIReqTomeMirrorSettings(player.inventory, (IInventariumBookLinksMirror)te, true);
                    }
            }

            return null;
        }
    }

    public static void sendInventariumContents(IInventariumBook book, EntityPlayer player, World world, int x, int y, int z) {
        boolean hasLocalItems = book.hasLocalItems();
        boolean canRequestItems = book.canRequestItems();
        IItemMap list = book.getInventariumItemList();
        WorldSpecificCoordinates link = book.getLinkCoord();
        MessageItemStacksList.sendToClient(player, world, link.x, link.y, link.z, list == null ? null : list.getItemStacks(), false, false);
        if (hasLocalItems) {
            list = book.getLocalItemList();
            MessageItemStacksList.sendToClient(player, world, x, y, z, list == null ? null : list.getItemStacks(), false, true);
        }

        if (canRequestItems) {
            IItemMap var11 = book.getInventariumRequestedItemList();
            MessageItemStacksRequested.sendToClient(player, world, link.x, link.y, link.z, var11 == null ? null : var11.getItemStacks(), false);
        }
    }
}
