package tuhljin.automagy.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public class ModelScribeTable extends ModelBase {
    ModelRenderer Leg1;
    ModelRenderer Leg2;
    ModelRenderer Leg3;
    ModelRenderer Leg4;
    ModelRenderer Top;
    ModelRenderer SupportRight;
    ModelRenderer SupportBack;
    ModelRenderer SupportLeft;
    ModelRenderer Inkwell;

    public ModelScribeTable() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.Leg1 = new ModelRenderer(this, 0, 16);
        this.Leg1.addBox(0.0F, 0.0F, 0.0F, 4, 12, 4);
        this.Leg1.setRotationPoint(3.0F, 12.0F, 3.0F);
        this.Leg1.setTextureSize(64, 32);
        this.Leg1.mirror = true;
        this.setRotation(this.Leg1, 0.0F, 0.0F, 0.0F);
        this.Leg2 = new ModelRenderer(this, 0, 16);
        this.Leg2.addBox(0.0F, 0.0F, 0.0F, 4, 12, 4);
        this.Leg2.setRotationPoint(-7.0F, 12.0F, -7.0F);
        this.Leg2.setTextureSize(64, 32);
        this.Leg2.mirror = true;
        this.setRotation(this.Leg2, 0.0F, 0.0F, 0.0F);
        this.Leg3 = new ModelRenderer(this, 0, 16);
        this.Leg3.addBox(0.0F, 0.0F, 0.0F, 4, 12, 4);
        this.Leg3.setRotationPoint(-7.0F, 12.0F, 3.0F);
        this.Leg3.setTextureSize(64, 32);
        this.Leg3.mirror = true;
        this.setRotation(this.Leg3, 0.0F, 0.0F, 0.0F);
        this.Leg4 = new ModelRenderer(this, 0, 16);
        this.Leg4.addBox(0.0F, 0.0F, 0.0F, 4, 12, 4);
        this.Leg4.setRotationPoint(3.0F, 12.0F, -7.0F);
        this.Leg4.setTextureSize(64, 32);
        this.Leg4.mirror = true;
        this.setRotation(this.Leg4, 0.0F, 0.0F, 0.0F);
        this.Top = new ModelRenderer(this, 0, 0);
        this.Top.addBox(-8.0F, 0.0F, -8.0F, 16, 4, 16);
        this.Top.setRotationPoint(0.0F, 8.0F, 0.0F);
        this.Top.setTextureSize(64, 32);
        this.Top.mirror = true;
        this.setRotation(this.Top, 0.0F, 0.0F, 0.0F);
        this.SupportRight = new ModelRenderer(this, 18, 24);
        this.SupportRight.addBox(0.0F, 0.0F, -3.0F, 2, 2, 6);
        this.SupportRight.setRotationPoint(4.0F, 19.0F, 0.0F);
        this.SupportRight.setTextureSize(64, 32);
        this.SupportRight.mirror = true;
        this.setRotation(this.SupportRight, 0.0F, 0.0F, 0.0F);
        this.SupportBack = new ModelRenderer(this, 35, 23);
        this.SupportBack.addBox(-3.0F, 0.0F, 0.0F, 6, 2, 2);
        this.SupportBack.setRotationPoint(0.0F, 19.0F, 4.0F);
        this.SupportBack.setTextureSize(64, 32);
        this.SupportBack.mirror = true;
        this.setRotation(this.SupportBack, 0.0F, 0.0F, 0.0F);
        this.SupportLeft = new ModelRenderer(this, 18, 24);
        this.SupportLeft.addBox(0.0F, 0.0F, -3.0F, 2, 2, 6);
        this.SupportLeft.setRotationPoint(-6.0F, 19.0F, 0.0F);
        this.SupportLeft.setTextureSize(64, 32);
        this.SupportLeft.mirror = true;
        this.setRotation(this.SupportLeft, 0.0F, 0.0F, 0.0F);
        this.Inkwell = new ModelRenderer(this, 52, 27);
        this.Inkwell.addBox(-1.5F, -1.0F, -1.5F, 3, 2, 3);
        this.Inkwell.setRotationPoint(-4.5F, 7.0F, 4.5F);
        this.Inkwell.setTextureSize(64, 32);
        this.Inkwell.mirror = true;
        this.setRotation(this.Inkwell, 0.0F, 0.0F, 0.0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        this.Leg1.render(f5);
        this.Leg2.render(f5);
        this.Leg3.render(f5);
        this.Leg4.render(f5);
        this.Top.render(f5);
        this.SupportRight.render(f5);
        this.SupportBack.render(f5);
        this.SupportLeft.render(f5);
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

    public void renderInkwell() {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        this.Inkwell.render(0.0625F);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }
}
