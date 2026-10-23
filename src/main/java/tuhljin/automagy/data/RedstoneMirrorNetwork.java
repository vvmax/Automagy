package tuhljin.automagy.data;

import cpw.mods.fml.common.FMLLog;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityRedcrystalMerc;

public class RedstoneMirrorNetwork extends WorldSavedData {
    protected Map<BlockCoord, Integer> highestPower = new HashMap<>();
    protected Map<BlockCoord, HashMap<BlockCoord, Integer>> powerLevels = new HashMap<>();
    private static final int NEEDS_POWER_RECALC = -1;
    private int dimensionID = -9909;

    public RedstoneMirrorNetwork(String dataMapName) {
        super(dataMapName);
    }

    public int getSignalGoingIn(BlockCoord mirrorPos) {
        Integer strength = this.highestPower.get(mirrorPos);
        return strength == null ? 0 : (strength == -1 ? this.calculateSignal(mirrorPos) : strength);
    }

    public int getSignalGoingIn(int x, int y, int z) {
        return this.getSignalGoingIn(new BlockCoord(x, y, z));
    }

    public void contributeToSignalInto(int x, int y, int z, TileEntity te, int strength) {
        this.dimensionID = te.getWorld().provider.dimensionId;
        BlockCoord mirrorPos = new BlockCoord(x, y, z);
        Integer currentStrength = this.highestPower.get(mirrorPos);
        boolean newHigh;
        if (currentStrength != null && currentStrength >= strength) {
            newHigh = false;
        } else {
            this.highestPower.put(mirrorPos, strength);
            newHigh = true;
        }

        HashMap<BlockCoord, Integer> submap = this.powerLevels.get(mirrorPos);
        if (submap == null) {
            submap = new HashMap<>();
            this.powerLevels.put(mirrorPos, submap);
            submap.put(new BlockCoord(te), strength);
        } else {
            BlockCoord tePos = new BlockCoord(te);
            Integer prev = null;
            if (!newHigh) {
                prev = submap.get(tePos);
            }

            if (prev != null) {
                if (prev != strength) {
                    submap.put(tePos, strength);
                    if (prev == this.highestPower.get(mirrorPos)) {
                        this.highestPower.put(mirrorPos, -1);
                    }
                }
            } else {
                submap.put(tePos, strength);
            }
        }

        this.markDirty();
    }

    private int calculateSignal(BlockCoord mirrorPos) {
        int highest = 0;
        HashMap<BlockCoord, Integer> map = this.powerLevels.get(mirrorPos);
        if (map != null) {
            for (Entry<BlockCoord, Integer> entry : map.entrySet()) {
                Integer strength = entry.getValue();
                highest = Math.max(highest, strength == null ? 0 : strength);
            }

            this.highestPower.put(mirrorPos, highest);
        }

        return highest;
    }

    public void removeSignal(int x, int y, int z) {
        BlockCoord mirrorPos = new BlockCoord(x, y, z);
        if (this.highestPower.containsKey(mirrorPos)) {
            this.highestPower.remove(mirrorPos);
            this.powerLevels.remove(mirrorPos);
            this.markDirty();
        }
    }

    public void removeContributor(int x, int y, int z, BlockCoord tePos) {
        this.removeContributor(new BlockCoord(x, y, z), tePos);
    }

    public void removeContributor(BlockCoord mirrorPos, BlockCoord tePos) {
        HashMap<BlockCoord, Integer> submap = this.powerLevels.get(mirrorPos);
        if (submap != null) {
            Integer prevStrength = submap.get(tePos);
            if (prevStrength != null) {
                submap.remove(tePos);
                this.markDirty();
                if (prevStrength >= this.getSignalGoingIn(mirrorPos)) {
                    this.highestPower.put(mirrorPos, -1);
                }
            }
        }
    }

    public void removeContributor(int x, int y, int z, TileEntity te) {
        this.removeContributor(x, y, z, new BlockCoord(te));
    }

    public void cleanEntries(World world, int x, int y, int z) {
        this.cleanEntries(world, new BlockCoord(x, y, z));
    }

