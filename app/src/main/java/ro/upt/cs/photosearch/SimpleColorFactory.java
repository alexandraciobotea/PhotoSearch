package ro.upt.cs.photosearch;

import android.graphics.Color;

/**
 * A simple color factory
 */
public class SimpleColorFactory {
    /**
     * The colors that this factory can provide
     */
    private static final int[] colors = {
           // Color.BLACK,
            Color.RED,
            Color.YELLOW,
            Color.GREEN,
            Color.BLUE
    };

    /**
     * The index of the next color int the colors array
     */
    private int nextIndex = 0;

    /**
     * Get the next color
     */
    public int getNextColor() {
        int color = colors[nextIndex];
        nextIndex = (nextIndex + 1) % colors.length;
        return color;
    }

    public int getColorForIndex(int index) {
        int i = Math.abs(index) % colors.length;
        return colors[i];
    }
}
