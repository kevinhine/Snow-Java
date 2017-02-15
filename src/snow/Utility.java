package snow;

/**
 * Various Utility Functions
 *
 * @author Kevin
 */
public class Utility {

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

    public static float lerp(float a, float b, double percent) {
        return (float) lerp((double) a, (double) b, percent);
    }

    public static int lerp(int a, int b, double percent) {
        return (int) lerp((double) a, (double) b, percent);
    }
}
