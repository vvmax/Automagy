package tuhljin.automagy.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelThaumostaticPylon extends ModelBase {
    ModelRenderer Central;
    ModelRenderer BackRight;
    ModelRenderer BackLeft;
    ModelRenderer FrontLeft;
    ModelRenderer FrontRight;

    public ModelThaumostaticPylon() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.Central = new ModelRenderer(this, 20, 0);
        this.Central.addBox(-5.0F, 0.0F, -5.0F, 10, 16, 10);
        this.Central.setRotationPoint(0.0F, 8.0F, 0.0F);
        this.Central.setTextureSize(64, 32);
        this.Central.mirror = true;
        this.setRotation(this.Central, 0.0F, 0.0F, 0.0F);
        this.BackRight = new ModelRenderer(this, 0, 0);
        this.BackRight.addBox(-2.0F, 0.0F, -2.0F, 4, 16, 4);
        this.BackRight.setRotationPoint(6.0F, 8.0F, 6.0F);
        this.BackRight.setTextureSize(64, 32);
        this.BackRight.mirror = true;
        this.setRotation(this.BackRight, 0.0F, 0.0F, 0.0F);
        this.BackLeft = new ModelRenderer(this, 0, 0);
        this.BackLeft.addBox(-2.0F, 0.0F, -2.0F, 4, 16, 4);
        this.BackLeft.setRotationPoint(-6.0F, 8.0F, 6.0F);
        this.BackLeft.setTextureSize(64, 32);
        this.BackLeft.mirror = true;
        this.setRotation(this.BackLeft, 0.0F, 0.0F, 0.0F);
        this.BackLeft.mirror = false;
        this.FrontLeft = new ModelRenderer(this, 0, 0);
        this.FrontLeft.addBox(-2.0F, 0.0F, -2.0F, 4, 16, 4);
        this.FrontLeft.setRotationPoint(-6.0F, 8.0F, -6.0F);
        this.FrontLeft.setTextureSize(64, 32);
        this.FrontLeft.mirror = true;
        this.setRotation(this.FrontLeft, 0.0F, 0.0F, 0.0F);
        this.FrontRight = new ModelRenderer(this, 0, 0);
        this.FrontRight.addBox(-2.0F, 0.0F, -2.0F, 4, 16, 4);
        this.FrontRight.setRotationPoint(6.0F, 8.0F, -6.0F);
        this.FrontRight.setTextureSize(64, 32);
        this.FrontRight.mirror = true;
        this.setRotation(this.FrontRight, 0.0F, 0.0F, 0.0F);
        this.FrontRight.mirror = false;
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        this.Central.render(f5);
        this.BackRight.render(f5);
        this.BackLeft.render(f5);
        this.FrontLeft.render(f5);
        this.FrontRight.render(f5);
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
}
