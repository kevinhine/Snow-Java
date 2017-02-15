package snow;

import java.awt.*;
import java.awt.image.*;

/**
 * Various Utility Functions
 *
 * @author Kevin
 */
public class Utility {

    /**
     * Overlay the second RGB color over the first
     *
     * @param c1
     * @param c2
     * @return blended color
     */
    public static int blend(int c1, int c2) {
        //System.out.println(String.format("C1:%x C2:%x", c1, c2));
        int alpha1 = (c1 & 0xff000000) >>> 24;
        int red1 = (c1 & 0x00ff0000) >> 16;
        int green1 = (c1 & 0x0000ff00) >> 8;
        int blue1 = c1 & 0x000000ff;

        int alpha2 = (c2 & 0xff000000) >>> 24;
        int red2 = (c2 & 0x00ff0000) >> 16;
        int green2 = (c2 & 0x0000ff00) >> 8;
        int blue2 = c2 & 0x000000ff;

        double blendStrength = (double) alpha2 / 255;

        int red = lerp(red1, red2, blendStrength);
        int green = lerp(green1, green2, blendStrength);
        int blue = lerp(blue1, blue2, blendStrength);

        int blend = alpha1 << 24;
        blend |= red << 16;
        blend |= green << 8;
        blend |= blue;

        return blend;
    }

    /**
     * Clamp the value within the range min, max (inclusive)
     *
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
     *
     * @param img Base image
     * @param c Color to blend
     */
    public static void colorize(BufferedImage img, Color c) {
        int blendColor = c.getRGB();
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                //Read Pixel
                int color = img.getRGB(x, y);
                int alpha = (color & 0xff000000) >> 24;
                if (alpha == 0) {
                    continue; //Don't blend with empty pixels
                }
                int colorized = blend(color, blendColor);
                img.setRGB(x, y, colorized);
            }
        }
    }

    /**
     * Fade image to alpha by percent
     *
     * @param img Base image
     * @param percent bounded 0,1
     */
    public static void fade(BufferedImage img, double percent) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                //Read Pixel
                int color = img.getRGB(x, y);
                int alpha = (color & 0xff000000) >>> 24;
                if (alpha == 0) {
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
     *
     * @param a
     * @param b
     * @param percent bounded 0,1
     * @return interpolation
     */
    public static double lerp(double a, double b, double percent) {
        return a * (1 - percent) + b * percent;
    }

    public static int lerp(int a, int b, double percent) {
        return (int) lerp((double) a, (double) b, percent);
    }

    /**
     * Rotate the image about the center
     *
     * @param img Base image
     * @param radians rotation amount
     * @return rotated image
     */
    public static BufferedImage rotate(BufferedImage img, double radians) {
        BufferedImage temp = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        double centerX = img.getWidth() * 0.5;
        double centerY = img.getHeight() * 0.5;

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                //Calculate new Position
                double xPrime = Math.cos(radians) * (x - centerX) - Math.sin(radians) * (y - centerY) + centerX;
                double yPrime = Math.sin(radians) * (x - centerX) - Math.cos(radians) * (y - centerY) + centerY;

                int color = img.getRGB(x, y);

                //Determine strengths for 4 surrounding pixels
                for (int j = (int) yPrime; j < yPrime + 2; j++) {
                    //Bounds Checking
                    if (j < 0 || j >= img.getHeight()) {
                        continue;
                    }
                    for (int i = (int) xPrime; i < xPrime + 2; i++) {
                        //Bounds Checking
                        if (i < 0 || i >= img.getWidth()) {
                            continue;
                        }

                        //Calculate overlap between current pixel and rotated position
                        double overlap = (Math.abs(xPrime - i) * Math.abs(yPrime - j));

                        int oldColor = temp.getRGB(i, j);
                        int colorPrime = lerp(oldColor, color, overlap); //wrong

                        temp.setRGB(i, j, colorPrime);
                    }
                }
            }
        }
        return temp;
    }
}
