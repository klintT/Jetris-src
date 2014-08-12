package net.sourceforge.jetris;
import java.awt.Color;

public abstract class Figure {
    
    public final static int I = 1;
    public final static int T = 2;
    public final static int O = 3;
    public final static int L = 4;
    public final static int J = 5;
    public final static int S = 6;
    public final static int Z = 7;
    
    public final static Color COL_I = Color.RED;
    public final static Color COL_T = Color.GRAY;
    public final static Color COL_O = Color.CYAN;
    public final static Color COL_L = Color.ORANGE;
    public final static Color COL_J = Color.MAGENTA;
    public final static Color COL_S = Color.BLUE;
    public final static Color COL_Z = Color.GREEN;

    public int[] arrX;
    public int[] arrY;
    
    public int offsetX;
    public int offsetY;
    
    public int offsetXLast;
    public int offsetYLast;
    
    public Figure(int[] arrX, int[]arrY) {
        this.arrX = arrX;
        this.arrY = arrY;
        offsetYLast = offsetY = 0;
        offsetXLast = offsetX = 4;
    }
    
    public int getMaxRightOffset() {
        int r = Integer.MIN_VALUE;
        for (int i = 0; i < arrX.length; i++) {
            if(r < arrX[i]) r = arrX[i];
        }
        return r+offsetX;
    }
    
    public void setOffset(int x, int y) {
        offsetXLast = offsetX;
        offsetYLast = offsetY; 
        offsetX = x;
        offsetY = y;
    }
    
    public void resetOffsets() {
        offsetX = offsetY = offsetXLast = offsetYLast = 0;
    }
    
    public abstract void rotationRight();
    
    public abstract void rotationLeft();
    
    public abstract int getGridVal();
    
    public abstract Color getGolor(); 
}
