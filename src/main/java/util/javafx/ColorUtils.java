package util.javafx;

import javafx.scene.paint.Color;

/**
 * Utility-class with functions for JFX colors.
 *
 * @see Color
 */
public class ColorUtils {
    public static String toWebString(Color color) {
        int red = (int) Math.round(color.getRed() * 255.0);
        int green = (int) Math.round(color.getGreen() * 255.0);
        int blue = (int) Math.round(color.getBlue() * 255.0);

        String raw = String.format("#%2s%2s%2s", Integer.toHexString(red).toUpperCase(),
                Integer.toHexString(green).toUpperCase(),
                Integer.toHexString(blue).toUpperCase());
        String result = raw.replace(' ', '0');
        return result;
    }
}
