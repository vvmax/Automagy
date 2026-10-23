package tuhljin.automagy.lib.compat.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IStackPositioner;
import java.util.ArrayList;

public class SimpleStackPositioner implements IStackPositioner {
    public int left;
    public int top;
    public int xMargin;
    public int yMargin;
    public int offsetx;
    public int offsety;
    public int divisor;

    public SimpleStackPositioner(int left, int top, int xMargin, int yMargin, int offsetX, int offsetY, int divisor) {
        this.left = left;
        this.top = top;
        this.xMargin = xMargin;
        this.yMargin = yMargin;
        this.offsetx = offsetX;
        this.offsety = offsetY;
        this.divisor = divisor;
    }

    public SimpleStackPositioner(int left, int top, int xMargin, int yMargin) {
        this(left, top, xMargin, yMargin, 25, 6, 18);
    }

    public ArrayList<PositionedStack> positionStacks(ArrayList<PositionedStack> ai) {
        for (PositionedStack stack : ai) {
            if (stack != null) {
                int column = (stack.relx - this.offsetx) / this.divisor;
                int row = (stack.rely - this.offsety) / this.divisor;
                stack.relx = this.left + column * this.xMargin;
                stack.rely = this.top + row * this.yMargin;
            }
        }

        return ai;
    }
}