    public void cleanEntries(World world, BlockCoord mirrorPos) {
        HashMap<BlockCoord, Integer> submap = this.powerLevels.get(mirrorPos);
        if (submap != null) {
            boolean anyInvalid = false;

            for (Entry<BlockCoord, Integer> entry : submap.entrySet()) {
                BlockCoord pos = entry.getKey();
                TileEntity te = world.getTileEntity(pos.x, pos.y, pos.z);
                boolean invalid = false;
                if (te == null) {
                    if (TjUtil.isChunkLoaded(world, mirrorPos.x, mirrorPos.z)) {
                        invalid = true;
                    }
                } else if (!(te instanceof TileEntityRedcrystalMerc)) {
                    invalid = true;
                } else {
                    TileEntityRedcrystalMerc ter = (TileEntityRedcrystalMerc)te;
                    if (ter.mirrorDim != world.provider.dimensionId || ter.mirrorX != mirrorPos.x || ter.mirrorY != mirrorPos.y || ter.mirrorZ != mirrorPos.z) {
                        invalid = true;
                    }
                }

                if (invalid) {
                    this.removeContributor(mirrorPos, pos);
                    anyInvalid = true;
                }
            }

            if (anyInvalid) {
                this.highestPower.put(mirrorPos, -1);
            }
        }
    }

    public void cleanEntries() {
        World world = null;

        try {
            world = MinecraftServer.getServer().worldServerForDimension(this.dimensionID);
        } catch (Exception var4) {
        }

        if (world != null) {
            for (Entry<BlockCoord, HashMap<BlockCoord, Integer>> entry : this.powerLevels.entrySet()) {
                this.cleanEntries(world, entry.getKey());
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        this.highestPower.clear();
        this.powerLevels.clear();
        int errors = 0;
        int suberrors = 0;
        NBTTagList mainList = nbttagcompound.getTagList("MirrorsRedstoneData", 10);
        int count = mainList.tagCount();

        for (int i = 0; i < count; i++) {
            NBTTagCompound compound = mainList.getCompoundTagAt(i);
            int[] arr = compound.getIntArray("mirrorPos");
            if (arr.length != 3) {
                errors++;
            } else {
                NBTTagList sublist = compound.getTagList("links", 10);
                int subcount = sublist.tagCount();
                if (subcount > 0) {
                    BlockCoord mirrorPos = new BlockCoord(arr);
                    this.highestPower.put(mirrorPos, -1);
                    HashMap<BlockCoord, Integer> submap = new HashMap<>();
                    this.powerLevels.put(mirrorPos, submap);

                    for (int j = 0; j < subcount; j++) {
                        NBTTagCompound tag = sublist.getCompoundTagAt(j);
                        int[] arr2 = tag.getIntArray("pos");
                        if (arr2.length != 3) {
                            suberrors++;
                        } else {
                            int strength = tag.getInteger("strength");
                            submap.put(new BlockCoord(arr2), strength);
                        }
                    }
                }
            }
        }

        this.dimensionID = nbttagcompound.getInteger("MirrorsRedstoneDimension");
        if (errors > 0 || suberrors > 0) {
            FMLLog.warning(
                "[Automagy] RedstoneMirrorNetwork: Malformed save data detected. Failed to load "
                    + errors
                    + " redstone strength entries and "
                    + suberrors
                    + " sub-entries."
            );
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        NBTTagList mainList = new NBTTagList();

        for (Entry<BlockCoord, HashMap<BlockCoord, Integer>> entry : this.powerLevels.entrySet()) {
            BlockCoord mirrorPos = entry.getKey();
            HashMap<BlockCoord, Integer> submap = entry.getValue();
            if (submap.size() > 0) {
                NBTTagCompound compound = new NBTTagCompound();
                mainList.appendTag(compound);
                compound.setIntArray("mirrorPos", new int[]{mirrorPos.x, mirrorPos.y, mirrorPos.z});
                NBTTagList sublist = new NBTTagList();
                compound.setTag("links", sublist);

                for (Entry<BlockCoord, Integer> subentry : submap.entrySet()) {
                    Integer strength = subentry.getValue();
                    if (strength != null && strength > 0) {
                        BlockCoord pos = subentry.getKey();
                        NBTTagCompound tag = new NBTTagCompound();
                        sublist.appendTag(tag);
                        tag.setIntArray("pos", new int[]{pos.x, pos.y, pos.z});
                        tag.setInteger("strength", strength);
                    }
                }
            }
        }

        nbttagcompound.setTag("MirrorsRedstoneData", mainList);
        nbttagcompound.setInteger("MirrorsRedstoneDimension", this.dimensionID);
    }

    public static RedstoneMirrorNetwork getData(int dimensionID) {
        World worldSave = MinecraftServer.getServer().worldServerForDimension(dimensionID);
        RedstoneMirrorNetwork data = (RedstoneMirrorNetwork)worldSave.loadItemData(RedstoneMirrorNetwork.class, "AutomagyUniversalRedstone");
        if (data == null) {
            data = new RedstoneMirrorNetwork("AutomagyUniversalRedstone");
            data.dimensionID = dimensionID;
            worldSave.setItemData("AutomagyUniversalRedstone", data);
        }

        return data;
    }

    public static RedstoneMirrorNetwork getData(World world) {
        return getData(world.provider.dimensionId);
    }
}
