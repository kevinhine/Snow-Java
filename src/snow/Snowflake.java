package snow;

import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Objects;
import javafx.geometry.Point3D;
import javax.imageio.ImageIO;

/*
 * Snowflakes are not being destroyed correctly
 */
/**
 * Snowflake that gently wafts and falls
 *
 * @author Kevin Hine
 */
public class Snowflake {

    private static final double FALL_SPEED = 100;

    private static final double DRIFT_SPEED = FALL_SPEED / 4;
    private static final double MAX_ANGLE_DELTA = Math.PI / 16;

    private static final double MIN_PARALLAX = 0.4;
    private static final double MAX_PARALLAX = 1;

    private static final double MAX_STRENGTH = 2;
    private static final double RETURN_RATE = 0.01;

    private static final BufferedImage[] TEXTURES;
    private static final Color HUE = new Color(142, 230, 255);
    private static final float MIN_TINT = 0.25f;
    private static final float MAX_TINT = 1f;

    /**
     * Preload Textures
     */
    static {
        ArrayList<BufferedImage> temp = new ArrayList<>();
        for (int i = 0; true; i++) {
            try {
                temp.add(ImageIO.read(Snowflake.class.getResource("resources/snowflake" + i + ".png")));
            } catch (Exception e) {
                break; //Stop reading when Run out of Files
            }
        }
        TEXTURES = temp.toArray(new BufferedImage[0]);
    }

    //Used as a Vector that affects the Falling Trajectory
    private double driftAngle = Math.random() * Math.PI * 2;
    private double strengthMultiplier = 1;

    /**
     * Snowflake position x,y are screen coordinates z is draw depth (higher
     * values are displayed on top)
     */
    private Point3D position;
    private BufferedImage texture;
    private double xDrift;
    private double yDrift;

    //Scale based on depth?
//    private static final int MAX_SIZE = 20;
    /**
     * Create a snowflake
     *
     * @param centerX
     * @param centerY
     */
    public Snowflake(double centerX, double centerY) {
        generateTexture();
        //Convert from center to top left for drawing, Depth is randomized
        position = new Point3D(centerX - texture.getWidth() / 2,
                centerY - texture.getHeight() / 2, Math.random());
    }

    /**
     * Continously update drift vector
     */
    public void drift() {
        double angleDelta = Utility.lerp(-MAX_ANGLE_DELTA, MAX_ANGLE_DELTA, Math.random());
        driftAngle += angleDelta;
        //Convert angle to Vector
        xDrift = DRIFT_SPEED * strengthMultiplier * Math.cos(driftAngle);
        yDrift = DRIFT_SPEED * strengthMultiplier * Math.sin(driftAngle);
        //Reduce Strength Multiplier
        strengthMultiplier = Utility.clamp(1, MAX_STRENGTH, strengthMultiplier - RETURN_RATE);
    }

    /**
     * Default comparison
     *
     * @param o object to be compared
     * @return true if the objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Snowflake) {
            Snowflake obj = (Snowflake) o;
            return position == obj.position;
        }
        return false;
    }

    /**
     * Cause the snowflake to fall to the ground
     *
     * @return snowflake location
     */
    public Point3D fall() {
        //Update Position
        drift();
        //0 is far away (slower), 1 is close (faster)
        double parallax = Utility.lerp(MIN_PARALLAX, MAX_PARALLAX, position.getZ());
        Point3D delta = new Point3D(xDrift, FALL_SPEED + yDrift, 0);
        delta = delta.multiply(parallax * DisplayCanvas.FRAME_DELTA_TIME);
        position = position.add(delta);
        return position;
    }

    /**
     * Location accessor
     *
     * @return Snowflake Location
     */
    public Point3D getPosition() {
        return position;
    }

    /**
     * Generate Colorized Texture
     */
    private void generateTexture() {
        int index = (int) (Math.random() * TEXTURES.length);
        float tintStrength = Utility.lerp(MIN_TINT, MAX_TINT, Math.random());
        texture = new BufferedImage(TEXTURES[index].getWidth(),
                TEXTURES[index].getHeight(), TEXTURES[index].getType());
        Graphics2D g2d = texture.createGraphics();

        //Mask
        g2d.drawImage(TEXTURES[index], 0, 0, null);

        //Tint
        g2d.setColor(HUE);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, tintStrength));
        g2d.fillRect(0, 0, texture.getWidth(), texture.getHeight());

        g2d.dispose();
    }

    /**
     * Default hash
     *
     * @return hash
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(position);
        hash = 37 * hash + Objects.hashCode(texture);
        return hash;
    }

    /**
     * Render the Snowflake
     *
     * @param g
     */
    public void paint(Graphics g) {
        //Z Depth Fade
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (float) position.getZ()));

        g.drawImage(texture, (int) position.getX(), (int) position.getY(), null);
    }

    /**
     * Allow the manual adjustment of the drift angle
     *
     * @param driftAngle (radians)
     * @param strengthMultiplier strength of gust relative to base currents
     */
    public void gust(double driftAngle, double strengthMultiplier) {
        this.driftAngle = driftAngle;
        this.strengthMultiplier = strengthMultiplier;
    }
}
