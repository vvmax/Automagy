package tuhljin.automagy.particles;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import thaumcraft.client.fx.particles.FXDrop;
import thaumcraft.common.config.ConfigBlocks;
import tuhljin.automagy.blocks.ModBlocks;
import tuhljin.automagy.codechicken.lib.math.MathHelper;
import tuhljin.automagy.lib.ThaumcraftExtension;

public class FXDropXP extends FXDrop {
    private int startY = MathHelper.floor_double(this.posY);

    public FXDropXP(World par1World, double par2, double par4, double par6, float r, float g, float b) {
        super(par1World, par2, par4, par6, r, g, b);
    }

    @Override
    public void moveEntity(double par1, double par3, double par5) {
        int x = MathHelper.floor_double(this.posX);
        int y = MathHelper.floor_double(this.posY);
        int z = MathHelper.floor_double(this.posZ);
        boolean inJar = this.jarAtPosition(x, y, z);
        if (this.noClip || inJar || this.startY - y < 1 && this.jarAtPosition(x, y - 1, z)) {
            this.boundingBox.offset(par1, par3, par5);
            this.posX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0;
            this.posY = this.boundingBox.minY + this.yOffset - this.yOffset2;
            this.posZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0;
            if (inJar && this.posY - y <= 0.15) {
                this.posX = this.prevPosX;
                this.posY = y + 0.15;
                this.posZ = this.prevPosZ;
                this.motionY = 0.0;
                this.onGround = true;
                this.particleGravity = 0.0F;
            }
        } else {
            super.moveEntity(par1, par3, par5);
        }
    }

    protected boolean jarAtPosition(int x, int y, int z) {
        Block block = this.worldObj.getBlock(x, y, z);
        return block == ConfigBlocks.blockJar || block == ModBlocks.jarExperience;
    }

    @Override
    public void setDead() {
        if (this.particleMaxAge < 1) {
            super.setDead();
        } else {
            int y = MathHelper.floor_double(this.posY);
            if (y == this.startY) {
                int x = MathHelper.floor_double(this.posX);
                int z = MathHelper.floor_double(this.posZ);
                if (!ThaumcraftExtension.blockIsItemGrate(this.worldObj, x, y, z, false)) {
                    super.setDead();
                }
            } else {
                super.setDead();
            }
        }
    }
}
