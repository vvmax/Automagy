package tuhljin.automagy.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelNethermind extends ModelBase {
    ModelRenderer BtmRight;
    ModelRenderer BtmLeft;
    ModelRenderer BtmFront;
    ModelRenderer BtmBack;
    ModelRenderer SideLeft;
    ModelRenderer SideRight;
    ModelRenderer SideFront;
    ModelRenderer SideBack;
    ModelRenderer TopRight;
    ModelRenderer TopLeft;
    ModelRenderer TopBack;
    ModelRenderer TopFront;
    ModelRenderer CrossLR;
    ModelRenderer CrossFB;

    public ModelNethermind() {
        this.textureWidth = 128;
        this.textureHeight = 64;
        this.BtmRight = new ModelRenderer(this, 0, 0);
        this.BtmRight.addBox(0.0F, 0.0F, 0.0F, 4, 1, 16);
        this.BtmRight.setRotationPoint(-8.0F, 23.0F, -8.0F);
        this.BtmRight.setTextureSize(128, 64);
        this.BtmRight.mirror = true;
        this.setRotation(this.BtmRight, 0.0F, 0.0F, 0.0F);
        this.BtmLeft = new ModelRenderer(this, 0, 0);
        this.BtmLeft.addBox(0.0F, 0.0F, 0.0F, 4, 1, 16);
        this.BtmLeft.setRotationPoint(4.0F, 23.0F, -8.0F);
        this.BtmLeft.setTextureSize(128, 64);
        this.BtmLeft.mirror = true;
        this.setRotation(this.BtmLeft, 0.0F, 0.0F, 0.0F);
        this.BtmFront = new ModelRenderer(this, 41, 0);
        this.BtmFront.addBox(0.0F, 0.0F, 0.0F, 8, 1, 4);
        this.BtmFront.setRotationPoint(-4.0F, 23.0F, -8.0F);
        this.BtmFront.setTextureSize(128, 64);
        this.BtmFront.mirror = true;
        this.setRotation(this.BtmFront, 0.0F, 0.0F, 0.0F);
        this.BtmBack = new ModelRenderer(this, 41, 0);
        this.BtmBack.addBox(0.0F, 0.0F, 0.0F, 8, 1, 4);
        this.BtmBack.setRotationPoint(-4.0F, 23.0F, 4.0F);
        this.BtmBack.setTextureSize(128, 64);
        this.BtmBack.mirror = true;
        this.setRotation(this.BtmBack, 0.0F, 0.0F, 0.0F);
        this.SideLeft = new ModelRenderer(this, 0, 32);
        this.SideLeft.addBox(0.0F, 0.0F, 0.0F, 2, 14, 16);
        this.SideLeft.setRotationPoint(6.0F, 9.0F, -8.0F);
        this.SideLeft.setTextureSize(128, 64);
        this.SideLeft.mirror = true;
        this.setRotation(this.SideLeft, 0.0F, 0.0F, 0.0F);
        this.SideRight = new ModelRenderer(this, 0, 32);
        this.SideRight.addBox(0.0F, 0.0F, 0.0F, 2, 14, 16);
        this.SideRight.setRotationPoint(-8.0F, 9.0F, -8.0F);
        this.SideRight.setTextureSize(128, 64);
        this.SideRight.mirror = true;
        this.setRotation(this.SideRight, 0.0F, 0.0F, 0.0F);
        this.SideFront = new ModelRenderer(this, 41, 7);
        this.SideFront.addBox(0.0F, 0.0F, 0.0F, 12, 14, 2);
        this.SideFront.setRotationPoint(-6.0F, 9.0F, -8.0F);
        this.SideFront.setTextureSize(128, 64);
        this.SideFront.mirror = true;
        this.setRotation(this.SideFront, 0.0F, 0.0F, 0.0F);
        this.SideBack = new ModelRenderer(this, 41, 7);
        this.SideBack.addBox(0.0F, 0.0F, 0.0F, 12, 14, 2);
        this.SideBack.setRotationPoint(-6.0F, 9.0F, 6.0F);
        this.SideBack.setTextureSize(128, 64);
        this.SideBack.mirror = true;
        this.setRotation(this.SideBack, 0.0F, 0.0F, 0.0F);
        this.TopRight = new ModelRenderer(this, 41, 25);
        this.TopRight.addBox(0.0F, 0.0F, 0.0F, 2, 1, 12);
        this.TopRight.setRotationPoint(-6.0F, 10.0F, -6.0F);
        this.TopRight.setTextureSize(128, 64);
        this.TopRight.mirror = true;
        this.setRotation(this.TopRight, 0.0F, 0.0F, 0.0F);
        this.TopLeft = new ModelRenderer(this, 41, 25);
        this.TopLeft.addBox(0.0F, 0.0F, 0.0F, 2, 1, 12);
        this.TopLeft.setRotationPoint(4.0F, 10.0F, -6.0F);
        this.TopLeft.setTextureSize(128, 64);
        this.TopLeft.mirror = true;
        this.setRotation(this.TopLeft, 0.0F, 0.0F, 0.0F);
        this.TopBack = new ModelRenderer(this, 0, 20);
        this.TopBack.addBox(0.0F, 0.0F, 0.0F, 8, 1, 2);
        this.TopBack.setRotationPoint(-4.0F, 10.0F, 4.0F);
        this.TopBack.setTextureSize(128, 64);
        this.TopBack.mirror = true;
        this.setRotation(this.TopBack, 0.0F, 0.0F, 0.0F);
        this.TopFront = new ModelRenderer(this, 0, 20);
        this.TopFront.addBox(0.0F, 0.0F, 0.0F, 8, 1, 2);
        this.TopFront.setRotationPoint(-4.0F, 10.0F, -6.0F);
        this.TopFront.setTextureSize(128, 64);
        this.TopFront.mirror = true;
        this.setRotation(this.TopFront, 0.0F, 0.0F, 0.0F);
        this.CrossLR = new ModelRenderer(this, 0, 25);
        this.CrossLR.addBox(0.0F, 0.0F, 0.0F, 8, 1, 2);
        this.CrossLR.setRotationPoint(-4.0F, 10.0F, -1.0F);
        this.CrossLR.setTextureSize(128, 64);
        this.CrossLR.mirror = true;
        this.setRotation(this.CrossLR, 0.0F, 0.0F, 0.0F);
        this.CrossFB = new ModelRenderer(this, 41, 41);
        this.CrossFB.addBox(0.0F, 0.0F, 0.0F, 2, 1, 8);
        this.CrossFB.setRotationPoint(-1.0F, 10.0F, -4.0F);
        this.CrossFB.setTextureSize(128, 64);
        this.CrossFB.mirror = true;
        this.setRotation(this.CrossFB, 0.0F, 0.0F, 0.0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        this.BtmRight.render(f5);
        this.BtmLeft.render(f5);
        this.BtmFront.render(f5);
        this.BtmBack.render(f5);
        this.SideLeft.render(f5);
        this.SideRight.render(f5);
        this.SideFront.render(f5);
        this.SideBack.render(f5);
        this.TopRight.render(f5);
        this.TopLeft.render(f5);
        this.TopBack.render(f5);
        this.TopFront.render(f5);
        this.CrossLR.render(f5);
        this.CrossFB.render(f5);
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
