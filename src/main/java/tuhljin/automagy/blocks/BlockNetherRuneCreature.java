package tuhljin.automagy.blocks;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.entities.monster.EntityFireBat;
import tuhljin.automagy.api.nethermind.INetherRuneCreature;
import tuhljin.automagy.api.nethermind.RuneCategory;
import tuhljin.automagy.entities.EntityWispNether;
import tuhljin.automagy.lib.AutomagyConfig;

public class BlockNetherRuneCreature extends ModBlock implements INetherRuneCreature {
    public static final int BLAZE_NORMAL_ID = 1;
    public static final int BLAZE_EXTRA_ID = 21;
    public static int BLAZE_EXTRA_CHANCE = 10;
    public static int INSTABILITY_BLAZE_EXTRA = 200;
    public static int INSTABILITY_GHAST = 200;
    public static int INSTABILITY_SKELETON = 50;
    public static int INSTABILITY_WISP = 375;
    public static int ZOMBIE_ANGRY_CHANCE = 20;
    private IIcon[] secondaryIconsDim;
    private IIcon[] secondaryIconsActive;
    private static Method becomeAngryAt = null;

    public BlockNetherRuneCreature(String name) {
        super(name, ModBlocks.materialNiceStone, ItemBlockNetherRune.class);
        this.setResistance(2000.0F);
        this.setTextureName("Automagy:blockNetherRune");
    }

