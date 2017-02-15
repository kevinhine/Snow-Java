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

    private static final double MIN_SPEED_MODIFIER = 0.4;
    private static final double MAX_SPEED_MODIFIER = 1;

    private static final double MAX_MULTIPLIER = 2;
    private static final double RETURN_RATE = 0.01;

    private static final BufferedImage[] TEXTURES;
    private static final Color TINT = new Color(142, 230, 255);

    /**
     * Preload Textures
     */
    static {
        ArrayList<BufferedImage> temp = new ArrayList<>();
        for (int i = 0; true; i++) {
            try {
                System.out.println(i);
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

    private int index;
    private double tintRatio;

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
        //Calculate Depth with placeholder x,y
        position = new Point3D(0, 0, Math.random());

        index = (int) (Math.random() * TEXTURES.length);
        tintRatio = Utility.lerp(0.25, 1, Math.random());

        //Convert from center to top left for drawing, Depth is randomized
        position = new Point3D(centerX - TEXTURES[index].getWidth() / 2, centerY - TEXTURES[index].getHeight() / 2, Math.random());
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
        strengthMultiplier = Utility.clamp(1, MAX_MULTIPLIER, strengthMultiplier - RETURN_RATE);
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
        double parallax = Utility.lerp(MIN_SPEED_MODIFIER, MAX_SPEED_MODIFIER, position.getZ()); //0 is far away (slower), 1 is close (faster)
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
     * Default hash
     *
     * @return hash
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(position);
        hash = 37 * hash + Objects.hashCode(index);
        return hash;
    }

    /**
     * Render the Snowflake
     *
     * @param g
     */
    public void paint(Graphics g) {
        BufferedImage buffer = new BufferedImage(TEXTURES[index].getWidth(), TEXTURES[index].getHeight(), TEXTURES[index].getType());
        Graphics2D g2dBuffer = buffer.createGraphics();

        //Mask
        g2dBuffer.drawImage(TEXTURES[index], 0, 0, null);

        //Tint
        g2dBuffer.setColor(TINT);
        g2dBuffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (float) tintRatio));
        g2dBuffer.fillRect(0, 0, TEXTURES[index].getWidth(), TEXTURES[index].getHeight());

        g2dBuffer.dispose();

        //Z Depth Fade
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (float) position.getZ()));

        g.drawImage(buffer, (int) position.getX(), (int) position.getY(), null);
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
