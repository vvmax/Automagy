package tuhljin.automagy.entities;

import cpw.mods.fml.common.FMLLog;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.apache.logging.log4j.Level;
import thaumcraft.common.entities.golems.EntityGolemBase;
import tuhljin.automagy.lib.struct.WorldSpecificCoordinates;
import tuhljin.automagy.tiles.IGolemLinkableDevice;

public class GolemLink implements IExtendedEntityProperties {
    public static final String EXT_PROP_NAME = "AutomagyGolemLink";
    public static final int WATCHER_ID = 25;
    public final EntityGolemBase golem;
    private static Map<String, WorldSpecificCoordinates> coordCache = new HashMap<>();

    public GolemLink(EntityGolemBase golem, String linkID) {
        this.golem = golem;
        golem.getDataWatcher().addObject(25, linkID);
    }

    public GolemLink(EntityGolemBase golem) {
        this(golem, "");
    }

    @Override
    public void init(Entity entity, World world) {
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound properties = new NBTTagCompound();
        properties.setString("linkID", this.getLinkID());
        compound.setTag("AutomagyGolemLink", properties);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        NBTTagCompound properties = (NBTTagCompound)compound.getTag("AutomagyGolemLink");
        if (properties != null) {
            this.setLinkID(properties.getString("linkID"));
        }
    }

    public String getLinkID() {
        return this.golem.getDataWatcher().getWatchableObjectString(25);
    }

    public void setLinkID(String linkID) {
        this.golem.getDataWatcher().updateObject(25, linkID);
    }

    public static GolemLink get(EntityGolemBase golem) {
        return (GolemLink)golem.getExtendedProperties("AutomagyGolemLink");
    }

    public static boolean golemLinkedTo(EntityGolemBase golem, IGolemLinkableDevice device) {
        String deviceLinkID = device.getGolemLinkID();
        if (deviceLinkID == null) {
            return false;
        } else {
            GolemLink link = get(golem);
            return link != null && deviceLinkID.equals(link.getLinkID());
        }
    }

    public static IGolemLinkableDevice getLinkedDevice(EntityGolemBase golem) {
        WorldSpecificCoordinates coord = getCoordinatesFromLinkString(get(golem).getLinkID());
        if (coord != null && coord.dim == golem.worldObj.provider.dimensionId) {
            TileEntity te = golem.worldObj.getTileEntity(coord.x, coord.y, coord.z);
            if (te instanceof IGolemLinkableDevice) {
                IGolemLinkableDevice device = (IGolemLinkableDevice)te;
                if (golemLinkedTo(golem, device)) {
                    return device;
                }
            }
        }

        return null;
    }

    public static GolemLink establishLink(EntityGolemBase golem, IGolemLinkableDevice device) {
        String linkID = device.getGolemLinkID();
        if (linkID == null) {
            FMLLog.log(Level.ERROR, "[Automagy] Failed to register golem extended properties. Linking golem to device failed!");
            return null;
        } else {
            GolemLink link = get(golem);
            if (link == null) {
                link = new GolemLink(golem, linkID);
                golem.registerExtendedProperties("AutomagyGolemLink", link);
            } else {
                link.setLinkID(linkID);
            }

            return link;
        }
    }

    public static GolemLink register(EntityGolemBase golem) {
        GolemLink link = new GolemLink(golem);
        golem.registerExtendedProperties("AutomagyGolemLink", link);
        return link;
    }

    public static WorldSpecificCoordinates getCoordinatesFromLinkString(String link) {
        if (link == null || link.isEmpty()) {
            return null;
        } else if (coordCache.containsKey(link)) {
            return coordCache.get(link);
        } else {
            String s = link.substring(link.lastIndexOf(58) + 1);
            String[] sCoord = s.split(",", 4);
            if (sCoord.length != 4) {
                return null;
            } else {
                int dim = Integer.parseInt(sCoord[0]);
                int x = Integer.parseInt(sCoord[1]);
                int y = Integer.parseInt(sCoord[2]);
                int z = Integer.parseInt(sCoord[3]);
                WorldSpecificCoordinates coord = new WorldSpecificCoordinates(dim, x, y, z);
                coordCache.put(link, coord);
                return coord;
            }
        }
    }
}