    @Override
    public MapColor getMapColor(int p_149728_1_) {
        return MapColor.obsidianColor;
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
        list.add(new ItemStack(item, 1, 2));
        list.add(new ItemStack(item, 1, 3));
        list.add(new ItemStack(item, 1, 4));
        list.add(new ItemStack(item, 1, 5));
        list.add(new ItemStack(item, 1, 6));
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        this.secondaryIconsActive = new IIcon[8];
        this.secondaryIconsActive[0] = ir.registerIcon(this.textureName + "Zombie");
        this.secondaryIconsActive[1] = ir.registerIcon(this.textureName + "Blaze");
        this.secondaryIconsActive[2] = ir.registerIcon(this.textureName + "Ghast");
        this.secondaryIconsActive[3] = ir.registerIcon(this.textureName + "Skull");
        this.secondaryIconsActive[4] = ir.registerIcon(this.textureName + "Slime");
        this.secondaryIconsActive[5] = ir.registerIcon(this.textureName + "Bat");
        this.secondaryIconsActive[6] = ir.registerIcon(this.textureName + "Wisp");
        this.secondaryIconsDim = new IIcon[8];
        this.secondaryIconsDim[0] = ir.registerIcon(this.textureName + "ZombieDim");
        this.secondaryIconsDim[1] = ir.registerIcon(this.textureName + "BlazeDim");
        this.secondaryIconsDim[2] = ir.registerIcon(this.textureName + "GhastDim");
        this.secondaryIconsDim[3] = ir.registerIcon(this.textureName + "SkullDim");
        this.secondaryIconsDim[4] = ir.registerIcon(this.textureName + "SlimeDim");
        this.secondaryIconsDim[5] = ir.registerIcon(this.textureName + "BatDim");
        this.secondaryIconsDim[6] = ir.registerIcon(this.textureName + "WispDim");
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
    public RuneCategory getRuneCategory(int runeType) {
        return RuneCategory.creature;
    }

    @Override
    public int getRuneType(World world, int x, int y, int z) {
        int type = this.getBaseRuneType(world, x, y, z);
        return type == 1 && world.rand.nextInt(BLAZE_EXTRA_CHANCE) == 0 ? 21 : type;
    }

    private int getBaseRuneType(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        return meta > 7 ? meta - 8 : meta;
    }

    @Override
    public Entity entityCreated(int runeType, World world) {
        switch (runeType) {
            case 0:
                return new EntityPigZombie(world);
            case 1:
            case 21:
                return new EntityBlaze(world);
            case 2:
                return new EntityGhast(world);
            case 3:
                return new EntitySkeleton(world);
            case 4:
                return new EntityMagmaCube(world);
            case 5:
                return new EntityFireBat(world);
            case 6:
                return new EntityWispNether(world);
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            default:
                FMLLog.warning("[Automagy] BlockNetherRuneCreature entityCreated: Invalid runeType.");
                return null;
        }
    }

    @Override
    public int ticksUntilCreated(int runeType) {
        return AutomagyConfig.nethermindSpawnerSpeed;
    }

    @Override
    public AspectList essentiaCostPerCreation(int runeType) {
        switch (runeType) {
            case 0:
                return AutomagyConfig.nethermindZombiePigmanCost;
            case 1:
            case 21:
                return AutomagyConfig.nethermindBlazeCost;
            case 2:
                return AutomagyConfig.nethermindGhastCost;
            case 3:
                return AutomagyConfig.nethermindSkeletonCost;
            case 4:
                return AutomagyConfig.nethermindSlimeCost;
            case 5:
                return AutomagyConfig.nethermindBatCost;
            case 6:
                return AutomagyConfig.nethermindWispCost;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            default:
                FMLLog.warning("[Automagy] BlockNetherRuneCreature essentiaCostPerBlockCreated: Invalid runeType.");
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
    public int getInstability(int runeType) {
        switch (runeType) {
            case 2:
                return INSTABILITY_GHAST;
            case 3:
                return INSTABILITY_SKELETON;
            case 6:
                return INSTABILITY_WISP;
            case 21:
                return INSTABILITY_BLAZE_EXTRA;
            default:
                return 0;
        }
    }

    @Override
    public AspectList secondaryEssentiaCost(int runeType) {
        switch (runeType) {
            case 0:
                return AutomagyConfig.dimLureZombiePigmanCost;
            case 1:
            case 21:
                return AutomagyConfig.dimLureBlazeCost;
            case 2:
                return AutomagyConfig.dimLureGhastCost;
            case 3:
                return AutomagyConfig.dimLureSkeletonCost;
            case 4:
                return AutomagyConfig.dimLureSlimeCost;
            case 5:
                return AutomagyConfig.dimLureBatCost;
            case 6:
                return AutomagyConfig.dimLureWispCost;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            default:
                FMLLog.warning("[Automagy] BlockNetherRuneCreature secondaryEssentiaCost: Invalid runeType.");
                return null;
        }
    }

    @Override
    public int secondaryEssentiaCostUses(int runeType) {
        switch (runeType) {
            case 0:
                return AutomagyConfig.dimLureZombiePigmanUses;
            case 1:
            case 21:
                return AutomagyConfig.dimLureBlazeUses;
            case 2:
                return AutomagyConfig.dimLureGhastUses;
            case 3:
                return AutomagyConfig.dimLureSkeletonUses;
            case 4:
                return AutomagyConfig.dimLureSlimeUses;
            case 5:
                return AutomagyConfig.dimLureBatUses;
            case 6:
                return AutomagyConfig.dimLureWispUses;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            default:
                FMLLog.warning("[Automagy] BlockNetherRuneCreature secondaryEssentiaCostUses: Invalid runeType.");
                return 2;
        }
    }

    @Override
    public int getSpawnCount(int runeType) {
        return runeType == 21 ? 2 : 1;
    }

    @Override
    public String getEntityIdentifier(int runeType) {
        switch (runeType) {
            case 0:
                return "PigZombie";
            case 1:
            case 21:
                return "Blaze";
            case 2:
                return "Ghast";
            case 3:
                return "Skeleton";
            case 4:
                return "LavaSlime";
            case 5:
                return "Firebat";
            case 6:
                return "WispNether";
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            default:
                FMLLog.warning("[Automagy] BlockNetherRuneCreature getMobIdentifier: Invalid runeType.");
                return "unknown";
        }
    }

    @Override
    public void onEntitySpawned(int runeType, Entity entity) {
        if (runeType == 0) {
            if (entity instanceof EntityPigZombie && entity.worldObj.rand.nextInt(ZOMBIE_ANGRY_CHANCE) == 0) {
                this.makePigZombieAngry((EntityPigZombie)entity);
            }
        } else if (runeType == 3 && entity.worldObj.provider.dimensionId != -1 && entity instanceof EntitySkeleton) {
            EntitySkeleton skeleton = (EntitySkeleton)entity;
            if (skeleton.getSkeletonType() != 1 && skeleton.getRNG().nextInt(5) > 0) {
                removeArmor(skeleton);
                skeleton.setSkeletonType(1);
                skeleton.setCurrentItemOrArmor(0, new ItemStack(Items.stone_sword));
                skeleton.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(4.0);
                skeleton.setCombatTask();
            }
        }
    }

    @Override
    public int requiredLureDistance(World world, int x, int y, int z) {
        return this.getBaseRuneType(world, x, y, z) == 2 ? 8 : 0;
    }

    @Override
    public boolean isBeaconBase(IBlockAccess worldObj, int x, int y, int z, int beaconX, int beaconY, int beaconZ) {
        return y + 1 == beaconY;
    }

    public static void removeArmor(EntityLiving entity) {
        for (int j = 3; j >= 0; j--) {
            entity.setCurrentItemOrArmor(j + 1, null);
        }
    }

    public void makePigZombieAngry(EntityPigZombie zombie) {
        if (becomeAngryAt == null) {
            try {
                becomeAngryAt = ReflectionHelper.findMethod(EntityPigZombie.class, zombie, new String[]{"becomeAngryAt", "func_70835_c"}, Entity.class);
            } catch (Exception var4) {
                FMLLog.warning("[Automagy] Failed to find zombie pigman anger controls.");
                var4.printStackTrace();
                return;
            }
        }

        try {
            becomeAngryAt.invoke(zombie, (Entity)null);
        } catch (Exception var3) {
            FMLLog.warning("[Automagy] Failed to make zombie pigman angry.");
            var3.printStackTrace();
        }
    }
}
