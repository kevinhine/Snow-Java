package snow;

import java.awt.*;
import java.awt.image.*;

/**
 * Various Utility Functions
 * @author Kevin
 */
public class Utility {

    /**
     * Overlay the second RGB color over the first
     * @param c1
     * @param c2
     * @return blended color
     */
    public static int blend(int c1, int c2) {
        //System.out.println(String.format("C1:%x C2:%x", c1, c2));
        int alpha1 = (c1 & 0xff000000) >>> 24;
        int red1   = (c1 & 0x00ff0000) >> 16;
        int green1 = (c1 & 0x0000ff00) >> 8;
        int blue1  =  c1 & 0x000000ff;

        int alpha2 = (c2 & 0xff000000) >>> 24;
        int red2   = (c2 & 0x00ff0000) >> 16;
        int green2 = (c2 & 0x0000ff00) >> 8;
        int blue2  =  c2 & 0x000000ff;

        double blendStrength = (double)alpha2/255;

        int red   = lerp(red1,   red2,   blendStrength);
        int green = lerp(green1, green2, blendStrength);
        int blue  = lerp(blue1,  blue2,  blendStrength);

        int blend = alpha1 << 24;
        blend |= red << 16;
        blend |= green << 8;
        blend |= blue;

        return blend;
    }

    /**
     * Clamp the value within the range min, max (inclusive)
     * @param min
     * @param max
     * @param val
     * @return clamped value
     */
    public static double clamp(double min, double max, double val) {
        return val < min ? min : val > max ? max : val;
    }

    /**
     * Colorize image
     * @param img Base image
     * @param c Color to blend
     */
    public static void colorize(BufferedImage img, Color c) {
        int blendColor = c.getRGB();
        for(int y = 0; y < img.getHeight(); y++) {
            for(int x = 0; x < img.getWidth(); x++) {
                //Read Pixel
                int color = img.getRGB(x, y);
                int alpha = (color & 0xff000000) >> 24;
                if(alpha == 0) {
                    continue; //Don't blend with empty pixels
                }
                int colorized = blend(color, blendColor);
                img.setRGB(x, y, colorized);
            }
        }
    }

    /**
     * Fade image to alpha by percent
     * @param img Base image
     * @param percent bounded 0,1
     */
    public static void fade(BufferedImage img, double percent) {
        for(int y = 0; y < img.getHeight(); y++) {
            for(int x = 0; x < img.getWidth(); x++) {
                //Read Pixel
                int color = img.getRGB(x, y);
                int alpha = (color & 0xff000000) >>> 24;
                if(alpha == 0) {
                    continue; //Don't set alpha of empty pixels
                }
                //Blend between full alpha and current by percent
                int alphaMask = lerp(alpha, 0, percent) << 24;

                int faded = alphaMask | (color & 0x00ffffff);
                img.setRGB(x, y, faded);
                //System.out.println(String.format("C:%x", color));
            }
        }
    }

    /**
     * Linearly interpolate between a and b by percent
     * @param a
     * @param b
     * @param percent bounded 0,1
     * @return interpolation
     */
    public static double lerp(double a, double b, double percent) {
        return a * (1 - percent) + b * percent;
    }

    /**
     * Linearly interpolate between a and b by percent
     * @param a
     * @param b
     * @param percent bounded 0,1
     * @return interpolation
     */
    public static int lerp(int a, int b, double percent) {
        return (int)lerp((double)a, (double)b, percent);
    }
}
