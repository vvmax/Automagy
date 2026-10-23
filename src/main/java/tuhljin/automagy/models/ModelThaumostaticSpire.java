package tuhljin.automagy.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelThaumostaticSpire extends ModelBase {
    ModelRenderer Base;
    ModelRenderer Layer2;
    ModelRenderer Layer3;
    ModelRenderer Layer4;
    ModelRenderer Layer5;
    ModelRenderer Top;

    public ModelThaumostaticSpire() {
        this.textureWidth = 128;
        this.textureHeight = 64;
        this.Base = new ModelRenderer(this, 0, 0);
        this.Base.addBox(-8.0F, 0.0F, -8.0F, 16, 2, 16);
        this.Base.setRotationPoint(0.0F, 22.0F, 0.0F);
        this.Base.setTextureSize(128, 64);
        this.Base.mirror = true;
        this.setRotation(this.Base, 0.0F, 0.0F, 0.0F);
        this.Layer2 = new ModelRenderer(this, 0, 20);
        this.Layer2.addBox(-7.0F, 0.0F, -7.0F, 14, 2, 14);
        this.Layer2.setRotationPoint(0.0F, 20.0F, 0.0F);
        this.Layer2.setTextureSize(128, 64);
        this.Layer2.mirror = true;
        this.setRotation(this.Layer2, 0.0F, 0.0F, 0.0F);
        this.Layer3 = new ModelRenderer(this, 0, 38);
        this.Layer3.addBox(-5.0F, 0.0F, -5.0F, 10, 6, 10);
        this.Layer3.setRotationPoint(0.0F, 14.0F, 0.0F);
        this.Layer3.setTextureSize(128, 64);
        this.Layer3.mirror = true;
        this.setRotation(this.Layer3, 0.0F, 0.0F, 0.0F);
        this.Layer4 = new ModelRenderer(this, 41, 38);
        this.Layer4.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6);
        this.Layer4.setRotationPoint(0.0F, 5.0F, 0.0F);
        this.Layer4.setTextureSize(128, 64);
        this.Layer4.mirror = true;
        this.setRotation(this.Layer4, 0.0F, 0.0F, 0.0F);
        this.Layer5 = new ModelRenderer(this, 57, 34);
        this.Layer5.addBox(0.0F, 0.0F, 0.0F, 2, 1, 2);
        this.Layer5.setRotationPoint(-1.0F, 4.0F, -1.0F);
        this.Layer5.setTextureSize(128, 64);
        this.Layer5.mirror = true;
        this.setRotation(this.Layer5, 0.0F, 0.0F, 0.0F);
        this.Top = new ModelRenderer(this, 58, 20);
        this.Top.addBox(-0.5F, 0.0F, -0.5F, 1, 12, 1);
        this.Top.setRotationPoint(0.0F, -8.0F, 0.0F);
        this.Top.setTextureSize(128, 64);
        this.Top.mirror = true;
        this.setRotation(this.Top, 0.0F, 0.0F, 0.0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        this.Base.render(f5);
        this.Layer2.render(f5);
        this.Layer3.render(f5);
        this.Layer4.render(f5);
        this.Layer5.render(f5);
        this.Top.render(f5);
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
