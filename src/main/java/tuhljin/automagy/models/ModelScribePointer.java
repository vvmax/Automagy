package tuhljin.automagy.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public class ModelScribePointer extends ModelBase {
    ModelRenderer Vertical;
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Base;
    ModelRenderer Base2;

    public ModelScribePointer() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.Vertical = new ModelRenderer(this, 0, 14);
        this.Vertical.addBox(-3.0F, 0.0F, -3.0F, 6, 8, 6);
        this.Vertical.setRotationPoint(0.0F, 14.0F, 0.0F);
        this.Vertical.setTextureSize(64, 32);
        this.Vertical.mirror = true;
        this.setRotation(this.Vertical, 0.0F, 0.0F, 0.0F);
        this.Shape1 = new ModelRenderer(this, 30, 0);
        this.Shape1.addBox(-6.0F, -3.0F, -3.0F, 6, 6, 8);
        this.Shape1.setRotationPoint(3.0F, 18.0F, -1.0F);
        this.Shape1.setTextureSize(64, 32);
        this.Shape1.mirror = true;
        this.setRotation(this.Shape1, 0.0F, 0.0F, 0.0F);
        this.Shape2 = new ModelRenderer(this, 0, 0);
        this.Shape2.addBox(0.0F, -3.0F, -3.0F, 8, 6, 6);
        this.Shape2.setRotationPoint(-4.0F, 18.0F, 0.0F);
        this.Shape2.setTextureSize(64, 32);
        this.Shape2.mirror = true;
        this.setRotation(this.Shape2, 0.0F, 0.0F, 0.0F);
        this.Base = new ModelRenderer(this, 30, 18);
        this.Base.addBox(-3.0F, 0.0F, -3.0F, 5, 1, 5);
        this.Base.setRotationPoint(0.5F, 22.0F, 0.5F);
        this.Base.setTextureSize(64, 32);
        this.Base.mirror = true;
        this.setRotation(this.Base, 0.0F, 0.0F, 0.0F);
        this.Base2 = new ModelRenderer(this, 30, 25);
        this.Base2.addBox(-3.0F, 0.0F, -3.0F, 6, 1, 6);
        this.Base2.setRotationPoint(0.0F, 23.0F, 0.0F);
        this.Base2.setTextureSize(64, 32);
        this.Base2.mirror = true;
        this.setRotation(this.Base2, 0.0F, 0.0F, 0.0F);
    }

    public void renderBase(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        this.Base.render(f5);
        this.Base2.render(f5);
    }

    public void renderCrystalBall() {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        this.Vertical.render(0.0625F);
        this.Shape1.render(0.0625F);
        this.Shape2.render(0.0625F);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
    }
}
