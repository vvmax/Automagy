package tuhljin.automagy.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelThirstyTank extends ModelBase {
    ModelRenderer Bottom;
    ModelRenderer FrontRight;
    ModelRenderer BackRight;
    ModelRenderer BackLeft;
    ModelRenderer FrontLeft;
    ModelRenderer BackRight2;
    ModelRenderer FrontRight2;
    ModelRenderer FrontLeft2;
    ModelRenderer BackLeft2;
    ModelRenderer Center;

    public ModelThirstyTank() {
        this.textureWidth = 128;
        this.textureHeight = 64;
        this.Bottom = new ModelRenderer(this, 0, 0);
        this.Bottom.addBox(-8.0F, 0.0F, -8.0F, 16, 2, 16);
        this.Bottom.setRotationPoint(0.0F, 22.0F, 0.0F);
        this.Bottom.setTextureSize(128, 64);
        this.Bottom.mirror = true;
        this.setRotation(this.Bottom, 0.0F, 0.0F, 0.0F);
        this.FrontRight = new ModelRenderer(this, 0, 32);
        this.FrontRight.addBox(0.0F, 0.0F, 0.0F, 4, 14, 2);
        this.FrontRight.setRotationPoint(4.0F, 8.0F, -8.0F);
        this.FrontRight.setTextureSize(128, 64);
        this.FrontRight.mirror = true;
        this.setRotation(this.FrontRight, 0.0F, 0.0F, 0.0F);
        this.BackRight = new ModelRenderer(this, 0, 32);
        this.BackRight.addBox(0.0F, 0.0F, 0.0F, 4, 14, 2);
        this.BackRight.setRotationPoint(4.0F, 8.0F, 6.0F);
        this.BackRight.setTextureSize(128, 64);
        this.BackRight.mirror = true;
        this.setRotation(this.BackRight, 0.0F, 0.0F, 0.0F);
        this.BackLeft = new ModelRenderer(this, 0, 32);
        this.BackLeft.mirror = true;
        this.BackLeft.addBox(0.0F, 0.0F, 0.0F, 4, 14, 2);
        this.BackLeft.setRotationPoint(-8.0F, 8.0F, 6.0F);
        this.BackLeft.setTextureSize(128, 64);
        this.BackLeft.mirror = true;
        this.setRotation(this.BackLeft, 0.0F, 0.0F, 0.0F);
        this.BackLeft.mirror = false;
        this.FrontLeft = new ModelRenderer(this, 0, 32);
        this.FrontLeft.mirror = true;
        this.FrontLeft.addBox(0.0F, 0.0F, 0.0F, 4, 14, 2);
        this.FrontLeft.setRotationPoint(-8.0F, 8.0F, -8.0F);
        this.FrontLeft.setTextureSize(128, 64);
        this.FrontLeft.mirror = true;
        this.setRotation(this.FrontLeft, 0.0F, 0.0F, 0.0F);
        this.FrontLeft.mirror = false;
        this.BackRight2 = new ModelRenderer(this, 13, 32);
        this.BackRight2.addBox(0.0F, 0.0F, 0.0F, 2, 14, 3);
        this.BackRight2.setRotationPoint(6.0F, 8.0F, 3.0F);
        this.BackRight2.setTextureSize(128, 64);
        this.BackRight2.mirror = true;
        this.setRotation(this.BackRight2, 0.0F, 0.0F, 0.0F);
        this.FrontRight2 = new ModelRenderer(this, 13, 32);
        this.FrontRight2.addBox(0.0F, 0.0F, 0.0F, 2, 14, 3);
        this.FrontRight2.setRotationPoint(6.0F, 8.0F, -6.0F);
        this.FrontRight2.setTextureSize(128, 64);
        this.FrontRight2.mirror = true;
        this.setRotation(this.FrontRight2, 0.0F, 0.0F, 0.0F);
        this.FrontLeft2 = new ModelRenderer(this, 13, 32);
        this.FrontLeft2.mirror = true;
        this.FrontLeft2.addBox(0.0F, 0.0F, 0.0F, 2, 14, 3);
        this.FrontLeft2.setRotationPoint(-8.0F, 8.0F, -6.0F);
        this.FrontLeft2.setTextureSize(128, 64);
        this.FrontLeft2.mirror = true;
        this.setRotation(this.FrontLeft2, 0.0F, 0.0F, 0.0F);
        this.FrontLeft2.mirror = false;
        this.BackLeft2 = new ModelRenderer(this, 13, 32);
        this.BackLeft2.mirror = true;
        this.BackLeft2.addBox(0.0F, 0.0F, 0.0F, 2, 14, 3);
        this.BackLeft2.setRotationPoint(-8.0F, 8.0F, 3.0F);
        this.BackLeft2.setTextureSize(128, 64);
        this.BackLeft2.mirror = true;
        this.setRotation(this.BackLeft2, 0.0F, 0.0F, 0.0F);
        this.BackLeft2.mirror = false;
        this.Center = new ModelRenderer(this, 28, 32);
        this.Center.addBox(-6.0F, 0.0F, -6.0F, 12, 14, 12);
        this.Center.setRotationPoint(0.0F, 8.0F, 0.0F);
        this.Center.setTextureSize(128, 64);
        this.Center.mirror = true;
        this.setRotation(this.Center, 0.0F, 0.0F, 0.0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        this.Bottom.render(f5);
        this.FrontRight.render(f5);
        this.BackRight.render(f5);
        this.BackLeft.render(f5);
        this.FrontLeft.render(f5);
        this.BackRight2.render(f5);
        this.FrontRight2.render(f5);
        this.FrontLeft2.render(f5);
        this.BackLeft2.render(f5);
        this.Center.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    }

    public void renderStone(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        this.Bottom.render(f5);
        this.FrontRight.render(f5);
        this.BackRight.render(f5);
        this.BackLeft.render(f5);
        this.FrontLeft.render(f5);
        this.BackRight2.render(f5);
        this.FrontRight2.render(f5);
        this.FrontLeft2.render(f5);
        this.BackLeft2.render(f5);
    }

    public void renderGlass(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        this.Center.render(f5);
    }
}
