package tuhljin.automagy.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCrystalBrainPedestal extends ModelBase {
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    public ModelRenderer shapeTip;

    public ModelCrystalBrainPedestal() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.Shape1 = new ModelRenderer(this, 0, 0);
        this.Shape1.addBox(-4.0F, 0.0F, -4.0F, 8, 2, 8);
        this.Shape1.setRotationPoint(0.0F, -2.0F, 0.0F);
        this.Shape1.setTextureSize(64, 32);
        this.Shape1.mirror = true;
        this.setRotation(this.Shape1, 0.0F, 0.0F, 0.0F);
        this.Shape2 = new ModelRenderer(this, 0, 11);
        this.Shape2.addBox(-1.5F, -5.0F, -1.5F, 3, 6, 3);
        this.Shape2.setRotationPoint(0.0F, -3.0F, 0.0F);
        this.Shape2.setTextureSize(64, 32);
        this.Shape2.mirror = true;
        this.setRotation(this.Shape2, 0.0F, 0.0F, 0.0F);
        this.Shape3 = new ModelRenderer(this, 33, 0);
        this.Shape3.addBox(-2.0F, 0.0F, -2.0F, 4, 1, 4);
        this.Shape3.setRotationPoint(0.0F, -9.0F, 0.0F);
        this.Shape3.setTextureSize(64, 32);
        this.Shape3.mirror = true;
        this.setRotation(this.Shape3, 0.0F, 0.0F, 0.0F);
        this.shapeTip = new ModelRenderer(this, 34, 6);
        this.shapeTip.addBox(-0.5F, 0.0F, -0.5F, 1, 1, 1);
        this.shapeTip.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.shapeTip.setTextureSize(64, 32);
        this.shapeTip.mirror = true;
        this.setRotation(this.shapeTip, 0.0F, 0.0F, 0.0F);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, boolean renderTip) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        this.Shape1.render(f5);
        this.Shape2.render(f5);
        this.Shape3.render(f5);
        if (renderTip) {
            this.shapeTip.render(f5);
        }
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.render(entity, f, f1, f2, f3, f4, f5, true);
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
