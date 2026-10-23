package tuhljin.automagy.lib.struct;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class VectorSimple {
    public double x;
    public double y;
    public double z;

    public VectorSimple(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public VectorSimple(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.x = x1 - x2;
        this.y = y1 - y2;
        this.z = z1 - z2;
    }

    public VectorSimple multiply(double d) {
        this.x *= d;
        this.y *= d;
        this.z *= d;
        return this;
    }

    public double mag() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public double magSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public VectorSimple normalize() {
        double d = this.mag();
        if (d != 0.0) {
            this.multiply(1.0 / d);
        }

        return this;
    }

    @Override
    public String toString() {
        MathContext cont = new MathContext(4, RoundingMode.HALF_UP);
        return "VectorSimple(" + new BigDecimal(this.x, cont) + ", " + new BigDecimal(this.y, cont) + ", " + new BigDecimal(this.z, cont) + ")";
    }
}
