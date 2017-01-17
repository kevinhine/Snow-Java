package snow;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Objects;
import javafx.geometry.Point3D;
import javax.imageio.ImageIO;

/*
 * Snowflakes are not being destroyed correctly
 */

/**
 * Snowflake that gently wafts and falls
 * @author Kevin Hine
 */
public class Snowflake {
    private static final double FALL_SPEED = 100;

    private static final double DRIFT_SPEED = FALL_SPEED / 4;
    private static final double MAX_ANGLE_DELTA = Math.PI / 16;

    private static final double MAX_SPEED_MODIFIER = 1;
    private static final double MIN_SPEED_MODIFIER = 0.4;

    private static final int NUM_SNOWFLAKE_TYPES;

    /**
     * Calculate the number of snowflake textures
     * (must be sequential)
     */
    static {
        int index = 0;
        while(Snowflake.class.getResource("resources/snowflake" + index + ".png") != null) {
            index++;
        }
        NUM_SNOWFLAKE_TYPES = index;
    }

    //Used as a Vector that affects the Falling Trajectory
    private double driftAngle = Math.random() * Math.PI * 2;

    /**
     * Snowflake position
     * x,y are screen coordinates
     * z is draw depth (higher values are displayed on top)
     */
    private Point3D position;
    private BufferedImage texture;
    private double xDrift;
    private double yDrift;

    //Scale based on depth?
//    private static final int MAX_SIZE = 20;

    /**
     * Create a snowflake
     * @param centerX
     * @param centerY
     */
    public Snowflake(double centerX, double centerY) {
        //Calculate Depth with placeholder x,y
        position = new Point3D(0, 0, Math.random());
        loadTexture();
        UpdateTexture();
        //Convert from center to top left for drawing
        position = position.add(centerX - texture.getWidth()/2, centerY - texture.getHeight()/2, 0);
    }

    /**
     * Colorize the snowflake texture
     */
    private void UpdateTexture() {
        //Colorize
        int alpha = (int)(Math.random() * 256);
        Color tint = new Color(142, 230, 255, alpha);
        Utility.colorize(texture, tint);
        //Depth
        Utility.fade(texture, 1 - position.getZ()); //0 is far away, 1 is close
    }

    /**
     * Continously update drift vector
     */
    public void drift() {
        double angleDelta = Utility.lerp(-MAX_ANGLE_DELTA, MAX_ANGLE_DELTA, Math.random());
        driftAngle += angleDelta;
        //Convert angle to Vector
        xDrift = DRIFT_SPEED * Math.cos(driftAngle);
        yDrift = DRIFT_SPEED * Math.sin(driftAngle);
    }

    /**
     * Default comparison
     * @param o object to be compared
     * @return true if the objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof Snowflake) {
            Snowflake obj = (Snowflake)o;
            return position == obj.position;
        }
        return false;
    }

    /**
     * Cause the snowflake to fall to the ground
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
     * Default hash
     * @return hash
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.position);
        hash = 37 * hash + Objects.hashCode(this.texture);
        return hash;
    }

    /**
     * Choose a snowflake texture
     */
    private void loadTexture() {
        int index = (int)(Math.random() * NUM_SNOWFLAKE_TYPES);
        try {
            texture = ImageIO.read(getClass().getResource("resources/snowflake" + index + ".png"));
        } catch(IOException e) {
            System.err.println(e); //Should not throw any errors as files are initially checked
        }
    }

    /**
     * Render the Snowflake
     * @param g
     */
    public void paint(Graphics g) {
        g.drawImage(texture, (int)position.getX(), (int)position.getY(), null);
    }
}
